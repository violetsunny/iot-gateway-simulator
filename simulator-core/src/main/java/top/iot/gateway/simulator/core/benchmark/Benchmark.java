package top.iot.gateway.simulator.core.benchmark;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import top.iot.gateway.simulator.core.Connection;
import top.iot.gateway.simulator.core.ConnectionManager;
import top.iot.gateway.simulator.core.ExceptionUtils;
import top.iot.gateway.simulator.core.report.Reporter;
import top.iot.gateway.simulator.core.script.Script;
import top.iot.gateway.simulator.core.script.ScriptFactory;
import top.iot.gateway.simulator.core.script.Scripts;
import org.joda.time.DateTime;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class Benchmark implements Disposable, BenchmarkHelper {

    public static final String REPORT_CONNECTING = "connecting";

    public static final ScriptFactory scriptFactory = Scripts.getFactory("js");

    @Getter
    private final String name;

    @Getter
    private final BenchmarkOptions options;

    private final Reporter reporter;

    @Getter
    private final ConnectionManager connectionManager;

    private final Function<ConnectCreateContext, Mono<? extends Connection>> connectionFactory;

    private final List<BiConsumer<Integer, Object>> beforeConnectHandler = new CopyOnWriteArrayList<>();
    private final List<Consumer<Connection>> connectionHandler = new CopyOnWriteArrayList<>();
    private final List<Runnable> completeHandler = new CopyOnWriteArrayList<>();

    private final Disposable.Composite disposable = Disposables.composite();

    private Disposable.Composite reloadable = Disposables.composite();

    private final Set<String> errors = ConcurrentHashMap.newKeySet();

    private final Deque<String> logs = new ConcurrentLinkedDeque<>();

    private final Deque<Snapshot> snapshots = new ConcurrentLinkedDeque<>();


    @Getter
    private Throwable lastError;


    public Benchmark(String name,
                     BenchmarkOptions options,
                     Reporter reporter,
                     ConnectionManager connectionManager,
                     Function<ConnectCreateContext, Mono<? extends Connection>> connectionFactory) {
        this.name = name;
        this.options = options;
        this.reporter = reporter;
        this.connectionManager = connectionManager;
        this.connectionFactory = connectionFactory;
    }

    public static Benchmark create(String name,
                                   BenchmarkOptions options,
                                   ConnectionManager connectionManager,
                                   Function<ConnectCreateContext, Mono<? extends Connection>> connectionFactory) {
        return new Benchmark(name, options, Reporter.create(), connectionManager, connectionFactory);
    }

    public Reporter getReporter() {
        return reporter;
    }

    public Deque<String> getLogs() {
        return logs;
    }

    public void start() {
        if (disposable.size() > 0) {
            return;
        }

        if (options.getFile() != null) {
            executeScript(Paths.get(options.getFile().toURI()));
        }
        disposable.add(
                Flux.interval(Duration.ofSeconds(1))
                        .flatMap(ignore -> this.snapshot())
                        .subscribe()
        );

        disposable.add(
                Flux
                        .range(options.getIndex(), options.getSize())
                        .flatMap(this::connect, options.getConcurrency(), options.getConcurrency())
                        .doOnNext(this::handleConnected)
                        .then()
                        .doFinally(ignore -> {
                            for (Runnable runnable : completeHandler) {
                                runnable.run();
                            }
                        })
                        .subscribe()
        );
    }

    public void reload() {
        beforeConnectHandler.clear();
        completeHandler.clear();
        connectionHandler.clear();

        reloadable.dispose();
        reloadable = Disposables.composite();

        if (options.getFile() != null) {
            executeScript(Paths.get(options.getFile().toURI()));
        }

        getConnectionManager()
                .getConnections()
                .filter(Connection::isAlive)
                .doOnNext(Connection::reset)
                .subscribe(this::fireConnectionListener);

        for (Runnable runnable : completeHandler) {
            runnable.run();
        }

    }

    private void handleConnected(Connection connection) {

        connectionManager
                .addConnection(connection);

        fireConnectionListener(connection);

    }

    private void fireConnectionListener(Connection connection) {
        for (Consumer<Connection> consumer : connectionHandler) {
            try {
                consumer.accept(connection);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    void handleBeforeConnect(int index, Object ctx) {
        for (BiConsumer<Integer, Object> consumer : beforeConnectHandler) {
            try {
                consumer.accept(index, ctx);
            } catch (Throwable e) {
                log.warn("handleBeforeConnect error:{}", ExceptionUtils.getErrorMessage(e));
            }
        }
    }

    public long getConnectedSize(){
        return connectionManager.getConnectionSize();
    }

    public Benchmark onConnected(Consumer<Connection> consumer) {
        this.connectionHandler.add(consumer);
        return this;
    }

    public Benchmark beforeConnect(BiConsumer<Integer, Object> handler) {
        beforeConnectHandler.add(handler);
        return this;
    }

    public Benchmark onComplete(Runnable runnable) {
        completeHandler.add(runnable);
        return this;
    }

    private Mono<Object> castMono(Object obj) {
        if (obj instanceof Publisher) {
            return Mono.from(((Publisher<?>) obj));
        }
        return Mono.justOrEmpty(obj);
    }

    @Override
    public Object require(String location) {
        return executeScript(Paths.get(location));
    }

    @SneakyThrows
    protected Object executeScript(Path file) {
        String script = new String(Files.readAllBytes(file));
        Map<String, Object> context = new HashMap<>();
        if (options.getScriptArgs() != null) {
            context.putAll(options.getScriptArgs());
            context.put("args", options.getScriptArgs());
        } else {
            context.put("args", Collections.emptyMap());
        }
        context.put("benchmark", this);
        return scriptFactory
                .compileExpose(Script.of("benchmark_" + name, script)
                                .returnNative(),
                        BenchmarkHelper.class)
                .call(this, context);

    }

    public Mono<Void> randomConnectionAsync(int size, Function<Connection, Object> handler) {
        return connectionManager
                .randomConnection(size)
                .flatMap(conn -> castMono(handler.apply(conn)), size)
                .then();
    }

    public Disposable randomConnection(int size, Function<Connection, Object> handler) {
        return randomConnectionAsync(size, handler)
                .subscribe();
    }

    public Disposable delay(Callable<Object> callable, int ms) {
        return Mono
                .delay(Duration.ofMillis(ms))
                .flatMap(ignore -> Mono
                        .fromCallable(callable)
                        .flatMap(this::castMono)
                        .onErrorResume(err -> {
                            error("delay execute", err);
                            return Mono.empty();
                        }))
                .subscribe();
    }


    public Disposable interval(Callable<Object> callable, int ms) {
        Disposable interval = Flux
                .interval(Duration.ofMillis(ms))
                .onBackpressureDrop()
                .concatMap(ignore -> Mono
                        .fromCallable(callable)
                        .flatMap(this::castMono)
                        .onErrorResume(err -> {
                            error("interval execute", err);
                            return Mono.empty();
                        }))
                .subscribe();

        reloadable.add(interval);
        return () -> {
            reloadable.remove(interval);
            interval.dispose();
        };
    }


    public Deque<Snapshot> snapshots() {
        return snapshots;
    }

    private Mono<Void> snapshot() {
        return connectionManager
                .summary()
                .map(sum -> new Snapshot(snapshots.peekLast(), System.currentTimeMillis(), sum))
                .doOnNext(snapshot -> {
                    snapshots.add(snapshot);
                    if (snapshots.size() >= 86400) {
                        snapshots.removeFirst();
                    }
                })
                .onErrorResume(err -> {
                    error("snapshot", err);
                    return Mono.empty();
                })
                .then();
    }

    private void error(String operation, Throwable e) {
        lastError = e;
        if (errors.size() > 100) {
            errors.clear();
        }
        errors.add(operation + ":" + ExceptionUtils.getErrorMessage(e));
        // log.warn(operation, e);
    }

    private Mono<? extends Connection> connect(int index) {
        Reporter.Point point = reporter.newPoint(REPORT_CONNECTING);
        point.start();

        return connectionFactory
                .apply(new ConnectCreateContextImpl(index))
                .doOnNext(ignore -> point.success())
                .onErrorResume(err -> {
                    point.error(getErrorMessage(err));
                    error("connect", err);
                    return Mono.empty();
                });
    }

    private String getErrorMessage(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    @Override
    public void dispose() {
        reloadable.dispose();
        disposable.dispose();
    }

    @Override
    public boolean isDisposed() {
        return disposable.isDisposed();
    }

    public void doOnDispose(Disposable disposable) {
        this.disposable.add(disposable);
    }

    public void doOnReload(Disposable reload) {
        reloadable.add(reload);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Snapshot {
        private final Snapshot pre;

        private final long timestamp;

        private final ConnectionManager.Summary summary;

        public Snapshot getDiff() {
            ConnectionManager.Summary sum = summary;
            long time = 0;
            if (pre != null) {
                sum = sum.sub(pre.summary);
                time = this.timestamp - pre.timestamp;
            }
            return new Snapshot(null, time, sum);
        }
    }

    @AllArgsConstructor
    class ConnectCreateContextImpl implements ConnectCreateContext {

        private final int index;

        @Override
        public int index() {
            return index;
        }

        @Override
        public void beforeConnect(Object context) {
            handleBeforeConnect(index, context);
        }
    }

    public void clear() {
        logs.clear();
        errors.clear();
        lastError = null;
    }

    public void print(String log, Object... args) {
        logs.add(new DateTime().toString("HH:mm:ss.SSS") + " " + String.format(log, args));
        if (logs.size() > 100) {
            logs.removeFirst();
        }
    }

}

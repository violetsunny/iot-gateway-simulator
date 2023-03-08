package top.iot.gateway.simulator.cmd.benchmark;

import top.iot.gateway.simulator.cmd.AbstractCommand;
import top.iot.gateway.simulator.core.Connection;
import top.iot.gateway.simulator.core.DefaultConnectionManager;
import top.iot.gateway.simulator.core.ExceptionUtils;
import top.iot.gateway.simulator.core.benchmark.Benchmark;
import top.iot.gateway.simulator.core.benchmark.ConnectCreateContext;
import picocli.CommandLine;
import reactor.core.publisher.Mono;

@CommandLine.Command(name = "benchmark", hidden = true)
public abstract class AbstractBenchmarkCommand extends AbstractCommand implements Runnable {
    @CommandLine.Mixin
    protected BenchmarkCommand.Options options;

    protected Benchmark benchmark;

    protected String getDefaultName() {
        return spec.name();
    }

    protected abstract Mono<? extends Connection> createConnection(ConnectCreateContext context);

    @Override
    public final void run() {
        String name = options.getName() == null ? getDefaultName() : options.getName();

        DefaultConnectionManager connectionManager = new DefaultConnectionManager();

        benchmark = Benchmark.create(
                name,
                options,
                connectionManager,
                ctx -> Mono
                        .defer(() -> this.createConnection(ctx))
                        .onErrorResume(err -> {
                            benchmark.print("create connection[" + ctx.index() + "] error: " + ExceptionUtils.getErrorMessage(err));
                            return Mono.empty();
                        })
        );

        BenchmarkCommand.addBenchmark(benchmark);
        benchmark.start();
        benchmark.doOnDispose(connectionManager);
        doAfter();
    }

    protected void doAfter() {
        main().getCommandLine().execute("benchmark", "stats", benchmark.getName());
    }
}

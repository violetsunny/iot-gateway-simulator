package top.iot.gateway.simulator.cmd.benchmark;

import top.iot.gateway.simulator.cmd.NetClientCommandOption;
import top.iot.gateway.simulator.core.Connection;
import top.iot.gateway.simulator.core.benchmark.ConnectCreateContext;
import top.iot.gateway.simulator.core.network.http.HTTPClient;
import top.iot.gateway.simulator.core.network.http.HTTPClientOptions;
import org.springframework.http.HttpHeaders;
import picocli.CommandLine;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

@CommandLine.Command(name = "http",
        showDefaultValues = true,
        description = {
                "Create HTTP Benchmark"
        },
        headerHeading = "%n", sortOptions = false)
class HTTPBenchMark extends AbstractBenchmarkCommand implements Runnable {

    @CommandLine.Mixin
    CommandOptions command;

    @CommandLine.Mixin
    NetClientCommandOption common;

    @Override
    protected Mono<? extends Connection> createConnection(ConnectCreateContext ctx) {
        HTTPClientOptions commandOptions = command.refactor(Collections.singletonMap("index", ctx.index()));
        if (null != common) {
            common.apply(commandOptions);
        }
        ctx.beforeConnect(commandOptions);
        return HTTPClient.create(commandOptions);
    }


    static class CommandOptions extends HTTPClientOptions {

        @Override
        @CommandLine.Option(names = {"--id"}, description = "ID", defaultValue = "http-client-{index}", order = 1)
        public void setId(String id) {
            super.setId(id);
        }

        @CommandLine.Option(names = {"-h", "--header"}, description = "Default Headers")
        public void setHeader(Map<String, String> header) {
            HttpHeaders headers = new HttpHeaders();
            header.forEach(headers::add);
            setHeaders(headers);
        }

        @CommandLine.Option(names = {"-u", "--url"}, description = "Request Base URL",required = true)
        public void setBasePath(URL basePath) {
            super.setBasePath(basePath.toString());
        }

        @CommandLine.Option(names = {"--shared"}, description = "Shared Client", defaultValue = "false")
        public void setShared0(boolean shared) {
            super.setShared(shared);
        }

    }

}
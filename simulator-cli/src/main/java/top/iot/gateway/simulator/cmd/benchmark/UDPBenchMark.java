package top.iot.gateway.simulator.cmd.benchmark;

import top.iot.gateway.simulator.cmd.NetworkInterfaceCompleter;
import top.iot.gateway.simulator.core.Connection;
import top.iot.gateway.simulator.core.benchmark.ConnectCreateContext;
import top.iot.gateway.simulator.core.network.udp.UDPClient;
import top.iot.gateway.simulator.core.network.udp.UDPOptions;
import picocli.CommandLine;
import reactor.core.publisher.Mono;

import java.util.Collections;

@CommandLine.Command(name = "udp",
        showDefaultValues = true,
        description = {
                "Create UDP Benchmark"
        },
        headerHeading = "%n", sortOptions = false)
class UDPBenchMark extends AbstractBenchmarkCommand implements Runnable {

    @CommandLine.Mixin
    CommandOptions command;

    @Override
    protected Mono<? extends Connection> createConnection(ConnectCreateContext ctx) {
        UDPOptions commandOptions = command.refactor(Collections.singletonMap("index", ctx.index()));
        ctx.beforeConnect(commandOptions);
        return UDPClient.create(commandOptions);
    }


    static class CommandOptions extends UDPOptions {

        @Override
        @CommandLine.Option(names = {"--id"}, description = "ID", defaultValue = "udp-client-{index}", order = 1)
        public void setId(String id) {
            super.setId(id);
        }

        @Override
        @CommandLine.Option(names = {"-h", "--host"}, description = "host", order = 2, defaultValue = "127.0.0.1")
        public void setHost(String host) {
            super.setHost(host);
        }

        @Override
        @CommandLine.Option(names = {"-p", "--port"}, description = "port", order = 3)
        public void setPort(int port) {
            super.setPort(port);
        }

        @Override
        @CommandLine.Option(names = {"--interface"}, description = "Network Interface", order = 7, completionCandidates = NetworkInterfaceCompleter.class)
        public void setLocalAddress(String localAddress) {
            super.setLocalAddress(localAddress);
        }
    }

}
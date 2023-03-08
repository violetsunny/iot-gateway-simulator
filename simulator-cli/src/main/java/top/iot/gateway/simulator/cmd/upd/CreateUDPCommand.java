package top.iot.gateway.simulator.cmd.upd;

import top.iot.gateway.simulator.cmd.AbstractCommand;
import top.iot.gateway.simulator.cmd.NetworkInterfaceCompleter;
import top.iot.gateway.simulator.core.ExceptionUtils;
import top.iot.gateway.simulator.core.network.udp.UDPClient;
import top.iot.gateway.simulator.core.network.udp.UDPOptions;
import picocli.CommandLine;

import java.time.Duration;

@CommandLine.Command(name = "create",
        showDefaultValues = true,
        description = "Create UDP Client",
        headerHeading = "%n", sortOptions = false)
public class CreateUDPCommand extends AbstractCommand implements Runnable {

    @CommandLine.Mixin
    private Options options;

    @Override
    public void run() {
        printf("create udp client %s", options.getId());
        try {
            UDPClient client = UDPClient.create(options).block(Duration.ofSeconds(10));
            if (client != null) {
                main().connectionManager().addConnection(client);
                printf(" success!%n");
                main()
                        .getCommandLine()
                        .execute("udp", "attach", client.getId());

            } else {
                printf(" error:%n");
            }
        } catch (Throwable err) {
            printfError(" error: %s %n", ExceptionUtils.getErrorMessage(err));
        }
    }

    static class Options extends UDPOptions {

        @Override
        @CommandLine.Option(names = {"--id"}, description = "Client ID", defaultValue = "udp-client")
        public void setId(String id) {
            super.setId(id);
        }

        @Override
        @CommandLine.Option(names = {"-h", "--host"}, description = "Remote host")
        public void setHost(String host) {
            super.setHost(host);
        }

        @Override
        @CommandLine.Option(names = {"-p", "--port"}, description = "Remote port")
        public void setPort(int port) {
            super.setPort(port);
        }

        @Override
        @CommandLine.Option(names = {"--interface"}, paramLabel = "interface", description = "Network Interface", order = 7, completionCandidates = NetworkInterfaceCompleter.class)
        public void setLocalAddress(String localAddress) {
            super.setLocalAddress(localAddress);
        }
    }
}

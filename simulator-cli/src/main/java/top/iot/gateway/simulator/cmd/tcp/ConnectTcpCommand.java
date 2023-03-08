package top.iot.gateway.simulator.cmd.tcp;

import top.iot.gateway.simulator.cmd.AbstractCommand;
import top.iot.gateway.simulator.cmd.NetClientCommandOption;
import top.iot.gateway.simulator.core.ExceptionUtils;
import top.iot.gateway.simulator.core.Global;
import top.iot.gateway.simulator.core.network.tcp.TcpClient;
import top.iot.gateway.simulator.core.network.tcp.TcpOptions;
import picocli.CommandLine;

import java.time.Duration;

@CommandLine.Command(name = "connect",
        showDefaultValues = true,
        description = "Create new TCP connection",
        headerHeading = "%n", sortOptions = false)
class ConnectTcpCommand extends AbstractCommand implements Runnable {

    @CommandLine.Mixin
    TCPCommandOptions command;

    @CommandLine.Mixin
    private NetClientCommandOption common;

    @Override
    public void run() {
        if (common != null) {
            common.apply(command);
        }
        printf("connecting  %s", command);
        try {
            TcpClient client = TcpClient
                    .connect(Global.vertx(), command)
                    .block(Duration.ofSeconds(10));


            if (client != null) {
                main().connectionManager().addConnection(client);
                printf(" success!%n");
                main()
                        .getCommandLine()
                        .execute("tcp", "attach", client.getId());
            } else {
                printfError(" error!%n");
            }

        } catch (Throwable err) {
            printfError(" error: %s %n", ExceptionUtils.getErrorMessage(err));
        }

    }

    static class TCPCommandOptions extends TcpOptions {

        @Override
        @CommandLine.Option(names = {"--id"}, description = "ID", order = 1)
        public void setId(String id) {
            super.setId(id);
        }

        @Override
        @CommandLine.Option(names = {"-h", "--host"}, description = "host", order = 2, defaultValue = "127.0.0.1")
        public void setHost(String host) {
            super.setHost(host);
        }

        @Override
        @CommandLine.Option(names = {"-p", "--port"}, description = "port", order = 3, required = true)
        public void setPort(int port) {
            super.setPort(port);
        }

        @Override
        @CommandLine.Option(names = {"--delimited"}, description = "使用分隔符来处理粘拆包", order = 4)
        public void setDelimited(String delimited) {
            super.setDelimited(delimited);
        }

        @Override
        @CommandLine.Option(names = {"--fixedLength"}, description = "使用固定长度来处理粘拆包", order = 5)
        public void setFixedLength(Integer fixedLength) {
            super.setFixedLength(fixedLength);
        }

        @Override
        @CommandLine.Option(names = {"--lengthField"},
                description = "使用某个字段作为长度来处理粘拆包,如: \"0,4\"标识从0字节开始的4字节作为接下来的报文长度",
                split = ",", order = 5)
        public void setLengthField(Integer[] lengthField) {
            super.setLengthField(lengthField);
        }

    }
}


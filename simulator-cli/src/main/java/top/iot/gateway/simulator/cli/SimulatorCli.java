package top.iot.gateway.simulator.cli;


import picocli.CommandLine;

@CommandLine.Command(name = "iot-gateway-simulator-cli",
        sortOptions = false,
        description = "%n"//
                + "@|italic " //
                + "iot-gateway 网络模拟器.%n" //
                + "支持模拟MQTT,HTTP,COAP,TCP等协议%n" //
                + "%n" //
                + "|@%n%n")
public class SimulatorCli implements Runnable {

    @CommandLine.Mixin
    private StandardHelpOptions options = new StandardHelpOptions();

    @Override
    public void run() {

    }
}

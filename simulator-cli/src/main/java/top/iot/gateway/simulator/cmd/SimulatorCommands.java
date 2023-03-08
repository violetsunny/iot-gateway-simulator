package top.iot.gateway.simulator.cmd;

import top.iot.gateway.simulator.cli.JLineInteractiveCommands;
import top.iot.gateway.simulator.cmd.benchmark.BenchmarkCommand;
import top.iot.gateway.simulator.cmd.http.HTTPOperationCommand;
import top.iot.gateway.simulator.cmd.mqtt.MqttOperationCommand;
import top.iot.gateway.simulator.cmd.tcp.TcpOperationCommand;
import top.iot.gateway.simulator.cmd.upd.UDPOperationCommand;
import top.iot.gateway.simulator.core.ConnectionManager;
import picocli.CommandLine;
import picocli.shell.jline3.PicocliCommands;
import reactor.core.Disposable;

@CommandLine.Command(name = "",
        description = "@|bold,underline iot-gateway Simulator CLI :|@%n",
        footer = {"%n@|italic Enter exit to exit.|@%n"},
        subcommands = {
                CommandLine.HelpCommand.class,
                PicocliCommands.ClearScreen.class,
                ListConnection.class,
                ExecuteScriptCommand.class,
                BenchmarkCommand.class,
                MqttOperationCommand.class,
                TcpOperationCommand.class,
                UDPOperationCommand.class,
                HTTPOperationCommand.class
        },
        customSynopsis = {""},
        synopsisHeading = "")
public class SimulatorCommands extends JLineInteractiveCommands implements Runnable, Disposable {

    final ConnectionManager connectionManager = ConnectionManager.global();


    public ConnectionManager connectionManager() {
        return connectionManager;
    }


    @Override
    public void run() {
        printUsageMessage();
    }


    @Override
    public void dispose() {
        connectionManager.dispose();
    }
}

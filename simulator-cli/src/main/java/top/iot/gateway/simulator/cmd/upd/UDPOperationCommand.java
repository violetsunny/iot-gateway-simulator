package top.iot.gateway.simulator.cmd.upd;

import top.iot.gateway.simulator.cmd.CommonCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "udp",
        description = "UDP client operations",
        subcommands = {
                CreateUDPCommand.class,
                UDPAttachCommand.class
        })
public class UDPOperationCommand extends CommonCommand implements Runnable {


    @Override
    public void run() {
        showHelp();
    }
}

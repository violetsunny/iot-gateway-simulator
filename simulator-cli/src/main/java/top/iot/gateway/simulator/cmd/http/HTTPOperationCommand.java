package top.iot.gateway.simulator.cmd.http;

import top.iot.gateway.simulator.cmd.CommonCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "http",
        description = "HTTP client operations",
        subcommands = {
                CreateHttpCommand.class,
                HTTPAttachCommand.class
        })
public class HTTPOperationCommand extends CommonCommand implements Runnable {


    @Override
    public void run() {
        showHelp();
    }
}

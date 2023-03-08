package top.iot.gateway.simulator.cmd;

import picocli.CommandLine;


@CommandLine.Command(name = "CommonCommand")
public class CommonCommand extends AbstractCommand implements Runnable{



    @Override
    public void run() {
        showHelp();
    }
}
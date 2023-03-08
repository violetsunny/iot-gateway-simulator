package top.iot.gateway.simulator.cmd.benchmark;

import top.iot.gateway.simulator.cmd.ListConnection;
import top.iot.gateway.simulator.core.CompositeConnectionManager;
import top.iot.gateway.simulator.core.ConnectionManager;
import top.iot.gateway.simulator.core.DefaultConnectionManager;
import top.iot.gateway.simulator.core.benchmark.Benchmark;
import picocli.CommandLine;

import java.util.stream.Collectors;


@CommandLine.Command(name = "list",
        description = "Search connections",
        headerHeading = "%n")
public class BenchmarkListCommand extends ListConnection {

    @CommandLine.Option(names = {"--name"},completionCandidates = BenchmarkCommand.NameComplete.class)
    private String name;

    @Override
    protected ConnectionManager connectionManager() {
        if (name != null) {
            Benchmark benchmark = BenchmarkCommand.allBenchMark.get(name);
            return benchmark == null ? new DefaultConnectionManager() : benchmark.getConnectionManager();
        }
        return new CompositeConnectionManager(BenchmarkCommand.allBenchMark
                                                      .values()
                                                      .stream()
                                                      .map(Benchmark::getConnectionManager)
                                                      .collect(Collectors.toList()));
    }
}

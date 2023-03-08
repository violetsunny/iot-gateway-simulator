package top.iot.gateway.simulator.cmd.mqtt;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import top.iot.gateway.simulator.cmd.AbstractCommand;
import top.iot.gateway.simulator.core.Connection;
import top.iot.gateway.simulator.core.ConnectionManager;
import top.iot.gateway.simulator.core.ExceptionUtils;
import top.iot.gateway.simulator.core.network.NetworkType;
import top.iot.gateway.simulator.core.network.mqtt.MqttClient;
import picocli.CommandLine;

import java.time.Duration;
import java.util.Iterator;

@CommandLine.Command(name = "publish", description = "Publish mqtt message")
@Getter
@Setter
public class MqttPublishCommand extends AbstractCommand implements Runnable {

    String clientId;

    @CommandLine.Option(names = {"-c", "--clientId"}, required = true, description = "clientId", completionCandidates = IdComplete.class)
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @CommandLine.Option(names = {"-t", "--topic"}, required = true, description = "mqtt topic")
    String topic;


    @CommandLine.Option(names = {"-q", "--qos"}, description = "QoS Level", defaultValue = "0")
    int qos;

    @CommandLine.Parameters(arity = "1", description = "0x开头为16进制")
    String payload;

    static class IdComplete implements Iterable<String> {

        @Override
        @SneakyThrows
        public Iterator<String> iterator() {
            return ConnectionManager
                    .global()
                    .getConnections()
                    .filter(c -> c.getType() == NetworkType.mqtt_client)
                    .map(Connection::getId)
                    .collectList()
                    .block()
                    .iterator();
        }
    }

    @Override
    public void run() {
        Connection connection = main().connectionManager().getConnection(clientId).blockOptional().orElse(null);
        if (connection == null) {
            printfError("请先使用命令创建mqtt连接: mqtt connect -c=%s %n", clientId);
            return;
        }
        MqttClient client = connection.unwrap(MqttClient.class);
        printf("publishing %s %s ", topic, payload);
        try {
            client.publishAsync(topic, qos, payload)
                  .block(Duration.ofSeconds(10));
            printf("success!%n");
        } catch (Throwable e) {
            printfError("error:%s%n", ExceptionUtils.getErrorMessage(e));
        }

    }


}

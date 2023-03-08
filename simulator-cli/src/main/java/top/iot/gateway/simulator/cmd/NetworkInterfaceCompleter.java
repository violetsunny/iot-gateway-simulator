package top.iot.gateway.simulator.cmd;

import top.iot.gateway.simulator.core.network.AddressManager;

import java.net.InetAddress;
import java.util.Iterator;

public class NetworkInterfaceCompleter implements Iterable<String> {
    @Override
    public Iterator<String> iterator() {
        return AddressManager
                .global()
                .getAliveLocalAddresses()
                .stream()
                .map(InetAddress::getHostAddress)
                .iterator();
    }
}

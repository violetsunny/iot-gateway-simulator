package top.iot.gateway.simulator.core.script.nashorn;

import top.iot.gateway.simulator.core.script.AbstractScriptFactoryProvider;
import top.iot.gateway.simulator.core.script.ScriptFactory;

public class NashornScriptFactoryProvider extends AbstractScriptFactoryProvider {

    public NashornScriptFactoryProvider() {
        super("js", "javascript", "nashorn");
    }

    @Override
    public ScriptFactory factory() {
        return new NashornScriptFactory();
    }
}

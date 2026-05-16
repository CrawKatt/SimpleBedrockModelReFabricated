package example.init;

import example.event.Ticker;
import net.fabricmc.api.ModInitializer;

public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleModRegister.register();
        Ticker.register();
    }
}

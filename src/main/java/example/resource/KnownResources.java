package example.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.RawResourceLoaders;
import example.init.ExampleModRegister;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class KnownResources {
    public static final ArrayList<Identifier> ANIMATION_AND_MODEL = new ArrayList<>();
    public static final ArrayList<Identifier> MODEL = new ArrayList<>();

    public static final Identifier TEST = registerAnimationAndModel(Identifier.of(ExampleModRegister.MOD_ID, "test"));
    public static final Identifier DEAGLE = registerAnimationAndModel(Identifier.of(ExampleModRegister.MOD_ID, "deagle"));

    private static Identifier registerAnimationAndModel(Identifier location) {
        ANIMATION_AND_MODEL.add(location);
        return location;
    }

    private static Identifier registerModel(Identifier location) {
        MODEL.add(location);
        return location;
    }

    //@SubscribeEvent
    public static void onAnimationRegister(RegisterBedrockAnimationEvent event) {
        for (Identifier resourceLocation : ANIMATION_AND_MODEL) {
            event.register(resourceLocation, resourceLocation, RawResourceLoaders.COMMON_LOADER);
        }
    }

    //@SubscribeEvent
    public static void onModelRegister(RegisterBedrockModelEvent event) {
        for (Identifier resourceLocation : MODEL) {
            event.register(resourceLocation, RawResourceLoaders.COMMON_LOADER);
        }
        for (Identifier resourceLocation : ANIMATION_AND_MODEL) {
            event.register(resourceLocation, RawResourceLoaders.COMMON_LOADER);
        }
    }
}

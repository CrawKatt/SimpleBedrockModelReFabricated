package example.init;

import example.capability.FPGunAnimationCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public final class ExampleEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<FPGunAnimationCapability> FP_GUN_ANIMATION =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of(ExampleModRegister.MOD_ID, "fp_gun_ani"), FPGunAnimationCapability.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FP_GUN_ANIMATION, FPGunAnimationCapability::new, RespawnCopyStrategy.NEVER_COPY);
    }
}

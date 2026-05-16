package example.capability;

import example.animation.FPGunAnimationInstance;
import example.init.ExampleEntityComponents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.component.Component;

public class FPGunAnimationCapability implements Component, IFPGunAnimationCapability {
    private FPGunAnimationInstance animationInstance;
    private int lastSelected = -1;

    public FPGunAnimationCapability(PlayerEntity ignoredPlayer) {
    }

    public void setPlayer(PlayerEntity player) {
        if (this.animationInstance == null) {
            this.animationInstance = new FPGunAnimationInstance(player);
        }
    }

    @Override
    public FPGunAnimationInstance getAnimationInstance() {
        return animationInstance;
    }

    public int getLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(int lastSelected) {
        this.lastSelected = lastSelected;
    }

    public static FPGunAnimationCapability get(PlayerEntity player) {
        FPGunAnimationCapability data = ExampleEntityComponents.FP_GUN_ANIMATION.get(player);
        data.setPlayer(player);
        return data;
    }

    private boolean inited() {
        return animationInstance != null;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.lastSelected = -1;
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
    }
}

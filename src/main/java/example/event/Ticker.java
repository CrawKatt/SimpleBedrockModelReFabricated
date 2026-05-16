package example.event;

import example.animation.FPGunAnimationInstance;
import example.animation.GunAnimationGraph;
import example.capability.FPGunAnimationCapability;
import example.item.GunItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

//@EventBusSubscriber
public class Ticker {
    public static void register() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.isClient()) {
                return;
            }
            for (PlayerEntity player : world.getPlayers()) {
                onServerTick(player);
            }
        });
    }

    public static void onServerTick(PlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        // 需要先更新 animationGraph 再 tick，保持逻辑严密
        var capability = FPGunAnimationCapability.get(player);
        if (capability.getLastSelected() != inventory.selectedSlot) {
            ItemStack selected = inventory.getMainHandStack();
            if (selected.getItem() instanceof GunItem gunItem) {
                FPGunAnimationInstance animationInstance = capability.getAnimationInstance();
                animationInstance.updateAnimationGraphAndDraw(gunItem.getAnimationGraph(animationInstance));
            } else {
                capability.getAnimationInstance().updateAnimationGraphAndDraw(null);
            }
            capability.setLastSelected(inventory.selectedSlot);
        }
        GunAnimationGraph animationGraph = capability.getAnimationInstance().getAnimationGraph();
        if (animationGraph != null) {
            animationGraph.tick();
        }
    }
}

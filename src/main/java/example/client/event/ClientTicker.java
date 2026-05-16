package example.client.event;

import example.animation.FPGunAnimationInstance;
import example.animation.GunAnimationGraph;
import example.capability.FPGunAnimationCapability;
import example.item.GunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerInventory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;


//@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTicker {
    public static void register() {
        WorldRenderEvents.START.register(context -> onRenderTick());
        ClientTickEvents.START_CLIENT_TICK.register(client -> onPlayerChangeSelect());
    }

    public static void onRenderTick() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            var cap = FPGunAnimationCapability.get(player);
            cap.setPlayer(player);
            GunAnimationGraph animationGraph = cap.getAnimationInstance().getAnimationGraph();
            if (animationGraph != null) {
                animationGraph.tick();
            }
        }
    }

    private static int oldHotBarSelected = -1;

    public static void onPlayerChangeSelect() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        // 这里就先简单地判断有没有切换选中的格子，用于测试。
        if (oldHotBarSelected != inventory.selectedSlot) {
            ItemStack selected = inventory.getMainHandStack();
            var cap = FPGunAnimationCapability.get(player);
            if (selected.getItem() instanceof GunItem gunItem) {
                FPGunAnimationInstance animationInstance = cap.getAnimationInstance();
                animationInstance.updateAnimationGraphAndDraw(gunItem.getAnimationGraph(animationInstance));
            } else {
                cap.getAnimationInstance().updateAnimationGraphAndDraw(null);
            }
            oldHotBarSelected = inventory.selectedSlot;
        }
    }
}

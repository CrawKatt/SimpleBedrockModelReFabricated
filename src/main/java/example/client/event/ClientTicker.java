package example.client.event;

import example.animation.FPGunAnimationInstance;
import example.animation.GunAnimationGraph;
import example.capability.FPGunAnimationCapability;
import example.init.ExampleModRegister;
import example.item.GunItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;


//@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTicker {
    //@SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Pre event) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            var cap = player.getData(ExampleModRegister.FP_GUN_ANIMATION);
            cap.setPlayer(player);
            GunAnimationGraph animationGraph = cap.getAnimationInstance().getAnimationGraph();
            if (animationGraph != null) {
                animationGraph.tick();
            }
        }
    }

    private static int oldHotBarSelected = -1;

    //@SubscribeEvent
    public static void onPlayerChangeSelect(ClientTickEvent.Pre event) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        Inventory inventory = player.getInventory();
        // 这里就先简单地判断有没有切换选中的格子，用于测试。
        if (oldHotBarSelected != inventory.selected) {
            ItemStack selected = inventory.getSelected();
            var cap = FPGunAnimationCapability.get(player);
            if (selected.getItem() instanceof GunItem gunItem) {
                FPGunAnimationInstance animationInstance = cap.getAnimationInstance();
                animationInstance.updateAnimationGraphAndDraw(gunItem.getAnimationGraph(animationInstance));
            } else {
                cap.getAnimationInstance().updateAnimationGraphAndDraw(null);
            }
            oldHotBarSelected = inventory.selected;
        }
    }
}

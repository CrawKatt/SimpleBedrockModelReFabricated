package com.github.mcmodderanchor.simplebedrockmodel.v1.client;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.compat.sodium.SodiumCompat;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.compat.epicfight.EpicFightCompat;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.BedrockRenderDispatcher;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.RenderBackendMode;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.compute.ComputeRenderBackend;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = SimpleBedrockModel.MOD_ID)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        SodiumCompat.init();
        EpicFightCompat.init();

        // 初始化 compute 渲染后端（默认 OFF，需要用户手动启用）
        // compute 后端的实际 GL 初始化是懒加载的，这里只注册实例
        BedrockRenderDispatcher.setComputeBackend(new ComputeRenderBackend());
        // 默认关闭，用户可通过配置或 API 切换
        BedrockRenderDispatcher.setMode(RenderBackendMode.OFF);
    }
}

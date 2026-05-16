package com.github.mcmodderanchor.simplebedrockmodel.v1.network;

import com.github.mcmodderanchor.simplebedrockmodel.v1.network.message.ServerMessageSwapItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class NetworkHandler {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(
                ServerMessageSwapItem.TYPE,
                ServerMessageSwapItem.CODEC
        );
    }

    public static void sendToClientPlayer(
            CustomPayload message,
            PlayerEntity player
    ) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, message);
        }
    }

    public static void sendToTrackingEntityAndSelf(
            Entity centerEntity,
            CustomPayload message
    ) {
        PlayerLookup.tracking(centerEntity)
                .forEach(player ->
                        ServerPlayNetworking.send(player, message));

        if (centerEntity instanceof ServerPlayerEntity self) {
            ServerPlayNetworking.send(self, message);
        }
    }

    public static void sendToAllPlayers(
            ServerWorld world,
            CustomPayload message
    ) {
        PlayerLookup.world(world)
                .forEach(player ->
                        ServerPlayNetworking.send(player, message));
    }

    public static void sendToTrackingEntity(
            CustomPayload message,
            Entity centerEntity
    ) {
        PlayerLookup.tracking(centerEntity)
                .forEach(player ->
                        ServerPlayNetworking.send(player, message));
    }

    public static void sendToDimension(
            CustomPayload message,
            Entity centerEntity
    ) {
        if (centerEntity.getWorld() instanceof ServerWorld serverWorld) {
            PlayerLookup.world(serverWorld)
                    .forEach(player ->
                            ServerPlayNetworking.send(player, message));
        }
    }
}
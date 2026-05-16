package com.github.mcmodderanchor.simplebedrockmodel.v1.network.message;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;

public record ServerMessageSwapItem() implements CustomPayload {
    public static final CustomPayload.Id<ServerMessageSwapItem> TYPE =
            new CustomPayload.Id<>(SimpleBedrockModel.modLoc("swap_item"));
    public static final PacketCodec<RegistryByteBuf, ServerMessageSwapItem> CODEC =
            PacketCodec.unit(new ServerMessageSwapItem());

    @NotNull
    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }
}

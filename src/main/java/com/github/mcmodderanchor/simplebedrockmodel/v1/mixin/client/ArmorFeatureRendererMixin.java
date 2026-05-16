package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.registry.ClientItemExtensionsRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
    @Unique
    private BipedEntityModel<?> sbm$armorModelOverride;

    @Inject(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At("HEAD")
    )
    private void sbm$clearOverride(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity livingEntity,
                                   EquipmentSlot equipmentSlot, int light, BipedEntityModel<?> model, CallbackInfo ci) {
        this.sbm$armorModelOverride = null;
    }

    @Inject(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;usesInnerModel(Lnet/minecraft/entity/EquipmentSlot;)Z",
                    shift = At.Shift.BEFORE
            )
    )
    private void sbm$resolveOverride(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity livingEntity,
                                     EquipmentSlot equipmentSlot, int light, BipedEntityModel<?> model, CallbackInfo ci) {
        ItemStack itemStack = livingEntity.getEquippedStack(equipmentSlot);
        IClientItemExtensions extensions = ClientItemExtensionsRegistry.get(itemStack);
        if (extensions == null) {
            return;
        }

        BipedEntityModel<?> overrideModel = extensions.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, model);
        if (overrideModel != null) {
            this.sbm$armorModelOverride = overrideModel;
        }
    }

    @ModifyArg(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmorParts(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/model/BipedEntityModel;ILnet/minecraft/util/Identifier;)V"
            ),
            index = 3
    )
    private BipedEntityModel<?> sbm$swapArmorPartsModel(BipedEntityModel<?> original) {
        return this.sbm$armorModelOverride != null ? this.sbm$armorModelOverride : original;
    }

    @ModifyArg(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderTrim(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrim;Lnet/minecraft/client/render/entity/model/BipedEntityModel;Z)V"
            ),
            index = 5
    )
    private BipedEntityModel<?> sbm$swapTrimModel(BipedEntityModel<?> original) {
        return this.sbm$armorModelOverride != null ? this.sbm$armorModelOverride : original;
    }

    @ModifyArg(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderGlint(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"
            ),
            index = 3
    )
    private BipedEntityModel<?> sbm$swapGlintModel(BipedEntityModel<?> original) {
        return this.sbm$armorModelOverride != null ? this.sbm$armorModelOverride : original;
    }

    @Inject(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V",
            at = @At("RETURN")
    )
    private void sbm$clearOverrideAfterRender(MatrixStack matrices, VertexConsumerProvider vertexConsumers, LivingEntity livingEntity,
                                              EquipmentSlot equipmentSlot, int light, BipedEntityModel<?> model, CallbackInfo ci) {
        this.sbm$armorModelOverride = null;
    }
}

package example.item;

import example.animation.DeagleAnimationGraph;
import example.animation.FPGunAnimationInstance;
import example.animation.GunAnimationGraph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DeagleItem extends Item implements GunItem {
    public DeagleItem() {
        super(new Settings().maxCount(1));
    }

    /*
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
//        entity.getCapability(ModCapability.FPGUN_ANIMATION_CAPABILITY).ifPresent(capability -> {
//            capability.getAnimationInstance().trigger();
//        });
        return true;
    }
    */

//    @Override
//    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
//        return true;
//    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return true;
    }

    //@SubscribeEvent
    /*
    public static void initializeClient(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            public static final DeagleWithoutLevelRenderer render = new DeagleWithoutLevelRenderer();

            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return render;
            }
        }, ExampleModRegister.DEAGLE_ITEM);
    }
    */

    @Override
    public GunAnimationGraph getAnimationGraph(FPGunAnimationInstance animationInstance) {
        return new DeagleAnimationGraph(animationInstance);
    }

    @Override
    public boolean hasMagInstalled(ItemStack itemStack) {
//        CompoundTag nbt = itemStack.get();
//        if (nbt.contains("HasMagInstalled")) {
//            return nbt.getBoolean("HasMagInstalled");
//        }
        return false;
    }

    @Override
    public int getAmmoInMag(ItemStack itemStack) {
//        CompoundTag nbt = itemStack.getOrCreateTag();
//        if (nbt.contains("AmmoInMag")) {
//            return nbt.getInt("AmmoInMag");
//        }
        return 99;
    }

    @Override
    public int getAmmoInGun(ItemStack itemStack) {
//        CompoundTag nbt = itemStack.getOrCreateTag();
//        if (nbt.contains("AmmoInGun")) {
//            return nbt.getInt("AmmoInGun");
//        }
        return 99;
    }

    @Override
    public void setMagInstalled(ItemStack itemStack, boolean installed) {
//        CompoundTag nbt = itemStack.getOrCreateTag();
//        nbt.putBoolean("HasMagInstalled", installed);
    }

    @Override
    public void setAmmoInMag(ItemStack itemStack, int ammo) {
//        CompoundTag nbt = itemStack.getOrCreateTag();
//        nbt.putInt("AmmoInMag", ammo);
    }

    @Override
    public void setAmmoInGun(ItemStack itemStack, int ammo) {
//        CompoundTag nbt = itemStack.getOrCreateTag();
//        nbt.putInt("AmmoInGun", ammo);
    }
}

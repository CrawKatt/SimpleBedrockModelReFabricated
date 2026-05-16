package example.capability;

import example.animation.FPGunAnimationInstance;
import net.minecraft.entity.player.PlayerEntity;

public class FPGunAnimationCapability implements IFPGunAnimationCapability{
    private FPGunAnimationInstance animationInstance;
    private int lastSelected = -1;

    public FPGunAnimationCapability() {
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

    /*
    public static FPGunAnimationCapability get(PlayerEntity player) {
        var data = player.getData(ExampleModRegister.FP_GUN_ANIMATION);
        if (!data.inited()) {
            data.setPlayer(player);
        }
        return data;
    }
    */

    private boolean inited() {
        return animationInstance != null;
    }
}

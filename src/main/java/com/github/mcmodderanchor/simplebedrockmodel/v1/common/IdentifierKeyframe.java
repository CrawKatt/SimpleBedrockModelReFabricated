package com.github.mcmodderanchor.simplebedrockmodel.v1.common;

import com.maydaymemory.mae.basic.BaseKeyframe;
import net.minecraft.util.Identifier;

public class IdentifierKeyframe extends BaseKeyframe<Identifier> {
    private final Identifier resourceLocation;

    public IdentifierKeyframe(float timeS, Identifier resourceLocation) {
        super(timeS);
        this.resourceLocation = resourceLocation;
    }

    @Override
    public Identifier getValue() {
        return resourceLocation;
    }
}

package net.typho.tungsten.item.jewel;

import net.minecraft.world.item.Item;

public abstract class Jewel extends Item {
    public Jewel(Properties pProperties) {
        super(pProperties.fireResistant());
    }
}

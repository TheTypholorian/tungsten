package net.typho.tungsten.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.typho.tungsten.TungstenMod;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    protected ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        //dropSelf(TungstenMod.TUNGSTEN_BLOCK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return TungstenMod.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}

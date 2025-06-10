package net.typho.tungsten.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.typho.tungsten.TungstenMod;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TungstenMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
    }

    protected void blockWithItem(RegistryObject<? extends Block> obj) {
        simpleBlockWithItem(obj.get(), cubeAll(obj.get()));
    }
}

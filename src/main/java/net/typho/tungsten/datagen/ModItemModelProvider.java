package net.typho.tungsten.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import net.typho.tungsten.TungstenMod;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TungstenMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(TungstenMod.NETHERITE_KNIFE);
    }

    protected ItemModelBuilder simpleItem(RegistryObject<? extends Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(TungstenMod.MODID, "item/" + item.getId().getPath()));
    }
}

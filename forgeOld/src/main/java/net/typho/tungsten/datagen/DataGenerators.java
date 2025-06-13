package net.typho.tungsten.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.typho.tungsten.TungstenMod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = TungstenMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent e) {
        DataGenerator gen = e.getGenerator();
        PackOutput out = gen.getPackOutput();
        ExistingFileHelper files = e.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = e.getLookupProvider();

        gen.addProvider(e.includeServer(), new ModRecipeProvider(out));

        gen.addProvider(e.includeClient(), new ModBlockStateProvider(out, files));
        gen.addProvider(e.includeClient(), new ModItemModelProvider(out, files));

        ModBlockTagGenerator tags = gen.addProvider(e.includeServer(), new ModBlockTagGenerator(out, lookup, files));
        gen.addProvider(e.includeServer(), new ModItemTagGenerator(out, lookup, tags.contentsGetter(), files));
    }
}

package net.typho.tungsten;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(TungstenMod.MODID)
public class TungstenMod {
    public static final String MODID = "tungsten";
    public static final TextColor ITEM_ACTION_COLOR = TextColor.fromRgb(0xFFC34C);
    public static final Style ITEM_ACTION_STYLE = Style.EMPTY.withColor(ITEM_ACTION_COLOR);

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<KnifeItem> WOODEN_KNIFE = ITEMS.register("wooden_knife", () -> new KnifeItem((SwordItem) Items.WOODEN_SWORD, new Item.Properties()));
    public static final RegistryObject<KnifeItem> STONE_KNIFE = ITEMS.register("stone_knife", () -> new KnifeItem((SwordItem) Items.STONE_SWORD, new Item.Properties()));
    public static final RegistryObject<KnifeItem> IRON_KNIFE = ITEMS.register("iron_knife", () -> new KnifeItem((SwordItem) Items.IRON_SWORD, new Item.Properties()));
    public static final RegistryObject<KnifeItem> DIAMOND_KNIFE = ITEMS.register("diamond_knife", () -> new KnifeItem((SwordItem) Items.DIAMOND_SWORD, new Item.Properties()));
    public static final RegistryObject<KnifeItem> GOLDEN_KNIFE = ITEMS.register("golden_knife", () -> new KnifeItem((SwordItem) Items.GOLDEN_SWORD, new Item.Properties()));
    public static final RegistryObject<KnifeItem> NETHERITE_KNIFE = ITEMS.register("netherite_knife", () -> new KnifeItem((SwordItem) Items.NETHERITE_SWORD, new Item.Properties().fireResistant()));

    public static final RegistryObject<HammerItem> WOODEN_HAMMER = ITEMS.register("wooden_hammer", () -> new HammerItem((SwordItem) Items.WOODEN_SWORD, new Item.Properties()));
    public static final RegistryObject<HammerItem> NETHERITE_HAMMER = ITEMS.register("netherite_hammer", () -> new HammerItem((SwordItem) Items.NETHERITE_SWORD, new Item.Properties().fireResistant()));

    public static final RegistryObject<LanceItem> NETHERITE_LANCE = ITEMS.register("netherite_lance", () -> new LanceItem((SwordItem) Items.NETHERITE_SWORD, new Item.Properties().fireResistant()));

    public TungstenMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ITEMS.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(HammerItem.FallDamageEliminator.class);
        MinecraftForge.EVENT_BUS.register(LanceItem.DashHandler.class);

        bus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(WOODEN_KNIFE);
            event.accept(STONE_KNIFE);
            event.accept(IRON_KNIFE);
            event.accept(DIAMOND_KNIFE);
            event.accept(GOLDEN_KNIFE);
            event.accept(NETHERITE_KNIFE);

            event.accept(WOODEN_HAMMER);
            event.accept(NETHERITE_HAMMER);

            event.accept(NETHERITE_LANCE);
        }
    }
}

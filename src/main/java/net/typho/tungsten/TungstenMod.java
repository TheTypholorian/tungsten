package net.typho.tungsten;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(TungstenMod.MODID)
public class TungstenMod {
    public static final String MODID = "tungsten";
    public static final String ITEM_ACTION_COLOR = "§6";

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

    public static final RegistryObject<GrenadeItem> GRENADE = ITEMS.register("grenade", () -> new GrenadeItem(new Item.Properties()));

    public static final RegistryObject<FlamethrowerItem> FLAMETHROWER = ITEMS.register("flamethrower", () -> new FlamethrowerItem(new Item.Properties()));

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<GrenadeProjectile>> GRENADE_PROJECTILE = ENTITIES.register("grenade_projectile", () -> EntityType.Builder.<GrenadeProjectile>of(GrenadeProjectile::new, MobCategory.MISC).sized(0.5f, 0.5f).build("grenade_projectile"));

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);

    public static final RegistryObject<PullEnchantment> PULLBACK = ENCHANTMENTS.register("pullback", () -> new PullEnchantment(Enchantment.Rarity.COMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static <T> void register(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, Optional<NetworkDirection> direction) {
        INSTANCE.registerMessage(packetId++, type, encoder, decoder, handler, direction);
    }

    public TungstenMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        ITEMS.register(bus);
        ENTITIES.register(bus);
        ENCHANTMENTS.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(HammerItem.FallDamageEliminator.class);
        MinecraftForge.EVENT_BUS.register(LanceItem.DashHandler.class);

        bus.addListener(this::addCreative);

        register(
                LanceDashPacket.class,
                LanceDashPacket::toBytes,
                LanceDashPacket::new,
                LanceDashPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        register(
                HammerSlamPacket.class,
                HammerSlamPacket::toBytes,
                HammerSlamPacket::new,
                HammerSlamPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
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

            event.accept(FLAMETHROWER);

            event.accept(GRENADE);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent e) {
            EntityRenderers.register(GRENADE_PROJECTILE.get(), ThrownItemRenderer::new);
        }
    }
}

package net.typho.tungsten;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
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
import net.typho.tungsten.enchantment.PullEnchantment;
import net.typho.tungsten.entity.GrenadeProjectile;
import net.typho.tungsten.item.*;
import net.typho.tungsten.network.HammerSlamPacket;
import net.typho.tungsten.network.LanceDashPacket;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(TungstenMod.MODID)
public class TungstenMod {
    public static final String MODID = "tungsten";
    public static final String ITEM_ACTION_COLOR = "§6";

    // ITEMS
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final Map<Tier, RegistryObject<KnifeItem>> KNIVES = new LinkedHashMap<>();

    public static final RegistryObject<Item> TUNGSTEN_INGOT = ITEMS.register("tungsten_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<HammerItem> WOODEN_HAMMER = ITEMS.register("wooden_hammer", () -> new HammerItem((SwordItem) Items.WOODEN_SWORD, new Item.Properties()));
    public static final RegistryObject<HammerItem> NETHERITE_HAMMER = ITEMS.register("netherite_hammer", () -> new HammerItem((SwordItem) Items.NETHERITE_SWORD, new Item.Properties().fireResistant()));
    public static final RegistryObject<LanceItem> NETHERITE_LANCE = ITEMS.register("netherite_lance", () -> new LanceItem((SwordItem) Items.NETHERITE_SWORD, new Item.Properties().fireResistant()));
    public static final RegistryObject<GrenadeItem> GRENADE = ITEMS.register("grenade", () -> new GrenadeItem(new Item.Properties()));
    public static final RegistryObject<FlamethrowerItem> FLAMETHROWER = ITEMS.register("flamethrower", () -> new FlamethrowerItem(new Item.Properties()));

    public static final TagKey<Block> NEEDS_TUNGSTEN_TOOL = BlockTags.create(new ResourceLocation(MODID, "needs_tungsten_tool"));
    public static final Tier TUNGSTEN_TIER = TierSortingRegistry.registerTier(
            new ForgeTier(5, 3000, 5, 5, 25, NEEDS_TUNGSTEN_TOOL, () -> Ingredient.of(TUNGSTEN_INGOT.get())),
            new ResourceLocation(MODID, "tungsten"),
            List.of(Tiers.NETHERITE),
            List.of()
    );
    public static final ArmorMaterial TUNGSTEN_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type pType) {
            return switch (pType) {
                case HELMET -> 11;
                case CHESTPLATE -> 16;
                case LEGGINGS -> 15;
                case BOOTS -> 13;
            } * 30;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type pType) {
            return switch (pType) {
                case HELMET, BOOTS -> 4;
                case CHESTPLATE -> 9;
                case LEGGINGS -> 7;
            };
        }

        @Override
        public int getEnchantmentValue() {
            return TUNGSTEN_TIER.getEnchantmentValue();
        }

        @Override
        public SoundEvent getEquipSound() {
            return TUNGSTEN_EQUIP.get();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of();
        }

        @Override
        public @NotNull String getName() {
            return MODID + ":tungsten";
        }

        @Override
        public float getToughness() {
            return 1;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    };

    public static final RegistryObject<ArmorItem> TUNGSTEN_HELMET = ITEMS.register("tungsten_helmet", () -> new ArmorItem(TUNGSTEN_MATERIAL, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().fireResistant()));
    public static final RegistryObject<ArmorItem> TUNGSTEN_CHESTPLATE = ITEMS.register("tungsten_chestplate", () -> new ArmorItem(TUNGSTEN_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().fireResistant()));
    public static final RegistryObject<ArmorItem> TUNGSTEN_LEGGINGS = ITEMS.register("tungsten_leggings", () -> new ArmorItem(TUNGSTEN_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().fireResistant()));
    public static final RegistryObject<ArmorItem> TUNGSTEN_BOOTS = ITEMS.register("tungsten_boots", () -> new ArmorItem(TUNGSTEN_MATERIAL, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().fireResistant()));

    // BLOCKS
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    // ENTITIES
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<GrenadeProjectile>> GRENADE_PROJECTILE = ENTITIES.register("grenade_projectile", () -> EntityType.Builder.<GrenadeProjectile>of(GrenadeProjectile::new, MobCategory.MISC).sized(0.5f, 0.5f).build("grenade_projectile"));

    // ENCHANTMENTS
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);

    public static final RegistryObject<PullEnchantment> PULLBACK = ENCHANTMENTS.register("pullback", () -> new PullEnchantment(Enchantment.Rarity.COMMON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));

    // SOUNDS
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> TUNGSTEN_EQUIP = SOUNDS.register("tungsten_equip", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "tungsten_equip")));

    public static void allTiers(TriConsumer<Tier, String, Supplier<Item.Properties>> out) {
        out.accept(Tiers.WOOD, "wooden", Item.Properties::new);
        out.accept(Tiers.STONE, "stone", Item.Properties::new);
        out.accept(Tiers.IRON, "iron", Item.Properties::new);
        out.accept(Tiers.DIAMOND, "diamond", Item.Properties::new);
        out.accept(Tiers.GOLD, "golden", Item.Properties::new);
        out.accept(Tiers.NETHERITE, "netherite", () -> new Item.Properties().fireResistant());
        out.accept(TUNGSTEN_TIER, "tungsten", () -> new Item.Properties().fireResistant().rarity(Rarity.EPIC));
    }

    public static RegistryObject<KnifeItem> knife(Tier tier, String tierName, Supplier<Item.Properties> prop) {
        if (KNIVES.containsKey(tier)) {
            throw new IllegalStateException("Tried to register a knife of tier " + tierName + " twice");
        }

        RegistryObject<KnifeItem> knife = ITEMS.register(tierName + "_knife", () -> new KnifeItem(tier, prop.get()));

        KNIVES.put(tier, knife);

        return knife;
    }

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

        allTiers(TungstenMod::knife);

        ITEMS.register(bus);
        ENTITIES.register(bus);
        ENCHANTMENTS.register(bus);
        SOUNDS.register(bus);

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
            for (RegistryObject<KnifeItem> knife : KNIVES.values()) {
                event.accept(knife);
            }

            event.accept(WOODEN_HAMMER);
            event.accept(NETHERITE_HAMMER);

            event.accept(NETHERITE_LANCE);

            event.accept(FLAMETHROWER);

            event.accept(GRENADE);

            event.accept(TUNGSTEN_HELMET);
            event.accept(TUNGSTEN_CHESTPLATE);
            event.accept(TUNGSTEN_LEGGINGS);
            event.accept(TUNGSTEN_BOOTS);
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

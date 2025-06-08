package net.typho.tungsten;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class LanceItem extends TieredItem implements Vanishable {
    public static final DustParticleOptions DASH_PARTICLE = new DustParticleOptions(new Vector3f(0f, 0.7f, 1f), 2) {
    };

    private final SwordItem parent;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public LanceItem(SwordItem parent, Properties prop) {
        super(parent.getTier(), prop);
        this.parent = parent;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getDamage(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.2, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public float getDamage() {
        return parent.getDamage() * 2;
    }

    public boolean canAttackBlock(@NotNull BlockState p_43291_, @NotNull Level p_43292_, @NotNull BlockPos p_43293_, Player p_43294_) {
        return !p_43294_.isCreative();
    }

    public boolean hurtEnemy(ItemStack p_43278_, @NotNull LivingEntity p_43279_, @NotNull LivingEntity p_43280_) {
        p_43278_.hurtAndBreak(1, p_43280_, (p_43296_) -> {
            p_43296_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    public boolean mineBlock(@NotNull ItemStack p_43282_, @NotNull Level p_43283_, BlockState p_43284_, @NotNull BlockPos p_43285_, @NotNull LivingEntity p_43286_) {
        if (p_43284_.getDestroySpeed(p_43283_, p_43285_) != 0.0F) {
            p_43282_.hurtAndBreak(2, p_43286_, (p_43276_) -> {
                p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level p_41432_, Player p_41433_, @NotNull InteractionHand p_41434_) {
        CompoundTag data = p_41433_.getPersistentData();

        if (!p_41433_.getCooldowns().isOnCooldown(this) && !p_41433_.getPersistentData().getBoolean("isTungstenLanceDashing") && !p_41433_.level().isClientSide()) {
            p_41433_.setDeltaMovement(p_41433_.getLookAngle().scale(5));
            p_41433_.hurtMarked = true;

            dash(true, p_41433_, data);
            p_41433_.getCooldowns().addCooldown(this, 100);
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> p_41423_, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        p_41423_.add(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Right-click to dash through the air, with no fall damage!"));
        p_41423_.add(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Attacking an enemy while dashing will do more damage with more speed."));
        p_41423_.add(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "However, your dash is canceled if your speed is <5 m/s"));
    }

    public static void dash(boolean dash, Player player, CompoundTag data) {
        data.putBoolean("isTungstenLanceDashing", dash);

        if (player instanceof ServerPlayer sp) {
            TungstenMod.INSTANCE.sendTo(
                    new LanceDashPacket(dash),
                    sp.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }

        if (dash) {
            player.displayClientMessage(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Lance Dashed"), true);
        }
    }

    public static class DashHandler {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            Player player = event.getEntity();
            Level level = player.level();
            ItemStack stack = player.getMainHandItem();

            if (!(stack.getItem() instanceof LanceItem lance)) {
                return;
            }

            if (!(event.getTarget() instanceof LivingEntity target)) {
                return;
            }

            CompoundTag data = player.getPersistentData();

            if (!data.getBoolean("isTungstenLanceDashing")) {
                return;
            }

            target.hurt(level.damageSources().playerAttack(player), lance.getDamage() + (float) player.getDeltaMovement().length() * 2);
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));

            event.setCanceled(true);

            dash(false, player, data);
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            CompoundTag data = event.player.getPersistentData();

            if (!data.getBoolean("isTungstenLanceDashing")) {
                return;
            }

            event.player.displayClientMessage(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Lance Dashing..."), true);

            if (event.player.getDeltaMovement().length() < 0.25D) {
                dash(false, event.player, data);
            }
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent e) {
            LivingEntity entity = e.getEntity();

            if (!(entity instanceof Player p)) {
                return;
            }

            CompoundTag data = p.getPersistentData();

            if (data.getBoolean("isTungstenLanceDashing")) {
                Level l = p.level();

                if (!l.isClientSide()) {
                    e.setDistance(0);

                    dash(false, p, data);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = TungstenMod.MODID, value = Dist.CLIENT)
    public static class DashParticles {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.level == null) {
                return;
            }

            CompoundTag data = mc.player.getPersistentData();

            if (!data.getBoolean("isTungstenLanceDashing")) {
                return;
            }

            for (int i = 0; i < 5; i++) {
                mc.level.addParticle(
                        DASH_PARTICLE,
                        mc.player.getX(),
                        mc.player.getY() + 1,
                        mc.player.getZ(),
                        (Math.random() - 0.5) * 10,
                        (Math.random() - 0.5) * 10,
                        (Math.random() - 0.5) * 10
                );
            }
        }
    }
}

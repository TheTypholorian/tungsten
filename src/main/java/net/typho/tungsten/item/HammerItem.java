package net.typho.tungsten.item;

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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.typho.tungsten.TungstenMod;
import net.typho.tungsten.network.HammerSlamPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class HammerItem extends TieredItem implements Vanishable {
    public static final DustParticleOptions SLAM_PARTICLE = new DustParticleOptions(new Vector3f(0.6f, 0f, 0.8f), 2);

    private final SwordItem parent;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public HammerItem(SwordItem parent, Properties prop) {
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

        if (!p_41433_.getCooldowns().isOnCooldown(this) && !p_41433_.getPersistentData().getBoolean("isTungstenHammerSlamming") && !p_41433_.onGround() && !p_41433_.level().isClientSide()) {
            Vec3 vec = p_41433_.getDeltaMovement();
            p_41433_.setDeltaMovement(new Vec3(vec.x(), -5, vec.z()));
            p_41433_.hurtMarked = true;

            slam(true, p_41433_, data);
            p_41433_.getCooldowns().addCooldown(this, 100);
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> p_41423_, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        p_41423_.add(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Right-click while in the air to do a slam attack!"));
    }

    public static void slam(boolean slam, Player player, CompoundTag data) {
        data.putBoolean("isTungstenHammerSlamming", slam);

        if (player instanceof ServerPlayer sp) {
            TungstenMod.INSTANCE.sendTo(
                    new HammerSlamPacket(slam),
                    sp.connection.connection,
                    NetworkDirection.PLAY_TO_CLIENT
            );
        }

        if (slam) {
            player.displayClientMessage(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Hammer Slammed"), true);
        }
    }

    public static class FallDamageEliminator {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
            CompoundTag data = e.player.getPersistentData();

            if (!data.getBoolean("isTungstenHammerSlamming")) {
                return;
            }

            Level l = e.player.level();

            e.player.displayClientMessage(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Hammer Slamming..."), true);

            if (l.isClientSide()) {
                return;
            }

            if (e.player.getDeltaMovement().length() < 0.1) {
                slam(false, e.player, data);
            }

            if (!(e.player.onGround() || e.player.isInWaterOrBubble() || e.player.isInLava())) {
                return;
            }

            ItemStack held = e.player.getMainHandItem();

            if (!(held.getItem() instanceof HammerItem hammer)) {
                slam(false, e.player, data);
                return;
            }

            held.hurtAndBreak((int) e.player.fallDistance, e.player, p1 -> p1.broadcastBreakEvent(EquipmentSlot.MAINHAND));

            l.explode(
                    e.player,
                    e.player.getX(),
                    e.player.getY(),
                    e.player.getZ(),
                    (float) Math.sqrt(e.player.fallDistance) * hammer.getDamage() / 8f,
                    Level.ExplosionInteraction.NONE
            );

            slam(false, e.player, data);
            e.player.fallDistance = 0;
        }
    }

    @Mod.EventBusSubscriber(modid = TungstenMod.MODID, value = Dist.CLIENT)
    public static class SlamParticles {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null || mc.level == null) {
                return;
            }

            CompoundTag data = mc.player.getPersistentData();

            if (!data.getBoolean("isTungstenHammerSlamming")) {
                return;
            }

            for (int i = 0; i < 5; i++) {
                mc.level.addParticle(
                        SLAM_PARTICLE,
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

package net.typho.tungsten;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HammerItem extends TieredItem implements Vanishable {
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

    public float getDestroySpeed(@NotNull ItemStack p_43288_, BlockState p_43289_) {
        if (p_43289_.is(Blocks.COBWEB)) {
            return 15.0F;
        } else {
            return 1.0F;
        }
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

    public boolean isCorrectToolForDrops(BlockState p_43298_) {
        return p_43298_.is(Blocks.COBWEB);
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level p_41432_, Player p_41433_, @NotNull InteractionHand p_41434_) {
        CompoundTag data = p_41433_.getPersistentData();

        if (!p_41433_.getCooldowns().isOnCooldown(this) && !p_41433_.getPersistentData().getBoolean("isTungstenHammerSlamming") && !p_41433_.onGround()) {
            Vec3 vec = p_41433_.getDeltaMovement();
            p_41433_.setDeltaMovement(new Vec3(vec.x(), -5, vec.z()));
            p_41433_.hurtMarked = true;

            data.putBoolean("isTungstenHammerSlamming", true);
            p_41433_.getCooldowns().addCooldown(this, 20);
        }

        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> p_41423_, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        p_41423_.add(Component.literal(ChatFormatting.BLUE + "Right-click while in the air to do a slam attack!"));
        p_41423_.add(Component.literal(ChatFormatting.BLUE + "The damage of this attack is influenced by player"));
        p_41423_.add(Component.literal(ChatFormatting.BLUE + "gravity, which the Apotheosis mod can help with."));
    }

    //@Override
    //public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
    //    return enchantment.category.canEnchant(parent) || super.canApplyAtEnchantingTable(stack, enchantment);
    //}

    public static class FallDamageEliminator {
        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent e) {
            LivingEntity entity = e.getEntity();

            if (!(entity instanceof Player p)) {
                return;
            }

            CompoundTag data = p.getPersistentData();

            if (data.getBoolean("isTungstenHammerSlamming")) {
                e.setDistance(0);
                Vec3 vec = p.getDeltaMovement();
                p.setDeltaMovement(new Vec3(vec.x(), 0.5, vec.z()));
                p.hurtMarked = true;
                data.putBoolean("isTungstenHammerSlamming", false);
            }
        }
    }
}

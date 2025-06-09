package net.typho.tungsten;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FlamethrowerItem extends Item {
    public FlamethrowerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack p_41421_, @Nullable Level p_41422_, @NotNull List<Component> p_41423_, @NotNull TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        p_41423_.add(Component.literal(TungstenMod.ITEM_ACTION_COLOR + "Consumes 1 fire charge per second to throw fire 25 blocks!"));
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);

        return level.isClientSide()
                ? InteractionResultHolder.success(player.getItemInHand(hand))
                : InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player p)) {
            return;
        }

        Inventory inv = p.getInventory();

        if (inv.countItem(Items.FIRE_CHARGE) < 1) {
            entity.stopUsingItem();
            return;
        }

        if (level.isClientSide()) {
            Vec3 look = entity.getLookAngle();

            level.addParticle(
                    ParticleTypes.FLAME,
                    entity.getX() + look.x,
                    entity.getEyeY() + look.y - 0.1,
                    entity.getZ() + look.z,
                    look.x,
                    look.y,
                    look.z
            );

            return;
        }

        byte inc = 0;
        CompoundTag tag;

        if (stack.hasTag()) {
            inc = Objects.requireNonNull(tag = stack.getTag()).getByte("tungstenFlamethrowerInc");
        } else {
            tag = stack.getOrCreateTag();
        }

        inc++;

        if (inc >= 20) {
            inc = 0;

            for (int index = 0; index < inv.getContainerSize(); index++) {
                ItemStack slot = inv.getItem(index);

                if (slot.getItem() == Items.FIRE_CHARGE) {
                    inv.removeItem(index, 1);
                    break;
                }
            }
        }

        tag.putByte("tungstenFlamethrowerInc", inc);

        Vec3 start = entity.getEyePosition(1);
        Vec3 lookVec = entity.getLookAngle();
        Vec3 end = start.add(lookVec.scale(25));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                entity,
                start,
                end,
                entity.getBoundingBox().expandTowards(lookVec.scale(25)),
                e -> !e.isSpectator() && e.isPickable() && e != entity
        );

        BlockHitResult blockHit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                entity
        ));

        double distEntity = (entityHit == null) ? Double.MAX_VALUE : entityHit.getLocation().distanceTo(start);
        double distBlock = blockHit.getLocation().distanceTo(start);

        if (entityHit != null && distEntity < distBlock) {
            LivingEntity target = (LivingEntity) entityHit.getEntity();

            target.setLastHurtByPlayer(p);
            target.setSecondsOnFire(5);

            if (inc == 0) {
                target.hurt(level.damageSources().playerAttack(p), 3.0F);
            }
        } else if (distBlock < Double.MAX_VALUE) {
            BlockPos firePos = blockHit.getBlockPos().relative(blockHit.getDirection());

            if (level.isEmptyBlock(firePos) && level.getBlockState(firePos.below()).isFaceSturdy(level, firePos.below(), Direction.UP)) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }
}

package net.typho.tungsten.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.typho.tungsten.entity.GrenadeProjectile;
import org.jetbrains.annotations.NotNull;

public class GrenadeItem extends Item {
    public GrenadeItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, @NotNull InteractionHand hand) {
        ItemStack held = p.getItemInHand(hand);

        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.TNT_PRIMED, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            GrenadeProjectile grenade = new GrenadeProjectile(level, p);
            grenade.setItem(held);
            grenade.shootFromRotation(p, p.getXRot(), p.getYRot(), 0.0F, 1.5F, 1.0F);
            grenade.power = (int) (Math.random() * 5) + 1;
            level.addFreshEntity(grenade);
        }

        p.awardStat(Stats.ITEM_USED.get(this));

        if (!p.getAbilities().instabuild) {
            held.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide());
    }
}

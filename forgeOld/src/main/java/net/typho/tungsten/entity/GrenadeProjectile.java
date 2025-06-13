package net.typho.tungsten.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.typho.tungsten.TungstenMod;
import org.jetbrains.annotations.NotNull;

public class GrenadeProjectile extends ThrowableItemProjectile {
    public int power = 1;

    public GrenadeProjectile(EntityType<? extends ThrowableItemProjectile> p_37442_, Level p_37443_) {
        super(p_37442_, p_37443_);
    }

    public GrenadeProjectile(Level p_37443_) {
        super(TungstenMod.GRENADE_PROJECTILE.get(), p_37443_);
    }

    public GrenadeProjectile(Level p_37443_, LivingEntity e) {
        super(TungstenMod.GRENADE_PROJECTILE.get(), e, p_37443_);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return TungstenMod.GRENADE.get();
    }

    @Override
    protected void onHit(@NotNull HitResult p_37260_) {
        if (!level().isClientSide()) {
            Vec3 loc = p_37260_.getLocation();

            level().explode(
                    null,
                    loc.x,
                    loc.y,
                    loc.z,
                    power * 4,
                    Level.ExplosionInteraction.TNT
            );
        }

        discard();

        super.onHit(p_37260_);
    }
}

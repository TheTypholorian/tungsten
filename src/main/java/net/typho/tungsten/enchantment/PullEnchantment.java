package net.typho.tungsten.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.typho.tungsten.TungstenMod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = TungstenMod.MODID)
public class PullEnchantment extends Enchantment {
    protected PullEnchantment(Rarity pRarity, EquipmentSlot[] pApplicableSlots) {
        super(pRarity, EnchantmentCategory.WEAPON, pApplicableSlots);
    }

    @Override
    public int getMinCost(int pEnchantmentLevel) {
        return 5 + 20 * (pEnchantmentLevel - 1);
    }

    @Override
    public int getMaxCost(int pEnchantmentLevel) {
        return super.getMinCost(pEnchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    protected boolean checkCompatibility(Enchantment pOther) {
        if (pOther == Enchantments.KNOCKBACK) {
            return false;
        }

        return super.checkCompatibility(pOther);
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity target = event.getEntity();
        LivingEntity attacker = Objects.requireNonNull(target.getLastHurtByMob());

        int pullLevel = attacker.getMainHandItem().getEnchantmentLevel(TungstenMod.PULLBACK.get());

        if (pullLevel < 1) {
            return;
        }

        float strength = event.getStrength() * pullLevel;

        event.setCanceled(true);

        Vec3 diff = new Vec3(
                attacker.getX() - target.getX(),
                attacker.getY() - target.getY(),
                attacker.getZ() - target.getZ()
        );
        double len = Math.sqrt(diff.x * diff.x + diff.y * diff.y + diff.z * diff.z);

        if (len > 0) {
            Vec3 pullMotion = new Vec3(
                    diff.x / len * strength,
                    diff.y / len * strength,
                    diff.z / len * strength
            );

            target.setDeltaMovement(target.getDeltaMovement().add(pullMotion));
        }
    }
}
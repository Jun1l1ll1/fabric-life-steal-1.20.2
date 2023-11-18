package junililli.lifesteal.mixin;

import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import junililli.lifesteal.effect.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    public ItemStack eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
        if (Objects.equals(stack.getItem().toString(), "heart")) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(this);

            if (playerState.extraHearts <= 4) { // (2 or fewer hearts)
                if (playerState.heartsOwned-this.getHealth() <= 0) { // More or the same health as hearts (give hearts)
                    playerState.extraHearts += 2;
                    this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts);
                }
            }
            this.setHealth(playerState.heartsOwned+playerState.extraHearts);

            this.removeStatusEffect(ModEffects.ADD_HEART);
            if (playerState.extraHearts > 0) {
                this.addStatusEffect(new StatusEffectInstance(ModEffects.ADD_HEART, -1, (int) ((playerState.extraHearts/2)-1), false, false));
            }
        }

        return stack;
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damageHEAD(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        PlayerData playerState = StateSaverAndLoader.getPlayerState(this);
        if (playerState.extraHearts > 0) {
            playerState.extraHearts -= (int) amount;
            if (playerState.extraHearts < 0) {
                playerState.extraHearts = 0;
            }
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    public void damageRETURN(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
        PlayerData playerState = StateSaverAndLoader.getPlayerState(this);
        this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts);
    }
}

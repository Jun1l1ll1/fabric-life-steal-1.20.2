package junililli.lifesteal.mixin;


import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import junililli.lifesteal.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin extends Item {
    public MilkBucketItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> ci) {
        PlayerData userState = StateSaverAndLoader.getPlayerState(user);
        if (userState.extraHearts > 0) {
            user.addStatusEffect(new StatusEffectInstance(ModEffects.ADD_HEART, -1, (int) ((userState.extraHearts/2)-1), false, false));
        }

        //return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
    }
}

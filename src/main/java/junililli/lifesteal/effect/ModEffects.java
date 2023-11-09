package junililli.lifesteal.effect;

import junililli.lifesteal.LifeSteal;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static StatusEffect ADD_HEART;

    public static StatusEffect registerStatusEffect(String name) {
        return Registry.register(Registries.STATUS_EFFECT, new Identifier(LifeSteal.MOD_ID, name), new AddHeartEffect(StatusEffectCategory.BENEFICIAL, 3124687));
    }

    public static void registerEffects() {
        ADD_HEART = registerStatusEffect("add_heart").addAttributeModifier(EntityAttributes.GENERIC_MAX_ABSORPTION, "EAE29CF0-701E-4ED6-883A-96F798F3DAB5", 5.0*2, EntityAttributeModifier.Operation.ADDITION);
    }
}

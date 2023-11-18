package junililli.lifesteal.item;

import net.minecraft.item.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent HEART = new FoodComponent.Builder().saturationModifier(0.02f).alwaysEdible().meat().build();
    public static final FoodComponent PERM_HEART = new FoodComponent.Builder().saturationModifier(0.02f).alwaysEdible().meat().build();
}


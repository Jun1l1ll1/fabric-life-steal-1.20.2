package junililli.lifesteal.item;

import junililli.lifesteal.LifeSteal;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SHRED = registerItem("shred", new Item(new FabricItemSettings()));
    public static final Item HEART = registerItem("heart", new Item(new FabricItemSettings().food(ModFoodComponents.HEART)));
    public static final Item PERM_HEART = registerItem("perm_heart", new Item(new FabricItemSettings().food(ModFoodComponents.PERM_HEART)));

    public static void addItemsToIngredientItemGroup(FabricItemGroupEntries entries) {
        entries.add(SHRED);
        entries.add(HEART);
        entries.add(PERM_HEART);
    }

    
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(LifeSteal.MOD_ID, name), item);
    }

    public static void registerModItems() {
        LifeSteal.LOGGER.info("Registering Mod Items for " + LifeSteal.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientItemGroup);
    }
}

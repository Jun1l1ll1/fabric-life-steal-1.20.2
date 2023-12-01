package junililli.lifesteal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import junililli.lifesteal.LifeSteal;
import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Stack;

public class ClaimChristmasCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("christmas_claim").executes(ClaimChristmasCommand::run));
    } // write: /christmas_claim


    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        if (LocalDateTime.now().getMonthValue() == 12 && LocalDateTime.now().getDayOfMonth() <= 24) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(Objects.requireNonNull(context.getSource().getPlayer()));
            //context.getSource().sendMessage(Text.literal(String.valueOf(playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1])));

            if (playerState.christmasClaimed.length == 0) {
                playerState.christmasClaimed = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            }

            if (playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1] == LocalDateTime.now().getDayOfMonth()) {
                context.getSource().sendError(Text.literal("You already claimed the christmas calender for today. Come back tomorrow!"));
            } else {
                playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1] = LocalDateTime.now().getDayOfMonth();

                int amount = Integer.parseInt(LifeSteal.C_PRESENTS[LocalDateTime.now().getDayOfMonth()-1].split(":")[0]);
                String itemID = LifeSteal.C_PRESENTS[LocalDateTime.now().getDayOfMonth()-1].split(":")[1];
                String playerName = context.getSource().getPlayer().getEntityName();

                if (Objects.equals(itemID, "firework_rocket_str_I")) { // 16. des.
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" firework_rocket{Fireworks:{Flight:1}} " + amount + " julenissenkommer4u2morrow");
                }
                else if (Objects.equals(itemID, "firework_rocket_fancy")) { // 20. des.
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" firework_rocket{Fireworks:{Flight:2,Explosions:[{Type:4,Flicker:1b,Colors:[I;15790320,11743532],FadeColors:[I;11743532]},{Type:3,Colors:[I;15790320],FadeColors:[I;4312372]},{Type:1,Flicker:1b,Colors:[I;4312372,11743532],FadeColors:[I;15790320]},{Type:2,Colors:[I;14602026]},{Colors:[I;4312372]}]},display:{Name:'[{\"text\":\"Christmas Rocket\",\"italic\":false,\"color\":\"dark_red\"}]',Lore:['[{\"text\":\"Makes even \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Rudolf\",\"bold\":true},{\"text\":\" fly \",\"bold\":false},{\"text\":\"off course\",\"bold\":false,\"underlined\":true}]']},HideFlags:3} " + amount + " julenissenkommer4u2morrow");
                }
                else if (Objects.equals(itemID, "knockback_II_stick")) { // 22. des.
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" stick{AttributeModifiers:[{AttributeName:\"generic.attack_damage\",Amount:-0.9999999,Operation:2,Name:\"generic.attack_damage\",UUID:[I;-1231030,13633,18208,-27266]}],display:{Name:'[{\"text\":\"Suspicious Stick\",\"italic\":false,\"color\":\"red\"}]',Lore:['[{\"text\":\"Perfect for everyones introverted needs!\",\"italic\":false,\"color\":\"gold\"}]']},Enchantments:[{id:knockback,lvl:2}],HideFlags:3} " + amount + " julenissenkommer4u2morrow");
                }
                else if (Objects.equals(itemID, "christmas_bundle")) { // 24. des.
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" golden_pickaxe{Enchantments:[{id:efficiency,lvl:6}]} 1 julenissenkommer4u2morrow");
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" diamond 32 julenissenkommer4u2morrow");
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" experience_bottle 64 julenissenkommer4u2morrow");
                }
                else { // The rest
                    context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "gift "+playerName+" " + itemID + " " + amount + " julenissenkommer4u2morrow");
                }


                context.getSource().sendMessage(Text.literal("You claimed the christmas calender door " + String.valueOf(LocalDateTime.now().getDayOfMonth()) + "!"));
            }

        } else {
            context.getSource().sendMessage(Text.literal("There are no doors with this date. Only dates from 1/12 to 24/12 has doors."));
        }

        //context.getSource().getServer().getCommandManager().executeWithPrefix(context.getSource(), "give @p firework_rocket{Fireworks:{Flight:2,Explosions:[{Type:4,Flicker:1b,Colors:[I;15790320,11743532],FadeColors:[I;11743532]},{Type:3,Colors:[I;15790320],FadeColors:[I;4312372]},{Type:1,Flicker:1b,Colors:[I;4312372,11743532],FadeColors:[I;15790320]},{Type:2,Colors:[I;14602026]},{Colors:[I;4312372]}]},display:{Name:'[{\"text\":\"Christmas Rocket\",\"italic\":false,\"color\":\"dark_red\"}]',Lore:['[{\"text\":\"Makes even \",\"italic\":false,\"color\":\"dark_green\"},{\"text\":\"Rudolf\",\"bold\":true},{\"text\":\" fly \",\"bold\":false},{\"text\":\"off course\",\"bold\":false,\"underlined\":true}]']},HideFlags:3} 16");


        return 1;
    }

}

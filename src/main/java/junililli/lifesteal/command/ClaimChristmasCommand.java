package junililli.lifesteal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.Objects;

public class ClaimChristmasCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("christmas_claim").executes(ClaimChristmasCommand::run));
    } // write: /christmas_claim


    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        if (LocalDateTime.now().getMonthValue() == 12 && LocalDateTime.now().getDayOfMonth() <= 24) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(Objects.requireNonNull(context.getSource().getPlayer()));
            //context.getSource().sendMessage(Text.literal(String.valueOf(playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1])));
            if (playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1] == LocalDateTime.now().getDayOfMonth()) {
                context.getSource().sendError(Text.literal("You already claimed the christmas calender for today. Come back tomorrow!"));
            } else {
                playerState.christmasClaimed[LocalDateTime.now().getDayOfMonth()-1] = LocalDateTime.now().getDayOfMonth();
                context.getSource().sendMessage(Text.literal("You claimed the christmas calender door " + String.valueOf(LocalDateTime.now().getDayOfMonth()) + "!"));
            }

        } else {
            context.getSource().sendMessage(Text.literal("There are no doors with this date. Only dates from 1/12 to 24/12 has doors."));
        }


        return 1;
    }
}

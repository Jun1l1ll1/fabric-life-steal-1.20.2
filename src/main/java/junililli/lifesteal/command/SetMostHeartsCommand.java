package junililli.lifesteal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

public class SetMostHeartsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("hearts").requires((source) -> {
                return source.hasPermissionLevel(2);
            })
            .then(CommandManager.literal("set_most")
            .then(CommandManager.argument("hearts", IntegerArgumentType.integer(3)).executes((context) -> {
                return run(context, IntegerArgumentType.getInteger(context, "hearts"));
            })))
        );
    } // write: /hearts @a show


    private static int run(CommandContext<ServerCommandSource> context, int hearts) throws CommandSyntaxException {

        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(context.getSource().getServer());
        serverState.mostHeartsOnServer = hearts;
        context.getSource().sendMessage(Text.literal("The tracker of most hearts on server is set to " + serverState.mostHeartsOnServer.toString() + " hearts. "));
        context.getSource().sendError(Text.literal("/heart most command will only work correctly when all players looses/gains hearts, or joins the server again."));

        return 1;
    }
}

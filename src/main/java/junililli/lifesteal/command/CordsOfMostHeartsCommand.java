package junililli.lifesteal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import junililli.lifesteal.PlayerData;
import junililli.lifesteal.StateSaverAndLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class CordsOfMostHeartsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("heart")
            .then(CommandManager.literal("most").executes(CordsOfMostHeartsCommand::run))
        );
    } // write: /heart most


    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(context.getSource().getServer());

        List<PlayerEntity> mostHeartsPlayerNames = new ArrayList<PlayerEntity>();
        for (PlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            if (playerState.heartsOwned >= serverState.mostHeartsOnServer) { // (It should never be over, as it is set to the highest. But just in case)
                mostHeartsPlayerNames.add(player);
                serverState.playerAmountMostHeartsOnServer += ":"+player.getUuidAsString();
            }
        }

        if (!mostHeartsPlayerNames.isEmpty() && serverState.mostHeartsOnServer > 20) {
            context.getSource().sendMessage(Text.literal("Player position has an offset of -50 to 50 on each axis"));
            for (PlayerEntity player : mostHeartsPlayerNames) {
                int x = player.getBlockX() + ThreadLocalRandom.current().nextInt(-50, 50+1);
                int y = player.getBlockY() + ThreadLocalRandom.current().nextInt(-50, 50+1);
                int z = player.getBlockZ() + ThreadLocalRandom.current().nextInt(-50, 50+1);
                context.getSource().sendMessage(Text.literal(player.getEntityName() +" is at ca. "+ x +" "+ y +" "+ z));
            }
        } else if (mostHeartsPlayerNames.isEmpty()) {
            context.getSource().sendError(Text.literal("The player(s) with most stealable hearts is not online at the moment..."));
        } else {
            context.getSource().sendError(Text.literal("No player has over 10 stealable hearts..."));
        }

        return 1;
    }

}

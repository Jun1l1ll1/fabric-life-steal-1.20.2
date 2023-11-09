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

import java.util.Collection;

public class TakeHeartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("hearts").requires((source) -> {
                return source.hasPermissionLevel(2);
            })
            .then(CommandManager.argument("targets", EntityArgumentType.entities())
            .then(CommandManager.literal("take").executes((context) -> {
                return run(context, EntityArgumentType.getPlayers(context, "targets"));
            })))
        );
    } // write: /hearts @a give


    private static int run(CommandContext<ServerCommandSource> context, Collection<? extends PlayerEntity> targets) throws CommandSyntaxException {

        for (PlayerEntity player : targets) {
            PlayerData playerState = StateSaverAndLoader.getPlayerState(player);
            playerState.heartsOwned -= 2;
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts);
            context.getSource().sendMessage(Text.literal("Called take heart " + player.getEntityName()));
        }
        return 1;
    }
}

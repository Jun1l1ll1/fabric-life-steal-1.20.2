package junililli.lifesteal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class SendChristmasGiftCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("gift")
            .then(CommandManager.argument("targets", EntityArgumentType.players())
            .then((RequiredArgumentBuilder)CommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
            .then(CommandManager.argument("count", IntegerArgumentType.integer(1))
            .then(CommandManager.argument("password", StringArgumentType.string()).executes((context) -> {
                return execute((ServerCommandSource)context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "count"), StringArgumentType.getString(context, "password"));
        }))))));
    }

    private static int execute(ServerCommandSource source, ItemStackArgument item, Collection<ServerPlayerEntity> targets, int count, String password) throws CommandSyntaxException {
        if (Objects.equals(password, "julenissenkommer4u2morrow")) {

            int i = item.getItem().getMaxCount();
            int j = i * 100;
            ItemStack itemStack = item.createStack(count, false);
            if (count > j) {
                source.sendError(Text.translatable("commands.give.failed.toomanyitems", new Object[]{j, itemStack.toHoverableText()}));
                return 0;
            } else {
                Iterator var7 = targets.iterator();

                label44:
                while (var7.hasNext()) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var7.next();
                    int k = count;

                    while (true) {
                        while (true) {
                            if (k <= 0) {
                                continue label44;
                            }

                            int l = Math.min(i, k);
                            k -= l;
                            ItemStack itemStack2 = item.createStack(l, false);
                            boolean bl = serverPlayerEntity.getInventory().insertStack(itemStack2);
                            ItemEntity itemEntity;
                            if (bl && itemStack2.isEmpty()) {
                                itemStack2.setCount(1);
                                itemEntity = serverPlayerEntity.dropItem(itemStack2, false);
                                if (itemEntity != null) {
                                    itemEntity.setDespawnImmediately();
                                }

                                serverPlayerEntity.getWorld().playSound((PlayerEntity) null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                                serverPlayerEntity.currentScreenHandler.sendContentUpdates();
                            } else {
                                itemEntity = serverPlayerEntity.dropItem(itemStack2, false);
                                if (itemEntity != null) {
                                    itemEntity.resetPickupDelay();
                                    itemEntity.setOwner(serverPlayerEntity.getUuid());
                                }
                            }
                        }
                    }
                }
            }
            source.sendMessage(Text.literal("You got gifted " + item.getItem().getName().getString()));
        } else {
            source.sendError(Text.literal("You do not have permission to use this command."));
        }
        return targets.size();
    }
}

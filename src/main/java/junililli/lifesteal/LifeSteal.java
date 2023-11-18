package junililli.lifesteal;

import junililli.lifesteal.command.CordsOfMostHeartsCommand;
import junililli.lifesteal.command.GiveHeartCommand;
import junililli.lifesteal.command.ShowHeartsCommand;
import junililli.lifesteal.command.TakeHeartCommand;
import junililli.lifesteal.effect.ModEffects;
import junililli.lifesteal.item.ModItems;
import junililli.lifesteal.util.ModLootTableModifiers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class LifeSteal implements ModInitializer {
	public static final String MOD_ID = "lifesteal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier INITIAL_SYNC = new Identifier(MOD_ID, "initial_sync");
	public boolean lastKillerIsPlayer = false;

	@Override
	public void onInitialize() {

		ModEffects.registerEffects();

		ModItems.registerModItems();

		ModLootTableModifiers.modifyLootTables();

		CommandRegistrationCallback.EVENT.register(GiveHeartCommand::register);
		CommandRegistrationCallback.EVENT.register(TakeHeartCommand::register);
		CommandRegistrationCallback.EVENT.register(ShowHeartsCommand::register);
		CommandRegistrationCallback.EVENT.register(CordsOfMostHeartsCommand::register);



		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
			PacketByteBuf data = PacketByteBufs.create();
			data.writeInt(playerState.heartsOwned + playerState.extraHearts + playerState.permaHearts);
			server.execute(() -> {
				ServerPlayNetworking.send(handler.getPlayer(), INITIAL_SYNC, data);
			});
			(handler.getPlayer()).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts+playerState.permaHearts);

			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			if (playerState.heartsOwned > serverState.mostHeartsOnServer) {
				serverState.mostHeartsOnServer = playerState.heartsOwned;
				serverState.playerAmountMostHeartsOnServer = 1;
			} else if (playerState.heartsOwned == serverState.mostHeartsOnServer) {
				serverState.playerAmountMostHeartsOnServer += 1;
			}
			System.out.println(serverState.mostHeartsOnServer);
			System.out.println(serverState.playerAmountMostHeartsOnServer);
		});


		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof PlayerEntity) {
				((PlayerEntity) entity).sendMessage(Text.of("You died at "+((PlayerEntity) entity).getLastDeathPos().get().getPos().toShortString()));
				if ((damageSource.getAttacker() instanceof PlayerEntity) || (entity.getPrimeAdversary() instanceof PlayerEntity)) {
					lastKillerIsPlayer = true;
					PlayerData playerState = StateSaverAndLoader.getPlayerState(entity);
					PlayerData killerState = damageSource.getAttacker() != null ? StateSaverAndLoader.getPlayerState((LivingEntity) damageSource.getAttacker()) : StateSaverAndLoader.getPlayerState(entity.getPrimeAdversary());
					if (playerState.heartsOwned > 6) { // Killed player has more than 3 stealable hearts
						killerState.heartsOwned += 2;
						if (damageSource.getAttacker() != null) {
							((LivingEntity) damageSource.getAttacker()).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(killerState.heartsOwned + killerState.extraHearts + killerState.permaHearts);
						} else {
							entity.getPrimeAdversary().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(killerState.heartsOwned + killerState.extraHearts + killerState.permaHearts);
						}

						StateSaverAndLoader serverState = damageSource.getAttacker() != null ? StateSaverAndLoader.getServerState(damageSource.getAttacker().getServer()) : StateSaverAndLoader.getServerState(entity.getPrimeAdversary().getServer());
						if (killerState.heartsOwned > serverState.mostHeartsOnServer) {
							serverState.mostHeartsOnServer = killerState.heartsOwned;
							serverState.playerAmountMostHeartsOnServer = 1;
						} else if (killerState.heartsOwned == serverState.mostHeartsOnServer) {
							serverState.playerAmountMostHeartsOnServer += 1;
						}
					}
				} else {
					lastKillerIsPlayer = false;
				}
			}
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			PlayerData playerState = StateSaverAndLoader.getPlayerState(newPlayer);
			if (lastKillerIsPlayer) {
				if (playerState.heartsOwned > 6) {
					playerState.heartsOwned -= 2;

					StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(newPlayer.getServer());
					if (playerState.heartsOwned+2 >= serverState.mostHeartsOnServer) { // Had most hearts on server
						serverState.playerAmountMostHeartsOnServer -= 1;
						if (serverState.playerAmountMostHeartsOnServer <= 0) { // Was the only one with this amount
							serverState.mostHeartsOnServer = playerState.heartsOwned;
							serverState.playerAmountMostHeartsOnServer = 1;
						}
					}
				}
			}
			if (playerState.permaHearts >= 2) {
				playerState.permaHearts -= 2;
			}
			newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts+playerState.permaHearts);
		});
	}
}
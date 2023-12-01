package junililli.lifesteal;

import junililli.lifesteal.command.*;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LifeSteal implements ModInitializer {
	public static final String MOD_ID = "lifesteal";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String[] C_PRESENTS = new String[]{
		"2:diamond", 				// 1. f
		"128:white_concrete", 		// 2. l
		"15:diamond", 				// 3. sunday
		"20:iron_ingot",			// 4. m
		"128:glass", 				// 5. t
		"8:ender_pearl", 			// 6. o
		"128:black_concrete", 		// 7. t
		"64:coal", 					// 8. f
		"192:terracotta", 			// 9. l
		"8:golden_apple", 			// 10. sunday
		"16:emerald", 				// 11. m
		"20:iron_ingot", 			// 12. t
		"128:cyan_concrete", 		// 13. o
		"2:diamond",				// 14. t
		"8:ender_pearl", 			// 15. f
		"32:firework_rocket_str_I",	// 16. l (Fix this in code)
		"2:enchanted_golden_apple",	// 17. sunday
		"128:gray_concrete", 		// 18. m
		"192:terracotta", 			// 19. t
		"16:firework_rocket_fancy",	// 20. o (Fix this in code)
		"32:copper_block", 			// 21. t
		"1:knockback_II_stick", 	// 22. f (Fix this in code)
		"48:bubble_coral_block",	// 23. l
		"1:christmas_bundle",	// 24. sunday + Christmas evening
	};

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
		CommandRegistrationCallback.EVENT.register(SetMostHeartsCommand::register);

		CommandRegistrationCallback.EVENT.register(ClaimChristmasCommand::register);
		CommandRegistrationCallback.EVENT.register(SendChristmasGiftCommand::register);



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
				serverState.playerAmountMostHeartsOnServer = ":"+handler.getPlayer().getUuidAsString();
			} else if ((!serverState.playerAmountMostHeartsOnServer.contains(":"+handler.getPlayer().getUuidAsString())) && (playerState.heartsOwned == serverState.mostHeartsOnServer)) {
				serverState.playerAmountMostHeartsOnServer += ":"+handler.getPlayer().getUuidAsString();
			}
			server.sendMessage(Text.literal(serverState.playerAmountMostHeartsOnServer));
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
							serverState.playerAmountMostHeartsOnServer = ":"+(damageSource.getAttacker() != null ? damageSource.getAttacker().getUuidAsString() : entity.getPrimeAdversary().getUuidAsString());
						} else if (killerState.heartsOwned == serverState.mostHeartsOnServer) {
							serverState.playerAmountMostHeartsOnServer += ":" + (damageSource.getAttacker() != null ? damageSource.getAttacker().getUuidAsString() : entity.getPrimeAdversary().getUuidAsString());
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
						serverState.playerAmountMostHeartsOnServer = serverState.playerAmountMostHeartsOnServer.replace(":"+newPlayer.getUuidAsString(), "");
						if (serverState.playerAmountMostHeartsOnServer.isEmpty()) { // Was the only one with this amount
							serverState.mostHeartsOnServer = playerState.heartsOwned;
							for (ServerPlayerEntity player : newPlayer.getServer().getPlayerManager().getPlayerList()) {
								PlayerData thisPlayerState = StateSaverAndLoader.getPlayerState(player);
								if (thisPlayerState.heartsOwned == serverState.mostHeartsOnServer) {
									serverState.playerAmountMostHeartsOnServer += ":" + player.getUuidAsString();
								}
							}
						}
					}
				}
			}
			if (playerState.permaHearts >= 2) {
				playerState.permaHearts -= 2;
			}
			newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts+playerState.permaHearts);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PlayerData playerState = StateSaverAndLoader.getPlayerState(newPlayer);

			if (playerState.permaHearts > 0) {
				newPlayer.addStatusEffect(new StatusEffectInstance(ModEffects.ADD_PERM_HEART, -1, (int) ((playerState.permaHearts/2)-1), false, false, true));
			}
		});
	}
}
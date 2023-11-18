package junililli.lifesteal;

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



		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			PlayerData playerState = StateSaverAndLoader.getPlayerState(handler.getPlayer());
			PacketByteBuf data = PacketByteBufs.create();
			data.writeInt(playerState.heartsOwned + playerState.extraHearts);
			server.execute(() -> {
				ServerPlayNetworking.send(handler.getPlayer(), INITIAL_SYNC, data);
			});
			(handler.getPlayer()).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts);
		});


		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (entity instanceof PlayerEntity) {
				((PlayerEntity) entity).sendMessage(Text.of("You died at "+((PlayerEntity) entity).getLastDeathPos().get().getPos().toShortString()));
				if ((damageSource.getAttacker() instanceof PlayerEntity) || (entity.getPrimeAdversary() instanceof PlayerEntity)) {
					lastKillerIsPlayer = true;
					PlayerData playerState = StateSaverAndLoader.getPlayerState(entity);
					PlayerData killerState = damageSource.getAttacker() != null ? StateSaverAndLoader.getPlayerState((LivingEntity) damageSource.getAttacker()) : StateSaverAndLoader.getPlayerState(entity.getPrimeAdversary());
					if (playerState.heartsOwned > 6) {
						killerState.heartsOwned += 2;
						if (damageSource.getAttacker() != null) {
							((LivingEntity) damageSource.getAttacker()).getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(killerState.heartsOwned + killerState.extraHearts);
						} else {
							entity.getPrimeAdversary().getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(killerState.heartsOwned + killerState.extraHearts);
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
				}
			}
			newPlayer.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(playerState.heartsOwned+playerState.extraHearts);
		});
	}
}
package junililli.lifesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public class LifeStealClient implements ClientModInitializer {
	public static PlayerData playerData = new PlayerData();
	@Override
	public void onInitializeClient() {
		/*ClientPlayNetworking.registerGlobalReceiver(LifeSteal.INITIAL_SYNC, (client, handler, buf, responseSender) -> {
			playerData.heartsOwned = buf.readInt();

			client.execute(() -> {
				client.player.sendMessage(Text.literal("Your hp: " + (playerData.heartsOwned)));
			});
		});*/ // To show the player their hp when joining the server.
	}
}
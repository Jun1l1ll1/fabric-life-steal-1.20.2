package junililli.lifesteal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public Integer mostHeartsOnServer = 0;
    public String playerAmountMostHeartsOnServer = ""; // eks.: "Jojouno:Iverlynet:Minnimina"
    public HashMap<UUID, PlayerData> players = new HashMap<>();


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("mostHeartsOnServer", mostHeartsOnServer);
        nbt.putString("playerAmountMostHeartsOnServer", playerAmountMostHeartsOnServer);

        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putInt("heartsOwned", playerData.heartsOwned);
            playerNbt.putInt("extraHearts", playerData.extraHearts);
            playerNbt.putInt("permaHearts", playerData.permaHearts);
            playerNbt.putIntArray("christmasClaimed", playerData.christmasClaimed);

            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        state.mostHeartsOnServer = tag.getInt("mostHeartsOnServer");
        state.playerAmountMostHeartsOnServer = tag.getString("playerAmountMostHeartsOnServer");

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();

            playerData.heartsOwned = playersNbt.getCompound(key).getInt("heartsOwned");
            playerData.extraHearts = playersNbt.getCompound(key).getInt("extraHearts");
            playerData.permaHearts = playersNbt.getCompound(key).getInt("permaHearts");
            playerData.christmasClaimed = playersNbt.getCompound(key).getIntArray("christmasClaimed");

            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });

        return state;
    }


    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, LifeSteal.MOD_ID);

        state.markDirty();

        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        StateSaverAndLoader serverState = getServerState(player.getWorld().getServer());

        PlayerData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());

        return playerState;
    }
}
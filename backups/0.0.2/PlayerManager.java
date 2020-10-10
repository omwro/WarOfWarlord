package omwro.warofwarlord;

import org.bukkit.entity.Player;

import java.util.HashMap;

class PlayerManager {
    private static HashMap<Player, WowPlayer> playerList = new HashMap<>();

    static WowPlayer getWowPlayer(Player player) {
        return playerList.get(player);
    }

    static void setWowPlayer(Player player) {
        WowPlayer wowPlayer =  new WowPlayer(player);
        PlayerManager.playerList.put(player, wowPlayer);
    }
}

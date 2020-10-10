package omwro.warofwarlord;

import org.bukkit.entity.Player;

import java.util.HashMap;

class PlayerManager {
    private static HashMap<Player, WowPlayer> playerList = new HashMap<>();

    static WowPlayer getWowPlayer(Player player) {
        return playerList.get(player);
    }

    static void addWowPlayer(Player player) {
        WowPlayer wowPlayer =  new WowPlayer(player);
        PlayerManager.playerList.put(player, wowPlayer);
    }

    static void updateWowPlayer(WowPlayer wp){
        playerList.replace(wp.getPlayer(), wp);
    }
}

package omwro.warofwarlord;

import org.bukkit.entity.Player;

import java.util.HashMap;

class PlayerManager {
    private static HashMap<Player, WowPlayer> playerList = new HashMap<>();

    static WowPlayer getWowPlayer(Player player) {
        return playerList.get(player);
    }

    static WowPlayer addWowPlayer(Player player) {
        WowPlayer wowPlayer =  new WowPlayer(player);
        return PlayerManager.playerList.put(player, wowPlayer);
    }

    static WowPlayer updateWowPlayer(WowPlayer wp){
        return playerList.put(wp.getPlayer(), wp);
    }
}

package omwro.warofwarlord;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

class Utils {

    static void tp(Player p, Location l){
        p.teleport(l);
    }

    static WowPlayer randomPlayer(List<WowPlayer> queue){
        int randomInt = (int) (Math.random() * queue.size());
        return queue.get(randomInt);
    }

    static Long timeBetween(LocalTime t1, LocalTime t2){
        return ChronoUnit.MILLIS.between(t1, t2);
    }

    static void broadcast(String msg){
        Plugin.server.broadcastMessage(ChatColor.GREEN + "BROADCAST: "+msg);
    }

    static void error(Player p, String msg){
        p.sendMessage(ChatColor.RED + msg);
    }

    static void warning(Player p, String msg){
        p.sendMessage(ChatColor.GOLD + msg);
    }


    static void pm(Player p, String msg){
        p.sendMessage(msg);
    }
}

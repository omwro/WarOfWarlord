package omwro.warofwarlord;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static omwro.warofwarlord.Plugin.*;

/**
 * @author Omer Erdem
 */

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(
            CommandSender sender,
            Command cmd,
            String label,
            String[] args) {
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("join")) {
            game1.join(p);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("leave")) {
            game1.leave(p);
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("start")) {
            game1.startGame();
            return true;
        }
        else if (cmd.getName().equalsIgnoreCase("check")) {
            game1.check();
            return true;
        }
        return false;
    }

    static void broadcast(String msg){
        server.broadcastMessage("BROADCAST: "+msg);
    }

    static void pm(Player p, String msg){
        p.sendMessage(msg);
    }
}

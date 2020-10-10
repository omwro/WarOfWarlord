package omwro.warofwarlord;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static omwro.warofwarlord.Utils.*;

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
        Player p = (Player)sender;
        if (PlayerManager.getWowPlayer(p) == null){
            PlayerManager.setWowPlayer(p);
        }
        WowPlayer wp = PlayerManager.getWowPlayer(p);

        if (cmd.getName().equalsIgnoreCase("wow")) {
            if (args.length == 0){
                Plugin.game1.check(wp);
                return true;
            } else if (args[0].equals("start")){
                Plugin.game1.startGame(wp);
                return true;
            } else if (args[0].equals("join")){
                Plugin.game1.join(wp);
                return true;
            } else if (args[0].equals("leave")){
                Plugin.game1.leave(wp);
                return true;
            } else {
                error(wp.getPlayer(), ChatColor.RED + "Invalid command");
            }
        }
        return false;
    }
}

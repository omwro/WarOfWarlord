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
            PlayerManager.addWowPlayer(p);
        }

        if (cmd.getName().equalsIgnoreCase("wow")) {
            if (args.length == 0){
                Plugin.game1.check(p);
                return true;
            } else if (args[0].equals("start")){
                Plugin.game1.preStart(p);
                return true;
            } else if (args[0].equals("join")){
                Plugin.game1.join(p);
                return true;
            } else if (args[0].equals("leave")){
                Plugin.game1.leave(p);
                return true;
            } else if (args[0].equals("admin")) {
                Plugin.game1.checkAdvanced(p);
                return true;
        } else {
                error(p, ChatColor.RED + "Invalid command");
            }
        }
        return false;
    }
}

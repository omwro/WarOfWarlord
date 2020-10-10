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
        Player p = (Player) sender;
        if (PlayerManager.getWowPlayer(p) == null) {
            PlayerManager.addWowPlayer(p);
        }

        if (cmd.getName().equalsIgnoreCase("wow")) {
            if (args.length == 0) {
                GameManager.getGamemodes().forEach((k, v) -> {
                    v.check(p);
                });
                return true;
            } else if (args[0].equals("start")) {
                try {
                    PlayerManager.getWowPlayer(p).getQueuedGamemode().preStart(p);
                } catch (Exception e) {
                    error(p, ChatColor.RED + "Something went wrong");
                }
                return true;
            } else if (args[0].equals("join")) {
                try {
                    GameManager.getGamemodeByID(Integer.parseInt(args[1])).join(p);
                } catch (Exception e) {
                    error(p, ChatColor.RED + "Invalid command");
                }
                return true;
            } else if (args[0].equals("leave")) {
                try {
                    PlayerManager.getWowPlayer(p).getQueuedGamemode().leave(p);
                } catch (Exception e) {
                    error(p, ChatColor.RED + "Something went wrong");
                }
                return true;
            } else if (args[0].equals("admin")) {
                GameManager.getGamemodes().forEach((k, v) -> {
                    v.checkAdvanced(p);
                });
                return true;
            } else {
                error(p, ChatColor.RED + "Invalid command");
            }
        }
        return false;
    }
}

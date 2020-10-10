package omwro.warofwarlord;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * @author Omer Erdem
 * This plugin is a gamemode called War of Warlords in request for Cheif
 */

public class Plugin extends JavaPlugin {
    static Server server;
    static Location lobbyLocation, startLocation;
    static Gamemode game1;

    static final int MAX_AMOUNT_PLAYERS = 2;
    static int playTime = 30; // in seconds
    static List<PotionEffect> warlordBuffs = Arrays.asList(
            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, playTime*20, 2),
            new PotionEffect(PotionEffectType.REGENERATION, playTime*20, 2)
    );

    @Override
    public void onEnable() {
        getLogger().info("PLUGIN STARTING");
        server = this.getServer();
        World world = server.getWorld("world");
        assert world != null;
        lobbyLocation = new Location(world, 0, world.getHighestBlockYAt(0, 0), 0);
        startLocation = new Location(world, 100, world.getHighestBlockYAt(100, 100), 100);
        game1 = new Gamemode(this);

        getServer().getPluginManager().registerEvents(new Gamemode(this), this);

        this.getCommand("join").setExecutor((CommandExecutor) new Commands());
        this.getCommand("leave").setExecutor((CommandExecutor) new Commands());
        this.getCommand("start").setExecutor((CommandExecutor) new Commands());
        this.getCommand("check").setExecutor((CommandExecutor) new Commands());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("PLUGIN DISABLING");
    }
}

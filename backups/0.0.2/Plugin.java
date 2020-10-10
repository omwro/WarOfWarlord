package omwro.warofwarlord;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Omer Erdem
 * This plugin is a gamemode called War of Warlords in request for Cheif
 */

public class Plugin extends JavaPlugin {
    static Server server;

    private FileConfiguration gamemodeConfig;
    private File gamemodeFile;

    static Location spawnLocation, lobbyLocation, startLocation;
    static Gamemode game1;
    static int maxAmountPlayers, playTime;
    static List<PotionEffect> warlordBuffs;
    private World world;

    @Override
    public void onEnable() {
        getLogger().info("PLUGIN STARTING");
        server = this.getServer();
        setupConfig();

        game1 = new Gamemode(this, 1);

        getServer().getPluginManager().registerEvents(new Gamemode(this, 1), this);

        this.getCommand("wow").setExecutor((CommandExecutor) new Commands());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("PLUGIN DISABLING");
    }

    private void setupConfig() {
        if (!this.getDataFolder().exists()){
            this.getDataFolder().mkdir();
        }
        gamemodeFile = new File(this.getDataFolder(), "warofwarlord.yml");
        if (!gamemodeFile.exists()){
            try {
                gamemodeFile.createNewFile();
                gamemodeConfig = YamlConfiguration.loadConfiguration(gamemodeFile);
                gamemodeConfig.set("MAX_AMOUNT_PLAYERS", 12);
                gamemodeConfig.set("PLAY_TIME_IN_SECONDS", 30);
                gamemodeConfig.set("WORLD_NAME", "world");
                List<String> effects = Arrays.asList("INCREASE_DAMAGE", "REGENERATION");
                List<Integer> amplifiers = Arrays.asList(2, 2);
                gamemodeConfig.set("WARLORD_BUFFS.EFFECTS", effects);
                gamemodeConfig.set("WARLORD_BUFFS.AMPLIFIERS", amplifiers);
                gamemodeConfig.set("LOCATIONS.SPAWN.X", 0);
                gamemodeConfig.set("LOCATIONS.SPAWN.Z", 0);
                gamemodeConfig.set("LOCATIONS.LOBBY.X", 0);
                gamemodeConfig.set("LOCATIONS.LOBBY.Z", 0);
                gamemodeConfig.set("LOCATIONS.START.X", 100);
                gamemodeConfig.set("LOCATIONS.START.Z", 100);
                gamemodeConfig.save(gamemodeFile);
                server.getConsoleSender().sendMessage(ChatColor.GREEN+"warofwarlord.yml file has been created");

            } catch (IOException e) {
                server.getConsoleSender().sendMessage(ChatColor.RED+"Could not create the warofwarlord.yml file");
            }
        }
        loadConfig();
    }

    private void loadConfig(){
        try {
            gamemodeConfig = YamlConfiguration.loadConfiguration(gamemodeFile);
            maxAmountPlayers = (int) gamemodeConfig.get("MAX_AMOUNT_PLAYERS");
            playTime = (int) gamemodeConfig.get("PLAY_TIME_IN_SECONDS");
            world = server.getWorld((String) gamemodeConfig.get("WORLD_NAME"));

            warlordBuffs = new ArrayList<>();
            List<String> effectList = (List<String>) gamemodeConfig.getList("WARLORD_BUFFS.EFFECTS");
            List<Integer> amplifiertList = (List<Integer>) gamemodeConfig.getList("WARLORD_BUFFS.AMPLIFIERS");
            for (int i = 0; i < effectList.size(); i++) {
                warlordBuffs.add(
                        new PotionEffect(
                                PotionEffectType.getByName(effectList.get(i)),
                                playTime*20,
                                amplifiertList.get(i)));
            }

            spawnLocation = new Location(
                    world,
                    (Integer) gamemodeConfig.get("LOCATIONS.SPAWN.X"),
                    world.getHighestBlockYAt(
                            (Integer) gamemodeConfig.get("LOCATIONS.SPAWN.X"),
                            (Integer) gamemodeConfig.get("LOCATIONS.SPAWN.Z")),
                    (Integer) gamemodeConfig.get("LOCATIONS.SPAWN.Z"));
            lobbyLocation = new Location(
                    world,
                    (Integer) gamemodeConfig.get("LOCATIONS.LOBBY.X"),
                    world.getHighestBlockYAt(
                            (Integer) gamemodeConfig.get("LOCATIONS.LOBBY.X"),
                            (Integer) gamemodeConfig.get("LOCATIONS.LOBBY.Z")),
                    (Integer) gamemodeConfig.get("LOCATIONS.LOBBY.Z"));
            startLocation = new Location(
                    world,
                    (Integer) gamemodeConfig.get("LOCATIONS.START.X"),
                    world.getHighestBlockYAt(
                            (Integer) gamemodeConfig.get("LOCATIONS.START.X"),
                            (Integer) gamemodeConfig.get("LOCATIONS.START.Z")),
                    (Integer) gamemodeConfig.get("LOCATIONS.START.Z"));

            world.setSpawnLocation(spawnLocation);

            server.getConsoleSender().sendMessage(ChatColor.GREEN+"warofwarlord.yml file has been loaded");
        } catch (Exception e){
            server.getConsoleSender().sendMessage(ChatColor.RED+"Could not load the warofwarlord.yml file");
            server.getConsoleSender().sendMessage(ChatColor.RED+e.toString());
        }
    }
}

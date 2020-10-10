package omwro.warofwarlord;

import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.bukkit.ChatColor.GREEN;

/**
 * @author Omer Erdem
 * This plugin is a gamemode called War of Warlords in request for Cheif
 */

public class Plugin extends JavaPlugin implements Listener {
    static Server server;

    private FileConfiguration cfg;
    private File file;

    static Location lobbyLocation;
    static java.util.Map<String, Map> mapslist;
    static int maxAmountPlayers, playTime;
    static List<PotionEffect> warlordBuffs;

    static List<ItemStack> warlordArmor;
    static List<ItemStack> soldierArmor;

    static List<ItemStack> warlordWeapon;
    static List<ItemStack> soldierWeapon;

    static List<Potion> warlordPotion;
    static List<Potion> soldierPotion;

    static List<PotionEffect> warlordEffect;
    static List<PotionEffect> soldierEffect;

    @Override
    public void onEnable() {
        LocalDateTime license = LocalDateTime.of(2020, 5, 1, 0, 0);
        if (LocalDateTime.now().compareTo(license) < 0) {
            getLogger().info("PLUGIN STARTING");
            server = this.getServer();

            setupConfig();

            GameManager.getGamemodes().forEach((k,v) -> {
                server.getPluginManager().registerEvents((Gamemode) v, this);
            });

            this.getCommand("wow").setExecutor((CommandExecutor) new Commands());
        } else {
            getLogger().info("WAR OF WARLORD ERROR: Could not start the War Of Warlord Plugin. "+
                    "Please refesh your license key by contacting the developer");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("PLUGIN DISABLING");
    }

    private void setupConfig() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        file = new File(this.getDataFolder(), "warofwarlord.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
                cfg = YamlConfiguration.loadConfiguration(file);
                this.setDefaultConfig();
                cfg.save(file);
                server.getConsoleSender().sendMessage(GREEN + "warofwarlord.yml file has been created");

            } catch (IOException e) {
                server.getConsoleSender().sendMessage(ChatColor.RED + "Could not create the warofwarlord.yml file");
            }
        }
        loadConfig();
    }

    private void loadConfig() {
        try {
            cfg = YamlConfiguration.loadConfiguration(file);
            maxAmountPlayers = (int) cfg.get("MAX_AMOUNT_PLAYERS");
            playTime = (int) cfg.get("PLAY_TIME_IN_SECONDS");

            lobbyLocation = XYZ.getLocation(
                    server.getWorld((String) cfg.get("LOCATIONS.LOBBY.WORLD")),
                    (Integer) cfg.get("LOCATIONS.LOBBY.X"),
                    (Integer) cfg.get("LOCATIONS.LOBBY.Y"),
                    (Integer) cfg.get("LOCATIONS.LOBBY.Z"));

            mapslist = new TreeMap<>();
            ConfigurationSection cfgMaps = cfg.getConfigurationSection("MAPS");
            Set<String> mapKeys = cfgMaps.getKeys(false);
            for (String mapIndex : mapKeys) {
                ConfigurationSection cfgMap = cfgMaps.getConfigurationSection(mapIndex);
                String mapName = (String) cfgMap.get("NAME");
                World mapWorld = server.getWorld((String) cfgMap.get("WORLD"));
                mapWorld.setSpawnLocation(XYZ.getLocation(
                        mapWorld,
                        (Integer) cfgMap.get("SPAWNLOCATION.X"),
                        (Integer) cfgMap.get("SPAWNLOCATION.Y"),
                        (Integer) cfgMap.get("SPAWNLOCATION.Z")));

                ConfigurationSection cfgStartLoc = cfgMap.getConfigurationSection("STARTLOCATION");
                Set<String> mapLocKeys = cfgStartLoc.getKeys(false);
                List<Location> mapLocations = new ArrayList<>();
                for (String locIndex : mapLocKeys) {
                    ConfigurationSection cfgLoc = cfgStartLoc.getConfigurationSection(locIndex);
                    mapLocations.add(XYZ.getLocation(
                            mapWorld,
                            (Integer) cfgLoc.get("X"),
                            (Integer) cfgLoc.get("Y"),
                            (Integer) cfgLoc.get("Z")));
                }
                mapslist.put(mapName, new Map(mapName, mapWorld, mapLocations));
            }

            List<String> games = (List<String>) cfg.getList("GAMES");
            for (int i = 0; i < games.size(); i++) {
                Map map = mapslist.get(cfg.getList("GAMES").get(i));
                ++i;
                GameManager.addGamemode(new Gamemode(this, i, map));
                --i;
            }

            this.loadKits();

            server.getConsoleSender().sendMessage(GREEN + "warofwarlord.yml file has been loaded");
        } catch (Exception e) {
            server.getConsoleSender().sendMessage(ChatColor.RED + "Could not load the warofwarlord.yml file");
            server.getConsoleSender().sendMessage(ChatColor.RED + e.toString());
        }
    }

    private void setDefaultConfig() {
        cfg.set("MAX_AMOUNT_PLAYERS", 12);
        cfg.set("PLAY_TIME_IN_SECONDS", 30);

        cfg.set("KITS.WARLORD.ARMOR.GOLDEN_HELMET.protection", 3);
        cfg.set("KITS.WARLORD.ARMOR.GOLDEN_HELMET.unbreaking", 2);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_CHESTPLATE.protection", 2);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_CHESTPLATE.unbreaking", 1);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_LEGGINGS.protection", 2);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_LEGGINGS.unbreaking", 1);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_BOOTS.protection", 2);
        cfg.set("KITS.WARLORD.ARMOR.DIAMOND_BOOTS.unbreaking", 1);
        cfg.set("KITS.WARLORD.WEAPON.DIAMOND_SWORD.sharpness", 4);
        cfg.set("KITS.WARLORD.WEAPON.BOW.power", 2);
        cfg.set("KITS.WARLORD.WEAPON.BOW.infinity", 1);
        cfg.set("KITS.WARLORD.WEAPON.ARROW", 1);
        cfg.set("KITS.WARLORD.POTION.INSTANT_HEAL", 1);
        cfg.set("KITS.WARLORD.POTION.INSTANT_DAMAGE", 1);

        cfg.set("KITS.SOLDIER.ARMOR.IRON_HELMET", 1);
        cfg.set("KITS.SOLDIER.ARMOR.IRON_CHESTPLATE", 1);
        cfg.set("KITS.SOLDIER.ARMOR.IRON_LEGGINGS", 1);
        cfg.set("KITS.SOLDIER.ARMOR.IRON_BOOTS", 1);
        cfg.set("KITS.SOLDIER.WEAPON.IRON_SWORD", 1);
        cfg.set("KITS.SOLDIER.WEAPON.BOW.power", 1);
        cfg.set("KITS.SOLDIER.WEAPON.BOW.infinity", 1);
        cfg.set("KITS.SOLDIER.WEAPON.ARROW", 1);
        cfg.set("KITS.SOLDIER.EFFECT.SPEED", 0);

        cfg.set("LOCATIONS.LOBBY.WORLD", "world");
        cfg.set("LOCATIONS.LOBBY.X", 0);
        cfg.set("LOCATIONS.LOBBY.Y", 0);
        cfg.set("LOCATIONS.LOBBY.Z", 15);

        List<XYZ> XYZList = new ArrayList<>();
        XYZList.add(new XYZ(0, 0, 0));
        XYZList.add(new XYZ(0, 0, 2));
        XYZList.add(new XYZ(0, 0, 4));
        XYZList.add(new XYZ(0, 0, 6));
        XYZList.add(new XYZ(0, 0, 8));
        XYZList.add(new XYZ(2, 0, 10));
        XYZList.add(new XYZ(2, 0, 0));
        XYZList.add(new XYZ(2, 0, 2));
        XYZList.add(new XYZ(2, 0, 4));
        XYZList.add(new XYZ(2, 0, 6));
        XYZList.add(new XYZ(2, 0, 8));
        XYZList.add(new XYZ(2, 0, 10));

        List<Map> maplist = new ArrayList<>();
        maplist.add(new Map("The Forest", "world", XYZList));
        maplist.add(new Map("Antartica", "world", XYZList));

        ConfigurationSection cfgMaps = cfg.createSection("MAPS");
        for (int i = 0; i < maplist.size(); i++) {
            Map map = maplist.get(i);
            ConfigurationSection cfgMap = cfgMaps.createSection(map.name);
            cfgMap.set("NAME", map.name);
            cfgMap.set("WORLD", map.worldname);
            cfgMap.set("SPAWNLOCATION.X", 0);
            cfgMap.set("SPAWNLOCATION.Y", 0);
            cfgMap.set("SPAWNLOCATION.Z", -5);
            ConfigurationSection cfgLoc = cfgMap.createSection("STARTLOCATION");
            for (int j = 0; j < map.XYZList.size(); j++) {
                ConfigurationSection cfgLocIndex = cfgLoc.createSection(String.valueOf(j));
                XYZ loc = map.XYZList.get(j);
                cfgLocIndex.set("X", loc.x);
                cfgLocIndex.set("Y", loc.y);
                cfgLocIndex.set("Z", loc.z);
            }
        }

        List<String> games = new ArrayList<>();
        games.add("The Forest");
        games.add("Antartica");
        games.add("The Forest");
        cfg.set("GAMES", games);
    }

    private void loadKits() {
        warlordArmor = this.loadArmor(cfg.getConfigurationSection("KITS.WARLORD.ARMOR"));
        soldierArmor = this.loadArmor(cfg.getConfigurationSection("KITS.SOLDIER.ARMOR"));

        warlordWeapon = this.loadWeapon(cfg.getConfigurationSection("KITS.WARLORD.WEAPON"));
        soldierWeapon = this.loadWeapon(cfg.getConfigurationSection("KITS.SOLDIER.WEAPON"));

        warlordPotion = this.loadPotions(cfg.getConfigurationSection("KITS.WARLORD.POTION"));
        soldierPotion = this.loadPotions(cfg.getConfigurationSection("KITS.SOLDIER.POTION"));

        warlordEffect = this.loadEffect(cfg.getConfigurationSection("KITS.WARLORD.EFFECT"));
        soldierEffect = this.loadEffect(cfg.getConfigurationSection("KITS.SOLDIER.EFFECT"));
    }

    private List<ItemStack> loadArmor(ConfigurationSection cfgSection){
        List<ItemStack> arr = new ArrayList<>();
        if (cfgSection != null){
            Set<String> armorKeys = cfgSection.getKeys(false);
            for (String armorItem : armorKeys) {
                ItemStack item = new ItemStack(Material.getMaterial(armorItem));
                if (cfgSection.getConfigurationSection(armorItem) != null) {
                    item = addEnchants(item, cfgSection.getConfigurationSection(armorItem));
                }
                arr.add(item);
            }
        }
        return arr;
    }

    private List<ItemStack> loadWeapon(ConfigurationSection cfgSection){
        List<ItemStack> arr = new ArrayList<>();
        if (cfgSection != null) {
            Set<String> weaponKeys = cfgSection.getKeys(false);
            for (String weaponItem : weaponKeys) {
                ItemStack item = new ItemStack(Material.getMaterial(weaponItem));
                if (cfgSection.getConfigurationSection(weaponItem) != null) {
                    item = addEnchants(item, cfgSection.getConfigurationSection(weaponItem));
                }
                arr.add(item);
            }
        }
        return arr;
    }

    private ItemStack addEnchants(ItemStack item, ConfigurationSection cfgSection){
        if (cfgSection != null) {
            Set<String> enchantKeys = cfgSection.getKeys(false);
            for (String enchant : enchantKeys) {
                item.addEnchantment(
                        Enchantment.getByKey(NamespacedKey.minecraft(enchant)),
                        cfgSection.getInt(enchant));
            }
        }
        return item;
    }

    private List<Potion> loadPotions(ConfigurationSection cfgSection) {
        List<Potion> arr = new ArrayList<>();
        if (cfgSection != null) {
            Set<String> potionKeys = cfgSection.getKeys(false);
            for (String potionItem : potionKeys) {
                Potion potion = new Potion(PotionType.valueOf(potionItem), cfgSection.getInt(potionItem));
                potion.setSplash(true);
                arr.add(potion);
            }
        }
        return arr;
    }

    private List<PotionEffect> loadEffect(ConfigurationSection cfgSection) {
        List<PotionEffect> arr = new ArrayList<>();
        if (cfgSection != null) {
            Set<String> effectKeys = cfgSection.getKeys(false);
            for (String effectItem : effectKeys) {
                arr.add(new PotionEffect(
                        PotionEffectType.getByName(effectItem),
                        playTime * 20,
                        (Integer) cfgSection.get(effectItem)));
            }
        }
        return arr;
    }
}

package omwro.warofwarlord;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static omwro.warofwarlord.Leaderboard.*;
import static omwro.warofwarlord.Plugin.*;
import static omwro.warofwarlord.Utils.*;

/**
 * @author Omer Erdem
 * Developer Note: most variables are static because the eventhandler function does not accept public/protected/private
 * scopes. Keep that in mind.
 */

public class Gamemode implements Listener {
    private JavaPlugin plugin;
    private int gameID;

    private List<WowPlayer> queue = new ArrayList<>();
    private Map map;
    private WowPlayer warlord;
    private boolean active;
    private BukkitRunnable timerStart;
    private BukkitRunnable timerEnd;
    private BukkitRunnable timerActive;

    Gamemode(JavaPlugin plugin, int gameID, Map map) {
        this.plugin = plugin;
        this.gameID = gameID;
        this.active = false;
        this.map = map;
    }

    private void initRunnable() {
        timerStart = new BukkitRunnable() {
            int x = 10;

            public void run() {
                if (x > 0) {
                    playSound("countdown");
                    x--;
                } else {
                    start();
                    this.cancel();
                }
            }
        };
        timerActive = new BukkitRunnable() {
            int timeLeft = playTime;
            int countdown = 10;

            public void run() {
                if (timeLeft > 0) {
                    warning(warlord.getPlayer(), timeLeft + " seconds left!");
                    if (timeLeft <= countdown) {
                        playSound("countdown");
                    }
                    timeLeft--;
                } else {
                    endGame();
                    this.cancel();
                }
            }
        };
        timerEnd = new BukkitRunnable() {
            int x = 10;

            public void run() {
                if (x > 0) {
                    x--;
                } else {
                    end();
                    this.cancel();
                }
            }
        };
    }

    int getGameID() {
        return gameID;
    }

    private void getKitWarlord(Player p) {
        for (ItemStack item : warlordArmor) {
            if (item.getType().toString().contains("HELMET")) {
                p.getEquipment().setHelmet(item);
            } else if (item.getType().toString().contains("CHESTPLATE")) {
                p.getEquipment().setChestplate(item);
            } else if (item.getType().toString().contains("LEGGINGS")) {
                p.getEquipment().setLeggings(item);
            } else if (item.getType().toString().contains("BOOTS")) {
                p.getEquipment().setBoots(item);
            }
        }
        for (ItemStack item : warlordWeapon) {
            p.getInventory().addItem(item);
        }
        for (Potion potion : warlordPotion) {
            p.getInventory().addItem(potion.toItemStack(2));
        }
        for (PotionEffect effect : warlordEffect) {
            p.addPotionEffect(effect);
        }
    }

    private void getKitSoldier(Player p) {
        for (ItemStack item : soldierArmor) {
            if (item.getType().toString().contains("HELMET")) {
                p.getEquipment().setHelmet(item);
            } else if (item.getType().toString().contains("CHESTPLATE")) {
                p.getEquipment().setChestplate(item);
            } else if (item.getType().toString().contains("LEGGINGS")) {
                p.getEquipment().setLeggings(item);
            } else if (item.getType().toString().contains("BOOTS")) {
                p.getEquipment().setBoots(item);
            }
        }
        for (ItemStack item : soldierWeapon) {
            p.getInventory().addItem(item);
        }
        for (Potion potion : soldierPotion) {
            p.getInventory().addItem(potion.toItemStack(2));
        }
        for (PotionEffect effect : soldierEffect) {
            p.addPotionEffect(effect);
        }
    }

    private void transferWarlord(WowPlayer killed, WowPlayer killer) {
        LocalTime transferTime = LocalTime.now();
        killed.addTotalWarlordTime(timeBetween(killed.getStartWarlordtime(), transferTime));
        warlord = killer;
        warlord.resetKit();
        getKitWarlord(killer.getPlayer());
        killer.setStartWarlordtime(transferTime);
        playSound("stolen");
    }

    private void playSound(String sound) {
        switch (sound) {
            case "stolen":
                for (WowPlayer wp : this.queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);
                }
                break;
            case "killed":
                for (WowPlayer wp : this.queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 10, 1);
                }
                break;
            case "countdown":
                for (WowPlayer wp : this.queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 10, 1);
                }
                break;
        }
    }

    void check(Player p) {
        pm(p, this.toString());
    }

    void checkAdvanced(Player p) {
        pm(p, "Queue (" + this.queue.size() + "): " + this.queue.toString());
        pm(p, "Game state: " + active);
        pm(p, "Warlord: " + warlord);
        pm(p, "Warlord buffs: " + warlordBuffs.toString());
        pm(p, "LobbyLocation: " + lobbyLocation.toString());
        pm(p, "Game duration(s): " + playTime);
        pm(p, "Queue size min:1 - max:" + maxAmountPlayers);
    }

    void join(Player p) {
        WowPlayer wp = PlayerManager.getWowPlayer(p);
        if (!active) {
            if (wp.getActiveGamemode() == null) {
                if (!this.queue.contains(wp) && wp.getQueuedGamemode() == null) {
                    if (this.queue.size() < maxAmountPlayers) {
                        wp.setQueuedGamemode(this);
                        this.queue.add(wp);
                        broadcast(wp.getPlayer().getDisplayName() + " joined the queue " + gameID + ". (" + this.queue.size() + "/" + maxAmountPlayers + ")!");
                    } else {
                        warning(wp.getPlayer(), "The queue is full");
                    }
                } else {
                    warning(wp.getPlayer(), "You are already in an queue");
                }
            } else {
                warning(wp.getPlayer(), "You are already in an match");
            }
        } else {
            warning(wp.getPlayer(), "The game is already active");
        }
    }

    void leave(Player p) {
        WowPlayer wp = PlayerManager.getWowPlayer(p);
        if (wp.getActiveGamemode() != null && this.queue.contains(wp)) {
            if (active) {
                leaveMatch(wp);
            } else {
                leaveQueue(wp);
            }
        } else {
            warning(wp.getPlayer(), "You are not in a queue");
        }
    }

    private void leaveQueue(WowPlayer wp) {
        wp.resetStats();
        this.queue.remove(wp);
        broadcast(wp.getPlayer().getDisplayName() + " left the queue " + this.gameID +
                ". (" + this.queue.size() + "/" + maxAmountPlayers + ")!");
    }

    private void leaveMatch(WowPlayer wp) {
        wp.resetStats();
        this.queue.remove(wp);
        tp(wp.getPlayer(), lobbyLocation);
        broadcast(wp.getPlayer().getDisplayName() + " left the match!");
        if (this.active) {
            if (!this.queue.isEmpty()) {
                if (this.warlord.equals(wp)) {
                    transferWarlord(wp, randomPlayer(this.queue));
                }
            } else {
                for (PotionEffect potionEffect : this.warlord.getPlayer().getActivePotionEffects()) {
                    this.warlord.getPlayer().removePotionEffect(potionEffect.getType());
                }
                end();
            }
        }
    }

    void preStart(Player p) {
        WowPlayer wp = PlayerManager.getWowPlayer(p);
        if (!this.active) {
            if (this.queue.size() > 0) {
                initRunnable();
                this.active = true;
                broadcast("Starting War of Warlords!");
                for (int i = 0; i < this.queue.size(); i++) {
                    WowPlayer wpInqueue = this.queue.get(i);
                    wpInqueue.setActiveGamemode(this);
                    wpInqueue.setAlive(true);
                    tp(this.queue.get(i).getPlayer(), this.map.startLocation.get(i));
                }
                this.timerStart.runTaskTimer(this.plugin, 0, 20);
            } else {
                warning(wp.getPlayer(), "Nobody is inside the queue");
            }
        } else {
            warning(wp.getPlayer(), "The match is already active");
        }
    }

    private void start() {
        if (!this.queue.isEmpty()) {
            this.warlord = randomPlayer(this.queue);
            this.warlord.setStartWarlordtime(LocalTime.now());
            for (WowPlayer wp : this.queue) {
                if (wp.equals(this.warlord)) {
                    getKitWarlord(wp.getPlayer());
                } else {
                    getKitSoldier(wp.getPlayer());
                }
            }
            broadcast(this.warlord.getPlayer().getDisplayName() + " is the warlord!");
            this.timerActive.runTaskTimer(this.plugin, 0, 20);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.setDeathMessage("");
        if (this.active && e.getEntity().getKiller() != null) {
            WowPlayer killed = PlayerManager.getWowPlayer(e.getEntity());
            WowPlayer killer = PlayerManager.getWowPlayer(e.getEntity().getKiller());
            killer.addKilledPlayers(killed.getPlayer());
            killed.setAlive(false);

            if (killer.equals(this.warlord)) {
                broadcast("The Warlord killed " + killed.getPlayer().getDisplayName());
                List<WowPlayer> survivors = this.queue.stream().filter(WowPlayer::isAlive).collect(Collectors.toList());
                if (survivors.size() == 1) {
                    broadcast("Nobody Left");
                    endGame();
                }
            } else if (killed.equals(this.warlord)) {
                if (this.queue.size() == 2) {
                    broadcast("Nobody Left");
                    endGame();
                } else {
                    broadcast("The Warlord got killed and " + killer.getPlayer().getDisplayName() + " became the new Warlord");
                    transferWarlord(killed, killer);
                    broadcast("All fallen players get resurrection");
                    List<WowPlayer> victims = this.queue.stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
                    for (int i = 0; i < victims.size(); i++) {
                        tp(victims.get(i).getPlayer(), this.map.startLocation.get(i));
                        getKitSoldier(victims.get(i).getPlayer());
                    }
                }
            }
        }
    }

    private void endGame() {
        this.timerActive.cancel();
        this.warlord.addTotalWarlordTime(timeBetween(this.warlord.getStartWarlordtime(), LocalTime.now()));

        List<Leaderboard> leaderboard = new ArrayList<>();
        for (WowPlayer wp : this.queue) {
            leaderboard.add(new Leaderboard(wp, wp.getKilledPlayers().size(), wp.getTotalWarlordTime()));
        }

        List<Leaderboard> list = calculateLeaderboard(leaderboard);
        broadcast(list.get(0).getWowPlayer().getPlayer().getDisplayName() + " won the game!");
        for (int i = 0; i < list.size(); i++) {
            broadcast((i + 1) + ". " + list.get(i).toString());
        }
        this.timerEnd.runTaskTimer(this.plugin, 0, 20);
    }

    private void end() {
        this.active = false;
        List<WowPlayer> tempQueue = new ArrayList<>(this.queue);
        for (WowPlayer wp : tempQueue) {
            this.leaveMatch(wp);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gamemode) {
            return (this.gameID == ((Gamemode) obj).gameID);
        }
        return false;
    }

    @Override
    public String toString() {
        String s = "Game " + this.gameID + " (" + this.map.name + "): ";
        if (this.active) s += ChatColor.RED + "Active";
        if (!this.active) s += ChatColor.GREEN + "Open";
        s += ChatColor.WHITE + " (" + this.queue.size() + "/" + maxAmountPlayers + ") ";
        List<String> namelist = new ArrayList<>();
        for (WowPlayer wp : this.queue) {
            namelist.add(wp.getPlayer().getDisplayName());
        }
        s += namelist.toString();
        return s;
    }
}

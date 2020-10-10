package omwro.warofwarlord;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static omwro.warofwarlord.Leaderboard.*;
import static omwro.warofwarlord.PlayerManager.*;
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

    private static List<WowPlayer> queue;
    private static WowPlayer warlord;
    private static boolean active;
    private int timeLeft;

    Gamemode(JavaPlugin plugin, int gameID) {
        this.plugin = plugin;
        this.gameID = gameID;
        queue = new ArrayList<>();
        active = false;
        timeLeft = 0;
    }

    int getGameID() {
        return gameID;
    }

    private void giveWarlordBuffs() {
        for (PotionEffect potionEffect : warlordBuffs) {
            server.getLogger().info(potionEffect.toString());
            warlord.getPlayer().addPotionEffect(potionEffect);
        }
    }

    private void transferWarlord(WowPlayer killed, WowPlayer killer){
        LocalTime transferTime = LocalTime.now();
        killed.addTotalWarlordTime(timeBetween(killed.getStartWarlordtime(), transferTime));
        warlord = killer;
        giveWarlordBuffs();
        killer.setStartWarlordtime(transferTime);
        playSound("stolen");
    }

    private void playSound(String sound){
        switch (sound) {
            case "stolen":
                for (WowPlayer wp : queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 10, 1);
                }
                break;
            case "killed":
                for (WowPlayer wp : queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 10, 1);
                }
                break;
            case "countdown":
                for (WowPlayer wp : queue) {
                    wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 10, 1);
                }
                break;
        }
    }

    void check(Player p) {
        pm(p, this.toString());
    }

    void checkAdvanced(Player p) {
        pm(p, "Queue (" + queue.size() + "): " + queue.toString());
        //broadcast("Survivors ("+survivors.size()+"): "+survivors.toString());
        //broadcast("Fallen ("+fallen.size()+"): "+fallen.toString());
        //broadcast("Warlord time ("+warlordTime.size()+"): "+warlordTime.toString());
        pm(p, "Game state: " + active);
        pm(p, "Warlord: " + warlord);
        pm(p, "Warlord buffs: " + warlordBuffs.toString());
        pm(p, "LobbyLocation: " + lobbyLocation.toString());
        pm(p, "StartLocation: " + startLocationList.toString());
        pm(p, "Game duration(s): " + playTime);
        pm(p, "Queue size min:1 - max:" + maxAmountPlayers);
    }

    void join(Player p) {
        WowPlayer wp = PlayerManager.getWowPlayer(p);
        if (!active) {
            if (wp.getActiveGamemode() == null) {
                if (!queue.contains(wp)) {
                    if (queue.size() < maxAmountPlayers) {
                        wp.setActiveGamemode(this);
                        wp.setAlive(true);
                        queue.add(wp);
                        broadcast(wp.getPlayer().getDisplayName() + " joined the queue (" + queue.size() + "/" + maxAmountPlayers + ")!");
                    } else {
                        warning(wp.getPlayer(), "The queue is full");
                    }
                } else {
                    warning(wp.getPlayer(), "You are already in the queue");
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
        if (wp.getActiveGamemode() != null && queue.contains(wp)){
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
        queue.remove(wp);
        broadcast(wp.getPlayer().getDisplayName() + " left the queue (" + queue.size() + "/" + maxAmountPlayers + ")!");
    }

    private void leaveMatch(WowPlayer wp) {
        wp.resetStats();
        tp(wp.getPlayer(), lobbyLocation);
        broadcast(wp.getPlayer().getDisplayName() + " left the match!");
    }

    void preStart(Player p) {
        WowPlayer wp = PlayerManager.getWowPlayer(p);
        if (!active) {
            if (queue.size() > 0) {
                active = true;
                broadcast("Starting War of Warlords!");
                for (int i = 0; i < queue.size(); i++) {
                    tp(queue.get(i).getPlayer(), startLocationList.get(i));
                }
                new BukkitRunnable() {
                    int x = 0;
                    public void run() {
                        if (x < 10){
                            playSound("countdown");
                            x++;
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 20);
                new BukkitRunnable() {
                    public void run() {
                        start();
                    }
                }.runTaskLater(plugin, 10*20);
            } else {
                warning(wp.getPlayer(), "Nobody is inside the queue");
            }
        } else {
            warning(wp.getPlayer(), "The match is already active");
        }
    }

    private void start() {
        warlord = randomPlayer(queue);
        warlord.setStartWarlordtime(LocalTime.now());
        giveWarlordBuffs();
        broadcast(warlord.getPlayer().getDisplayName() + " is the warlord!");
        timeLeft = playTime;

        // Timer for the last 10 seconds
        new BukkitRunnable() {
            int x = 0;
            public void run() {
                if (x < 10){
                    playSound("countdown");
                    x++;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, (playTime-10)*20, 20);

        // Delay to end the game
        new BukkitRunnable() {
            public void run() {
                if (active){
                    endGame();
                }
            }
        }.runTaskLater(plugin, playTime*20);

        // Active timer
        new BukkitRunnable() {
            int x = playTime+1;
            public void run() {
                if (x > 0){
                    x--;
                    timeLeft = x;
                    warning(warlord.getPlayer(), timeLeft+" seconds left!");
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (active && e.getEntity().getKiller() != null) {
            WowPlayer killed = PlayerManager.getWowPlayer(e.getEntity());
            WowPlayer killer = PlayerManager.getWowPlayer(e.getEntity().getKiller()) ;
            killer.addKilledPlayers(killed.getPlayer());
            killed.setAlive(false);

            if (killer.equals(warlord)) {
                broadcast("The Warlord killed " + killed.getPlayer().getDisplayName());
                List<WowPlayer> survivors = queue.stream().filter(WowPlayer::isAlive).collect(Collectors.toList());
                if (survivors.size() == 1) {
                    endGame();
                }
            } else if (killed.equals(warlord)) {
                broadcast("The Warlord got killed and " + killer.getPlayer().getDisplayName() + " became the new Warlord");
                transferWarlord(killed, killer);
                broadcast("All fallen players get resurrection");
                List<WowPlayer> victims = queue.stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
                for (int i = 0; i < victims.size(); i++) {
                    tp(victims.get(i).getPlayer(), startLocationList.get(i));
                }
            }
        }
    }

    private void endGame() {
        broadcast("Time is over!");
        active = false;
        warlord.addTotalWarlordTime(timeBetween(warlord.getStartWarlordtime(), LocalTime.now()));

        List<Leaderboard> leaderboard = new ArrayList<>();
        for (WowPlayer wp : queue){
            leaderboard.add(new Leaderboard(wp, wp.getKilledPlayers().size(), wp.getTotalWarlordTime()));
        }
        List<Leaderboard> list = calculateLeaderboard(leaderboard);
        broadcast(list.get(0).getWowPlayer().getPlayer().getDisplayName()+" won the game!");
        for (int i = 0; i < list.size(); i++) {
            broadcast((i+1)+". "+list.get(i).toString());
        }
        new BukkitRunnable() {
            public void run() {
                end();
            }
        }.runTaskLater(plugin, 10*20);
    }

    private void end() {
        for (PotionEffect potionEffect : warlord.getPlayer().getActivePotionEffects()) {
            warlord.getPlayer().removePotionEffect(potionEffect.getType());
        }
        for (WowPlayer wp : queue) {
            this.leaveMatch(wp);
        }
        queue.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Gamemode){
            return (this.gameID == ((Gamemode) obj).gameID);
        }
        return false;
    }

    @Override
    public String toString() {
        String s = "Game " + this.gameID + ": ";
        if (active) s += ChatColor.RED + "Active";
        if (!active) s += ChatColor.GREEN + "Open";
        s += ChatColor.WHITE + " - ";
        s += "(" + queue.size() + "/" + maxAmountPlayers + ")";
        List<String> namelist = new ArrayList<>();
        for (WowPlayer wp : queue){
            namelist.add(wp.getPlayer().getDisplayName());
        }
        s += namelist.toString();
        return s;
    }
}

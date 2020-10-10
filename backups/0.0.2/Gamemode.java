package omwro.warofwarlord;

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

import static omwro.warofwarlord.Leaderboard.calculateLeaderboard;
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
    private static LocalTime startTime;

    Gamemode(JavaPlugin plugin, int gameID) {
        this.plugin = plugin;
        this.gameID = gameID;
        queue = new ArrayList<>();
        active = false;
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
        killed.addTotalWarlordTime(timeBetween(killed.getStartWarlordtime(), LocalTime.now()));

        warlord = killer;
        giveWarlordBuffs();
        killer.setStartWarlordtime(LocalTime.now());
        playSound("stolen");
    }

    private void playSound(String sound){
        if (sound.equals("stolen")){
            for (WowPlayer wp : queue){
                wp.getPlayer().playSound(wp.getPlayer().getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 10, 1);
            }
        }
    }

    void check(WowPlayer wp) {
        pm(wp.getPlayer(), "Queue (" + queue.size() + "): " + queue.toString());
        //broadcast("Survivors ("+survivors.size()+"): "+survivors.toString());
        //broadcast("Fallen ("+fallen.size()+"): "+fallen.toString());
        //broadcast("Warlord time ("+warlordTime.size()+"): "+warlordTime.toString());
        pm(wp.getPlayer(), "Game state: " + active);
        pm(wp.getPlayer(), "Warlord: " + warlord);
        pm(wp.getPlayer(), "Warlord buffs: " + warlordBuffs.toString());
        pm(wp.getPlayer(), "LobbyLocation: " + lobbyLocation.toString());
        pm(wp.getPlayer(), "StartLocation: " + startLocation.toString());
        pm(wp.getPlayer(), "Game duration(s): " + playTime);
        pm(wp.getPlayer(), "Queue size min:1 - max:" + maxAmountPlayers);
    }

    void join(WowPlayer wp) {
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

    void leave(WowPlayer wp) {
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

    void startGame(WowPlayer wp) {
        if (!active) {
            if (queue.size() > 0) {
                active = true;
                broadcast("Starting War of Warlords!");
                for (WowPlayer wowPlayer : queue){
                    tp(wowPlayer.getPlayer(), startLocation);
                }
                start();
            } else {
                warning(wp.getPlayer(), "Nobody is inside the queue");
            }
        } else {
            warning(wp.getPlayer(), "The match is already active");
        }
    }

    private void start() {
        startTime = LocalTime.now();
        warlord = randomPlayer(queue);
        warlord.setStartWarlordtime(startTime);
        giveWarlordBuffs();
        broadcast(warlord.getPlayer().getDisplayName() + " is the warlord!");

        new BukkitRunnable() {
            public void run() {
                if (active) {
                    broadcast("Time is over!");
                    endGame();
                }
            }
        }.runTaskLater(plugin, Plugin.playTime * 20);
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
                for (WowPlayer wp : victims) {
                    tp(wp.getPlayer(), startLocation);
                }
            }
        }
    }

    private void endGame() {
        warlord.setTotalWarlordTime(timeBetween(warlord.getStartWarlordtime(), LocalTime.now()));

        /*List<Leaderboard> leaderboard = new ArrayList<>();
        for (WowPlayer wp : queue){
            leaderboard.add(new Leaderboard(wp, wp.getKilledPlayers().size(), wp.getTotalWarlordTime()));
        }
        List<Leaderboard> list = calculateLeaderboard(leaderboard);
        for (Leaderboard lb : list){
            broadcast(lb.toString());
        }*/

        Map<WowPlayer, Integer> killsList = new HashMap<>();
        for (WowPlayer p : queue){
            killsList.put(p, p.getKilledPlayers().size());
        }

        int maxKills = Collections.max(killsList.values());
        List<WowPlayer> topKillsList = killsList.entrySet().stream()
                .filter(entry -> entry.getValue() == maxKills)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (topKillsList.size() == 1) {
            /*broadcast(topKillsList.get(0).getPlayer().getDisplayName() +
                    " won the game!");*/
            broadcast(topKillsList.get(0).getPlayer().getDisplayName() +
                    " won the game with " +
                    topKillsList.get(0).getKilledPlayers().size() +
                    " kills!");
            end();
        } else if (topKillsList.size() > 1) {
            Map<WowPlayer, Long> timeList = new HashMap<>();
            for (WowPlayer p : topKillsList){
                timeList.put(p, p.getTotalWarlordTime());
            }

            long maxTime = Collections.max(timeList.values());
            List<WowPlayer> topTimeList = timeList.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxTime)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (topTimeList.size() == 1) {
                /*broadcast(topTimeList.get(0).getPlayer().getDisplayName() +
                        " won the game!");*/
                broadcast(topTimeList.get(0).getPlayer().getDisplayName() +
                        " won the game with " +
                        topTimeList.get(0).getKilledPlayers().size() +
                        " kills and a time of " +
                        (topTimeList.get(0).getTotalWarlordTime() / 1000) +
                        " seconds");
                end();
            } else if (topTimeList.size() > 1) {
                StringBuilder msg = new StringBuilder("It is a draw between " + topTimeList.get(0));
                for (int i = 1; i < topTimeList.size(); i++) {
                    msg.append(" & ").append(topTimeList.get(i));
                }
                msg.append(" with ")
                        .append(topKillsList.get(0))
                        .append(" kills and a Warlord playtime of ")
                        .append(topTimeList.get(0))
                        .append(" seconds");
                broadcast(msg.toString());
                end();
            }
        }
    }

    private void end() {
        for (PotionEffect potionEffect : warlord.getPlayer().getActivePotionEffects()) {
            warlord.getPlayer().removePotionEffect(potionEffect.getType());
        }
        for (WowPlayer wp : queue) {
            this.leaveMatch(wp);
        }
        queue.clear();
        active = false;
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
        return "Game{" +
                "ID: " + this.gameID + ", " +
                "Active: " + active + ", " +
                "Warlord: " + warlord + ", " +
                "Queue: " + queue + ", " +
                "}";
    }
}

package omwro.warofwarlord;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static omwro.warofwarlord.Commands.broadcast;
import static omwro.warofwarlord.Commands.pm;
import static omwro.warofwarlord.Plugin.*;

/**
 * @author Omer Erdem
 * Developer Note: most variables are static because the eventhandler function does not accept public/protected/private
 * scopes. Keep that in mind.
 */

public class Gamemode implements Listener {
    private JavaPlugin plugin;

    static HashMap<Player, Integer> queue = new HashMap<>();
    static ArrayList<Player> survivors = new ArrayList<>();
    static ArrayList<Player> fallen = new ArrayList<>();

    static Player warlord;
    static HashMap<Player, Integer> warlordTime = new HashMap<>();
    static LocalTime currentTime;

    static boolean active;

    Gamemode(JavaPlugin plugin) {
        this.plugin = plugin;
        active = false;
    }

    private void tp(Player p, Location l){
        p.teleport(l);
    }

    private Player randomPlayer(){
        int randomInt = (int) (Math.random() * MAX_AMOUNT_PLAYERS);
        return (Player) queue.keySet().toArray()[randomInt];
    }

    private int timeBetween(){
        int between = (int) ChronoUnit.SECONDS.between(currentTime, LocalTime.now());
        currentTime = LocalTime.now();
        return between;
    }

    private void giveWarlordBuffs(){
        for (PotionEffect potionEffect : warlordBuffs){
            warlord.addPotionEffect(potionEffect);
        }
    }

    void check(){
        broadcast("Queue ("+queue.size()+"): "+queue.toString());
        broadcast("Game state: "+active);
    }

    void join(Player p){
        if (!active){
            if (!queue.containsKey(p)){
                if (queue.size() < MAX_AMOUNT_PLAYERS){
                    queue.put(p, 0);
                    broadcast(p.getDisplayName()+" joined War of Warlords Queue ("+queue.size()+"/"+MAX_AMOUNT_PLAYERS+")!");
                } else {
                    pm(p, "The queue is full");
                }
            } else {
                pm(p, "You are already in the queue");
            }
        } else {
            pm(p, "The game is already active");
        }
    }

    void leave(Player p){
        if (queue.containsKey(p)){
            queue.remove(p);
            broadcast(p.getDisplayName()+" left War of Warlords Queue ("+queue.size()+"/"+MAX_AMOUNT_PLAYERS+")!");
        } else {
            pm(p, "You are not in the queue");
        }
    }
    void startGame(){
        if (!active){
            if (beforeStart()){
                start();
            }
        }
    }

    private boolean beforeStart(){
        if (queue.size() == MAX_AMOUNT_PLAYERS){
            active = true;
            broadcast("Starting War of Warlords!");
            survivors.clear();
            fallen.clear();
            queue.forEach((p, s) -> {
                tp(p, startLocation);
                survivors.add(p);
                warlordTime.put(p,0);
            });
            return true;
        }
        broadcast("Failed to start the game. Not enough players");
        return false;
    }

    private void start(){
        currentTime = LocalTime.now();
        warlord = randomPlayer();
        giveWarlordBuffs();
        broadcast(warlord.getDisplayName()+" is the warlord!");

        new BukkitRunnable() {
            public void run() {
                if (active){
                    broadcast("Time is over!");
                    endGame();
                }
            }
        }.runTaskLater(plugin, playTime*20);
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent e) {
        if (active && e.getEntity().getKiller() != null){
            Player killed = e.getEntity();
            Player killer = killed.getKiller();
            queue.put(killer, queue.get(killer)+1);
            survivors.remove(killed);
            fallen.add(killed);

            if (killer.equals(warlord)){
                broadcast("The Warlord killed "+killed.getDisplayName());
                if (survivors.size() == 1){
                    endGame();
                }
            } else if (killed.equals(warlord)){
                broadcast("The Warlord got killed and "+killer.getDisplayName()+" became the new Warlord");
                warlordTime.put(warlord, warlordTime.get(warlord) + timeBetween());
                warlord = killer;
                giveWarlordBuffs();
                broadcast("Fallen players get resurrection");
                for (Player player : fallen){
                    tp(player, startLocation);
                }
            }
        }
    }

    private void endGame(){
        warlordTime.put(warlord, warlordTime.get(warlord) + timeBetween());
        broadcast("Warlord times: "+warlordTime.toString());

        int maxKills = Collections.max(queue.values());
        List<Player> results = queue.entrySet().stream()
                .filter(entry -> entry.getValue() == maxKills)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (results.size() == 1){
            broadcast("The game has ended. "+
                    results.get(0).getDisplayName()+
                    " won the game with "+
                    queue.get(results.get(0))+
                    " kills!");
            end();
        } else if (results.size() > 1){
            int maxTime = Collections.max(warlordTime.values());
            List<Player> results2 = warlordTime.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxTime)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (results2.size() == 1){
                broadcast("The game has ended. "+
                        results2.get(0).getDisplayName()+
                        " won the game with "+
                        queue.get(results2.get(0))+
                        " kills and a time of "+
                        warlordTime.get(results2.get(0))+
                        " seconds");
                end();
            } else if (results2.size() > 1){
                StringBuilder msg = new StringBuilder("It is a draw between " + results2.get(0));
                for (int i = 1; i < results2.size(); i++){
                    msg.append(" & ").append(results2.get(i));
                }
                msg.append(" with ")
                        .append(results.get(0))
                        .append(" kills and a Warlord playtime of ")
                        .append(results2.get(0))
                        .append(" seconds");
                broadcast(msg.toString());
                end();
            }
        }
    }

    private void end(){
        for (PotionEffect potionEffect : warlord.getActivePotionEffects()){
            warlord.removePotionEffect(potionEffect.getType());
        }
        queue.forEach((p, s) -> {
            tp(p, lobbyLocation);
        });
        queue.clear();
        active = false;
    }
}

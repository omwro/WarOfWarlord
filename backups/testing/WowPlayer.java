package omwro.warofwarlord;

import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


class WowPlayer {
    private Player player;
    private Gamemode activeGamemode;
    private boolean alive;
    private List<Player> killedPlayers;
    private LocalTime startWarlordtime;
    private long totalWarlordTime;

    WowPlayer(Player player) {
        this.player = player;
        this.activeGamemode = null;
        this.alive = false;
        this.killedPlayers = new ArrayList<>();
        this.startWarlordtime = null;
        this.totalWarlordTime = 0;
    }

    Player getPlayer() {
        return player;
    }

    Gamemode getActiveGamemode() {
        return activeGamemode;
    }

    void setActiveGamemode(Gamemode activeGamemode) {
        this.activeGamemode = activeGamemode;
        PlayerManager.updateWowPlayer(this);
    }

    boolean isAlive() {
        return alive;
    }

    void setAlive(boolean alive) {
        this.alive = alive;
        PlayerManager.updateWowPlayer(this);
    }

    List<Player> getKilledPlayers() {
        return killedPlayers;
    }

    void addKilledPlayers(Player killedPlayer) {
        this.killedPlayers.add(killedPlayer);
        PlayerManager.updateWowPlayer(this);
    }

    LocalTime getStartWarlordtime() {
        return startWarlordtime;
    }

    void setStartWarlordtime(LocalTime startWarlordtime) {
        this.startWarlordtime = startWarlordtime;
        PlayerManager.updateWowPlayer(this);
    }

    long getTotalWarlordTime() {
        return totalWarlordTime;
    }

    void addTotalWarlordTime(long totalWarlordTime) {
        this.totalWarlordTime += totalWarlordTime;
        PlayerManager.updateWowPlayer(this);
    }

    void resetStats(){
        this.activeGamemode = null;
        this.alive = false;
        this.killedPlayers.clear();
        this.totalWarlordTime = 0;
        PlayerManager.updateWowPlayer(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WowPlayer){
            return (this.getPlayer().getEntityId() == ((WowPlayer) obj).getPlayer().getEntityId());
        }
        return false;
    }

    @Override
    public String toString() {
        String gm = null;
        if (this.getActiveGamemode() != null) {
            gm = String.valueOf(this.getActiveGamemode().getGameID());
        }
        return "WoWPlayer{" +
                "ID: " + this.getPlayer().getEntityId() + ", " +
                "Name: " + this.getPlayer().getDisplayName() + ", " +
                "Game: " + gm + ", " +
                "}";

    }
}

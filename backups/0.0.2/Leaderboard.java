package omwro.warofwarlord;

import java.util.List;

public class Leaderboard {
    WowPlayer wowPlayer;
    int kills;
    long time;

    public Leaderboard(WowPlayer wowPlayer, int kills, long time) {
        this.wowPlayer = wowPlayer;
        this.kills = kills;
        this.time = time;
    }

    public WowPlayer getWowPlayer() {
        return wowPlayer;
    }

    public void setWowPlayer(WowPlayer wowPlayer) {
        this.wowPlayer = wowPlayer;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    static List<Leaderboard> calculateLeaderboard(List<Leaderboard> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i+1; j < list.size(); j++) {
                if (list.get(i).getKills() < list.get(j).getKills()){
                    Leaderboard temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j , temp);
                }
            }
        }
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i+1; j < list.size(); j++) {
                if (list.get(i).getKills() == list.get(j).getKills() &&
                    list.get(i).getTime() < list.get(j).getTime()){
                    Leaderboard temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j , temp);
                }
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return "{"+wowPlayer.getPlayer().getDisplayName()+", "+kills+", "+time+"}";
    }
}

package omwro.warofwarlord;

import java.util.List;

public class Leaderboard {
    private WowPlayer wowPlayer;
    private int kills;
    private long time;

    Leaderboard(WowPlayer wowPlayer, int kills, long time) {
        this.wowPlayer = wowPlayer;
        this.kills = kills;
        this.time = time;
    }

    WowPlayer getWowPlayer() {
        return wowPlayer;
    }

    int getKills() {
        return kills;
    }

    long getTime() {
        return time;
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
        return wowPlayer.getPlayer().getDisplayName()+" - "+kills+" kills - "+(int) Math.ceil(time / 1000.0)+" seconds";
    }
}

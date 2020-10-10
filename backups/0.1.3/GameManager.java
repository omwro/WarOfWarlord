package omwro.warofwarlord;

import java.util.HashMap;

class GameManager {
    private static HashMap<Integer, Gamemode> gameList = new HashMap<>();

    static HashMap<Integer, Gamemode> getGamemodes() {
        return gameList;
    }

    static Gamemode getGamemodeByID(int id) {
        return gameList.get(id);
    }

    static Gamemode addGamemode(Gamemode gamemode) {
        return gameList.put(gamemode.getGameID(), gamemode);
    }

    static Gamemode updateGamemode(Gamemode gamemode) {
        return gameList.put(gamemode.getGameID(), gamemode);
    }
}

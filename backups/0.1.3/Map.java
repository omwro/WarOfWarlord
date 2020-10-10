package omwro.warofwarlord;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

class Map {
    String name;
    World world;
    List<Location> startLocation;

    String worldname;
    List<XYZ> XYZList;

    Map(String name, String worldname, List<XYZ> XYZList) {
        this.name = name;
        this.worldname = worldname;
        this.XYZList = XYZList;
    }

    Map(String name, World world, List<Location> startLocation) {
        this.name = name;
        this.world = world;
        this.startLocation = startLocation;
    }

    @Override
    public String toString() {
        String s = "{";
        s += "Name="+this.name+", ";
        s += "World="+this.world.getName()+", ";
        s += "StartLocation={";
        for (Location loc : startLocation) {
            s += "[x="+loc.getX()+", y="+loc.getY()+", z="+loc.getZ()+"]";
        }
        s += "}";
        return s;
    }
}

class XYZ {
    int x, y, z;

    XYZ(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    static Location getLocation(World world, int x, int y, int z) {
        if (y == 0) {
            y = world.getHighestBlockYAt(x, z);
        }
        return new Location(world, x, y, z);
    }
}

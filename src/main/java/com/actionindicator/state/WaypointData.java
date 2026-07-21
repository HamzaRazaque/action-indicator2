package com.actionindicator.state;

import java.util.ArrayList;
import java.util.List;

public final class WaypointData {

    public static final List<Waypoint> waypoints = new ArrayList<>();

    public static class Waypoint {
        public String name;
        public double x, y, z;
        public int color;

        public Waypoint(String name, double x, double y, double z, int color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }

        public String toShortString() {
            return name + " [" + (int)x + ", " + (int)y + ", " + (int)z + "]";
        }
    }

    private WaypointData() {}
}

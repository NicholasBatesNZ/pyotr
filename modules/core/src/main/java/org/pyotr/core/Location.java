package org.pyotr.core;

import org.terasology.gestalt.entitysystem.component.Component;

public final class Location implements Component<Location> {

    private int x = 0;
    private int y = 0;

    public int getX() {
        return x;
    }

    public void setX(int value) {
        x = value;
    }

    public int getY() {
        return y;
    }

    public void setY(int value) {
        y = value;
    }

    @Override
    public void copy(Location other) {
        this.x = other.x;
        this.y = other.y;
    }
}
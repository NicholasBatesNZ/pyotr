package org.pyotr.engine.entitysystem;

import org.terasology.gestalt.entitysystem.event.Event;

public class ScareEvent implements Event {

    private int amount;

    public ScareEvent(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
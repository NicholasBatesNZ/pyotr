package org.pyotr.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.entitysystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.EventResult;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

public class CourageSystem {

    private static final Logger logger = LoggerFactory.getLogger(CourageSystem.class);

    // @Before(SomeOtherSystem.class) to make sure this one goes first. also @After
    @ReceiveEvent(components = Location.class)
    public EventResult onScare(ScareEvent event, EntityRef entity) {
        entity.getComponent(Location.class).ifPresent(location -> {
            location.setX(location.getX() - event.getAmount());
            entity.setComponent(location);
            logger.info("My courage was just challenged and I jumped backwards " + event.getAmount() + " units to "
                    + location.getX());
        });
        return EventResult.CONTINUE;
    }
}
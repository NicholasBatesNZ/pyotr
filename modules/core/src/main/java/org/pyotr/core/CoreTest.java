package org.pyotr.core;

import org.pyotr.engine.EntitySystemManager;
import org.pyotr.engine.IDoSomething;
import org.pyotr.engine.context.In;
import org.slf4j.Logger;

public class CoreTest implements IDoSomething {

    @In
    EntitySystemManager esManager;

    @Override
    public void doSomething(Logger logger) {
        logger.info("Sup dude, I'm from a module seperated from the main engine!");
        esManager.registerSystem(new CourageSystem());
        esManager.sendEvent(new ScareEvent(10), new Location());
    }
}
package org.pyotr.core;

import org.pyotr.engine.IDoSomething;

import org.slf4j.Logger;

public class CoreTest implements IDoSomething {
    
    @Override
    public void doSomething(Logger logger) {
        logger.info("Sup dude, I'm from a module seperated from the main engine!");
    }
}
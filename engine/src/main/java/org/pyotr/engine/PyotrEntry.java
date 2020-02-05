package org.pyotr.engine;

import org.pyotr.engine.ModuleManager;
import org.pyotr.engine.IDoSomething;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PyotrEntry {

    private static final Logger logger = LoggerFactory.getLogger(PyotrEntry.class);

    private static ModuleManager moduleManager;

    public static void main(String[] args) {

        logger.info("In the beginning, was the PyotrEntry");

        moduleManager = new ModuleManager();

        // example of calling module classes
        for (Class<?> somethingClass : moduleManager.getEnvironment().getSubtypesOf(IDoSomething.class)) {
            try {
                IDoSomething somethingSystem = (IDoSomething) somethingClass.newInstance();
                somethingSystem.doSomething(logger);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        EntitySystemManager esManager = new EntitySystemManager(moduleManager);

        moduleManager.getEnvironment().close();
    }
}
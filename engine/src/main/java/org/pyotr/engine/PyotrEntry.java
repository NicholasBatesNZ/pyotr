package org.pyotr.engine;

import org.pyotr.engine.ModuleManager;
import org.pyotr.engine.IDoSomething;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PyotrEntry {

    private static final Logger logger = LoggerFactory.getLogger(PyotrEntry.class);

    private static ModuleManager moduleManager;
    private static boolean initFinished = false;

    public static void main(String[] args) {

        logger.info("In the beginning, was the PyotrEntry");

        // start looking for modules
        new Thread(() -> {
            moduleManager = new ModuleManager();
            try {
                moduleManager.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            initFinished = true;
        }).start();

        // wait until all modules are found
        while (!initFinished) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // example of calling module classes
        for (Class<?> somethingClass : moduleManager.getEnvironment().getSubtypesOf(IDoSomething.class)) {
            try {
                IDoSomething somethingSystem = (IDoSomething) somethingClass.newInstance();
                somethingSystem.doSomething(logger);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
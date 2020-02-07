package org.pyotr.engine;

import org.pyotr.engine.ModuleManager;
import org.pyotr.engine.context.Context;
import org.pyotr.engine.context.InjectionHelper;
import org.pyotr.engine.IDoSomething;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PyotrEntry {

    private static final Logger logger = LoggerFactory.getLogger(PyotrEntry.class);

    private static ModuleManager moduleManager;

    public static void main(String[] args) {

        logger.info("In the beginning, was the PyotrEntry");

        moduleManager = new ModuleManager();

        Context context = new Context();
        context.put(EntitySystemManager.class, new EntitySystemManager(moduleManager));

        // example of calling module classes
        for (Class<?> somethingClass : moduleManager.getEnvironment().getSubtypesOf(IDoSomething.class)) {
            try {
                IDoSomething somethingSystem = (IDoSomething) somethingClass.newInstance();
                InjectionHelper.inject(somethingSystem, context);
                somethingSystem.doSomething(logger);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        moduleManager.getEnvironment().close();
    }
}
package org.pyotr.engine;

import org.pyotr.engine.ModuleManager;
import org.pyotr.engine.entitysystem.CourageSystem;
import org.pyotr.engine.entitysystem.LocationComponent;
import org.pyotr.engine.entitysystem.ScareEvent;
import org.pyotr.engine.IDoSomething;

import java.util.List;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.component.management.ComponentManager;
import org.terasology.gestalt.entitysystem.component.management.LambdaComponentTypeFactory;
import org.terasology.gestalt.entitysystem.component.store.ArrayComponentStore;
import org.terasology.gestalt.entitysystem.component.store.ComponentStore;
import org.terasology.gestalt.entitysystem.component.store.ConcurrentComponentStore;
import org.terasology.gestalt.entitysystem.entity.EntityManager;
import org.terasology.gestalt.entitysystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.entity.manager.CoreEntityManager;
import org.terasology.gestalt.entitysystem.event.EventSystem;
import org.terasology.gestalt.entitysystem.event.MethodHandleEventHandle;
import org.terasology.gestalt.entitysystem.event.impl.EventReceiverMethodSupport;
import org.terasology.gestalt.entitysystem.event.impl.EventSystemImpl;

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

        // setup entity store
        ComponentManager componentManager = new ComponentManager(new LambdaComponentTypeFactory());
        List<ComponentStore<?>> stores = Lists.newArrayList();
        for (Class<? extends Component> componentType : moduleManager.getEnvironment().getSubtypesOf(Component.class)) {
            stores.add(new ConcurrentComponentStore(new ArrayComponentStore(componentManager.getType(componentType))));
        }
        EntityManager entityManager = new CoreEntityManager(stores);

        EntityRef entity = entityManager.createEntity(new LocationComponent());

        EventSystem eventSystem = new EventSystemImpl();
        EventReceiverMethodSupport eventReceiverMethodSupport = new EventReceiverMethodSupport(MethodHandleEventHandle::new);
        eventReceiverMethodSupport.register(new CourageSystem(), eventSystem);
        
        logger.info("Attempting to scare entity");
        eventSystem.send(new ScareEvent(10), entity);
        eventSystem.processEvents();

        moduleManager.getEnvironment().close();
    }
}
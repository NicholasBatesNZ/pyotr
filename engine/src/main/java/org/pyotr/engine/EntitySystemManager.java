package org.pyotr.engine;

import java.util.List;

import com.google.common.collect.Lists;

import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.format.producer.AssetFileDataProducer;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.assets.module.ModuleDependencyResolutionStrategy;
import org.terasology.gestalt.assets.module.ModuleEnvironmentDependencyProvider;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.component.management.ComponentManager;
import org.terasology.gestalt.entitysystem.component.management.ComponentTypeIndex;
import org.terasology.gestalt.entitysystem.component.store.ArrayComponentStore;
import org.terasology.gestalt.entitysystem.component.store.ComponentStore;
import org.terasology.gestalt.entitysystem.component.store.ConcurrentComponentStore;
import org.terasology.gestalt.entitysystem.entity.EntityIterator;
import org.terasology.gestalt.entitysystem.entity.EntityManager;
import org.terasology.gestalt.entitysystem.entity.manager.CoreEntityManager;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.EventSystem;
import org.terasology.gestalt.entitysystem.event.MethodHandleEventHandle;
import org.terasology.gestalt.entitysystem.event.impl.EventReceiverMethodSupport;
import org.terasology.gestalt.entitysystem.event.impl.EventSystemImpl;
import org.terasology.gestalt.entitysystem.prefab.GeneratedFromRecipeComponent;
import org.terasology.gestalt.entitysystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.prefab.PrefabData;
import org.terasology.gestalt.entitysystem.prefab.PrefabJsonFormat;

public class EntitySystemManager {

    private static EntityManager entityManager;
    private static EventSystem eventSystem = new EventSystemImpl();
    private static EventReceiverMethodSupport eventReceiverMethodSupport = new EventReceiverMethodSupport(
            MethodHandleEventHandle::new);

    public EntitySystemManager(ModuleManager moduleManager) {

        // TODO: current Gestalt snapshup LambdaComponentTypeFactory is broken. Replace when fixed
        //ComponentManager componentManager = new ComponentManager(new LambdaComponentTypeFactory());
        ComponentManager componentManager = new ComponentManager();
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        AssetManager assetManager = new AssetManager(assetTypeManager);

        AssetType<Prefab, PrefabData> prefabAssetType = assetTypeManager.createAssetType(Prefab.class, Prefab::new,
                "prefabs");
        AssetFileDataProducer<PrefabData> prefabDataProducer = assetTypeManager
                .getAssetFileDataProducer(prefabAssetType);
        prefabDataProducer.addAssetFormat(new PrefabJsonFormat.Builder(
                new ComponentTypeIndex(moduleManager.getEnvironment(),
                        new ModuleDependencyResolutionStrategy(
                                new ModuleEnvironmentDependencyProvider(moduleManager.getEnvironment()))),
                componentManager, assetManager).create());

        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());

        List<ComponentStore<?>> stores = Lists.newArrayList();
        for (Class<? extends Component> componentType : moduleManager.getEnvironment().getSubtypesOf(Component.class)) {
            stores.add(
                    new ConcurrentComponentStore<>(new ArrayComponentStore<>(componentManager.getType(componentType))));
        }
        // TODO: check an updated Gestalt to see if this line can be removed
        stores.add(new ConcurrentComponentStore<>(
                new ArrayComponentStore<>(componentManager.getType(GeneratedFromRecipeComponent.class))));
        entityManager = new CoreEntityManager(stores);

        assetManager.getAvailableAssets(Prefab.class).forEach(urn -> {
            assetManager.getAsset(urn, Prefab.class).ifPresent(prefab -> {
                entityManager.createEntity(prefab);
            });
        });
    }

    public void registerSystem(Object system) {
        eventReceiverMethodSupport.register(system, eventSystem);
    }

    public void sendEvent(Event event, Component... components) {
        EntityIterator iterator = entityManager.iterate(components);
        while (iterator.next()) {
            eventSystem.send(event, iterator.getEntity());
        }
        eventSystem.processEvents();
    }
}
package org.pyotr.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

import org.pyotr.engine.protobuf.EntityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.entity.EntityManager;
import org.terasology.gestalt.entitysystem.entity.EntityRef;

public final class SerialisationManager {

    private static final Logger logger = LoggerFactory.getLogger(SerialisationManager.class);
    private File file;
    private EntityManager entityManager;
    private ClassLoader classLoader;

    public SerialisationManager(String path, EntityManager entityManager, ClassLoader classLoader) {
        file = new File(path);
        this.entityManager = entityManager;
        this.classLoader = classLoader;
    }

    public void serialise()
            throws IllegalArgumentException, IllegalAccessException, FileNotFoundException, IOException {

        EntityData.EntityStore.Builder storeBuilder = EntityData.EntityStore.newBuilder();
        for (EntityRef entity : entityManager.allEntities()) {
            if (entity.getId() == -1) {
                break;
            }
            EntityData.Entity.Builder entityBuilder = EntityData.Entity.newBuilder();
            entityBuilder.setId(entity.getId());

            for (Component component : entity.getAllComponents().values()) {
                EntityData.Component.Builder componentBuilder = EntityData.Component.newBuilder();
                componentBuilder.setTypeName(component.getClass().toString().replaceFirst("class ", ""));

                for (Field field : component.getClass().getDeclaredFields()) {
                    EntityData.Field.Builder fieldBuilder = EntityData.Field.newBuilder();
                    fieldBuilder.setName(field.getName());
                    fieldBuilder.setType(field.getType().toString());
                    field.setAccessible(true);

                    String value = "";
                    switch (field.getType().toString()) {
                        case "double":
                            value = String.valueOf((double) field.get(component));
                            break;
                        case "float":
                            value = String.valueOf((float) field.get(component));
                            break;
                        case "int":
                            value = String.valueOf((int) field.get(component));
                            break;
                        case "long":
                            value = String.valueOf((long) field.get(component));
                            break;
                        case "boolean":
                            value = String.valueOf((boolean) field.get(component));
                            break;
                        case "class java.lang.String":
                            value = (String) field.get(component);
                            break;
                        case "class org.terasology.gestalt.assets.ResourceUrn":
                            value = ((ResourceUrn) field.get(component)).toString();
                            break;
                        default:
                            logger.error("Trying to serialise unknown data-type: '{}'", field);
                            break;
                    }
                    fieldBuilder.setValue(ByteString.copyFrom(value.getBytes()));
                    componentBuilder.addField(fieldBuilder);
                }
                entityBuilder.addComponent(componentBuilder);
            }
            storeBuilder.addEntity(entityBuilder);
        }

        FileOutputStream output = new FileOutputStream(file);
        storeBuilder.build().writeTo(output);
        output.close();
    }

    public void deserialise() throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchFieldException {

        FileInputStream input = new FileInputStream(file);
        EntityData.EntityStore store = EntityData.EntityStore.parseFrom(input);
        input.close();

        for (EntityData.Entity entity : store.getEntityList()) {
            Collection<Component> componentsToAdd = Lists.newArrayList();

            for (EntityData.Component component : entity.getComponentList()) {
                Class<?> componentClass = Class.forName(component.getTypeName(), true, classLoader);
                Component<?> componentObject = (Component<?>) componentClass.newInstance();

                for (EntityData.Field field : component.getFieldList()) {
                    Field componentObjectField = componentClass.getDeclaredField(field.getName());
                    componentObjectField.setAccessible(true);

                    String value = new String(field.getValue().toByteArray());
                    switch (field.getType()) {
                        case "double":
                            componentObjectField.set(componentObject, Double.valueOf(value));
                            break;
                        case "float":
                            componentObjectField.set(componentObject, Float.valueOf(value));
                            break;
                        case "int":
                            componentObjectField.set(componentObject, Integer.valueOf(value));
                            break;
                        case "long":
                            componentObjectField.set(componentObject, Long.valueOf(value));
                            break;
                        case "boolean":
                            componentObjectField.set(componentObject, Boolean.valueOf(value));
                            break;
                        case "class java.lang.String":
                            componentObjectField.set(componentObject, value);
                            break;
                        case "class org.terasology.gestalt.assets.ResourceUrn":
                            componentObjectField.set(componentObject, new ResourceUrn(value));
                            break;
                        default:
                            logger.error("Trying to deserialise unknown data-type: '{}'", componentObjectField);
                            break;
                    }
                }
                componentsToAdd.add(componentObject);
            }
            entityManager.createEntity(componentsToAdd);
        }
    }
}
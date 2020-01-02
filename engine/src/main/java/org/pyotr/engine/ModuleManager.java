package org.pyotr.engine;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModulePathScanner;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.sandbox.APIScanner;
import org.terasology.gestalt.module.sandbox.ModuleSecurityManager;
import org.terasology.gestalt.module.sandbox.ModuleSecurityPolicy;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ReflectPermission;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.Set;

public class ModuleManager {

    protected static ModuleEnvironment environment;
    protected ModuleRegistry registry;
    protected Module engineModule;

    public ModuleManager() {
    }

    public void init() throws Exception {
        try {
            Reader engineModuleReader = new InputStreamReader(getClass().getResourceAsStream("/module.json"), Charsets.UTF_8);
            ModuleMetadata engineMetadata = new ModuleMetadataJsonAdapter().read(engineModuleReader);
            engineModuleReader.close();
            ModuleFactory moduleFactory = new ModuleFactory();
            File file = new File(Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).resolve("..").toUri());
            engineModule = moduleFactory.createModule(engineMetadata, file);

            registry = new TableModuleRegistry();
            File modulesRoot = Paths.get("..").resolve("modules").toFile();
            ModulePathScanner scanner = new ModulePathScanner(moduleFactory);
            scanner.scan(registry, modulesRoot);

            Set<Module> requiredModules = Sets.newHashSet();
            registry.add(engineModule);
            requiredModules.addAll(registry);

            loadEnvironment(requiredModules);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void loadEnvironment(Set<Module> modules) {
        StandardPermissionProviderFactory permissionFactory = new StandardPermissionProviderFactory();

        permissionFactory.getBasePermissionSet().addAPIPackage("java.lang");
        permissionFactory.getBasePermissionSet().addAPIClass(Logger.class);

        // The JSON serializers need to reflect classes to discover what exists
        permissionFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);
        permissionFactory.getBasePermissionSet().grantPermission("com.google.gson", RuntimePermission.class);
        permissionFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", RuntimePermission.class);

        ConfigurationBuilder config = new ConfigurationBuilder()
                .addClassLoader(ClasspathHelper.contextClassLoader())
                .addUrls(ClasspathHelper.forClassLoader())
                .addScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
        Reflections reflections = new Reflections(config);
        APIScanner scanner = new APIScanner(permissionFactory);
        scanner.scan(reflections);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());
        environment = new ModuleEnvironment(modules, permissionFactory);
    }

    public ModuleEnvironment getEnvironment() {
        return environment;
    }
}
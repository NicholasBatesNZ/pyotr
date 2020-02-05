package org.pyotr.engine;

import com.google.common.collect.Sets;

import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModulePathScanner;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.sandbox.ModuleSecurityManager;
import org.terasology.gestalt.module.sandbox.ModuleSecurityPolicy;
import org.terasology.gestalt.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.gestalt.module.sandbox.WarnOnlyProviderFactory;

import java.nio.file.Paths;
import java.security.Policy;

public class ModuleManager {

    private static ModuleEnvironment environment;

    public ModuleManager() {
        try {
            ModulePathScanner scanner = new ModulePathScanner(new ModuleFactory());
            ModuleRegistry registry = new TableModuleRegistry();
            scanner.scan(registry, Paths.get("..").resolve("modules").toFile());

            Policy.setPolicy(new ModuleSecurityPolicy());
            System.setSecurityManager(new ModuleSecurityManager());
            environment = new ModuleEnvironment(Sets.newHashSet(registry),
                    new WarnOnlyProviderFactory(new StandardPermissionProviderFactory()));
        } catch (Exception e) {
            throw e;
        }
    }

    public ModuleEnvironment getEnvironment() {
        return environment;
    }
}
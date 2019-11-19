package org.pyotr.engine.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.exceptions.InvalidModulePathException;
import org.terasology.gestalt.module.resources.CompositeFileSource;
import org.terasology.gestalt.module.resources.DirectoryFileSource;
import org.terasology.gestalt.module.resources.ModuleFileSource;
import org.terasology.gestalt.util.Varargs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PyotrModuleFactory extends ModuleFactory {

    private String reflectionsCachePath = "reflections.cache";

    public Module createClasspathModule(ModuleMetadata metadata, Class<?> primaryClass, Class<?>... additionalClasses) throws URISyntaxException {
        Set<Path> paths = Sets.newLinkedHashSet();

        for (Class<?> type : Varargs.combineToSet(primaryClass, additionalClasses)) {
            try {
                paths.add(Paths.get(type.getProtectionDomain().getCodeSource().getLocation().toURI()));
            } catch (Exception e) {
                throw new InvalidModulePathException("Path cannot be converted to URL: " + type.toString(), e);
            }
        }

        Reflections reflectionsCache = null;
        ImmutableList.Builder<File> builder = ImmutableList.builder();
        List<ModuleFileSource> fileSources = new ArrayList<ModuleFileSource>();
        for (Path path : paths) {
            try {
                File pathFile = new File(path.toUri());
                builder.add(pathFile);
                Reflections reflection;
                reflection = readReflectionsCacheFromPath(path.resolve("java/main"), reflectionsCache);
                fileSources.add(new DirectoryFileSource(pathFile));

                if (reflection == null) {
                    reflection = new Reflections(new ConfigurationBuilder()
                            .addClassLoader(ClassLoader.getSystemClassLoader())
                            .addUrls(path.toUri().toURL())
                            .addScanners(new SubTypesScanner(), new TypeAnnotationsScanner()));
                }

                if (reflectionsCache == null) {
                    reflectionsCache = reflection;
                } else {
                    reflectionsCache.merge(reflection);
                }
            } catch (Exception e) {
                throw new InvalidModulePathException("Path cannot be converted to URL: " + path, e);
            }
        }
        return new Module(metadata, new CompositeFileSource(fileSources), builder.build(), reflectionsCache, x -> true);
    }

    private Reflections readReflectionsCacheFromPath(Path path, Reflections reflectionsCache) {
        Path reflectionsCacheFile = path.resolve(reflectionsCachePath);
        if (Files.isRegularFile(reflectionsCacheFile)) {
            try (InputStream stream = new BufferedInputStream(Files.newInputStream(path.resolve(reflectionsCachePath)))) {
                if (reflectionsCache == null) {
                    reflectionsCache = new ConfigurationBuilder().getSerializer().read(stream);
                } else {
                    reflectionsCache.collect(stream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reflectionsCache;
    }
}
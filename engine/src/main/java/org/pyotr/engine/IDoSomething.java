package org.pyotr.engine;

import org.slf4j.Logger;
import org.terasology.gestalt.module.sandbox.API;

@API
public interface IDoSomething {

    void doSomething(Logger logger);
}
package org.kie.dmn.api.core;

import org.kie.api.io.Resource;


public interface DMNEntity {

    String getName();

    String getNamespace();

    Resource getResource();
}

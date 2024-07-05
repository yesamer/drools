/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.dmn.core.compiler;

import java.util.Collection;
import java.util.List;

import org.kie.dmn.api.core.DMNEntity;
import org.kie.dmn.feel.util.Either;
import org.kie.dmn.model.api.Import;
import org.kie.dmn.model.api.NamespaceConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportDMNResolverUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDMNResolverUtil.class);

    private ImportDMNResolverUtil() {
        // No constructor for util class.
    }

    public static <T extends DMNEntity> Either<String, T> resolve(Import importElement, Collection<T> dmnEntities) {
        final String importNamespace = importElement.getNamespace();
        final String importName = importElement.getName();
        final String importLocationURI = importElement.getLocationURI(); // This is optional

        LOGGER.debug("Resolving DMN Import with namespace={} name={} locationURI={}", importNamespace, importName, importLocationURI);

        List<T> matchingDMNEntities = dmnEntities.stream()
                .filter(entity -> findDMNEntityByLocationURI(entity, importLocationURI))
                .filter(entity -> findByDMNEntityNamespace(entity, importNamespace))
                .toList();
        if (matchingDMNEntities.size() == 1) {
            DMNEntity matched = matchingDMNEntities.get(0);
            LOGGER.debug("DMN Import resolved! namespace={} name={} locationURI={}", matched.getNamespace(), matched.getName(), matched.getResource().getSourcePath());
            return Either.ofRight(matchingDMNEntities.get(0));
        } else if (matchingDMNEntities.isEmpty())  {
            LOGGER.error("Impossible to find the Imported DMN with {} namespace and {} name located at {}",
                    importNamespace, importName, importLocationURI);
            return Either.ofLeft(String.format("Impossible to find the Imported DMN with %s namespace and %s name located at %s",
                    importNamespace, importName, importLocationURI));
        } else {
            LOGGER.error("Found {} number of collision resolving an Imported DMN with {} namespace and {} name located at {}.",
                    matchingDMNEntities.size(), importNamespace, importName, importLocationURI);
                return Either.ofLeft(String.format("Found a collision resolving an Imported DMN with %s namespace and %s " +
                                "name located at %s. There are %s DMN files with those values in your project. Please " +
                                "change the DMN namespaces and make them unique to fix this issue.",
                        importNamespace, importName, importLocationURI, matchingDMNEntities.size()));
        }
    }

    static boolean findDMNEntityByLocationURI(DMNEntity entity, String locationURI) {
        if (locationURI == null || entity.getResource() == null || entity.getResource().getSourcePath() == null) {
            return true;
        }
        String locationURIPath = locationURI.replace('\\', '/')
                                            .replace( "../", "")
                                            .replace( "./", "");
        String dmnEntitySourcePath = entity.getResource().getSourcePath().replace('\\', '/');
        return dmnEntitySourcePath.endsWith(locationURIPath);
    }

    static boolean findByDMNEntityNamespace(DMNEntity entity, String namespace) {
        if (namespace == null || entity.getNamespace() == null) {
            return false;
        }
        String dmnEntityNamespace = entity.getNamespace();
        return dmnEntityNamespace.equals(namespace);
    }

    public enum ImportType {
        UNKNOWN,
        DMN,
        PMML;
    }

    public static ImportType whichImportType(Import importElement) {
        return switch (importElement.getImportType()) {
            case org.kie.dmn.model.v1_1.KieDMNModelInstrumentedBase.URI_DMN,
                 "http://www.omg.org/spec/DMN1-2Alpha/20160929/MODEL",
                 org.kie.dmn.model.v1_2.KieDMNModelInstrumentedBase.URI_DMN,
                 org.kie.dmn.model.v1_3.KieDMNModelInstrumentedBase.URI_DMN,
                 org.kie.dmn.model.v1_4.KieDMNModelInstrumentedBase.URI_DMN,
                 org.kie.dmn.model.v1_5.KieDMNModelInstrumentedBase.URI_DMN -> ImportType.DMN;
            case NamespaceConsts.PMML_3_0, NamespaceConsts.PMML_3_1, NamespaceConsts.PMML_3_2, NamespaceConsts.PMML_4_0,
                 NamespaceConsts.PMML_4_1, NamespaceConsts.PMML_4_2, NamespaceConsts.PMML_4_3 -> ImportType.PMML;
            default -> ImportType.UNKNOWN;
        };
    }
}

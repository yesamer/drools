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
import java.util.function.Function;

import javax.xml.namespace.QName;

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

    public static <T> Either<String, T> resolveImportDMN(Import importElement, Collection<T> dmnAssets, Function<T, QName> idExtractor) {
        final String importNamespace = importElement.getNamespace();  // MUST BE UNIQUE
        final String importName = importElement.getName();  // The import prefix
        final String importLocationUri = importElement.getLocationURI(); // Optional Location URI of the

        List<T> dmnAssetsWithImportNamespace = dmnAssets.stream()
                .filter(dmnAsset -> idExtractor.apply(dmnAsset).getNamespaceURI().equals(importNamespace))
                .toList();
        if (dmnAssetsWithImportNamespace.size() == 1) {
            return Either.ofRight(dmnAssetsWithImportNamespace.get(0));
        } else if (dmnAssetsWithImportNamespace.isEmpty()) {
            return Either.ofLeft(String.format("Impossible to find the Imported DMN with %s namespace and %s name",
                    importNamespace, importName));
        } else {
            return Either.ofLeft(String.format("The project contains " + dmnAssetsWithImportNamespace.size() + " DMN files with the same namespace: %s, name: %s",
                    importNamespace, importName));
        }
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

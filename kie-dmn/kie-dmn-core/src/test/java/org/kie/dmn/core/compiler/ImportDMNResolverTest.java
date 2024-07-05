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

import java.util.Arrays;
import java.util.List;

import org.drools.io.FileSystemResource;
import org.junit.jupiter.api.Test;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.feel.util.Either;
import org.kie.dmn.model.api.Definitions;
import org.kie.dmn.model.api.Import;
import org.kie.dmn.model.v1_5.TDefinitions;
import org.kie.dmn.model.v1_5.TImport;

import static org.assertj.core.api.Assertions.assertThat;

class ImportDMNResolverUtilTest {

    static String FAKE_PATH_UNIX = "/myproject/src/main/resources/dmn/";
    static String FAKE_PATH_WINDOWS = "C:\\myproject\\src\\main\\resources\\dmn\\";

    @Test
    void resolveImportNoLocationURI() {
        final Import i = makeImport("ns1", null, null);
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX +  "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX +  "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns1");
    }

    @Test
    void resolveImportWithAbsoluteLocationURI() {
        final Import i = makeImport("ns2", null, FAKE_PATH_UNIX +  "m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns2");
    }

    @Test
    void resolveImportWithAbsoluteLocationURIWindows() {
        final Import i = makeImport("ns2", null, FAKE_PATH_WINDOWS +  "m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_WINDOWS + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_WINDOWS + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_WINDOWS + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns2");
    }

    @Test
    void resolveImportWithRelativeLocationURI() {
        final Import i = makeImport("ns2", null, "./m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns2");
    }

    @Test
    void resolveImportWithRelativeLocationURI2() {
        final Import i = makeImport("ns3", null, "./../dmn/m3.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns3");
    }

    @Test
    void resolveImportWithRelativeLocationURIWindows() {
        final Import i = makeImport("ns2", null, "\\m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns2");
    }

    @Test
    void resolveImportWithRelativeLocationURIWindows2() {
        final Import i = makeImport("ns3", null, "..\\dmn\\m3.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns3");
    }

    @Test
    void resolveImportWithSameFolderLocationURI() {
        final Import i = makeImport("ns3", null, "m3.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns3");
    }

    @Test
    void resolveImportWithSameFolderLocationURIWindows() {
        final Import i = makeImport("ns3", null, "m3.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_WINDOWS + "m1.dmn"),
                makeDMNModel("ns2", "m2", FAKE_PATH_WINDOWS + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_WINDOWS + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns3");
    }

    @Test
    void resolveImportNoLocationURIDuplicatedNamespace() {
        final Import i = makeImport("ns1", null, null);
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns1", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isLeft()).isTrue();
    }

    @Test
    void resolveImportWithLocationURIDuplicatedNamespace() {
        final Import i = makeImport("ns1", null, FAKE_PATH_UNIX + "m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_UNIX + "m1.dmn"),
                makeDMNModel("ns1", "m2", FAKE_PATH_UNIX + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_UNIX + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns1");
        assertThat(result.getOrElse(null).getResource().getSourcePath()).isEqualTo(FAKE_PATH_UNIX + "m2.dmn");
    }

    @Test
    void resolveImportWithLocationURIDuplicatedNamespaceWindows() {
        final Import i = makeImport("ns1", null, FAKE_PATH_WINDOWS + "m2.dmn");
        final List<DMNModel> models = Arrays.asList(
                makeDMNModel("ns1", "m1", FAKE_PATH_WINDOWS + "m1.dmn"),
                makeDMNModel("ns1", "m2", FAKE_PATH_WINDOWS + "m2.dmn"),
                makeDMNModel("ns3", "m3", FAKE_PATH_WINDOWS + "m3.dmn"));
        final Either<String, DMNModel> result = ImportDMNResolverUtil.resolve(i, models);
        assertThat(result.isRight()).isTrue();
        assertThat(result.getOrElse(null).getNamespace()).isEqualTo("ns1");
        assertThat(result.getOrElse(null).getResource().getSourcePath()).isEqualTo(FAKE_PATH_WINDOWS + "m2.dmn");
    }

    private DMNModel makeDMNModel(final String namespace, final String name, final String sourcePath) {
        final Definitions definitions = new TDefinitions();
        definitions.setNamespace(namespace);
        definitions.setName(name);
        final Resource resource = new FileSystemResource();
        resource.setSourcePath(sourcePath);

        return new DMNModelImpl(definitions, resource);
    }

    private Import makeImport(final String namespace, final String name, final String localtionURI) {
        final Import i = new TImport();
        i.setNamespace(namespace);
        i.setName(name);
        i.setLocationURI(localtionURI);
        return i;
    }

}

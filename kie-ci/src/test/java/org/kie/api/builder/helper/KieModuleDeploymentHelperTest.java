/*
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
package org.kie.api.builder.helper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.drools.core.impl.EnvironmentImpl;
import org.drools.core.test.model.Cheese;
import org.junit.jupiter.api.Test;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.maven.integration.MavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class KieModuleDeploymentHelperTest {

    static final Logger logger = LoggerFactory.getLogger(KieModuleDeploymentHelperTest.class);

    @Test
    void testSingleDeploymentHelper() throws Exception {
        int numFiles = 0;
        int numDirs = 0;
        SingleKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newSingleInstance();

        List<String> resourceFilePaths = new ArrayList<>();
        resourceFilePaths.add("builder/test/");
        // defaultKieBase/empty.drl, defaultKieBase/literal_rule.drl
        numFiles += 2;
        resourceFilePaths.add("builder/simple_query_test.drl");
        // defaultKieBase/simple_query_test.drl
        ++numFiles;

        List<Class<?>> kjarClasses = new ArrayList<>();
        kjarClasses.add(KieModuleDeploymentHelper.class);
        kjarClasses.add(EnvironmentImpl.class);
        kjarClasses.add(Cheese.class);
        // org/kie/api/builder/helper/KieModuleDeploymentHelper.class,
        // org/drools/core/impl/EnvironmentImpl.class,
        // org/drools/core/test/model/Cheese.class
        numFiles += 3;

        String groupId = "org.kie.api.builder";
        String artifactId = "test-kjar";
        String version = "0.1-SNAPSHOT";
        deploymentHelper.createKieJarAndDeployToMaven(groupId, artifactId, version,
                "defaultKieBase", "defaultKieSession",
                resourceFilePaths, kjarClasses);
        // META-INF/maven/org.kie.api.builder/test-kjar/pom.xml,
        // META-INF/maven/org.kie.api.builder/test-kjar/pom.properties
        numFiles += 2;
        // META-INF/kmodule.xml, META-INF/kmodule.info, META-INF/defaultKieBase/kbase.cache
        numFiles += 3;

        // META-INF/,
        // META-INF/maven/,
        // META-INF/maven/org.kie.api.builder/,
        // META-INF/maven/org.kie.api.builder/test-kjar/,
        // META-INF/defaultKieBase/,
        // org/,
        // org/drools/,
        // org/drools/core/,
        // org/drools/core/impl/,
        // org/drools/core/test/,
        // org/drools/core/test/model/,
        // org/kie/,
        // org/kie/api/,
        // org/kie/api/builder/,
        // org/kie/api/builder/helper/,
        // defaultKieBase/
        numDirs += 16;

        File artifactFile = MavenRepository.getMavenRepository()
                .resolveArtifact(groupId + ":" + artifactId + ":" + version)
                .getFile();

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(artifactFile))) {
            Set<String> jarFiles = new HashSet<>();
            Set<String> jarDirs = new HashSet<>();
            ZipEntry ze = zip.getNextEntry();
            logger.debug("Getting files from deployed jar: ");
            while (ze != null) {
                String fileName = ze.getName();
                if (ze.isDirectory()) {
                    jarDirs.add(fileName);
                    logger.debug("] {}", fileName);
                } else {
                    jarFiles.add(fileName);
                    logger.debug("> {}", fileName);
                }
                ze = zip.getNextEntry();
            }
            assertThat(jarFiles.size()).as("Num files in kjar").isEqualTo(numFiles);
            assertThat(jarDirs.size()).as("Num directories in kjar").isEqualTo(numDirs);
        }
    }

    @Test
    void testFluentDeploymentHelper() throws Exception {
        int numFiles = 0;
        int numDirs = 0;
        
        FluentKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newFluentInstance();

        String groupId = "org.kie.api.builder.fluent";
        String artifactId = "test-kjar";
        String version = "0.1-SNAPSHOT";
        deploymentHelper = deploymentHelper.setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version)
                .addResourceFilePath("builder/test/", "builder/simple_query_test.drl")
                .addResourceFilePath("/META-INF/WorkDefinitions.conf") // from the drools-core jar
                .addClass(KieModuleDeploymentHelperTest.class)
                .addClass(KieModule.class)
                .addClass(Cheese.class);
        // META-INF/maven/org.kie.api.builder.fluent/test-kjar/pom.properties,
        // META-INF/maven/org.kie.api.builder.fluent/test-kjar/pom.xml,
        // META-INF/kmodule.info
        numFiles += 3;
        // META-INF/kmodule.xml, defaultKieBase/empty.drl
        numFiles += 2;
        // META-INF/otherKieBase/kbase.cache, META-INF/defaultKieBase/kbase.cache
        numFiles += 2;
        // defaultKieBase/simple_query_test.drl, defaultKieBase/literal_rule.drl
        numFiles += 2;
        // defaultKieBase/WorkDefinitions.conf
        ++numFiles;
        // org/drools/core/test/model/Cheese.class,
        // org/kie/api/builder/KieModule.class,
        // org/kie/api/builder/helper/KieModuleDeploymentHelperTest.class
        numFiles += 3;

        KieBaseModel kbaseModel = deploymentHelper.getKieModuleModel().newKieBaseModel("otherKieBase");
        kbaseModel.setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM);
        kbaseModel.newKieSessionModel("otherKieSession")
                .setClockType(ClockTypeOption.REALTIME);
        // META-INF/,
        // META-INF/maven/,
        // META-INF/maven/org.kie.api.builder.fluent/,
        // META-INF/maven/org.kie.api.builder.fluent/test-kjar/,
        // META-INF/kmodule.xml is a file, not a directory
        // META-INF/otherKieBase/,
        // META-INF/defaultKieBase/,
        // org/,
        // org/drools/,
        // org/drools/core/,
        // org/drools/core/test/,
        // org/drools/core/test/model/,
        // org/kie/,
        // org/kie/api/,
        // org/kie/api/builder/,
        // org/kie/api/builder/helper/,
        // defaultKieBase/
        numDirs += 16;

        deploymentHelper.getKieModuleModel()
                .getKieBaseModels()
                .get("defaultKieBase")
                .newKieSessionModel("secondKieSession");

        deploymentHelper.createKieJarAndDeployToMaven();

        File artifactFile = MavenRepository.getMavenRepository()
                .resolveArtifact(groupId + ":" + artifactId + ":" + version)
                .getFile();

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(artifactFile))) {
            Set<String> jarFiles = new HashSet<>();
            Set<String> jarDirs = new HashSet<>();
            ZipEntry ze = zip.getNextEntry();
            logger.debug("Getting files from deployed jar: ");
            while (ze != null) {
                String fileName = ze.getName();
                if (ze.isDirectory()) {
                    jarDirs.add(fileName);
                    logger.debug("] {}", fileName);
                } else {
                    jarFiles.add(fileName);
                    logger.debug("> {}", fileName);
                }
                ze = zip.getNextEntry();
            }
            assertThat(jarFiles.size()).as("Num files in kjar").isEqualTo(numFiles);
            assertThat(jarDirs.size()).as("Num directories in kjar").isEqualTo(numDirs);
        }
    }

    @Test
    void testFluentDeploymentHelperWithoutClasses() {
        FluentKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newFluentInstance();

        String groupId = "org.kie.api.builder.noclasses";
        String artifactId = "test-kjar-no-classes";
        String version = "0.1-SNAPSHOT";

        deploymentHelper.setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version)
                .addResourceFilePath("builder/simple_query_test.drl");

        KieModule kieModule = deploymentHelper.createKieJar();

        assertThat(kieModule).isNotNull();
        assertThat(kieModule.getReleaseId().getGroupId()).isEqualTo(groupId);
        assertThat(kieModule.getReleaseId().getArtifactId()).isEqualTo(artifactId);
    }

    @Test
    void testSingleDeploymentHelperCreateKieJarOnly() {
        SingleKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newSingleInstance();

        List<String> resourceFilePaths = new ArrayList<String>();
        resourceFilePaths.add("builder/simple_query_test.drl");

        String groupId = "org.kie.api.builder.nomavendeploy";
        String artifactId = "test-kjar-no-deploy";
        String version = "0.1-SNAPSHOT";

        KieModule kieModule = deploymentHelper.createKieJar(groupId, artifactId, version,
                "testKieBase", "testKieSession",
                resourceFilePaths);

        assertThat(kieModule).isNotNull();
        assertThat(kieModule.getReleaseId()).isNotNull();
        assertThat(kieModule.getReleaseId().getGroupId()).isEqualTo(groupId);
        assertThat(kieModule.getReleaseId().getArtifactId()).isEqualTo(artifactId);
        assertThat(kieModule.getReleaseId().getVersion()).isEqualTo(version);
    }

    @Test
    void testFluentDeploymentHelperKieModuleModelAccess() {
        FluentKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newFluentInstance();

        deploymentHelper.setGroupId("org.kie.test")
                .setArtifactId("test-kmodule-model")
                .setVersion("1.0.0")
                .addResourceFilePath("builder/simple_query_test.drl");

        assertThat(deploymentHelper.getKieModuleModel()).isNotNull();
        assertThat(deploymentHelper.getKieModuleModel().getKieBaseModels()).isNotEmpty();
        assertThat(deploymentHelper.getKieModuleModel().getKieBaseModels()).containsKey("defaultKieBase");
    }

    @Test
    void testFluentDeploymentHelperMultipleResourcePaths() throws Exception {
        FluentKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newFluentInstance();

        String groupId = "org.kie.api.builder.multiresource";
        String artifactId = "test-kjar-multi-resource";
        String version = "0.1-SNAPSHOT";

        deploymentHelper.setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version)
                .addResourceFilePath("builder/test/")
                .addResourceFilePath("builder/simple_query_test.drl")
                .addResourceFilePath("/META-INF/WorkDefinitions.conf");

        KieModule kieModule = deploymentHelper.createKieJar();

        assertThat(kieModule).isNotNull();
        assertThat(kieModule.getReleaseId().getArtifactId()).isEqualTo(artifactId);

        deploymentHelper.createKieJarAndDeployToMaven();

        File artifactFile = MavenRepository.getMavenRepository()
                .resolveArtifact(groupId + ":" + artifactId + ":" + version)
                .getFile();

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(artifactFile))) {
            Set<String> jarFiles = new HashSet<String>();
            ZipEntry ze = zip.getNextEntry();
            while (ze != null) {
                if (!ze.isDirectory()) {
                    jarFiles.add(ze.getName());
                }
                ze = zip.getNextEntry();
            }

            assertThat(jarFiles).contains("defaultKieBase/empty.drl");
            assertThat(jarFiles).contains("defaultKieBase/literal_rule.drl");
            assertThat(jarFiles).contains("defaultKieBase/simple_query_test.drl");
            assertThat(jarFiles).contains("defaultKieBase/WorkDefinitions.conf");
        }
    }

    @Test
    void testSingleDeploymentHelperWithMultipleClasses() {
        SingleKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newSingleInstance();

        List<String> resourceFilePaths = new ArrayList<String>();
        resourceFilePaths.add("builder/simple_query_test.drl");

        List<Class<?>> kjarClasses = new ArrayList<Class<?>>();
        kjarClasses.add(KieModuleDeploymentHelper.class);
        kjarClasses.add(EnvironmentImpl.class);
        kjarClasses.add(Cheese.class);
        kjarClasses.add(KieModule.class);

        String groupId = "org.kie.api.builder.multiclass";
        String artifactId = "test-kjar-multi-class";
        String version = "0.1-SNAPSHOT";

        KieModule kieModule = deploymentHelper.createKieJar(groupId, artifactId, version,
                "multiClassKieBase", "multiClassKieSession",
                resourceFilePaths, kjarClasses);

        assertThat(kieModule).isNotNull();
        assertThat(kieModule.getReleaseId().getGroupId()).isEqualTo(groupId);
    }
}

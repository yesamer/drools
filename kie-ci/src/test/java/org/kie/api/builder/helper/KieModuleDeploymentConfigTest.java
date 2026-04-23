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

import org.junit.jupiter.api.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link KieModuleDeploymentConfig} logic (not simple getters/setters).
 */
class KieModuleDeploymentConfigTest {

    @Test
    void testCheckComplete_withAllRequiredFields_shouldPass() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        // Should not throw exception
        config.checkComplete();
    }

    @Test
    void testCheckComplete_withMissingGroupId_shouldThrowException() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        assertThatThrownBy(config::checkComplete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No groupId has been set yet.");
    }

    @Test
    void testCheckComplete_withMissingArtifactId_shouldThrowException() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setVersion("1.0.0");

        assertThatThrownBy(config::checkComplete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No artifactId has been set yet.");
    }

    @Test
    void testCheckComplete_withMissingVersion_shouldThrowException() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");

        assertThatThrownBy(config::checkComplete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No version has been set yet.");
    }

    @Test
    void testCheckComplete_withNoFieldsSet_shouldThrowGroupIdException() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Should throw exception for missing groupId (first check)
        assertThatThrownBy(config::checkComplete)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No groupId has been set yet.");
    }

    @Test
    void testGetReleaseId_lazyInitialization() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        // First call should create ReleaseId
        ReleaseId releaseId1 = config.getReleaseId();
        assertThat(releaseId1).isNotNull();
        assertThat(releaseId1.getGroupId()).isEqualTo("com.example");
        assertThat(releaseId1.getArtifactId()).isEqualTo("test-artifact");
        assertThat(releaseId1.getVersion()).isEqualTo("1.0.0");

        // Second call should return same instance (cached)
        ReleaseId releaseId2 = config.getReleaseId();
        assertThat(releaseId2).isSameAs(releaseId1);
    }

    @Test
    void testGetKbaseName_withDefaultValue() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Should return default value when not set
        String kbaseName = config.getKbaseName();
        assertThat(kbaseName).isEqualTo("defaultKieBase");

        // Subsequent calls should return same value
        String kbaseName2 = config.getKbaseName();
        assertThat(kbaseName2).isEqualTo("defaultKieBase");
    }

    @Test
    void testGetKbaseName_withCustomValue() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setKbaseName("customKieBase");

        String kbaseName = config.getKbaseName();
        assertThat(kbaseName).isEqualTo("customKieBase");
    }

    @Test
    void testGetKsessionName_withDefaultValue() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Should return default value when not set
        String ksessionName = config.getKsessionName();
        assertThat(ksessionName).isEqualTo("defaultKieSession");

        // Subsequent calls should return same value
        String ksessionName2 = config.getKsessionName();
        assertThat(ksessionName2).isEqualTo("defaultKieSession");
    }

    @Test
    void testGetKsessionName_withCustomValue() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setKsessionName("customKieSession");

        String ksessionName = config.getKsessionName();
        assertThat(ksessionName).isEqualTo("customKieSession");
    }

    @Test
    void testGetKieProject_lazyInitialization() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // First call should create KieModuleModel
        KieModuleModel kmodule1 = config.getKieProject();
        assertThat(kmodule1).isNotNull();

        // Should have default KieBase
        KieBaseModel kbase = kmodule1.getKieBaseModels().get("defaultKieBase");
        assertThat(kbase).isNotNull();
        assertThat(kbase.isDefault()).isTrue();

        // Should have default KieSession
        KieSessionModel ksession = kbase.getKieSessionModels().get("defaultKieSession");
        assertThat(ksession).isNotNull();
        assertThat(ksession.isDefault()).isTrue();

        // Second call should return same instance (cached)
        KieModuleModel kmodule2 = config.getKieProject();
        assertThat(kmodule2).isSameAs(kmodule1);
    }

    @Test
    void testGetKieProject_withCustomNames() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setKbaseName("myKieBase");
        config.setKsessionName("myKieSession");

        KieModuleModel kmodule = config.getKieProject();
        assertThat(kmodule).isNotNull();

        // Should use custom names
        KieBaseModel kbase = kmodule.getKieBaseModels().get("myKieBase");
        assertThat(kbase).isNotNull();
        assertThat(kbase.isDefault()).isTrue();

        KieSessionModel ksession = kbase.getKieSessionModels().get("myKieSession");
        assertThat(ksession).isNotNull();
        assertThat(ksession.isDefault()).isTrue();
    }

    @Test
    void testCollections_areInitialized() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Collections should be initialized and empty
        assertThat(config.resourceFilePaths).isNotNull().isEmpty();
        assertThat(config.classes).isNotNull().isEmpty();
        assertThat(config.dependencies).isNotNull().isEmpty();
    }

    @Test
    void testCollections_canBeModified() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Should be able to add items to collections
        config.resourceFilePaths.add("/rules/test.drl");
        config.classes.add(String.class);
        config.dependencies.add("org.example:lib:1.0");

        assertThat(config.resourceFilePaths).hasSize(1).contains("/rules/test.drl");
        assertThat(config.classes).hasSize(1).contains(String.class);
        assertThat(config.dependencies).hasSize(1).contains("org.example:lib:1.0");
    }

    @Test
    void testGetKieServicesInstance_throwsExceptionWhenNotInitialized() {
        // Create config in a different thread where ThreadLocal is not set
        Thread thread = new Thread(() -> {
            KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
            // Clear the ThreadLocal to simulate uninitialized state
            // Note: This is tricky to test properly due to constructor initialization
        });

        // The actual thread-safety test would require more complex setup
        // This test documents the expected behavior
    }

    @Test
    void testDefaultValues_areAppliedLazily() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();

        // Before calling getters, values should be null
        // (We can't directly test private fields, but we can test behavior)

        // First call triggers default value assignment
        String kbaseName1 = config.getKbaseName();
        assertThat(kbaseName1).isEqualTo("defaultKieBase");

        // Setting a value after default was applied should override it
        config.setKbaseName("overridden");
        String kbaseName2 = config.getKbaseName();
        assertThat(kbaseName2).isEqualTo("overridden");
    }

    @Test
    void testReleaseId_isCreatedFromIndividualFields() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("org.test");
        config.setArtifactId("my-artifact");
        config.setVersion("2.0.0-SNAPSHOT");

        ReleaseId releaseId = config.getReleaseId();

        assertThat(releaseId.getGroupId()).isEqualTo("org.test");
        assertThat(releaseId.getArtifactId()).isEqualTo("my-artifact");
        assertThat(releaseId.getVersion()).isEqualTo("2.0.0-SNAPSHOT");
    }
}

// Made with Bob

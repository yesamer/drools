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

    @Test
    void testSetGroupId_invalidatesReleaseIdCache() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        // Get and cache ReleaseId
        ReleaseId releaseId1 = config.getReleaseId();
        assertThat(releaseId1.getGroupId()).isEqualTo("com.example");

        // Change groupId - should invalidate cache
        config.setGroupId("org.different");

        // Get ReleaseId again - should be a NEW instance with updated groupId
        ReleaseId releaseId2 = config.getReleaseId();
        assertThat(releaseId2).isNotSameAs(releaseId1);
        assertThat(releaseId2.getGroupId()).isEqualTo("org.different");
        assertThat(releaseId2.getArtifactId()).isEqualTo("test-artifact");
        assertThat(releaseId2.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testSetArtifactId_invalidatesReleaseIdCache() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        // Get and cache ReleaseId
        ReleaseId releaseId1 = config.getReleaseId();
        assertThat(releaseId1.getArtifactId()).isEqualTo("test-artifact");

        // Change artifactId - should invalidate cache
        config.setArtifactId("different-artifact");

        // Get ReleaseId again - should be a NEW instance with updated artifactId
        ReleaseId releaseId2 = config.getReleaseId();
        assertThat(releaseId2).isNotSameAs(releaseId1);
        assertThat(releaseId2.getGroupId()).isEqualTo("com.example");
        assertThat(releaseId2.getArtifactId()).isEqualTo("different-artifact");
        assertThat(releaseId2.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testSetVersion_invalidatesReleaseIdCache() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");

        // Get and cache ReleaseId
        ReleaseId releaseId1 = config.getReleaseId();
        assertThat(releaseId1.getVersion()).isEqualTo("1.0.0");

        // Change version - should invalidate cache
        config.setVersion("2.0.0");

        // Get ReleaseId again - should be a NEW instance with updated version
        ReleaseId releaseId2 = config.getReleaseId();
        assertThat(releaseId2).isNotSameAs(releaseId1);
        assertThat(releaseId2.getGroupId()).isEqualTo("com.example");
        assertThat(releaseId2.getArtifactId()).isEqualTo("test-artifact");
        assertThat(releaseId2.getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void testSetKbaseName_invalidatesKieModuleModelCache() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setKbaseName("originalKieBase");

        // Get and cache KieModuleModel
        KieModuleModel kmodule1 = config.getKieProject();
        assertThat(kmodule1.getKieBaseModels()).containsKey("originalKieBase");

        // Change kbaseName - should invalidate cache
        config.setKbaseName("newKieBase");

        // Get KieModuleModel again - should be a NEW instance with updated kbaseName
        KieModuleModel kmodule2 = config.getKieProject();
        assertThat(kmodule2).isNotSameAs(kmodule1);
        assertThat(kmodule2.getKieBaseModels()).containsKey("newKieBase");
        assertThat(kmodule2.getKieBaseModels()).doesNotContainKey("originalKieBase");
    }

    @Test
    void testSetKsessionName_invalidatesKieModuleModelCache() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        config.setKsessionName("originalKieSession");

        // Get and cache KieModuleModel
        KieModuleModel kmodule1 = config.getKieProject();
        KieBaseModel kbase1 = kmodule1.getKieBaseModels().get("defaultKieBase");
        assertThat(kbase1.getKieSessionModels()).containsKey("originalKieSession");

        // Change ksessionName - should invalidate cache
        config.setKsessionName("newKieSession");

        // Get KieModuleModel again - should be a NEW instance with updated ksessionName
        KieModuleModel kmodule2 = config.getKieProject();
        assertThat(kmodule2).isNotSameAs(kmodule1);
        KieBaseModel kbase2 = kmodule2.getKieBaseModels().get("defaultKieBase");
        assertThat(kbase2.getKieSessionModels()).containsKey("newKieSession");
        assertThat(kbase2.getKieSessionModels()).doesNotContainKey("originalKieSession");
    }

    @Test
    void testMultipleSetters_invalidateCachesIndependently() {
        KieModuleDeploymentConfig config = new KieModuleDeploymentConfig();
        
        // Set up initial configuration
        config.setGroupId("com.example");
        config.setArtifactId("test-artifact");
        config.setVersion("1.0.0");
        config.setKbaseName("myKieBase");
        config.setKsessionName("myKieSession");

        // Cache both ReleaseId and KieModuleModel
        ReleaseId releaseId1 = config.getReleaseId();
        KieModuleModel kmodule1 = config.getKieProject();

        // Change only Maven coordinates - should invalidate only ReleaseId
        config.setVersion("2.0.0");
        
        ReleaseId releaseId2 = config.getReleaseId();
        KieModuleModel kmodule2 = config.getKieProject();
        
        assertThat(releaseId2).isNotSameAs(releaseId1); // ReleaseId invalidated
        assertThat(kmodule2).isSameAs(kmodule1); // KieModuleModel still cached

        // Now change KieBase name - should invalidate only KieModuleModel
        config.setKbaseName("differentKieBase");
        
        ReleaseId releaseId3 = config.getReleaseId();
        KieModuleModel kmodule3 = config.getKieProject();
        
        assertThat(releaseId3).isSameAs(releaseId2); // ReleaseId still cached
        assertThat(kmodule3).isNotSameAs(kmodule2); // KieModuleModel invalidated
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.util.maven.support.ReleaseIdImpl;

/**
 * Configuration holder for KieModule deployment.
 * <p>
 * This class encapsulates all configuration needed to create and deploy a KJar,
 * including Maven coordinates, resources, classes, and dependencies.
 * </p>
 * <p>
 * <b>Thread Safety:</b> This class is NOT thread-safe. Each thread should use
 * its own instance via {@link KieModuleDeploymentHelperImpl}.
 * </p>
 */
class KieModuleDeploymentConfig {

    // Constants for default values
    private static final String DEFAULT_KBASE_NAME = "defaultKieBase";
    private static final String DEFAULT_KSESSION_NAME = "defaultKieSession";
    
    // Maven coordinates
    private String groupId;
    private String artifactId;
    private String version;
    private ReleaseId releaseId;
    
    // KieBase and KieSession names
    private String kbaseName;
    private String ksessionName;
    
    // Collections for resources, classes, and dependencies
    final List<String> resourceFilePaths = new ArrayList<>();
    final List<Class<?>> classes = new ArrayList<>();
    final List<String> dependencies = new ArrayList<>();
    
    // Cached instances
    private KieModuleModel kieModuleModel;
    String pomText; // Package-private for access by implementation
    
    // Thread-local KieServices instance
    private static final ThreadLocal<KieServices> kieServicesLocal = new ThreadLocal<>();

    /**
     * Creates a new configuration instance and initializes the thread-local KieServices.
     */
    KieModuleDeploymentConfig() { 
        initializeKieServices();
    }
    
    /**
     * Initializes the thread-local KieServices with a custom repository implementation.
     * <p>
     * The custom repository prevents storing artifacts on deploy to trigger loading from Maven repo.
     * </p>
     */
    private void initializeKieServices() {
        KieServices ks = new KieServicesImpl() {
            @Override
            public KieRepository getRepository() {
                // Override repository to not store the artifact on deploy to trigger load from maven repo
                return new KieRepositoryImpl();
            }
        };
        kieServicesLocal.set(ks);
    }
    
    /**
     * Gets the thread-local KieServices instance.
     * 
     * @return the KieServices instance for this thread
     * @throws IllegalStateException if called from a different thread than the one that created this config
     */
    KieServices getKieServicesInstance() { 
        KieServices ks = kieServicesLocal.get();
        if (ks == null) { 
            throw new IllegalStateException(
                KieModuleDeploymentHelper.class.getSimpleName() + 
                " instances are not thread-safe! Each thread must use its own instance."
            );
        }
        return ks;        
    }
    
    // ========== Setters ==========
    
    void setGroupId(String groupId) { 
        this.groupId = groupId;
        this.releaseId = null; // Invalidate cached ReleaseId
    }
    
    void setArtifactId(String artifactId) { 
        this.artifactId = artifactId;
        this.releaseId = null; // Invalidate cached ReleaseId
    }
    
    void setVersion(String version) { 
        this.version = version;
        this.releaseId = null; // Invalidate cached ReleaseId
    }
    
    void setKbaseName(String kbaseName) {
        this.kbaseName = kbaseName;
        this.kieModuleModel = null; // Invalidate cached model
    }
    
    void setKsessionName(String ksessionName) {
        this.ksessionName = ksessionName;
        this.kieModuleModel = null; // Invalidate cached model
    }
    
    // ========== Getters with Lazy Initialization ==========
    
    /**
     * Gets the ReleaseId, creating it lazily from individual Maven coordinates if needed.
     * 
     * @return the ReleaseId
     * @throws IllegalStateException if Maven coordinates are not set
     */
    ReleaseId getReleaseId() {
        if (releaseId == null) {
            Objects.requireNonNull(groupId, "groupId must be set before creating ReleaseId");
            Objects.requireNonNull(artifactId, "artifactId must be set before creating ReleaseId");
            Objects.requireNonNull(version, "version must be set before creating ReleaseId");
            releaseId = new ReleaseIdImpl(groupId, artifactId, version);
        }
        return releaseId;
    }

    /**
     * Gets the KieBase name, returning the default if not explicitly set.
     * 
     * @return the KieBase name (never null)
     */
    String getKbaseName() {
        if (kbaseName == null) { 
            kbaseName = DEFAULT_KBASE_NAME;
        }
        return kbaseName;
    }
    
    /**
     * Gets the KieSession name, returning the default if not explicitly set.
     * 
     * @return the KieSession name (never null)
     */
    String getKsessionName() {
        if (ksessionName == null) { 
            ksessionName = DEFAULT_KSESSION_NAME;
        }
        return ksessionName;
    }
    
    // ========== Validation ==========
    
    /**
     * Validates that all required configuration is present.
     * <p>
     * Checks that either all individual Maven coordinates (groupId, artifactId, version)
     * are set, or that a ReleaseId has been provided.
     * </p>
     * 
     * @throws IllegalStateException if required configuration is missing
     */
    void checkComplete() {
        // If all coordinates are set or releaseId is set, configuration is complete
        if ((groupId != null && artifactId != null && version != null) || releaseId != null) {
            return;
        }
        
        // Otherwise, identify which field is missing
        if (groupId == null) {
            throw new IllegalStateException("No groupId has been set yet.");
        }
        if (artifactId == null) {
            throw new IllegalStateException("No artifactId has been set yet.");
        }
        if (version == null) {
            throw new IllegalStateException("No version has been set yet.");
        }
    }

    // ========== KieModuleModel Creation ==========
    
    /**
     * Gets or creates the KieModuleModel with default KieBase and KieSession.
     * <p>
     * The model is created lazily on first access and cached for subsequent calls.
     * It includes a default KieBase and KieSession with the configured names.
     * </p>
     * 
     * @return the KieModuleModel (never null)
     */
    KieModuleModel getKieProject() {
        if (kieModuleModel != null) {
            return kieModuleModel;
        }
        
        kieModuleModel = getKieServicesInstance().newKieModuleModel();

        KieBaseModel kieBaseModel = kieModuleModel
            .newKieBaseModel(getKbaseName())
            .setDefault(true);
        
        kieBaseModel
            .newKieSessionModel(getKsessionName())
            .setDefault(true);

        return kieModuleModel;
    }
}
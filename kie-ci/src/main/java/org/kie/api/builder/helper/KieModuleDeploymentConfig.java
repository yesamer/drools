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
    
    List<String> resourceFilePaths = new ArrayList<>();
    List<Class<?>> classes = new ArrayList<>();
    List<String> dependencies = new ArrayList<>();
    
    private KieModuleModel kproj = null;
    String pomText;

    public KieModuleDeploymentConfig() { 
        KieServices ks = new KieServicesImpl() {
            public KieRepository getRepository() {
                // override repository to not store the artifact on deploy to trigger load from maven repo
                return new KieRepositoryImpl();
            }
        };
        kieServicesLocal.set(ks);
    }
    
    private static final ThreadLocal<KieServices> kieServicesLocal = new ThreadLocal<>();
    
    KieServices getKieServicesInstance() { 
        KieServices ks = kieServicesLocal.get();
        if( ks == null ) { 
            throw new IllegalStateException(KieModuleDeploymentHelper.class.getSimpleName() + " instances are not thread-safe!");
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
    
    
    /**
     * Other methods
     */
    
    void checkComplete() {
        if ((groupId != null && artifactId != null && version != null) || releaseId != null) {
            return;
        } else if (releaseId == null) {
            if (groupId == null) {
                throw new IllegalStateException("No groupId has been set yet.");
            } else if (artifactId == null) {
                throw new IllegalStateException("No artifactId has been set yet.");
            } else if (version == null) {
                throw new IllegalStateException("No version has been set yet.");
            } else if (groupId.equals(artifactId) && artifactId.equals(version) && version == null) {
                throw new IllegalStateException("None of groupId, artifactId, version or releaseId have been set.");
            }
        }
    }

    KieModuleModel getKieProject() {
        if (kproj != null) {
            return kproj;
        }
        kproj = getKieServicesInstance().newKieModuleModel();

        KieBaseModel kieBaseModel = kproj.newKieBaseModel(getKbaseName()).setDefault(true);
        kieBaseModel.newKieSessionModel(getKsessionName()).setDefault(true);

        return kproj;
    }
}

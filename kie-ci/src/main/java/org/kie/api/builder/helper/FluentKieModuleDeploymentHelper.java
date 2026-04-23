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

import java.util.List;

import org.kie.api.KieBase;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;

/**
 * Fluent interface for creating and deploying KJars (Knowledge JARs) with method chaining.
 * <p>
 * Provides a readable, step-by-step API for configuring KJars. Ideal when you want to build
 * configuration incrementally or when readability is a priority.
 * </p>
 * 
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * KieModuleDeployment.fluent()
 *     .setGroupId("com.example")
 *     .setArtifactId("business-rules")
 *     .setVersion("1.0.0")
 *     .setKBaseName("defaultKieBase")
 *     .setKieSessionname("defaultKieSession")
 *     .addResourceFilePath("/rules/validation.drl")
 *     .addClass(Customer.class)
 *     .addDependencies("org.apache.commons:commons-lang3:3.12.0")
 *     .createKieJarAndDeployToMaven();
 * }</pre>
 * 
 * <h3>Advanced Configuration:</h3>
 * <p>
 * For multiple KieBases or custom configurations, access the {@link KieModuleModel}:
 * </p>
 * <pre>{@code
 * FluentKieModuleDeploymentHelper helper = KieModuleDeployment.fluent()
 *     .setGroupId("com.example")
 *     .setArtifactId("multi-base-rules")
 *     .setVersion("1.0.0");
 * 
 * KieModuleModel kmodule = helper.getKieModuleModel();
 * kmodule.newKieBaseModel("streamingBase")
 *     .setEventProcessingMode(EventProcessingOption.STREAM)
 *     .newKieSessionModel("streamSession")
 *     .setClockType(ClockTypeOption.PSEUDO);
 * 
 * helper.addResourceFilePath("/rules/").createKieJarAndDeployToMaven();
 * }</pre>
 * 
 * @see org.kie.api.builder.KieModuleDeployment#fluent()
 * @see SingleKieModuleDeploymentHelper
 * @since 1.0.0
 */
public abstract class FluentKieModuleDeploymentHelper extends KieModuleDeploymentHelper {

    /**
     * Sets the Maven group ID (e.g., "com.example").
     * 
     * @param groupId the Maven group ID (required)
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if groupId is null
     */
    public abstract FluentKieModuleDeploymentHelper setGroupId(String groupId);

    /**
     * Sets the Maven artifact ID (e.g., "business-rules").
     * 
     * @param artifactId the Maven artifact ID (required)
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if artifactId is null
     */
    public abstract FluentKieModuleDeploymentHelper setArtifactId(String artifactId);

    /**
     * Sets the Maven version (e.g., "1.0.0" or "1.0-SNAPSHOT").
     * 
     * @param version the Maven version (required)
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if version is null
     */
    public abstract FluentKieModuleDeploymentHelper setVersion(String version);

    /**
     * Sets the default KieBase name.
     * <p>
     * For multiple KieBases with different configurations, use {@link #getKieModuleModel()} instead.
     * </p>
     * 
     * @param kbaseName the KieBase name (e.g., "defaultKieBase")
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if kbaseName is null
     */
    public abstract FluentKieModuleDeploymentHelper setKBaseName(String kbaseName);
   
    /**
     * Sets the default KieSession name.
     * <p>
     * For multiple KieSessions with different configurations, use {@link #getKieModuleModel()} instead.
     * </p>
     * 
     * @param ksessionName the KieSession name (e.g., "defaultKieSession")
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if ksessionName is null
     */
    public abstract FluentKieModuleDeploymentHelper setKieSessionname(String ksessionName);

    /**
     * Sets the resource file paths, replacing any previously set paths.
     * <p>
     * Paths can be individual files, directories (ending with /), classpath resources, or file system paths.
     * </p>
     * 
     * @param resourceFilePaths list of paths to rule files or directories
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if resourceFilePaths is null
     */
    public abstract FluentKieModuleDeploymentHelper setResourceFilePaths(List<String> resourceFilePaths);

    /**
     * Adds one or more resource file paths (additive operation).
     * 
     * @param resourceFilePath one or more paths to rule files or directories
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if resourceFilePath is null or contains null elements
     */
    public abstract FluentKieModuleDeploymentHelper addResourceFilePath(String... resourceFilePath);

    /**
     * Sets the classes to include in the KJar, replacing any previously set classes.
     * 
     * @param classesForKjar list of domain model classes to include
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if classesForKjar is null
     */
    public abstract FluentKieModuleDeploymentHelper setClasses(List<Class<?>> classesForKjar);

    /**
     * Adds one or more classes to include in the KJar (additive operation).
     * 
     * @param classForKjar one or more classes to include
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if classForKjar is null or contains null elements
     */
    public abstract FluentKieModuleDeploymentHelper addClass(Class<?>... classForKjar);
   
    /**
     * Sets Maven dependencies, replacing any previously set dependencies.
     * <p>
     * Dependencies must be in "groupId:artifactId:version" format.
     * </p>
     * 
     * @param dependencies list of dependencies in "G:A:V" format
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if dependencies is null
     */
    public abstract FluentKieModuleDeploymentHelper setDependencies(List<String> dependencies);

    /**
     * Adds one or more Maven dependencies (additive operation).
     * <p>
     * Dependencies must be in "groupId:artifactId:version" format.
     * </p>
     * 
     * @param dependency one or more dependency strings in "G:A:V" format
     * @return this helper instance for method chaining
     * @throws IllegalArgumentException if dependency is null, contains null elements, or format is incorrect
     */
    public abstract FluentKieModuleDeploymentHelper addDependencies(String... dependency);
   
    /**
     * Gets the underlying KieModuleModel for advanced configuration.
     * <p>
     * Use this to configure multiple KieBases, custom KieSessions, event processing modes,
     * equality behaviors, and other advanced features.
     * </p>
     * 
     * @return the {@link KieModuleModel} for advanced configuration
     */
    public abstract KieModuleModel getKieModuleModel();

    /**
     * Resets all configuration on this helper instance.
     * <p>
     * Clears ALL settings including Maven coordinates, resources, classes, dependencies,
     * and KieModuleModel configuration. Useful for reusing the same helper instance.
     * </p>
     * 
     * @return this helper instance (with cleared configuration) for method chaining
     */
    public abstract FluentKieModuleDeploymentHelper resetHelper();
   
    /**
     * Creates the KJar with current configuration (does not deploy to Maven).
     * 
     * @return the created {@link KieModule}
     * @throws IllegalStateException if required configuration (groupId, artifactId, version) is missing
     * @throws RuntimeException if KJar creation fails
     */
    public abstract KieModule createKieJar();
   
    /**
     * Creates the KJar and deploys it to the local Maven repository (~/.m2/repository).
     * <p>
     * After deployment, the KJar can be used as a Maven dependency in other projects.
     * </p>
     * 
     * @throws IllegalStateException if required configuration is missing
     * @throws RuntimeException if KJar creation or deployment fails
     */
    public abstract void createKieJarAndDeployToMaven();
    
}
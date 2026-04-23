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

import org.kie.api.builder.KieModule;

/**
 * Single-method interface for creating and deploying KJars (Knowledge JARs) programmatically.
 * <p>
 * Provides a straightforward API where all configuration parameters are passed in a single method call.
 * Ideal when you have all information upfront and prefer concise code over method chaining.
 * </p>
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * SingleKieModuleDeploymentHelper helper = KieModuleDeployment.single();
 * 
 * helper.createKieJarAndDeployToMaven(
 *     "com.example", "business-rules", "1.0.0",
 *     "defaultKieBase", "defaultKieSession",
 *     Arrays.asList("/rules/validation.drl", "/rules/calculation.drl"),
 *     Arrays.asList(Customer.class, Order.class),
 *     Arrays.asList("org.apache.commons:commons-lang3:3.12.0")
 * );
 * }</pre>
 * 
 * <p>
 * <b>Key Concepts:</b>
 * <ul>
 *   <li><b>KJar</b> - JAR containing Drools rules with kmodule.xml, deployable to Maven</li>
 *   <li><b>KieBase</b> - Repository of compiled rules (can have multiple per KJar)</li>
 *   <li><b>KieSession</b> - Runtime instance for executing rules</li>
 *   <li><b>Resource paths</b> - Classpath resources, file paths, or directories (ending with /)</li>
 *   <li><b>Dependencies</b> - Maven format: "groupId:artifactId:version"</li>
 * </ul>
 * </p>
 * 
 * @see org.kie.api.builder.KieModuleDeployment#single()
 * @see FluentKieModuleDeploymentHelper
 * @since 1.0.0
 */
public interface SingleKieModuleDeploymentHelper {

    /**
     * Creates a KJar with basic configuration (no custom classes or dependencies).
     * 
     * @param groupId Maven group ID (e.g., "com.example")
     * @param artifactId Maven artifact ID (e.g., "my-rules")
     * @param version Maven version (e.g., "1.0.0" or "1.0-SNAPSHOT")
     * @param kbaseName KieBase name (e.g., "defaultKieBase")
     * @param ksessionName KieSession name (e.g., "defaultKieSession")
     * @param resourceFilePaths paths to rule files or directories
     * @return the created {@link KieModule}
     * @throws IllegalArgumentException if any required parameter is null or invalid
     * @throws RuntimeException if KJar creation fails
     */
    KieModule createKieJar(String groupId, String artifactId, String version,
                           String kbaseName, String ksessionName,
                           List<String> resourceFilePaths);

    /**
     * Creates a KJar including custom domain model classes.
     * 
     * @param groupId Maven group ID
     * @param artifactId Maven artifact ID
     * @param version Maven version
     * @param kbaseName KieBase name
     * @param ksessionName KieSession name
     * @param resourceFilePaths paths to rule files or directories
     * @param classesForKjar domain model classes to include in the KJar
     * @return the created {@link KieModule}
     * @throws IllegalArgumentException if any required parameter is null or invalid
     * @throws RuntimeException if KJar creation fails
     */
    KieModule createKieJar(String groupId, String artifactId, String version,
            String kbaseName, String ksessionName,
            List<String> resourceFilePaths, List<Class<?>> classesForKjar);

    /**
     * Creates a KJar with custom classes and Maven dependencies.
     * 
     * @param groupId Maven group ID
     * @param artifactId Maven artifact ID
     * @param version Maven version
     * @param kbaseName KieBase name
     * @param ksessionName KieSession name
     * @param resourceFilePaths paths to rule files or directories
     * @param classesForKjar domain model classes to include
     * @param dependencies Maven dependencies in "groupId:artifactId:version" format (can be null)
     * @return the created {@link KieModule}
     * @throws IllegalArgumentException if any parameter is invalid or dependency format is incorrect
     * @throws RuntimeException if KJar creation fails
     */
    KieModule createKieJar(String groupId, String artifactId, String version,
            String kbaseName, String ksessionName,
            List<String> resourceFilePaths, List<Class<?>> classesForKjar, 
            List<String> dependencies);

    /**
     * Creates a KJar and deploys it to the local Maven repository (~/.m2/repository).
     * 
     * @param groupId Maven group ID
     * @param artifactId Maven artifact ID
     * @param version Maven version
     * @param kbaseName KieBase name
     * @param ksessionName KieSession name
     * @param resourceFilePaths paths to rule files or directories
     * @throws IllegalArgumentException if any required parameter is null or invalid
     * @throws RuntimeException if KJar creation or deployment fails
     */
    void createKieJarAndDeployToMaven(String groupId, String artifactId, String version,
            String kbaseName, String ksessionName,
            List<String> resourceFilePaths);

    /**
     * Creates a KJar with custom classes and deploys it to the local Maven repository.
     * 
     * @param groupId Maven group ID
     * @param artifactId Maven artifact ID
     * @param version Maven version
     * @param kbaseName KieBase name
     * @param ksessionName KieSession name
     * @param resourceFilePaths paths to rule files or directories
     * @param classesForKjar domain model classes to include
     * @throws IllegalArgumentException if any required parameter is null or invalid
     * @throws RuntimeException if KJar creation or deployment fails
     */
    void createKieJarAndDeployToMaven(String groupId, String artifactId, String version,
            String kbaseName, String ksessionName,
            List<String> resourceFilePaths, List<Class<?>> classesForKjar);

    /**
     * Creates a fully configured KJar with classes and dependencies, then deploys to local Maven repository.
     * <p>
     * After deployment, the KJar can be used as a Maven dependency in other projects.
     * </p>
     * 
     * @param groupId Maven group ID
     * @param artifactId Maven artifact ID
     * @param version Maven version
     * @param kbaseName KieBase name
     * @param ksessionName KieSession name
     * @param resourceFilePaths paths to rule files or directories
     * @param classesForKjar domain model classes to include
     * @param dependencies Maven dependencies in "G:A:V" format (can be null)
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws RuntimeException if KJar creation or deployment fails
     */
    void createKieJarAndDeployToMaven(String groupId, String artifactId, String version,
            String kbaseName, String ksessionName, 
            List<String> resourceFilePaths, List<Class<?>> classesForKjar, 
            List<String> dependencies);
}
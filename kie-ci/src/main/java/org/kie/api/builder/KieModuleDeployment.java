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
package org.kie.api.builder;

import org.kie.api.builder.helper.FluentKieModuleDeploymentHelper;
import org.kie.api.builder.helper.KieModuleDeploymentHelperImpl;
import org.kie.api.builder.helper.SingleKieModuleDeploymentHelper;

/**
 * Modern factory for creating KieModule deployment helpers.
 * <p>
 * This class provides a clean, intuitive API for programmatically creating and deploying
 * KJars (Knowledge JARs) to Maven repositories. It offers two distinct factory methods
 * that clearly indicate which API style you're using:
 * <ul>
 *   <li>{@link #fluent()} - For step-by-step configuration with method chaining</li>
 *   <li>{@link #single()} - For all-at-once configuration with a single method call</li>
 * </ul>
 * </p>
 * 
 * <h3>Usage Examples:</h3>
 * 
 * <h4>Fluent API:</h4>
 * <pre>{@code
 * KieModuleDeployment.fluent()
 *     .setGroupId("com.example")
 *     .setArtifactId("my-rules")
 *     .setVersion("1.0.0")
 *     .setKBaseName("myKBase")
 *     .addResourceFilePath("/rules/myRules.drl")
 *     .addClass(MyFactClass.class)
 *     .createKieJarAndDeployToMaven();
 * }</pre>
 * 
 * <h4>Single-Method API:</h4>
 * <pre>{@code
 * KieModuleDeployment.single()
 *     .createKieJarAndDeployToMaven(
 *         "com.example", "my-rules", "1.0.0",
 *         "myKBase", "mySession",
 *         resourcePaths, classes, dependencies);
 * }</pre>
 * 
 * @since 10.3.0
 * @see FluentKieModuleDeploymentHelper
 * @see SingleKieModuleDeploymentHelper
 */
public final class KieModuleDeployment {
    
    private KieModuleDeployment() {
        // Prevent instantiation - this is a utility class with only static methods
    }
    
    /**
     * Creates a new fluent-style deployment helper for step-by-step configuration.
     * <p>
     * Use this method when you want to configure the KJar creation incrementally
     * using method chaining. This approach is more readable for complex configurations
     * and allows you to build up the configuration over multiple statements.
     * </p>
     * 
     * @return a new {@link FluentKieModuleDeploymentHelper} instance
     * 
     * @see FluentKieModuleDeploymentHelper
     */
    public static FluentKieModuleDeploymentHelper fluent() {
        return new KieModuleDeploymentHelperImpl();
    }
    
    /**
     * Creates a new single-method deployment helper for all-at-once configuration.
     * <p>
     * Use this method when you have all the configuration parameters ready and want
     * to create and deploy the KJar in a single method call. This approach is more
     * concise for simple configurations.
     * </p>
     * 
     * @return a new {@link SingleKieModuleDeploymentHelper} instance
     * 
     * @see SingleKieModuleDeploymentHelper
     */
    public static SingleKieModuleDeploymentHelper single() {
        return new KieModuleDeploymentHelperImpl();
    }
}
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

import org.kie.api.builder.KieModuleDeployment;

/**
 * This class provides users with the ability to programmatically create
 * kjars and deploy them to the available maven repositories.
 * </p>
 * Both a fluent and "single-method" interface are provided.
 *
 * @deprecated Use {@link org.kie.api.builder.KieModuleDeployment} instead for a clearer, more intuitive API.
 *             The factory methods in this class will be removed in a future version.
 *             <p>
 *             Migration guide:
 *             <ul>
 *               <li>{@code KieModuleDeploymentHelper.newFluentInstance()} → {@link org.kie.api.builder.KieModuleDeployment#fluent()}</li>
 *               <li>{@code KieModuleDeploymentHelper.newSingleInstance()} → {@link org.kie.api.builder.KieModuleDeployment#single()}</li>
 *             </ul>
 *             </p>
 */
@Deprecated(since = "10.3.0", forRemoval = true)
public class KieModuleDeploymentHelper {

    /**
     * Creates a new fluent-style deployment helper.
     *
     * @return a new {@link FluentKieModuleDeploymentHelper} instance
     * @deprecated Use {@link org.kie.api.builder.KieModuleDeployment#fluent()} instead.
     *             This method will be removed in a future version.
     */
    @Deprecated(since = "10.3.0", forRemoval = true)
    public static FluentKieModuleDeploymentHelper newFluentInstance() {
        return KieModuleDeployment.fluent();
    }
    
    /**
     * Creates a new single-method deployment helper.
     *
     * @return a new {@link SingleKieModuleDeploymentHelper} instance
     * @deprecated Use {@link org.kie.api.builder.KieModuleDeployment#single()} instead.
     *             This method will be removed in a future version.
     */
    @Deprecated(since = "10.3.0", forRemoval = true)
    public static SingleKieModuleDeploymentHelper newSingleInstance() {
        return KieModuleDeployment.single();
    }
}

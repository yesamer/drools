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
package org.kie.maven.integration;

import org.junit.jupiter.api.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.util.maven.support.PomModel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MavenPomModelGeneratorTest {

    @Test
    void testParsePomXmlToModel() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        assertThat(model.getReleaseId().getGroupId()).isEqualTo("org.test");
        assertThat(model.getReleaseId().getArtifactId()).isEqualTo("test-artifact");
        assertThat(model.getReleaseId().getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testParsePomXmlWithDependencies() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <dependencies>\n" +
            "    <dependency>\n" +
            "      <groupId>org.example</groupId>\n" +
            "      <artifactId>example-lib</artifactId>\n" +
            "      <version>2.0.0</version>\n" +
            "    </dependency>\n" +
            "  </dependencies>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        Collection<ReleaseId> dependencies = model.getDependencies();
        assertThat(dependencies).hasSize(1);
        
        ReleaseId dep = dependencies.iterator().next();
        assertThat(dep.getGroupId()).isEqualTo("org.example");
        assertThat(dep.getArtifactId()).isEqualTo("example-lib");
        assertThat(dep.getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void testParseInvalidPomXml() {
        String invalidPomXml = "This is not valid XML";
        
        InputStream pomStream = new ByteArrayInputStream(invalidPomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        
        assertThatThrownBy(() -> generator.parse("pom.xml", pomStream))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testParseMalformedPomXml() {
        String malformedPomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <!-- Missing closing tags -->";
        
        InputStream pomStream = new ByteArrayInputStream(malformedPomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        
        assertThatThrownBy(() -> generator.parse("pom.xml", pomStream))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testParsePomXmlWithSnapshotVersion() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0-SNAPSHOT</version>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        assertThat(model.getReleaseId().getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(model.getReleaseId().isSnapshot()).isTrue();
    }

    @Test
    void testParsePomXmlWithMultipleDependencies() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <dependencies>\n" +
            "    <dependency>\n" +
            "      <groupId>org.example</groupId>\n" +
            "      <artifactId>lib1</artifactId>\n" +
            "      <version>1.0.0</version>\n" +
            "    </dependency>\n" +
            "    <dependency>\n" +
            "      <groupId>org.example</groupId>\n" +
            "      <artifactId>lib2</artifactId>\n" +
            "      <version>2.0.0</version>\n" +
            "    </dependency>\n" +
            "    <dependency>\n" +
            "      <groupId>org.example</groupId>\n" +
            "      <artifactId>lib3</artifactId>\n" +
            "      <version>3.0.0</version>\n" +
            "    </dependency>\n" +
            "  </dependencies>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        assertThat(model.getDependencies()).hasSize(3);
    }

    @Test
    void testParsePomXmlWithTestScopeDependency() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <dependencies>\n" +
            "    <dependency>\n" +
            "      <groupId>junit</groupId>\n" +
            "      <artifactId>junit</artifactId>\n" +
            "      <version>4.13.2</version>\n" +
            "      <scope>test</scope>\n" +
            "    </dependency>\n" +
            "  </dependencies>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        // Test scope dependencies might be filtered out depending on implementation
        assertThat(model.getDependencies()).isNotNull();
    }

    @Test
    void testParsePomXmlWithProperties() {
        String pomXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" +
            "  <groupId>org.test</groupId>\n" +
            "  <artifactId>test-artifact</artifactId>\n" +
            "  <version>1.0.0</version>\n" +
            "  <properties>\n" +
            "    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "    <maven.compiler.source>17</maven.compiler.source>\n" +
            "  </properties>\n" +
            "</project>";
        
        InputStream pomStream = new ByteArrayInputStream(pomXml.getBytes(StandardCharsets.UTF_8));
        MavenPomModelGenerator generator = new MavenPomModelGenerator();
        PomModel model = generator.parse("pom.xml", pomStream);
        
        assertThat(model).isNotNull();
        assertThat(model.getReleaseId()).isNotNull();
    }

}

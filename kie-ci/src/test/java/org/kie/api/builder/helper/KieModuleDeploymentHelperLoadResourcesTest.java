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

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kie.api.builder.helper.KieModuleDeploymentHelperImpl.KJarResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kie.api.builder.helper.KieModuleDeploymentHelperImpl.internalLoadResources;

public class KieModuleDeploymentHelperLoadResourcesTest {

    @Test
    void testLoadSingleLocalResource() throws Exception {
        String path = "/builder/simple_query_test.drl";
        List<KJarResource> resources = internalLoadResources(path, false);
        
        assertThat(resources).hasSize(1);
        String content = resources.get(0).content;
        assertThat(content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadDirectoryWithMultipleResources() throws Exception {
        String path = "/builder/test/";
        List<KJarResource> resources = internalLoadResources(path, true);
        
        assertThat(resources).hasSize(2);
        String content = resources.get(0).content;
        assertThat(content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadDirectoryNonRecursive() throws Exception {
        String path = "/builder/";
        List<KJarResource> resources = internalLoadResources(path, true);
        
        assertThat(resources).hasSize(1);
        String content = resources.get(0).content;
        assertThat(content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadClasspathResource() throws Exception {
        String path = "META-INF/WorkDefinitions.conf";
        List<KJarResource> resources = internalLoadResources(path, false);
        
        assertThat(resources).hasSize(1);
        String content = resources.get(0).content;
        assertThat(content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadClasspathDirectory() throws Exception {
        String path = "META-INF/plexus/";
        List<KJarResource> resources = internalLoadResources(path, true);
        
        assertThat(resources).hasSize(1);
        String content = resources.get(0).content;
        assertThat(content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadFileSystemResource(@TempDir Path tempDir) throws Exception {
        String content = "test file created by " + this.getClass().getSimpleName();
        Path tempFile = tempDir.resolve(UUID.randomUUID() + ".tst");
        
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        
        List<KJarResource> resources = internalLoadResources(tempFile.toString(), false);
        
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).content)
            .isNotNull()
            .contains("test file created by");
    }

    @Test
    void testLoadFileSystemDirectory(@TempDir Path tempDir) throws Exception {
        String content = "test file created by " + this.getClass().getSimpleName();
        Path tempFile = tempDir.resolve(UUID.randomUUID() + ".tst");
        
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        
        List<KJarResource> resources = internalLoadResources(tempDir.toString(), true);
        
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).content)
            .isNotNull()
            .hasSizeGreaterThan(10);
    }

    @Test
    void testLoadNonExistentResource() {
        String nonExistentPath = "/nonexistent/path/to/resource.drl";
        
        assertThatThrownBy(() -> internalLoadResources(nonExistentPath, false))
            .isInstanceOf(FileNotFoundException.class)
            .hasMessageContaining("nonexistent");
    }

    @Test
    void testLoadEmptyDirectory(@TempDir Path tempDir) throws Exception {
        List<KJarResource> resources = internalLoadResources(tempDir.toString(), true);
        
        assertThat(resources).isEmpty();
    }

    @Test
    void testLoadMultipleFilesFromDirectory(@TempDir Path tempDir) throws Exception {
        // Create multiple test files
        Files.writeString(tempDir.resolve("file1.drl"), "rule 'test1' when then end", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("file2.drl"), "rule 'test2' when then end", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("file3.txt"), "some text content", StandardCharsets.UTF_8);
        
        List<KJarResource> resources = internalLoadResources(tempDir.toString(), true);
        
        assertThat(resources).hasSize(3);
        assertThat(resources).allMatch(r -> r.content != null && !r.content.isEmpty());
    }

    @Test
    void testLoadResourceWithSpecialCharacters(@TempDir Path tempDir) throws Exception {
        String content = "Content with special chars: äöü ñ 中文 🎉";
        Path tempFile = tempDir.resolve("special-chars.txt");
        
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        
        List<KJarResource> resources = internalLoadResources(tempFile.toString(), false);
        
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).content).contains("äöü", "ñ", "中文");
    }

    @Test
    void testLoadLargeFile(@TempDir Path tempDir) throws Exception {
        String largeContent = "x".repeat(10000); // 10KB file
        Path tempFile = tempDir.resolve("large-file.txt");
        
        Files.writeString(tempFile, largeContent, StandardCharsets.UTF_8);
        
        List<KJarResource> resources = internalLoadResources(tempFile.toString(), false);
        
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).content).hasSize(10000);
    }

    @Test
    void testLoadResourcePathNormalization(@TempDir Path tempDir) throws Exception {
        String content = "test content";
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectories(subDir);
        Path tempFile = subDir.resolve("test.txt");
        
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        
        // Test with different path formats
        String pathWithSlash = tempDir + "/subdir/test.txt";
        List<KJarResource> resources = internalLoadResources(pathWithSlash, false);
        
        assertThat(resources).hasSize(1);
        assertThat(resources.get(0).content).isEqualTo(content);
    }
}

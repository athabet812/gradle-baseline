/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.baseline

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

class BaselineCheckstyleIntegrationTest extends AbstractPluginTest {

    def setup() {
        FileUtils.copyDirectory(
                new File("../gradle-baseline-java-config/resources"),
                new File(projectDir, ".baseline"))
    }

    def standardBuildFile = '''
        plugins {
            id 'java'
            id 'com.palantir.baseline-checkstyle'
        }
        repositories {
            // to resolve the `checkstyle` configuration
            mavenCentral()
        }
    '''.stripIndent()

    def exampleJavaFile = '''
    package example;
    
    public class Example {
    }
    '''.stripIndent()

    def 'checkstyleMain succeeds'() {
        when:
        buildFile << standardBuildFile
        file('src/main/java/example/Example.java') << exampleJavaFile

        then:
        BuildResult result = with('checkstyleMain').build()
        result.task(":checkstyleMain").outcome == TaskOutcome.SUCCESS
    }
    
    def badJavaFile = '''
    package blacklist;
    
    public class Blacklist {
    }
    '''.stripIndent()
    
    def 'inclusive code linter enabled by default'() {
        when:
        buildFile << standardBuildFile
        file('src/main/java/example/Blacklist.java') << badJavaFile
        
        then:
        BuildResult result = with('checkstyleMain').build()
        result.task(":checkstyleMain").outcome == TaskOutcome.SUCCESS
    }
}

/*
 * Copyright 2021 depresolve project
 * 
 * Website: https://github.com/lambdaprime/depresolve
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
package id.depresolve.tests.integration;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

import id.depresolve.Depresolve;
import id.xfunction.ResourceUtils;
import id.xfunction.lang.XExec;
import id.xfunction.nio.file.XFiles;
import id.xfunction.text.WildcardMatcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DepresolveAppTests {

    private static final ResourceUtils resourceUtil = new ResourceUtils();

    private static final String APP_PATH =
            Paths.get("").toAbsolutePath().resolve("build/depresolve/depresolve").toString();

    private static class CommandOutput {
        private String stdout;
        private String stderr;

        public CommandOutput(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public String getStderr() {
            return stderr;
        }

        public String getStdout() {
            return stdout;
        }

        public String getCombined() {
            return stdout + "\n" + stderr + "\n";
        }

        @Override
        public String toString() {
            return getCombined();
        }
    }

    @BeforeEach
    void setup() throws IOException {}

    @AfterEach
    void cleanup() throws IOException {}

    @Test
    public void test_download() {
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar",
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-impl/1.6.2/maven-resolver-impl-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-spi/1.6.2/maven-resolver-spi-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-util/1.6.2/maven-resolver-util-1.6.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        removeFiles(repoHome, files);
        var args = String.format("org.apache.maven.resolver:maven-resolver-impl:1.6.2");
        var out = runOk(args);
        Assertions.assertEquals(
                true,
                new WildcardMatcher(resourceUtil.readResource("test_download_to_custom_repo"))
                        .matches(out.getCombined()));
        assertFilesExist(repoHome, files);
    }

    @Test
    public void test_download_to_custom_repo() throws Exception {
        var repoHome = Files.createTempDirectory("depresolver-repo");
        System.out.println("Temp repo is: " + repoHome);
        var files =
                List.of(
                        "org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar",
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-impl/1.6.2/maven-resolver-impl-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-spi/1.6.2/maven-resolver-spi-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-util/1.6.2/maven-resolver-util-1.6.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        var args =
                String.format(
                        "--repo-home %s org.apache.maven.resolver:maven-resolver-impl:1.6.2",
                        repoHome.toString());
        var out = runOk(args);
        Assertions.assertEquals(
                true,
                new WildcardMatcher(resourceUtil.readResource("test_download_to_custom_repo"))
                        .matches(out.getCombined()));
        assertFilesExist(repoHome, files);
    }

    @Test
    public void test_download_multiple() {
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        removeFiles(repoHome, files);
        var args =
                String.format(
                        "org.apache.maven.resolver:maven-resolver-api:1.6.2"
                                + " org.slf4j:slf4j-api:1.7.30");
        runOk(args);
        assertFilesExist(repoHome, files);
    }

    @Test
    public void test_different_scopes() throws IOException {
        var repoHome = Files.createTempDirectory("depresolver-repo");
        System.out.println("Temp repo is: " + repoHome);
        var files =
                List.of(
                        "junit/junit/4.13/junit-4.13.jar",
                        "org/hamcrest/hamcrest-core/2.2/hamcrest-core-2.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        var args =
                String.format(
                        "--repo-home %s --scope test"
                            + " org.apache.maven.resolver:maven-resolver-api:1.6.2 --scope compile"
                            + " org.slf4j:slf4j-api:1.7.30",
                        repoHome.toString());
        runOk(args);
        assertFilesExist(repoHome, files);
    }

    @Test
    public void test_download_once() throws IOException {
        var repoHome = Files.createTempDirectory("depresolver-repo");
        var args =
                String.format(
                        "--repo-home %s org.apache.maven.resolver:maven-resolver-api:1.6.2",
                        repoHome.toString());
        runOk(args);
        var out = runOk(args);
        Assertions.assertEquals(false, out.getCombined().contains("Downloading"));
    }

    @Test
    public void test_download_cp() {
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "org/apache/commons/commons-lang3/3.8.1/commons-lang3-3.8.1.jar",
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-impl/1.6.2/maven-resolver-impl-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-spi/1.6.2/maven-resolver-spi-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-util/1.6.2/maven-resolver-util-1.6.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        var expected =
                files.stream().map(f -> repoHome.resolve(f).toString()).collect(joining(":"))
                        + "\n\n";
        removeFiles(repoHome, files);
        var args = String.format("-cp org.apache.maven.resolver:maven-resolver-impl:1.6.2");
        var out = runOk(args);
        Assertions.assertEquals(expected, out.getCombined());
    }

    @Test
    public void test_cp_resolved_to_latest_versions() {
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "io/github/lambdaprime/id.xfunction/26.0/id.xfunction-26.0.jar",
                        "io/github/lambdaprime/jros2client/8.0/jros2client-8.0.jar",
                        "io/github/lambdaprime/jros2messages/8.0/jros2messages-8.0.jar",
                        "io/github/lambdaprime/jrosclient/9.0/jrosclient-9.0.jar",
                        "io/github/lambdaprime/jrosmessages/9.0/jrosmessages-9.0.jar",
                        "io/github/lambdaprime/kineticstreamer/8.0/kineticstreamer-8.0.jar",
                        "io/github/pinorobotics/jros2actionlib/3.0/jros2actionlib-3.0.jar",
                        "io/github/pinorobotics/jros2moveit/1.0/jros2moveit-1.0.jar",
                        "io/github/pinorobotics/jros2services/5.0/jros2services-5.0.jar",
                        "io/github/pinorobotics/jrosactionlib/3.0/jrosactionlib-3.0.jar",
                        "io/github/pinorobotics/jrosmoveit/2.0/jrosmoveit-2.0.jar",
                        "io/github/pinorobotics/jrosservices/3.0/jrosservices-3.0.jar",
                        "io/github/pinorobotics/rtpstalk/8.0/rtpstalk-8.0.jar",
                        "io/opentelemetry/opentelemetry-api/1.34.1/opentelemetry-api-1.34.1.jar",
                        "io/opentelemetry/opentelemetry-context/1.34.1/opentelemetry-context-1.34.1.jar");
        var expected =
                files.stream().map(f -> repoHome.resolve(f).toString()).collect(joining(":"))
                        + "\n\n";
        removeFiles(repoHome, files);
        var args = String.format("-cp io.github.pinorobotics:jros2moveit:1.0");
        var out = runOk(args);
        Assertions.assertEquals(expected, out.getCombined());
    }

    @Test
    public void test_download_cp_multiple() {
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        var expected =
                files.stream().map(f -> repoHome.resolve(f).toString()).collect(joining(":"))
                        + "\n\n";
        removeFiles(repoHome, files);
        var args =
                String.format(
                        "-cp org.apache.maven.resolver:maven-resolver-api:1.6.2"
                                + " org.slf4j:slf4j-api:1.7.30");
        var out = runOk(args);
        Assertions.assertEquals(expected, out.getCombined());
    }

    @Test
    public void test_no_args() throws Exception {
        var out = runFail("");
        Assertions.assertEquals(
                Files.readString(Paths.get("../README.md")) + "\n", out.getCombined());
    }

    @Test
    public void test_output() throws IOException {
        var outputDir = Files.createTempDirectory("depresolce-output");
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "commons-lang3-3.8.1.jar",
                        "maven-resolver-api-1.6.2.jar",
                        "maven-resolver-impl-1.6.2.jar",
                        "maven-resolver-spi-1.6.2.jar",
                        "maven-resolver-util-1.6.2.jar",
                        "slf4j-api-1.7.30.jar");
        removeFiles(repoHome, files);
        var args =
                String.format(
                        "--output %s org.apache.maven.resolver:maven-resolver-impl:1.6.2",
                        outputDir);
        runOk(args);
        assertFilesExist(outputDir, files);
    }

    @Test
    public void test_output_links() throws IOException {
        var outputDir = Files.createTempDirectory("depresolce-output");
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "commons-lang3-3.8.1.jar",
                        "maven-resolver-api-1.6.2.jar",
                        "maven-resolver-impl-1.6.2.jar",
                        "maven-resolver-spi-1.6.2.jar",
                        "maven-resolver-util-1.6.2.jar",
                        "slf4j-api-1.7.30.jar");
        removeFiles(repoHome, files);
        var args =
                String.format(
                        "--output-links %s org.apache.maven.resolver:maven-resolver-impl:1.6.2",
                        outputDir);
        runOk(args);
        assertFilesExist(outputDir, files);
        assertFilesAreLinks(outputDir, files);
    }

    @Test
    public void test_output_links_exists() throws IOException {
        var outputDir = Files.createTempDirectory("depresolce-output");
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var files =
                List.of(
                        "commons-lang3-3.8.1.jar",
                        "maven-resolver-api-1.6.2.jar",
                        "maven-resolver-impl-1.6.2.jar",
                        "maven-resolver-spi-1.6.2.jar",
                        "maven-resolver-util-1.6.2.jar",
                        "slf4j-api-1.7.30.jar");
        removeFiles(repoHome, files);
        var args =
                String.format(
                        "--output-links %s org.apache.maven.resolver:maven-resolver-impl:1.6.2",
                        outputDir);
        runOk(args);
        runOk(args);
        assertFilesExist(outputDir, files);
        assertFilesAreLinks(outputDir, files);
    }

    @Test
    public void test_ignore_not_found() throws IOException {
        var args = String.format("-cp org.apache.maven.resolver:maven-resolver-impl:1.6.2");
        runOk(args);
        var files =
                List.of(
                        "org/apache/maven/resolver/maven-resolver-api/1.6.2/maven-resolver-api-1.6.2.jar",
                        "org/apache/maven/resolver/maven-resolver-impl/1.6.2/maven-resolver-impl-1.6.2.jar");
        var repoHome = new Depresolve().findLocalRepositoryHome();
        var expected =
                files.stream().map(f -> repoHome.resolve(f).toString()).collect(joining(":"));
        var pomFile =
                repoHome.resolve(
                        "org/apache/maven/resolver/maven-resolver-impl/1.6.2/maven-resolver-impl-1.6.2.pom");
        Files.writeString(pomFile, resourceUtil.readResource("maven-resolver-impl-1.6.2.pom"));
        var actual = runOk(args);
        XFiles.deleteRecursively(pomFile.getParent());
        assertEquals(true, actual.getCombined().startsWith(expected));
    }

    @Test
    public void test_exec() throws Exception {
        var out = runOk("--exec printenv org.apache.maven.resolver:maven-resolver-impl:1.6.2");
        var classpath =
                out.getCombined().lines().filter(s -> s.startsWith("CLASSPATH")).findFirst().get();
        Assertions.assertEquals(
                true,
                Stream.of(
                                "commons-lang3-3.8.1.jar",
                                "maven-resolver-api-1.6.2.jar",
                                "maven-resolver-impl-1.6.2.jar",
                                "maven-resolver-spi-1.6.2.jar",
                                "maven-resolver-util-1.6.2.jar",
                                "slf4j-api-1.7.30.jar")
                        .allMatch(file -> classpath.contains(file)));
    }

    private CommandOutput runFail(String fmt, Object... args) {
        return run(1, fmt, args);
    }

    private CommandOutput runOk(String fmt, Object... args) {
        return run(0, fmt, args);
    }

    private CommandOutput run(int expectedCode, String fmt, Object... args) {
        var proc = new XExec(APP_PATH + " " + String.format(fmt, args)).start();
        var code = proc.await();
        var out = new CommandOutput(proc.stdout(), proc.stderr());
        System.out.println("Output:");
        System.out.println(">>>");
        System.out.print(out);
        System.out.println("<<<");
        Assertions.assertEquals(expectedCode, code);
        return out;
    }

    private boolean fileExist(Path filePath, String... subFilePath) {
        return Paths.get(filePath.toString(), subFilePath).toFile().exists();
    }

    private boolean linkExist(Path filePath, String... subFilePath) {
        return Files.isSymbolicLink(Paths.get(filePath.toString(), subFilePath));
    }

    private void removeFiles(Path parentDir, List<String> files) {
        for (var file : files) Paths.get(parentDir.toString(), file).toFile().delete();
    }

    private void assertFilesExist(Path parentDir, List<String> files) {
        for (var file : files) Assertions.assertEquals(true, fileExist(parentDir, file));
    }

    private void assertFilesAreLinks(Path parentDir, List<String> files) {
        for (var file : files) Assertions.assertEquals(true, linkExist(parentDir, file));
    }
}

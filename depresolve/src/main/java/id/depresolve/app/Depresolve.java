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
/*
 * Authors:
 * - lambdaprime <intid@protonmail.com>
 */
package id.depresolve.app;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.maven.resolver.examples.util.Booter;

import id.depresolve.ArtifactInfo;
import id.depresolve.utils.MavenClasspathResolver;
import id.depresolve.utils.RepositoryUtills;
import id.xfunction.function.Unchecked;
import id.xfunction.io.DevNullOutputStream;

public class Depresolve {

    private boolean generateClasspath;
    private Optional<Path> repositoryHome = Optional.empty();
    private Optional<Path> outputDir = Optional.empty();
    private boolean useLinks = false;
    private List<ArtifactInfo> artifacts = new ArrayList<>();
    private Optional<List<File>> classpathOutput = Optional.empty();

    public Depresolve withGenerateClasspath() {
        generateClasspath = true;
        return this;
    }
    
    public Depresolve withGenerateClasspath(List<File> output) {
        generateClasspath = true;
        classpathOutput = Optional.of(output);
        return this;
    }

    public Depresolve withOutputDir(Path dir) {
        outputDir = Optional.of(dir);
        return this;
    }

    public Depresolve withUseLinks() {
        useLinks = true;
        return this;
    }

    public Depresolve addArtifactToResolve(ArtifactInfo artifact) {
        artifacts.add(artifact);
        return this;
    }
    
    public Depresolve withRepositoryHome(Path dir) {
        repositoryHome = Optional.of(dir);
        return this;
    }

    public List<ArtifactInfo> getArtifacts() {
        return artifacts;
    }
    
    // visible for testing
    public Path findLocalRepositoryHome() {
        var userHome = System.getProperty("user.home");
        if (userHome == null)
            throw new RuntimeException("Cannot find Maven local repository default location");
        return Paths.get(userHome, ".m2", "repository");
    }
    
    public void run() throws Exception {
        var output = System.out;
        if (generateClasspath) {
            output = new PrintStream(new DevNullOutputStream());
        }
        var repoHome = repositoryHome.orElse(findLocalRepositoryHome());
        var system = Booter.newRepositorySystem();
        var session = RepositoryUtills.newRepositorySystemSession(system, repoHome, output);

        var resolver = new MavenClasspathResolver(system, session);
        for (var artifact: artifacts) {
            resolver.resolve(artifact.getName(), artifact.getScope());
        }
        if (generateClasspath) {
            if (classpathOutput.isEmpty()) {
                System.out.println(resolver);
            } else {
                classpathOutput.get().addAll(resolver.getAllResolvedFiles());
            }
        }
        if (outputDir.isPresent()) {
            var dst = outputDir.get();
            if (Files.notExists(dst)) {
                Files.createDirectory(dst);
            }
            resolver.getAllResolvedFiles().forEach(file -> {
                Unchecked.run(() -> {
                    var src = file.toPath();
                    var target = dst.resolve(file.getName());
                    if (useLinks)
                        Files.createSymbolicLink(target, src);
                    else
                        Files.copy(src, target);
                });
            });
        }
    }

}

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
package id.depresolve;

import id.depresolve.utils.MavenClasspathResolver;
import id.depresolve.utils.RepositoryUtills;
import id.xfunction.Preconditions;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.maven.resolver.examples.util.Booter;

/**
 * Resolve Maven dependencies.
 *
 * <p>{@link Depresolve} main flow looks like this:
 *
 * <ul>
 *   <li>Given full name of Maven artifact {@link Depresolve} checks if it is present in local Maven
 *       repository:
 *       <ul>
 *         <li>if it is there, {@link Depresolve} returns complete file path to this artifact
 *         <li>if it is not, then {@link Depresolve} downloads it using <a
 *             href="https://github.com/apache/maven-resolver">maven-resolver</a> which effectively
 *             downloads artifact and stores it in local Maven repository same way as during
 *             execution of "mvn" command
 *       </ul>
 * </ul>
 *
 * <p>If artifact has transient dependencies this flow repeats recursively for all of them.
 *
 * <p>To use {@link Depresolve} class you first configure it using "with" methods and tell what
 * actions you want it to perform and then call {@link #run()}.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class Depresolve {

    private Optional<Path> repositoryHome = Optional.empty();
    private Optional<Path> outputDir = Optional.empty();
    private boolean useLinks = false;
    private List<ArtifactInfo> artifacts = new ArrayList<>();
    private Optional<Consumer<File>> classpathConsumer = Optional.empty();
    private boolean silentMode;

    /**
     * Do not print any additional output.
     *
     * <p>Examples of such output include download progress from <a
     * href="https://github.com/apache/maven-resolver">maven-resolver</a> when it downloads missing
     * artifacts to local Maven repository.
     */
    public Depresolve withSilentMode() {
        silentMode = true;
        return this;
    }

    /**
     * Consume full file-system path to artifacts (inside local Maven repository) which will be
     * resolved by {@link Depresolve}.
     */
    public Depresolve withClasspathConsumer(Consumer<File> classpathConsumer) {
        this.classpathConsumer = Optional.of(classpathConsumer);
        return this;
    }

    /**
     * Copy all artifacts including their transient dependencies to the given folder.
     *
     * <p>This is a simple alternative for manually setting up a consumer with {@link
     * #withClasspathConsumer(Consumer)} and copying resolved artifacts inside local Maven
     * repository to the additional folder.
     */
    public Depresolve withOutputDir(Path dir) {
        outputDir = Optional.of(dir);
        return this;
    }

    /**
     * Used together with {@link #withOutputDir(Path)} but instead of copying artifacts from local
     * Maven repository to the target folder it will create a symbolic links
     */
    public Depresolve withUseLinks() {
        Preconditions.isTrue(outputDir.isPresent(), "Use links requires output folder to be set");
        useLinks = true;
        return this;
    }

    /** Tells {@link Depresolve} which artifacts to resolve. */
    public Depresolve addArtifactToResolve(ArtifactInfo artifact) {
        artifacts.add(artifact);
        return this;
    }

    /** Set path to local Maven Repository. Default is ~/.m2/repository */
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

    /** Run {@link Depresolve} */
    public void run() throws Exception {
        var output = System.out;
        if (silentMode) {
            output = new PrintStream(OutputStream.nullOutputStream());
        } else {
            output = System.err;
        }
        var repoHome = repositoryHome.orElse(findLocalRepositoryHome());
        var system = Booter.newRepositorySystem();
        var session = RepositoryUtills.newRepositorySystemSession(system, repoHome, output);

        var resolver = new MavenClasspathResolver(system, session);
        for (var artifact : artifacts) {
            resolver.resolve(artifact.getName(), artifact.getScope());
        }
        resolver.dropOldVersions();
        if (classpathConsumer.isEmpty()) {
            resolver.getAllResolvedFiles().forEach(System.out::println);
        } else {
            Consumer<File> consumer = classpathConsumer.get();
            resolver.getAllResolvedFiles().forEach(consumer);
        }
        if (outputDir.isPresent()) {
            var dst = outputDir.get();
            if (Files.notExists(dst)) {
                Files.createDirectory(dst);
            }
            resolver.getAllResolvedFiles()
                    .forEach(
                            file -> {
                                try {
                                    var src = file.toPath();
                                    var target = dst.resolve(file.getName());
                                    if (useLinks) Files.createSymbolicLink(target, src);
                                    else Files.copy(src, target);
                                } catch (FileAlreadyExistsException e) {
                                    System.out.format(
                                            "File already exist %s, skipping...\n", e.getFile());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
        }
    }
}

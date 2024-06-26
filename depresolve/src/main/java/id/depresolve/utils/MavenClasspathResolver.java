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
package id.depresolve.utils;

import static java.util.stream.Collectors.toList;

import id.depresolve.Scope;
import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.resolver.examples.util.Booter;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class MavenClasspathResolver {

    private String scopeName;
    private RepositorySystem system;
    private RepositorySystemSession session;
    private List<RemoteRepository> repos = Booter.newRepositories(system, session);
    private Set<Artifact> classpath = new HashSet<>();

    public MavenClasspathResolver(RepositorySystem system, RepositorySystemSession session) {
        this.system = system;
        this.session = session;
    }

    public void resolve(String fullArtifactId, Scope scope) throws Exception {
        scopeName = scope.toString().toLowerCase();
        var artifact = new DefaultArtifact(fullArtifactId);
        classpath.add(resolvePath(artifact));
        resolveDependencyTree(artifact);
    }

    private void resolveDependencyTree(Artifact artifact) throws Exception {
        var descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.setRepositories(repos);

        var descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
        for (var dependency : descriptorResult.getDependencies()) {
            if (!Objects.equals(scopeName, dependency.getScope())) {
                continue;
            }
            Artifact depArtifact = dependency.getArtifact();
            try {
                var depFile = resolvePath(depArtifact);
                if (classpath.contains(depFile)) continue;
                classpath.add(depFile);
                resolveDependencyTree(depArtifact);
            } catch (ArtifactResolutionException e) {
                // keep going trying to resolve what is left
                e.printStackTrace();
            }
        }
    }

    public void dropOldVersions() {
        classpath =
                classpath.stream()
                        .collect(Collectors.groupingBy(a -> a.getArtifactId()))
                        .values()
                        .stream()
                        .map(this::selectLatestVersion)
                        .collect(Collectors.toSet());
    }

    public List<File> getAllResolvedFiles() {
        return classpath.stream().map(Artifact::getFile).sorted().collect(toList());
    }

    @Override
    public String toString() {
        return classpath.stream()
                .map(Artifact::getFile)
                .map(File::toString)
                .sorted()
                .collect(Collectors.joining(System.getProperty("path.separator", ":")));
    }

    private Artifact resolvePath(Artifact artifact) throws ArtifactResolutionException {
        var artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(repos);
        return system.resolveArtifact(session, artifactRequest).getArtifact();
    }

    private Artifact selectLatestVersion(List<Artifact> artifacts) {
        return artifacts.stream()
                .sorted(
                        Comparator.comparing((Artifact a) -> new ComparableVersion(a.getVersion()))
                                .reversed())
                .findFirst()
                .get();
    }
}

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

import java.io.PrintStream;
import java.nio.file.Path;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.resolver.examples.util.ConsoleRepositoryListener;
import org.apache.maven.resolver.examples.util.ConsoleTransferListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class RepositoryUtills {

    public static RepositorySystemSession newRepositorySystemSession(
            RepositorySystem system, Path repositoryHome, PrintStream out) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(repositoryHome.toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new ConsoleTransferListener(out));
        session.setRepositoryListener(new ConsoleRepositoryListener(out));

        return session;
    }
}

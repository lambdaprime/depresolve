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
package id.depresolve.app;

import id.depresolve.ArtifactInfo;
import id.depresolve.Depresolve;
import id.depresolve.DepresolveUtils;
import id.depresolve.Scope;
import id.xfunction.ResourceUtils;
import id.xfunction.cli.ArgumentParsingException;
import id.xfunction.cli.SmartArgs;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implements access to <b>depresolve</b> functionality from command-line.
 *
 * @author lambdaprime intid@protonmail.com
 */
public class Main {

    private static DepresolveUtils utils = new DepresolveUtils();

    private static void usage() {
        new ResourceUtils()
                .readResourceAsStream("README-depresolve.md")
                .forEach(System.out::println);
    }

    private static void run(String[] args) throws Exception {
        var resolver = new Depresolve();
        Map<String, Consumer<String>> handlers =
                Map.of(
                        "--output",
                        dir -> {
                            resolver.withOutputDir(Paths.get(dir));
                        });
        LinkedList<String> positionalArgs = new LinkedList<>();
        new SmartArgs(handlers, positionalArgs::add).parse(args);
        if (positionalArgs.isEmpty()) throw new ArgumentParsingException("No arguments provided");
        var scope = Scope.COMPILE;
        List<File> classpath = new ArrayList<>();
        while (!positionalArgs.isEmpty()) {
            switch (positionalArgs.peek()) {
                case "-cp":
                case "-classpath":
                    positionalArgs.remove();
                    resolver.withClasspathConsumer(classpath::add);
                    resolver.withSilentMode();
                    break;
                case "--repo-home":
                    positionalArgs.remove();
                    resolver.withRepositoryHome(Paths.get(positionalArgs.remove()));
                    break;
                case "--scope":
                    positionalArgs.remove();
                    scope = Scope.valueOf(positionalArgs.remove().toUpperCase());
                    break;
                case "--output":
                    positionalArgs.remove();
                    resolver.withOutputDir(Paths.get(positionalArgs.remove()));
                    break;
                case "--output-links":
                    positionalArgs.remove();
                    resolver.withOutputDir(Paths.get(positionalArgs.remove())).withUseLinks();
                    break;
                default:
                    resolver.addArtifactToResolve(new ArtifactInfo(positionalArgs.remove(), scope));
            }
        }
        if (resolver.getArtifacts().isEmpty())
            throw new ArgumentParsingException("No artifacts to resolve");
        resolver.run();
        if (!classpath.isEmpty()) System.out.println(utils.toClasspathString(classpath));
    }

    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } catch (ArgumentParsingException e) {
            usage();
            System.exit(1);
        }
    }
}

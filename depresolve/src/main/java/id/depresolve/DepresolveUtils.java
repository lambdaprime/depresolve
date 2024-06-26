/*
 * Copyright 2023 depresolve project
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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lambdaprime intid@protonmail.com
 */
public class DepresolveUtils {

    public String toClasspathString(List<File> classpath) {
        return classpath.stream()
                .map(file -> file.isDirectory() ? new File(file, "*") : file)
                .map(File::toString)
                .sorted()
                .collect(Collectors.joining(System.getProperty("path.separator", ":")));
    }
}

**depresolve** - command line tool which helps to manage Java dependencies (from Maven repository). It allows:

- downloading all Maven dependencies to a directory
- building a classpath string for Maven artifact(s) with all its dependencies (useful in Java scripting)

# Usage

```bash
depresolve [ -cp | -classpath ] [ --repo-home <REPO_FOLDER> ] [ --output|--output-links <OUTPUT_FOLDER> ] [--scope <test|compile> ] <ARTIFACT_NAME> [ ... [--scope <test|compile> ] <ARTIFACT_NAME> ]
```

By default **depresolve** prints resolved classpath to ARTIFACT_NAME including all its dependencies.

REPO_FOLDER - Maven repository location

OUTPUT_FOLDER - output folder where to place all artifacts (or symbolic links to them)

ARTIFACT_NAME - full name of the artifact in the format:

``` 
<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
```

Please note: when using `classifier` the `extension` IS REQUIRED.

Example: "org.apache.maven.resolver:maven-resolver-impl:1.6.2"

With classifier "com.badlogicgames.gdx:gdx-platform:jar:natives-desktop:1.9.14", here we specify "jar" extension.

# Documentation

http://portal2.atwebpages.com/depresolve/

# Download

[Release versions](https://github.com/lambdaprime/depresolve/releases)

# Details

Based on [maven-resolver](https://github.com/apache/maven-resolver) library

# Contributors

lambdaprime <intid@protonmail.com>

**depresolve** - command line tool which helps to manage Java dependencies (from Maven repository).

Usage

```bash
depresolve [ -cp | -classpath ] [ --repo-home <REPO_FOLDER> ] [ --output <OUTPUT_FOLDER> ] [--scope <test|compile> ] <ARTIFACT_NAME> [ ... [--scope <test|compile> ] <ARTIFACT_NAME> ]
```

Where: 

REPO_FOLDER - Maven repository location

OUTPUT_FOLDER - output folder where to place all artifacts

ARTIFACT_NAME - full name of the artifact. Example: "org.apache.maven.resolver:maven-resolver-impl:1.6.2"

# Documentation

http://portal2.atwebpages.com/depresolve/

# Download

You can download **depresolve** from <https://github.com/lambdaprime/depresolve/releases>

# Details

Based on maven-resolver library https://github.com/apache/maven-resolver

lambdaprime <intid@protonmail.com>


**depresolve** - helps to manage Java dependencies (from Maven repository). It allows:

- downloading all Maven dependencies to a directory
- building a classpath string for Maven artifact(s) including their transient dependencies (useful in Java scripting)

**depresolve** can be used from the command line (through **depresolve** command) or directly from code as Java library.

# Usage

```bash
depresolve [ -cp | -classpath ] [ --repo-home <REPO_FOLDER> ] [ --output|--output-links <OUTPUT_FOLDER> ] [--scope <test|compile> ] <ARTIFACT_NAME> [ ... [--scope <test|compile> ] <ARTIFACT_NAME> ]
```

# Documentation

[Documentation](http://portal2.atwebpages.com/depresolve/)

# Download

[Release versions](https://github.com/lambdaprime/depresolve/releases)

# Details

Based on [maven-resolver](https://github.com/apache/maven-resolver) library

# Contributors

lambdaprime <intid@protonmail.com>

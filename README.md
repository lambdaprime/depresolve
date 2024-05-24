**depresolve** - command line tool which helps to manage Java dependencies in your local Maven repository.

You can think of **depresolve** as something similar to "apt" command for Debian packages (or "pip" command for Python packages).

It allows:

- downloading all Maven dependencies to a directory
- building a classpath string for Maven artifact(s) including their transient dependencies (useful in Java scripting)

**depresolve** can be used from the command line (through **depresolve** command) or directly from code as Java library.

# Usage

```bash
depresolve [ -cp | -classpath ] [ --repo-home <REPO_FOLDER> ] [ --output|--output-links <OUTPUT_FOLDER> ] [--scope <test|compile> ] <ARTIFACT_NAME> [ ... [--scope <test|compile> ] <ARTIFACT_NAME> ]
```

# Documentation

[Documentation](http://portal2.atwebpages.com/depresolve/)

[Development](DEVELOPMENT.md)

# Contacts

lambdaprime <intid@protonmail.com>

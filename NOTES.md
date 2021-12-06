# Modularity issues

Major issue is:

Packages with same name org.apache.maven.model.merge in:
- maven-model-builder:3.8.4
- maven-model:3.8.4 libraries:

Package with same name org.apache.maven.artifact.repository.metadata in
- maven.repository.metadata
- maven.artifact

Minor issue which can be fixed by placing to module path:

maven-resolver-provider:3.8.4 is not automatic module
module not found: maven.resolver.provider
    requires maven.resolver.provider;  

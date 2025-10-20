package io.github.leonesoj.honey;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class HoneyLoader implements PluginLoader {

  @Override
  public void classloader(PluginClasspathBuilder classpathBuilder) {

    MavenLibraryResolver resolver = new MavenLibraryResolver();
    resolver.addDependency(new Dependency(
        new DefaultArtifact("com.zaxxer:HikariCP:6.3.0"),
        null
    ));
    resolver.addDependency(new Dependency(
        new DefaultArtifact("io.lettuce:lettuce-core:6.5.5.RELEASE"),
        null
    ));
    resolver.addDependency(new Dependency(
        new DefaultArtifact("com.fasterxml.jackson.core:jackson-databind:2.17.0"),
        null
    ));
    resolver.addDependency(new Dependency(
        new DefaultArtifact("com.github.ben-manes.caffeine:caffeine:3.2.0"),
        null
    ));
    resolver.addRepository(new RemoteRepository.Builder(
        "paper",
        "default",
        "https://repo.papermc.io/repository/maven-public/")
        .build()
    );

    classpathBuilder.addLibrary(resolver);
  }
}

package org.jboss.pnc.konfluxtooling.deploy;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;
import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "deploy")
public class DeployCommand implements Runnable {

    @CommandLine.Option(names = "--directory", required = true)
    String artifactDirectory;

    // Maven Repo Deployment specification
    @CommandLine.Option(names = "--mvn-username")
    String mvnUser;

    @ConfigProperty(name = "access.token")
    Optional<String> accessToken;

    @ConfigProperty(name = "maven.password")
    Optional<String> mvnPassword;

    @CommandLine.Option(names = "--mvn-settings")
    String mvnSettings;

    @CommandLine.Option(names = "--mvn-repo", required = true)
    String mvnRepo;

    @CommandLine.Option(names = "--server-id")
    String serverId = "indy-mvn";

    @Inject
    BootstrapMavenContext mvnCtx;

    public void run() {
        try {
            var deploymentPath = Path.of(artifactDirectory);

            if (!deploymentPath.toFile().exists()) {
                Log.warnf("No deployed artifacts found. Has the build been correctly configured to deploy?");
                throw new RuntimeException("Deploy failed");
            }
            if (isNotEmpty(mvnSettings)) {
                if (!Path.of(mvnSettings).toFile().exists()) {
                    Log.errorf("Invalid Maven settings path: %s", mvnSettings);
                    throw new RuntimeException("Invalid Maven settings");
                }
                System.setProperty("maven.settings", mvnSettings);
            }
            if (accessToken.isPresent()) {
                String servers = """
                        <settings>
                            <!--
                                Needed for Maven 3.9+. Switched to native resolver
                                https://maven.apache.org/guides/mini/guide-resolver-transport.html
                            -->
                            <servers>
                                <server>
                                    <id>indy-mvn</id>
                                    <configuration>
                                        <connectionTimeout>60000</connectionTimeout>
                                        <httpHeaders>
                                            <property>
                                                <name>Authorization</name>
                                                <value>Bearer ${ACCESS_TOKEN}</value>
                                            </property>
                                        </httpHeaders>
                                    </configuration>
                                </server>
                            </servers>
                        </settings>
                        """;
                if (isNotEmpty(mvnSettings)) {
                    // TODO: Would need to merge the two files. NYI for now as I don't think we need this pattern
                    throw new RuntimeException("Merging settings.xml not supported");
                } else {
                    Path settings = Path.of(deploymentPath.getParent().toString(), "settings.xml");
                    Files.write(settings, servers.getBytes());
                    System.setProperty("maven.settings", settings.toString());
                }
            }
            // Maven Repo Deployment
            MavenRepositoryDeployer deployer = new MavenRepositoryDeployer(
                    mvnCtx,
                    mvnUser,
                    mvnPassword.orElse(""),
                    mvnRepo,
                    serverId,
                    deploymentPath);
            deployer.deploy();
        } catch (Exception e) {
            Log.error("Deployment failed", e);
            throw new RuntimeException(e);
        }
    }
}

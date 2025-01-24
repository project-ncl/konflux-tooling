package org.jboss.pnc.konfluxtooling.prebuild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.quarkus.test.junit.QuarkusTest;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
@QuarkusTest
public class PreprocessorTest {

    {
        System.setProperty(
                "org.slf4j.simpleLogger.log.com.github.dockerjava.api.command.BuildImageResultCallback",
                "debug");
    }

    @SystemStub
    private EnvironmentVariables variables = new EnvironmentVariables("BUILD_SCRIPT", """
            echo 'BUILDING!'""");

    @Test
    public void testGenerate(@TempDir Path tempDir) throws IOException, InterruptedException {

        PreprocessorCommand preprocessor = new PreprocessorCommand();
        preprocessor.type = PreprocessorCommand.ToolType.ANT;
        preprocessor.recipeImage = "quay.io/redhat-user-workloads/konflux-jbs-pnc-tenant/jvm-build-service-builder-images/ubi7:latest";
        preprocessor.toolingImage = "quay.io/redhat-user-workloads/konflux-jbs-pnc-tenant/konflux-tooling:latest";
        preprocessor.buildRoot = tempDir;
        preprocessor.javaVersion = "7";
        preprocessor.buildToolVersion = "1.9.16";

        preprocessor.run();

        Process process = new ProcessBuilder("buildah", "build", "-f", tempDir.toString() + "/.jbs/Containerfile", ".")
                .directory(tempDir.toFile())
                .redirectErrorStream(true)
                .start();

        String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(text.contains("BUILDING!"));
        assertTrue(text.contains("Listening on: http://0.0.0.0:8084"));
        assertEquals(0, process.waitFor());
    }
}

package org.jboss.pnc.konfluxtooling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ResultsUpdater {

    public static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public void updateResults(Map<String, String> results) {

        for (var e : results.entrySet()) {
            try {
                Files.writeString(Paths.get("/tekton/results", e.getKey()), e.getValue());
            } catch (IOException ex) {
                Log.errorf(ex, "Failed to write result %s", e.getKey());
            }
        }
    }
}

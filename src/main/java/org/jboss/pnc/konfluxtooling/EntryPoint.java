package org.jboss.pnc.konfluxtooling;

import org.jboss.pnc.konfluxtooling.deploy.CopyArtifactsCommand;
import org.jboss.pnc.konfluxtooling.deploy.DeployCommand;
import org.jboss.pnc.konfluxtooling.logging.UploadLogCommand;
import org.jboss.pnc.konfluxtooling.notification.NotifyCommand;
import org.jboss.pnc.konfluxtooling.prebuild.PreprocessorCommand;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        CopyArtifactsCommand.class,
        DeployCommand.class,
        UploadLogCommand.class,
        NotifyCommand.class,
        PreprocessorCommand.class
})
public class EntryPoint {
}

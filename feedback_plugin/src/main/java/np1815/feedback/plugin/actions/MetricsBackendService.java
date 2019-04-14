package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import np1815.feedback.metricsbackend.api.DefaultApi;
import np1815.feedback.metricsbackend.client.ApiClient;
import np1815.feedback.metricsbackend.model.AllApplicationVersions;
import np1815.feedback.metricsbackend.model.PerformanceForFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.internal.psiView.PsiViewerDialog.LOG;

public class MetricsBackendService {
    private final ApiClient client;

    public static MetricsBackendService getInstance() {
        return ServiceManager.getService(MetricsBackendService.class);
    }

    public MetricsBackendService() {
        client = new ApiClient("http://localhost:8080/api", null, null, null);
    }

    public DefaultApi getClient() {
        return client.defaultApi();
    }

    public PerformanceForFile getPerformance(Project project, Repository repository, VirtualFile file) throws IOException, VcsException {
        String basePath = project.getBasePath();
        assert basePath != null;

        String currentVersion = repository.getCurrentRevision();
        LOG.debug("Version: " + currentVersion);

        String latestAvailableVersion = determineLastAvailableVersionInBackend(project, repository, currentVersion);
        LOG.debug("Determined latest available version: " + latestAvailableVersion);

        String path = Paths.get(basePath).relativize(Paths.get(file.getPath())).toString();
        LOG.debug("File path: " + path);

        PerformanceForFile performance = new PerformanceForFile();

        performance = getClient().getPerformanceForFile(path, latestAvailableVersion);

        return performance;
    }

    private String determineLastAvailableVersionInBackend(Project project, Repository repository, String currentVersion) throws IOException, VcsException {
        Git git = Git.getInstance();

        AllApplicationVersions versions = getClient().getApplicationVersions();
        List<String> versionsWithHead = new ArrayList<>(versions.getVersions());
        versionsWithHead.add(0, currentVersion);

        GitLineHandler gitLineHandler = new GitLineHandler(project, repository.getRoot(), GitCommand.MERGE_BASE);
        gitLineHandler.addParameters(versionsWithHead);
        GitCommandResult result = git.runCommand(gitLineHandler);

        return result.getOutputOrThrow();
    }
}

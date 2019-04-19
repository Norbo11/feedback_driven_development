package np1815.feedback.plugin.services;

import com.intellij.dvcs.repo.Repository;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsDiffUtil;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;
import com.intellij.util.diff.FilesTooBigForDiffException;
import git4idea.GitContentRevision;
import git4idea.GitVcs;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import np1815.feedback.metricsbackend.model.AllApplicationVersions;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.actions.DisplayFeedbackAction;
import np1815.feedback.plugin.components.FeedbackConfiguration;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MetricsBackendService {
    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);

    public static MetricsBackendService getInstance() {
        return ServiceManager.getService(MetricsBackendService.class);
    }

    public FilePerformanceDisplayProvider getPerformance(Project project, Repository repository, VirtualFile file,
                                                         String currentVersion, String latestAvailableVersion) throws IOException,
        VcsException {

        FeedbackDrivenDevelopment feedback = FeedbackDrivenDevelopment.getInstance(project);
        FeedbackConfiguration config = feedback.getFeedbackConfiguration();

        assert feedback.getState() != null;
        Path basePath = Paths.get(feedback.getState().feedbackConfigPath).getParent().resolve(feedback.getFeedbackConfiguration().getSourceBasePath());
        basePath = basePath.normalize();

        String path = basePath.relativize(Paths.get(file.getPath())).toString();
        LOG.debug("File path: " + path);

        String applicationName = config.getApplicationName();

        PerformanceForFile performance = feedback.getApiClient()
            .getPerformanceForFile(applicationName, latestAvailableVersion, path);

        boolean stale = !currentVersion.equals(latestAvailableVersion);

        Map<Integer, TranslatedLineNumber> translatedLineNumbers = translateLinesAccordingToChanges(project, file, latestAvailableVersion, performance);

        return new FilePerformanceDisplayProvider(performance, stale, translatedLineNumbers);
    }

    /* Translate the performance data into the new file view according to the edited changes */
    private Map<Integer, TranslatedLineNumber> translateLinesAccordingToChanges(Project project, VirtualFile file, String latestAvailableVersion, PerformanceForFile performance) throws VcsException {
        GitVcs vcs = GitVcs.getInstance(project);
        FilePath vcsFile = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
        VcsRevisionNumber latestRevisionNumber = vcs.parseRevisionNumber(latestAvailableVersion);

        // Get the changes between the current edited file and the previous known version we have
        ContentRevision beforeContentRevision = GitContentRevision.createRevision(file, latestRevisionNumber, project);
        List<Change> changes = VcsDiffUtil.createChangesWithCurrentContentForFile(vcsFile, beforeContentRevision);

        assert changes.size() == 1;

        Map<Integer, TranslatedLineNumber> translatedLineNumbers = new HashMap<>();

        for (Change change : changes) {
            try {
                String before = change.getBeforeRevision().getContent();
                String after = change.getAfterRevision().getContent();
                final Diff.Change c = Diff.buildChanges(before, after);

                // Translate lines based on the change
                for (Map.Entry<String, PerformanceForFileLines> entry : performance.getLines().entrySet()) {
                    int oldLineNumber = Integer.valueOf(entry.getKey());
                    int newLineNumber = Diff.translateLine(c, oldLineNumber);
                    boolean veryStale = false;

                    if (newLineNumber == -1) {
                        newLineNumber = Diff.translateLine(c, oldLineNumber, true);
                        veryStale = true;
                    }

                    translatedLineNumbers.put(newLineNumber, new TranslatedLineNumber(oldLineNumber, veryStale));
                }
            } catch (FilesTooBigForDiffException ignored) {
            }
        }

        return translatedLineNumbers;
    }

    public Optional<String> determineLastAvailableVersionInBackend(Project project, Repository repository, String currentVersion) throws IOException,
        VcsException {
        Git git = Git.getInstance();

        FeedbackDrivenDevelopment feedback = FeedbackDrivenDevelopment.getInstance(project);

        AllApplicationVersions versions = feedback.getApiClient().getApplicationVersions(feedback.getFeedbackConfiguration().getApplicationName());

        if (versions.getVersions().size() > 0) {
            List<String> versionsWithHead = new ArrayList<>(versions.getVersions());
            versionsWithHead.add(0, currentVersion);

            GitLineHandler gitLineHandler = new GitLineHandler(project, repository.getRoot(), GitCommand.MERGE_BASE);
            gitLineHandler.addParameters(versionsWithHead);
            GitCommandResult result = git.runCommand(gitLineHandler);


            return Optional.of(result.getOutputOrThrow());
        }

        return Optional.empty();
    }

}

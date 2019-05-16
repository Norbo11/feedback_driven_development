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
import git4idea.GitContentRevision;
import git4idea.GitVcs;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import np1815.feedback.metricsbackend.model.AllApplicationVersions;
import np1815.feedback.metricsbackend.model.FileFeedback;
import np1815.feedback.plugin.actions.DisplayFeedbackAction;
import np1815.feedback.plugin.config.FeedbackWrapperConfiguration;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;
import np1815.feedback.plugin.util.vcs.LineTranslator;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MetricsBackendService {
    public static final Logger LOG = Logger.getInstance(DisplayFeedbackAction.class);

    public static MetricsBackendService getInstance() {
        return ServiceManager.getService(MetricsBackendService.class);
    }

    public FileFeedbackWrapper getMultiVersionFeedback(Project project, Repository repository, VirtualFile file,
                                                                   String currentVersion, String latestAvailableVersion) throws IOException,
        VcsException {

        FeedbackDrivenDevelopment feedback = FeedbackDrivenDevelopment.getInstance(project);
        FeedbackWrapperConfiguration config = feedback.getFeedbackWrapperConfiguration();
        assert feedback.getState() != null;

        String path = getMetricBackendPath(file, feedback);
        LOG.debug("File path: " + path);

        String applicationName = config.getApplicationName();
        List<String> versions = getSortedCommitsUpTillVersion(project, repository, "HEAD", 3);

        Map<String, FileFeedback> versionedFeedback = feedback.getApiClient().getFeedbackForFile(applicationName, versions, path, "beginning_of_version", null);

        String afterVersion = versions.get(0);

        Map<String, Map<Integer, TranslatedLineNumber>> versionTranslations = new HashMap<>();

        // Iterate through all versions, translating lines between versions
        for (String beforeVersion : versions.subList(1, versions.size())) {
            LOG.debug("Looking at version " + beforeVersion);
            List<Change> changes = getChangesBetweenVersions(project, file, beforeVersion, afterVersion);

            Map<Integer, TranslatedLineNumber> translatedLineNumbers = LineTranslator.translateLinesAccordingToChanges(changes);

            versionTranslations.put(afterVersion, translatedLineNumbers);
            afterVersion = beforeVersion;
        }

        List<Change> localChanges = getChangesComparedToLocal(project, file, versions.get(0));
        Map<Integer, TranslatedLineNumber> localTranslations = LineTranslator.translateLinesAccordingToChanges(localChanges);

        return new FileFeedbackWrapper(versions, versionedFeedback, versionTranslations, localTranslations);
    }

    /**
     * Generate the path expected by the metric backend, by relativising and normalising against the feedback config file path
     */
    private String getMetricBackendPath(VirtualFile file, FeedbackDrivenDevelopment feedback) {
        Path basePath = Paths.get(feedback.getState().feedbackConfigPath).getParent().resolve(feedback.getFeedbackWrapperConfiguration().getSourceBasePath());
        basePath = basePath.normalize();
        return basePath.relativize(Paths.get(file.getPath())).toString();
    }

    /**
     * Get the changes between the current content of a file and another version
     *
     * @return
     */
    @NotNull
    public List<Change> getChangesComparedToLocal(Project project, VirtualFile file, String version) throws VcsException {
        GitVcs vcs = GitVcs.getInstance(project);
        FilePath vcsFile = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
        VcsRevisionNumber vcsVersion = vcs.parseRevisionNumber(version);
        ContentRevision beforeContentRevision = GitContentRevision.createRevision(file, vcsVersion, project);
        return VcsDiffUtil.createChangesWithCurrentContentForFile(vcsFile, beforeContentRevision);
    }

    public List<Change> getChangesBetweenVersions(Project project, VirtualFile file, String before, String after) throws VcsException {
        GitVcs vcs = GitVcs.getInstance(project);
        FilePath vcsFile = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);

        VcsRevisionNumber vcsVersion1 = vcs.parseRevisionNumber(before);
        VcsRevisionNumber vcsVersion2 = vcs.parseRevisionNumber(after);

        ContentRevision revision1 = GitContentRevision.createRevision(file, vcsVersion1, project);
        ContentRevision revision2 = GitContentRevision.createRevision(file, vcsVersion2, project);

        return Collections.singletonList(new Change(revision1, revision2));
    }

    public Optional<String> determineLastAvailableVersionInBackend(Project project, Repository repository, String currentVersion) throws IOException,
        VcsException {
        Git git = Git.getInstance();

        FeedbackDrivenDevelopment feedback = FeedbackDrivenDevelopment.getInstance(project);

        AllApplicationVersions versions = feedback.getApiClient().getApplicationVersions(feedback.getFeedbackWrapperConfiguration().getApplicationName());

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

    /**
     * Return a sorted list of commits up until the currently checked out version, with the most recent first
     */
    public List<String> getSortedCommitsUpTillVersion(Project project, Repository repository, String version, int limit) throws VcsException {
        Git git = Git.getInstance();

        GitLineHandler gitLineHandler = new GitLineHandler(project, repository.getRoot(), GitCommand.REV_LIST);
        gitLineHandler.addParameters("-n " + limit);
        gitLineHandler.addParameters(version);
        GitCommandResult result = git.runCommand(gitLineHandler);

        result.throwOnError();
        return result.getOutput();
    }
}

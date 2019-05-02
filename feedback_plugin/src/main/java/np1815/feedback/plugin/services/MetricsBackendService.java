package np1815.feedback.plugin.services;

import com.intellij.dvcs.repo.Repository;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsDiffUtil;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFileImpl;
import git4idea.GitContentRevision;
import git4idea.GitVcs;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import np1815.feedback.metricsbackend.model.AllApplicationVersions;
import np1815.feedback.metricsbackend.model.FileFeedback;
import np1815.feedback.plugin.actions.DisplayFeedbackAction;
import np1815.feedback.plugin.components.FeedbackConfiguration;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.FileFeedbackWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MetricsBackendService {
    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);

    public static MetricsBackendService getInstance() {
        return ServiceManager.getService(MetricsBackendService.class);
    }

    public FileFeedbackWrapper getPerformance(Project project, Repository repository, VirtualFile file,
                                              String currentVersion, String latestAvailableVersion) throws IOException,
        VcsException {

        FeedbackDrivenDevelopment feedback = FeedbackDrivenDevelopment.getInstance(project);
        FeedbackConfiguration config = feedback.getFeedbackConfiguration();
        assert feedback.getState() != null;

        String path = getMetricBackendPath(file, feedback);
        LOG.debug("File path: " + path);

        String applicationName = config.getApplicationName();
        FileFeedback fileFeedback = feedback.getApiClient().getFeedbackForFile(applicationName, latestAvailableVersion, path);

        boolean stale = !currentVersion.equals(latestAvailableVersion);

        List<Change> changes = getChangesSinceVersion(project, file, latestAvailableVersion);

        Map<Integer, TranslatedLineNumber> translatedLineNumbers = MetricsBackendServiceUtil.translateLinesAccordingToChanges(changes,
            fileFeedback.getLines().keySet().stream().map(Integer::valueOf).collect(Collectors.toSet()));

        return new FileFeedbackWrapper(fileFeedback, stale, translatedLineNumbers);
    }
    /**
     * Generate the path expected by the metric backend, by relativising and normalising against the feedback config file path
     */
    private String getMetricBackendPath(VirtualFile file, FeedbackDrivenDevelopment feedback) {
        Path basePath = Paths.get(feedback.getState().feedbackConfigPath).getParent().resolve(feedback.getFeedbackConfiguration().getSourceBasePath());
        basePath = basePath.normalize();
        return basePath.relativize(Paths.get(file.getPath())).toString();
    }

    /**
     * Get the changes between the current content of a file and the previous known version we have in the metric backend
     *
     * @return
     */
    @NotNull
    public List<Change> getChangesSinceVersion(Project project, VirtualFile file, String latestAvailableVersion) throws VcsException {
        GitVcs vcs = GitVcs.getInstance(project);
        FilePath vcsFile = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
        VcsRevisionNumber latestRevisionNumber = vcs.parseRevisionNumber(latestAvailableVersion);
        ContentRevision beforeContentRevision = GitContentRevision.createRevision(file, latestRevisionNumber, project);
        return VcsDiffUtil.createChangesWithCurrentContentForFile(vcsFile, beforeContentRevision);
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

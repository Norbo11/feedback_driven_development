package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.ui.FeedbackMouseMotionListener;
import np1815.feedback.plugin.ui.FilePerformanceGutterProvider;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.util.BranchProbabilityProvider;
import np1815.feedback.plugin.util.FileFeedbackDisplayProvider;
import np1815.feedback.plugin.util.FileFeedbackWrapper;
import np1815.feedback.plugin.util.PythonBranchProbabilityProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;

public class DisplayFeedbackAction extends AnAction {

    private static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);
    private static final int HIGHLIGHTER_LAYER = HighlighterLayer.SELECTION - 1;
    private static final NotificationGroup NOTIFICATIONS_GROUP_ERROR = NotificationGroup.balloonGroup("FeedbackDrivenDevelopment.Error");

    private final MetricsBackendService metricsBackend;
    private FeedbackMouseMotionListener mouseMotionListener;
    Timer feedbackDisplayTimer;
    private boolean started;
    private MarkupModel markupModel;

    public DisplayFeedbackAction() {
        super("Feedback");

        this.metricsBackend = MetricsBackendService.getInstance();
        this.started = false;
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        event.getPresentation().setEnabled(project != null && editor != null && file != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        LOG.debug("Action hit");

        // A project is the current project being edited
        Project project = event.getProject();
        assert project != null;

        // An editor is the editor that was open when the action was launched (what happens with split editors?)
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        assert editor != null;

        // A virtual file is an abstraction over the file system
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        assert file != null;

        if (started) {
            stopDisplayingFeedback(project, editor, file);
        } else {
            startDisplayingFeedback(project, editor, file);
        }
    }

    /**
     * Start displaying feedback for a given project, editor and file
     * 1) Fetch feedback
     * 2) Schedule an update timer for the feedback, but only if the first fetch was successful
     * 3) Set up gutter, line highlights and mouse listener
     * @param project The project which we operate on, required for most IntelliJ calls
     * @param editor The editor which we want to highlight
     * @param file The file for which to fetch feedback
     */
    public void startDisplayingFeedback(Project project, Editor editor, VirtualFile file) {
        try {
            // A document is an editable sequence of characters
            Document document = editor.getDocument();

            // A markup model represents the text effects on a particular document
            markupModel = DocumentMarkupModel.forDocument(document, project, true);

            Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
            assert repository != null;

            FeedbackDrivenDevelopment feedbackComponent = FeedbackDrivenDevelopment.getInstance(project);
            BranchProbabilityProvider branchProbabilityProvider = new PythonBranchProbabilityProvider(file, PsiManager.getInstance(project), FileDocumentManager.getInstance());
            FileFeedbackDisplayProvider displayProvider = new FileFeedbackDisplayProvider(branchProbabilityProvider);
            FilePerformanceGutterProvider gutterProvider = new FilePerformanceGutterProvider(displayProvider);

            // TODO: Need one listener/timer per file (maybe? not if a new action is created every time)
            mouseMotionListener = new FeedbackMouseMotionListener(displayProvider);
            editor.addEditorMouseMotionListener(mouseMotionListener);

            ActionListener refreshFeedback = (e) -> {
                LOG.info("Refreshing feedback");
                FileFeedbackWrapper newFeedback = fetchFeedback(project, file, repository);

                if (newFeedback != null) {
                    displayProvider.refreshFeedback(newFeedback);
                    clearFeedback(editor);
                    annotateFileWithFeedback(editor, markupModel, displayProvider, gutterProvider);
                }
            };

            if (feedbackComponent.getState().autoRefresh) {
                feedbackDisplayTimer = UIUtil.createNamedTimer("FeedbackDisplayTimer", feedbackComponent.getState().autoRefreshInterval * 1000, refreshFeedback);
                feedbackDisplayTimer.setRepeats(true);
                feedbackDisplayTimer.start();
            }

            refreshFeedback.actionPerformed(null);
        } finally {
            started = true;
            getTemplatePresentation().setIcon(AllIcons.Actions.Pause);
        }
    }

    private void stopDisplayingFeedback(Project project, Editor editor, VirtualFile file) {
        try {
            if (feedbackDisplayTimer != null) {
                feedbackDisplayTimer.stop();
                feedbackDisplayTimer = null;
            }

            if (mouseMotionListener != null) {
                editor.removeEditorMouseMotionListener(mouseMotionListener);
                mouseMotionListener = null;
            }

            clearFeedback(editor);
        } finally {
            started = false;
            getTemplatePresentation().setIcon(AllIcons.Actions.QuickfixBulb);
        }
    }

    /**
     * Fetches feedback for the given project and file, for the version git version as provided by the repository object.
     * 1) Determine the current version from the git repository
     * 2) Determine the latest version available in the backend, depending on our current version
     * 3) Fetch performance information for the file, if a version of feedback is available
     * @param project
     * @param file
     * @param repository
     * @return A FileFeedbackWrapper containing new feedback, or null if we couldn't fetch for some reason (user is also notified)
     */
    @Nullable
    private FileFeedbackWrapper fetchFeedback(Project project, VirtualFile file, Repository repository) {
        String currentVersion = repository.getCurrentRevision();
        assert currentVersion != null;
        LOG.debug("Current Version: " + currentVersion);

        FileFeedbackWrapper feedbackWrapper = null;
        try {
            Optional<String> latestAvailableVersion = metricsBackend.determineLastAvailableVersionInBackend(project, repository, currentVersion);
            LOG.debug("Determined latest available version: " + latestAvailableVersion);

            if (latestAvailableVersion.isPresent()) {
                feedbackWrapper = metricsBackend.getPerformance(project, repository, file, currentVersion, latestAvailableVersion.get());
            } else {
                Notifications.Bus.notify(new Notification(
                    NOTIFICATIONS_GROUP_ERROR.getDisplayId(),
                    "Feedback not available",
                    "No feedback versions are available for this application",
                    NotificationType.INFORMATION
                ));
            }
        } catch (IOException e) {
            Notifications.Bus.notify(new Notification(
                NOTIFICATIONS_GROUP_ERROR.getDisplayId(),
                "Could not fetch feedback",
                "There was an error connecting to the metric handling backend: " + e.getMessage(),
                NotificationType.ERROR
            ));
        } catch (VcsException e) {
            Notifications.Bus.notify(new Notification(
                NOTIFICATIONS_GROUP_ERROR.getDisplayId(),
                "Could not fetch feedback",
                "There was an error using git: " + e.getMessage(),
                NotificationType.ERROR
            ));
        }

        return feedbackWrapper;
    }

    private void clearFeedback(Editor editor) {
        markupModel.removeAllHighlighters();
        editor.getGutter().closeAllAnnotations();
    }

    public void annotateFileWithFeedback(
        Editor editor,
        MarkupModel markupModel,
        FileFeedbackDisplayProvider displayProvider,
        FilePerformanceGutterProvider gutterProvider) {

        editor.getGutter().registerTextAnnotation(gutterProvider);

        for (int lineNumber : displayProvider.getLineNumbers()) {
            TextAttributes attributes = new TextAttributes(
                    displayProvider.getForegroundColourForLine(lineNumber),
                    displayProvider.getBackgroundColourForLine(lineNumber),
                    null,
                    null,
                    EditorFontType.PLAIN.ordinal());

            markupModel.addLineHighlighter(lineNumber, HIGHLIGHTER_LAYER, attributes);
        }
    }
}
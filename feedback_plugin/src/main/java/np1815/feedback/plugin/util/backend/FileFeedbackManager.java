package np1815.feedback.plugin.util.backend;

import com.intellij.dvcs.repo.Repository;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.language.python.PythonFunctionPerformanceProvider;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.ui.FeedbackMouseMotionListener;
import np1815.feedback.plugin.ui.FilePerformanceGutterProvider;
import np1815.feedback.plugin.language.BranchProbabilityProvider;
import np1815.feedback.plugin.util.ui.FileFeedbackDisplayProvider;
import np1815.feedback.plugin.language.python.PythonBranchProbabilityProvider;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Optional;

import static np1815.feedback.plugin.components.FeedbackDrivenDevelopment.HIGHLIGHTER_LAYER;
import static np1815.feedback.plugin.components.FeedbackDrivenDevelopment.NOTIFICATIONS_GROUP_ERROR;


public class FileFeedbackManager {

    private static final Logger LOG = Logger.getInstance(FileFeedbackManager.class);

    private final MetricsBackendService metricsBackend;
    private final Project project;
    private final Editor editor;
    private final VirtualFile file;
    private final Repository repository;
    private final MarkupModel markupModel;
    private final FileDocumentManager documentManager;
    private final PsiManager psiManager;

    private FeedbackMouseMotionListener mouseMotionListener;
    private Timer feedbackDisplayTimer;
    private boolean started;

    /**
     *
     * @param metricsBackend Metric backend which supplies feedback
     * @param project The project which we operate on, required for most IntelliJ calls
     * @param editor The editor which we want to highlight
     * @param file The file for which to fetch feedback
     * @param repository The VCS repository for this file
     * @param documentManager The document manager providing file contents for this file
     */
    public FileFeedbackManager(MetricsBackendService metricsBackend,
                               Project project,
                               Editor editor,
                               VirtualFile file,
                               Repository repository,
                               FileDocumentManager documentManager,
                               PsiManager psiManager
                               ) {

        this.metricsBackend = metricsBackend;
        this.project = project;
        this.editor = editor;
        this.file = file;
        this.repository = repository;
        this.documentManager = documentManager;
        this.psiManager = psiManager;

        Document document = documentManager.getDocument(file);
        assert document != null;

        // A markup model represents the text effects on a particular document
        this.markupModel = DocumentMarkupModel.forDocument(document, project, true);
    }

    /**
     * Start displaying feedback for a given project, editor and file
     * 1) Fetch feedback
     * 2) Schedule an update timer for the feedback, but only if the first fetch was successful
     * 3) Set up gutter, line highlights and mouse listener
     */
    private void startDisplayingFeedback() {
        try {
            FeedbackDrivenDevelopment feedbackComponent = FeedbackDrivenDevelopment.getInstance(project);
            BranchProbabilityProvider branchProbabilityProvider = new PythonBranchProbabilityProvider(file, psiManager, documentManager);
            PythonFunctionPerformanceProvider functionPerformanceProvider = new PythonFunctionPerformanceProvider(file, psiManager, documentManager,
                editor.getCaretModel());
            FileFeedbackDisplayProvider displayProvider = new FileFeedbackDisplayProvider(branchProbabilityProvider, functionPerformanceProvider);
            FilePerformanceGutterProvider gutterProvider = new FilePerformanceGutterProvider(displayProvider);

            // TODO: Need one listener/timer per file (maybe? not if a new action is created every time)
            mouseMotionListener = new FeedbackMouseMotionListener(displayProvider);
            editor.addEditorMouseMotionListener(mouseMotionListener);

            ActionListener refreshFeedback = (e) -> {
                LOG.info("Refreshing feedback");
                FileFeedbackWrapper newFeedback = fetchFeedback();

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
        }
    }

    private void stopDisplayingFeedback() {
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
        }
    }

    /**
     * Toggle display of feedback
     * @return New state
     */
    public boolean toggleFeedback() {
        if (started) {
            stopDisplayingFeedback();
        } else {
            startDisplayingFeedback();
        }

        return !started;
    }

    /**
     * Fetches feedback for the given project and file, for the version git version as provided by the repository object.
     * 1) Determine the current version from the git repository
     * 2) Determine the latest version available in the backend, depending on our current version
     * 3) Fetch performance information for the file, if a version of feedback is available
     * @return A FileFeedbackWrapper containing new feedback, or null if we couldn't fetch for some reason (user is also notified)
     */
    @Nullable
    private FileFeedbackWrapper fetchFeedback() {
        String currentVersion = repository.getCurrentRevision();
        assert currentVersion != null;
        LOG.debug("Current Version: " + currentVersion);

        FileFeedbackWrapper feedbackWrapper = null;
        try {
            Optional<String> latestAvailableVersion = metricsBackend.determineLastAvailableVersionInBackend(project, repository, currentVersion);
            LOG.debug("Determined latest available version: " + latestAvailableVersion);

            if (latestAvailableVersion.isPresent()) {
                feedbackWrapper = metricsBackend.getMultiVersionFeedback(project, repository, file, currentVersion, latestAvailableVersion.get());
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

        // TODO: Sometimes breaks UI? look into this
//        editor.getGutter().registerTextAnnotation(gutterProvider);

        Document document = documentManager.getDocument(file);
        assert document != null;

        for (int lineNumber = 0; lineNumber < document.getLineCount(); lineNumber++) {
            if (displayProvider.containsFeedbackForLine(lineNumber)) {
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
}

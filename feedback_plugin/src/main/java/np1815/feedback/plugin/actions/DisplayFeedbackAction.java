package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import np1815.feedback.plugin.ui.FeedbackMouseMotionListener;
import np1815.feedback.plugin.ui.FilePerformanceGutterProvider;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class DisplayFeedbackAction extends AnAction {

    private static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);
    private static final int HIGHLIGHTER_LAYER = HighlighterLayer.SELECTION - 1;
    private static final NotificationGroup NOTIFICATIONS_GROUP_ERROR = NotificationGroup.balloonGroup("FeedbackDrivenDevelopment.Error");

    private final MetricsBackendService metricsBackend;
    private FeedbackMouseMotionListener mouseMotionListener;
    private boolean displayed;
    private MarkupModel markupModel;

    public DisplayFeedbackAction() {
        super("Feedback");

        this.metricsBackend = MetricsBackendService.getInstance();
        this.displayed = false;
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        event.getPresentation().setEnabledAndVisible(project != null && editor != null && file != null);
    }

    public void display(Project project, Editor editor, VirtualFile file) {
        // A document is an editable sequence of characters
        Document document = editor.getDocument();

        // A markup model represents the text effects on a particular document
        markupModel = DocumentMarkupModel.forDocument(document, project, true);

        Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
        assert repository != null;

        String currentVersion = repository.getCurrentRevision();
        assert currentVersion != null;
        LOG.debug("Current Version: " + currentVersion);

        FilePerformanceDisplayProvider displayProvider = null;
        try {
            Optional<String> latestAvailableVersion = metricsBackend.determineLastAvailableVersionInBackend(project, repository, currentVersion);
            LOG.debug("Determined latest available version: " + latestAvailableVersion);

            if (latestAvailableVersion.isPresent()) {
                displayProvider = metricsBackend.getPerformance(project, repository, file, currentVersion, latestAvailableVersion.get());
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
                "There was an error connecting to the metric handling backend. Is it running?",
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

        if (displayProvider != null) {
            FilePerformanceGutterProvider textAnnotationProvider = new FilePerformanceGutterProvider(displayProvider);

            displayGlobalPerformance(editor, markupModel, displayProvider, textAnnotationProvider);

            mouseMotionListener = new FeedbackMouseMotionListener(displayProvider);
            editor.addEditorMouseMotionListener(mouseMotionListener);
            displayed = true;
        }
    }

    private void stopDisplaying(Project project, Editor editor, VirtualFile file) {
        markupModel.removeAllHighlighters();
        editor.getGutter().closeAllAnnotations();
        editor.removeEditorMouseMotionListener(mouseMotionListener);
        displayed = false;
    }

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

        if (displayed) {
            stopDisplaying(project, editor, file);
        } else {
            display(project, editor, file);
        }
    }

    public void displayGlobalPerformance(
        Editor editor,
        MarkupModel markupModel,
        FilePerformanceDisplayProvider displayProvider,
        FilePerformanceGutterProvider textAnnotationProvider) {

        editor.getGutter().registerTextAnnotation(textAnnotationProvider);

        for (int lineNumber : displayProvider.getLineNumbers()) {
            TextAttributes attributes = new TextAttributes(
                    displayProvider.getForegroundColourForLine(lineNumber).orElse(null),
                    displayProvider.getBackgroundColourForLine(lineNumber).orElse(null),
                    null,
                    null,
                    EditorFontType.PLAIN.ordinal());

            markupModel.addLineHighlighter(lineNumber, HIGHLIGHTER_LAYER, attributes);
        }
    }
}
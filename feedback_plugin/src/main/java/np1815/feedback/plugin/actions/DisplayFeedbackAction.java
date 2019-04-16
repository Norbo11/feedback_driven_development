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
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import np1815.feedback.plugin.ui.FilePerformanceGutterProvider;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DisplayFeedbackAction extends AnAction {

    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);
    private final MetricsBackendService metricsBackend;

    public DisplayFeedbackAction() {
        super("Feedback");

        this.metricsBackend = MetricsBackendService.getInstance();

        NotificationGroup.balloonGroup("FeedbackDrivenDevelopment.Error");
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

        // A document is an editable sequence of characters
        Document document = editor.getDocument();

        // A markup model represents the text effects on a particular document
        MarkupModel markupModel = DocumentMarkupModel.forDocument(document, project, true);

        Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
        assert repository != null;

        FilePerformanceDisplayProvider displayProvider = null;
        try {
            displayProvider = metricsBackend.getPerformance(project, repository, file);
        } catch (IOException e) {
            Notifications.Bus.notify(new Notification(
                "FeedbackDrivenDevelopment.Error",
                "Could not fetch feedback",
                "There was an error connecting to the metric handling backend. Is it running?",
                NotificationType.ERROR
            ));
        } catch (VcsException e) {
            Notifications.Bus.notify(new Notification(
                "FeedbackDrivenDevelopment.Error",
                "Could not fetch feedback",
                "There was an error using git: " + e.getMessage(),
                NotificationType.ERROR
            ));
        }

        if (displayProvider != null) {
            FilePerformanceGutterProvider textAnnotationProvider = new FilePerformanceGutterProvider(displayProvider);

            displayGlobalPerformance(editor, markupModel, displayProvider, textAnnotationProvider);
        }
    }

    public void displayGlobalPerformance(
        Editor editor,
        MarkupModel markupModel,
        FilePerformanceDisplayProvider displayProvider,
        FilePerformanceGutterProvider textAnnotationProvider) {

        markupModel.removeAllHighlighters();
        editor.getGutter().closeAllAnnotations();
        editor.getGutter().registerTextAnnotation(textAnnotationProvider);

        for (int lineNumber : displayProvider.getLines()) {
            TextAttributes attributes = new TextAttributes(
                    null,
                    displayProvider.getColorForLine(lineNumber).orElse(null),
                    null,
                    null,
                    EditorFontType.PLAIN.ordinal());

            markupModel.addLineHighlighter(lineNumber, 1, attributes);
        }
    }
}
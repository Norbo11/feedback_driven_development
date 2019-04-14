package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
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
import com.intellij.openapi.vfs.VirtualFile;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.intellij.FilePerformanceGutterProvider;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);
    private final MetricsBackendService metricsBackend;

    public DisplayFeedbackAction() {
        super("Feedback");

        this.metricsBackend = MetricsBackendService.getInstance();
    }

    public void actionPerformed(AnActionEvent event) {
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

        String basePath = project.getBasePath();
        assert basePath != null;

        Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
        assert repository != null;

        String currentVersion = repository.getCurrentRevision();
        LOG.debug("Version: " + currentVersion);

        String latestAvailableVersion = metricsBackend.determineLastAvailableVersionInBackend(project, repository, currentVersion);
        LOG.debug("Determined latest available version: " + latestAvailableVersion);

        String path = Paths.get(basePath).relativize(Paths.get(file.getPath())).toString();
        LOG.debug("File path: " + path);

        PerformanceForFile performance = metricsBackend.getPerformance(path, currentVersion);

        FilePerformanceDisplayProvider displayProvider = new FilePerformanceDisplayProvider(performance);
        FilePerformanceGutterProvider textAnnotationProvider = new FilePerformanceGutterProvider(displayProvider);

        displayGlobalPerformance(editor, markupModel, performance, displayProvider, textAnnotationProvider);
    }

    public void displayGlobalPerformance(
        Editor editor,
        MarkupModel markupModel,
        PerformanceForFile performance,
        FilePerformanceDisplayProvider displayProvider,
        FilePerformanceGutterProvider textAnnotationProvider) {

        markupModel.removeAllHighlighters();
        editor.getGutter().closeAllAnnotations();
        editor.getGutter().registerTextAnnotation(textAnnotationProvider);

        for (Map.Entry<String, PerformanceForFileLines> line : performance.getLines().entrySet()) {
            int lineNumber = Integer.valueOf(line.getKey());

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
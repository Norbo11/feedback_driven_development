package np1815.feedback.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.intellij.FilePerformanceGutterProvider;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);

    public DisplayFeedbackAction() {
        super("Feedback");

    }

    public void actionPerformed(AnActionEvent event) {
        // A project is the current project being edited
        Project project = event.getProject();

        // An editor is the editor that was open when the action was launched (what happens with split editors?)
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        assert editor != null;

        // A document is an editable sequence of characters
        Document document = editor.getDocument();

        // A virtual file is an abstraction over the file system
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        assert file != null;

        // A markup model represents the text effects on a particular document
        MarkupModel markupModel = DocumentMarkupModel.forDocument(document, project, true);

        System.out.println(file.getPath());

        // Make API call to lookup name
        String path = "playground_application/controllers/default_controller.py";

        PerformanceForFile performance = new PerformanceForFile();
        editor.getGutter().closeAllAnnotations();
        try {
            performance = MetricsBackendService.getInstance().getClient().getPerformanceForFile(path);
            FilePerformanceDisplayProvider displayProvider = new FilePerformanceDisplayProvider(performance);
            FilePerformanceGutterProvider textAnnotationProvider = new FilePerformanceGutterProvider(displayProvider);

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
        } catch (IOException e) {
            LOG.error("Exception while fetching performance: " + e.getMessage());
        }

//        Useful classes:
//        RangeHighlighter
//        event.getData(CommonDataKeys.);
//        FileEditorManager manager = FileEditorManager.getInstance(project);
//        FileEditor fileEditor = manager.getSelectedEditor();
//        VirtualFile file = fileEditor.getFile();
//        String name = file.getName();

    }
}
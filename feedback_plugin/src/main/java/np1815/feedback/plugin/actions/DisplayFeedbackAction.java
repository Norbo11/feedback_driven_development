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
import np1815.feedback.plugin.intellij.PerformanceGutterProvider;
import np1815.feedback.plugin.util.PerformanceColorProvider;

import java.io.IOException;
import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    public DisplayFeedbackAction() {
        super("Feedback");

    }


    public void newMethod() {
        System.out.println("hey there");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        MarkupModel markupModel = DocumentMarkupModel.forDocument(document, project, true);
        FileEditorManager manager = FileEditorManager.getInstance(project);
        FileEditor fileEditor = manager.getSelectedEditor();
        VirtualFile file = fileEditor.getFile();
        String name = file.getName();

        VirtualFile file2 = event.getData(CommonDataKeys.VIRTUAL_FILE);
        assert file == file2;

        System.out.println(file.getPath());
        newMethod();

        // Make API call to lookup name
        String path = "playground_application/controllers/default_controller.py";

        PerformanceForFile performance = new PerformanceForFile();
        editor.getGutter().closeAllAnnotations();
        try {
            performance = MetricsBackendService.getInstance().getClient().getPerformanceForFile(path);
            PerformanceColorProvider colors = new PerformanceColorProvider(performance);
            PerformanceGutterProvider textAnnotationProvider = new PerformanceGutterProvider(path, performance, colors);

            editor.getGutter().registerTextAnnotation(textAnnotationProvider);

            for (Map.Entry<String, PerformanceForFileLines> line : performance.getLines().entrySet()) {
                int lineNumber = Integer.valueOf(line.getKey());

                TextAttributes attributes = new TextAttributes(
                        null,
                        colors.getColorForLine(path, lineNumber),
                        null,
                        null,
                        EditorFontType.PLAIN.ordinal());

                markupModel.addLineHighlighter(lineNumber, 1, attributes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Useful classes:
//        RangeHighlighter
//        event.getData(CommonDataKeys.);

    }
}
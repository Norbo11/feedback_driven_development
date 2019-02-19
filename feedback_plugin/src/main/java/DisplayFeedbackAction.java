import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;

public class DisplayFeedbackAction extends AnAction {
    public DisplayFeedbackAction() {
        super("Feedback");
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

        System.out.println(name);
//        event.getData(CommonDataKeys.);

        // Make API call to lookup name




        TextAttributes attributes = new TextAttributes(Color.BLACK, Color.GREEN, Color.BLUE, EffectType.BOXED, 1);
        markupModel.addLineHighlighter(1, 1, attributes);
    }
}
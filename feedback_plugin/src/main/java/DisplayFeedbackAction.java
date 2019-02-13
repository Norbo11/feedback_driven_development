import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

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


        TextAttributes attributes = new TextAttributes(Color.BLACK, Color.GREEN, Color.BLUE, EffectType.BOXED, 1);
        markupModel.addLineHighlighter(1, 1, attributes);
    }
}
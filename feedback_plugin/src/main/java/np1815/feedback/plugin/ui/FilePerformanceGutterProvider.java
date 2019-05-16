package np1815.feedback.plugin.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import np1815.feedback.plugin.util.ui.FileFeedbackDisplayProvider;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class FilePerformanceGutterProvider implements TextAnnotationGutterProvider {

    private final FileFeedbackDisplayProvider feedbackDisplayProvider;

    public FilePerformanceGutterProvider(FileFeedbackDisplayProvider feedbackDisplayProvider) {
        this.feedbackDisplayProvider = feedbackDisplayProvider;
    }

    @Nullable
    @Override
    public String getLineText(int line, Editor editor) {
        return feedbackDisplayProvider.getGutterTextForLine(line);
    }

    @Nullable
    @Override
    public String getToolTip(int line, Editor editor) {
//        return feedbackDisplayProvider.getLineStatus(line);
        return null;
    }

    @Override
    public EditorFontType getStyle(int line, Editor editor) {
        if (feedbackDisplayProvider.isLineStale(line)) {
            return EditorFontType.ITALIC;
        }

        return EditorFontType.BOLD;
    }

    @Nullable
    @Override
    public ColorKey getColor(int line, Editor editor) {
        return EditorColors.ANNOTATIONS_COLOR;
    }

    @Nullable
    @Override
    public Color getBgColor(int line, Editor editor) {
        return feedbackDisplayProvider.getBackgroundColourForLine(line);
    }

    @Override
    public List<AnAction> getPopupActions(int line, Editor editor) {
        return null;
    }

    @Override
    public void gutterClosed() {

    }
}

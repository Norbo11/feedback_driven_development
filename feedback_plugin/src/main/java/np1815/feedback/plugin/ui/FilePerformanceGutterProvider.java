package np1815.feedback.plugin.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import np1815.feedback.plugin.util.FileFeedbackDisplayProvider;
import np1815.feedback.plugin.util.FileFeedbackWrapper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class FilePerformanceGutterProvider implements TextAnnotationGutterProvider {

    private final FileFeedbackWrapper fileFeedbackWrapper;
    private final FileFeedbackDisplayProvider performanceDisplayProvider;

    public FilePerformanceGutterProvider(FileFeedbackWrapper fileFeedbackWrapper, FileFeedbackDisplayProvider performanceDisplayProvider) {
        this.fileFeedbackWrapper = fileFeedbackWrapper;
        this.performanceDisplayProvider = performanceDisplayProvider;
    }

    @Nullable
    @Override
    public String getLineText(int line, Editor editor) {
        return performanceDisplayProvider.getGlobalAverageForLine(line);
    }

    @Nullable
    @Override
    public String getToolTip(int line, Editor editor) {
        return performanceDisplayProvider.getLineStatus(line);
    }

    @Override
    public EditorFontType getStyle(int line, Editor editor) {
        if (fileFeedbackWrapper.isLineVeryStale(line).orElse(false)) {
            return EditorFontType.BOLD_ITALIC;
        }

        if (fileFeedbackWrapper.isFileStale()) {
            return EditorFontType.ITALIC;
        }

        return EditorFontType.PLAIN;
    }

    @Nullable
    @Override
    public ColorKey getColor(int line, Editor editor) {
        return EditorColors.ANNOTATIONS_COLOR;
    }

    @Nullable
    @Override
    public Color getBgColor(int line, Editor editor) {
        return performanceDisplayProvider.getBackgroundColourForLine(line);
    }

    @Override
    public List<AnAction> getPopupActions(int line, Editor editor) {
        return null;
    }

    @Override
    public void gutterClosed() {

    }
}

package np1815.feedback.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class FilePerformanceGutterProvider implements TextAnnotationGutterProvider {

    private final FilePerformanceDisplayProvider performanceDisplayProvider;

    public FilePerformanceGutterProvider(FilePerformanceDisplayProvider performanceDisplayProvider) {
        this.performanceDisplayProvider = performanceDisplayProvider;
    }

    @Nullable
    @Override
    public String getLineText(int line, Editor editor) {
        return performanceDisplayProvider.getGlobalAverageForLine(line).orElse(null);
    }

    @Nullable
    @Override
    public String getToolTip(int line, Editor editor) {
        return "Global Average";
    }

    @Override
    public EditorFontType getStyle(int line, Editor editor) {
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
        return performanceDisplayProvider.getColorForLine(line).orElse(null);
    }

    @Override
    public List<AnAction> getPopupActions(int line, Editor editor) {
        return null;
    }

    @Override
    public void gutterClosed() {

    }
}

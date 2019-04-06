package np1815.feedback.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.util.PerformanceColorProvider;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class PerformanceGutterProvider implements TextAnnotationGutterProvider {

    private final String filePath;
    private final PerformanceForFile performance;
    private final PerformanceColorProvider colors;

    public PerformanceGutterProvider(String filePath, PerformanceForFile performance, PerformanceColorProvider colors) {
        this.filePath = filePath;
        this.performance = performance;
        this.colors = colors;
    }

    @Nullable
    @Override
    public String getLineText(int line, Editor editor) {
        PerformanceForFileLines perf = performance.getLines().get(String.valueOf(line));
        return perf == null ? null : perf.getGlobalAverage().toString();
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
        if (performance.getLines().containsKey(String.valueOf(line))) {
            return colors.getColorForLine(filePath, line);
        } else {
            return null;
        }
    }

    @Override
    public List<AnAction> getPopupActions(int line, Editor editor) {
        return null;
    }

    @Override
    public void gutterClosed() {

    }
}

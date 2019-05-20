package np1815.feedback.plugin.listeners;

import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;
import org.jetbrains.annotations.NotNull;

public class FeedbackCaretListener implements CaretListener {

    private final FileFeedbackManager fileFeedbackManager;

    public FeedbackCaretListener(FileFeedbackManager fileFeedbackManager) {
        this.fileFeedbackManager = fileFeedbackManager;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        fileFeedbackManager.repaintFeedback();
    }
}

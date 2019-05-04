package np1815.feedback.plugin.ui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import np1815.feedback.plugin.util.FileFeedbackDisplayProvider;
import np1815.feedback.plugin.util.FileFeedbackWrapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class FeedbackMouseMotionListener implements EditorMouseMotionListener {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackMouseMotionListener.class);
    private static final double REQUIRED_HOVER_SECONDS = 0.5;
    private final FileFeedbackDisplayProvider displayProvider;

    private int lineNumberLastHoveredOver;
    private javax.swing.Timer hoverTimer;
    private JBPopup popup;

    public FeedbackMouseMotionListener(FileFeedbackDisplayProvider displayProvider) {
        this.displayProvider = displayProvider;
        this.lineNumberLastHoveredOver = -1;
    }

    @Override
    public void mouseMoved(@NotNull EditorMouseEvent event) {
        int lineNumber = getLineNumber(event.getEditor(), event.getMouseEvent().getPoint());

        if (lineNumberLastHoveredOver == -1 || lineNumberLastHoveredOver != lineNumber) {
            if (hoverTimer != null) {
                hoverTimer.stop();
            }

            if (displayProvider.containsFeedbackForLine(lineNumber)) {
                hoverTimer = UIUtil.createNamedTimer("FeedbackTooltipTimer",
                    (int) (REQUIRED_HOVER_SECONDS * 1000), (x) ->
                        showFeedbackPopup(event.getEditor(), event.getMouseEvent().getLocationOnScreen(), lineNumber)
                );

                hoverTimer.setRepeats(false);
                hoverTimer.start();
            }

            lineNumberLastHoveredOver = lineNumber;
        }

    }

    private void showFeedbackPopup(Editor editor, Point point, int line) {

        FeedbackPopup feedbackPopup = new FeedbackPopup(line, displayProvider);
        displayProvider.addFeedbackChangeListener(feedbackPopup::update);

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(feedbackPopup.getRootComponent(), feedbackPopup.getRootComponent());
        popupBuilder.setTitle("Feedback");

        // Remove popup if we move 2 lines away
        popupBuilder.setCancelOnMouseOutCallback(event -> Math.abs(getLineNumber(editor, event.getPoint()) - line) > 2);

        // Only allow one popup at a time
        if (popup != null) {
            popup.cancel();
        }

        popup = popupBuilder.createPopup();
        popup.show(RelativePoint.fromScreen(point));
    }

    private int getLineNumber(Editor editor, Point point) {
        LogicalPosition logicalPosition = editor.xyToLogicalPosition(point);
        int offset = editor.logicalPositionToOffset(logicalPosition);
        return editor.getDocument().getLineNumber(offset);
    }
}

package np1815.feedback.plugin.ui;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FeedbackMouseMotionListener implements EditorMouseMotionListener {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackMouseMotionListener.class);
    private static final int REQUIRED_HOVER_SECONDS = 1;
    private final FilePerformanceDisplayProvider displayProvider;

    private int lineNumberLastHoveredOver;
    private javax.swing.Timer hoverTimer;

    public FeedbackMouseMotionListener(FilePerformanceDisplayProvider displayProvider) {
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
                    REQUIRED_HOVER_SECONDS * 1000, (x) -> showFeedbackPopup(event.getEditor(), event.getMouseEvent().getLocationOnScreen()));

                hoverTimer.setRepeats(false);
                hoverTimer.start();
            }

            lineNumberLastHoveredOver = lineNumber;
        }
    }

    private void showFeedbackPopup(Editor editor, Point point) {
        FeedbackPopup feedbackPopup = new FeedbackPopup();

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(feedbackPopup.getRootComponent(), feedbackPopup.getRootComponent());
        popupBuilder.setTitle("Feedback");

        // TODO: DO something with this
        popupBuilder.setMinSize(new Dimension(100, 100));
        popupBuilder.setCancelOnMouseOutCallback(event -> getLineNumber(editor, event.getPoint()) != lineNumberLastHoveredOver);

        JBPopup popup = popupBuilder.createPopup();
        popup.show(RelativePoint.fromScreen(point));
    }

    private int getLineNumber(Editor editor, Point point) {
        LogicalPosition logicalPosition = editor.xyToLogicalPosition(point);
        int offset = editor.logicalPositionToOffset(logicalPosition);
        return editor.getDocument().getLineNumber(offset);
    }
}

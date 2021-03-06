package np1815.feedback.plugin.listeners;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.IconButton;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.ui.FeedbackPopup;
import np1815.feedback.plugin.util.ui.FileFeedbackDisplayProvider;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;


import java.awt.*;

public class FeedbackMouseMotionListener implements EditorMouseMotionListener {

    private static final Logger LOG = Logger.getInstance(FeedbackMouseMotionListener.class);
    private static final double REQUIRED_HOVER_SECONDS = 0.5;
    private final FeedbackDrivenDevelopment feedbackComponent;
    private final FileFeedbackDisplayProvider displayProvider;

    private int lineNumberLastHoveredOver;
    private javax.swing.Timer hoverTimer;
    private JBPopup popup;
    private FeedbackPopup feedbackPopup;

    public FeedbackMouseMotionListener(FeedbackDrivenDevelopment feedbackComponent, FileFeedbackDisplayProvider displayProvider) {
        this.feedbackComponent = feedbackComponent;
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
        // Only allow one popup at a time
        if (popup != null) {
            displayProvider.removeFeedbackChangeListener(feedbackPopup::update);
            popup.cancel();
            popup.dispose();
        }

        feedbackPopup = new FeedbackPopup(feedbackComponent, line, displayProvider);
        displayProvider.addFeedbackChangeListener(feedbackPopup::update);

        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(feedbackPopup.getRootComponent(), feedbackPopup.getRootComponent());
        popupBuilder.setFocusable(true);
        popupBuilder.setMovable(true);
        popupBuilder.setRequestFocus(true);
        popupBuilder.setResizable(true);
        popupBuilder.setCancelOnClickOutside(true);
//        popupBuilder.setCancelOnWindowDeactivation(true);
        popupBuilder.setCancelButton(new IconButton("Close", AllIcons.Windows.CloseSmall));
        popupBuilder.setTitle("Feedback Driven Development");

        // Remove popup if we move 2 lines away
//        popupBuilder.setCancelOnMouseOutCallback(event -> Math.abs(getLineNumber(editor, event.getPoint()) - line) > 2);

        popup = popupBuilder.createPopup();
        popup.show(RelativePoint.fromScreen(point));
    }

    private int getLineNumber(Editor editor, Point point) {
        LogicalPosition logicalPosition = editor.xyToLogicalPosition(point);
        int offset = editor.logicalPositionToOffset(logicalPosition);
        return editor.getDocument().getLineNumber(offset);
    }
}

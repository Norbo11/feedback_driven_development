package np1815.feedback.plugin.ui;

import com.intellij.ConfigurableFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import org.jetbrains.annotations.NotNull;

public class FeedbackToolbarFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        FeedbackToolbar feedbackToolbar = new FeedbackToolbar(project, toolWindow);
        FeedbackConfigurationPanel feedbackConfigurationPanel = new FeedbackConfigurationPanel(project, FeedbackDrivenDevelopment.getInstance(project));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content displaySettings = contentFactory.createContent(feedbackToolbar.getRootComponent(), "Display Settings", false);
        Content feedbackConfiguration = contentFactory.createContent(feedbackConfigurationPanel.getRootComponent(), "Feedback Configuration", false);

        toolWindow.getContentManager().addContent(displaySettings);
        toolWindow.getContentManager().addContent(feedbackConfiguration);
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        //TODO: Implement logic to determine if toolbar should be available
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        // We don't want the tool to be loaded on project startup
        return true;
    }
}

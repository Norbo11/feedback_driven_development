package np1815.feedback.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.util.FormatUtils;
import np1815.feedback.plugin.util.RegressionItem.RegressionItem;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeedbackRegressionsPanel {
    private final FeedbackDrivenDevelopment feedbackComponent;

    private DefaultMutableTreeNode top;
    private DefaultMutableTreeNode regressions;
    private DefaultMutableTreeNode improvements;
    private Tree regressionTree;
    private JPanel rootComponent;
    private DefaultTreeModel treeModel;

    public FeedbackRegressionsPanel(FeedbackDrivenDevelopment feedbackComponent) {
        this.feedbackComponent = feedbackComponent;

        regressionTree.setCellRenderer(new ColoredTreeCellRenderer() {
            @Override
            public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof RegressionItem) {
                    RegressionItem regressionItem = (RegressionItem) userObject;
                    append(regressionItem.getLineNumber() + ": " + FormatUtils.formatPercentage(Math.abs(regressionItem.getIncrease())) +
                        (regressionItem.getIncrease() > 0 ? " increase" : " decrease") + " in mean execution time");
                    setIcon(regressionItem.getIncrease() > 0 ? AllIcons.General.ArrowUp : AllIcons.General.ArrowDown);
                    setForeground(regressionItem.getIncrease() > 0 ? JBColor.red : JBColor.green.darker());
                } else {
                    append(value.toString());
                }

                setVisible(true);
            }
        });

        feedbackComponent.addMultiFileFeedbackChangeListener(this::update);

        update();
    }


    public void createUIComponents() {
        top = new DefaultMutableTreeNode("Regressions & Improvements");
        regressions = new DefaultMutableTreeNode("Regressions");
        improvements = new DefaultMutableTreeNode("Improvements");
        top.add(regressions);
        top.add(improvements);

        treeModel = new DefaultTreeModel(top);
        regressionTree = new Tree(treeModel);
        regressionTree.setRootVisible(false);
    }

    public void update() {
        // TODO: Shouldn't remove children every time as that messes up order. should only update if new regressions are detected
        regressions.removeAllChildren();
        improvements.removeAllChildren();

        for (FileFeedbackManager manager : feedbackComponent.getFeedbackManagers().values()) {
            DefaultMutableTreeNode regressionsNode = new DefaultMutableTreeNode();
            DefaultMutableTreeNode improvementsNode = new DefaultMutableTreeNode();

            List<RegressionItem> regressionsAndImprovements = manager.getDisplayProvider().getFileFeedbackWrapper().getRegressions();
            List<RegressionItem> regressions = regressionsAndImprovements.stream().filter(ri -> ri.getIncrease() > 0).collect(Collectors.toList());
            List<RegressionItem> improvements = regressionsAndImprovements.stream().filter(ri -> ri.getIncrease() < 0).collect(Collectors.toList());

            for (RegressionItem item : regressions) {
                regressionsNode.add(new DefaultMutableTreeNode(item));
            }

            for (RegressionItem item : improvements) {
                improvementsNode.add(new DefaultMutableTreeNode(item));
            }

            regressionsNode.setUserObject(manager.getFile().getName() + " (" + regressions.size() + " regressions)");
            improvementsNode.setUserObject(manager.getFile().getName() + " (" + improvements.size() + " improvements)");

            this.regressions.add(regressionsNode);
            this.improvements.add(improvementsNode);
        }

        treeModel.reload();
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }
}

package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import git4idea.repo.GitRepository;
import np1815.feedback.plugin.components.FeedbackDrivenDevelopment;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;
import com.intellij.openapi.diagnostic.Logger;


import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(DisplayFeedbackAction.class);

    private final MetricsBackendService metricsBackend;

    public DisplayFeedbackAction() {
        super();

        this.metricsBackend = MetricsBackendService.getInstance();
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        boolean enabled = project != null && editor != null && file != null;
        event.getPresentation().setEnabled(enabled);

        if (enabled) {
            FileFeedbackManager manager = FeedbackDrivenDevelopment.getInstance(project).getFeedbackManagers().get(file);
            updateIcon(event, manager != null && manager.isStarted());
        } else {
            event.getPresentation().setIcon(AllIcons.Actions.QuickfixBulb);
            event.getPresentation().setText("Click on the desired editor first");
        }
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        LOG.info("Action hit");

        // A project is the current project being edited
        Project project = event.getProject();
        assert project != null;

        // An editor is the editor that was open when the action was launched TODO: (what happens with split editors?)
        Editor editor = event.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        assert editor != null;

        // A virtual file is an abstraction over the file system
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        assert file != null;

        // A PSI manager allows us to manipulate the AST
        PsiManager psiManager = PsiManager.getInstance(project);

        // A document is an editable sequence of characters
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();

        // A repository is a IntelliJ abstraction over a version control system
        Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
        assert repository != null;
        assert repository instanceof GitRepository;

        Map<VirtualFile, FileFeedbackManager> feedbackManagers = FeedbackDrivenDevelopment.getInstance(project).getFeedbackManagers();

        //TODO: Test effect with multiple editors editing the same file
        if (!feedbackManagers.containsKey(file)) {
            feedbackManagers.put(file, new FileFeedbackManager(metricsBackend, project, editor, file, (GitRepository) repository, fileDocumentManager, psiManager));
        }

        boolean feedbackDisplaying = feedbackManagers.get(file).toggleFeedback();

        updateIcon(event, feedbackDisplaying);
    }

    private void updateIcon(AnActionEvent event, boolean feedbackDisplaying) {
        if (feedbackDisplaying) {
            event.getPresentation().setIcon(AllIcons.Actions.Pause);
            event.getPresentation().setText("Pause feedback");
        } else {
            event.getPresentation().setIcon(AllIcons.Actions.QuickfixBulb);
            event.getPresentation().setText("Display feedback");
        }
    }
}
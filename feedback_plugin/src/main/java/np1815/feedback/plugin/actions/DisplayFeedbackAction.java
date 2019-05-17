package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import np1815.feedback.plugin.services.MetricsBackendService;
import np1815.feedback.plugin.util.backend.FileFeedbackManager;
import com.intellij.openapi.diagnostic.Logger;


import java.util.HashMap;
import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(DisplayFeedbackAction.class);

    private final Map<VirtualFile, FileFeedbackManager> feedbackManagers;
    private final MetricsBackendService metricsBackend;

    public DisplayFeedbackAction() {
        super();

        this.metricsBackend = MetricsBackendService.getInstance();
        this.feedbackManagers = new HashMap<>();
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);

        event.getPresentation().setEnabled(project != null && editor != null && file != null);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        LOG.debug("Action hit");

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

        //TODO: Test effect with multiple editors editing the same file
        if (!feedbackManagers.containsKey(file)) {
            feedbackManagers.put(file, new FileFeedbackManager(metricsBackend, project, editor, file, repository, fileDocumentManager, psiManager));
        }

        boolean feedbackDisplaying = feedbackManagers.get(file).toggleFeedback();

        if (feedbackDisplaying) {
            getTemplatePresentation().setIcon(AllIcons.Actions.Pause);
        } else {
            getTemplatePresentation().setIcon(AllIcons.Actions.QuickfixBulb);
        }
    }
}
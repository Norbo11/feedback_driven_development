package np1815.feedback.plugin.actions;

import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsType;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.configurable.VcsManagerConfigurable;
import com.intellij.openapi.vcs.history.VcsAbstractHistorySession;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsHistorySession;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.vcs.history.VcsHistoryProviderEx;
import git4idea.GitVcs;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import np1815.feedback.metricsbackend.api.DefaultApi;
import np1815.feedback.metricsbackend.model.AllApplicationVersions;
import np1815.feedback.metricsbackend.model.PerformanceForFile;
import np1815.feedback.metricsbackend.model.PerformanceForFileLines;
import np1815.feedback.plugin.intellij.FilePerformanceGutterProvider;
import np1815.feedback.plugin.util.FilePerformanceDisplayProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DisplayFeedbackAction extends AnAction {

    public static final Logger LOG = LoggerFactory.getLogger(DisplayFeedbackAction.class);
    private final DefaultApi metricsBackend;

    public DisplayFeedbackAction() {
        super("Feedback");

        this.metricsBackend = MetricsBackendService.getInstance().getClient();
    }

    public void actionPerformed(AnActionEvent event) {
        // A project is the current project being edited
        Project project = event.getProject();
        assert project != null;

        // An editor is the editor that was open when the action was launched (what happens with split editors?)
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        assert editor != null;

        // A document is an editable sequence of characters
        Document document = editor.getDocument();

        // A virtual file is an abstraction over the file system
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        assert file != null;

        // A markup model represents the text effects on a particular document
        MarkupModel markupModel = DocumentMarkupModel.forDocument(document, project, true);

        String basePath = project.getBasePath();
        assert basePath != null;

//        AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(file);

        Repository repository = VcsRepositoryManager.getInstance(project).getRepositoryForFile(file);
        assert repository != null;

        GitVcs gitVcs = GitVcs.getInstance(project);
        Git git = Git.getInstance();

        String version = repository.getCurrentRevision();
        LOG.debug("Version: " + version);

        try {
            AllApplicationVersions versions = metricsBackend.getApplicationVersions();
            List<String> versionsWithHead = new ArrayList<>(versions.getVersions());
            versionsWithHead.add(0, version);

            GitLineHandler gitLineHandler = new GitLineHandler(project, repository.getRoot(), GitCommand.MERGE_BASE);
            gitLineHandler.addParameters(versionsWithHead);
            GitCommandResult result = git.runCommand(gitLineHandler);

            if (!result.success()) {
                LOG.error("Git error: " + result.getErrorOutputAsJoinedString());
                return;
            }

            version = result.getOutputAsJoinedString();
            LOG.debug("Determined version: " + version);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String path = Paths.get(basePath).relativize(Paths.get(file.getPath())).toString();
        LOG.debug(String.format("File path: %s", path));

        PerformanceForFile performance = new PerformanceForFile();

        markupModel.removeAllHighlighters();
        editor.getGutter().closeAllAnnotations();

        try {
            performance = metricsBackend.getPerformanceForFile(path, version);
            FilePerformanceDisplayProvider displayProvider = new FilePerformanceDisplayProvider(performance);
            FilePerformanceGutterProvider textAnnotationProvider = new FilePerformanceGutterProvider(displayProvider);

            editor.getGutter().registerTextAnnotation(textAnnotationProvider);

            for (Map.Entry<String, PerformanceForFileLines> line : performance.getLines().entrySet()) {
                int lineNumber = Integer.valueOf(line.getKey());

                TextAttributes attributes = new TextAttributes(
                        null,
                        displayProvider.getColorForLine(lineNumber).orElse(null),
                        null,
                        null,
                        EditorFontType.PLAIN.ordinal());

                markupModel.addLineHighlighter(lineNumber, 1, attributes);
            }
        } catch (IOException e) {
            LOG.error("Exception while fetching performance: " + e.getMessage());
        }

//        PsiManager.getInstance(project).addPsiTreeChangeListener(PsiTreeChange);

//        Useful classes:
//        RangeHighlighter
//        event.getData(CommonDataKeys.);
//        FileEditorManager manager = FileEditorManager.getInstance(project);
//        FileEditor fileEditor = manager.getSelectedEditor();
//        VirtualFile file = fileEditor.getFile();
//        String name = file.getName();
    }
}
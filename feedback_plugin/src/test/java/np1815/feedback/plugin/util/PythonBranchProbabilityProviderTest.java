package np1815.feedback.plugin.util;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import np1815.feedback.metricsbackend.model.FileFeedbackLines;
import np1815.feedback.metricsbackend.model.LineGeneral;
import np1815.feedback.plugin.language.python.PythonBranchProbabilityProvider;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;
//import com.intellij.testFramework.
import np1815.feedback.plugin.test.PyLightProjectDescriptor;

import java.util.HashMap;
import java.util.Map;

public class PythonBranchProbabilityProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    private PythonBranchProbabilityProvider branchProbabilityProvider;
    private VirtualFile mainFile;
    private FileFeedbackWrapper feedbackWrapper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.mainFile = myFixture.copyFileToProject("main.py");
        this.branchProbabilityProvider = new PythonBranchProbabilityProvider(mainFile, getPsiManager(), FileDocumentManager.getInstance());

        Map<String, FileFeedbackLines> feedbackMap = new HashMap<>();
        feedbackMap.put("7", new FileFeedbackLines().general(new LineGeneral().profileCount(3)));
        feedbackMap.put("11", new FileFeedbackLines().general(new LineGeneral().profileCount(9)));
        feedbackMap.put("15", new FileFeedbackLines().general(new LineGeneral().profileCount(8)));

        Map<Integer, TranslatedLineNumber> translatedLineNumbers = new HashMap<>();

        for (Map.Entry<String, FileFeedbackLines> line : feedbackMap.entrySet()) {
            int lineNumber = Integer.valueOf(line.getKey());
            translatedLineNumbers.put(lineNumber, new TranslatedLineNumber(lineNumber));
        }

        //TODO: fix
//        this.feedbackWrapper = new FileFeedbackWrapper(new FileFeedback().lines(feedbackMap), false, translatedLineNumbers, "latest");
//        PsiTestUtil.

    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new PyLightProjectDescriptor("3.7");
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testdata/pythonProject1";
    }

    public void testBothBranchesProfiled() {
        Map<Integer, Double> branchExecutionProbability = branchProbabilityProvider.getBranchExecutionProbability(feedbackWrapper);

        assertEquals(0.15, branchExecutionProbability.get(7));
        assertEquals(0.45, branchExecutionProbability.get(11));
        assertEquals(0.4, branchExecutionProbability.get(15));
    }
}

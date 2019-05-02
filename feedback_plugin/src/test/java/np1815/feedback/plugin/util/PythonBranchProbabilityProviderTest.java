package np1815.feedback.plugin.util;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.python.testing.PyTestFactory;
import com.jetbrains.python.testing.PythonUnitTestUtil;
import np1815.feedback.metricsbackend.model.FileFeedback;
import np1815.feedback.metricsbackend.model.FileFeedbackLines;
import np1815.feedback.metricsbackend.model.LineGeneral;
import np1815.feedback.metricsbackend.model.LinePerformance;
import np1815.feedback.plugin.services.MetricsBackendServiceUtil;
import np1815.feedback.plugin.services.TranslatedLineNumber;
//import com.intellij.testFramework.
import np1815.feedback.plugin.util.np1815.feedback.plugin.test.PyLightProjectDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PythonBranchProbabilityProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    private PythonBranchProbabilityProvider branchProbabilityProvider;
    private VirtualFile mainFile;
    private FileFeedbackWrapper feedbackWrapper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.branchProbabilityProvider = new PythonBranchProbabilityProvider(getPsiManager(), FileDocumentManager.getInstance());
        this.mainFile = myFixture.copyFileToProject("main.py");

        Map<String, FileFeedbackLines> feedbackMap = new HashMap<>();
        feedbackMap.put("7", new FileFeedbackLines().general(new LineGeneral().executionCount(3)));
        feedbackMap.put("11", new FileFeedbackLines().general(new LineGeneral().executionCount(9)));
        feedbackMap.put("15", new FileFeedbackLines().general(new LineGeneral().executionCount(8)));

        Map<Integer, TranslatedLineNumber> translatedLineNumbers = new HashMap<>();

        for (Map.Entry<String, FileFeedbackLines> line : feedbackMap.entrySet()) {
            int lineNumber = Integer.valueOf(line.getKey());
            translatedLineNumbers.put(lineNumber, new TranslatedLineNumber(lineNumber, false, ""));
        }

        this.feedbackWrapper = new FileFeedbackWrapper(new FileFeedback().lines(feedbackMap), false, translatedLineNumbers);
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
        Map<Integer, Double> branchExecutionProbability = branchProbabilityProvider.getBranchExecutionProbability(mainFile, feedbackWrapper);

        assertEquals(0.15, branchExecutionProbability.get(7));
        assertEquals(0.45, branchExecutionProbability.get(11));
        assertEquals(0.4, branchExecutionProbability.get(15));
    }
}

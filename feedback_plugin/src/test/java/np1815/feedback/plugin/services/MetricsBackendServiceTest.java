package np1815.feedback.plugin.services;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vcs.VcsApplicationSettings;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsRootSettings;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// TODO: Fix this test not picking up that main.py is under VCS
// Possibly need to use a heavy test, or set up a real temporary directory instead of the in-memory file system
@Ignore
public class MetricsBackendServiceTest extends LightPlatformCodeInsightFixtureTestCase {

    private VirtualFile virtualFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyDirectoryToProject(".git", ".git");
        this.virtualFile = myFixture.copyFileToProject("main.py");

//        IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder()
//            IdeaTestExecutionPolicy.current().cr
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new DefaultLightProjectDescriptor();
    }

    public void testTranslateLinesAccordingToChanges() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        List<Change> changes = service.getChangesSinceVersion(myFixture.getProject(), virtualFile, "0e43d213d3048105fc2735356e21cff62c96afed");

        Map<Integer, TranslatedLineNumber> map = MetricsBackendServiceUtil.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Arrays.asList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testdata/pythonProject1";
    }

}
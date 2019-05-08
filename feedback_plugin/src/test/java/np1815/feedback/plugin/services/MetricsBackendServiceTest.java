package np1815.feedback.plugin.services;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.*;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// TODO: Fix this test not picking up that main.py is under VCS
// Possibly need to use a heavy test, or set up a real temporary directory instead of the in-memory file system
// hi I got feedback from git developers about writing tests
// The correct way to write a VCS test is to extend the VcsPlatformTest (or GitPlatformTest if it is Git specific). It is a Heavy test indeed, but it is not
// a big problem, since VCS tests won't benefit much from being transformed to Light tests. Inside the test one can generate Git repository as we usually do by firing correspondent commands (create file, modify, git add, git commit, etc.), or by copying a prepared .git folder: that doesn't matter, AFAIR the GitPlatformTest doesn't have any restrictions on that.
// But please note that one can't depend on the vcs-tests module from a plugin right now, until
// extends GitPlatformTest
// NOTES:
// - Can't depend on test sources, so need to get everything to compile first
// - Need to handle copying from test data directory yourself unless we inline the files
// - May need to refactor code to be testable
// - Overall probably not worth it?

@Ignore
public class MetricsBackendServiceTest extends LightPlatformCodeInsightFixtureTestCase {

    private VirtualFile virtualFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
//        myFixture.copyDirectoryToProject(".git", ".git");
//        this.virtualFile = myFixture.copyFileToProject("main.py")
//        testRoot.


//        IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder()
//            IdeaTestExecutionPolicy.current().c
//        getTestRootFile().
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return new DefaultLightProjectDescriptor();
    }

    public void testTranslateLinesAccordingToChanges() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        List<Change> changes = service.getChangesSinceVersion(getProject(), virtualFile, "0e43d213d3048105fc2735356e21cff62c96afed");

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
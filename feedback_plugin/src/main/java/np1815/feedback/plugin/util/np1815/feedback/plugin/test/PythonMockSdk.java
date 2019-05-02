package np1815.feedback.plugin.util.np1815.feedback.plugin.test;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.MockSdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.python.codeInsight.typing.PyTypeShed;
import com.jetbrains.python.codeInsight.userSkeletons.PyUserSkeletonsUtil;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.sdk.PythonSdkType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author yole
 */
public class PythonMockSdk {
    @NonNls private static final String MOCK_SDK_NAME = "Mock Python SDK";

    private PythonMockSdk() {
    }

    public static Sdk create(final String version, @NotNull final VirtualFile... additionalRoots) {
        final String mock_path = "src/test/testdata/MockSdk" + version + "/";

        String sdkHome = new File(mock_path, "bin/python" + version).getPath();
        SdkType sdkType = PythonSdkType.getInstance();

        MultiMap<OrderRootType, VirtualFile> roots = MultiMap.create();

        File libPath = new File(mock_path, "Lib");
        if (libPath.exists()) {
            roots.putValue(OrderRootType.CLASSES, LocalFileSystem.getInstance().refreshAndFindFileByIoFile(libPath));
        }

        roots.putValue(OrderRootType.CLASSES, PyUserSkeletonsUtil.getUserSkeletonsDirectory());

        final LanguageLevel level = LanguageLevel.fromPythonVersion(version);
        final VirtualFile typeShedDir = PyTypeShed.INSTANCE.getDirectory();
        assert typeShedDir != null;
        PyTypeShed.INSTANCE.findRootsForLanguageLevel(level).forEach(path -> {
            final VirtualFile file = typeShedDir.findFileByRelativePath(path);
            if (file != null) {
                roots.putValue(OrderRootType.CLASSES, file);
            }
        });

        String mock_stubs_path = mock_path + PythonSdkType.SKELETON_DIR_NAME;
        roots.putValue(PythonSdkType.BUILTIN_ROOT_TYPE, LocalFileSystem.getInstance().refreshAndFindFileByPath(mock_stubs_path));

        for (final VirtualFile root : additionalRoots) {
            roots.putValue(OrderRootType.CLASSES, root);
        }

        MockSdk sdk = new MockSdk(MOCK_SDK_NAME + " " + version, sdkHome, "Python " + version + " Mock SDK", roots, sdkType);

        // com.jetbrains.python.psi.resolve.PythonSdkPathCache.getInstance() corrupts SDK, so have to clone
        return sdk.clone();
    }
}

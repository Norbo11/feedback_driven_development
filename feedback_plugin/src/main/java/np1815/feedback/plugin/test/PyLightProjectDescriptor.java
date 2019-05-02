package np1815.feedback.plugin.test;

import com.intellij.testFramework.LightProjectDescriptor;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.python.PythonModuleTypeBase;
import org.jetbrains.annotations.NotNull;

public class PyLightProjectDescriptor extends LightProjectDescriptor {
    private final String myPythonVersion;

    public PyLightProjectDescriptor(String pythonVersion) {
        myPythonVersion = pythonVersion;
    }

    @NotNull
    @Override
    public ModuleType getModuleType() {
        return PythonModuleTypeBase.getInstance();
    }

    @Override
    public Sdk getSdk() {
        return PythonMockSdk.create(myPythonVersion, getAdditionalRoots());
//        return PythonSdkType.findSdkByPath()
    }

    /**
     * @return additional roots to add to mock python
     */
    @NotNull
    protected VirtualFile[] getAdditionalRoots() {
        return VirtualFile.EMPTY_ARRAY;
    }

    protected void createLibrary(ModifiableRootModel model, final String name, final String path) {
        final Library.ModifiableModel modifiableModel = model.getModuleLibraryTable().createLibrary(name).getModifiableModel();
        final VirtualFile home =
            LocalFileSystem.getInstance().refreshAndFindFileByPath(PathManager.getHomePath() + path);

        modifiableModel.addRoot(home, OrderRootType.CLASSES);
        modifiableModel.commit();
    }
}

package np1815.feedback.plugin.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyElementTypes;
import com.jetbrains.python.psi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface BranchProbabilityProvider {

    public Map<Integer, Double> getBranchExecutionProbability(FileFeedbackWrapper feedbackWrapper);
}

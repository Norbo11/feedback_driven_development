package np1815.feedback.plugin.language.python;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRecursiveElementVisitor;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class PythonFunctionPerformanceProvider {

    private final VirtualFile file;
    private final PsiManager psiManager;
    private final FileDocumentManager fileDocumentManager;

    public PythonFunctionPerformanceProvider(VirtualFile file, PsiManager psiManager, FileDocumentManager fileDocumentManager) {
        this.file = file;
        this.psiManager = psiManager;
        this.fileDocumentManager = fileDocumentManager;
    }


    private class AggregatedFunctionFeedback {

    }

    public Map<Integer, AggregatedFunctionFeedback> getFeedbackForFunctionsInFile(FileFeedbackWrapper fileFeedbackWrapper) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;
        assert psiFile instanceof PyFile;
        PyFile pyFile = (PyFile) psiFile;

        Document document = fileDocumentManager.getDocument(file);
        assert document != null;

        psiFile.accept(new PyRecursiveElementVisitor() {
            @Override
            public void visitPyFunction(PyFunction function) {
                super.visitPyFunction(function);

                // Can estimate the average performance of the function by considering the performance of the lines contained within it
            }
        });

        return null;
    }

    public Optional<Double> getAggregatePerformanceForFunction(FileFeedbackWrapper fileFeedbackWrapper, int line) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;

        Document document = fileDocumentManager.getDocument(file);
        assert document != null;

        PsiElement elementAtLine = psiFile.findElementAt(document.getLineStartOffset(line));
        assert elementAtLine != null;

        PyFunction containingFunction = (PyFunction) PsiTreeUtil.findFirstParent(elementAtLine, e -> e instanceof PyFunction);
        assert containingFunction != null;

        int from = document.getLineNumber(containingFunction.getTextRange().getStartOffset());
        int to = document.getLineNumber(containingFunction.getTextRange().getEndOffset());

        double totalGlobalAverage = IntStream.rangeClosed(from, to).mapToDouble(i -> fileFeedbackWrapper.getGlobalAverageForLine(i).orElse(0.0)).sum();
        return Optional.of(totalGlobalAverage);
    }
}

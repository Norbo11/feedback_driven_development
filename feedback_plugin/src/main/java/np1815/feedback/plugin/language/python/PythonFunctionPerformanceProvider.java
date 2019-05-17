package np1815.feedback.plugin.language.python;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class PythonFunctionPerformanceProvider {

    private final VirtualFile file;
    private final PsiManager psiManager;
    private final FileDocumentManager fileDocumentManager;
    private final CaretModel caretModel;

    public PythonFunctionPerformanceProvider(VirtualFile file, PsiManager psiManager, FileDocumentManager fileDocumentManager, CaretModel caretModel) {
        this.file = file;
        this.psiManager = psiManager;
        this.fileDocumentManager = fileDocumentManager;
        this.caretModel = caretModel;
    }

    public <T> Optional<Double> getAggregatePerformanceForScope(FileFeedbackWrapper fileFeedbackWrapper, int line, Class<? extends PsiElement> scopeClass) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;

        Document document = fileDocumentManager.getDocument(file);
        assert document != null;

        PsiElement elementAtLine = psiFile.findElementAt(document.getLineStartOffset(line));
        assert elementAtLine != null;

        PyFunction containingFunction = (PyFunction) PsiTreeUtil.findFirstParent(elementAtLine, scopeClass::isInstance);
        assert containingFunction != null;

        return calculateAggregatePerformanceInScope(fileFeedbackWrapper, document, containingFunction);
    }

    public Optional<Double> calculateAggregatePerformanceInScope(FileFeedbackWrapper fileFeedbackWrapper, Document document, PsiElement containingScope) {
        int from = document.getLineNumber(containingScope.getTextRange().getStartOffset());
        int to = document.getLineNumber(containingScope.getTextRange().getEndOffset());

        double totalGlobalAverage = IntStream.rangeClosed(from, to).mapToDouble(i -> fileFeedbackWrapper.getGlobalAverageForLine(i).orElse(0.0)).sum();
        return Optional.of(totalGlobalAverage);
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

    public Optional<Double> getAggregatePerformanceForCurrentScope(FileFeedbackWrapper fileFeedbackWrapper, int line) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;
        assert psiFile instanceof PyFile;
        PyFile pyFile = (PyFile) psiFile;

        Document document = fileDocumentManager.getDocument(file);
        assert document != null;

        // Get the element at the cursor
        PsiElement elementAtCursor = psiFile.findElementAt(caretModel.getOffset());
        assert elementAtCursor != null;

        // Get its parent PyStatementList element (a scope)
        elementAtCursor = PsiTreeUtil.findFirstParent(elementAtCursor, e -> e instanceof PyStatementList);
        assert elementAtCursor != null;

        // Get the element for the current line we're trying to colour
        PsiElement elementAtLine = psiFile.findElementAt(document.getLineStartOffset(line));
        assert elementAtLine != null;

        // Get its parent PyStatementList element (a scope)
        elementAtLine = PsiTreeUtil.findFirstParent(elementAtLine, e -> e instanceof PyStatementList);
        if (elementAtLine == null) {
            return Optional.empty();
        }
        assert elementAtLine != null;

        // If they are the same scope, return a value
        return psiManager.areElementsEquivalent(elementAtCursor, elementAtLine) ? calculateAggregatePerformanceInScope(fileFeedbackWrapper, document,
            elementAtCursor) : Optional.empty();
    }

    public Optional<Double> getAggregatePerformanceForFunction(FileFeedbackWrapper fileFeedbackWrapper, int line) {
        return getAggregatePerformanceForScope(fileFeedbackWrapper, line, PyFunction.class);
    }
}

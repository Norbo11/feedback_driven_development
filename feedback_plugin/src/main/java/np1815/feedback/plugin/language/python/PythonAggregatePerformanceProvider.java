package np1815.feedback.plugin.language.python;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class PythonAggregatePerformanceProvider {

    private static final Logger LOG = Logger.getInstance(PythonAggregatePerformanceProvider.class);

    private final VirtualFile file;
    private final PsiManager psiManager;
    private final FileDocumentManager fileDocumentManager;
    private final CaretModel caretModel;

    public PythonAggregatePerformanceProvider(VirtualFile file, PsiManager psiManager, FileDocumentManager fileDocumentManager, CaretModel caretModel) {
        this.file = file;
        this.psiManager = psiManager;
        this.fileDocumentManager = fileDocumentManager;
        this.caretModel = caretModel;
    }

    private Optional<Double> getAggregatePerformanceForScope(FileFeedbackWrapper fileFeedbackWrapper, PsiElement containingScope) {
        Document document = fileDocumentManager.getDocument(file);

        int from = document.getLineNumber(containingScope.getTextRange().getStartOffset());
        int to = document.getLineNumber(containingScope.getTextRange().getEndOffset());

        double totalGlobalAverage = IntStream.rangeClosed(from, to).mapToDouble(i -> fileFeedbackWrapper.getGlobalAverageForLine(i).orElse(0.0)).sum();
        return Optional.of(totalGlobalAverage);
    }

    public Optional<Double> getAggregatePerformanceForFile(FileFeedbackWrapper fileFeedbackWrapper) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;
        return getAggregatePerformanceForScope(fileFeedbackWrapper, psiFile);
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
        if (elementAtCursor == null) {
            return Optional.empty();
        }
        if (elementAtCursor instanceof PsiWhiteSpace) {
            elementAtCursor = PsiTreeUtil.skipWhitespacesForward(elementAtCursor);
        }

        // Get its parent PyStatementList element (a scope)
        PsiElement cursorParentScope = PsiTreeUtil.findFirstParent(elementAtCursor, e -> e instanceof PyStatementList);
        if (cursorParentScope == null) {
            return Optional.empty();
        }

        // Get the element for the current line we're trying to colour
        PsiElement elementAtLine = psiFile.findElementAt(document.getLineStartOffset(line));
        if (elementAtLine == null) {
            return Optional.empty();
        }
        if (elementAtLine instanceof PsiWhiteSpace) {
            elementAtLine = PsiTreeUtil.skipWhitespacesForward(elementAtLine);
        }

        // Get its parent PyStatementList element (a scope)
        PsiElement lineParentScope = PsiTreeUtil.findFirstParent(elementAtLine, e -> psiManager.areElementsEquivalent(e, cursorParentScope));
        if (lineParentScope == null) {
            return Optional.empty();
        }

        // If they are the same scope, return a value
        return getAggregatePerformanceForScope(fileFeedbackWrapper, cursorParentScope);
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

        return getAggregatePerformanceForScope(fileFeedbackWrapper, containingFunction);
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

        PyFunction function = ((PyFile) psiFile).findTopLevelFunction("branch");
        assert function != null;

        function.accept(new PyRecursiveElementVisitor() {
            @Override
            public void visitPyStatementList(PyStatementList node) {
                super.visitPyStatementList(node);

                LOG.info("In statement list " + node.getText());
            }

            @Override
            public void visitPyIfStatement(PyIfStatement node) {
                super.visitPyIfStatement(node);

                LOG.info("In if statement " + node.getText());
            }
        });

        return null;
    }
}

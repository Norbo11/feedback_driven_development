package np1815.feedback.plugin.language.python;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import np1815.feedback.plugin.language.BranchProbabilityProvider;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;
import com.intellij.openapi.diagnostic.Logger;


import java.util.*;


public class PythonBranchProbabilityProvider implements BranchProbabilityProvider {

    private static final Logger LOG = Logger.getInstance(PythonBranchProbabilityProvider.class);

    private final PsiManager psiManager;
    private final FileDocumentManager fileDocumentManager;
    private final VirtualFile file;

    public PythonBranchProbabilityProvider(VirtualFile file, PsiManager psiManager, FileDocumentManager fileDocumentManager) {
        this.file = file;
        this.psiManager = psiManager;
        this.fileDocumentManager = fileDocumentManager;
    }

    public Map<Integer, Double> getBranchExecutionProbability(FileFeedbackWrapper feedbackWrapper) {
        PsiFile psiFile = psiManager.findFile(file);
        assert psiFile != null;
        assert psiFile instanceof PyFile;
        PyFile pyFile = (PyFile) psiFile;

        Document document = fileDocumentManager.getDocument(file);
        assert document != null;

        Map<Integer, Double> executionProbabilities = new HashMap<Integer, Double>();

        pyFile.accept(new PyRecursiveElementVisitor() {

            // For every if statement, calculate the amount of times that it has been executed
            @Override
            public void visitPyIfStatement(PyIfStatement ifStatement) {
                super.visitPyIfStatement(ifStatement);

                List<PyStatementPart> parts = new ArrayList<>();
                parts.add(ifStatement.getIfPart());
                parts.add(ifStatement.getElsePart());
                Collections.addAll(parts, ifStatement.getElifParts());

                Map<Integer, Integer> partCounts = new HashMap<>();

                for (PyStatementPart part : parts) {
                    if (part != null) {
                        partCounts.put(textOffsetToLineNumber(document, part), getExecutionCountForElement(document, part, feedbackWrapper));
                    }
                }

                int totalCount = partCounts.values().stream().mapToInt(i -> i).sum();

                // If the else part is null, that means that we might never enter any branches of the if statement at all, and thus we should consider the
                // probability relative to the parent of the if statement
                if (ifStatement.getElsePart() == null) {
                    PyElement parent = PsiTreeUtil.getParentOfType(ifStatement, PyElement.class);
                    totalCount = getExecutionCountForElement(document, parent, feedbackWrapper);
                }

                // Compute the execution probability of each if statement branch as a fraction of the total execution count of all branches, as long as
                // at least one of the branches was executed once
                if (totalCount != 0) {
                    for (Map.Entry<Integer, Integer> entry : partCounts.entrySet()) {
                        executionProbabilities.put(entry.getKey(), entry.getValue() / (double) totalCount);
                    }
                }
            }
        });

        return executionProbabilities;
    }

    public static int textOffsetToLineNumber(Document document, PsiElement psiElement) {
        return document.getLineNumber(psiElement.getTextOffset());
    }

    /**
     * Finds the execution count of an element
     * For most elements, the logic here is to take the maximum execution count of all children, because if a child executes X times, then the parent
     * must also have executed that many times
     * For certain other elements, such as if branches, we have to sum the execution counts of all the branches
     * @param document For converting text offsets to line numbers
     * @param pyElement The statement part to analyse
     * @param fileFeedbackWrapper Feedback wrapper from which to get execution counts of lines
     * @return Number of executions or 0 if the given element has no children of the type PyStatement
     */
    private static int getExecutionCountForElement(Document document, PyElement pyElement, FileFeedbackWrapper fileFeedbackWrapper) {
        Collection<PyStatement> statements = PsiTreeUtil.findChildrenOfAnyType(pyElement, PyStatement.class);

        OptionalInt max = statements.stream().mapToInt(s -> {
            if (s instanceof PyIfStatement) {
                PyIfStatement ifStatement = (PyIfStatement) s;
                return getExecutionCountForElement(document, ifStatement.getIfPart(), fileFeedbackWrapper)
                    + getExecutionCountForElement(document, ifStatement.getElsePart(), fileFeedbackWrapper)
                    + Arrays.stream(ifStatement.getElifParts()).mapToInt(elif -> getExecutionCountForElement(document, elif, fileFeedbackWrapper)).sum();
            }

            int lineNumber = document.getLineNumber(s.getTextOffset());
            return fileFeedbackWrapper.getExecutionCount(lineNumber);
        }).max();

        return max.orElse(0);
    }

}

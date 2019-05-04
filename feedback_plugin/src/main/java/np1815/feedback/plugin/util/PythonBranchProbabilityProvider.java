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


public class PythonBranchProbabilityProvider implements BranchProbabilityProvider {

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
        Map<Integer, Integer> ifStatementTotalCount = new HashMap<Integer, Integer>();

        pyFile.accept(new PyRecursiveElementVisitor() {

            @Override
            public void visitPyIfStatement(PyIfStatement ifStatement) {
                super.visitPyIfStatement(ifStatement);

                ArrayList<Integer> partCounts = new ArrayList<>();

                // TODO: This assumes that there exists an execution count on the first child of every branch
                partCounts.add(feedbackWrapper.getExecutionCount(PythonBranchProbabilityProvider.getLineNumberOfChild(document, ifStatement.getIfPart(), PyStatementList.class)));

                for (PyIfPart part : ifStatement.getElifParts()) {
                    partCounts.add(feedbackWrapper.getExecutionCount(PythonBranchProbabilityProvider.getLineNumberOfChild(document, part, PyStatementList.class)));
                }

                if (ifStatement.getElsePart() != null) {
                    partCounts.add(feedbackWrapper.getExecutionCount(PythonBranchProbabilityProvider.getLineNumberOfChild(document, ifStatement.getElsePart(), PyStatementList.class)));
                }

                int totalCount = partCounts.stream().mapToInt(i -> i).sum();

                ifStatementTotalCount.put(ifStatement.getTextOffset(), totalCount);
            }
        });


        for (int line : feedbackWrapper.getLineNumbers()) {
            int offset = document.getLineStartOffset(line);
            PsiElement element = pyFile.findElementAt(offset);
            assert element != null;

            element = PsiTreeUtil.findSiblingForward(element, PyElementTypes.STATEMENT_LIST, e -> {});

            if (element == null) {
                continue;
            }

            PyIfStatement ifStatement = PsiTreeUtil.getParentOfType(element, PyIfStatement.class);

            if (ifStatement != null) {
                executionProbabilities.put(line, feedbackWrapper.getExecutionCount(line) / (double) ifStatementTotalCount.get(ifStatement.getTextOffset()));
            }
        }

        return executionProbabilities;
    }

    private static int getLineNumberOfChild(Document document, PsiElement element, Class<? extends PsiElement> elementClass) {
        return document.getLineNumber(PsiTreeUtil.findChildOfType(element, elementClass).getTextOffset());
    }

}

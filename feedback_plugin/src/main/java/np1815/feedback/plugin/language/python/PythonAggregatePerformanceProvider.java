package np1815.feedback.plugin.language.python;

import com.intellij.find.findUsages.FindMethodUsagesDialog;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.find.findUsages.FindUsagesUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import np1815.feedback.plugin.util.backend.FileFeedbackWrapper;

import java.util.*;
import java.util.stream.Collectors;
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

        try {
            int from = document.getLineNumber(containingScope.getTextRange().getStartOffset());
            int to = document.getLineNumber(containingScope.getTextRange().getEndOffset());

            double totalGlobalAverage = IntStream.rangeClosed(from, to).mapToDouble(i -> fileFeedbackWrapper.getGlobalAverageForLine(i).orElse(0.0)).sum();
            return Optional.of(totalGlobalAverage);
        } catch (IndexOutOfBoundsException e) {
            // TODO: File changed in the middle of the call of this function, should handle this properly
            e.printStackTrace();
            return Optional.empty();
        }
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

        psiFile.accept(new PyRecursiveElementVisitor() {
            @Override
            public void visitPyFunction(PyFunction node) {
                super.visitPyFunction(node);

                LOG.info(node.getName());
            }
        });

//        PyFunction function = ((PyFile) psiFile).findTopLevelFunction("branch");
//        assert function != null;
//
//        PythonBranchProbabilityProvider branchProbabilityProvider = new PythonBranchProbabilityProvider(file, psiManager, fileDocumentManager);
//        List<DistributionEntry> performanceDistributionForElement = getPerformanceDistributionForElement(document, fileFeedbackWrapper, function.getStatementList(), branchProbabilityProvider.getBranchExecutionProbability(fileFeedbackWrapper));
        return null;
    }

    private class DistributionEntry {
        private final double probability;
        private final double sampleTime;

        private DistributionEntry(double probability, double sampleTime) {
            this.probability = probability;
            this.sampleTime = sampleTime;
        }
    }

    private List<DistributionEntry> getPerformanceDistributionForElement(Document document, FileFeedbackWrapper fileFeedbackWrapper, PsiElement element,
                                                                         Map<Integer, Double> branchExecutionProbability) {
        List<DistributionEntry> distribution = new ArrayList<>();

        if (element instanceof PyIfStatement) {
            PyIfStatement ifStatement = (PyIfStatement) element;

            List<DistributionEntry> ifDistribution = getPerformanceDistributionForElement(document, fileFeedbackWrapper, ifStatement.getIfPart(), branchExecutionProbability);
            List<DistributionEntry> elseDistribution = getPerformanceDistributionForElement(document, fileFeedbackWrapper, ifStatement.getElsePart(), branchExecutionProbability);

            double ifProbability = branchExecutionProbability.get(PythonBranchProbabilityProvider.textOffsetToLineNumber(document, ifStatement.getIfPart()));

            for (DistributionEntry entry : ifDistribution) {
                distribution.add(new DistributionEntry(entry.probability * ifProbability, entry.sampleTime));
            }

            if (ifStatement.getElsePart() != null) {
                double elseProbability = branchExecutionProbability.get(PythonBranchProbabilityProvider.textOffsetToLineNumber(document, ifStatement.getElsePart()));

                for (DistributionEntry entry : elseDistribution) {
                    distribution.add(new DistributionEntry(entry.probability * elseProbability, entry.sampleTime));
                }
            }
        }

        else if (element instanceof PyStatementList) {
            PyStatementList statementList = (PyStatementList) element;

            for (int i = 0; i < statementList.getStatements().length; i++) {
                PyStatement statement = statementList.getStatements()[i];
                List<DistributionEntry> currentDistribution = getPerformanceDistributionForElement(document, fileFeedbackWrapper, statement, branchExecutionProbability);

                List<DistributionEntry> newDistribution = new ArrayList<>(distribution);
                for (DistributionEntry de : currentDistribution) {
                    if (distribution.size() > 0) {
                        for (DistributionEntry de2 : distribution) {
                            newDistribution.add(new DistributionEntry(de.probability * de2.probability, de.sampleTime + de2.sampleTime));
                        }
                    } else {
                        newDistribution = currentDistribution;
                    }
                }

                distribution = newDistribution;
            }
        }

        else if (element instanceof PyStatement) {
            int line = PythonBranchProbabilityProvider.textOffsetToLineNumber(document, element);
            Optional<Double> sampleTime = fileFeedbackWrapper.getGlobalAverageForLine(line);

            if (sampleTime.isPresent()) {
                DistributionEntry entry = new DistributionEntry(1.0, sampleTime.get());
                distribution.add(entry);
            }
        }

        else if (element instanceof PyStatementListContainer) {
            PyStatementListContainer pyStatementListContainer = (PyStatementListContainer) element;
            return getPerformanceDistributionForElement(document, fileFeedbackWrapper, pyStatementListContainer.getStatementList(), branchExecutionProbability);
        }

         distribution = distribution.stream().collect(Collectors.groupingBy(de -> de.probability, Collectors.summingDouble(de -> de.sampleTime)))
             .entrySet().stream().map(e -> new DistributionEntry(e.getKey(), e.getValue())).collect(Collectors.toList());

        return distribution;
    }
}

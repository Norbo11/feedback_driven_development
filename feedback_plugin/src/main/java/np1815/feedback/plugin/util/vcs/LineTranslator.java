package np1815.feedback.plugin.util.vcs;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.util.diff.Diff;
import com.intellij.util.diff.FilesTooBigForDiffException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LineTranslator {

    /**
     * Translate lines according to a list of changes.
     * @param changes List of VCS changes - needs to contain a single change of type MODIFICATION
     * @param linesToTranslate List of line numbers in the old revision to translate
     * @return A map of newLineNumber -> oldLineNumber with the same size as linesToTranslate
     */
    public static Map<Integer, TranslatedLineNumber> translateLinesAccordingToChanges(List<Change> changes, Set<Integer> linesToTranslate) throws VcsException {
        List<Change> modifiedChanges = changes.stream().filter(c -> c.getType() == Change.Type.MODIFICATION).collect(Collectors.toList());

        if (modifiedChanges.size() != 1) {
            throw new IllegalArgumentException("Expected one modification in list of changes");
        }

        Change change = modifiedChanges.get(0);
        Map<Integer, TranslatedLineNumber> translatedLineNumbers = new HashMap<>();

        try {
            String before = change.getBeforeRevision().getContent();
            String after = change.getAfterRevision().getContent();
            final Diff.Change c = Diff.buildChanges(before, after);

            // Translate lines based on the change
            for (Integer oldLineNumber : IntStream.range(0, before.split("\n").length).boxed().collect(Collectors.toSet())) {
                int newLineNumber = Diff.translateLine(c, oldLineNumber);
                boolean veryStale = false;

                if (newLineNumber == -1) {
                    newLineNumber = Diff.translateLine(c, oldLineNumber, true);
                    veryStale = true;
                }

                //TODO: Get latest available performance information on a per-line basis, instead of for full file
                if (translatedLineNumbers.containsKey(newLineNumber)) {
                    // Replace an existing mapping only if very stale
                    if (translatedLineNumbers.get(newLineNumber).isVeryStale()) {
                        translatedLineNumbers.put(newLineNumber, new TranslatedLineNumber(oldLineNumber, veryStale, ""));
                    }
                } else {
                    translatedLineNumbers.put(newLineNumber, new TranslatedLineNumber(oldLineNumber, veryStale, ""));
                }
            }
        } catch (FilesTooBigForDiffException ignored) {
        }

        return translatedLineNumbers;
    }

}

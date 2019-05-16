package np1815.feedback.plugin.util.vcs;

import com.google.common.base.Splitter;
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
     * @return A map of newLineNumber -> oldLineNumber with the same size as linesToTranslate
     */
    public static Map<Integer, TranslatedLineNumber> translateLinesAccordingToChanges(List<Change> changes) throws VcsException {
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
            for (Integer oldLineNumber : IntStream.range(0, Splitter.on("\n").splitToList(before).size()).boxed().collect(Collectors.toSet())) {
                int newLineNumber = Diff.translateLine(c, oldLineNumber);

                if (newLineNumber != -1) {
                    if (translatedLineNumbers.containsKey(newLineNumber)) {
                        throw new AssertionError("Should not map to the same line more than once");
                    } else {
                        translatedLineNumbers.put(newLineNumber, new TranslatedLineNumber(oldLineNumber));
                    }
                } else {
                    // TODO: Return a list of line numbers which failed to be translated
                }
            }
        } catch (FilesTooBigForDiffException ignored) {
        }

        return translatedLineNumbers;
    }

}

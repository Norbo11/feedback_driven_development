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
     * @param change A VCS change
     * @return A map of newLineNumber -> oldLineNumber with the same size as linesToTranslate
     */
    public static Map<Integer, TranslatedLineNumber> translateLinesAccordingToChanges(Change change) throws VcsException {
        Map<Integer, TranslatedLineNumber> translatedLineNumbers = new HashMap<>();

        try {
            String before = "";
            String after = "";

            if (change.getType() == Change.Type.MODIFICATION || change.getType() == Change.Type.MOVED) {
                before = change.getBeforeRevision().getContent();
                after = change.getAfterRevision().getContent();
            } else if (change.getType() == Change.Type.NEW) {
                before = change.getAfterRevision().getContent();
                after = change.getAfterRevision().getContent();
            } else if (change.getType() == Change.Type.DELETED) {
                before = change.getBeforeRevision().getContent();
            }

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

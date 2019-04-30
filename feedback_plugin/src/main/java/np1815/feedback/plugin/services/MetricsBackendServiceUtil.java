package np1815.feedback.plugin.services;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.util.diff.Diff;
import com.intellij.util.diff.FilesTooBigForDiffException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsBackendServiceUtil {
    /**
     * Translate the performance data into the new file view according to the edited changes
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
            for (Integer oldLineNumber : linesToTranslate) {
                int newLineNumber = Diff.translateLine(c, oldLineNumber);
                boolean veryStale = false;

                if (newLineNumber == -1) {
                    newLineNumber = Diff.translateLine(c, oldLineNumber, true);
                    veryStale = true;
                }

                //TODO: Get latest available performance information on a per-line basis, instead of for full file
                translatedLineNumbers.put(newLineNumber, new TranslatedLineNumber(oldLineNumber, veryStale, ""));
            }
        } catch (FilesTooBigForDiffException ignored) {
        }

        return translatedLineNumbers;
    }
}

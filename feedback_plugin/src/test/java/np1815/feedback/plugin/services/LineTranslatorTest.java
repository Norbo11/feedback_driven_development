package np1815.feedback.plugin.services;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import np1815.feedback.plugin.util.vcs.LineTranslator;
import np1815.feedback.plugin.util.vcs.TranslatedLineNumber;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.*;
import java.util.*;

public class LineTranslatorTest {

    public void setUp() throws Exception {
    }

    @Test
    public void sameLineChangedIsVeryStale() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('testChanged')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertTrue(map.get(1).isVeryStale());
    }

    @Test
    public void noChange() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertFalse(map.get(1).isVeryStale());
    }

    @Test
    public void insertLineBefore() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('changed');\n" +
            "    print('test')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(2));
        assertEquals("1", map.get(2).getLineNumberBeforeChange());
        assertFalse(map.get(2).isVeryStale());
    }

    @Test
    public void insertLineAfter() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')\n"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')\n" +
            "    print('changed')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertFalse(map.get(1).isVeryStale());
    }

    @Test
    public void multipleLinesInsertedBetweenOtherLines() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test');\n" +
            "    print('test1')"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('tes2');\n" +
            "    print('test');\n" +
            "    print('tes3');\n" +
            "    print('test1')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Arrays.asList(1, 2)));

        assertEquals(2, map.size());
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(4));
        assertEquals("1", map.get(2).getLineNumberBeforeChange());
        assertFalse(map.get(2).isVeryStale());
        assertEquals("2", map.get(4).getLineNumberBeforeChange());
        assertFalse(map.get(4).isVeryStale());
    }

    @Test
    public void whitespaceChangeMeansNotStale() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')\n"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());

        // Whitespace should not be marked as very stale
        assertFalse(map.get(1).isVeryStale());
    }

    @Test
    public void ambigiousTranslationFavoursLatestLine() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('test')\n"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
            "    print('changes1')\n" +
            "    print('test')\n" +
            "    print('changes2')\n" +
            "    print('test')\n"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(4));
        assertEquals("1", map.get(4).getLineNumberBeforeChange());

        // TODO: Maybe have some form of ambiguity flag
        assertFalse(map.get(4).isVeryStale());
    }

    @Test
    public void createMoveDeleteAreIgnored() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change1 = mock(Change.class, RETURNS_DEEP_STUBS);
        Change change2 = mock(Change.class, RETURNS_DEEP_STUBS);
        Change change3 = mock(Change.class, RETURNS_DEEP_STUBS);
        Change change4 = mock(Change.class, RETURNS_DEEP_STUBS);

        // All non-modification changes should be ignored
        when(change1.getType()).thenReturn(Change.Type.DELETED);
        when(change2.getType()).thenReturn(Change.Type.NEW);
        when(change3.getType()).thenReturn(Change.Type.MOVED);
        when(change4.getType()).thenReturn(Change.Type.MODIFICATION);

        when(change4.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
                "    print('test')\n"
        );

        when(change4.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
                "    print('changes1')\n" +
                "    print('test')\n"
        );

        List<Change> changes = Arrays.asList(change1, change2, change3, change4);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Collections.singletonList(1)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(2));
        assertEquals("1", map.get(2).getLineNumberBeforeChange());
        assertFalse(map.get(2).isVeryStale());
    }

    @Test
    public void crossoverLinesFavoursLatestLine() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def f():\n" +
                "    print('1')\n" +
                "    print('2')\n"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def f():\n" +
                "    print('2')\n" +
                "    print('1')\n"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Arrays.asList(1, 2)));

        assertEquals(1, map.size());
        assertTrue(map.containsKey(1));

        assertEquals("2", map.get(1).getLineNumberBeforeChange());
        assertFalse(map.get(1).isVeryStale());
    }

    @Test
    public void farAwayChangeToExistingLine() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def a():\n" +
            "    print('1')\n" +
            "    print('2')\n" +
            "def b():\n" +
            "    print('3')\n" +
            "    print('4')\n" +
            "def c():\n" +
            "    print('5')\n" +
            "    print('6')\n"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def a():\n" +
            "    print('1')\n" +
            "    print('2')\n" +
            "def b():\n" +
            "    print('3')\n" +
            "    print('4')\n" +
            "def c():\n" +
            "    print('1')\n" +
            "    print('6')\n"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Arrays.asList(1, 2, 4, 5, 7, 8)));

        assertEquals(6, map.size());
        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(4));
        assertTrue(map.containsKey(5));
        assertTrue(map.containsKey(7));
        assertTrue(map.containsKey(8));

        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertFalse(map.get(1).isVeryStale());

        assertEquals("2", map.get(2).getLineNumberBeforeChange());
        assertFalse(map.get(2).isVeryStale());

        assertEquals("4", map.get(4).getLineNumberBeforeChange());
        assertFalse(map.get(4).isVeryStale());

        assertEquals("5", map.get(5).getLineNumberBeforeChange());
        assertFalse(map.get(5).isVeryStale());

        assertEquals("7", map.get(7).getLineNumberBeforeChange());
        assertTrue(map.get(7).isVeryStale());

        assertEquals("8", map.get(8).getLineNumberBeforeChange());
        assertFalse(map.get(8).isVeryStale());
    }

    @Test
    public void farAwayChangeToExistingLineWithInsertions() throws VcsException {
        MetricsBackendService service = new MetricsBackendService();

        Change change = mock(Change.class, RETURNS_DEEP_STUBS);

        when(change.getType()).thenReturn(Change.Type.MODIFICATION);
        when(change.getBeforeRevision().getContent()).thenReturn(
            "def a():\n" +
                "    print('1')\n" +
                "    print('2')\n" +
                "def b():\n" +
                "    print('3')\n" +
                "    print('4')\n" +
                "def c():\n" +
                "    print('5')\n" +
                "    print('6')"
        );

        when(change.getAfterRevision().getContent()).thenReturn(
            "def a():\n" +
                "    print('1')\n" +
                "    print('2')\n" +
                "def b():\n" +
                "    print('3')\n" +
                "    print('4')\n" +
                "def c():\n" +
                "    print('6')\n" +
                "    print('1')\n" +
                "    print('6')"
        );

        List<Change> changes = Collections.singletonList(change);

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(
            changes,
            new HashSet<>(Arrays.asList(1, 2, 4, 5, 7, 8)));

        assertEquals(6, map.size());
        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(4));
        assertTrue(map.containsKey(5));
        assertTrue(map.containsKey(7));
        assertTrue(map.containsKey(9));

        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertFalse(map.get(1).isVeryStale());

        assertEquals("2", map.get(2).getLineNumberBeforeChange());
        assertFalse(map.get(2).isVeryStale());

        assertEquals("4", map.get(4).getLineNumberBeforeChange());
        assertFalse(map.get(4).isVeryStale());

        assertEquals("5", map.get(5).getLineNumberBeforeChange());
        assertFalse(map.get(5).isVeryStale());

        assertEquals("7", map.get(7).getLineNumberBeforeChange());
        assertTrue(map.get(7).isVeryStale());

        assertEquals("8", map.get(9).getLineNumberBeforeChange());
        assertFalse(map.get(9).isVeryStale());
    }
}
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
    public void sameLineChangeIsNotTranslated() throws VcsException {
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(1, map.size());
        assertEquals("0", map.get(0).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(2, map.size());
        assertTrue(map.containsKey(1));
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(2, map.size());
        assertTrue(map.containsKey(2));
        assertEquals("1", map.get(2).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(2, map.size());
        assertEquals("0", map.get(0).getLineNumberBeforeChange());
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(3, map.size());
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(4));
        assertEquals("1", map.get(2).getLineNumberBeforeChange());
        assertEquals("2", map.get(4).getLineNumberBeforeChange());
    }

    @Test
    public void whitespaceChangeTranslatesSuccessfully() throws VcsException {
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(2, map.size());
        assertEquals("0", map.get(0).getLineNumberBeforeChange());
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(3, map.size());
        assertTrue(map.containsKey(4));
        assertEquals("1", map.get(4).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        // Two of the original lines map to the same thing. One of them is very stale and therefore gets replaced
        assertEquals(3, map.size());
        assertTrue(map.containsKey(1));

        assertEquals("2", map.get(1).getLineNumberBeforeChange());
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
            "    print('1')\n" +
            "    print('6')"
        );

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(8, map.size());
        assertEquals("0", map.get(0).getLineNumberBeforeChange());
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertEquals("2", map.get(2).getLineNumberBeforeChange());
        assertEquals("3", map.get(3).getLineNumberBeforeChange());
        assertEquals("4", map.get(4).getLineNumberBeforeChange());
        assertEquals("5", map.get(5).getLineNumberBeforeChange());
        assertEquals("6", map.get(6).getLineNumberBeforeChange());
        assertEquals("8", map.get(8).getLineNumberBeforeChange());
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

        Map<Integer, TranslatedLineNumber> map = LineTranslator.translateLinesAccordingToChanges(change);

        assertEquals(8, map.size());
        assertEquals("0", map.get(0).getLineNumberBeforeChange());
        assertEquals("1", map.get(1).getLineNumberBeforeChange());
        assertEquals("2", map.get(2).getLineNumberBeforeChange());
        assertEquals("3", map.get(3).getLineNumberBeforeChange());
        assertEquals("4", map.get(4).getLineNumberBeforeChange());
        assertEquals("5", map.get(5).getLineNumberBeforeChange());
        assertEquals("6", map.get(6).getLineNumberBeforeChange());
        assertEquals("8", map.get(9).getLineNumberBeforeChange());
    }
}
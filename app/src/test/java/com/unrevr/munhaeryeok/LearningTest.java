package com.unrevr.munhaeryeok;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LearningTest {
    @Test public void questionZeroIsAValidSavedQuestion() throws Exception {
        String data = "first|a|b|c|d|2|hint|why;\nsecond|a|b|c|d|1|hint|why;\n";
        List<Problem> parsed = Problem.parse(Problem.MC,
                new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        assertEquals(2, parsed.size());
        assertEquals(0, parsed.get(0).id);
        assertEquals("first", parsed.get(0).question);
        assertTrue(parsed.get(0).checkAnswer(1));
        assertFalse(parsed.get(0).checkAnswer(0));
    }

    @Test public void scoringRetainsLegacyFormulaAndHandlesLargeHistories() {
        assertEquals(0, UserInfo.scoreAfter(0, 0));
        assertEquals(25, UserInfo.scoreAfter(1, 1));
        assertEquals(75, UserInfo.scoreAfter(200, 200));
        assertEquals(100, UserInfo.scoreAfter(Integer.MAX_VALUE, 0));
    }
}

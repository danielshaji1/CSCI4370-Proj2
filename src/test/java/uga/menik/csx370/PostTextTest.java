package uga.menik.csx370;

import java.sql.Timestamp;
import java.util.Set;
import junit.framework.TestCase;
import uga.menik.csx370.utility.PostText;

public class PostTextTest extends TestCase {
    public void testBlankAndOverlongPostsRejected() {
        for (String input : new String[]{"", " \t\n", "\u2003\n", "a".repeat(501)}) {
            try { PostText.required(input, 500, "Post"); fail("Invalid post accepted"); }
            catch (IllegalArgumentException expected) { }
        }
        assertEquals("hello", PostText.required("  hello\n", 500, "Post"));
    }
    public void testCaseInsensitiveDeduplicatedHashtags() {
        assertEquals(Set.of("uga", "2025", "fireworks", "café"),
                PostText.hashtags("#UGA #uga (#2025) #fireworks! #café"));
        assertTrue(PostText.hashtags("no tags and word#not_a_tag").isEmpty());
    }
    public void testLongHashtagRejected() {
        try { PostText.hashtags("#" + "a".repeat(101)); fail(); }
        catch (IllegalArgumentException expected) { }
    }
    public void testTimestampFormatAndUnknown() {
        assertEquals("Mar 07, 2025, 10:54 PM", PostText.date(Timestamp.valueOf("2025-03-07 22:54:00")));
        assertEquals("Unknown", PostText.date(null));
    }
}

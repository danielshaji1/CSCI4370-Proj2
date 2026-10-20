package uga.menik.csx370.utility;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Shared rules for stored content and case-insensitive hashtag matching. */
public final class PostText {
    private static final Pattern TAG = Pattern.compile("(?<![\\p{L}\\p{N}_])#([\\p{L}\\p{N}_]+)");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a", Locale.US);
    private PostText() {}

    public static String required(String text, int limit, String label) {
        String value = text == null ? "" : text.strip();
        if (value.isBlank()) throw new IllegalArgumentException(label + " cannot be empty.");
        if (value.codePointCount(0, value.length()) > limit) {
            throw new IllegalArgumentException(label + " must be at most " + limit + " characters.");
        }
        return value;
    }

    public static Set<String> hashtags(String text) {
        Set<String> tags = new LinkedHashSet<>();
        Matcher matcher = TAG.matcher(text == null ? "" : text);
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase(Locale.ROOT);
            if (tag.codePointCount(0, tag.length()) > 100) {
                throw new IllegalArgumentException("Each hashtag must be at most 100 characters.");
            }
            tags.add(tag);
        }
        return tags;
    }

    public static String date(Timestamp date) {
        return date == null ? "Unknown" : date.toLocalDateTime().format(DISPLAY);
    }
}

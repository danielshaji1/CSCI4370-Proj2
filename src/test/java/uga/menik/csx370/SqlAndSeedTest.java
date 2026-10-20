package uga.menik.csx370;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import uga.menik.csx370.db.SqlQueries;

public class SqlAndSeedTest extends TestCase {
    public void testDmlContainsEveryExactApplicationStatement() throws Exception {
        String catalog = Files.readString(Path.of("dml.sql")).replaceAll("\\s+", " ");
        for (var field : SqlQueries.class.getFields()) {
            String sql = ((String) field.get(null)).replace("%s", "?, ?").replaceAll("\\s+", " ").strip();
            assertTrue("Missing SQL: " + field.getName(), catalog.contains(sql + ";"));
        }
    }
    public void testDemoPasswordHashesMatchDocumentedPassword() throws Exception {
        String seed = Files.readString(Path.of("database_setup.sql"));
        var matches = Pattern.compile("'(?:alex|jordan|taylor|avery)',\\s*'(\\$2a\\$10\\$[./A-Za-z0-9]{53})'").matcher(seed);
        int count = 0;
        var encoder = new BCryptPasswordEncoder();
        while (matches.find()) { assertTrue(encoder.matches("password", matches.group(1))); count++; }
        assertTrue("At least the three original demo accounts should be present", count >= 3);
    }
}

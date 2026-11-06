package patterns.lia.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateTimeUtil() {
    }

    public static LocalDateTime parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(text.trim().replace(' ', 'T'));
    }

    public static String format(LocalDateTime value) {
        return value == null ? null : value.format(DB_FORMAT);
    }
}

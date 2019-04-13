package np1815.feedback.metricsbackend.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeUtil {
    public static Timestamp dateTimeToTimestamp(LocalDateTime startTimestamp) {
        return Timestamp.from(startTimestamp.toInstant(ZoneOffset.UTC));
    }
}

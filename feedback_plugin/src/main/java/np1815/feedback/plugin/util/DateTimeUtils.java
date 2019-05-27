package np1815.feedback.plugin.util;

import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class DateTimeUtils {
    private static PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendDays().appendSuffix("day", "days")
        .appendHours().appendSuffix("hour", "hours")
        .appendMinutes().appendSuffix("min", "mins")
        .appendSeconds().appendSuffix("sec", "secs")
        .toFormatter();

    public static Optional<LocalDateTime> parseDateTimeString(String string) {
        if (string.contains("-")) {
            String[] parts = string.split("-");

            try {
                long duration = formatter.parsePeriod(parts[1]).toStandardDuration().getMillis();
                return parseDateTimeString(parts[0]).map(dt -> dt.minus(duration, ChronoUnit.MILLIS));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        if (string.contains("+")) {
            String[] parts = string.split("\\+");

            try {
                long duration = formatter.parsePeriod(parts[1]).toStandardDuration().getMillis();
                return parseDateTimeString(parts[0]).map(dt -> dt.plus(duration, ChronoUnit.MILLIS));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        if (string.equalsIgnoreCase("now")) {
            return Optional.of(LocalDateTime.now(ZoneId.of("UTC")));
        }

        return Optional.of(LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(string)));
    }
}

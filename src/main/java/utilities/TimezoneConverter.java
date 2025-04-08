package utilities;

import java.time.LocalDateTime;

/**
 * Functional interface for converting times between timezones.
 */
@FunctionalInterface
public interface TimezoneConverter {
    /**
     * Converts a LocalDateTime from one timezone to another.
     *
     * @param dateTime the LocalDateTime to convert
     * @return the converted LocalDateTime
     */
    LocalDateTime convert(LocalDateTime dateTime);
}

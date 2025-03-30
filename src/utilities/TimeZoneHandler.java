package utilities;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Handles timezone operations for the calendar application.
 * Provides methods for converting times between different timezones
 * and validating timezone formats. All internal storage is in UTC.
 */
public class TimeZoneHandler {

  private static final String DEFAULT_TIMEZONE = "America/New_York";
  private static final String UTC_TIMEZONE = "UTC";

  /**
   * Validates if the provided timezone string is valid.
   * Checks if the timezone is a valid IANA timezone identifier.
   *
   * @param timezone the timezone to validate
   * @return true if valid, false otherwise
   */
  public boolean isValidTimezone(String timezone) {
    if (timezone == null || timezone.trim().isEmpty()) {
      return false;
    }

    try {
      ZoneId.of(timezone);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the default timezone.
   *
   * @return the default timezone string
   */
  public String getDefaultTimezone() {
    return DEFAULT_TIMEZONE;
  }

  /**
   * Gets the system's default timezone.
   *
   * @return the system's default timezone ID
   */
  public String getSystemDefaultTimezone() {
    return java.util.TimeZone.getDefault().getID();
  }

  /**
   * Converts a LocalDateTime from a specific timezone to UTC.
   *
   * @param dateTime     the LocalDateTime to convert
   * @param fromTimezone the source timezone
   * @return the UTC LocalDateTime
   */
  public LocalDateTime convertToUTC(LocalDateTime dateTime, String fromTimezone) {
    if (dateTime == null || !isValidTimezone(fromTimezone)) {
      throw new IllegalArgumentException("Invalid parameters for time conversion");
    }

    ZonedDateTime sourceZoned = dateTime.atZone(ZoneId.of(fromTimezone));
    ZonedDateTime utcZoned = sourceZoned.withZoneSameInstant(ZoneId.of(UTC_TIMEZONE));
    return utcZoned.toLocalDateTime();
  }

  /**
   * Converts a UTC LocalDateTime to a specific timezone.
   *
   * @param dateTime   the UTC LocalDateTime to convert
   * @param toTimezone the target timezone
   * @return the LocalDateTime in the target timezone
   */
  public LocalDateTime convertFromUTC(LocalDateTime dateTime, String toTimezone) {
    if (dateTime == null || !isValidTimezone(toTimezone)) {
      throw new IllegalArgumentException("Invalid parameters for time conversion");
    }

    ZonedDateTime utcZoned = dateTime.atZone(ZoneId.of(UTC_TIMEZONE));
    ZonedDateTime targetZoned = utcZoned.withZoneSameInstant(ZoneId.of(toTimezone));
    return targetZoned.toLocalDateTime();
  }

  /**
   * Converts a LocalDateTime from one timezone to another.
   * This method first converts to UTC, then to the target timezone.
   *
   * @param dateTime     the LocalDateTime to convert
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @return the converted LocalDateTime
   */
  public LocalDateTime convertTime(LocalDateTime dateTime, String fromTimezone, String toTimezone) {
    if (dateTime == null || !isValidTimezone(fromTimezone) || !isValidTimezone(toTimezone)) {
      throw new IllegalArgumentException("Invalid parameters for time conversion");
    }

    // First convert to UTC
    LocalDateTime utcTime = convertToUTC(dateTime, fromTimezone);
    // Then convert from UTC to target timezone
    return convertFromUTC(utcTime, toTimezone);
  }

  /**
   * Gets a timezone converter for converting between two timezones.
   * The converter will first convert to UTC, then to the target timezone.
   *
   * @param fromTimezone the source timezone
   * @param toTimezone   the target timezone
   * @return a TimezoneConverter for the specified conversion
   */
  public TimezoneConverter getConverter(String fromTimezone, String toTimezone) {
    return dateTime -> convertTime(dateTime, fromTimezone, toTimezone);
  }
}
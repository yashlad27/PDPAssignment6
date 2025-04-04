package model.calendar.timezone;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * Implementation of ITimezoneService that handles timezone conversions.
 * This consolidated class merges functionality from both TimezoneService and TimeZoneHandler
 * to provide a unified timezone management solution.
 */
public class TimezoneService implements ITimezoneService {
  private static final String UTC = "UTC";
  private static final String DEFAULT_TIMEZONE = "America/New_York";

  @Override
  public LocalDateTime convertToUTC(LocalDateTime dateTime, String timezone) {
    if (dateTime == null || timezone == null) {
      throw new IllegalArgumentException("DateTime and timezone cannot be null");
    }

    ZoneId sourceZone = ZoneId.of(timezone);
    ZoneId utcZone = ZoneId.of(UTC);

    ZonedDateTime zonedDateTime = dateTime.atZone(sourceZone);
    ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(utcZone);

    return utcDateTime.toLocalDateTime();
  }

  @Override
  public LocalDateTime convertFromUTC(LocalDateTime dateTime, String timezone) {
    if (dateTime == null || timezone == null) {
      throw new IllegalArgumentException("DateTime and timezone cannot be null");
    }

    ZoneId utcZone = ZoneId.of(UTC);
    ZoneId targetZone = ZoneId.of(timezone);

    ZonedDateTime utcZonedDateTime = dateTime.atZone(utcZone);
    ZonedDateTime targetDateTime = utcZonedDateTime.withZoneSameInstant(targetZone);

    return targetDateTime.toLocalDateTime();
  }

  @Override
  public String[] getAvailableTimezones() {
    return TimeZone.getAvailableIDs();
  }

  @Override
  public String getDefaultTimezone() {
    return DEFAULT_TIMEZONE;
  }

  /**
   * Gets the system's default timezone.
   *
   * @return the system's default timezone ID
   */
  public String getSystemDefaultTimezone() {
    return TimeZone.getDefault().getID();
  }

  @Override
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

  /**
   * Interface for timezone conversion functions.
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
} 
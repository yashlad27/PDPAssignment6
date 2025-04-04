package model.calendar.timezone;

import java.time.LocalDateTime;

/**
 * Interface for timezone-related operations.
 */
public interface ITimezoneService {
  /**
   * Checks if a timezone is valid.
   *
   * @param timezone the timezone to check
   * @return true if valid, false otherwise
   */
  boolean isValidTimezone(String timezone);

  /**
   * Gets the default timezone.
   *
   * @return the default timezone
   */
  String getDefaultTimezone();

  /**
   * Gets the system's default timezone.
   *
   * @return the system's default timezone ID
   */
  String getSystemDefaultTimezone();

  /**
   * Gets all available timezones.
   *
   * @return array of timezone IDs
   */
  String[] getAvailableTimezones();

  /**
   * Converts a local date/time to UTC.
   *
   * @param dateTime the date/time to convert
   * @param timezone the source timezone ID
   * @return the UTC date/time
   */
  LocalDateTime convertToUTC(LocalDateTime dateTime, String timezone);

  /**
   * Converts a UTC date/time to local time.
   *
   * @param dateTime the UTC date/time
   * @param timezone the target timezone ID
   * @return the local date/time
   */
  LocalDateTime convertFromUTC(LocalDateTime dateTime, String timezone);
} 
package utilities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utility class for date and time operations.
 */
public class DateTimeUtil {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
          "yyyy-MM-dd'T'HH:mm");

  /**
   * Private constructor to prevent instantiation.
   */
  private DateTimeUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Parses a date string in the format "YYYY-MM-DD".
   *
   * @param dateStr the date string to parse
   * @return the parsed LocalDate
   * @throws IllegalArgumentException if the date string is invalid
   */
  public static LocalDate parseDate(String dateStr) {
    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format: " + dateStr
              + ". Expected format: YYYY-MM-DD", e);
    }
  }

  /**
   * Parses a time string in the format "HH:MM".
   *
   * @param timeStr the time string to parse
   * @return the parsed LocalTime
   * @throws IllegalArgumentException if the time string is invalid
   */
  public static LocalTime parseTime(String timeStr) {
    try {
      return LocalTime.parse(timeStr, TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid time format: " + timeStr +
              ". Expected format: HH:MM", e);
    }
  }

  /**
   * Parses a date and time string in the format "YYYY-MM-DDThh:mm" or "YYYY-MM-DDThh:mm:ss".
   *
   * @param dateTimeStr the date and time string to parse
   * @return the parsed LocalDateTime
   * @throws IllegalArgumentException if the date and time string is invalid
   */
  public static LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      try {
        DateTimeFormatter withSecondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"
                + "'T'HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, withSecondsFormatter);
      } catch (DateTimeParseException e2) {
        throw new IllegalArgumentException("Invalid date time format: " + dateTimeStr +
                ". Expected format: YYYY-MM-DDThh:mm or YYYY-MM-DDThh:mm:ss", e2);
      }
    }
  }

  /**
   * Converts a string of weekday characters to a set of DayOfWeek. Uses 'M' for Monday, 'T' for
   * Tuesday, 'W' for Wednesday, 'R' for Thursday, 'F' for Friday, 'S' for Saturday, and 'U' for
   * Sunday.
   *
   * @param weekdaysStr the string of weekday characters
   * @return a set of DayOfWeek
   * @throws IllegalArgumentException if the string contains invalid characters
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdaysStr) {
    if (weekdaysStr == null || weekdaysStr.isEmpty()) {
      throw new IllegalArgumentException("Weekdays string cannot be null or empty");
    }

    Set<DayOfWeek> weekdays = EnumSet.noneOf(DayOfWeek.class);

    for (char c : weekdaysStr.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          weekdays.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          weekdays.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          weekdays.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          weekdays.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          weekdays.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          weekdays.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          weekdays.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }

    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException("No valid weekdays found in string: " + weekdaysStr);
    }

    return weekdays;
  }
}
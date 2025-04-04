package utilities;

import java.time.format.DateTimeFormatter;

import model.event.Event;

/**
 * Utility class for formatting events consistently across the application.
 */
public class EventFormatter {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Formats an event into a human-readable string.
   *
   * @param event the event to format
   * @return formatted string representation of the event
   */
  public static String formatEvent(Event event) {
    StringBuilder sb = new StringBuilder();
    sb.append(event.getSubject());

    if (event.isAllDay()) {
      sb.append(" (All Day)");
    } else {
      sb.append(" from ").append(formatTime(event.getStartDateTime()))
              .append(" to ").append(formatTime(event.getEndDateTime()));
    }

    if (event.getDescription() != null) {
      sb.append("\n  Description: ").append(event.getDescription());
    }
    if (event.getLocation() != null) {
      sb.append("\n  Location: ").append(event.getLocation());
    }
    sb.append("\n  ").append(event.isPublic() ? "Public" : "Private").append("\n");

    return sb.toString();
  }

  /**
   * Formats a date into a string.
   *
   * @param date the date to format
   * @return formatted date string
   */
  public static String formatDate(java.time.LocalDate date) {
    return date.format(DATE_FORMATTER);
  }

  /**
   * Formats a date/time into a time string.
   *
   * @param dateTime the date/time to format
   * @return formatted time string
   */
  public static String formatTime(java.time.LocalDateTime dateTime) {
    return dateTime.format(TIME_FORMATTER);
  }
} 
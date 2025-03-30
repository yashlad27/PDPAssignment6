package model.export;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import model.event.Event;

/**
 * Implementation of IDataExporter that handles CSV format exports.
 * This class provides functionality to convert Event objects into CSV format
 * for data persistence and interoperability with other applications.
 */
public class CSVExporter implements IDataExporter {

  /**
   * Date formatter used to format just the date portion (YYYY-MM-DD) of date-time values.
   * This is used for CSV column values that require only date information.
   */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Time formatter used to format just the time portion (HH:MM) of date-time values.
   * This is used for CSV column values that require only time information.
   */
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Date-time formatter used to format complete date-time values (YYYY-MM-DDTHH:MM).
   * This is used for formatting date-time values in ISO-8601-like format.
   */
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-"
          + "MM-dd'T'HH:mm");

  @Override
  public String export(String filePath, List<Event> events) throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day," +
              "Description,Location,Public\n");

      events.stream()
              .map(this::formatEventForCSV)
              .forEach(line -> {
                try {
                  writer.write(line);
                } catch (IOException e) {
                  throw new RuntimeException("Failed to write event to CSV", e);
                }
              });
    }
    return filePath;
  }

  @Override
  public String formatForDisplay(List<Event> events, boolean showDetails) {
    if (events == null || events.isEmpty()) {
      return "No events found.";
    }

    return events.stream()
            .map(event -> formatEventForDisplay(event, showDetails))
            .collect(Collectors.joining("\n"));
  }

  /**
   * Formats a single event as a CSV row.
   * This method generates a properly formatted CSV string for an event, with
   * values properly escaped according to CSV standards.
   *
   * @param event the event to format as a CSV row
   * @return a CSV-formatted string representing the event, ending with a newline
   */
  private String formatEventForCSV(Event event) {
    return String.format("%s,%s,%s,%s,%s,%b,%s,%s,%b\n",
            escapeCSV(event.getSubject()),
            event.getStartDateTime().format(DATE_FORMATTER),
            event.getStartDateTime().format(TIME_FORMATTER),
            event.getEndDateTime().format(DATE_FORMATTER),
            event.getEndDateTime().format(TIME_FORMATTER),
            event.isAllDay(),
            escapeCSV(event.getDescription()),
            escapeCSV(event.getLocation()),
            event.isPublic());
  }

  /**
   * Formats a single event for display in a human-readable format.
   * This method generates a string representation of an event with basic information
   * and optional details depending on the showDetails parameter.
   *
   * @param event       the event to format for display
   * @param showDetails whether to include detailed information like description,location & privacy
   * @return a formatted string representation of the event
   */
  private String formatEventForDisplay(Event event, boolean showDetails) {
    StringBuilder display = new StringBuilder();

    display.append(event.getSubject());

    if (event.isAllDay()) {
      display.append(" (All Day)");
    } else {
      display.append(" from ")
              .append(event.getStartDateTime().format(TIME_FORMATTER))
              .append(" to ")
              .append(event.getEndDateTime().format(TIME_FORMATTER));
    }

    if (showDetails) {
      String description = event.getDescription();
      if (description != null && !description.trim().isEmpty()) {
        display.append("\n  Description: ").append(description);
      }
      String location = event.getLocation();
      if (location != null && !location.trim().isEmpty()) {
        display.append("\n  Location: ").append(location);
      } else {
        display.append("\n  Location: N/A");
      }
      if (!event.isPublic()) {
        display.append("\n  Private");
      }
    }

    return display.toString();
  }

  /**
   * Escapes a string value for CSV format.
   * This method handles special characters in CSV:
   * - If the value contains commas, double quotes, or newlines, it wraps the value in quotes
   * - Any existing double quotes are escaped by doubling them
   * - Null values are converted to empty strings
   *
   * @param value the string value to escape for CSV format
   * @return the properly escaped CSV value
   */
  private String escapeCSV(String value) {
    if (value == null) {
      return "";
    }

    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}
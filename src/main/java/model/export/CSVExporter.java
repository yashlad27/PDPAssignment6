package model.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
   * Imports events from a CSV file.
   *
   * @param file the CSV file to import from
   * @return a list of imported events
   * @throws IOException if there is an error reading the file
   */
  public List<Event> importEvents(File file) throws IOException {
    List<Event> events = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      // Skip header line
      String header = reader.readLine();
      if (header == null || !header.startsWith("Subject,Start Date,Start Time")) {
        throw new IOException("Invalid CSV format");
      }

      String line;
      while ((line = reader.readLine()) != null) {
        try {
          Event event = parseEventFromCSV(line);
          if (event != null) {
            events.add(event);
          }
        } catch (DateTimeParseException e) {
          throw new IOException("Invalid date/time format in CSV: " + e.getMessage());
        }
      }
    }
    return events;
  }

  /**
   * Exports events to a CSV file.
   *
   * @param events the list of events to export
   * @param file   the file to export to
   * @throws IOException if there is an error writing to the file
   */
  public void exportEvents(List<Event> events, File file) throws IOException {
    export(file.getAbsolutePath(), events);
  }

  /**
   * Parses a single event from a CSV line.
   *
   * @param line the CSV line to parse
   * @return the parsed event, or null if parsing fails
   */
  private Event parseEventFromCSV(String line) {
    String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    if (parts.length < 9) {
      return null;
    }

    try {
      String subject = unescapeCSV(parts[0]);
      LocalDate startDate = LocalDate.parse(parts[1], DATE_FORMATTER);
      LocalTime startTime = LocalTime.parse(parts[2], TIME_FORMATTER);
      LocalDate endDate = LocalDate.parse(parts[3], DATE_FORMATTER);
      LocalTime endTime = LocalTime.parse(parts[4], TIME_FORMATTER);
      String description = unescapeCSV(parts[6]);
      String location = unescapeCSV(parts[7]);
      boolean isPublic = Boolean.parseBoolean(parts[8]);

      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      return new Event(subject, startDateTime, endDateTime, description, location, isPublic);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Unescapes a CSV value.
   *
   * @param value the CSV value to unescape
   * @return the unescaped value
   */
  private String unescapeCSV(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    // Remove surrounding quotes if present
    if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }

    // Replace escaped quotes with single quotes
    return value.replace("\"\"", "\"");
  }

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
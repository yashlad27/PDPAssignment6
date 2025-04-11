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
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import model.event.Event;
import utilities.TimeZoneHandler;

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
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty");
    }
    if (events == null) {
      throw new IllegalArgumentException("Events list cannot be null");
    }

    File file = new File(filePath);
    ensureDirectoryExists(file.getParentFile());

    try (FileWriter writer = new FileWriter(file)) {
      // Write header
      writer.write(getHeaderLine());
      writer.write("\n");

      // Write events
      for (Event event : events) {
        writer.write(formatEventAsCSV(event));
        writer.write("\n");
      }
    }

    return filePath;
  }

  /**
   * Formats a list of events for display with proper timezone conversion.
   *
   * @param events        the list of events to format
   * @param includeHeader whether to include a header row
   * @param timezone      the timezone to display times in
   * @return a formatted string representing the events
   */
  public String formatForDisplay(List<Event> events, boolean includeHeader, String timezone) {
    if (events == null || events.isEmpty()) {
      return "No events found.";
    }

    StringBuilder builder = new StringBuilder();
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();

    // Sort events by start time
    events.sort(Comparator.comparing(Event::getStartDateTime));

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    for (Event event : events) {
      // Convert from UTC to the calendar's timezone for display
      LocalDateTime localStartTime = timezoneHandler.convertFromUTC(event.getStartDateTime(), timezone);
      LocalDateTime localEndTime = timezoneHandler.convertFromUTC(event.getEndDateTime(), timezone);
      
      String startTime = localStartTime.format(timeFormatter);
      String endTime = localEndTime.format(timeFormatter);
      
      builder.append(event.getSubject());
      
      // Handle all-day events differently
      if (event.isAllDay()) {
        builder.append(" (All Day)");
      } else {
        builder.append(" from ").append(startTime)
               .append(" to ").append(endTime);
      }
      builder.append("\n");
      
      // Add location if present
      if (includeHeader || event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
        builder.append("  Location: ");
        if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
          builder.append(event.getLocation());
        } else {
          builder.append("N/A");
        }
        builder.append("\n");
      }
    }

    return builder.toString();
  }

  /**
   * Format events for display, using the default timezone.
   *
   * @param events        the list of events to format
   * @param includeHeader whether to include a header row
   * @return a formatted string representing the events
   */
  public String formatForDisplay(List<Event> events, boolean includeHeader) {
    // Use system default timezone as fallback
    return formatForDisplay(events, includeHeader, TimeZone.getDefault().getID());
  }

  private String getHeaderLine() {
    return String.join(",", "Subject", "Start Date", "Start Time", "End Date", "End Time",
            "Description", "Location", "Is Public");
  }

  private String formatEventAsCSV(Event event) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    String startDate = event.getStartDateTime().format(dateFormatter);
    String startTime = event.getStartDateTime().format(timeFormatter);

    String endDate = event.getEndDateTime().format(dateFormatter);
    String endTime = event.getEndDateTime().format(timeFormatter);

    String description = event.getDescription() != null ? escapeCSV(event.getDescription()) : "";
    String location = event.getLocation() != null ? escapeCSV(event.getLocation()) : "";

    return String.join(",",
            escapeCSV(event.getSubject()),
            startDate,
            startTime,
            endDate,
            endTime,
            description,
            location,
            String.valueOf(event.isPublic()));
  }

  private String escapeCSV(String field) {
    if (field == null) {
      return "";
    }
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
      return "\"" + field.replace("\"", "\"\"") + "\"";
    }
    return field;
  }

  private String formatEventForDisplay(Event event, boolean showDetails) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    StringBuilder builder = new StringBuilder();

    builder.append(event.getSubject())
            .append(" from ")
            .append(event.getStartDateTime().format(timeFormatter))
            .append(" to ")
            .append(event.getEndDateTime().format(timeFormatter));

    if (showDetails) {
      builder.append("\n  Location: ");
      if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
        builder.append(event.getLocation());
      } else {
        builder.append("N/A");
      }
    }

    return builder.toString();
  }

  private void ensureDirectoryExists(File directory) throws IOException {
    if (directory != null && !directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
      }
    }
  }
}
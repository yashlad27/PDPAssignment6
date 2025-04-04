package model.export;

import java.io.IOException;
import java.util.List;

import model.event.Event;

/**
 * Interface defining the contract for exporting calendar data in various formats.
 * This interface allows for different export implementations (CSV, JSON, etc.)
 * while keeping the calendar model independent of specific export formats.
 */
public interface IDataExporter {
  /**
   * Exports a list of events to a file in the specified format.
   *
   * @param filePath the path where the file should be created
   * @param events   the list of events to export
   * @return the path of the created file
   * @throws IOException if there is an error creating or writing to the file
   */
  String export(String filePath, List<Event> events) throws IOException;

  /**
   * Formats events for display in a human-readable format.
   *
   * @param events      the list of events to format
   * @param showDetails whether to include detailed information
   * @return a formatted string representation of the events
   */
  String formatForDisplay(List<Event> events, boolean showDetails);
} 
package controller.command.event;

import java.io.File;
import java.io.IOException;
import java.util.List;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.export.CSVExporter;

/**
 * Command for importing events to the calendar from a CSV file.
 */
public class ImportCalendarCommand implements ICommand {

  private final ICalendar calendar;
  private final CSVExporter csvExporter;

  /**
   * Constructs a new ImportCalendarCommand.
   *
   * @param calendar the calendar model
   */
  public ImportCalendarCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.csvExporter = new CSVExporter();
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Missing filename for import command";
    }

    String filePath = args[0];
    File file = new File(filePath);

    if (!file.exists()) {
      return "Error: File not found: " + filePath;
    }

    try {
      // Import events from the CSV file
      List<Event> importedEvents = csvExporter.importEvents(file);

      if (importedEvents.isEmpty()) {
        return "No events found in the CSV file";
      }

      // Add each imported event to the calendar
      int successCount = 0;
      for (Event event : importedEvents) {
        try {
          boolean added = calendar.addEvent(event, true); // Set autoDecline to true
          if (added) {
            successCount++;
          }
        } catch (Exception e) {
          // Continue with the next event if one fails
          System.err.println("Failed to add event '" + event.getSubject() + "': " + e.getMessage());
        }
      }

      return successCount + " events imported successfully from " + file.getName();
    } catch (IOException e) {
      return "Failed to import calendar: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "import";
  }
}

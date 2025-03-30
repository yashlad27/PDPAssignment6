package controller.command.event;

import java.io.IOException;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.export.CSVExporter;

/**
 * Command for exporting the calendar to a CSV file.
 */
public class ExportCalendarCommand implements ICommand {

  private final ICalendar calendar;
  private final CSVExporter csvExporter;

  /**
   * Constructs a new ExportCalendarCommand.
   *
   * @param calendar the calendar model
   */
  public ExportCalendarCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.csvExporter = new CSVExporter();
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Missing filename for export command";
    }

    String filePath = args[0];

    try {
      String absolutePath = calendar.exportData(filePath, csvExporter);
      return "Calendar exported successfully to: " + absolutePath;
    } catch (IOException e) {
      return "Failed to export calendar: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "export";
  }
}
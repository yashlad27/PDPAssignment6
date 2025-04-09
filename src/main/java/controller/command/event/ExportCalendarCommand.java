package controller.command.event;

import java.io.File;
import java.io.IOException;
import java.util.List;

import controller.command.ICommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.export.CSVExporter;
import viewmodel.ExportImportViewModel;
import view.IGUIView;

/**
 * Command for exporting the calendar to a CSV file.
 */
public class ExportCalendarCommand implements ICommand {

  private final ICalendar calendar;
  private final CSVExporter csvExporter;
  private ExportImportViewModel viewModel;
  private IGUIView view;

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
  
  /**
   * Constructs a new ExportCalendarCommand with a view model and view.
   *
   * @param calendar the calendar model
   * @param viewModel the export/import view model
   * @param view the GUI view
   */
  public ExportCalendarCommand(ICalendar calendar, ExportImportViewModel viewModel, IGUIView view) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
    this.csvExporter = new CSVExporter();
    this.viewModel = viewModel;
    this.view = view;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Missing filename for export command";
    }

    String filePath = args[0];
    File file = new File(filePath);
    
    return exportToFile(file);
  }
  
  /**
   * Exports calendar events to a file directly.
   * 
   * @param file the file to export to
   * @return status message
   */
  public String exportToFile(File file) {
    try {
      // If view model is available, use it for exporting
      if (viewModel != null) {
        viewModel.setCurrentCalendar(calendar);
        viewModel.exportToCSV(file);
        return "Calendar exported successfully to: " + file.getAbsolutePath();
      }
      
      // Otherwise, fall back to direct exporting
      List<Event> events = calendar.getAllEvents();
      csvExporter.exportEvents(events, file);
      
      return "Calendar exported successfully to: " + file.getAbsolutePath();
    } catch (IOException e) {
      return "Failed to export calendar: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "export";
  }
}
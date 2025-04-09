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
 * Command for importing events to the calendar from a CSV file.
 */
public class ImportCalendarCommand implements ICommand {

  private final ICalendar calendar;
  private final CSVExporter csvExporter;
  private ExportImportViewModel viewModel;
  private IGUIView view;

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
  
  /**
   * Constructs a new ImportCalendarCommand with a view model and view.
   *
   * @param calendar the calendar model
   * @param viewModel the export/import view model
   * @param view the GUI view
   */
  public ImportCalendarCommand(ICalendar calendar, ExportImportViewModel viewModel, IGUIView view) {
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
      return "Error: Missing filename for import command";
    }

    String filePath = args[0];
    File file = new File(filePath);

    if (!file.exists()) {
      return "Error: File not found: " + filePath;
    }
    
    return importFromFile(file);
  }
  
  /**
   * Imports events from a file directly.
   * 
   * @param file the file to import from
   * @return status message
   */
  public String importFromFile(File file) {
    try {
      // If view model is available, use it for importing
      if (viewModel != null) {
        viewModel.setCurrentCalendar(calendar);
        int successCount = viewModel.importFromCSV(file);
        
        if (successCount == 0) {
          return "No events were imported";
        }
        
        return "Successfully imported " + successCount + " events";
      }
      
      // Otherwise, fall back to direct importing
      List<Event> importedEvents = csvExporter.importEvents(file);

      if (importedEvents.isEmpty()) {
        return "No events found in the CSV file";
      }

      int successCount = 0;
      for (Event event : importedEvents) {
        try {
          // Add each event to the calendar
          boolean added = calendar.addEvent(event, true); // Set autoDecline to true
          if (added) {
            successCount++;
          }
        } catch (Exception e) {
          // Log the error but continue processing
          System.err.println("Error adding event: " + e.getMessage());
        }
      }

      if (successCount == 0) {
        return "Failed to import any events";
      }
      
      // Update view if available
      if (view != null) {
        view.getCalendarPanel().updateCalendar(calendar);
      }

      return "Successfully imported " + successCount + " events";
    } catch (IOException e) {
      return "Error importing events: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "import";
  }
}

package controller;

import java.io.File;
import java.util.List;

import model.calendar.ICalendar;
import model.event.Event;
import model.export.CSVExporter;
import view.GUIView;

/**
 * Controller focused specifically on import and export operations.
 * Part of refactoring for SOLID principles - Single Responsibility.
 */
public class ImportExportController {
  private final GUIView view;
  private ICalendar currentCalendar;

  /**
   * Constructs a new ImportExportController.
   *
   * @param view the GUI view
   */
  public ImportExportController(GUIView view) {
    this.view = view;
    this.currentCalendar = null;
  }

  /**
   * Constructs a new ImportExportController with a specific calendar.
   *
   * @param calendar the calendar to use
   * @param view     the GUI view
   */
  public ImportExportController(ICalendar calendar, GUIView view) {
    System.out.println("[DEBUG] Creating ImportExportController with calendar: " +
            (calendar != null ? calendar.getName() : "null"));
    this.view = view;
    this.currentCalendar = calendar;
  }

  /**
   * Sets the current calendar.
   *
   * @param calendar the calendar to set as current
   */
  public void setCurrentCalendar(ICalendar calendar) {
    this.currentCalendar = calendar;
  }

  /**
   * Gets the current calendar.
   *
   * @return the current calendar
   */
  public ICalendar getCurrentCalendar() {
    return currentCalendar;
  }

  /**
   * Handles importing a calendar from a file.
   *
   * @param file the file to import from
   */
  public void onImport(File file) {
    if (file == null || !file.exists()) {
      view.showErrorMessage("Invalid file selected");
      return;
    }

    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }

    // Simply display the message - no try-catch needed for a basic UI message
    view.displayMessage("Import functionality is not yet implemented.");
  }

  /**
   * Handles exporting a calendar to a file.
   *
   * @param file the file to export to
   */
  public void onExport(File file) {
    System.out.println("[DEBUG] ImportExportController.onExport called with file: " +
            (file != null ? file.getAbsolutePath() : "null"));

    if (file == null) {
      view.showErrorMessage("Invalid file selected");
      return;
    }

    if (currentCalendar == null) {
      System.out.println("[DEBUG] No calendar selected for export");
      view.showErrorMessage("Please select a calendar first");
      return;
    }

    try {
      String filePath = file.getPath();
      if (!filePath.toLowerCase().endsWith(".csv")) {
        filePath += ".csv";
        System.out.println("[DEBUG] Added .csv extension to file path: " + filePath);
      }

      // Get events from the calendar
      List<Event> events = currentCalendar.getAllEvents();
      System.out.println("[DEBUG] Retrieved " + events.size() + " events from calendar " +
              currentCalendar.getName() + " for export");

      // Create the exporter and export the events
      CSVExporter exporter = new CSVExporter();
      File exportFile = new File(filePath);
      System.out.println("[DEBUG] Exporting events to file: " + exportFile.getAbsolutePath());

      exporter.exportEvents(events, exportFile);

      String successMessage = "Exported " + events.size() + " events from " +
              currentCalendar.getName() + " to " + file.getName();
      System.out.println("[DEBUG] Export successful: " + successMessage);
      view.displayMessage(successMessage);
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to export events: " + e.getMessage());
      e.printStackTrace();
      view.showErrorMessage("Error exporting events: " + e.getMessage());
    }
  }
}

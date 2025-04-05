package controller;

import java.io.File;

import model.calendar.ICalendar;
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
    
    try {
      // TODO: Implement CSVImporter or use alternative import mechanism
      // CSVImporter importer = new CSVImporter();
      // int count = importer.importEvents(currentCalendar, file.getPath());
      // For now, show a placeholder message
      view.displayMessage("Import functionality is not yet implemented.");
    } catch (Exception e) {
      view.showErrorMessage("Error importing events: " + e.getMessage());
    }
  }

  /**
   * Handles exporting a calendar to a file.
   *
   * @param file the file to export to
   */
  public void onExport(File file) {
    if (file == null) {
      view.showErrorMessage("Invalid file selected");
      return;
    }
    
    if (currentCalendar == null) {
      view.showErrorMessage("Please select a calendar first");
      return;
    }
    
    try {
      String filePath = file.getPath();
      if (!filePath.toLowerCase().endsWith(".csv")) {
        filePath += ".csv";
      }
      
      CSVExporter exporter = new CSVExporter();
      // Get events from the calendar and export them to the file
      File exportFile = new File(filePath);
      exporter.exportEvents(currentCalendar.getAllEvents(), exportFile);
      view.displayMessage("Exported events to " + file.getName());
    } catch (Exception e) {
      view.showErrorMessage("Error exporting events: " + e.getMessage());
    }
  }
}

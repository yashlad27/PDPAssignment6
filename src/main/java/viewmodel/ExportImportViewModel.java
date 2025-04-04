package viewmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.calendar.ICalendar;

/**
 * ViewModel for managing CSV import/export operations.
 * This class handles the business logic for importing and exporting calendar data
 * to and from CSV files.
 */
public class ExportImportViewModel implements IViewModel {
  private ICalendar currentCalendar;
  private final List<ExportImportViewModelListener> listeners;

  /**
   * Interface for listeners that want to be notified of changes in the ExportImportViewModel.
   */
  public interface ExportImportViewModelListener {
    void onImportSuccess(String message);

    void onExportSuccess();

    void onError(String error);
  }

  /**
   * Constructs a new ExportImportViewModel.
   */
  public ExportImportViewModel() {
    this.listeners = new ArrayList<>();
  }

  @Override
  public void initialize() {
    // Initialize with default state if needed
  }

  @Override
  public void dispose() {
    listeners.clear();
  }

  @Override
  public void refresh() {
    // Refresh the current state
  }

  /**
   * Adds a listener to be notified of changes.
   *
   * @param listener the listener to add
   */
  public void addListener(ExportImportViewModelListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener from notifications.
   *
   * @param listener the listener to remove
   */
  public void removeListener(ExportImportViewModelListener listener) {
    listeners.remove(listener);
  }

  /**
   * Sets the current calendar.
   *
   * @param calendar the calendar to set
   */
  public void setCurrentCalendar(ICalendar calendar) {
    this.currentCalendar = calendar;
  }

  /**
   * Imports events from a CSV file.
   *
   * @param file the CSV file to import from
   * @return the number of events successfully imported
   */
  public int importFromCSV(File file) {
    if (currentCalendar == null) {
      notifyError("No calendar selected for import");
      return 0;
    }

    try {
      System.out.println("[DEBUG] Starting CSV import from file: " + file.getAbsolutePath());
      
      // Create a CSVExporter instance to handle the import
      model.export.CSVExporter csvExporter = new model.export.CSVExporter();
      
      // Import events from the CSV file
      System.out.println("[DEBUG] Reading events from CSV file");
      List<model.event.Event> importedEvents = csvExporter.importEvents(file);
      
      System.out.println("[DEBUG] Successfully imported " + importedEvents.size() + " events from CSV");
      
      // Add each imported event to the current calendar
      int successCount = 0;
      for (model.event.Event event : importedEvents) {
        try {
          System.out.println("[DEBUG] Adding event to calendar: " + event.getSubject());
          boolean added = currentCalendar.addEvent(event, true); // Set autoDecline to true
          if (added) {
            successCount++;
          }
        } catch (Exception e) {
          System.err.println("[ERROR] Failed to add event '" + event.getSubject() + "': " + e.getMessage());
        }
      }
      
      System.out.println("[DEBUG] Import completed successfully. Added " + successCount + " out of " + importedEvents.size() + " events");
      
      // Notify listeners of successful import
      String message = "Successfully imported " + successCount + " events";
      System.out.println("[DEBUG] Notifying listeners with success message: " + message);
      notifyImportSuccess(message);
      
      // Force a refresh of the calendar display to show the imported events
      System.out.println("[DEBUG] Requesting calendar refresh to display imported events");
      refresh();
      
      return successCount;
    } catch (Exception e) {
      System.err.println("[ERROR] Import failed: " + e.getMessage());
      e.printStackTrace();
      notifyError("Failed to import from CSV: " + e.getMessage());
      return 0;
    }
  }

  /**
   * Exports events to a CSV file.
   *
   * @param file the CSV file to export to
   */
  public void exportToCSV(File file) {
    if (currentCalendar == null) {
      notifyError("No calendar selected for export");
      return;
    }

    try {
      System.out.println("[DEBUG] Starting CSV export to file: " + file.getAbsolutePath());
      
      // Get all events from the current calendar
      String calendarName = ((model.calendar.Calendar)currentCalendar).getName();
      System.out.println("[DEBUG] Retrieving events from calendar: " + calendarName);
      List<model.event.Event> events = currentCalendar.getAllEvents();
      System.out.println("[DEBUG] Found " + events.size() + " events to export");
      
      // Create a CSVExporter instance
      model.export.CSVExporter csvExporter = new model.export.CSVExporter();
      
      // Export events to the CSV file
      csvExporter.exportEvents(events, file);
      
      System.out.println("[DEBUG] Export completed successfully");
      notifyExportSuccess();
    } catch (Exception e) {
      System.err.println("[ERROR] Export failed: " + e.getMessage());
      e.printStackTrace();
      notifyError("Failed to export to CSV: " + e.getMessage());
    }
  }

  private void notifyImportSuccess(String message) {
    for (ExportImportViewModelListener listener : listeners) {
      listener.onImportSuccess(message);
    }
  }

  private void notifyExportSuccess() {
    for (ExportImportViewModelListener listener : listeners) {
      listener.onExportSuccess();
    }
  }

  /**
   * Notifies listeners of an error.
   *
   * @param error the error message
   */
  public void notifyError(String error) {
    for (ExportImportViewModelListener listener : listeners) {
      listener.onError(error);
    }
  }

  public ICalendar getCurrentCalendar() {
    return currentCalendar;
  }
} 
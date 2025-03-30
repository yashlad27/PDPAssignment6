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
    void onImportSuccess();

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
   */
  public void importFromCSV(File file) {
    if (currentCalendar == null) {
      notifyError("No calendar selected for import");
      return;
    }

    try {
      // TODO: Implement CSV import logic
      // This will involve:
      // 1. Reading the CSV file
      // 2. Parsing the events
      // 3. Adding them to the current calendar
      // 4. Notifying success
      notifyImportSuccess();
    } catch (Exception e) {
      notifyError("Failed to import from CSV: " + e.getMessage());
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
      // TODO: Implement CSV export logic
      // This will involve:
      // 1. Getting all events from the current calendar
      // 2. Converting them to CSV format
      // 3. Writing to the file
      // 4. Notifying success
      notifyExportSuccess();
    } catch (Exception e) {
      notifyError("Failed to export to CSV: " + e.getMessage());
    }
  }

  private void notifyImportSuccess() {
    for (ExportImportViewModelListener listener : listeners) {
      listener.onImportSuccess();
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
} 
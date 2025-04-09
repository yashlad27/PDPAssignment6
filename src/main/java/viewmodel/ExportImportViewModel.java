package viewmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.exceptions.CalendarNotFoundException;
import view.IGUIView;

/**
 * ViewModel for managing CSV import/export operations.
 * This class handles the business logic for importing and exporting calendar data
 * to and from CSV files.
 */
public class ExportImportViewModel implements IViewModel {
  private ICalendar currentCalendar;
  private final List<ExportImportViewModelListener> listeners;
  private CalendarManager calendarManager;
  private IGUIView view;

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
  
  /**
   * Constructs a new ExportImportViewModel with a calendar manager and view.
   * 
   * @param calendarManager the calendar manager
   * @param view the GUI view
   */
  public ExportImportViewModel(CalendarManager calendarManager, IGUIView view) {
    this.listeners = new ArrayList<>();
    this.calendarManager = calendarManager;
    this.view = view;
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
   * Sets the current calendar.
   *
   * @param calendar the calendar to set
   */
  public void setCurrentCalendar(ICalendar calendar) {
    this.currentCalendar = calendar;
  }
  
  /**
   * Sets the calendar manager.
   *
   * @param calendarManager the calendar manager to set
   */
  public void setCalendarManager(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
  }
  
  /**
   * Sets the GUI view.
   *
   * @param view the view to set
   */
  public void setView(IGUIView view) {
    this.view = view;
  }
  
  /**
   * Finds a suitable calendar for import/export operations.
   * Tries to get the calendar in the following order:
   * 1. From the selector panel
   * 2. From the current calendar in this view model
   * 3. The active calendar from the calendar manager
   * 4. The first available calendar from the registry
   *
   * @return a suitable calendar or null if none found
   */
  public ICalendar findSuitableCalendar() {
    if (calendarManager == null) {
      return currentCalendar;
    }
    
    // Try to get the calendar from the selector panel if view is available
    ICalendar selectedCalendar = null;
    if (view != null && view.getCalendarSelectorPanel() != null) {
      selectedCalendar = view.getCalendarSelectorPanel().getSelectedCalendar();
    }
    
    // If nothing selected, use the current calendar
    if (selectedCalendar == null) {
      selectedCalendar = currentCalendar;
    }
    
    // If still null, try getting from calendar manager
    if (selectedCalendar == null) {
      try {
        // Try to get the active calendar
        selectedCalendar = calendarManager.getActiveCalendar();
      } catch (CalendarNotFoundException e) {
        // If no active calendar, try to get the first calendar by name
        Set<String> calendarNames = calendarManager.getCalendarRegistry().getCalendarNames();
        if (!calendarNames.isEmpty()) {
          String firstName = calendarNames.iterator().next();
          try {
            selectedCalendar = calendarManager.getCalendar(firstName);
          } catch (CalendarNotFoundException ex) {
            // Ignore, we'll return null
          }
        }
      }
    }
    
    return selectedCalendar;
  }

  /**
   * Imports events from a CSV file.
   *
   * @param file the CSV file to import from
   * @return the number of events successfully imported
   */
  public int importFromCSV(File file) {
    // Find the suitable calendar if none provided
    if (currentCalendar == null) {
      currentCalendar = findSuitableCalendar();
      
      if (currentCalendar == null) {
        notifyError("No calendar selected for import");
        return 0;
      }
    }

    try {
      // Using CSV exporter to read events
      model.export.CSVExporter csvExporter = new model.export.CSVExporter();
      List<model.event.Event> importedEvents = csvExporter.importEvents(file);

      int successCount = 0;
      for (model.event.Event event : importedEvents) {
        try {
          boolean added = currentCalendar.addEvent(event, true);
          if (added) {
            successCount++;
          }
        } catch (Exception e) {
          // Continue with other events even if one fails
        }
      }

      String message = "Successfully imported " + successCount + " events";
      notifyImportSuccess(message);
      
      // Update view if available
      updateViewAfterImport();
      refresh();

      return successCount;
    } catch (Exception e) {
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
    // Find the suitable calendar if none provided
    if (currentCalendar == null) {
      currentCalendar = findSuitableCalendar();
      
      if (currentCalendar == null) {
        notifyError("No calendar selected for export");
        return;
      }
    }

    try {
      // Get events from calendar
      List<model.event.Event> events = currentCalendar.getAllEvents();
      
      // Use CSV exporter to write events
      model.export.CSVExporter csvExporter = new model.export.CSVExporter();
      csvExporter.exportEvents(events, file);

      notifyExportSuccess();
    } catch (Exception e) {
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
  
  /**
   * Updates the view after importing events.
   * If a view is connected, updates the calendar panel.
   */
  public void updateViewAfterImport() {
    if (view != null && currentCalendar != null) {
      view.getCalendarPanel().updateCalendar(currentCalendar);
    }
  }
} 
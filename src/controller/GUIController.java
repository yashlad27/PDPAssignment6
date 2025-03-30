package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import controller.command.edit.strategy.EventEditor;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import utilities.TimeZoneHandler;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;
import view.GUIExportImportPanel;

/**
 * Controller class that handles GUI-specific logic and coordinates between the model and view.
 */
public class GUIController {
  private final CalendarManager calendarManager;
  private final GUIView view;
  private final TimeZoneHandler timezoneHandler;
  private ICalendar currentCalendar;

  /**
   * Constructs a new GUIController.
   *
   * @param calendarManager the calendar manager
   * @param view            the GUI view
   */
  public GUIController(CalendarManager calendarManager, GUIView view) {
    this.calendarManager = calendarManager;
    this.view = view;
    CalendarViewModel calendarViewModel = view.getCalendarViewModel();
    EventViewModel eventViewModel = view.getEventViewModel();
    ExportImportViewModel exportImportViewModel = view.getExportImportViewModel();
    this.timezoneHandler = new TimeZoneHandler();
    this.currentCalendar = null;
  }

  /**
   * Initializes the application.
   */
  public void initialize() throws CalendarNotFoundException {
    // Set up initial state
    try {
      // Create a default calendar if none exists
      if (calendarManager.getCalendarCount() == 0) {
        String defaultCalendar = "Default Calendar";
        calendarManager.createCalendar(defaultCalendar, timezoneHandler.getSystemDefaultTimezone());
      }

      // Get the first available calendar
      ICalendar firstCalendar = null;
      String firstCalendarName = null;
      for (String name : calendarManager.getCalendarRegistry().getCalendarNames()) {
        firstCalendarName = name;
        firstCalendar = calendarManager.getCalendar(name);
        break;
      }

      if (firstCalendar == null) {
        throw new CalendarNotFoundException("No calendars available");
      }

      // Set up the view with the first calendar
      currentCalendar = firstCalendar;
      view.setSelectedCalendar(firstCalendarName);
      view.updateCalendarView(firstCalendar);
      view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));

    } catch (Exception e) {
      view.displayError("Failed to initialize calendar: " + e.getMessage());
      throw new CalendarNotFoundException("Failed to initialize calendar");
    }

    // Set up event listeners
    setupEventListeners();
  }

  /**
   * Sets up event listeners for the view components.
   */
  private void setupEventListeners() {
    // Calendar selection
    view.getCalendarSelectorPanel().addCalendarSelectionListener(new GUICalendarSelectorPanel.CalendarSelectionListener() {
      @Override
      public void onCalendarSelected(String calendarName) {
        try {
          ICalendar calendar = calendarManager.getCalendar(calendarName);
          if (calendar != null) {
            currentCalendar = calendar;
            view.updateCalendarView(calendar);

            // Update events display
            List<Event> events = getAllEvents();
            List<RecurringEvent> recurringEvents = getAllRecurringEvents();
            view.getCalendarPanel().updateEvents(events);
            view.getCalendarPanel().updateRecurringEvents(recurringEvents);
          }
        } catch (CalendarNotFoundException e) {
          view.displayError("Calendar not found: " + calendarName);
        }
      }

      @Override
      public void onCalendarCreated(String calendarName, String timezone) {
        try {
          calendarManager.createCalendar(calendarName, timezone);
          view.displayMessage("Calendar created successfully");
          view.refreshView();
        } catch (Exception e) {
          view.displayError("Failed to create calendar: " + e.getMessage());
        }
      }
    });

    // Event creation
    view.getEventPanel().addEventPanelListener(new GUIEventPanel.EventPanelListener() {
      @Override
      public void onEventSaved(String[] args, boolean isRecurring) {
        try {
          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          editor.executeEdit(currentCalendar);
          view.displayMessage("Event created successfully");
          view.refreshView();
        } catch (Exception e) {
          view.displayError("Failed to create event: " + e.getMessage());
        }
      }

      @Override
      public void onEventCancelled() {
        view.getEventPanel().clearForm();
      }

      @Override
      public void onEventUpdated(String[] args, boolean isRecurring) {
        try {
          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          editor.executeEdit(currentCalendar);
          view.displayMessage("Event updated successfully");
          view.refreshView();
        } catch (Exception e) {
          view.displayError("Failed to update event: " + e.getMessage());
        }
      }

      @Override
      public void onEventDeleted(String[] args, boolean isRecurring) {
        try {
          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          editor.executeEdit(currentCalendar);
          view.displayMessage("Event deleted successfully");
          view.refreshView();
        } catch (Exception e) {
          view.displayError("Failed to delete event: " + e.getMessage());
        }
      }
    });

    // Date selection
    view.getCalendarPanel().addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        try {
          view.getEventPanel().setDate(date);
          List<Event> events = getEventsOnDate(date);
          view.getEventPanel().setEvents(events);
        } catch (Exception e) {
          view.displayError("Failed to get events for date: " + e.getMessage());
        }
      }

      @Override
      public void onEventSelected(Event event) {
        view.getEventPanel().displayEvent(event);
      }

      @Override
      public void onRecurringEventSelected(RecurringEvent event) {
        view.getEventPanel().displayRecurringEvent(event);
      }
    });

    // CSV import/export
    view.getExportImportPanel().addExportImportListener(new GUIExportImportPanel.ExportImportListener() {
      @Override
      public void onImportRequested(File file) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }
          String[] args = new String[]{"all", "import_csv", file.getAbsolutePath()};
          EventEditor editor = EventEditor.forType("all", args);
          editor.executeEdit(currentCalendar);
          view.getExportImportPanel().showImportSuccess();
          view.refreshView();
        } catch (Exception e) {
          view.getExportImportPanel().showError("Failed to import from CSV: " + e.getMessage());
        }
      }

      @Override
      public void onExportRequested(File file) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }
          String[] args = new String[]{"all", "export_csv", file.getAbsolutePath()};
          EventEditor editor = EventEditor.forType("all", args);
          editor.executeEdit(currentCalendar);
          view.getExportImportPanel().showExportSuccess();
        } catch (Exception e) {
          view.getExportImportPanel().showError("Failed to export to CSV: " + e.getMessage());
        }
      }
    });
  }

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to get events for
   * @return list of events on the date
   */
  public List<Event> getEventsOnDate(LocalDate date) {
    if (currentCalendar == null) {
      return List.of();
    }
    try {
      String[] args = new String[]{"single", "get_events", date.toString()};
      EventEditor editor = EventEditor.forType("single", args);
      editor.executeEdit(currentCalendar);
      return currentCalendar.getEventsOnDate(date);
    } catch (Exception e) {
      view.displayError("Failed to get events for date: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets all events in a date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return list of events in the range
   */
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (currentCalendar == null) {
      return List.of();
    }
    try {
      String[] args = new String[]{"all", "get_range", startDate.toString(), endDate.toString()};
      EventEditor editor = EventEditor.forType("all", args);
      editor.executeEdit(currentCalendar);
      return currentCalendar.getEventsInRange(startDate, endDate);
    } catch (Exception e) {
      view.displayError("Failed to get events in range: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets all events in the current calendar.
   *
   * @return list of all events
   */
  public List<Event> getAllEvents() {
    if (currentCalendar == null) {
      return List.of();
    }
    try {
      String[] args = new String[]{"all", "get_all"};
      EventEditor editor = EventEditor.forType("all", args);
      editor.executeEdit(currentCalendar);
      return currentCalendar.getAllEvents();
    } catch (Exception e) {
      view.displayError("Failed to get all events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Gets all recurring events in the current calendar.
   *
   * @return list of all recurring events
   */
  public List<RecurringEvent> getAllRecurringEvents() {
    if (currentCalendar == null) {
      return List.of();
    }
    try {
      String[] args = new String[]{"series_from_date", "get_all"};
      EventEditor editor = EventEditor.forType("series_from_date", args);
      editor.executeEdit(currentCalendar);
      return currentCalendar.getAllRecurringEvents();
    } catch (Exception e) {
      view.displayError("Failed to get all recurring events: " + e.getMessage());
      return List.of();
    }
  }

  /**
   * Handles the creation of a new event.
   */
  public void createNewEvent() {
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    view.getEventPanel().clearForm();
    view.getEventPanel().setDate(view.getCalendarPanel().getSelectedDate());
  }

  /**
   * Handles the deletion of an event.
   *
   * @param event the event to delete
   */
  public void deleteEvent(Event event) {
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }

    try {
      String[] args = new String[]{"single", "delete", event.getSubject(), event.getStartDateTime().toString()};
      EventEditor editor = EventEditor.forType("single", args);
      editor.executeEdit(currentCalendar);
      view.displayMessage("Event deleted successfully");
      view.refreshView();
    } catch (Exception e) {
      view.displayError("Failed to delete event: " + e.getMessage());
    }
  }

  /**
   * Handles the deletion of a recurring event.
   *
   * @param event the recurring event to delete
   */
  public void deleteRecurringEvent(RecurringEvent event) {
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }

    try {
      String[] args = new String[]{"series_from_date", "delete", event.getSubject(), event.getStartDateTime().toString()};
      EventEditor editor = EventEditor.forType("series_from_date", args);
      editor.executeEdit(currentCalendar);
      view.displayMessage("Recurring event deleted successfully");
      view.refreshView();
    } catch (Exception e) {
      view.displayError("Failed to delete recurring event: " + e.getMessage());
    }
  }

  /**
   * Handles the editing of an event.
   *
   * @param event the event to edit
   */
  public void editEvent(Event event) {
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    view.getEventPanel().displayEvent(event);
  }

  /**
   * Handles the editing of a recurring event.
   *
   * @param event the recurring event to edit
   */
  public void editRecurringEvent(RecurringEvent event) {
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    view.getEventPanel().displayRecurringEvent(event);
  }

  /**
   * Handles the closing of the application.
   */
  public void handleApplicationClose() {
    try {
      view.displayMessage("Saving changes...");
      // TODO: Implement save functionality if needed
      view.displayMessage("Application closed successfully");
    } catch (Exception e) {
      view.displayError("Error while closing application: " + e.getMessage());
    }
  }
} 
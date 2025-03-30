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
import utilities.CalendarNameValidator;
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
    System.out.println("Initializing GUI controller...");
    // Set up initial state
    try {
      // Clear any existing calendar names
      CalendarNameValidator.removeAllCalendarNames();

      // Create a default calendar if none exists
      String defaultCalendar = "Default_Calendar";
      if (calendarManager.getCalendarCount() == 0) {
        System.out.println("Creating default calendar...");
        calendarManager.createCalendar(defaultCalendar, timezoneHandler.getSystemDefaultTimezone());
      }

      // Get the first available calendar
      System.out.println("Getting first available calendar...");
      currentCalendar = calendarManager.getCalendar(defaultCalendar);
      if (currentCalendar == null) {
        throw new CalendarNotFoundException("No calendars available");
      }

      // Set up the view with the first calendar
      System.out.println("Setting up view with calendar: " + defaultCalendar);
      view.setSelectedCalendar(defaultCalendar);
      view.updateCalendarView(currentCalendar);
      view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));

      // Set up event listeners
      System.out.println("Setting up event listeners...");
      setupEventListeners();

      // Update the calendar display with events
      System.out.println("Updating calendar display...");
      List<Event> events = getAllEvents();
      List<RecurringEvent> recurringEvents = getAllRecurringEvents();
      view.getCalendarPanel().updateEvents(events);
      view.getCalendarPanel().updateRecurringEvents(recurringEvents);
      System.out.println("GUI controller initialized successfully.");

    } catch (Exception e) {
      System.out.println("Error initializing GUI controller: " + e.getMessage());
      e.printStackTrace();
      view.displayError("Failed to initialize calendar: " + e.getMessage());
      throw new CalendarNotFoundException("Failed to initialize calendar: " + e.getMessage());
    }
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
          if (calendarName == null || calendarName.trim().isEmpty()) {
            view.displayError("Please select a calendar");
            return;
          }
          
          ICalendar calendar = calendarManager.getCalendar(calendarName);
          if (calendar != null) {
            currentCalendar = calendar;
            view.updateCalendarView(calendar);

            // Update events display
            List<Event> events = getAllEvents();
            List<RecurringEvent> recurringEvents = getAllRecurringEvents();
            view.getCalendarPanel().updateEvents(events);
            view.getCalendarPanel().updateRecurringEvents(recurringEvents);
            view.displayMessage("Selected calendar: " + calendarName);
          }
        } catch (CalendarNotFoundException e) {
          view.displayError("Calendar not found: " + calendarName);
        } catch (Exception e) {
          view.displayError("Error selecting calendar: " + e.getMessage());
        }
      }

      @Override
      public void onCalendarCreated(String calendarName, String timezone) {
        try {
          if (calendarName == null || calendarName.trim().isEmpty()) {
            view.displayError("Please enter a calendar name");
            return;
          }
          
          if (timezone == null || timezone.trim().isEmpty()) {
            view.displayError("Please select a timezone");
            return;
          }

          calendarManager.createCalendar(calendarName, timezone);
          // Update the calendar list in the view
          view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));
          // Select the newly created calendar
          view.setSelectedCalendar(calendarName);
          // Update the current calendar
          currentCalendar = calendarManager.getCalendar(calendarName);
          view.updateCalendarView(currentCalendar);
          view.displayMessage("Calendar '" + calendarName + "' created successfully");
          view.refreshView();
        } catch (IllegalArgumentException e) {
          view.displayError("Invalid calendar name or timezone: " + e.getMessage());
        } catch (Exception e) {
          view.displayError("Failed to create calendar: " + e.getMessage());
        }
      }
    });

    // Calendar panel events
    view.getCalendarPanel().addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        try {
          if (date == null) {
            view.displayError("Please select a valid date");
            return;
          }
          
          view.getEventPanel().setDate(date);
          List<Event> events = getEventsOnDate(date);
          view.getEventPanel().setEvents(events);
          view.getCalendarPanel().updateEventList(date);
          updateStatus(date);
        } catch (Exception e) {
          view.displayError("Failed to get events for date: " + e.getMessage());
        }
      }

      @Override
      public void onEventSelected(Event event) {
        if (event == null) {
          view.displayError("No event selected");
          return;
        }
        view.getEventPanel().displayEvent(event);
      }

      @Override
      public void onRecurringEventSelected(RecurringEvent event) {
        if (event == null) {
          view.displayError("No recurring event selected");
          return;
        }
        view.getEventPanel().displayRecurringEvent(event);
      }

      @Override
      public void onStatusRequested(LocalDate date) {
        if (date == null) {
          view.displayError("Please select a valid date");
          return;
        }
        updateStatus(date);
      }

      @Override
      public void onEventsListRequested(LocalDate date) {
        if (date == null) {
          view.displayError("Please select a valid date");
          return;
        }
        view.getCalendarPanel().updateEventList(date);
      }

      @Override
      public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
        try {
          if (startDate == null || endDate == null) {
            view.displayError("Please select both start and end dates");
            return;
          }
          
          if (startDate.isAfter(endDate)) {
            view.displayError("Start date must be before or equal to end date");
            return;
          }

          List<Event> events = getEventsInRange(startDate, endDate);
          view.getCalendarPanel().updateEventListRange(startDate, endDate, events);
        } catch (Exception e) {
          view.displayError("Failed to get events in range: " + e.getMessage());
        }
      }
    });

    // Event creation
    view.getEventPanel().addEventPanelListener(new GUIEventPanel.EventPanelListener() {
      @Override
      public void onEventSaved(String[] args, boolean isRecurring) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }

          if (args == null || args.length == 0) {
            view.displayError("Invalid event data");
            return;
          }

          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          String result = editor.executeEdit(currentCalendar);
          
          // Show the result message from the editor
          if (result != null && !result.isEmpty()) {
            view.displayMessage(result);
          } else {
            view.displayMessage("Event created successfully");
          }
          
          // Refresh the view
          view.refreshView();
          updateStatus(view.getCalendarPanel().getSelectedDate());
        } catch (IllegalArgumentException e) {
          view.displayError("Invalid event data: " + e.getMessage());
        } catch (Exception e) {
          view.displayError("Failed to create event: " + e.getMessage());
        }
      }

      @Override
      public void onEventCancelled() {
        view.getEventPanel().clearForm();
        view.displayMessage("Event creation cancelled");
      }

      @Override
      public void onEventUpdated(String[] args, boolean isRecurring) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }

          if (args == null || args.length == 0) {
            view.displayError("Invalid event data");
            return;
          }

          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          String result = editor.executeEdit(currentCalendar);
          
          // Show the result message from the editor
          if (result != null && !result.isEmpty()) {
            view.displayMessage(result);
          } else {
            view.displayMessage("Event updated successfully");
          }
          
          view.refreshView();
          updateStatus(view.getCalendarPanel().getSelectedDate());
        } catch (IllegalArgumentException e) {
          view.displayError("Invalid event data: " + e.getMessage());
        } catch (Exception e) {
          view.displayError("Failed to update event: " + e.getMessage());
        }
      }

      @Override
      public void onEventDeleted(String[] args, boolean isRecurring) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }

          if (args == null || args.length == 0) {
            view.displayError("Invalid event data");
            return;
          }

          EventEditor editor = EventEditor.forType(isRecurring ? "series_from_date" : "single", args);
          String result = editor.executeEdit(currentCalendar);
          
          // Show the result message from the editor
          if (result != null && !result.isEmpty()) {
            view.displayMessage(result);
          } else {
            view.displayMessage("Event deleted successfully");
          }
          
          view.refreshView();
          updateStatus(view.getCalendarPanel().getSelectedDate());
        } catch (IllegalArgumentException e) {
          view.displayError("Invalid event data: " + e.getMessage());
        } catch (Exception e) {
          view.displayError("Failed to delete event: " + e.getMessage());
        }
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
          updateStatus(view.getCalendarPanel().getSelectedDate());
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
   * Updates the busy/available status for a given date.
   *
   * @param date the date to check
   */
  private void updateStatus(LocalDate date) {
    if (currentCalendar == null) {
      return;
    }
    try {
      List<Event> events = getEventsOnDate(date);
      boolean isBusy = !events.isEmpty();
      view.getCalendarPanel().updateStatus(isBusy);
    } catch (Exception e) {
      view.displayError("Failed to update status: " + e.getMessage());
    }
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
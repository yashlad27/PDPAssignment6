package controller;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import controller.command.edit.strategy.EventEditor;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import model.export.CSVExporter;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import view.GUIView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

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
        String timezone = timezoneHandler.getSystemDefaultTimezone();
        System.out.println("[DEBUG] Using system timezone: " + timezone);
        calendarManager.createCalendar(defaultCalendar, timezone);
        System.out.println("[DEBUG] Default calendar created");
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
    view.getCalendarSelectorPanel().addCalendarSelectorListener(new GUICalendarSelectorPanel.CalendarSelectorListener() {
      @Override
      public void onCalendarSelected(ICalendar calendar) {
        try {
          if (calendar == null) {
            view.displayError("Please select a valid calendar");
            return;
          }
          System.out.println("[DEBUG] Calendar selected: " + calendar.toString());
          currentCalendar = calendar;
          view.updateCalendarView(calendar);

          // Update events display
          List<Event> events = getAllEvents();
          List<RecurringEvent> recurringEvents = getAllRecurringEvents();
          view.getCalendarPanel().updateEvents(events);
          view.getCalendarPanel().updateRecurringEvents(recurringEvents);
          view.displayMessage("Selected calendar: " + calendar.toString());
        } catch (Exception e) {
          System.out.println("[DEBUG] Calendar selection error: " + e.getMessage());
          view.displayError("Failed to select calendar: " + e.getMessage());
        }
      }

      @Override
      public void onCalendarCreated(String name, String timezone) {
        System.out.println("[DEBUG] Calendar creation initiated: " + name + " with timezone: " + timezone);
        try {
          // Validate timezone format
          if (!timezone.contains("/")) {
            System.out.println("[DEBUG] Invalid timezone format: " + timezone);
            view.showError("Invalid timezone format. Please use Area/Location format.");
            return;
          }

          // Create the calendar
          calendarManager.createCalendar(name, timezone);
          System.out.println("[DEBUG] Calendar created");

          // Update the view
          view.updateCalendarList(new ArrayList<>(calendarManager.getCalendarRegistry().getCalendarNames()));
          view.setSelectedCalendar(name);
          currentCalendar = calendarManager.getCalendar(name);
          view.displayMessage("Calendar created successfully: " + name);
          view.refreshView();
        } catch (Exception ex) {
          System.out.println("[DEBUG] Calendar creation error: " + ex.getMessage());
          view.showError("Could not create calendar: " + ex.getMessage());
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

          // Use proper controller methods instead of direct view updates
          // This ensures all necessary model checks are performed
          setSelectedDate(date);
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

        // Use proper controller methods to retrieve and update events
        List<Event> events = getEventsOnDate(date);
        view.updateEventList(events);
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

          if (args == null || args.length < 2) {
            view.displayError("Invalid event data");
            return;
          }

          String result = executeCommand("create", args);
          if (result.startsWith("Error")) {
            view.displayError(result);
          } else {
            view.displayMessage(result);
            view.refreshView();
          }
        } catch (Exception e) {
          view.displayError("Failed to save event: " + e.getMessage());
        }
      }

      @Override
      public void onEventCancelled() {
        view.getEventPanel().clearForm();
      }

      @Override
      public void onEventUpdated(String[] args, boolean isRecurring) {
        try {
          if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
          }

          if (args == null || args.length < 2) {
            view.displayError("Invalid event data");
            return;
          }

          String result = executeCommand("edit", args);
          if (result.startsWith("Error")) {
            view.displayError(result);
          } else {
            view.displayMessage(result);
            view.refreshView();
          }
        } catch (Exception e) {
          view.displayError("Failed to update event: " + e.getMessage());
        }
      }
    });

    // Export/Import panel events
    view.getExportImportPanel().addExportImportListener(new GUIExportImportPanel.ExportImportListener() {
      @Override
      public void onImport(File file) {
        if (file != null) {
          try {
            System.out.println("[DEBUG] Starting import process for file: " + file.getAbsolutePath());
            
            // Get the selected calendar from the view
            ICalendar currentCalendar = view.getCalendarSelectorPanel().getSelectedCalendar();
            System.out.println("[DEBUG] Selected calendar from selector panel: " + 
                (currentCalendar != null ? currentCalendar.toString() : "null"));
            
            // If no calendar is selected in the selector panel, try getting the current calendar from the controller
            if (currentCalendar == null) {
              System.out.println("[DEBUG] No calendar selected in selector panel, using current calendar from controller");
              currentCalendar = GUIController.this.currentCalendar;
            }
            
            // If still null, try getting the first available calendar
            if (currentCalendar == null) {
              System.out.println("[DEBUG] Still no calendar, trying to get first available calendar");
              try {
                // Try to get the active calendar
                currentCalendar = calendarManager.getActiveCalendar();
                System.out.println("[DEBUG] Using active calendar: " + currentCalendar.toString());
              } catch (CalendarNotFoundException e) {
                // If no active calendar, try to get the first calendar by name
                Set<String> calendarNames = calendarManager.getCalendarRegistry().getCalendarNames();
                if (!calendarNames.isEmpty()) {
                  String firstName = calendarNames.iterator().next();
                  currentCalendar = calendarManager.getCalendar(firstName);
                  System.out.println("[DEBUG] Using first available calendar: " + currentCalendar.toString());
                }
              }
            }
            
            // Final check if we have a valid calendar
            if (currentCalendar == null) {
              System.out.println("[ERROR] No calendar available for import");
              view.showError("No calendar available. Please create a calendar first.");
              return;
            }
            
            System.out.println("[DEBUG] Using calendar for import: " + ((model.calendar.Calendar)currentCalendar).getName());
            
            // Use the ExportImportViewModel to handle the import
            ExportImportViewModel exportImportViewModel = view.getExportImportViewModel();
            exportImportViewModel.setCurrentCalendar(currentCalendar);
            
            // Import the events
            System.out.println("[DEBUG] Starting import process via ExportImportViewModel");
            int importedCount = exportImportViewModel.importFromCSV(file);
            
            System.out.println("[DEBUG] Import completed. Imported " + importedCount + " events");
            
            // Update the calendar view to show the imported events
            System.out.println("[DEBUG] Updating calendar view with imported events");
            view.getCalendarPanel().updateCalendar(currentCalendar);
            
            // The success message will be shown by the ExportImportViewModel callback
          } catch (Exception e) {
            System.err.println("[ERROR] Import failed: " + e.getMessage());
            e.printStackTrace();
            view.showError("Failed to import calendar data: " + e.getMessage());
          }
        }
      }

      @Override
      public void onExport(File file) {
        try {
          // Export current calendar to CSV file
          ICalendar currentCalendar = view.getCalendarSelectorPanel().getSelectedCalendar();
          if (currentCalendar != null) {
            CSVExporter exporter = new CSVExporter();
            exporter.exportEvents(currentCalendar.getAllEvents(), file);
          } else {
            view.showError("Please select a calendar first");
          }
        } catch (Exception e) {
          view.showError("Failed to export calendar data: " + e.getMessage());
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
      return currentCalendar.getEventsOnDate(date);
    } catch (Exception e) {
      // Don't show error for empty calendars
      if (!e.getMessage().contains("Insufficient arguments")) {
        view.displayError("Failed to get events for date: " + e.getMessage());
      }
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
      return currentCalendar.getEventsInRange(startDate, endDate);
    } catch (Exception e) {
      // Don't show error for empty calendars
      if (!e.getMessage().contains("Insufficient arguments")) {
        view.displayError("Failed to get events in range: " + e.getMessage());
      }
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
      return currentCalendar.getAllEvents();
    } catch (Exception e) {
      // Don't show error for empty calendars
      if (!e.getMessage().contains("Insufficient arguments")) {
        view.displayError("Failed to get all events: " + e.getMessage());
      }
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
      return currentCalendar.getAllRecurringEvents();
    } catch (Exception e) {
      // Don't show error for empty calendars
      if (!e.getMessage().contains("Insufficient arguments")) {
        view.displayError("Failed to get all recurring events: " + e.getMessage());
      }
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

  public String executeCommand(String command, String[] args) {
    System.out.println("[DEBUG] Executing command: " + command + " with args: " + String.join(", ", args));
    if (currentCalendar == null) {
      System.out.println("[DEBUG] No calendar selected!");
      return "Error: No calendar selected";
    }

    try {
      String eventType = null;
      String subject = null;
      LocalDateTime startDateTime = null;

      if (args != null && args.length >= 4) {
        eventType = args[0];
        subject = args[2];
        startDateTime = LocalDateTime.parse(args[3]);
      }

      switch (command.toLowerCase()) {
        case "create":
          if (args.length < 4) {
            System.out.println("[DEBUG] Invalid number of arguments for create command");
            return "Error: Invalid command arguments";
          }

          if ("single".equals(eventType)) {
            System.out.println("[DEBUG] Creating single event: " + subject);
            String[] eventDetails = args[4].split(",");
            if (eventDetails.length < 4) {
              System.out.println("[DEBUG] Invalid event details format");
              return "Error: Invalid event details format";
            }

            Event event = new Event(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    eventDetails.length > 4 ? eventDetails[4] : "", // description
                    eventDetails[3], // location
                    true // isPublic
            );
            System.out.println("[DEBUG] Adding event to calendar: " + event);
            boolean added = currentCalendar.addEvent(event, false);
            System.out.println("[DEBUG] Event added: " + added);
            if (added) {
              updateEventList(startDateTime.toLocalDate());
              return "Event created successfully";
            }
            return "Failed to create event";
          } else if ("series_from_date".equals(eventType)) {
            System.out.println("[DEBUG] Creating recurring event: " + subject);
            String[] eventDetails = args[4].split(",");
            if (eventDetails.length < 7) {
              System.out.println("[DEBUG] Invalid recurring event details format");
              return "Error: Invalid recurring event details format";
            }

            Set<DayOfWeek> weekdays = Arrays.stream(eventDetails[5].replaceAll("[\\[\\]]", "").split(", "))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());

            RecurringEvent recurringEvent = new RecurringEvent(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    "", // description
                    eventDetails[3], // location
                    true, // isPublic
                    weekdays, // repeatDays
                    Integer.parseInt(eventDetails[4]), // occurrences
                    LocalDate.parse(eventDetails[6]) // untilDate
            );
            System.out.println("[DEBUG] Adding recurring event to calendar: " + recurringEvent);
            boolean added = currentCalendar.addRecurringEvent(recurringEvent, false);
            System.out.println("[DEBUG] Recurring event added: " + added);
            if (added) {
              updateEventList(startDateTime.toLocalDate());
              return "Recurring event created successfully";
            }
            return "Failed to create recurring event";
          }
          break;

        case "edit":
          if (args.length < 4) {
            System.out.println("[DEBUG] Invalid number of arguments for edit command");
            return "Error: Invalid command arguments";
          }

          if ("single".equals(eventType)) {
            System.out.println("[DEBUG] Editing single event: " + subject);
            String[] eventDetails = args[4].split(",");
            if (eventDetails.length < 4) {
              System.out.println("[DEBUG] Invalid event details format");
              return "Error: Invalid event details format";
            }

            Event event = new Event(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    eventDetails.length > 4 ? eventDetails[4] : "", // description
                    eventDetails[3], // location
                    true // isPublic
            );
            System.out.println("[DEBUG] Updating event in calendar: " + event);
            boolean updated = currentCalendar.addEvent(event, false);
            System.out.println("[DEBUG] Event updated: " + updated);
            if (updated) {
              updateEventList(startDateTime.toLocalDate());
              return "Event updated successfully";
            }
            return "Failed to update event";
          } else if ("series_from_date".equals(eventType)) {
            System.out.println("[DEBUG] Editing recurring event: " + subject);
            String[] eventDetails = args[4].split(",");
            if (eventDetails.length < 7) {
              System.out.println("[DEBUG] Invalid recurring event details format");
              return "Error: Invalid recurring event details format";
            }

            Set<DayOfWeek> weekdays = Arrays.stream(eventDetails[5].replaceAll("[\\[\\]]", "").split(", "))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());

            RecurringEvent recurringEvent = new RecurringEvent(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    "", // description
                    eventDetails[3], // location
                    true, // isPublic
                    weekdays, // repeatDays
                    Integer.parseInt(eventDetails[4]), // occurrences
                    LocalDate.parse(eventDetails[6]) // untilDate
            );
            System.out.println("[DEBUG] Updating recurring event in calendar: " + recurringEvent);
            boolean updated = currentCalendar.addRecurringEvent(recurringEvent, false);
            System.out.println("[DEBUG] Recurring event updated: " + updated);
            if (updated) {
              updateEventList(startDateTime.toLocalDate());
              return "Recurring event updated successfully";
            }
            return "Failed to update recurring event";
          }
          break;

        default:
          System.out.println("[DEBUG] Unknown command: " + command);
          return "Error: Unknown command";
      }
      return "Error: Invalid command arguments";
    } catch (Exception e) {
      System.out.println("[DEBUG] Error executing command: " + e.getMessage());
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
  }

  private void updateEventList(LocalDate date) {
    if (date != null && currentCalendar != null) {
      java.util.List<Event> events = currentCalendar.getEventsOnDate(date);
      view.getCalendarPanel().updateEvents(events);
      view.getCalendarPanel().updateEventList(date);
    }
  }

  /**
   * Sets the selected date and updates all relevant views.
   *
   * @param date The date to select
   */
  public void setSelectedDate(LocalDate date) {
    if (date == null) {
      return;
    }

    try {
      // First update the model's knowledge of the selected date
      view.setSelectedDate(date);

      // Then get events for this date
      List<Event> events = getEventsOnDate(date);

      // Update all relevant view components with a consistent flow
      view.updateSelectedDate(date);
      view.updateEventList(events);
      view.getCalendarPanel().updateEventList(date);
      updateStatus(date);
    } catch (Exception e) {
      view.displayError("Error setting selected date: " + e.getMessage());
    }
  }

  private void updateEvents(LocalDate date) {
    try {
      List<Event> events = getEventsOnDate(date);
      view.getCalendarPanel().updateEventList(date);
      view.getCalendarPanel().updateEvents(events);
    } catch (Exception e) {
      view.displayError("Failed to update events: " + e.getMessage());
    }
  }
} 
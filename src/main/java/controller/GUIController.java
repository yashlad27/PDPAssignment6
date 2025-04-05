package controller;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;

import controller.command.copy.DirectCopyEventCommand;
import controller.command.edit.strategy.ConsolidatedEventEditor;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.export.CSVExporter;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;

import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.EventFormData;
import view.GUIExportImportPanel;
import view.GUIView;
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
    // ViewModels are managed by the view itself, so we don't need references here
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
        System.out.println("[DEBUG] Calendar selected: " + calendar);
        try {
          if (calendar == null) {
            view.displayError("Please select a valid calendar");
            return;
          }
          System.out.println("[DEBUG] Calendar details: " + calendar.toString());
          System.out.println("[DEBUG] Calendar has " + calendar.getAllEvents().size() + " events");
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
            view.showErrorMessage("Invalid timezone format. Please use Area/Location format.");
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
          view.showErrorMessage("Could not create calendar: " + ex.getMessage());
        }
      }
    });

    // Calendar panel events
    view.getCalendarPanel().addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        System.out.println("[DEBUG] Date selected: " + date);
        try {
          if (date == null) {
            view.displayError("Please select a valid date");
            return;
          }
          
          // Explicitly log the date to verify it's correct
          System.out.println("[DEBUG] Processing date selection for: " + date);
          
          // Use proper controller methods instead of direct view updates
          // This ensures all necessary model checks are performed
          setSelectedDate(date);
        } catch (Exception e) {
          System.out.println("[ERROR] Date selection error: " + e.getMessage());
          e.printStackTrace();
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
        System.out.println("[DEBUG] Events list requested for date: " + date);
        if (date == null) {
          view.displayError("Please select a valid date");
          return;
        }
        listEvents(date);
      }

      @Override
      public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
        System.out.println("[DEBUG] Date range selected: " + startDate + " to " + endDate);
        showRange(startDate, endDate);
      }

      @Override
      public void onEditEvent(Event event) {
        System.out.println("[DEBUG] Edit event requested: " + event.getSubject());
        editEvent(event);
      }

      @Override
      public void onCopyEvent(Event event) {
        System.out.println("[DEBUG] Copy event requested: " + event.getSubject());
        showCopyEventDialog(event);
      }

      @Override
      public void onPrintEvent(Event event) {
        System.out.println("[DEBUG] Print event requested: " + event.getSubject());
        printEvent(event);
      }
    });

    // Event panel events
    view.getEventPanel().addEventPanelListener(new GUIEventPanel.EventPanelListener() {
      @Override
      public void onEventSaved(EventFormData formData) {
        GUIController.this.onEventSaved(formData);
      }

      @Override
      public void onEventCancelled() {
        // Handle event cancel
        view.getEventPanel().clearForm();
      }

      @Override
      public void onEventUpdated(EventFormData formData) {
        GUIController.this.onEventUpdated(formData);
      }

      @Override
      public void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime) {
        Event currentEvent = view.getEventPanel().getCurrentEvent();
        if (currentEvent == null) {
          view.displayError("No event selected to copy");
          return;
        }
        
        // Copy the event using the strategy
        boolean success = copyEvent(currentEvent, targetCalendarName, targetStartDateTime, targetEndDateTime);
        if (success) {
          view.displayMessage("Event copied successfully");
          view.getEventPanel().clearForm();
          updateEvents(LocalDate.now()); // Refresh the events list
        } else {
          view.displayError("Failed to copy event");
        }
      }
      
      @Override
      public List<String> getAvailableCalendarNames() {
        System.out.println("[DEBUG] Getting available calendar names from GUIController");
        List<String> calendarNames = new ArrayList<>();
        try {
          if (calendarManager != null) {
            calendarNames = new ArrayList<>(calendarManager.getCalendarNames());
            System.out.println("[DEBUG] Found " + calendarNames.size() + " calendars");
          }
        } catch (Exception ex) {
          System.out.println("[DEBUG] Error getting calendar names: " + ex.getMessage());
        }
        return calendarNames;
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
              view.showErrorMessage("No calendar available. Please create a calendar first.");
              return;
            }

            System.out.println("[DEBUG] Using calendar for import: " + ((model.calendar.Calendar) currentCalendar).getName());

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
            view.showErrorMessage("Failed to import calendar data: " + e.getMessage());
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
            view.displayError("Please select a calendar first");
          }
        } catch (Exception e) {
          view.showErrorMessage("Failed to export calendar data: " + e.getMessage());
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
   * Updates the events for the specified date.
   *
   * @param date the date to update events for
   */
  private void updateEvents(LocalDate date) {
    if (date == null) {
      return;
    }

    try {
      List<Event> events = getEventsOnDate(date);
      view.updateEventList(events);
      view.getCalendarPanel().updateEventList(date);
    } catch (Exception e) {
      view.displayError("Failed to update events: " + e.getMessage());
    }
  }
  
  /**
   * Lists events for the specified date.
   *
   * @param date the date to list events for
   */
  private void listEvents(LocalDate date) {
    if (date == null) {
      return;
    }

    try {
      List<Event> events = getEventsOnDate(date);
      view.updateEventList(events);
      view.getCalendarPanel().updateEventList(date);
    } catch (Exception e) {
      view.displayError("Failed to list events: " + e.getMessage());
    }
  }
  
  /**
   * Shows events in the specified date range.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   */
  private void showRange(LocalDate startDate, LocalDate endDate) {
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
      ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
          UUID.fromString(args[1]), args[2], args[3], args[4]);
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
      LocalDate fromDate = LocalDate.parse(args[5]);
      ConsolidatedEventEditor editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
          UUID.fromString(args[1]), args[2], args[3], args[4], fromDate);
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
    System.out.println("[DEBUG] GUIController.editEvent called for: " + event.getSubject());
    System.out.println("[DEBUG] Editing event: " + event.getSubject());

    // Show the event edit dialog
    view.showEventEditDialog(event, false);

    // The view will handle the editing process and call back to the controller
    // when the edit is confirmed via the onEventUpdated method
  }

  /**
   * Handles the editing of a recurring event.
   *
   * @param event the recurring event to edit
   */
  public void editRecurringEvent(RecurringEvent event) {
    System.out.println("[DEBUG] Editing recurring event: " + event.getSubject());

    // Show the event edit dialog
    view.showEventEditDialog(event, true);

    // The view will handle the editing process and call back to the controller
    // when the edit is confirmed via the onEventUpdated method
  }
  
  /**
   * Shows the copy event dialog for the given event.
   *
   * @param event the event to copy
   */
  public void showCopyEventDialog(Event event) {
    System.out.println("[DEBUG] GUIController.showCopyEventDialog called for: " + event.getSubject());
    System.out.println("[DEBUG] Showing copy dialog for event: " + event.getSubject());
    
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    
    try {
      // Create a simple dialog to get target calendar
      String[] calendarNames = calendarManager.getCalendarRegistry().getCalendarNames().toArray(new String[0]);
      String targetCalendarName = (String) JOptionPane.showInputDialog(
          null,
          "Select target calendar:",
          "Copy Event",
          JOptionPane.QUESTION_MESSAGE,
          null,
          calendarNames,
          calendarNames.length > 0 ? calendarNames[0] : null);
      
      if (targetCalendarName != null) {
        // Get the target calendar
        ICalendar targetCalendar = null;
        try {
          targetCalendar = calendarManager.getCalendar(targetCalendarName);
        } catch (Exception e) {
          view.showErrorMessage("Target calendar not found: " + e.getMessage());
          return;
        }
        
        if (targetCalendar == null) {
          view.showErrorMessage("Target calendar not found");
          return;
        }
        
        // Execute the copy command
        DirectCopyEventCommand copyCommand = new DirectCopyEventCommand(targetCalendar, event);
        
        boolean success = copyCommand.execute();
        if (success) {
          JOptionPane.showMessageDialog(null, "Event copied successfully");
          view.refreshView();
        } else {
          JOptionPane.showMessageDialog(null, "Failed to copy event. There may be a conflict.");
        }
      }
    } catch (Exception e) {
      System.out.println("[ERROR] Error showing copy dialog: " + e.getMessage());
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Error copying event: " + e.getMessage());
    }
  }

  /**
   * Handles a new event being saved from the event panel.
   *
   * @param formData the form data collected from the event panel
   */
  public void onEventSaved(EventFormData formData) {
    try {
      System.out.println("[DEBUG] Saving event: " + formData.getSubject());
      
      if (currentCalendar == null) {
        view.displayError("Please select a calendar first");
        return;
      }

      // Convert EventFormData to the format expected by the command executor
      String[] args = convertFormDataToCommandArgs(formData, "create");
      
      if (args == null || args.length < 2) {
        view.displayError("Invalid event data");
        return;
      }

      String result = executeCommand("create", args);
      if (result.startsWith("Error")) {
        System.out.println("[ERROR] Failed to create event: " + result);
        view.displayError(result);
      } else {
        System.out.println("[DEBUG] Event created successfully: " + formData.getSubject());
        view.displayMessage(result);
        
        // Get the selected date from the form data and convert from Date to LocalDate
        Date selectedDate = formData.getSelectedDate();
        LocalDate eventDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("[DEBUG] Updating display for date: " + eventDate);
        
        // Update the views to reflect the new event
        updateEvents(eventDate);
        view.refreshView();
        
        // Ensure the date is properly selected and displayed
        setSelectedDate(eventDate);
        
        // Clear the form after successful creation
        view.getEventPanel().clearForm();
      }
    } catch (Exception e) {
      System.out.println("[ERROR] Exception while saving event: " + e.getMessage());
      e.printStackTrace();
      view.displayError("Failed to save event: " + e.getMessage());
    }
  }

  /**
   * Handles an event being updated from the event panel.
   *
   * @param formData the form data collected from the event panel
   */
  public void onEventUpdated(EventFormData formData) {
    try {
      System.out.println("[DEBUG] Updating event: " + formData.getSubject());
      System.out.println("[DEBUG] Updated details: Subject=" + formData.getSubject() + 
                         ", Location=" + formData.getLocation() + 
                         ", AllDay=" + formData.isAllDay());
      if (currentCalendar == null) {
        view.displayError("Please select a calendar first");
        return;
      }
      
      Event currentEvent = view.getEventPanel().getCurrentEvent();
      if (currentEvent == null) {
        view.displayError("No event selected for update");
        return;
      }
      
      // Get data from the form
      String subject = formData.getSubject();
      String description = formData.getDescription();
      String location = formData.getLocation();
      boolean isAllDay = formData.isAllDay();
      
      // Get start and end date/time
      LocalDateTime startDateTime;
      LocalDateTime endDateTime;
      
      if (isAllDay) {
        // For all-day events, set time to beginning and end of day
        LocalDate eventDate = formData.getSelectedDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        startDateTime = eventDate.atStartOfDay();
        endDateTime = eventDate.atTime(23, 59, 59);
      } else {
        // Regular events with specific times
        Date startDate = formData.getStartTime();
        Date endDate = formData.getEndTime();
        
        startDateTime = startDate.toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
        endDateTime = endDate.toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDateTime();
      }
      
      // Create updated event
      Event updatedEvent = new Event(
          subject,
          startDateTime,
          endDateTime,
          description,
          location,
          currentEvent.isPublic()
      );
      
      // Update the event in the calendar
      boolean success = currentCalendar.updateEvent(currentEvent.getId(), updatedEvent);
      
      if (success) {
        view.displayMessage("Event updated successfully");
        // Refresh the view to show the updated event
        LocalDate selectedDate = startDateTime.toLocalDate();
        setSelectedDate(selectedDate);
        view.refreshCalendarView();
        view.refreshEventView();
      } else {
        view.displayError("Failed to update event");
      }
    } catch (Exception e) {
      view.displayError("Failed to update event: " + e.getMessage());
    }
  }
  
  /**
   * Handles copying an event.
   *
   * @param event             the event to copy
   * @param targetCalendarName the name of the target calendar
   * @param targetStartDateTime the target start date/time
   * @param targetEndDateTime the target end date/time
   * @return true if the copy was successful, false otherwise
   */
  public boolean copyEvent(Event event, String targetCalendarName, LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime) {
    System.out.println("[DEBUG] Copying event: " + event.getSubject() + " to calendar: " + targetCalendarName);
    
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return false;
    }
    
    try {
      // Find the target calendar by name
      ICalendar targetCalendar = null;
      try {
        targetCalendar = calendarManager.getCalendar(targetCalendarName);
      } catch (Exception e) {
        view.showErrorMessage("Target calendar not found: " + e.getMessage());
        return false;
      }
      
      if (targetCalendar == null) {
        view.showErrorMessage("Target calendar not found");
        return false;
      }
        
      // Create the event in the target calendar directly
      System.out.println("[DEBUG] Copying event directly to target calendar");
      
      // Create a new event with the same details but at the new date/time
      Event copiedEvent = new Event(
          event.getSubject(), // Keep the original subject
          targetStartDateTime,
          targetEndDateTime,
          event.getDescription(),
          event.getLocation(),
          event.isPublic()
      );
      
      // Add the event to the target calendar
      boolean success = false;
      try {
          targetCalendar.addEvent(copiedEvent, false);
          success = true;
      } catch (Exception ex) {
          System.out.println("[ERROR] Failed to add event to target calendar: " + ex.getMessage());
          success = false;
      }
      
      if (success) {
        view.displayMessage("Event copied successfully to " + targetCalendarName);
        view.refreshView();
        return true;
      } else {
        view.showErrorMessage("Failed to copy event");
        return false;
      }
    } catch (Exception e) {
      view.showErrorMessage("Error copying event: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
  
  /**
   * Handles printing an event.
   *
   * @param event the event to print
   */
  public void printEvent(Event event) {
    System.out.println("[DEBUG] Printing event: " + event.getSubject());
    
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    
    try {
      // Format the event details for printing
      StringBuilder eventDetails = new StringBuilder();
      eventDetails.append("Event: ").append(event.getSubject()).append("\n");
      eventDetails.append("Start: ").append(event.getStartDateTime()).append("\n");
      eventDetails.append("End: ").append(event.getEndDateTime()).append("\n");
      
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        eventDetails.append("Location: ").append(event.getLocation()).append("\n");
      }
      
      if (event.getDescription() != null && !event.getDescription().isEmpty()) {
        eventDetails.append("Description: ").append(event.getDescription()).append("\n");
      }
      
      eventDetails.append("Public: ").append(event.isPublic() ? "Yes" : "No").append("\n");
      
      // Display a print dialog or send to printer
      // For now, we'll just display a message with the event details
      view.displayMessage("Printing event:\n" + eventDetails.toString());
      
      // In a real application, we would use the Java printing API here
      // PrinterJob job = PrinterJob.getPrinterJob();
      // job.setPrintable(new EventPrintable(event));
      // if (job.printDialog()) {
      //   job.print();
      // }
    } catch (Exception e) {
      view.showErrorMessage("Error printing event: " + e.getMessage());
    }
  }
  
  /**
   * Executes copying an event to another calendar.
   *
   * @param event             the event to copy
   * @param targetCalendarName the name of the target calendar
   * @return a message indicating the result of the operation
   */
  public String executeCopyEvent(Event event, String targetCalendarName) {
    System.out.println("[DEBUG] Executing copy event: " + event.getSubject() + " to calendar: " + targetCalendarName);
    
    if (event == null) {
      return "Error: No event selected";
    }
    
    try {
      // Get the target calendar
      ICalendar targetCalendar = null;
      try {
        targetCalendar = calendarManager.getCalendar(targetCalendarName);
      } catch (Exception e) {
        return "Error: Target calendar not found";
      }
      
      if (targetCalendar == null) {
        return "Error: Target calendar not found";
      }
        
      // Create a new event with the same details
      Event copiedEvent = new Event(
          event.getSubject(),
          event.getStartDateTime(),
          event.getEndDateTime(),
          event.getDescription(),
          event.getLocation(),
          event.isPublic()
      );
      
      // Add the event to the target calendar
      boolean added = targetCalendar.addEvent(copiedEvent, false);
      
      if (added) {
        // Refresh the view if the target calendar is the current calendar
        if (currentCalendar != null && targetCalendar == currentCalendar) {
          view.refreshView();
        }
        return "Event copied successfully to " + targetCalendarName;
      } else {
        return "Failed to copy event to " + targetCalendarName;
      }
    } catch (Exception e) {
      return "Error copying event: " + e.getMessage();
    }
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
  
  /**
   * Converts EventFormData to command arguments array.
   * 
   * @param formData The form data to convert
   * @param commandType The type of command (create, edit, etc.)
   * @return String array of command arguments
   */
  private String[] convertFormDataToCommandArgs(EventFormData formData, String commandType) {
    if (formData == null) {
      return null;
    }
    
    List<String> args = new ArrayList<>();
    
    // First argument is always the command type
    args.add(commandType);
    
    // Add calendar name if we have a current calendar
    if (currentCalendar != null) {
      args.add(currentCalendar.toString()); // Using toString() instead of getName()
    } else {
      return null; // Cannot proceed without a calendar
    }
    
    // Add subject
    args.add(formData.getSubject());
    
    // Add start date/time
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    args.add(dateFormat.format(formData.getSelectedDate()));
    args.add(timeFormat.format(formData.getStartTime()));
    args.add(timeFormat.format(formData.getEndTime()));
    
    // Add location if present
    if (formData.getLocation() != null && !formData.getLocation().isEmpty()) {
      args.add("-l");
      args.add(formData.getLocation());
    }
    
    // Add description if present
    if (formData.getDescription() != null && !formData.getDescription().isEmpty()) {
      args.add("-d");
      args.add(formData.getDescription());
    }
    
    // Add all-day flag if needed
    if (formData.isAllDay()) {
      args.add("-a");
    }
    
    // Add recurring event options if needed
    if (formData.isRecurring() && formData.getWeekdays() != null && !formData.getWeekdays().isEmpty()) {
      args.add("-r");
      
      // Convert weekdays to string format
      StringBuilder weekdaysStr = new StringBuilder();
      for (DayOfWeek day : formData.getWeekdays()) {
        weekdaysStr.append(day.toString().substring(0, 3).toLowerCase()).append(",");
      }
      // Remove trailing comma
      if (weekdaysStr.length() > 0) {
        weekdaysStr.deleteCharAt(weekdaysStr.length() - 1);
      }
      
      args.add(weekdaysStr.toString());
      
      // Add end date if present
      if (formData.getUntilDate() != null) {
        args.add("-e");
        args.add(formData.getUntilDate().toString());
      }
    }
    
    // Add private flag if needed
    if (formData.isPrivateEvent()) {
      args.add("-p");
    }
    
    // Add auto-decline flag if needed
    if (formData.isAutoDecline()) {
      args.add("-ad");
    }
    
    return args.toArray(new String[0]);
  }
  
  /**
   * Handles the updating of an event after it has been edited.
   *
   * @param event the updated event
   */
  public void onEventUpdated(Event event) {
    System.out.println("[DEBUG] Event updated: " + event.getSubject());
    
    if (currentCalendar == null) {
      view.displayError("Please select a calendar first");
      return;
    }
    
    try {
      // Get the original event properties
      String subject = event.getSubject();
      LocalDateTime startDateTime = event.getStartDateTime();
      LocalDateTime endDateTime = event.getEndDateTime();
      String description = event.getDescription();
      String location = event.getLocation();
      boolean isPublic = event.isPublic();
      
      // Find the original event to verify it exists
      currentCalendar.findEvent(subject, startDateTime);
      
      // Update event properties using editSingleEvent
      boolean updated = true;
      
      // Only update properties that might have changed
      if (endDateTime != null) {
        String endTimeStr = endDateTime.toLocalTime().toString();
        updated = currentCalendar.editSingleEvent(subject, startDateTime, "endTime", endTimeStr) && updated;
      }
      
      if (description != null) {
        updated = currentCalendar.editSingleEvent(subject, startDateTime, "description", description) && updated;
      }
      
      if (location != null) {
        updated = currentCalendar.editSingleEvent(subject, startDateTime, "location", location) && updated;
      }
      
      updated = currentCalendar.editSingleEvent(subject, startDateTime, "isPublic", String.valueOf(isPublic)) && updated;
      
      if (updated) {
        view.showInfoMessage("Event updated successfully: " + event.getSubject());
        view.refreshView();
      } else {
        view.showErrorMessage("Failed to update event due to conflicts");
      }
    } catch (EventNotFoundException e) {
      view.showErrorMessage("Original event not found: " + e.getMessage());
    } catch (InvalidEventException e) {
      view.showErrorMessage("Invalid event data: " + e.getMessage());
    } catch (ConflictingEventException e) {
      view.showErrorMessage("Event conflicts with existing events: " + e.getMessage());
    } catch (Exception e) {
      view.showErrorMessage("Error updating event: " + e.getMessage());
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
        
        // Date parsing based on command format
        if (command.equalsIgnoreCase("create") && args.length >= 6) {
          // If we have date and separate time components (from the form)
          String dateStr = args[3];
          String startTimeStr = args[4];
          // Format: 2025-04-19T10:00
          startDateTime = LocalDateTime.parse(dateStr + "T" + startTimeStr);
        } else {
          // Try to parse the date directly if it's a full date-time string
          try {
            startDateTime = LocalDateTime.parse(args[3]);
          } catch (DateTimeParseException e) {
            System.out.println("[DEBUG] Failed to parse date directly: " + e.getMessage());
            return "Error: Invalid date format. Use yyyy-MM-ddTHH:mm format.";
          }
        }
      }

      switch (command.toLowerCase()) {
        case "create":
          if (args.length < 4) {
            System.out.println("[DEBUG] Invalid number of arguments for create command");
            return "Error: Invalid command arguments";
          }
          
          // Handle form-based event creation (standard case from the GUI form)
          if (args.length >= 6) {
            try {
              System.out.println("[DEBUG] Creating event from form data: " + subject);
              
              // Get start and end times
              String dateStr = args[3];
              String startTimeStr = args[4];
              String endTimeStr = args[5];
              
              // Parse the dates
              LocalDate eventDate = LocalDate.parse(dateStr);
              LocalTime startTime = LocalTime.parse(startTimeStr);
              LocalTime endTime = LocalTime.parse(endTimeStr);
              
              LocalDateTime eventStartDateTime = LocalDateTime.of(eventDate, startTime);
              LocalDateTime endDateTime = LocalDateTime.of(eventDate, endTime);
              
              // Parse optional arguments
              String description = "";
              String location = "";
              boolean isPrivate = false;
              
              // Process optional args
              for (int i = 6; i < args.length; i++) {
                if ("-d".equals(args[i]) && i + 1 < args.length) {
                  description = args[i + 1];
                  i++; // Skip the next argument which is the description value
                } else if ("-l".equals(args[i]) && i + 1 < args.length) {
                  location = args[i + 1];
                  i++; // Skip the next argument which is the location value
                } else if ("-p".equals(args[i])) {
                  isPrivate = true;
                }
              }
              
              // Create the event
              Event event = new Event(
                      subject, 
                      startDateTime, 
                      endDateTime, 
                      description, 
                      location, 
                      !isPrivate // isPublic is the opposite of isPrivate
              );
              
              System.out.println("[DEBUG] Adding event to calendar: Event{subject='" + subject 
                  + "', startDateTime=" + startDateTime 
                  + ", endDateTime=" + endDateTime 
                  + ", isAllDay=false" 
                  + ", location='" + location + "'}");
              boolean added = currentCalendar.addEvent(event, false);
              System.out.println("[DEBUG] Event added: " + added);
              
              if (added) {
                updateEventList(eventStartDateTime.toLocalDate());
                String locationMsg = location != null && !location.isEmpty() ? " at " + location : " at no location";
                view.displayMessage("Event \"" + subject + "\" created successfully" + locationMsg);
                return "Event created successfully";
              } else {
                return "Error: Failed to add event";
              }
            } catch (Exception e) {
              System.out.println("[DEBUG] Error creating event: " + e.getMessage());
              e.printStackTrace();
              return "Error: " + e.getMessage();
            }
          } 
          // Handle the legacy command format with single event type
          else if ("single".equals(eventType)) {
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
            } else {
              return "Error: Failed to add event";
            }
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
      System.out.println("[DEBUG] Setting selected date to: " + date);
      
      // First update the model's knowledge of the selected date
      view.setSelectedDate(date);

      // Then get events for this date
      List<Event> events = getEventsOnDate(date);
      System.out.println("[DEBUG] Found " + events.size() + " events for date " + date);
      
      // Update all relevant view components with a consistent flow
      view.updateSelectedDate(date);
      view.updateEventList(events);
      view.getCalendarPanel().updateEventList(date);
      updateStatus(date);
      
      // Force a refresh to ensure events are visible
      view.getCalendarPanel().repaint();
    } catch (Exception e) {
      System.out.println("[ERROR] Error setting selected date: " + e.getMessage());
      e.printStackTrace();
      view.displayError("Error setting selected date: " + e.getMessage());
    }
  }

}
 
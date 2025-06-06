package controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import controller.command.event.ExportCalendarCommand;
import controller.command.event.ImportCalendarCommand;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import utilities.CalendarNameValidator;
import utilities.TimeZoneHandler;
import view.ButtonStyler;
import view.CalendarViewFeatures;
import view.EventFormData;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import viewmodel.ExportImportViewModel;

/**
 * Controller class that handles GUI-specific logic and coordinates between the model and view.
 */
public class GUIController {
  private final CalendarManager calendarManager;
  private final CalendarViewFeatures view;
  private final TimeZoneHandler timezoneHandler;
  private ICalendar currentCalendar;

  /**
   * Constructs a new GUIController.
   *
   * @param calendarManager the calendar manager
   * @param view            the GUI view implementing CalendarViewFeatures
   */
  public GUIController(CalendarManager calendarManager, CalendarViewFeatures view) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.timezoneHandler = new TimeZoneHandler();
    this.currentCalendar = null;
  }

  /**
   * Initializes the application.
   */
  public void initialize() throws CalendarNotFoundException {
    System.out.println("Initializing GUI controller...");
    try {
      CalendarNameValidator.removeAllCalendarNames();

      String defaultCalendar = "Default_Calendar";
      if (calendarManager.getCalendarCount() == 0) {
        System.out.println("Creating default calendar...");
        String timezone = timezoneHandler.getSystemDefaultTimezone();
        calendarManager.createCalendar(defaultCalendar, timezone);

      }

      System.out.println("Getting first available calendar...");
      currentCalendar = calendarManager.getCalendar(defaultCalendar);
      if (currentCalendar == null) {
        throw new CalendarNotFoundException("No calendars available");
      }
      System.out.println("Setting up view with calendar: " + defaultCalendar);
      view.setSelectedCalendar(defaultCalendar);
      view.updateCalendarView(currentCalendar);
      view.updateCalendarList(new ArrayList<>(calendarManager
              .getCalendarRegistry().getCalendarNames()));

      System.out.println("Setting up event listeners...");
      setupEventListeners();

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
    view.getCalendarSelectorPanel()
            .addCalendarSelectorListener(new GUICalendarSelectorPanel.CalendarSelectorListener() {
              @Override
              public void onCalendarSelected(ICalendar calendar) {
                System.out.println("[DEBUG] Calendar selected (ICalendar): " + calendar);
                try {
                  if (calendar == null) {
                    view.displayError("Please select a valid calendar");
                    return;
                  }
                  System.out.println("[DEBUG] Calendar details: " + calendar.toString());
                  System.out.println("[DEBUG] Calendar has "
                          + calendar.getAllEvents().size() + " events");
                  currentCalendar = calendar;
                  view.updateCalendarView(calendar);

                  // Update the calendar name in the calendar panel
                  view.getCalendarPanel().updateCalendarName(calendar.getName());

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
              public void onCalendarSelected(String calendarName) {
                System.out.println("[DEBUG] Calendar selected by name: " + calendarName);
                try {
                  if (calendarName == null || calendarName.isEmpty()) {
                    view.displayError("Please select a valid calendar");
                    return;
                  }

                  // Get the calendar by name
                  ICalendar calendar = calendarManager.getCalendar(calendarName);
                  if (calendar == null) {
                    System.out.println("[ERROR] Could not find calendar with name: "
                            + calendarName);
                    view.displayError("Could not find calendar: " + calendarName);
                    return;
                  }

                  System.out.println("[DEBUG] Found calendar: " + calendar.toString());
                  System.out.println("[DEBUG] Calendar has " + calendar.getAllEvents().size()
                          + " events");

                  currentCalendar = calendar;

                  view.getCalendarPanel().updateCalendarName(calendarName);

                  view.getCalendarPanel().clearEvents();

                  view.updateCalendarView(calendar);
                  view.setSelectedCalendar(calendarName);

                  LocalDate currentDate = view.getCalendarPanel().getSelectedDate();
                  YearMonth currentMonth = YearMonth.from(currentDate);
                  System.out.println("[DEBUG] Getting events for current month: " + currentMonth);

                  List<Event> allEvents = currentCalendar.getAllEvents();
                  List<RecurringEvent> recurringEvents = currentCalendar.getAllRecurringEvents();
                  System.out.println("[DEBUG] Found " + allEvents.size()
                          + " total events in calendar");
                  System.out.println("[DEBUG] Calendar has " + recurringEvents.size()
                          + " recurring events in total");

                  view.getCalendarPanel().updateEvents(allEvents);
                  view.getCalendarPanel().updateRecurringEvents(recurringEvents);

                  List<Event> eventsOnDate = currentCalendar.getEventsOnDate(currentDate);
                  view.updateEventList(eventsOnDate);

                  view.displayMessage("Selected calendar: " + calendarName);

                  view.refreshView();

                  view.refreshView();
                } catch (Exception e) {
                  System.out.println("[DEBUG] Calendar selection error: " + e.getMessage());
                  e.printStackTrace();
                  view.displayError("Failed to select calendar: " + e.getMessage());
                }
              }

              @Override
              public void onCalendarCreated(String name, String timezone) {
                System.out.println("[DEBUG] Calendar creation initiated: "
                        + name + " with timezone: " + timezone);
                try {
                  // Validate timezone format
                  if (!timezone.contains("/")) {
                    System.out.println("[DEBUG] Invalid timezone format: " + timezone);
                    view.showErrorMessage("Invalid timezone format. "
                            + "Please use Area/Location format.");
                    return;
                  }

                  // Create the calendar
                  calendarManager.createCalendar(name, timezone);
                  System.out.println("[DEBUG] Calendar created");

                  // Update the view
                  view.updateCalendarList(new ArrayList<>(calendarManager
                          .getCalendarRegistry().getCalendarNames()));
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

    view.getCalendarPanel().addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        System.out.println("[DEBUG] Date selected: " + date);
        try {
          if (date == null) {
            view.displayError("Please select a valid date");
            return;
          }
          System.out.println("[DEBUG] Processing date selection for: " + date);
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
      public void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime,
                                LocalDateTime targetEndDateTime) {
        Event currentEvent = view.getEventPanel().getCurrentEvent();
        if (currentEvent == null) {
          view.displayError("No event selected to copy");
          return;
        }

        // Copy the event using the strategy
        boolean success = copyEvent(currentEvent, targetCalendarName, targetStartDateTime,
                targetEndDateTime);
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

    view.getExportImportPanel()
            .addExportImportListener(new GUIExportImportPanel.ExportImportListener() {
              @Override
              public void onImport(File file) {
                if (file != null) {
                  try {
                    ExportImportViewModel exportImportViewModel = view.getExportImportViewModel();
                    exportImportViewModel.setCalendarManager(calendarManager);
                    exportImportViewModel.setView(view);
                    ICalendar currentCalendar = exportImportViewModel.findSuitableCalendar();

                    if (currentCalendar == null) {
                      view.showErrorMessage("No calendar available. "
                              + "Please create a calendar first.");
                      return;
                    }

                    ImportCalendarCommand importCommand = new ImportCalendarCommand(
                            currentCalendar, exportImportViewModel, view);

                    String result = importCommand.importFromFile(file);

                    if (!result.startsWith("Successfully")) {
                      view.showErrorMessage(result);
                    }
                  } catch (Exception e) {
                    view.showErrorMessage("Failed to import calendar data: " + e.getMessage());
                  }
                }
              }

              @Override
              public void onExport(File file) {
                try {
                  ExportImportViewModel exportImportViewModel = view.getExportImportViewModel();
                  exportImportViewModel.setCalendarManager(calendarManager);
                  exportImportViewModel.setView(view);

                  ICalendar calendarToExport = GUIController.this.currentCalendar;
                  if (calendarToExport == null) {
                    calendarToExport = exportImportViewModel.findSuitableCalendar();
                  }

                  if (calendarToExport == null) {
                    view.displayError("Please select a calendar first");
                    return;
                  }

                  ExportCalendarCommand exportCommand = new ExportCalendarCommand(
                          calendarToExport, exportImportViewModel, view);

                  String result = exportCommand.exportToFile(file);

                  if (result.startsWith("Calendar exported")) {
                    view.displayMessage("Export Successful: " + result);
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

      List<Event> localEvents = new ArrayList<>();
      String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

      for (Event event : events) {
        LocalDateTime localStartDateTime = timezoneHandler.convertFromUTC(event
                .getStartDateTime(), systemTimezone);
        if (localStartDateTime.toLocalDate().equals(date)) {
          localEvents.add(event);
        }
      }

      boolean isBusy = !localEvents.isEmpty();
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
      System.out.println("[DEBUG] Listing events for date: " + date);
      System.out.println("[DEBUG] Current calendar: " + (currentCalendar != null
              ? currentCalendar.getName() : "null"));

      if (currentCalendar == null) {
        view.displayError("No calendar selected");
        return;
      }

      List<Event> events = currentCalendar.getEventsOnDate(date);
      System.out.println("[DEBUG] Found " + events.size() + " events for date " + date);

      view.updateEventList(events);
      view.getCalendarPanel().updateEventList(date);

      if (events.isEmpty()) {
        view.displayMessage("No events found for " + date);
      } else {
        view.displayMessage("Found " + events.size() + " events for " + date);
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to list events: " + e.getMessage());
      e.printStackTrace();
      view.displayError("Failed to list events: " + e.getMessage());
    }
  }

  /**
   * Shows events in the specified date range.
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
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

      System.out.println("[DEBUG] Showing events in range: " + startDate + " to " + endDate);
      System.out.println("[DEBUG] Current calendar: " + (currentCalendar != null
              ? currentCalendar.getName() : "null"));

      if (currentCalendar == null) {
        view.displayError("No calendar selected");
        return;
      }

      // Get events in the date range from the current calendar
      List<Event> events = currentCalendar.getEventsInRange(startDate, endDate);
      System.out.println("[DEBUG] Found " + events.size() + " events in date range");

      // Update the event list with the range of events
      view.getCalendarPanel().updateEventListRange(startDate, endDate, events);

      if (events.isEmpty()) {
        view.displayMessage("No events found in selected date range");
      } else {
        view.displayMessage("Found " + events.size() + " events in selected date range");
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to get events in range: " + e.getMessage());
      e.printStackTrace();
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
      if (!e.getMessage().contains("Insufficient arguments")) {
        view.displayError("Failed to get all recurring events: " + e.getMessage());
      }
      return List.of();
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
    System.out.println("[DEBUG] Event details: ID=" + event.getId() + ", start="
            + event.getStartDateTime() + ", end=" + event.getEndDateTime());

    try {
      System.out.println("[DEBUG] About to show edit popup image");
      if (view instanceof JFrame) {
        ButtonStyler.showEditEventPopup((JFrame) view);
      } else {
        System.out.println("[DEBUG] View is not a JFrame, using alternative approach for popup");
        ButtonStyler.showEditEventPopup(null);
      }
      System.out.println("[DEBUG] Edit popup image display method called");
    } catch (Exception e) {
      System.err.println("[ERROR] Error showing edit popup: " + e.getMessage());
      e.printStackTrace();
    }

    try {
      System.out.println("[DEBUG] About to show event edit dialog");
      view.showEventEditDialog(event, false);
      System.out.println("[DEBUG] Event edit dialog displayed");
    } catch (Exception e) {
      System.err.println("[ERROR] Error showing edit dialog: " + e.getMessage());
      e.printStackTrace();
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

      String[] args = convertFormDataToCommandArgs(formData, "create");

      if (args == null || args.length < 2) {
        view.displayError("Invalid event data");
        return;
      }
      System.out.println("[DEBUG] Creating event with following parameters:");
      System.out.println("[DEBUG] Subject: " + formData.getSubject());
      System.out.println("[DEBUG] Start Time: " + formData.getStartTime());
      System.out.println("[DEBUG] End Time: " + formData.getEndTime());
      System.out.println("[DEBUG] Is Recurring: " + formData.isRecurring());

      if (formData.isRecurring()) {
        System.out.println("[DEBUG] Recurring event details:");
        System.out.println("[DEBUG] Repeat days: " + formData.getWeekdays());
        if (formData.getOccurrences() > 0) {
          System.out.println("[DEBUG] Occurrences: " + formData.getOccurrences());
        } else if (formData.getRecurringEndDate() != null) {
          System.out.println("[DEBUG] End date: " + formData.getRecurringEndDate());
        }
      }

      String result = executeCommand("create", args);
      if (result.startsWith("Error")) {
        System.out.println("[ERROR] Failed to create event: " + result);
        view.displayError(result);
      } else {
        System.out.println("[DEBUG] Event created successfully: " + formData.getSubject());
        System.out.println("[DEBUG] Result from command: " + result);
        view.displayMessage(result);

        Date selectedDate = formData.getSelectedDate();
        LocalDate eventDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println("[DEBUG] Updating display for date: " + eventDate);

        List<Event> allEvents = currentCalendar.getAllEvents();
        System.out.println("[DEBUG] Total events in calendar after creation: " + allEvents.size());
        view.getCalendarPanel().updateEvents(allEvents);

        List<RecurringEvent> recurringEvents = currentCalendar.getAllRecurringEvents();
        System.out.println("[DEBUG] Total recurring events in calendar: " + recurringEvents.size());

        if (!recurringEvents.isEmpty() && formData.isRecurring()) {
          RecurringEvent lastAdded = recurringEvents.get(recurringEvents.size() - 1);
          System.out.println("*************************************************************");
          System.out.println("[DEBUG-RECURRING] Latest recurring event details:");
          System.out.println("[DEBUG-RECURRING] Subject: " + lastAdded.getSubject());
          System.out.println("[DEBUG-RECURRING] Start Date/Time: " + lastAdded.getStartDateTime());
          System.out.println("[DEBUG-RECURRING] End Date/Time: " + lastAdded.getEndDateTime());
          System.out.println("[DEBUG-RECURRING] Location: " + lastAdded.getLocation());
          System.out.println("[DEBUG-RECURRING] Description: " + lastAdded.getDescription());
          System.out.println("[DEBUG-RECURRING] Repeat days: " + lastAdded.getRepeatDays());
          System.out.println("[DEBUG-RECURRING] All-day: " + lastAdded.isAllDay());

          if (lastAdded.getOccurrences() > 0) {
            System.out.println("[DEBUG-RECURRING] Occurrences limit: "
                    + lastAdded.getOccurrences());
          } else if (lastAdded.getEndDate() != null) {
            System.out.println("[DEBUG-RECURRING] End date: " + lastAdded.getEndDate());
          }
          System.out.println("[DEBUG-RECURRING] Recurring ID: " + lastAdded.getRecurringId());

          LocalDate today = LocalDate.now();

          LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
          LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
          List<Event> weeklyOccurrences = lastAdded.getOccurrencesBetween(startOfWeek, endOfWeek);
          System.out.println("[DEBUG-RECURRING] Generated " + weeklyOccurrences.size()
                  + " occurrences for current week");

          LocalDate firstOfMonth = today.withDayOfMonth(1);
          LocalDate lastOfMonth = today.withDayOfMonth(today.lengthOfMonth());
          List<Event> monthlyOccurrences = lastAdded.getOccurrencesBetween(firstOfMonth,
                  lastOfMonth);
          System.out.println("[DEBUG-RECURRING] Generated " + monthlyOccurrences.size()
                  + " occurrences for current month");

          System.out.println("[DEBUG-RECURRING] Sample occurrences:");
          List<Event> allOccurrences = lastAdded.getAllOccurrences();
          int occurrencesToShow = Math.min(5, allOccurrences.size());
          for (int i = 0; i < occurrencesToShow; i++) {
            Event occurrence = allOccurrences.get(i);
            System.out.println("[DEBUG-RECURRING] #" + (i + 1) + ": "
                    + occurrence.getStartDateTime().toLocalDate()
                    + " (" + occurrence.getStartDateTime().getDayOfWeek()
                    + ") with ID: " + occurrence.getId());
          }
          System.out.println("*************************************************************");
        }

        view.getCalendarPanel().updateRecurringEvents(recurringEvents);

        updateEvents(eventDate);

        view.refreshView();
        setSelectedDate(eventDate);
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

      String subject = formData.getSubject();
      String description = formData.getDescription();
      String location = formData.getLocation();
      boolean isAllDay = formData.isAllDay();

      LocalDateTime startDateTime;
      LocalDateTime endDateTime;

      if (isAllDay) {
        LocalDate eventDate = formData.getSelectedDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        startDateTime = eventDate.atStartOfDay();
        endDateTime = eventDate.atTime(23, 59, 59);
      } else {
        Date startDate = formData.getStartTime();
        Date endDate = formData.getEndTime();

        System.out.println("[DEBUG] GUIController.onEventUpdated - Raw start time: " + startDate);
        System.out.println("[DEBUG] GUIController.onEventUpdated - Raw end time: " + endDate);

        LocalDate selectedDate = formData.getSelectedDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        int startHour = startCal.get(Calendar.HOUR_OF_DAY);
        int startMinute = startCal.get(Calendar.MINUTE);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        int endHour = endCal.get(Calendar.HOUR_OF_DAY);
        int endMinute = endCal.get(Calendar.MINUTE);

        startDateTime = selectedDate.atTime(startHour, startMinute);
        endDateTime = selectedDate.atTime(endHour, endMinute);

        System.out.println("[DEBUG] GUIController.onEventUpdated - Parsed start time: "
                + startDateTime);
        System.out.println("[DEBUG] GUIController.onEventUpdated - Parsed end time: "
                + endDateTime);

        if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
          System.out.println("[ERROR] GUIController.onEventUpdated - End time validation failed: " +
                  "Start=" + startDateTime + ", End=" + endDateTime);
          view.displayError("End date/time cannot be before or equal to start date/time");
          return;
        }
      }

      // Convert times from local timezone to UTC for storage
      TimeZoneHandler timezoneHandler = new TimeZoneHandler();
      String systemTimezone = timezoneHandler.getSystemDefaultTimezone();
      String calendarTimezone = currentCalendar.getTimeZone().getID();

      // Convert to UTC for storage
      LocalDateTime utcStartDateTime = timezoneHandler.convertToUTC(startDateTime, systemTimezone);
      LocalDateTime utcEndDateTime = timezoneHandler.convertToUTC(endDateTime, systemTimezone);

      System.out.println("[DEBUG] Event times - Local Start: " + startDateTime);
      System.out.println("[DEBUG] Event times - Local End: " + endDateTime);
      System.out.println("[DEBUG] Event times - UTC Start: " + utcStartDateTime);
      System.out.println("[DEBUG] Event times - UTC End: " + utcEndDateTime);
      System.out.println("[DEBUG] Calendar timezone: " + calendarTimezone);

      try {
        Event updatedEvent = new Event(
                currentEvent.getId(),
                subject,
                utcStartDateTime,
                utcEndDateTime,
                description,
                location,
                currentEvent.isPublic(),
                currentEvent.isAllDay()
        );

        System.out.println("[DEBUG] GUIController.onEventUpdated - Updating event with ID: "
                + currentEvent.getId());
        System.out.println("[DEBUG] GUIController.onEventUpdated - Event details before update: "
                + "Subject=" + currentEvent.getSubject()
                + ", Location=" + currentEvent.getLocation()
                + ", Start=" + currentEvent.getStartDateTime()
                + ", End=" + currentEvent.getEndDateTime());
        System.out.println("[DEBUG] GUIController.onEventUpdated - Event details after update: "
                + "Subject=" + updatedEvent.getSubject()
                + ", Location=" + updatedEvent.getLocation()
                + ", Start=" + updatedEvent.getStartDateTime()
                + ", End=" + updatedEvent.getEndDateTime());

        boolean success = currentCalendar.updateEvent(currentEvent.getId(), updatedEvent);

        if (success) {
          System.out.println("[DEBUG] GUIController.onEventUpdated - Update successful");
          view.displayMessage("Event updated successfully");
          LocalDate selectedDate = startDateTime.toLocalDate();
          setSelectedDate(selectedDate);
          List<Event> updatedEvents = currentCalendar.getEventsOnDate(selectedDate);
          view.updateEvents(selectedDate, updatedEvents);

          Event refreshedEvent = null;
          for (Event event : updatedEvents) {
            System.out.println("[DEBUG] GUIController.onEventUpdated - Found event in list: " +
                    "ID=" + event.getId() +
                    ", Subject=" + event.getSubject() +
                    ", Location=" + event.getLocation());
            if (event.getId().equals(updatedEvent.getId())) {
              refreshedEvent = event;
              break;
            }
          }

          if (refreshedEvent != null) {
            System.out.println("[DEBUG] GUIController.onEventUpdated - Displaying updated event: "
                    + refreshedEvent.getSubject());
            view.showEventDetails(refreshedEvent);
          } else {
            System.out.println("[DEBUG] GUIController.onEventUpdated "
                    + "- Updated event not found in list, using original");
            view.showEventDetails(updatedEvent);
          }

          // Refresh all views
          view.refreshCalendarView();
          view.refreshEventView();
          view.refreshView();
        } else {
          System.out.println("[ERROR] GUIController.onEventUpdated - Failed to update event");
          view.displayError("Failed to update event");
        }
      } catch (IllegalArgumentException e) {
        System.out.println("[ERROR] Validation error: " + e.getMessage());
        view.displayError("Failed to update event: " + e.getMessage());
      }
    } catch (Exception e) {
      System.out.println("[ERROR] Exception while updating event: " + e.getMessage());
      e.printStackTrace();
      view.displayError("Failed to update event: " + e.getMessage());
    }
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
      String subject = event.getSubject();
      LocalDateTime startDateTime = event.getStartDateTime();
      LocalDateTime endDateTime = event.getEndDateTime();
      String description = event.getDescription();
      String location = event.getLocation();
      boolean isPublic = event.isPublic();

      currentCalendar.findEvent(subject, startDateTime);

      boolean updated = true;
      if (endDateTime != null) {
        String endTimeStr = endDateTime.toLocalTime().toString();
        updated = currentCalendar.editSingleEvent(subject, startDateTime,
                "endTime", endTimeStr) && updated;
      }

      if (description != null) {
        updated = currentCalendar.editSingleEvent(subject, startDateTime,
                "description", description) && updated;
      }

      if (location != null) {
        updated = currentCalendar.editSingleEvent(subject, startDateTime,
                "location", location) && updated;
      }

      updated = currentCalendar.editSingleEvent(subject, startDateTime,
              "isPublic", String.valueOf(isPublic)) && updated;

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

  /**
   * Handles copying an event.
   *
   * @param event               the event to copy
   * @param targetCalendarName  the name of the target calendar
   * @param targetStartDateTime the target start date/time
   * @param targetEndDateTime   the target end date/time
   * @return true if the copy was successful, false otherwise
   */
  public boolean copyEvent(Event event, String targetCalendarName,
                           LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime) {
    System.out.println("[DEBUG] Copying event: " + event.getSubject()
            + " to calendar: " + targetCalendarName);

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

      System.out.println("[DEBUG] Copying event directly to target calendar");

      Event copiedEvent = new Event(
              event.getSubject(), // Keep the original subject
              targetStartDateTime,
              targetEndDateTime,
              event.getDescription(),
              event.getLocation(),
              event.isPublic()
      );

      try {
        targetCalendar.addEvent(copiedEvent, true);
        view.displayMessage("Event copied successfully to " + targetCalendarName);
        view.refreshView();
        return true;
      } catch (Exception ex) {
        System.out.println("[ERROR] Failed to add event to target calendar: " + ex.getMessage());
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

      view.displayMessage("Printing event:\n" + eventDetails.toString());
    } catch (Exception e) {
      view.showErrorMessage("Error printing event: " + e.getMessage());
    }
  }

  /**
   * Handles the closing of the application.
   */
  public void handleApplicationClose() {
    try {
      view.displayMessage("Saving changes...");
      view.displayMessage("Application closed successfully");
    } catch (Exception e) {
      view.displayError("Error while closing application: " + e.getMessage());
    }
  }

  /**
   * Converts EventFormData to command arguments array.
   *
   * @param formData    The form data to convert
   * @param commandType The type of command (create, edit, etc.)
   * @return String array of command arguments
   */
  private String[] convertFormDataToCommandArgs(EventFormData formData, String commandType) {
    if (formData == null) {
      return null;
    }

    List<String> args = new ArrayList<>();

    args.add(commandType);

    if (currentCalendar != null) {
      args.add(currentCalendar.toString());
    } else {
      return null;
    }

    args.add(formData.getSubject());

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    args.add(dateFormat.format(formData.getSelectedDate()));
    args.add(timeFormat.format(formData.getStartTime()));
    args.add(timeFormat.format(formData.getEndTime()));

    if (formData.getLocation() != null && !formData.getLocation().isEmpty()) {
      args.add("-l");
      args.add(formData.getLocation());
    }

    if (formData.getDescription() != null && !formData.getDescription().isEmpty()) {
      args.add("-d");
      args.add(formData.getDescription());
    }

    if (formData.isAllDay()) {
      args.add("-a");
    }

    if (formData.isRecurring() && formData.getWeekdays() != null
            && !formData.getWeekdays().isEmpty()) {
      args.add("-r");

      StringBuilder weekdaysStr = new StringBuilder();
      for (DayOfWeek day : formData.getWeekdays()) {
        weekdaysStr.append(day.toString().substring(0, 3).toLowerCase()).append(",");
      }
      if (weekdaysStr.length() > 0) {
        weekdaysStr.deleteCharAt(weekdaysStr.length() - 1);
      }

      args.add(weekdaysStr.toString());

      if (formData.getUntilDate() != null) {
        args.add("-e");
        args.add(formData.getUntilDate().toString());
      }
    }

    if (formData.isPrivateEvent()) {
      args.add("-p");
    }

    if (formData.isAutoDecline()) {
      args.add("-ad");
    }

    return args.toArray(new String[0]);
  }

  /**
   * Executes a calendar command with the given arguments.
   * This method processes commands like "create" and "edit" to manage calendar events.
   * It handles both single events and recurring events with various parameters.
   *
   * @param command the command to execute (e.g., "create", "edit")
   * @param args the arguments for the command, including event details such as subject,
   *             date, time, location, and other properties
   * @return a String message indicating the result of command execution (success or error message)
   */
  public String executeCommand(String command, String[] args) {
    System.out.println("[DEBUG] Executing command: " + command
            + " with args: " + String.join(", ", args));
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

        if (command.equalsIgnoreCase("create") && args.length >= 6) {
          String dateStr = args[3];
          String startTimeStr = args[4];
          startDateTime = LocalDateTime.parse(dateStr + "T" + startTimeStr);
        } else {
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

          if (args.length >= 6) {
            try {
              System.out.println("[DEBUG] Creating event from form data: " + subject);

              String dateStr = args[3];
              String startTimeStr = args[4];
              String endTimeStr = args[5];

              LocalDate eventDate = LocalDate.parse(dateStr);
              LocalTime startTime = LocalTime.parse(startTimeStr);
              LocalTime endTime = LocalTime.parse(endTimeStr);

              LocalDateTime eventStartDateTime = LocalDateTime.of(eventDate, startTime);
              LocalDateTime endDateTime = LocalDateTime.of(eventDate, endTime);
              String description = "";
              String location = "";
              boolean isPrivate = false;

              for (int i = 6; i < args.length; i++) {
                if ("-d".equals(args[i]) && i + 1 < args.length) {
                  description = args[i + 1];
                  i++;
                } else if ("-l".equals(args[i]) && i + 1 < args.length) {
                  location = args[i + 1];
                  i++;
                } else if ("-p".equals(args[i])) {
                  isPrivate = true;
                }
              }

              boolean isRecurring = false;
              Set<DayOfWeek> repeatDays = new HashSet<>();
              LocalDate untilDate = null;
              int occurrences = -1;

              for (int i = 6; i < args.length; i++) {
                if ("-r".equals(args[i]) && i + 1 < args.length) {
                  isRecurring = true;
                  String[] weekdays = args[i + 1].split(",");
                  for (String day : weekdays) {
                    switch (day.toLowerCase()) {
                      case "mon":
                        repeatDays.add(DayOfWeek.MONDAY);
                        break;
                      case "tue":
                        repeatDays.add(DayOfWeek.TUESDAY);
                        break;
                      case "wed":
                        repeatDays.add(DayOfWeek.WEDNESDAY);
                        break;
                      case "thu":
                        repeatDays.add(DayOfWeek.THURSDAY);
                        break;
                      case "fri":
                        repeatDays.add(DayOfWeek.FRIDAY);
                        break;
                      case "sat":
                        repeatDays.add(DayOfWeek.SATURDAY);
                        break;
                      case "sun":
                        repeatDays.add(DayOfWeek.SUNDAY);
                        break;
                      default:
                        System.out.println("[DEBUG] Unknown day: " + day);
                    }
                  }
                  i++;
                } else if ("-e".equals(args[i]) && i + 1 < args.length) {
                  untilDate = LocalDate.parse(args[i + 1]);
                  i++;
                } else if ("-o".equals(args[i]) && i + 1 < args.length) {
                  occurrences = Integer.parseInt(args[i + 1]);
                  i++;
                }
              }

              boolean added;

              if (isRecurring && !repeatDays.isEmpty()) {
                System.out.println("[DEBUG] Creating recurring event with repeat days: "
                        + repeatDays);

                UUID recurringSeriesId = UUID.randomUUID();

                RecurringEvent.Builder builder = new RecurringEvent.Builder(
                        subject,
                        startDateTime,
                        endDateTime,
                        repeatDays
                )
                        .description(description)
                        .location(location)
                        .isPublic(!isPrivate)
                        .recurringId(recurringSeriesId);

                if (untilDate != null) {
                  builder = builder.endDate(untilDate);
                  System.out.println("[DEBUG] Recurring event will repeat until: " + untilDate);
                } else if (occurrences > 0) {
                  builder = builder.occurrences(occurrences);
                  System.out.println("[DEBUG] Recurring event will have "
                          + occurrences + " occurrences");
                } else {
                  builder = builder.occurrences(10);
                  System.out.println("[DEBUG] Defaulting to 10 occurrences for recurring event");
                }

                RecurringEvent builtEvent = builder.build();
                System.out.println("[DEBUG] Adding recurring event to calendar: "
                        + builtEvent.getSubject());
                added = currentCalendar.addRecurringEvent(builtEvent, false);
                System.out.println("[DEBUG] Recurring event added: " + added);
              } else {
                Event event = new Event(
                        subject,
                        startDateTime,
                        endDateTime,
                        description,
                        location,
                        !isPrivate
                );

                System.out.println("[DEBUG] Adding event to calendar: Event{subject='" + subject
                        + "', startDateTime=" + startDateTime
                        + ", endDateTime=" + endDateTime
                        + ", isAllDay=false"
                        + ", location='" + location + "'}");
                added = currentCalendar.addEvent(event, false);
                System.out.println("[DEBUG] Event added: " + added);
              }

              if (added) {
                LocalDate newEventDate = eventStartDateTime.toLocalDate();
                updateEventList(newEventDate);
                view.refreshCalendarView();
                setSelectedDate(newEventDate);

                String locationMsg = location != null && !location.isEmpty() ? " at "
                        + location : " at no location";
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
          } else if ("single".equals(eventType)) {
            System.out.println("[DEBUG] Creating single event: " + subject);
            String[] eventDetails = args[4].split(",");
            if (eventDetails.length < 4) {
              System.out.println("[DEBUG] Invalid event details format");
              return "Error: Invalid event details format";
            }

            Event event = new Event(
                    eventDetails[0],
                    LocalDateTime.parse(eventDetails[1]),
                    LocalDateTime.parse(eventDetails[2]),
                    eventDetails.length > 4 ? eventDetails[4] : "",
                    eventDetails[3],
                    true
            );
            System.out.println("[DEBUG] Adding event to calendar: " + event);
            boolean added = currentCalendar.addEvent(event, false);
            System.out.println("[DEBUG] Event added: " + added);
            if (added) {
              LocalDate singleEventDate = startDateTime.toLocalDate();
              updateEventList(singleEventDate);
              view.refreshCalendarView();
              setSelectedDate(singleEventDate);  // This updates all relevant view components
              view.getCalendarPanel().repaint();
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

            Set<DayOfWeek> weekdays = Arrays.stream(eventDetails[5]
                            .replaceAll("[\\[\\]]", "").split(", "))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());

            RecurringEvent recurringEvent = new RecurringEvent.Builder(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    weekdays // repeatDays
            )
                    .description("") // description
                    .location(eventDetails[3]) // location
                    .isPublic(true) // isPublic
                    .occurrences(Integer.parseInt(eventDetails[4])) // occurrences
                    .endDate(LocalDate.parse(eventDetails[6])) // untilDate
                    .build();
            System.out.println("[DEBUG] Adding recurring event to calendar: " + recurringEvent);
            boolean added = currentCalendar.addRecurringEvent(recurringEvent, false);
            System.out.println("[DEBUG] Recurring event added: " + added);
            if (added) {
              LocalDate recurringEventDate = startDateTime.toLocalDate();
              updateEventList(recurringEventDate);
              view.refreshCalendarView();
              setSelectedDate(recurringEventDate);  // This updates all relevant view components
              view.getCalendarPanel().repaint(); // Force repaint
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
              // Refresh all calendar views to immediately display the updated event
              LocalDate updatedEventDate = startDateTime.toLocalDate();
              updateEventList(updatedEventDate);
              view.refreshCalendarView();
              setSelectedDate(updatedEventDate);  // This updates all relevant view components
              view.getCalendarPanel().repaint(); // Force repaint
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

            Set<DayOfWeek> weekdays = Arrays.stream(eventDetails[5]
                            .replaceAll("[\\[\\]]", "").split(", "))
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet());

            RecurringEvent recurringEvent = new RecurringEvent.Builder(
                    eventDetails[0], // subject
                    LocalDateTime.parse(eventDetails[1]), // startDateTime
                    LocalDateTime.parse(eventDetails[2]), // endDateTime
                    weekdays // repeatDays
            )
                    .description("") // description
                    .location(eventDetails[3]) // location
                    .isPublic(true) // isPublic
                    .occurrences(Integer.parseInt(eventDetails[4])) // occurrences
                    .endDate(LocalDate.parse(eventDetails[6])) // untilDate
                    .build();
            System.out.println("[DEBUG] Updating recurring event in calendar: " + recurringEvent);
            boolean updated = currentCalendar.addRecurringEvent(recurringEvent, false);
            System.out.println("[DEBUG] Recurring event updated: " + updated);
            if (updated) {
              // Refresh all calendar views to immediately display the updated recurring event
              LocalDate updatedRecurringDate = startDateTime.toLocalDate();
              updateEventList(updatedRecurringDate);
              view.refreshCalendarView();
              setSelectedDate(updatedRecurringDate);  // This updates all relevant view components
              view.getCalendarPanel().repaint(); // Force repaint
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
   * @param date the date to set as selected
   */
  private void setSelectedDate(LocalDate date) {
    if (date == null) {
      return;
    }
    System.out.println("[DEBUG] Setting selected date to: " + date);
    view.getEventPanel().clearForm();
    view.getEventPanel().setDate(date);
    view.getCalendarPanel().setSelectedDate(date);
    updateEvents(date);
    updateStatus(date);
  }

}
 
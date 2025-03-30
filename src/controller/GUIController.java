package controller;

import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarNotFoundException;
import model.exceptions.ConflictingEventException;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.exceptions.InvalidTimezoneException;
import utilities.TimeZoneHandler;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import view.GUICalendarSelectorPanel;
import view.GUIView;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Controller class that manages all GUI-specific logic and coordinates between
 * GUI components and the model. This controller handles user interactions from
 * the GUI and updates the model accordingly.
 */
public class GUIController {
    private final GUIView view;
    private final CalendarManager calendarManager;
    private final TimeZoneHandler timezoneHandler;
    private ICalendar currentCalendar;

    /**
     * Constructs a new GUIController.
     *
     * @param view the GUI view
     * @param calendarManager the calendar manager
     * @param timezoneHandler the timezone handler
     */
    public GUIController(GUIView view, CalendarManager calendarManager, TimeZoneHandler timezoneHandler) {
        this.view = view;
        this.calendarManager = calendarManager;
        this.timezoneHandler = timezoneHandler;
        this.currentCalendar = null;

        setupListeners();
    }

    /**
     * Sets up event listeners for all GUI components.
     */
    private void setupListeners() {
        // Calendar selection listeners
        view.getCalendarSelectorPanel().addCalendarSelectionListener(
            new GUICalendarSelectorPanel.CalendarSelectionListener() {
                @Override
                public void onCalendarSelected(String calendarName) {
                    handleCalendarSelection(calendarName);
                }

                @Override
                public void onCalendarCreated(String calendarName, String timezone) {
                    handleCalendarCreation(calendarName, timezone);
                }
            }
        );

        // Event panel listeners
        view.getEventPanel().addEventPanelListener(
            new GUIEventPanel.EventPanelListener() {
                @Override
                public void onEventSaved(GUIEventPanel.EventData eventData) {
                    handleEventSave(eventData);
                }

                @Override
                public void onEventCancelled() {
                    view.getEventPanel().clearForm();
                }
            }
        );

        // Export/Import listeners
        view.getExportImportPanel().addExportImportListener(
            new GUIExportImportPanel.ExportImportListener() {
                @Override
                public void onImportRequested(File file) {
                    handleImportRequest(file);
                }

                @Override
                public void onExportRequested(File file) {
                    handleExportRequest(file);
                }
            }
        );
    }

    // Calendar Management Methods

    /**
     * Handles calendar selection.
     *
     * @param calendarName the name of the selected calendar
     */
    private void handleCalendarSelection(String calendarName) {
        try {
            currentCalendar = calendarManager.getCalendar(calendarName);
            view.displayMessage("Selected calendar: " + calendarName);
        } catch (CalendarNotFoundException e) {
            view.displayError("Calendar not found: " + calendarName);
        }
    }

    /**
     * Handles calendar creation.
     *
     * @param calendarName the name of the new calendar
     * @param timezone the timezone of the new calendar
     */
    private void handleCalendarCreation(String calendarName, String timezone) {
        try {
            calendarManager.createCalendar(calendarName, timezone);
            view.displayMessage("Created new calendar: " + calendarName);
        } catch (Exception e) {
            view.displayError("Failed to create calendar: " + e.getMessage());
        }
    }

    /**
     * Gets the currently selected calendar.
     *
     * @return the selected calendar
     * @throws CalendarNotFoundException if no calendar is selected
     */
    public ICalendar getSelectedCalendar() throws CalendarNotFoundException {
        String calendarName = view.getCalendarSelectorPanel().getSelectedCalendar();
        if (calendarName == null) {
            throw new CalendarNotFoundException("No calendar selected");
        }
        return calendarManager.getCalendar(calendarName);
    }

    /**
     * Gets the name of the currently selected calendar.
     *
     * @return the name of the selected calendar
     */
    public String getSelectedCalendarName() {
        return view.getCalendarSelectorPanel().getSelectedCalendar();
    }

    /**
     * Gets all calendar names.
     *
     * @return set of calendar names
     */
    public Set<String> getCalendarNames() {
        return calendarManager.getCalendarNames();
    }

    /**
     * Gets the number of calendars.
     *
     * @return the number of calendars
     */
    public int getCalendarCount() {
        return calendarManager.getCalendarCount();
    }

    /**
     * Sets the active calendar.
     *
     * @param name the name of the calendar to set as active
     * @throws CalendarNotFoundException if no calendar with the specified name exists
     */
    public void setActiveCalendar(String name) throws CalendarNotFoundException {
        calendarManager.setActiveCalendar(name);
    }

    /**
     * Gets the name of the currently active calendar.
     *
     * @return the name of the active calendar
     */
    public String getActiveCalendarName() {
        return calendarManager.getActiveCalendarName();
    }

    /**
     * Validates a timezone.
     *
     * @param timezone the timezone to validate
     * @return true if the timezone is valid
     */
    public boolean isValidTimezone(String timezone) {
        return timezoneHandler.isValidTimezone(timezone);
    }

    /**
     * Gets the system default timezone.
     *
     * @return the system default timezone
     */
    public String getSystemDefaultTimezone() {
        return timezoneHandler.getSystemDefaultTimezone();
    }

    // Event Management Methods

    /**
     * Handles event saving.
     *
     * @param eventData the event data to save
     */
    private void handleEventSave(GUIEventPanel.EventData eventData) {
        if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
        }

        try {
            if (eventData.isRecurring) {
                saveRecurringEvent(eventData);
            } else {
                saveSingleEvent(eventData);
            }
            view.getEventPanel().clearForm();
            view.displayMessage("Event saved successfully");
        } catch (Exception e) {
            view.displayError("Failed to save event: " + e.getMessage());
        }
    }

    /**
     * Saves a single event.
     *
     * @param eventData the event data
     * @throws InvalidEventException if the event data is invalid
     * @throws ConflictingEventException if there's a scheduling conflict
     */
    private void saveSingleEvent(GUIEventPanel.EventData eventData)
            throws InvalidEventException, ConflictingEventException {
        LocalDateTime startDateTime = LocalDateTime.of(eventData.date, eventData.startTime);
        LocalDateTime endDateTime = LocalDateTime.of(eventData.date, eventData.endTime);

        Event event = new Event(
            eventData.subject,
            startDateTime,
            endDateTime,
            eventData.location,
            eventData.description
        );

        currentCalendar.addEvent(event, true);
    }

    /**
     * Saves a recurring event.
     *
     * @param eventData the event data
     * @throws InvalidEventException if the event data is invalid
     * @throws ConflictingEventException if there's a scheduling conflict
     */
    private void saveRecurringEvent(GUIEventPanel.EventData eventData)
            throws InvalidEventException, ConflictingEventException {
        LocalDateTime startDateTime = LocalDateTime.of(eventData.date, eventData.startTime);
        LocalDateTime endDateTime = LocalDateTime.of(eventData.date, eventData.endTime);

        RecurringEvent event = new RecurringEvent(
            eventData.subject,
            startDateTime,
            endDateTime,
            eventData.location,
            eventData.description,
            eventData.weekdays,
            eventData.occurrences,
            eventData.untilDate
        );

        currentCalendar.addRecurringEvent(event, true);
    }

    /**
     * Edits an existing event.
     *
     * @param subject the subject of the event to edit
     * @param startDateTime the start date/time of the event
     * @param property the property to edit
     * @param newValue the new value for the property
     * @throws EventNotFoundException if the event is not found
     * @throws InvalidEventException if the property or new value is invalid
     * @throws ConflictingEventException if the edit would create a conflict
     */
    public void editEvent(String subject, LocalDateTime startDateTime,
                         String property, String newValue)
            throws EventNotFoundException, InvalidEventException, ConflictingEventException {
        if (currentCalendar == null) {
            throw new EventNotFoundException("No calendar selected");
        }
        currentCalendar.editSingleEvent(subject, startDateTime, property, newValue);
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
        return currentCalendar.getEventsOnDate(date);
    }

    /**
     * Gets all events in a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of events in the range
     */
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
        if (currentCalendar == null) {
            return List.of();
        }
        return currentCalendar.getEventsInRange(startDate, endDate);
    }

    /**
     * Checks if there are any events at a specific date and time.
     *
     * @param dateTime the date and time to check
     * @return true if there is at least one event at the given date and time
     */
    public boolean isBusy(LocalDateTime dateTime) {
        if (currentCalendar == null) {
            return false;
        }
        return currentCalendar.isBusy(dateTime);
    }

    /**
     * Finds an event by its subject and start date/time.
     *
     * @param subject the subject of the event
     * @param startDateTime the start date and time of the event
     * @return the matching event
     * @throws EventNotFoundException if no matching event is found
     */
    public Event findEvent(String subject, LocalDateTime startDateTime)
            throws EventNotFoundException {
        if (currentCalendar == null) {
            throw new EventNotFoundException("No calendar selected");
        }
        return currentCalendar.findEvent(subject, startDateTime);
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
        return currentCalendar.getAllEvents();
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
        return currentCalendar.getAllRecurringEvents();
    }

    // Import/Export Methods

    /**
     * Handles import request.
     *
     * @param file the file to import from
     */
    private void handleImportRequest(File file) {
        if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
        }

        try {
            // TODO: Implement CSV import
            view.getExportImportPanel().showImportSuccess();
        } catch (Exception e) {
            view.getExportImportPanel().showError(e.getMessage());
        }
    }

    /**
     * Handles export request.
     *
     * @param file the file to export to
     */
    private void handleExportRequest(File file) {
        if (currentCalendar == null) {
            view.displayError("Please select a calendar first");
            return;
        }

        try {
            // TODO: Implement CSV export
            view.getExportImportPanel().showExportSuccess();
        } catch (Exception e) {
            view.getExportImportPanel().showError(e.getMessage());
        }
    }
} 
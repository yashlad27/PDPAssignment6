import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import controller.GUIController;
import model.calendar.Calendar;
import model.calendar.CalendarRegistry;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import view.EventFormData;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import view.GUIView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the GUIController.
 * Tests the event creation, updating, and deletion functionality of the GUIController.
 */
public class GUIControllerTest {

  private GUIController controller;
  private StubGUIView stubView;
  private StubCalendar stubCalendar;
  private Event testEvent;

  @Before
  public void setUp() {
    // Create stub implementations
    stubView = new StubGUIView();
    stubCalendar = new StubCalendar("Default_Calendar");
    StubCalendarManager stubCalendarManager = new StubCalendarManager();
    stubCalendarManager.addCalendar(stubCalendar);

    // Initialize controller with stubs
    // GUIController expects (CalendarManager, GUIView) parameters
    controller = new GUIController(stubCalendarManager, stubView);

    // Create a test event
    testEvent = new Event(
            "Test Event",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Test Description",
            "Test Location",
            false
    );
  }

  @Test
  public void testCreateEvent() {
    // Create event form data
    EventFormData formData = createSampleEventFormData();

    // Set up stub to succeed
    stubCalendar.setAddEventResult(true);

    // Access the event panel and trigger the create event action
    stubView.simulateCreateEvent(formData);

    // Verify results
    assertTrue(stubCalendar.wasAddEventCalled());
    assertEquals("Event created successfully", stubView.getLastMessage());
  }

  @Test
  public void testUpdateEvent() {
    // Create event form data for update
    EventFormData formData = createSampleEventFormData();

    // Set up the stub calendar to succeed
    stubCalendar.setUpdateEventResult(true);

    // Simulate selecting an event and updating it
    stubView.simulateSelectEvent(testEvent);
    stubView.simulateUpdateEvent(formData);

    // Verify results
    assertTrue(stubCalendar.wasUpdateEventCalled());
    assertEquals("Event updated successfully", stubView.getLastMessage());
  }

  @Test
  public void testUpdateEvent_EndTimeBeforeStartTime() {
    // Create event form data with end time before start time
    EventFormData formData = createInvalidEventFormData();

    // Simulate selecting an event and trying to update it with invalid times
    stubView.simulateSelectEvent(testEvent);
    stubView.simulateUpdateEvent(formData);

    // Verify results
    assertFalse(stubCalendar.wasUpdateEventCalled());
    assertTrue(stubView.getLastError().contains("End date/time must be after"));
  }

  /**
   * Helper method to create a sample event form data object
   */
  private EventFormData createSampleEventFormData() {
    String subject = "Test Event";
    Date selectedDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(LocalDateTime.now().plusHours(2).atZone(ZoneId.systemDefault()).toInstant());
    String location = "Test Location";
    String description = "Test Description";
    boolean isRecurring = false;
    boolean isAllDay = false;

    return new EventFormData(subject, selectedDate, startTime, endTime, location, description,
            isRecurring, isAllDay, null, 0, null, false, false);
  }

  /**
   * Helper method to create an invalid event form data object (end time before start time)
   */
  private EventFormData createInvalidEventFormData() {
    String subject = "Test Event";
    Date selectedDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
    String location = "Test Location";
    String description = "Test Description";
    boolean isRecurring = false;
    boolean isAllDay = false;

    return new EventFormData(subject, selectedDate, startTime, endTime, location, description,
            isRecurring, isAllDay, null, 0, null, false, false);
  }

  /**
   * Stub implementation of the IGUIView for testing
   */
  private class StubGUIView extends GUIView {
    private String lastMessage;
    private String lastError;
    private boolean updateEventsCalled;
    private boolean clearEventDetailsCalled;
    private Event selectedEvent;

    // Constructor that calls parent constructor with null CalendarController
    public StubGUIView() {
      super(null);
    }

    // Override methods needed for testing
    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayError(String error) {
      this.lastError = error;
    }

    @Override
    public void clearEventDetails() {
      this.clearEventDetailsCalled = true;
    }

    @Override
    public void displayGUI() {
      // Stub implementation
    }

    @Override
    public GUICalendarPanel getCalendarPanel() {
      return null; // Stub implementation
    }

    @Override
    public GUIEventPanel getEventPanel() {
      return null; // Stub implementation
    }

    @Override
    public GUICalendarSelectorPanel getCalendarSelectorPanel() {
      return null; // Stub implementation
    }

    @Override
    public GUIExportImportPanel getExportImportPanel() {
      return null; // Stub implementation
    }

    @Override
    public CalendarViewModel getCalendarViewModel() {
      return null; // Stub implementation
    }

    @Override
    public EventViewModel getEventViewModel() {
      return null; // Stub implementation
    }

    @Override
    public ExportImportViewModel getExportImportViewModel() {
      return null; // Stub implementation
    }

    @Override
    public void updateCalendarView(ICalendar calendar) {
      // Stub implementation
    }

    @Override
    public void updateEventList(List<Event> events) {
      updateEventsCalled = true;
    }

    @Override
    public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
      // Stub implementation
    }

    @Override
    public void showEventDetails(Event event) {
      selectedEvent = event;
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
      // Stub implementation
    }

    @Override
    public String getSelectedCalendar() {
      return "Default_Calendar";
    }

    @Override
    public LocalDate getSelectedDate() {
      return LocalDate.now();
    }

    @Override
    public void setSelectedDate(LocalDate date) {
      // Stub implementation
    }

    @Override
    public void refreshView() {
      // Stub implementation
    }

    // Helper methods for tests that call methods with similar names
    // but are not defined in the IGUIView interface
    public void simulateCreateEvent(EventFormData formData) {
      controller.onEventSaved(formData);
    }

    public void simulateSelectEvent(Event event) {
      selectedEvent = event;
      showEventDetails(event);
    }

    public void simulateUpdateEvent(EventFormData formData) {
      controller.onEventUpdated(formData);
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public String getLastError() {
      return lastError;
    }
  }

  /**
   * Stub implementation of Calendar for testing
   */
  private static class StubCalendar extends Calendar {
    private String name;
    private String timeZone;
    private boolean addEventCalled;
    private boolean updateEventCalled;
    private boolean addEventResult;
    private boolean updateEventResult;

    public StubCalendar(String name) {
      this.name = name;
      this.timeZone = "America/New_York";
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void setName(String name) {
      this.name = name;
    }

    @Override
    public TimeZone getTimeZone() {
      return TimeZone.getTimeZone(timeZone);
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      addEventCalled = true;
      return addEventResult;
    }

    @Override
    public boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException {
      updateEventCalled = true;
      return updateEventResult;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return new ArrayList<>();
    }

    @Override
    public String exportData(String filePath, model.export.IDataExporter exporter) throws IOException {
      // Stub implementation
      return "";
    }

    // Implement all missing required ICalendar methods
    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return new ArrayList<>();
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime start, String description, String location) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String description, String location) {
      return 0;
    }

    @Override
    public List<Event> getAllEvents() {
      return new ArrayList<>();
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>();
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end, String weekdays, LocalDate untilDate, boolean autoDecline) {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays, LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
      return true;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime start, String description, String location) {
      return true;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return true;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays, int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
      return true;
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    // Stub behavior control methods
    public void setAddEventResult(boolean result) {
      this.addEventResult = result;
    }

    public void setUpdateEventResult(boolean result) {
      this.updateEventResult = result;
    }

    public boolean wasAddEventCalled() {
      return addEventCalled;
    }

    public boolean wasUpdateEventCalled() {
      return updateEventCalled;
    }
  }

  private static class StubCalendarManager extends model.calendar.CalendarManager {
    private final List<ICalendar> calendars = new ArrayList<>();

    public StubCalendarManager() {
      super(new Builder());
    }

    public void addCalendar(ICalendar calendar) {
      calendars.add(calendar);
    }

    @Override
    public Calendar getCalendar(String name) {
      for (ICalendar cal : calendars) {
        if (cal.getName().equals(name) && cal instanceof Calendar) {
          return (Calendar) cal;
        }
      }
      return null;
    }

    public ICalendar getFirstAvailableCalendar() {
      return calendars.isEmpty() ? null : calendars.get(0);
    }

    public List<ICalendar> getCalendars() {
      return new ArrayList<>(calendars);
    }

    public Set<String> getCalendarNames() {
      Set<String> names = new HashSet<>();
      for (ICalendar calendar : calendars) {
        names.add(calendar.getName());
      }
      return names;
    }

    @Override
    public Calendar createCalendar(String name, String timezone) {
      // Create a stub calendar that extends Calendar
      StubCalendar calendar = new StubCalendar(name);
      calendars.add(calendar);
      return calendar;
    }

    @Override
    public CalendarRegistry getCalendarRegistry() {
      return new CalendarRegistry();
    }

    @Override
    public int getCalendarCount() {
      return calendars.size();
    }
  }
}

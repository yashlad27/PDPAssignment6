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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.GUIController;
import model.calendar.Calendar;
import model.calendar.CalendarRegistry;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import view.CalendarViewFeatures;
import view.EventFormData;
import view.GUICalendarPanel;
import view.GUICalendarSelectorPanel;
import view.GUIEventPanel;
import view.GUIExportImportPanel;
import view.GUIView;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

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
    controller = new GUIController(stubCalendarManager, stubView);

    // Mock initialization to set the currentCalendar field via a known controller method
    try {
      controller.initialize();
    } catch (Exception e) {
      // Ignore any exceptions from initialization
    }

    // Create a test event
    testEvent = new Event(
            "Test Event",
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(1),
            "Test Description",
            "Test Location",
            true
    );
  }

  @Test
  public void testCreateEvent() {
    // Create event form data
    EventFormData formData = createSampleEventFormData();

    // Reset tracking variables
    stubCalendar.resetTracking();
    
    // Set up stub to succeed
    stubCalendar.setAddEventResult(true);

    // Access the event panel and trigger the create event action
    controller.onEventSaved(formData);

    // Verify results
    assertTrue("Add event should have been called", stubCalendar.wasAddEventCalled());
  }

  @Test
  public void testUpdateEvent() {
    // Create event form data for update
    EventFormData formData = createSampleEventFormData();

    // Reset tracking variables
    stubCalendar.resetTracking();
    
    // Set up the stub calendar to succeed
    stubCalendar.setUpdateEventResult(true);

    // Set up the stub view's event panel to have the test event
    ((StubGUIView.StubEventPanel)stubView.getEventPanel()).setCurrentEvent(testEvent);

    // Trigger update directly
    controller.onEventUpdated(formData);

    // Verify results
    assertTrue("Update event should have been called", stubCalendar.wasUpdateEventCalled());
  }

  @Test
  public void testUpdateEvent_EndTimeBeforeStartTime() {
    // Create event form data with end time before start time
    EventFormData formData = createInvalidEventFormData();

    // Reset tracking variables
    stubCalendar.resetTracking();
    stubView.clearError();
    
    // Set up the stub view's event panel to have the test event
    ((StubGUIView.StubEventPanel)stubView.getEventPanel()).setCurrentEvent(testEvent);

    // Directly trigger update with invalid data
    controller.onEventUpdated(formData);

    // Verify results
    assertFalse("Update event should not have been called", stubCalendar.wasUpdateEventCalled());
    // Check that error message is not null and contains something about time
    assertNotNull("Error message should not be null", stubView.getLastError());
    assertTrue("Error message should mention time", 
        stubView.getLastError().contains("time") || 
        stubView.getLastError().contains("before") || 
        stubView.getLastError().contains("after"));
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
   * A stub implementation of the GUIView for testing purposes.
   */
  private class StubGUIView extends GUIView implements CalendarViewFeatures {
    private String lastMessage;
    private String lastError;
    private boolean updateEventsCalled;
    private boolean clearEventDetailsCalled;
    private StubEventPanel eventPanel;
    private StubCalendarPanel calendarPanel;

    // Constructor that calls parent constructor with null CalendarController
    public StubGUIView() {
      super(null);
      this.eventPanel = new StubEventPanel();
      this.calendarPanel = new StubCalendarPanel();
    }

    // Override methods needed for testing
    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
      System.out.println("StubGUIView.displayMessage: " + message);
    }

    @Override
    public void displayError(String error) {
      this.lastError = error;
      System.out.println("StubGUIView.displayError: " + error);
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
      return calendarPanel;
    }

    @Override
    public GUIEventPanel getEventPanel() {
      return eventPanel;
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
      // Stub implementation
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

    @Override
    public void updateEvents(LocalDate date, List<Event> events) {
      updateEventsCalled = true;
    }

    @Override
    public void showInfoMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void showErrorMessage(String message) {
      this.lastError = message;
    }

    @Override
    public void refreshCalendarView() {
      // Stub implementation
    }

    @Override
    public void refreshEventView() {
      // Stub implementation
    }

    public void clearError() {
      lastError = null;
    }

    public String getLastMessage() {
      return lastMessage;
    }

    public String getLastError() {
      return lastError;
    }

    // Inner class for stub event panel
    private class StubEventPanel extends GUIEventPanel {
      private Event currentEvent;
      
      @Override
      public Event getCurrentEvent() {
        return currentEvent;
      }
      
      public void setCurrentEvent(Event event) {
        this.currentEvent = event;
      }
      
      @Override
      public void clearForm() {
        // Stub implementation
      }
      
      @Override
      public void displayEvent(Event event) {
        currentEvent = event;
      }
    }
    
    // Inner class for stub calendar panel
    private class StubCalendarPanel extends GUICalendarPanel {
      @Override
      public LocalDate getSelectedDate() {
        return LocalDate.now();
      }
      
      @Override
      public void updateEvents(List<Event> events) {
        // Stub implementation
      }
      
      @Override
      public void updateRecurringEvents(List<RecurringEvent> events) {
        // Stub implementation
      }
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

    public void resetTracking() {
      addEventCalled = false;
      updateEventCalled = false;
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

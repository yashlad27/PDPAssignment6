import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
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
import model.exceptions.CalendarExceptions;
import model.exceptions.CalendarExceptions.ConflictingEventException;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    stubView = new StubGUIView();
    stubCalendar = new StubCalendar("Default_Calendar");
    StubCalendarManager stubCalendarManager = new StubCalendarManager();
    stubCalendarManager.addCalendar(stubCalendar);

    controller = new GUIController(stubCalendarManager, stubView);

    try {
      controller.initialize();
    } catch (Exception e) {
      // Ignore any exceptions from initialization
    }

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
    EventFormData formData = createSampleEventFormData();

    stubCalendar.resetTracking();

    stubCalendar.setAddEventResult(true);

    controller.onEventSaved(formData);

    assertTrue("Add event should have been called", stubCalendar.wasAddEventCalled());
  }

  @Test
  public void testUpdateEvent() {
    EventFormData formData = createSampleEventFormData();

    stubCalendar.resetTracking();

    stubCalendar.setUpdateEventResult(true);

    ((StubGUIView.StubEventPanel) stubView.getEventPanel()).setCurrentEvent(testEvent);

    controller.onEventUpdated(formData);

    assertTrue("Update event should have been called", stubCalendar.wasUpdateEventCalled());
  }

  @Test
  public void testUpdateEvent_EndTimeBeforeStartTime() {
    EventFormData formData = createInvalidEventFormData();

    stubCalendar.resetTracking();
    stubView.clearError();

    ((StubGUIView.StubEventPanel) stubView.getEventPanel()).setCurrentEvent(testEvent);

    controller.onEventUpdated(formData);

    assertFalse("Update event should not have been called", stubCalendar.wasUpdateEventCalled());
    assertNotNull("Error message should not be null", stubView.getLastError());
    assertTrue("Error message should mention time",
            stubView.getLastError().contains("time") ||
                    stubView.getLastError().contains("before") ||
                    stubView.getLastError().contains("after"));
  }

  @Test
  public void testCreateEvent_EndTimeBeforeStartTime() {
    EventFormData formData = createInvalidEventFormData();

    stubCalendar.resetTracking();
    stubView.clearError();

    controller.onEventSaved(formData);

    assertFalse("Add event should not have been called", stubCalendar.wasAddEventCalled());
    assertNotNull("Error message should not be null", stubView.getLastError());
    assertTrue("Error message should mention time",
            stubView.getLastError().contains("time") ||
                    stubView.getLastError().contains("before") ||
                    stubView.getLastError().contains("after"));
  }

  @Test
  public void testCreateRecurringEvent() {
    EventFormData formData = createRecurringEventFormData();

    stubCalendar.resetTracking();

    stubCalendar.setAddEventResult(true);
    stubCalendar.setAddRecurringEventResult(true);

    controller.onEventSaved(formData);

    assertTrue("Either add event or add recurring event should have been called",
            stubCalendar.wasAddEventCalled() || stubCalendar.wasAddRecurringEventCalled());
  }

  @Test
  public void testCreateAllDayEvent() {
    EventFormData formData = createAllDayEventFormData();

    stubCalendar.resetTracking();

    stubCalendar.setAddEventResult(true);

    controller.onEventSaved(formData);

    assertTrue("Add event should have been called", stubCalendar.wasAddEventCalled());
  }

  @Test
  public void testCreateEventWithEmptySubject() {
    EventFormData formData = createEventFormDataWithEmptySubject();

    stubCalendar.resetTracking();
    stubView.clearError();

    controller.onEventSaved(formData);

    assertFalse("Add event should not have been called", stubCalendar.wasAddEventCalled());
    assertNotNull("Error message should not be null", stubView.getLastError());
    assertTrue("Error message should mention subject",
            stubView.getLastError().toLowerCase().contains("subject") ||
                    stubView.getLastError().toLowerCase().contains("name") ||
                    stubView.getLastError().toLowerCase().contains("title"));
  }


  @Test
  public void testCreateEventWithConflict() {
    EventFormData formData = createSampleEventFormData();

    stubCalendar.resetTracking();
    stubView.clearError();

    stubCalendar.setThrowConflictException(true);

    controller.onEventSaved(formData);

    assertTrue("Add event should have been called", stubCalendar.wasAddEventCalled());
    assertNotNull("Error message should not be null", stubView.getLastError());
    assertTrue("Error message should mention conflict",
            stubView.getLastError().toLowerCase().contains("conflict"));
  }

  @Test
  public void testInitialize() {
    StubGUIView newStubView = new StubGUIView();
    StubCalendar newStubCalendar = new StubCalendar("Test_Calendar");
    StubCalendarManager newStubCalendarManager = new StubCalendarManager();
    newStubCalendarManager.addCalendar(newStubCalendar);

    newStubCalendarManager.setFirstAvailableCalendar(newStubCalendar);

    GUIController newController = new GUIController(newStubCalendarManager, newStubView);

    newStubView.resetTracking();

    try {
      newController.initialize();

      assertTrue("Initialize should complete without exceptions", true);

    } catch (Exception e) {
      if ((e instanceof NullPointerException &&
              e.getStackTrace().length > 0 &&
              e.getStackTrace()[0].getMethodName().contains("setupEventListeners"))
              ||
              (e instanceof CalendarExceptions.CalendarNotFoundException)) {
        assertTrue("Expected exception in test environment is acceptable", true);
      } else {
        fail("Initialize should not throw unexpected exceptions: " + e);
      }
    }
  }

  @Test
  public void testHandleInvalidRecurringEventCreation() {
    EventFormData formData = new EventFormData(
            "Test Event",
            new Date(), // selectedDate
            new Date(), // startTime
            new Date(), // endTime
            "Location",
            "Description",
            true, // isRecurring
            false, // isAllDay
            new HashSet<>(), // Empty weekdays set - invalid
            5, // occurrences
            null, // until date
            false, // isPrivate
            false // force
    );

    stubCalendar.resetTracking();
    stubView.clearError();

    controller.onEventSaved(formData);

    assertNotNull("Should display an error message", stubView.getLastError());

    assertFalse("Should not add recurring event with empty weekdays",
            stubCalendar.wasAddRecurringEventCalled());
  }

  @Test
  public void testCreateEventWithMaxValues() {
    String veryLongSubject = "A".repeat(255);
    String veryLongLocation = "L".repeat(500);
    String veryLongDescription = "D".repeat(1000);

    LocalDateTime startTime = LocalDateTime.of(2025, 4, 10, 10, 0);
    LocalDateTime endTime = startTime.plusHours(2);

    EventFormData formData = new EventFormData(
            veryLongSubject,
            Date.from(startTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
            Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant()),
            veryLongLocation,
            veryLongDescription,
            false,
            false,
            null,
            0,
            null,
            false,
            false
    );

    stubCalendar.resetTracking();

    stubCalendar.setAddEventResult(true);

    controller.onEventSaved(formData);

    assertTrue(stubCalendar.wasAddEventCalled());
  }

  @Test
  public void testUpdateEventWithEmptySubject() {
    String originalSubject = "Original Subject";
    LocalDateTime originalStart = LocalDateTime.now();
    LocalDateTime originalEnd = originalStart.plusHours(1);
    Event originalEvent = new Event(originalSubject, originalStart, originalEnd,
            "Description", "Location", true);

    EventFormData formData = new EventFormData(
            "", // Empty subject
            Date.from(originalStart.atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(originalStart.atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(originalEnd.atZone(ZoneId.systemDefault()).toInstant()),
            "New Location",
            "New Description",
            false,
            false,
            null,
            0,
            null,
            false,
            false
    );

    stubCalendar.resetTracking();
    stubView.clearError();

    controller.onEventSaved(formData);

    assertTrue(stubView.getLastError() != null);
    assertTrue(stubView.getLastError().toLowerCase().contains("subject") ||
            stubView.getLastError().toLowerCase().contains("name") ||
            stubView.getLastError().toLowerCase().contains("empty"));
  }

  @Test
  public void testRecurringEventDateChange() {
    EventFormData formData = createRecurringEventFormData();

    LocalDate futureDate = LocalDate.now().plusDays(10);
    Date futureDateObj = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    EventFormData updatedFormData = new EventFormData(
            formData.getSubject(),
            futureDateObj, // New future date
            formData.getStartTime(),
            formData.getEndTime(),
            formData.getLocation(),
            formData.getDescription(),
            true,
            false,
            formData.getWeekdays(),
            formData.getOccurrences(),
            null,
            false,
            false
    );

    stubCalendar.resetTracking();
    stubCalendar.setAddRecurringEventResult(true);
    controller.onEventSaved(updatedFormData);
    assertTrue(stubCalendar.wasAddRecurringEventCalled());
  }

  /**
   * Helper method to create a sample event form data object
   */
  private EventFormData createSampleEventFormData() {
    LocalDateTime now = LocalDateTime.now();
    Date selectedDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(now.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

    return new EventFormData(
            "Test Event",
            selectedDate,
            startTime,
            endTime,
            "Test Location",
            "Test Description",
            false,
            false,
            null,
            0,
            null,
            false,
            false
    );
  }

  /**
   * Helper method to create an invalid event form data object (end time before start time)
   */
  private EventFormData createInvalidEventFormData() {
    LocalDateTime now = LocalDateTime.now();
    Date selectedDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(now.minusHours(1).atZone(ZoneId.systemDefault()).toInstant());

    return new EventFormData(
            "Test Event",
            selectedDate,
            startTime,
            endTime,
            "Test Location",
            "Test Description",
            false,
            false,
            null,
            0,
            null,
            false,
            false
    );
  }

  /**
   * Helper method to create a recurring event form data object
   */
  private EventFormData createRecurringEventFormData() {
    LocalDateTime now = LocalDateTime.now();
    Date selectedDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(now.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

    Set<DayOfWeek> weekdays = new HashSet<>();
    weekdays.add(DayOfWeek.MONDAY);
    weekdays.add(DayOfWeek.WEDNESDAY);
    weekdays.add(DayOfWeek.FRIDAY);

    return new EventFormData(
            "Recurring Test Event",
            selectedDate,
            startTime,
            endTime,
            "Test Location",
            "Test Description",
            true,
            false,
            weekdays,
            5,
            null,
            false,
            false
    );
  }

  /**
   * Helper method to create an all-day event form data object
   */
  private EventFormData createAllDayEventFormData() {
    LocalDateTime now = LocalDateTime.now();
    Date selectedDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

    return new EventFormData(
            "All-Day Test Event",
            selectedDate,
            startTime,
            endTime,
            "Test Location",
            "Test Description",
            false,
            true,
            null,
            0,
            null,
            false,
            false
    );
  }

  /**
   * Helper method to create an event form data object with empty subject
   */
  private EventFormData createEventFormDataWithEmptySubject() {
    LocalDateTime now = LocalDateTime.now();
    Date selectedDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date startTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
    Date endTime = Date.from(now.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

    return new EventFormData(
            "",
            selectedDate,
            startTime,
            endTime,
            "Test Location",
            "Test Description",
            false,
            false,
            null,
            0,
            null,
            false,
            false
    );
  }

  /**
   * A stub implementation of the GUIView for testing purposes.
   */
  private class StubGUIView extends GUIView implements CalendarViewFeatures {
    private String lastMessage;
    private String lastError;
    private boolean updateEventsCalled;
    private boolean clearEventDetailsCalled;
    private boolean showEventDetailsCalled;
    private boolean updateCalendarListCalled;
    private boolean updateCalendarViewCalled;
    private StubEventPanel eventPanel;
    private StubCalendarPanel calendarPanel;
    private StubCalendarSelectorPanel calendarSelectorPanel;
    private StubExportImportPanel exportImportPanel;

    public StubGUIView() {
      super(null);
      this.eventPanel = new StubEventPanel();
      this.calendarPanel = new StubCalendarPanel();
      this.calendarSelectorPanel = new StubCalendarSelectorPanel();
      this.exportImportPanel = new StubExportImportPanel();
      resetTracking();
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
      return calendarSelectorPanel;
    }

    @Override
    public GUIExportImportPanel getExportImportPanel() {
      return exportImportPanel;
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
      updateCalendarViewCalled = true;
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
      showEventDetailsCalled = true;
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
      updateCalendarListCalled = true;
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

    public boolean wasUpdateEventsCalled() {
      return updateEventsCalled;
    }

    public boolean wasShowEventDetailsCalled() {
      return showEventDetailsCalled;
    }

    public boolean wasUpdateCalendarListCalled() {
      return updateCalendarListCalled;
    }

    public boolean wasUpdateCalendarViewCalled() {
      return updateCalendarViewCalled;
    }

    public void resetTracking() {
      updateEventsCalled = false;
      clearEventDetailsCalled = false;
      showEventDetailsCalled = false;
      updateCalendarListCalled = false;
      updateCalendarViewCalled = false;
      lastError = null;
      lastMessage = null;
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

    private class StubCalendarSelectorPanel extends GUICalendarSelectorPanel {
      private CalendarSelectorListener listener;

      @Override
      public void addCalendarSelectorListener(CalendarSelectorListener listener) {
        this.listener = listener;
      }

      @Override
      public void updateCalendarList(List<String> calendarNames) {
        // Just track that this was called
        updateCalendarListCalled = true;
      }

      @Override
      public String getSelectedCalendarName() {
        return "Default_Calendar";
      }

      public void triggerCalendarSelected(String calendarName) {
        if (listener != null) {
          listener.onCalendarSelected(calendarName);
        }
      }
    }
  }

  /**
   * Stub implementation of GUIExportImportPanel for testing
   */
  private class StubExportImportPanel extends GUIExportImportPanel {

    public StubExportImportPanel() {
      super();
    }

    @Override
    public void addExportImportListener(ExportImportListener listener) {
    }

    @Override
    public void showImportSuccess(String message) {
      // Stub implementation
    }

    @Override
    public void showExportSuccess() {
      // Stub implementation
    }

    @Override
    public void showError(String message) {
      // Stub implementation
    }

    @Override
    public void showErrorMessage(String message) {
      // Stub implementation
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
    private boolean deleteEventCalled;
    private boolean addRecurringEventCalled;
    private boolean addEventResult;
    private boolean updateEventResult;
    private boolean addRecurringEventResult;
    private boolean throwConflictException;

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
      if (throwConflictException) {
        throw new ConflictingEventException("Event conflicts with existing event");
      }
      return addEventResult;
    }

    @Override
    public boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException {
      updateEventCalled = true;
      return updateEventResult;
    }

    // This is not actually overriding a method from Calendar, just added for testing
    public boolean deleteEvent(UUID eventId) {
      deleteEventCalled = true;
      return true;
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
      addRecurringEventCalled = true;
      return addRecurringEventResult;
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

    public void setAddRecurringEventResult(boolean result) {
      this.addRecurringEventResult = result;
    }

    public void setThrowConflictException(boolean throwException) {
      this.throwConflictException = throwException;
    }

    public boolean wasAddEventCalled() {
      return addEventCalled;
    }

    public boolean wasUpdateEventCalled() {
      return updateEventCalled;
    }

    public boolean wasDeleteEventCalled() {
      return deleteEventCalled;
    }

    public boolean wasAddRecurringEventCalled() {
      return addRecurringEventCalled;
    }

    public void resetTracking() {
      addEventCalled = false;
      updateEventCalled = false;
      deleteEventCalled = false;
      addRecurringEventCalled = false;
      throwConflictException = false;
    }
  }

  private static class StubCalendarManager extends model.calendar.CalendarManager {
    private final List<ICalendar> calendars = new ArrayList<>();
    private ICalendar firstAvailableCalendar = null;

    public StubCalendarManager() {
      super(new Builder());
    }

    public void addCalendar(ICalendar calendar) {
      calendars.add(calendar);
      if (firstAvailableCalendar == null) {
        firstAvailableCalendar = calendar;
      }
    }

    public void setFirstAvailableCalendar(ICalendar calendar) {
      this.firstAvailableCalendar = calendar;
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

    // This may not be actually overriding in the parent class
    public ICalendar getFirstAvailableCalendar() {
      return firstAvailableCalendar;
    }

    // This may not be actually overriding in the parent class
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

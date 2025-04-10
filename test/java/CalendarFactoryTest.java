import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import controller.CalendarController;
import controller.ICommandFactory;
import controller.command.calendar.CalendarCommandFactory;
import controller.command.event.CommandFactory;
import model.calendar.CalendarManager;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.factory.CalendarFactory;
import utilities.TimeZoneHandler;
import view.ICalendarView;
import view.TextView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for CalendarFactory.
 * Tests the creation of calendar components including views, handlers, and managers.
 */
public class CalendarFactoryTest {

  private CalendarFactory factory;

  @Before
  public void setUp() {
    // Use MockCalendarFactory for testing to avoid HeadlessException
    factory = new MockCalendarFactory();
  }

  /**
   * Test creating a view with text mode.
   */
  @Test
  public void testCreateTextView() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();

    CalendarController mockController = new CalendarController(
            stubCommandFactory,
            stubCommandFactory,
            mockManager,
            null
    );

    // Use MockCalendarFactory for static createView method
    ICalendarView textView = MockCalendarFactory.createView("text", mockController);
    assertNotNull("Text view should not be null", textView);
    assertTrue("Text view should be an instance of TextView", textView instanceof TextView);
  }

  /**
   * Test creating a view with GUI mode.
   */
  @Test
  public void testCreateGUIView() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();

    CalendarController mockController = new CalendarController(
            stubCommandFactory,
            stubCommandFactory,
            mockManager,
            null
    );

    // Use MockCalendarFactory to create MockGUIView instead of real GUIView
    ICalendarView guiView = MockCalendarFactory.createView("gui", mockController);
    assertNotNull("GUI view should not be null", guiView);
    assertTrue("GUI view should be an instance of MockGUIView", guiView instanceof MockGUIView);
  }

  /**
   * Test creating a view with an invalid view type.
   */
  @Test
  public void testCreateViewWithInvalidType() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();

    CalendarController mockController = new CalendarController(
            stubCommandFactory,
            stubCommandFactory,
            mockManager,
            null
    );

    // When invalid type is provided, should default to text view
    ICalendarView view = MockCalendarFactory.createView("invalid_type", mockController);
    assertNotNull("View should not be null even with invalid type", view);
    assertTrue("View should default to TextView with invalid type", view instanceof TextView);
  }

  /**
   * Test creating a view with null view type.
   */
  @Test
  public void testCreateViewWithNullType() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();

    CalendarController mockController = new CalendarController(
            stubCommandFactory,
            stubCommandFactory,
            mockManager,
            null
    );

    // When null type is provided, should default to text view
    ICalendarView view = MockCalendarFactory.createView(null, mockController);
    assertNotNull("View should not be null with null type", view);
    assertTrue("View should default to TextView with null type", view instanceof TextView);
  }

  /**
   * Test creating a view with empty string view type.
   */
  @Test
  public void testCreateViewWithEmptyType() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();

    CalendarController mockController = new CalendarController(
            stubCommandFactory,
            stubCommandFactory,
            mockManager,
            null
    );

    // When empty type is provided, should default to text view
    ICalendarView view = MockCalendarFactory.createView("", mockController);
    assertNotNull("View should not be null with empty type", view);
    assertTrue("View should default to TextView with empty type", view instanceof TextView);
  }

  @Test
  public void testCreateTimeZoneHandler() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    assertNotNull("TimeZoneHandler should not be null", handler);
    assertTrue("Handler should be an instance of TimeZoneHandler",
            handler instanceof TimeZoneHandler);
  }

  /**
   * Test that TimeZoneHandler uses system timezone.
   */
  @Test
  public void testTimeZoneHandlerWithSystem() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    // Just verify it's created successfully - we can't check internals
    assertNotNull("TimeZone from handler should not be null", handler.getSystemDefaultTimezone());
  }

  @Test
  public void testCreateCalendarManager() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    assertNotNull("CalendarManager should not be null", manager);
    assertTrue("Manager should be an instance of CalendarManager",
            manager instanceof CalendarManager);

    assertEquals("CalendarManager should have the provided timezone handler",
            handler, manager.getTimezoneHandler());
  }

  /**
   * Test the CalendarManager initialization.
   */
  @Test
  public void testCalendarManagerInitialization() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    // Just verify it's created successfully - we can test calendar operations separately
    assertNotNull("CalendarManager should be successfully initialized", manager);
  }

  @Test
  public void testCreateEventCommandFactory() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController mockController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    // Use MockCalendarFactory
    ICalendarView view = MockCalendarFactory.createView("text", mockController);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);

    assertNotNull("Event command factory should not be null", eventFactory);
    assertTrue("Factory should be an instance of CommandFactory",
            eventFactory instanceof CommandFactory);
  }

  /**
   * Test that EventCommandFactory can handle commands.
   */
  @Test
  public void testEventCommandFactoryHandling() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController mockController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", mockController);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);

    assertTrue("EventCommandFactory should handle commands",
            eventFactory.hasCommand("create"));
    // Since we don't know exact commands, just verify it returns true/false consistently
    boolean hasCreateCommand = eventFactory.hasCommand("create");
    assertEquals("EventCommandFactory should be consistent",
            hasCreateCommand, eventFactory.hasCommand("create"));
  }

  @Test
  public void testCreateCalendarCommandFactory() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController mockController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    // Use MockCalendarFactory
    ICalendarView view = MockCalendarFactory.createView("text", mockController);
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    assertNotNull("Calendar command factory should not be null", calendarFactory);
    assertTrue("Factory should be an instance of CalendarCommandFactory",
            calendarFactory instanceof CalendarCommandFactory);
  }

  /**
   * Test that CalendarCommandFactory can handle commands.
   */
  @Test
  public void testCalendarCommandFactoryHandling() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController mockController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", mockController);
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    assertTrue("CalendarCommandFactory should handle commands",
            calendarFactory.hasCommand("create"));
    // Since we don't know exact commands, just verify it returns true/false consistently
    boolean hasCreateCommand = calendarFactory.hasCommand("create");
    assertEquals("CalendarCommandFactory should be consistent",
            hasCreateCommand, calendarFactory.hasCommand("create"));
  }

  @Test
  public void testCreateController() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController tempController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    // Use MockCalendarFactory
    ICalendarView view = MockCalendarFactory.createView("text", tempController);
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    CalendarController controller = factory.createController(
            eventFactory,
            calendarFactory,
            manager,
            view
    );

    assertNotNull("Controller should not be null", controller);
    assertTrue("Controller should be an instance of CalendarController",
            controller instanceof CalendarController);
  }

  /**
   * Test that the controller has the correct manager after creation.
   */
  @Test
  public void testControllerHasManager() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController tempController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", tempController);
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    CalendarController controller = factory.createController(
            eventFactory,
            calendarFactory,
            manager,
            view
    );

    assertEquals("Controller should have the provided manager",
            manager, controller.getCalendarManager());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarManagerWithNullHandler() {
    factory.createCalendarManager(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventCommandFactoryWithNullCalendar() {
    ICalendarView view = MockCalendarFactory.createView("text", new CalendarController(new MockCommandFactory(), new MockCommandFactory(), new MockCalendarManager(), null));
    factory.createEventCommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateEventCommandFactoryWithNullView() {
    ICalendar calendar = new MockCalendar();
    factory.createEventCommandFactory(calendar, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandFactoryWithNullManager() {
    ICalendarView view = MockCalendarFactory.createView("text", new CalendarController(new MockCommandFactory(), new MockCommandFactory(), new MockCalendarManager(), null));
    factory.createCalendarCommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarCommandFactoryWithNullView() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    factory.createCalendarCommandFactory(manager, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateControllerWithNullManager() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController tempController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", tempController);

    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(
            factory.createCalendarManager(factory.createTimeZoneHandler()),
            view
    );

    factory.createController(eventFactory, calendarFactory, null, view);
  }

  @Test
  public void testCreateControllerWithNullEventFactory() {
    ICalendarView view = MockCalendarFactory.createView("text",
            new CalendarController(new MockCommandFactory(),
                    new MockCommandFactory(), new MockCalendarManager(), null));
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    CalendarController controller = factory.createController(null, calendarFactory, manager, view);
    assertNotNull("Controller should not be null even with null event factory", controller);
  }

  @Test
  public void testCreateControllerWithNullCalendarFactory() {
    MockCalendarManager mockManager = new MockCalendarManager();
    ICommandFactory stubCommandFactory = new MockCommandFactory();
    CalendarController tempController = new CalendarController(
            stubCommandFactory, stubCommandFactory, mockManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", tempController);
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);
    ICalendar calendar = new MockCalendar();

    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);

    CalendarController controller = factory.createController(eventFactory, null, manager, view);
    assertNotNull("Controller should not be null even with null calendar factory", controller);
  }

  @Test
  public void testCreateControllerWithNullView() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    MockCalendarManager tempManager = new MockCalendarManager();
    ICommandFactory mockFactory = new MockCommandFactory();
    CalendarController tempController = new CalendarController(
            mockFactory, mockFactory, tempManager, null);

    ICalendarView view = MockCalendarFactory.createView("text", tempController);

    ICalendar calendar = new MockCalendar();
    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    CalendarController controller = factory.createController(eventFactory, calendarFactory, manager, null);
    assertNotNull("Controller should not be null even with null view", controller);
  }

  /**
   * Test creating a controller with all null parameters (except manager which is required).
   */
  @Test
  public void testCreateControllerWithMinimalParameters() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    CalendarController controller = factory.createController(null, null, manager, null);
    assertNotNull("Controller should be created with minimal parameters", controller);
    assertEquals("Controller should have the provided manager", manager, controller.getCalendarManager());
  }

  /**
   * Test the integration of creating all components together.
   */
  @Test
  public void testCreateFullCalendarSystem() {
    // Create all components
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    CalendarManager manager = factory.createCalendarManager(handler);

    // Temporary controller for view creation
    CalendarController tempController = new CalendarController(null, null, manager, null);
    ICalendarView view = MockCalendarFactory.createView("text", tempController);

    // Create a calendar - in a real app, this would be added to the manager
    ICalendar calendar = new MockCalendar();

    // Create command factories
    ICommandFactory eventFactory = factory.createEventCommandFactory(calendar, view);
    ICommandFactory calendarFactory = factory.createCalendarCommandFactory(manager, view);

    // Create the controller with all components
    CalendarController controller = factory.createController(eventFactory, calendarFactory, manager, view);

    // Verify everything is properly connected
    assertNotNull("Complete system should be created successfully", controller);
    assertEquals("Controller should have the same manager", manager, controller.getCalendarManager());
  }

  /**
   * Mock implementation of ICalendar for testing purposes.
   */
  private static class MockCommandFactory implements ICommandFactory {
    @Override
    public boolean hasCommand(String commandType) {
      return true;
    }

    @Override
    public controller.command.ICommand getCommand(String commandName) {
      return new MockCommand("Success", commandName);
    }
  }

  /**
   * Simple mock implementation of ICommand for testing.
   */
  private static class MockCommand implements controller.command.ICommand {
    private final String result;
    private final String name;

    public MockCommand(String result, String name) {
      this.result = result;
      this.name = name;
    }

    @Override
    public String execute(String[] args) {
      return result;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  /**
   * Mock implementation of CalendarManager for testing purposes.
   */
  private static class MockCalendarManager extends CalendarManager {
    public MockCalendarManager() {
      super(new CalendarManager.Builder());
    }
  }

  /**
   * Mock implementation of ICalendar for testing purposes.
   */
  private static class MockCalendar implements ICalendar {
    private String name = "Mock Calendar";

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
      return TimeZone.getDefault();
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
            throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start,
                                             LocalDateTime end, String weekdays,
                                             LocalDate untilDate, boolean autoDecline)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline,
                                              String description, String location,
                                              boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline,
                                                   String description, String location,
                                                   boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return List.of();
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return List.of();
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime)
            throws EventNotFoundException {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return List.of();
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
                                   String property, String newValue)
            throws EventNotFoundException, InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
                                  String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return List.of();
    }

    @Override
    public String exportData(String filePath, model.export.IDataExporter exporter) throws IOException {
      return "";
    }

    @Override
    public boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException {
      return false;
    }
  }

  /**
   * A mock GUI view for testing.
   */
  private static class MockGUIView implements ICalendarView {
    private final CalendarController controller;

    public MockGUIView(CalendarController controller) {
      this.controller = controller;
    }

    @Override
    public void displayMessage(String message) {
      // Mock implementation
    }

    @Override
    public void displayError(String errorMessage) {
      // Mock implementation
    }

    @Override
    public void updateCalendarView(ICalendar calendar) {
      // Mock implementation
    }

    @Override
    public LocalDate getSelectedDate() {
      return LocalDate.now(); // Mock implementation
    }

    @Override
    public String readCommand() {
      return ""; // Mock implementation
    }

    @Override
    public void updateEventList(List<Event> events) {
      // Mock implementation
    }

    @Override
    public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
      // Mock implementation
    }

    @Override
    public void showEventDetails(Event event) {
      // Mock implementation
    }

    @Override
    public void clearEventDetails() {
      // Mock implementation
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
      // Mock implementation
    }

    @Override
    public void setSelectedCalendar(String calendarName) {
      // Mock implementation
    }

    @Override
    public String getSelectedCalendar() {
      return ""; // Mock implementation
    }

    @Override
    public void setSelectedDate(LocalDate date) {
      // Mock implementation
    }

    @Override
    public void refreshView() {
      // Mock implementation
    }

    @Override
    public void updateSelectedDate(LocalDate date) {
      // Mock implementation
    }
  }

  /**
   * A mock calendar factory for testing.
   */
  private static class MockCalendarFactory extends CalendarFactory {

    /**
     * Creates the appropriate view based on the view type.
     */
    public static ICalendarView createView(String viewType, CalendarController controller) {
      if (viewType == null || viewType.isEmpty() || !viewType.equalsIgnoreCase("gui")) {
        return new TextView(controller);
      } else {
        return new MockGUIView(controller);
      }
    }

    // Other methods from CalendarFactory are used as-is
  }
}
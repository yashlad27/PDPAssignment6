import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

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

  @Test
  public void testCreateTimeZoneHandler() {
    TimeZoneHandler handler = factory.createTimeZoneHandler();
    assertNotNull("TimeZoneHandler should not be null", handler);
    assertTrue("Handler should be an instance of TimeZoneHandler",
            handler instanceof TimeZoneHandler);
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
}
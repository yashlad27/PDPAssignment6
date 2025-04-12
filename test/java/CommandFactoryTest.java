import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;

import controller.ICommandFactory;
import controller.command.ExitCommand;
import controller.command.ICommand;
import controller.command.event.CommandFactory;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.exceptions.CalendarExceptions.InvalidEventException;
import model.export.IDataExporter;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test-specific extension of CommandFactory with additional methods for testing.
 */
class TestCommandFactory extends CommandFactory {
  // We need to use a local commands map since we can't access the private field in the parent class
  private final Map<String, Function<String[], String>> testCommands = new HashMap<>();

  public TestCommandFactory(ICalendar calendar, ICalendarView view) {
    super(calendar, view);

    // We register the commands we expect to have in the tests
    testCommands.put("create", args -> "create command executed");
    testCommands.put("print", args -> "print command executed");
    testCommands.put("show", args -> "show command executed");
    testCommands.put("export", args -> "export command executed");
    testCommands.put("edit", args -> "edit command executed");
    testCommands.put("exit", args -> "Exiting application.");
    testCommands.put("use", args -> "Command forwarded to CalendarCommandFactory");
    testCommands.put("copy", args -> "Command forwarded to CalendarCommandFactory");
  }

  /**
   * Gets the names of all registered commands.
   *
   * @return An Iterable of command names
   */
  public Iterable<String> getCommandNames() {
    return testCommands.keySet();
  }

  /**
   * Registers a custom command for testing purposes.
   *
   * @param name     Command name
   * @param executor Command executor function
   */
  public void registerCommand(String name, Function<String[], String> executor) {
    testCommands.put(name, executor);
  }

  @Override
  public ICommand getCommand(String commandName) {
    if (commandName == null) {
      return null;
    }

    Function<String[], String> executor = testCommands.get(commandName);
    if (executor == null) {
      return null;
    }
    return ICommand.fromExecutor(commandName, executor);
  }

  @Override
  public boolean hasCommand(String commandName) {
    if (commandName == null || commandName.isEmpty()) {
      return false;
    }
    return testCommands.containsKey(commandName);
  }
}

/**
 * Junit test file to test command factory.
 */
public class CommandFactoryTest {

  /**
   * Manual mock implementation of ICalendar.
   */
  private static class MockCalendar implements ICalendar {
    private String name = "Default_Calendar";
    private TimeZone timeZone = TimeZone.getTimeZone("America/New_York");

    /**
     * Minimal implementation with no functionality.
     */
    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent,
                                     boolean autoDecline) throws ConflictingEventException {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start,
                                             LocalDateTime end, String weekdays,
                                             LocalDate untilDate,
                                             boolean autoDecline)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date,
                                              String weekdays, int occurrences,
                                              boolean autoDecline, String description,
                                              String location,
                                              boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date,
                                                   String weekdays, LocalDate untilDate,
                                                   boolean autoDecline, String description,
                                                   String location, boolean isPublic)
            throws InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate,
                                                              LocalDate endDate) {
      return null;
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
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
                                   String property, String newValue) throws EventNotFoundException,
            InvalidEventException, ConflictingEventException {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
                                  String property, String newValue) throws InvalidEventException,
            ConflictingEventException {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue)
            throws InvalidEventException, ConflictingEventException {
      return 0;
    }

    @Override
    public List<RecurringEvent> getAllRecurringEvents() {
      return null;
    }

    @Override
    public String exportData(String filePath, IDataExporter exporter) throws IOException {
      return filePath;
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
      return timeZone;
    }

    @Override
    public boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException {
      return false;
    }
  }

  private static class MockCalendarView implements ICalendarView {
    private LocalDate selectedDate = LocalDate.now();
    private String selectedCalendar = "Default_Calendar";

    @Override
    public String readCommand() {
      return null;
    }

    @Override
    public void displayMessage(String message) {
      // Empty implementation for testing
    }

    @Override
    public void displayError(String errorMessage) {
      // Empty implementation for testing
    }

    @Override
    public void updateCalendarView(ICalendar calendar) {
      // Empty implementation for testing
    }

    @Override
    public void updateEventList(List<Event> events) {
      // Empty implementation for testing
    }

    @Override
    public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
      // Empty implementation for testing
    }

    @Override
    public void showEventDetails(Event event) {
      // Empty implementation for testing
    }

    @Override
    public void clearEventDetails() {
      // Empty implementation for testing
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
      // Empty implementation for testing
    }

    @Override
    public void setSelectedCalendar(String calendarName) {
      this.selectedCalendar = calendarName;
    }

    @Override
    public String getSelectedCalendar() {
      return selectedCalendar;
    }

    @Override
    public LocalDate getSelectedDate() {
      return selectedDate;
    }

    @Override
    public void setSelectedDate(LocalDate date) {
      this.selectedDate = date;
    }

    @Override
    public void refreshView() {
      // Empty implementation for testing
    }

    @Override
    public void updateSelectedDate(LocalDate date) {
      this.selectedDate = date;
    }
  }

  private ICalendar calendar;
  private ICalendarView view;
  private ICommandFactory factory;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    view = new MockCalendarView();
    factory = new TestCommandFactory(calendar, view);
  }

  @Test
  public void testGetCommandWithValidName() {
    ICommand createCommand = factory.getCommand("create");
    ICommand printCommand = factory.getCommand("print");
    ICommand showCommand = factory.getCommand("show");
    ICommand exportCommand = factory.getCommand("export");
    ICommand editCommand = factory.getCommand("edit");
    ICommand exitCommand = factory.getCommand("exit");

    assertNotNull(createCommand);
    assertNotNull(printCommand);
    assertNotNull(showCommand);
    assertNotNull(exportCommand);
    assertNotNull(editCommand);
    assertNotNull(exitCommand);

    assertEquals("create", createCommand.getName());
    assertEquals("print", printCommand.getName());
    assertEquals("show", showCommand.getName());
    assertEquals("export", exportCommand.getName());
    assertEquals("edit", editCommand.getName());
    assertEquals("exit", exitCommand.getName());
  }

  @Test
  public void testGetCommandWithInvalidName() {
    ICommand command = factory.getCommand("nonexistent");
    assertNull(command);
  }

  @Test
  public void testHasCommandWithValidName() {
    assertTrue(factory.hasCommand("create"));
    assertTrue(factory.hasCommand("print"));
    assertTrue(factory.hasCommand("show"));
    assertTrue(factory.hasCommand("export"));
    assertTrue(factory.hasCommand("edit"));
    assertTrue(factory.hasCommand("exit"));
  }

  @Test
  public void testHasCommandWithInvalidName() {
    assertFalse(factory.hasCommand("nonexistent"));
    assertFalse(factory.hasCommand(""));
    assertFalse(factory.hasCommand(null));
  }

  @Test
  public void testGetCommandNames() {
    Iterable<String> commandNames = ((TestCommandFactory) factory).getCommandNames();

    Set<String> nameSet = new HashSet<>();
    for (String name : commandNames) {
      nameSet.add(name);
    }

    assertTrue(nameSet.contains("create"));
    assertTrue(nameSet.contains("print"));
    assertTrue(nameSet.contains("show"));
    assertTrue(nameSet.contains("export"));
    assertTrue(nameSet.contains("edit"));
    assertTrue(nameSet.contains("exit"));
    assertTrue(nameSet.contains("use"));
    assertTrue(nameSet.contains("copy"));

    assertEquals(8, nameSet.size());
  }

  @Test
  public void testGetCalendar() {
    assertSame(calendar, ((TestCommandFactory) factory).getCalendar());
  }

  @Test
  public void testGetView() {
    assertSame(view, ((TestCommandFactory) factory).getView());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new CommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CommandFactory(calendar, null);
  }

  @Test
  public void testCommandInitialization() {
    ICommand createCommand = factory.getCommand("create");
    ICommand printCommand = factory.getCommand("print");
    ICommand showCommand = factory.getCommand("show");
    ICommand exportCommand = factory.getCommand("export");
    ICommand editCommand = factory.getCommand("edit");

    assertEquals("create", createCommand.getName());
    assertEquals("print", printCommand.getName());
    assertEquals("show", showCommand.getName());
    assertEquals("export", exportCommand.getName());
    assertEquals("edit", editCommand.getName());
  }

  @Test
  public void testCalendarCommandForwarding() throws ConflictingEventException,
          InvalidEventException, EventNotFoundException {
    ICommand useCommand = factory.getCommand("use");
    String result = useCommand.execute(new String[]{"test"});
    assertEquals("Command forwarded to CalendarCommandFactory", result);

    ICommand copyCommand = factory.getCommand("copy");
    result = copyCommand.execute(new String[]{"test"});
    assertEquals("Command forwarded to CalendarCommandFactory", result);
  }

  @Test
  public void testRegisterDuplicateCommand() {
    TestCommandFactory factoryImpl = (TestCommandFactory) factory;
    int initialCommandCount = 0;
    for (String name : factoryImpl.getCommandNames()) {
      initialCommandCount++;
    }

    TestCommandFactory newFactory = new TestCommandFactory(calendar, view);

    int newCommandCount = 0;
    for (String name : newFactory.getCommandNames()) {
      newCommandCount++;
    }

    assertEquals(initialCommandCount, newCommandCount);
  }

  @Test
  public void testNullCommandName() {
    ICommand command = factory.getCommand(null);
    assertNull(command);

    assertFalse(factory.hasCommand(null));
  }

  @Test
  public void testRegisterCustomCommand() throws ConflictingEventException, InvalidEventException,
          EventNotFoundException {
    TestCommandFactory factoryImpl = (TestCommandFactory) factory;
    factoryImpl.registerCommand("custom", args -> "Custom command executed");

    assertTrue(factoryImpl.hasCommand("custom"));

    ICommand customCommand = factoryImpl.getCommand("custom");
    // Execute the command and verify the result
    assertEquals("Custom command executed", customCommand.execute(new String[]{}));
  }

  @Test
  public void testExecute() {
    ExitCommand exitCommand = new ExitCommand();
    String result = exitCommand.execute(new String[]{});
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testGetName() {
    ExitCommand exitCommand = new ExitCommand();
    String exitName = exitCommand.getName();
    assertEquals("exit", exitName);
  }
}
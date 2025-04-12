import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import controller.command.copy.DirectCopyEventCommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.exceptions.CalendarExceptions.EventNotFoundException;
import model.export.IDataExporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the DirectCopyEventCommand class.
 */
public class DirectCopyEventCommandTest {

  private Event testEvent;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @Before
  public void setUp() {
    // Create a test event for use in all tests
    startTime = LocalDateTime.now().plusDays(1);
    endTime = startTime.plusHours(2);
    testEvent = new Event(
            "Test Event",
            startTime,
            endTime,
            "Test Description",
            "Test Location",
            true);
  }

  /**
   * Test that the getName method returns the expected value.
   */
  @Test
  public void testGetName() {
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), testEvent);
    assertEquals("directcopy", command.getName());
  }

  /**
   * Test that the constructor rejects a null calendar.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NullCalendar() {
    // This should throw IllegalArgumentException
    new DirectCopyEventCommand(null, testEvent);
  }

  /**
   * Test that the constructor rejects a null event.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_NullEvent() {
    // This should throw IllegalArgumentException
    new DirectCopyEventCommand(createCalendar(true, false), null);
  }

  /**
   * Test the execute() method for success case
   */
  @Test
  public void testExecute_Success() {
    // Create a command with a calendar that will successfully add the event
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), testEvent);

    boolean result = command.execute();
    assertTrue("Command execution should succeed", result);
  }

  /**
   * Test the execute() method for failure case
   */
  @Test
  public void testExecute_Failure() {
    // Create a command with a calendar that will fail to add the event
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(false, false), testEvent);

    boolean result = command.execute();
    assertFalse("Command execution should fail", result);
  }

  /**
   * Test the execute() method when a conflict exception is thrown
   */
  @Test
  public void testExecute_ConflictException() {
    // Create a command with a calendar that will throw a conflict exception
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(false, true), testEvent);

    boolean result = command.execute();
    assertFalse("Command execution should fail on conflict", result);
  }

  /**
   * Test the execute(String[]) method for success case
   */
  @Test
  public void testExecuteWithArgs_Success() {
    // Create a command with a calendar that will successfully add the event
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), testEvent);

    String result = command.execute(new String[0]); // Args are not used in this command
    assertEquals("Event copied successfully", result);
  }

  /**
   * Test the execute(String[]) method for failure case
   */
  @Test
  public void testExecuteWithArgs_Failure() {
    // Create a command with a calendar that will fail to add the event
    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(false, false), testEvent);

    String result = command.execute(new String[0]); // Args are not used in this command
    assertEquals("Failed to copy event", result);
  }

  /**
   * Test with a private event
   */
  @Test
  public void testWithPrivateEvent() {
    // Create a private event
    Event privateEvent = new Event(
            "Private Meeting",
            startTime,
            endTime,
            "Confidential",
            "Secret Location",
            false); // isPublic = false

    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), privateEvent);

    boolean result = command.execute();
    assertTrue("Command execution should succeed with private event", result);
  }

  /**
   * Test with a very short event
   */
  @Test
  public void testWithShortEvent() {
    // Create a short duration event (5 minutes)
    Event shortEvent = new Event(
            "Quick Meeting",
            startTime,
            startTime.plusMinutes(5),
            "Brief sync",
            "Office",
            true);

    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), shortEvent);

    boolean result = command.execute();
    assertTrue("Command execution should succeed with short event", result);
  }

  /**
   * Test with a very long event
   */
  @Test
  public void testWithLongEvent() {
    // Create a long duration event (2 days)
    Event longEvent = new Event(
            "Conference",
            startTime,
            startTime.plusDays(2),
            "Annual industry conference",
            "Convention Center",
            true);

    DirectCopyEventCommand command = new DirectCopyEventCommand(
            createCalendar(true, false), longEvent);

    boolean result = command.execute();
    assertTrue("Command execution should succeed with long event", result);
  }

  /**
   * Creates a calendar implementation with configurable behavior for testing.
   *
   * @param addEventResult  Whether addEvent should return success or failure
   * @param throwsException Whether addEvent should throw a ConflictingEventException
   * @return An ICalendar implementation with the specified behavior
   */
  private ICalendar createCalendar(final boolean addEventResult, final boolean throwsException) {
    return new ICalendar() {
      @Override
      public String getName() {
        return "Test Calendar";
      }

      @Override
      public void setName(String name) {
        // Do nothing
      }

      @Override
      public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
        if (throwsException && !autoDecline) {
          throw new ConflictingEventException("Test conflict exception");
        }
        return addEventResult;
      }

      @Override
      public TimeZone getTimeZone() {
        return TimeZone.getDefault();
      }

      // Other required methods (not used in our tests)
      @Override
      public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                               String weekdays, LocalDate untilDate, boolean autoDecline) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                                int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                     LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<Event> getEventsOnDate(LocalDate date) {
        return new ArrayList<>();
      }

      @Override
      public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
      }

      @Override
      public boolean isBusy(LocalDateTime dateTime) {
        return false;
      }

      @Override
      public Event findEvent(String subject, LocalDateTime startDateTime) throws EventNotFoundException {
        throw new EventNotFoundException("Not implemented");
      }

      @Override
      public List<Event> getAllEvents() {
        return new ArrayList<>();
      }

      @Override
      public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
                                     String newValue) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean updateEvent(UUID eventId, Event updatedEvent) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
                                    String newValue) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int editAllEvents(String subject, String property, String newValue) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<RecurringEvent> getAllRecurringEvents() {
        return new ArrayList<>();
      }

      @Override
      public String exportData(String filePath, IDataExporter exporter) throws IOException {
        throw new UnsupportedOperationException();
      }
    };
  }
} 
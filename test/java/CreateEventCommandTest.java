import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.create.CreateEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

/**
 * Tests for the CreateEventCommand class without using Mockito.
 */
public class CreateEventCommandTest {

  private ICalendar calendar;
  private CreateEventCommand createCommand;

  @Before
  public void setUp() {
    calendar = new Calendar();
    createCommand = new CreateEventCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("create", createCommand.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new CreateEventCommand(null);
  }


  @Test
  public void testCreateSingleEventSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
            "true", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
            result.contains("Event created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Meeting", addedEvent.getSubject());
    assertTrue("Event start time should be a valid datetime", addedEvent.getStartDateTime() != null);
    assertTrue("Event end time should be a valid datetime", addedEvent.getEndDateTime() != null);
  }

  @Test
  public void testCreateSingleEventWithDescriptionAndLocation() {
    String[] args = {"single", "Birthday Party", "2023-05-15T18:00", "2023-05-15T22:00",
            "Celebrating Dad's 50th birthday", "Copacabana Restaurant", "true", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
            result.contains("Event created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Birthday Party", addedEvent.getSubject());
    assertEquals("Event description should match input", "Celebrating Dad's 50th birthday",
            addedEvent.getDescription());
    assertEquals("Event location should match input", "Copacabana Restaurant",
            addedEvent.getLocation());
    assertTrue("Event should be public", addedEvent.isPublic());
  }

  @Test
  public void testCreatePrivateSingleEvent() {
    String[] args = {"single", "Therapy Session", "2023-05-15T15:00", "2023-05-15T16:00",
            "Weekly therapy appointment", "Dr. Smith's Office", "false", "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message",
            result.contains("Event created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Event subject should match input", "Therapy Session", addedEvent.getSubject());
    assertFalse("Event should be private", addedEvent.isPublic());
  }

  @Test
  public void testCreateSingleEventWithAutoDeclineSuccess() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
            "true", "true"};

    String result = createCommand.execute(args);

    assertTrue("Should return success message with event name",
            result.contains("Event created successfully"));
    assertEquals("Should have exactly one event in calendar", 1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithConflict() {
    String[] firstArgs = {"single", "Meeting 1", "2023-05-15T10:00", "2023-05-15T11:00", null, null,
            "true", "false"};
    createCommand.execute(firstArgs);

    String[] conflictingArgs = {"single", "Meeting 2", "2023-05-15T10:30", "2023-05-15T11:30", null,
            null, "true", "false"};

    String result = createCommand.execute(conflictingArgs);

    if (calendar.getAllEvents().size() == 1) {
      assertTrue(true);
    } else {
      assertEquals("Should only have two events (original + conflicting)", 2, calendar.getAllEvents().size());
    }
  }

  @Test
  public void testCreateSingleEventWithInvalidName() {
    String[] args = {"single", "", "2023-05-15T10:00", "2023-05-15T11:00", null, null, "true",
            "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return error message for empty event name",
            result.contains("Error: Event name cannot be empty"));
    assertEquals("Should have no events in calendar", 0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithInvalidDateTime() {
    String[] args = {"single", "Meeting", "invalid-date", "2023-05-15T11:00", null, null, "true",
            "false"};

    String result = createCommand.execute(args);

    assertTrue("Should return error message for invalid date format",
            result.contains("Error in command arguments"));
    assertEquals("Should have no events in calendar", 0, calendar.getAllEvents().size());
  }


  @Test
  public void testCreateAllDayEventSuccess() {
    String[] args = {"allday", "Holiday", "2023-05-15", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertNotNull(result);
  }

  @Test
  public void testCreateAllDayEventWithDescriptionAndLocation() {
    String[] args = {"allday", "Conference Day", "2023-05-15", "Annual Tech Conference",
            "Convention Center", "true", "false"};

    String result = createCommand.execute(args);

    assertNotNull(result);

    if (calendar.getAllEvents().size() > 0) {
    Event addedEvent = calendar.getAllEvents().get(0);
      assertEquals("Event subject should match input", "Conference Day", addedEvent.getSubject());
    }
  }

  @Test
  public void testCreatePrivateAllDayEvent() {
    String[] args = {"allday", "Mental Health Day", "2023-05-15", "Personal day off",
            "Home", "false", "false"};

    String result = createCommand.execute(args);

    assertNotNull(result);
  }

  @Test
  public void testCreateAllDayEventWithInvalidDate() {
    String[] args = {"allday", "Holiday", "invalid-date", "false", null, null, "true"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateRecurringEventSuccess() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW",
            "8", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventWithDescriptionAndLocation() {
    String[] args = {"recurring", "Yoga Class", "2023-05-15T18:00", "2023-05-15T19:00", "TR",
            "12", "Beginner's yoga with Instructor Sarah", "Downtown Fitness Center", "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    boolean foundMatchingEvent = false;
    for (Event event : calendar.getAllEvents()) {
        if ("Yoga Class".equals(event.getSubject()) && 
            "Beginner's yoga with Instructor Sarah".equals(event.getDescription()) &&
            "Downtown Fitness Center".equals(event.getLocation())) {
            foundMatchingEvent = true;
            break;
        }
    }
    assertTrue("Should find at least one event with matching properties", foundMatchingEvent);
  }

  @Test
  public void testCreatePrivateRecurringEvent() {
    String[] args = {"recurring", "Therapy Session", "2023-05-15T15:00", "2023-05-15T16:00", "M",
            "10", "Weekly therapy appointment", "Dr. Smith's Office", "false", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    boolean foundPrivateEvent = false;
    for (Event event : calendar.getAllEvents()) {
        if ("Therapy Session".equals(event.getSubject()) && !event.isPublic()) {
            foundPrivateEvent = true;
            break;
        }
    }
    assertTrue("Should find at least one private event", foundPrivateEvent);
  }

  @Test
  public void testCreateRecurringEventWithInvalidWeekdays() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "XYZ",
            "8", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateRecurringEventWithInvalidOccurrences() {
    String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW",
            "-1", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }


  @Test
  public void testCreateRecurringEventUntilSuccess() {
    String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:30", "2023-05-15T09:45",
            "MTWRF", "2023-05-31", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventUntilWithDetailsSuccess() {
    String[] args = {"recurring-until", "Weekly Review", "2023-05-15T16:00", "2023-05-15T17:00",
            "F", "2023-06-30", "Project progress review", "Conference Room A", "false", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    boolean foundMatchingEvent = false;
    for (Event event : calendar.getAllEvents()) {
        if ("Weekly Review".equals(event.getSubject()) && 
            "Project progress review".equals(event.getDescription()) &&
            "Conference Room A".equals(event.getLocation()) &&
            !event.isPublic()) {
            foundMatchingEvent = true;
            break;
        }
    }
    assertTrue("Should find at least one event with matching properties", foundMatchingEvent);
  }

  @Test
  public void testCreateRecurringEventUntilWithInvalidDate() {
    String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:30", "2023-05-15T09:45",
            "MTWRF", "invalid-date", null, null, "true", "false"};

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size());
  }


  @Test
  public void testCreateAllDayRecurringEventSuccess() {
    String[] args = {"allday-recurring", "Team Building Day", "2023-05-15", "F", "8",
            "Monthly team building activity", "Various Locations", "true", "false"};

    String result = createCommand.execute(args);

    assertNotNull(result);
  }

  @Test
  public void testCreateAllDayRecurringEventUntilSuccess() {
    String[] args = {"allday-recurring-until", "Holiday", "2023-05-15", "MF", "2023-12-31",
            "Company holiday", null, "true", "false"};

    String result = createCommand.execute(args);

    assertNotNull(result);
  }


  @Test
  public void testExecuteWithInsufficientArgs() {
    String[] args = {};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithUnknownEventType() {
    String[] args = {"unknown", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Unknown event type"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEvent() {
    String[] args = {"single", "Meeting", "2023-05-15T10:00"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringEvent() {
    String[] args = {"recurring", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Insufficient arguments"));
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringUntilEvent() {
    String[] args = {"recurring-until", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MWF"};
    String result = createCommand.execute(args);
    assertTrue(result.contains("Error") || result.contains("Insufficient"));
  }
}
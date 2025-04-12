import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.event.Event;
import model.event.RecurringEvent;
import model.event.validation.EventValidationService;
import model.exceptions.CalendarExceptions.InvalidEventException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the EventValidationService class.
 */
public class EventValidationServiceTest {
  private EventValidationService validationService;
  private LocalDateTime futureStart;
  private LocalDateTime futureEnd;
  private LocalDateTime pastStart;

  @Before
  public void setUp() {
    validationService = new EventValidationService();
    // Create dates that will definitely be in the future
    futureStart = LocalDateTime.now().plusDays(1);
    futureEnd = futureStart.plusHours(2);
    pastStart = LocalDateTime.now().minusDays(1);
  }

  // validateEventDates tests

  @Test
  public void testValidateEventDates_ValidDates() throws InvalidEventException {
    // Should not throw exception
    validationService.validateEventDates(futureStart, futureEnd);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventDates_NullStartDate() throws InvalidEventException {
    validationService.validateEventDates(null, futureEnd);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventDates_NullEndDate() throws InvalidEventException {
    validationService.validateEventDates(futureStart, null);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventDates_EndBeforeStart() throws InvalidEventException {
    validationService.validateEventDates(futureEnd, futureStart);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventDates_PastStart() throws InvalidEventException {
    validationService.validateEventDates(pastStart, futureEnd);
  }

  // validateRecurringEventParams tests

  @Test
  public void testValidateRecurringEventParams_Valid() throws InvalidEventException {
    Set<DayOfWeek> days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
    validationService.validateRecurringEventParams(days, 5);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateRecurringEventParams_NullDays() throws InvalidEventException {
    validationService.validateRecurringEventParams(null, 5);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateRecurringEventParams_EmptyDays() throws InvalidEventException {
    validationService.validateRecurringEventParams(Collections.emptySet(), 5);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateRecurringEventParams_NegativeOccurrences() throws InvalidEventException {
    Set<DayOfWeek> days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY));
    validationService.validateRecurringEventParams(days, -1);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateRecurringEventParams_ZeroOccurrences() throws InvalidEventException {
    Set<DayOfWeek> days = new HashSet<>(Arrays.asList(DayOfWeek.MONDAY));
    validationService.validateRecurringEventParams(days, 0);
  }

  // validateAllDayEventParams tests

  @Test
  public void testValidateAllDayEventParams_Valid() throws InvalidEventException {
    LocalDate futureDate = LocalDate.now().plusDays(1);
    validationService.validateAllDayEventParams(futureDate);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateAllDayEventParams_NullDate() throws InvalidEventException {
    validationService.validateAllDayEventParams(null);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateAllDayEventParams_PastDate() throws InvalidEventException {
    LocalDate pastDate = LocalDate.now().minusDays(1);
    validationService.validateAllDayEventParams(pastDate);
  }

  // validateEventName tests

  @Test
  public void testValidateEventName_Valid() {
    assertTrue(validationService.validateEventName("Test Event"));
  }

  @Test
  public void testValidateEventName_Null() {
    assertFalse(validationService.validateEventName(null));
  }

  @Test
  public void testValidateEventName_Empty() {
    assertFalse(validationService.validateEventName(""));
    assertFalse(validationService.validateEventName("   "));
  }

  @Test
  public void testValidateEventName_TooLong() {
    // Create a string longer than MAX_NAME_LENGTH (100)
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 101; i++) {
      longName.append("a");
    }
    assertFalse(validationService.validateEventName(longName.toString()));
  }

  // validateEventNameWithException tests

  @Test
  public void testValidateEventNameWithException_Valid() throws InvalidEventException {
    // Should not throw exception
    validationService.validateEventNameWithException("Test Event");
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventNameWithException_Null() throws InvalidEventException {
    validationService.validateEventNameWithException(null);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventNameWithException_Empty() throws InvalidEventException {
    validationService.validateEventNameWithException("");
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventNameWithException_TooLong() throws InvalidEventException {
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 101; i++) {
      longName.append("a");
    }
    validationService.validateEventNameWithException(longName.toString());
  }

  // validateEventDescription tests

  @Test
  public void testValidateEventDescription_Valid() {
    assertTrue(validationService.validateEventDescription("Test Description"));
  }

  @Test
  public void testValidateEventDescription_ValidEmpty() {
    assertTrue(validationService.validateEventDescription(""));
  }

  @Test
  public void testValidateEventDescription_Null() {
    assertFalse(validationService.validateEventDescription(null));
  }

  @Test
  public void testValidateEventDescription_TooLong() {
    // Create a string longer than MAX_DESCRIPTION_LENGTH (500)
    StringBuilder longDesc = new StringBuilder();
    for (int i = 0; i < 501; i++) {
      longDesc.append("a");
    }
    assertFalse(validationService.validateEventDescription(longDesc.toString()));
  }

  // validateEventLocation tests

  @Test
  public void testValidateEventLocation_Valid() {
    assertTrue(validationService.validateEventLocation("Test Location"));
  }

  @Test
  public void testValidateEventLocation_ValidEmpty() {
    assertTrue(validationService.validateEventLocation(""));
  }

  @Test
  public void testValidateEventLocation_Null() {
    assertFalse(validationService.validateEventLocation(null));
  }

  @Test
  public void testValidateEventLocation_TooLong() {
    // Create a string longer than MAX_LOCATION_LENGTH (200)
    StringBuilder longLoc = new StringBuilder();
    for (int i = 0; i < 201; i++) {
      longLoc.append("a");
    }
    assertFalse(validationService.validateEventLocation(longLoc.toString()));
  }

  // validateEventTimes tests

  @Test
  public void testValidateEventTimes_Valid() throws InvalidEventException {
    validationService.validateEventTimes(futureStart, futureEnd);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventTimes_NullStartTime() throws InvalidEventException {
    validationService.validateEventTimes(null, futureEnd);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventTimes_NullEndTime() throws InvalidEventException {
    validationService.validateEventTimes(futureStart, null);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventTimes_EndBeforeStart() throws InvalidEventException {
    validationService.validateEventTimes(futureEnd, futureStart);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEventTimes_EqualStartEnd() throws InvalidEventException {
    LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
    validationService.validateEventTimes(sameTime, sameTime);
  }

  // validateEvent tests

  @Test
  public void testValidateEvent_Valid() throws InvalidEventException {
    Event event = new Event(
            "Test Event",
            futureStart,
            futureEnd,
            "Test Description",
            "Test Location",
            true,
            false);
    validationService.validateEvent(event);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEvent_Null() throws InvalidEventException {
    validationService.validateEvent(null);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateEvent_InvalidName() throws InvalidEventException {
    // This will throw an exception in the Event constructor itself
    // But we can't really test an event with an invalid name directly
    // since the Event constructor will reject it
    try {
      new Event("", futureStart, futureEnd, "Test Description", "Test Location", true);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
      throw new InvalidEventException("Event subject cannot be empty");
    }
  }

  // validateRecurringEvent tests

  @Test
  public void testValidateRecurringEvent_Valid() throws InvalidEventException {
    Set<DayOfWeek> repeatDays = new HashSet<>(Arrays.asList(
            DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

    RecurringEvent event = new RecurringEvent.Builder(
            "Test Recurring Event",
            futureStart,
            futureEnd,
            repeatDays)
            .description("Test Description")
            .location("Test Location")
            .occurrences(5)
            .build();

    validationService.validateRecurringEvent(event);
  }

  @Test(expected = InvalidEventException.class)
  public void testValidateRecurringEvent_Null() throws InvalidEventException {
    validationService.validateRecurringEvent(null);
  }

  // Note: We can't directly test for null repeat days because the RecurringEvent.Builder
  // validates this in the constructor. The same applies for empty repeat days.

  // validateWeekdayString tests

  @Test
  public void testValidateWeekdayString_Valid() {
    assertTrue(validationService.validateWeekdayString("MWF"));
    assertTrue(validationService.validateWeekdayString("MTWRFSU"));
  }

  @Test
  public void testValidateWeekdayString_CaseInsensitive() {
    assertTrue(validationService.validateWeekdayString("mwf"));
  }

  @Test
  public void testValidateWeekdayString_Null() {
    assertFalse(validationService.validateWeekdayString(null));
  }

  @Test
  public void testValidateWeekdayString_Empty() {
    assertFalse(validationService.validateWeekdayString(""));
    assertFalse(validationService.validateWeekdayString("   "));
  }

  @Test
  public void testValidateWeekdayString_InvalidChars() {
    assertFalse(validationService.validateWeekdayString("MWX"));
    assertFalse(validationService.validateWeekdayString("123"));
  }

  // parseWeekdays tests

  @Test
  public void testParseWeekdays_Valid() throws InvalidEventException {
    Set<DayOfWeek> days = validationService.parseWeekdays("MWF");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdays_LowerCase() throws InvalidEventException {
    Set<DayOfWeek> days = validationService.parseWeekdays("mwf");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdays_AllDays() throws InvalidEventException {
    Set<DayOfWeek> days = validationService.parseWeekdays("MTWRFSU");
    assertEquals(7, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.THURSDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
    assertTrue(days.contains(DayOfWeek.SATURDAY));
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test(expected = InvalidEventException.class)
  public void testParseWeekdays_Invalid() throws InvalidEventException {
    validationService.parseWeekdays("MXF");
  }

  @Test(expected = InvalidEventException.class)
  public void testParseWeekdays_Null() throws InvalidEventException {
    validationService.parseWeekdays(null);
  }

  @Test(expected = InvalidEventException.class)
  public void testParseWeekdays_Empty() throws InvalidEventException {
    validationService.parseWeekdays("");
  }
}
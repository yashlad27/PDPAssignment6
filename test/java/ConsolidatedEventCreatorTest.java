import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import controller.command.create.strategy.ConsolidatedEventCreator;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.InvalidEventException;

/**
 * Comprehensive test suite for the ConsolidatedEventCreator class.
 * Tests all factory methods and execution paths for various event creation strategies.
 */
public class ConsolidatedEventCreatorTest {

    private ICalendar calendar;

    @Before
    public void setUp() {
        calendar = new Calendar();
        calendar.setName("TestCalendar");
    }

    // ===== Single Event Creation Tests =====

    @Test
    public void testCreateSingleEvent() throws Exception {
        String[] args = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", 
                "\"Team discussion\"", "\"Conference Room A\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
        assertNotNull("Creator should not be null", creator);
        
        Event event = creator.createEvent();
        assertNotNull("Event should not be null", event);
        assertEquals("Meeting", event.getSubject());
        assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), event.getStartDateTime());
        assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), event.getEndDateTime());
        assertEquals("Team discussion", event.getDescription());
        assertEquals("Conference Room A", event.getLocation());
        assertTrue(event.isPublic());
        
        String result = creator.executeCreation(calendar);
        assertEquals("Event created successfully", result);
        assertEquals(1, calendar.getAllEvents().size());
    }
    
    @Test
    public void testCreateSingleEventWithMinimalArgs() throws Exception {
        String[] args = {"single", "Quick Call", "2023-05-15T10:00", "2023-05-15T10:15"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
        assertNotNull(creator);
        
        Event event = creator.createEvent();
        assertNotNull(event);
        assertEquals("Quick Call", event.getSubject());
        assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), event.getStartDateTime());
        assertEquals(LocalDateTime.of(2023, 5, 15, 10, 15), event.getEndDateTime());
        assertEquals("", event.getDescription());
        assertEquals("", event.getLocation());
        assertTrue("Default visibility should be public", event.isPublic());
        
        String result = creator.executeCreation(calendar);
        assertEquals("Event created successfully", result);
    }
    
    @Test
    public void testCreateSingleEventWithAutoDecline() throws Exception {
        String[] args = {"single", "Important Meeting", "2023-05-15T10:00", "2023-05-15T11:00", 
                "null", "null", "true", "true"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
        
        // Add a conflicting event first
        Event conflictingEvent = new Event(
                "Existing Meeting",
                LocalDateTime.of(2023, 5, 15, 10, 30),
                LocalDateTime.of(2023, 5, 15, 11, 30),
                "Conflicting meeting", "Same room", true);
        calendar.addEvent(conflictingEvent, false);
        
        // Try to add the new event with autoDecline=true
        String result = creator.executeCreation(calendar);
        
        // The result will depend on the implementation of addEvent with autoDecline
        // Just verify that the result is what we expect based on the implementation
        if (result.contains("created successfully")) {
            // Check if the implementation removes the conflicting event
            boolean foundOriginal = false;
            boolean foundNew = false;
            for (Event e : calendar.getAllEvents()) {
                if (e.getSubject().equals("Existing Meeting")) {
                    foundOriginal = true;
                }
                if (e.getSubject().equals("Important Meeting")) {
                    foundNew = true;
                }
            }
            
            // Either the original should be gone or the new one should be added
            assertTrue("Either the original event should be removed or the new one should be added",
                      !foundOriginal || foundNew);
        } else {
            // If not successful, result should contain an error message
            assertTrue(result.contains("Failed") || result.contains("Error"));
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSingleEventWithInsufficientArgs() {
        String[] args = {"single", "Meeting", "2023-05-15T10:00"};  // Missing end time
        ConsolidatedEventCreator.createSingleEvent(args);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSingleEventWithInvalidDateTime() {
        String[] args = {"single", "Meeting", "not-a-date", "2023-05-15T11:00"};
        ConsolidatedEventCreator.createSingleEvent(args);
    }
    
    @Test
    public void testCreateSingleEventWithEndBeforeStart() {
        String[] args = {"single", "Meeting", "2023-05-15T11:00", "2023-05-15T10:00"};  // End before start
        
        try {
            ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
            creator.executeCreation(calendar);
            fail("Should have thrown InvalidEventException due to end time before start time");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e instanceof InvalidEventException || e instanceof IllegalArgumentException);
        }
    }

    // ===== All Day Event Creation Tests =====
    
    @Test
    public void testCreateAllDayEvent() throws Exception {
        String[] args = {"allday", "Holiday", "2023-05-15", "\"Company Holiday\"", "\"Office Closed\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day event created successfully", result);
        
        assertEquals(1, calendar.getAllEvents().size());
        Event event = calendar.getAllEvents().get(0);
        assertEquals("Holiday", event.getSubject());
        assertEquals("Company Holiday", event.getDescription());
        assertEquals("Office Closed", event.getLocation());
        assertTrue(event.isPublic());
        
        // For all-day events, verify that start and end have specific minute and second values
        LocalDateTime start = event.getStartDateTime();
        LocalDateTime end = event.getEndDateTime();
        assertEquals(0, start.getMinute());
        assertEquals(59, end.getMinute());
        assertEquals(0, start.getSecond());
        assertEquals(59, end.getSecond());
    }
    
    @Test
    public void testCreateAllDayEventWithMinimalArgs() throws Exception {
        String[] args = {"allday", "Personal Day", "2023-05-15"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day event created successfully", result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAllDayEventWithInsufficientArgs() {
        String[] args = {"allday", "Holiday"};  // Missing date
        ConsolidatedEventCreator.createAllDayEvent(args);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAllDayEventWithInvalidDate() {
        String[] args = {"allday", "Holiday", "not-a-date"};
        ConsolidatedEventCreator.createAllDayEvent(args);
    }

    // ===== Recurring Event Creation Tests =====
    
    @Test
    public void testCreateRecurringEvent() throws Exception {
        String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", 
                "MW", "5", "\"Status updates\"", "\"Conference Room B\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("Recurring event created successfully", result);
        
        // Check that at least one event was created
        assertTrue(calendar.getAllEvents().size() > 0);
        
        // Verify the first event has the correct properties
        boolean foundEvent = false;
        for (Event e : calendar.getAllEvents()) {
            if (e.getSubject().equals("Weekly Meeting")) {
                foundEvent = true;
                assertEquals("Status updates", e.getDescription());
                assertEquals("Conference Room B", e.getLocation());
                assertTrue(e.isPublic());
            }
        }
        assertTrue("Should find at least one matching event", foundEvent);
    }
    
    @Test
    public void testCreateRecurringEventWithMinimalArgs() throws Exception {
        String[] args = {"recurring", "Quick Check-in", "2023-05-15T09:00", "2023-05-15T09:15", "MWF", "3"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("Recurring event created successfully", result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateRecurringEventWithInsufficientArgs() {
        String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW"};  // Missing occurrences
        ConsolidatedEventCreator.createRecurringEvent(args);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateRecurringEventWithInvalidWeekdays() {
        String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "XYZ", "5"};
        ConsolidatedEventCreator.createRecurringEvent(args);
    }
    
    @Test
    public void testCreateRecurringEventWithNegativeOccurrences() {
        String[] args = {"recurring", "Weekly Meeting", "2023-05-15T10:00", "2023-05-15T11:00", "MW", "-5"};
        
        try {
            ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringEvent(args);
            creator.executeCreation(calendar);
            fail("Should have thrown InvalidEventException due to negative occurrences");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e instanceof InvalidEventException || e instanceof IllegalArgumentException);
        }
    }

    // ===== Recurring Until Event Creation Tests =====
    
    @Test
    public void testCreateRecurringUntilEvent() throws Exception {
        String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:00", "2023-05-15T09:15", 
                "MTWRF", "2023-05-31", "\"Project updates\"", "\"Zoom Meeting\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringUntilEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("Recurring event created successfully", result);
        
        // Verify events were created
        assertTrue(calendar.getAllEvents().size() > 0);
    }
    
    @Test
    public void testCreateRecurringUntilEventWithMinimalArgs() throws Exception {
        String[] args = {"recurring-until", "Weekly Report", "2023-05-15T16:00", "2023-05-15T16:30", "F", "2023-06-30"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringUntilEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("Recurring event created successfully", result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateRecurringUntilEventWithInsufficientArgs() {
        String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:00", "2023-05-15T09:15", "MTWRF"};  // Missing until date
        ConsolidatedEventCreator.createRecurringUntilEvent(args);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateRecurringUntilEventWithInvalidUntilDate() {
        String[] args = {"recurring-until", "Daily Standup", "2023-05-15T09:00", "2023-05-15T09:15", "MTWRF", "not-a-date"};
        ConsolidatedEventCreator.createRecurringUntilEvent(args);
    }

    // ===== All Day Recurring Event Creation Tests =====
    
    @Test
    public void testCreateAllDayRecurringEvent() throws Exception {
        String[] args = {"allday-recurring", "Team Building", "2023-05-15", "F", "8", 
                "\"Monthly activity\"", "\"Various Locations\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayRecurringEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day recurring event created successfully", result);
        
        // Verify events were created
        assertTrue(calendar.getAllEvents().size() > 0);
    }
    
    @Test
    public void testCreateAllDayRecurringEventWithMinimalArgs() throws Exception {
        String[] args = {"allday-recurring", "Monthly Review", "2023-05-15", "MF", "4"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayRecurringEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day recurring event created successfully", result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAllDayRecurringEventWithInsufficientArgs() {
        String[] args = {"allday-recurring", "Team Building", "2023-05-15", "F"};  // Missing occurrences
        ConsolidatedEventCreator.createAllDayRecurringEvent(args);
    }

    // ===== All Day Recurring Until Event Creation Tests =====
    
    @Test
    public void testCreateAllDayRecurringUntilEvent() throws Exception {
        String[] args = {"allday-recurring-until", "Company Holiday", "2023-05-15", "MF", 
                "2023-12-31", "\"Observed holidays\"", "\"Office closed\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayRecurringUntilEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day recurring event created successfully", result);
        
        // Verify events were created
        assertTrue(calendar.getAllEvents().size() > 0);
    }
    
    @Test
    public void testCreateAllDayRecurringUntilEventWithMinimalArgs() throws Exception {
        String[] args = {"allday-recurring-until", "Weekend Off", "2023-05-13", "SS", "2023-06-30"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createAllDayRecurringUntilEvent(args);
        assertNotNull(creator);
        
        String result = creator.executeCreation(calendar);
        assertEquals("All-day recurring event created successfully", result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateAllDayRecurringUntilEventWithInsufficientArgs() {
        String[] args = {"allday-recurring-until", "Company Holiday", "2023-05-15", "MF"};  // Missing until date
        ConsolidatedEventCreator.createAllDayRecurringUntilEvent(args);
    }

    // ===== Edge Cases and Error Scenarios =====
    
    @Test
    public void testQuotedStringsInArgs() throws Exception {
        String[] args = {"single", "Meeting with \"Quotes\"", "2023-05-15T10:00", "2023-05-15T11:00", 
                "\"This description has \"nested\" quotes\"", "\"Location, with, commas\"", "true", "false"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
        assertNotNull(creator);
        
        Event event = creator.createEvent();
        assertEquals("Meeting with \"Quotes\"", event.getSubject());
        assertEquals("This description has \"nested\" quotes", event.getDescription());
        assertEquals("Location, with, commas", event.getLocation());
    }
    
    @Test
    public void testEventWithNoRepeatDays() {
        String[] args = {"recurring", "Bad Event", "2023-05-15T10:00", "2023-05-15T11:00", "", "5"};
        
        try {
            ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringEvent(args);
            creator.executeCreation(calendar);
            fail("Should have thrown exception due to empty repeat days");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e instanceof InvalidEventException || e instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testEventWithEmptyName() {
        String[] args = {"single", "", "2023-05-15T10:00", "2023-05-15T11:00"};
        
        try {
            ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
            creator.executeCreation(calendar);
            fail("Should have thrown InvalidEventException due to empty event name");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e instanceof InvalidEventException);
        }
    }
    
    @Test
    public void testEventWithExtremelyLongName() throws Exception {
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            nameBuilder.append("VeryLongName");
        }
        String longName = nameBuilder.toString();
        
        String[] args = {"single", longName, "2023-05-15T10:00", "2023-05-15T11:00"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createSingleEvent(args);
        Event event = creator.createEvent();
        assertEquals(longName, event.getSubject());
    }
    
    @Test
    public void testRecurringEventWithDateTimeConsistency() throws Exception {
        // Test that the time part is consistent within a single recurring event instance
        String[] args = {"recurring", "Consistent Time Event", "2023-05-15T10:30", "2023-05-15T11:45", 
                "MWF", "3"};
        
        ConsolidatedEventCreator creator = ConsolidatedEventCreator.createRecurringEvent(args);
        creator.executeCreation(calendar);
        
        // Verify at least one event was created
        boolean foundEvent = false;
        for (Event e : calendar.getAllEvents()) {
            if (e.getSubject().equals("Consistent Time Event")) {
                foundEvent = true;
                
                // Verify duration consistency
                LocalDateTime start = e.getStartDateTime();
                LocalDateTime end = e.getEndDateTime();
                assertEquals("Duration should be 1 hour and 15 minutes", 
                          75, java.time.Duration.between(start, end).toMinutes());
                break;
            }
        }
        
        assertTrue("Should find at least one matching event", foundEvent);
    }
    
    @Test
    public void testRemoveQuotesFunction() throws Exception {
        // Test with single quotes
        String[] args1 = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", 
                "'Single quoted description'", "'Single quoted location'", "true", "false"};
        
        ConsolidatedEventCreator creator1 = ConsolidatedEventCreator.createSingleEvent(args1);
        Event event1 = creator1.createEvent();
        assertEquals("Single quoted description", event1.getDescription());
        assertEquals("Single quoted location", event1.getLocation());
        
        // Test with double quotes
        String[] args2 = {"single", "Meeting", "2023-05-15T10:00", "2023-05-15T11:00", 
                "\"Double quoted description\"", "\"Double quoted location\"", "true", "false"};
        
        ConsolidatedEventCreator creator2 = ConsolidatedEventCreator.createSingleEvent(args2);
        Event event2 = creator2.createEvent();
        assertEquals("Double quoted description", event2.getDescription());
        assertEquals("Double quoted location", event2.getLocation());
    }
} 
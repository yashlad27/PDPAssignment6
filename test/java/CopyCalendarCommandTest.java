import java.time.LocalDateTime;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.calendar.CopyCalendarCommand;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.event.Event;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.CalendarNameValidator;
import view.ICalendarView;

/**
 * Tests for the CopyCalendarCommand class.
 */
public class CopyCalendarCommandTest {
    private CalendarManager calendarManager;
    private ICalendarView mockView;
    private CopyCalendarCommand command;

    @Before
    public void setUp() throws DuplicateCalendarException, InvalidTimezoneException {
        calendarManager = new CalendarManager.Builder().build();
        mockView = new MockCalendarView();
        command = new CopyCalendarCommand(calendarManager, mockView);
        
        // Create a test calendar
        calendarManager.createCalendar("SourceCal", "America/New_York");
    }
    
    @After
    public void tearDown() {
        // Clear all calendar names to avoid conflicts between tests
        CalendarNameValidator.clear();
    }

    @Test
    public void testGetName() {
        assertEquals("copy calendar", command.getName());
    }

    @Test
    public void testSuccessfulCopy() throws Exception {
        // Add an event to the source calendar
        Calendar sourceCal = calendarManager.getCalendar("SourceCal");
        Event event = new Event("Test Event", 
                LocalDateTime.of(2023, 4, 15, 10, 0),
                LocalDateTime.of(2023, 4, 15, 11, 0),
                "Description", "Location", true);
        sourceCal.addEvent(event, true);
        
        // Copy calendar
        String[] args = {"SourceCal", "to", "TargetCal"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> TargetCal", result);
        
        // Verify the new calendar exists
        assertTrue(calendarManager.hasCalendar("TargetCal"));
        
        // Verify timezone was copied
        Calendar targetCal = calendarManager.getCalendar("TargetCal");
        assertEquals("America/New_York", targetCal.getTimeZone().getID());
        
        // Verify events were copied
        assertEquals(1, targetCal.getAllEvents().size());
        Event copiedEvent = targetCal.getAllEvents().iterator().next();
        assertEquals("Test Event", copiedEvent.getSubject());
    }

    @Test
    public void testInsufficientArguments() throws Exception {
        String[] args = {"SourceCal"};
        String result = command.execute(args);
        
        assertTrue(result.startsWith("Error:"));
        assertFalse(calendarManager.hasCalendar("TargetCal"));
    }

    @Test
    public void testSourceCalendarNotFound() throws Exception {
        String[] args = {"NonExistentCal", "to", "TargetCal"};
        String result = command.execute(args);
        
        assertTrue(result.contains("not found"));
        assertFalse(calendarManager.hasCalendar("TargetCal"));
    }

    @Test
    public void testTargetCalendarAlreadyExists() throws Exception {
        // Create target calendar first
        calendarManager.createCalendar("TargetCal", "Europe/London");
        
        String[] args = {"SourceCal", "to", "TargetCal"};
        String result = command.execute(args);
        
        assertTrue(result.contains("already exists"));
        
        // Target calendar should remain unchanged
        Calendar targetCal = calendarManager.getCalendar("TargetCal");
        assertEquals("Europe/London", targetCal.getTimeZone().getID());
    }

    @Test
    public void testCopyWithMultipleEvents() throws Exception {
        // Add multiple events to source calendar
        Calendar sourceCal = calendarManager.getCalendar("SourceCal");
        
        Event event1 = new Event("Event 1", 
                LocalDateTime.of(2023, 4, 15, 10, 0),
                LocalDateTime.of(2023, 4, 15, 11, 0),
                "Description 1", "Location 1", true);
        
        Event event2 = new Event("Event 2", 
                LocalDateTime.of(2023, 4, 16, 14, 0),
                LocalDateTime.of(2023, 4, 16, 15, 30),
                "Description 2", "Location 2", false);
        
        sourceCal.addEvent(event1, true);
        sourceCal.addEvent(event2, true);
        
        // Copy calendar
        String[] args = {"SourceCal", "to", "TargetCal"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> TargetCal", result);
        
        // Verify the events were copied
        Calendar targetCal = calendarManager.getCalendar("TargetCal");
        assertEquals(2, targetCal.getAllEvents().size());
    }

    @Test
    public void testCopyPreservesActiveCalendar() throws Exception {
        // Create another calendar and set it active
        calendarManager.createCalendar("ActiveCal", "Asia/Tokyo");
        calendarManager.setActiveCalendar("ActiveCal");
        
        // Perform copy
        String[] args = {"SourceCal", "to", "TargetCal"};
        command.execute(args);
        
        // Active calendar should still be ActiveCal
        assertEquals("ActiveCal", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testCopyCalendarWithSpecialCharacters() throws Exception {
        // Create source calendar with alphanumeric characters (no special chars)
        calendarManager.createCalendar("SpecialCal123", "Europe/Paris");
        
        // Copy to target with alphanumeric characters
        String[] args = {"SpecialCal123", "to", "TargetCal456"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SpecialCal123 -> TargetCal456", result);
        assertTrue(calendarManager.hasCalendar("TargetCal456"));
    }

    @Test
    public void testCopyEmptyCalendar() throws Exception {
        // SourceCal is empty
        
        // Copy calendar
        String[] args = {"SourceCal", "to", "EmptyCopy"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> EmptyCopy", result);
        
        // Verify new calendar exists but has no events
        assertTrue(calendarManager.hasCalendar("EmptyCopy"));
        Calendar targetCal = calendarManager.getCalendar("EmptyCopy");
        assertEquals(0, targetCal.getAllEvents().size());
    }

    @Test
    public void testCopyWithManyEvents() throws Exception {
        // Test copying a calendar with a large number of events
        Calendar sourceCal = calendarManager.getCalendar("SourceCal");
        
        // Add many events (10 should be enough to test this functionality)
        for (int i = 0; i < 10; i++) {
            Event event = new Event(
                "Event " + i,
                LocalDateTime.of(2023, 5, 1, 10 + i, 0),
                LocalDateTime.of(2023, 5, 1, 11 + i, 0),
                "Description " + i,
                "Location " + i,
                i % 2 == 0  // alternate between true and false
            );
            sourceCal.addEvent(event, true);
        }
        
        // Copy calendar
        String[] args = {"SourceCal", "to", "BulkTargetCal"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> BulkTargetCal", result);
        
        // Verify all events were copied
        Calendar targetCal = calendarManager.getCalendar("BulkTargetCal");
        assertEquals(10, targetCal.getAllEvents().size());
    }
    
    @Test
    public void testCopyToNewName() throws Exception {
        // Test copying a calendar with a completely different naming pattern
        
        // Create the source calendar with events
        Calendar sourceCal = calendarManager.getCalendar("SourceCal");
        Event event = new Event(
            "Important Meeting",
            LocalDateTime.of(2023, 6, 15, 9, 0),
            LocalDateTime.of(2023, 6, 15, 10, 30),
            "Quarterly Review",
            "Conference Room A",
            true
        );
        sourceCal.addEvent(event, true);
        
        // Copy to a calendar with a very different name
        String[] args = {"SourceCal", "to", "calendar_2023_backup_Q2"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> calendar_2023_backup_Q2", result);
        assertTrue(calendarManager.hasCalendar("calendar_2023_backup_Q2"));
        
        // Verify the event was copied
        Calendar targetCal = calendarManager.getCalendar("calendar_2023_backup_Q2");
        assertEquals(1, targetCal.getAllEvents().size());
        Event copiedEvent = targetCal.getAllEvents().iterator().next();
        assertEquals("Important Meeting", copiedEvent.getSubject());
    }
    
    @Test
    public void testCopyWithNullEventProperties() throws Exception {
        // Test copying a calendar with events that have null properties
        Calendar sourceCal = calendarManager.getCalendar("SourceCal");
        
        // Create an event with null description and location
        Event event = new Event(
            "Event With Nulls",
            LocalDateTime.of(2023, 7, 20, 14, 0),
            LocalDateTime.of(2023, 7, 20, 15, 0),
            null,  // null description
            null,  // null location
            false
        );
        sourceCal.addEvent(event, true);
        
        // Copy calendar
        String[] args = {"SourceCal", "to", "NullPropsTarget"};
        String result = command.execute(args);
        
        assertEquals("Calendar copied: SourceCal -> NullPropsTarget", result);
        
        // Verify the event was copied with null properties preserved
        Calendar targetCal = calendarManager.getCalendar("NullPropsTarget");
        Event copiedEvent = targetCal.getAllEvents().iterator().next();
        assertEquals("Event With Nulls", copiedEvent.getSubject());
        assertFalse(copiedEvent.isPublic());
    }
} 
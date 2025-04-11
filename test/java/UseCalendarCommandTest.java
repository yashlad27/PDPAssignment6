import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.calendar.UseCalendarCommand;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.exceptions.DuplicateCalendarException;
import model.exceptions.InvalidTimezoneException;
import utilities.CalendarNameValidator;
import view.ICalendarView;

/**
 * Tests for the UseCalendarCommand class.
 */
public class UseCalendarCommandTest {
    private CalendarManager calendarManager;
    private ICalendarView mockView;
    private UseCalendarCommand command;

    @Before
    public void setUp() throws DuplicateCalendarException, InvalidTimezoneException {
        calendarManager = new CalendarManager.Builder().build();
        mockView = new MockCalendarView();
        command = new UseCalendarCommand(calendarManager, mockView);
        
        // Create test calendars
        calendarManager.createCalendar("Cal1", "America/New_York");
        calendarManager.createCalendar("Cal2", "Europe/London");
    }
    
    @After
    public void tearDown() {
        // Clear all calendar names to avoid conflicts between tests
        CalendarNameValidator.clear();
    }

    @Test
    public void testGetName() {
        assertEquals("use", command.getName());
    }

    @Test
    public void testSuccessfullySetActiveCalendar() throws Exception {
        // By default, Cal1 should be active
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Change to Cal2
        String[] args = {"use", "--name", "Cal2"};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: Cal2", result);
        assertEquals("Cal2", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testSetSameActiveCalendar() throws Exception {
        // By default, Cal1 should be active
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Try to set Cal1 again
        String[] args = {"use", "--name", "Cal1"};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: Cal1", result);
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testInsufficientArguments() throws Exception {
        String[] args = {"use"};
        String result = command.execute(args);
        
        assertTrue(result.startsWith("Error:"));
        // Active calendar should not change
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testMissingNameFlag() throws Exception {
        String[] args = {"use", "Cal2"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        // Active calendar should not change
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testNonExistentCalendar() throws Exception {
        String[] args = {"use", "--name", "NonExistentCal"};
        String result = command.execute(args);
        
        assertTrue(result.contains("not found"));
        // Active calendar should not change
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testSwitchBetweenCalendars() throws Exception {
        // Create a third calendar
        calendarManager.createCalendar("Cal3", "Asia/Tokyo");
        
        // Switch to Cal2
        String[] args1 = {"use", "--name", "Cal2"};
        command.execute(args1);
        assertEquals("Cal2", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Switch to Cal3
        String[] args2 = {"use", "--name", "Cal3"};
        command.execute(args2);
        assertEquals("Cal3", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Switch back to Cal1
        String[] args3 = {"use", "--name", "Cal1"};
        command.execute(args3);
        assertEquals("Cal1", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testCalendarWithSpecialCharacters() throws Exception {
        // Create a calendar with valid characters
        calendarManager.createCalendar("SpecialCal123", "Europe/London");
        
        // Try to set it as active
        String[] args = {"use", "--name", "SpecialCal123"};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: SpecialCal123", result);
        assertEquals("SpecialCal123", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testCaseSensitivity() throws Exception {
        // Create a calendar with uppercase letters
        calendarManager.createCalendar("UPPERCASE", "America/Chicago");
        
        // Try to set it as active
        String[] args = {"use", "--name", "UPPERCASE"};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: UPPERCASE", result);
        assertEquals("UPPERCASE", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Try with wrong case
        String[] args2 = {"use", "--name", "uppercase"};
        String result2 = command.execute(args2);
        
        assertTrue(result2.contains("not found"));
        assertEquals("UPPERCASE", calendarManager.getCalendarRegistry().getActiveCalendarName());
    }

    @Test
    public void testUseNewlyCreatedCalendar() throws Exception {
        // Test using a newly created calendar
        String calName = "BrandNewCal";
        
        // Create the calendar
        calendarManager.createCalendar(calName, "Asia/Singapore");
        
        // Try to use it immediately
        String[] args = {"use", "--name", calName};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: " + calName, result);
        assertEquals(calName, calendarManager.getCalendarRegistry().getActiveCalendarName());
    }
    
    @Test
    public void testUseAfterEdit() throws Exception {
        // Test using a calendar after it's been edited
        
        // Edit Cal2's timezone
        calendarManager.editCalendarTimezone("Cal2", "Asia/Dubai");
        
        // Use Cal2
        String[] args = {"use", "--name", "Cal2"};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: Cal2", result);
        assertEquals("Cal2", calendarManager.getCalendarRegistry().getActiveCalendarName());
        
        // Verify timezone was maintained
        Calendar cal = calendarManager.getCalendar("Cal2");
        assertEquals("Asia/Dubai", cal.getTimeZone().getID());
    }
    
    @Test
    public void testUseRapidSwitching() throws Exception {
        // Test rapidly switching between calendars multiple times
        String[] calNames = {"Cal1", "Cal2"};
        
        for (int i = 0; i < 5; i++) {
            String name = calNames[i % 2];
            String[] args = {"use", "--name", name};
            String result = command.execute(args);
            
            assertEquals("Now using calendar: " + name, result);
            assertEquals(name, calendarManager.getCalendarRegistry().getActiveCalendarName());
        }
    }
    
    @Test
    public void testUseWithUnderscoreInName() throws Exception {
        // Test using a calendar with underscore in name
        String calName = "Calendar_With_Underscores";
        
        // Create the calendar
        calendarManager.createCalendar(calName, "UTC");
        
        // Try to use it
        String[] args = {"use", "--name", calName};
        String result = command.execute(args);
        
        assertEquals("Now using calendar: " + calName, result);
        assertEquals(calName, calendarManager.getCalendarRegistry().getActiveCalendarName());
    }
} 
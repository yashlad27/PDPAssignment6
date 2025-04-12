import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import controller.command.calendar.EditCalendarCommand;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import model.exceptions.CalendarExceptions.CalendarNotFoundException;
import model.exceptions.CalendarExceptions.DuplicateCalendarException;
import model.exceptions.CalendarExceptions.InvalidTimezoneException;
import utilities.CalendarNameValidator;
import view.ICalendarView;

/**
 * Tests for the EditCalendarCommand class.
 */
public class EditCalendarCommandTest {
    private CalendarManager calendarManager;
    private ICalendarView mockView;
    private EditCalendarCommand command;

    @Before
    public void setUp() throws DuplicateCalendarException, InvalidTimezoneException {
        calendarManager = new CalendarManager.Builder().build();
        mockView = new MockCalendarView();
        command = new EditCalendarCommand(calendarManager, mockView);
        
        // Create a test calendar
        calendarManager.createCalendar("TestCal", "America/New_York");
    }
    
    @After
    public void tearDown() {
        // Clear all calendar names to avoid conflicts between tests
        CalendarNameValidator.clear();
    }

    @Test
    public void testGetName() {
        assertEquals("edit calendar", command.getName());
    }

    @Test
    public void testSuccessfulTimezoneEdit() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property", "timezone", "Europe/London"};
        String result = command.execute(args);
        
        assertEquals("Calendar timezone updated: TestCal", result);
        
        // Verify the timezone was updated
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("Europe/London", calendar.getTimeZone().getID());
    }

    @Test
    public void testSuccessfulNameEdit() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property", "name", "NewTestCalName"};
        String result = command.execute(args);
        
        assertEquals("Calendar name updated from 'TestCal' to 'NewTestCalName'", result);
        
        // Verify the calendar name was updated
        try {
            // Old name should not exist
            calendarManager.getCalendar("TestCal");
            fail("Should have thrown CalendarNotFoundException");
        } catch (CalendarNotFoundException e) {
            // Expected
        }
        
        // New name should exist
        Calendar calendar = calendarManager.getCalendar("NewTestCalName");
        assertNotNull(calendar);
        assertEquals("NewTestCalName", calendar.getName());
    }

    @Test
    public void testInsufficientArguments() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property"};
        String result = command.execute(args);
        
        assertTrue(result.startsWith("Error:"));
        
        // Verify the calendar timezone wasn't changed
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testMissingNameFlag() throws Exception {
        String[] args = {"edit", "TestCal", "--property", "timezone", "Europe/London"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        
        // Verify the calendar timezone wasn't changed
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testMissingPropertyFlag() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "timezone", "Europe/London"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        
        // Verify the calendar timezone wasn't changed
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testNonExistentCalendar() throws Exception {
        String[] args = {"edit", "--name", "NonExistentCal", "--property", "timezone", "Europe/London"};
        String result = command.execute(args);
        
        assertTrue(result.contains("not found"));
    }

    @Test
    public void testInvalidTimezone() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property", "timezone", "InvalidTZ"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Invalid timezone"));
        
        // Verify the calendar timezone wasn't changed
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testUnsupportedProperty() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property", "unsupported", "value"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Unsupported property"));
        
        // Verify the calendar timezone wasn't changed
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testSameTimezone() throws Exception {
        String[] args = {"edit", "--name", "TestCal", "--property", "timezone", "America/New_York"};
        String result = command.execute(args);
        
        assertEquals("Calendar timezone updated: TestCal", result);
        
        // Verify the calendar timezone is still the same
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testEditMultipleCalendars() throws Exception {
        // Create a second calendar
        calendarManager.createCalendar("TestCal2", "Europe/Paris");
        
        // Edit first calendar
        String[] args1 = {"edit", "--name", "TestCal", "--property", "timezone", "Asia/Tokyo"};
        command.execute(args1);
        
        // Edit second calendar
        String[] args2 = {"edit", "--name", "TestCal2", "--property", "timezone", "Australia/Sydney"};
        command.execute(args2);
        
        // Verify both calendars have updated timezones
        Calendar calendar1 = calendarManager.getCalendar("TestCal");
        Calendar calendar2 = calendarManager.getCalendar("TestCal2");
        
        assertEquals("Asia/Tokyo", calendar1.getTimeZone().getID());
        assertEquals("Australia/Sydney", calendar2.getTimeZone().getID());
    }

    @Test
    public void testEditBetweenExtremeTimezones() throws Exception {
        // Test editing between extreme timezones
        
        // First set to UTC-11
        String[] args1 = {"edit", "--name", "TestCal", "--property", "timezone", "Pacific/Pago_Pago"};
        String result1 = command.execute(args1);
        assertEquals("Calendar timezone updated: TestCal", result1);
        
        Calendar calendar1 = calendarManager.getCalendar("TestCal");
        assertEquals("Pacific/Pago_Pago", calendar1.getTimeZone().getID());
        
        // Then set to UTC+14
        String[] args2 = {"edit", "--name", "TestCal", "--property", "timezone", "Pacific/Kiritimati"};
        String result2 = command.execute(args2);
        assertEquals("Calendar timezone updated: TestCal", result2);
        
        Calendar calendar2 = calendarManager.getCalendar("TestCal");
        assertEquals("Pacific/Kiritimati", calendar2.getTimeZone().getID());
    }
    
    @Test
    public void testRapidConsecutiveEdits() throws Exception {
        // Test rapidly editing the same calendar multiple times
        String[] timezones = {
            "America/New_York", 
            "Europe/London", 
            "Asia/Tokyo", 
            "Australia/Sydney", 
            "Pacific/Auckland"
        };
        
        for (String timezone : timezones) {
            String[] args = {"edit", "--name", "TestCal", "--property", "timezone", timezone};
            String result = command.execute(args);
            assertEquals("Calendar timezone updated: TestCal", result);
            
            Calendar calendar = calendarManager.getCalendar("TestCal");
            assertEquals(timezone, calendar.getTimeZone().getID());
        }
    }
    
    @Test
    public void testEditActiveCalendar() throws Exception {
        // Test editing the active calendar
        
        // Make TestCal the active calendar
        calendarManager.setActiveCalendar("TestCal");
        
        // Edit its timezone
        String[] args = {"edit", "--name", "TestCal", "--property", "timezone", "Asia/Tokyo"};
        String result = command.execute(args);
        
        assertEquals("Calendar timezone updated: TestCal", result);
        
        // Verify the active calendar was updated
        Calendar activeCalendar = calendarManager.getCalendar(
            calendarManager.getCalendarRegistry().getActiveCalendarName());
        assertEquals("Asia/Tokyo", activeCalendar.getTimeZone().getID());
    }
} 
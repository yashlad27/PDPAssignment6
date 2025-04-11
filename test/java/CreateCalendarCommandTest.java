import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.calendar.CreateCalendarCommand;
import model.calendar.Calendar;
import model.calendar.CalendarManager;
import utilities.CalendarNameValidator;
import view.ICalendarView;

/**
 * Tests for the CreateCalendarCommand class.
 */
public class CreateCalendarCommandTest {
    private CalendarManager calendarManager;
    private ICalendarView mockView;
    private CreateCalendarCommand command;

    @Before
    public void setUp() {
        calendarManager = new CalendarManager.Builder().build();
        mockView = new MockCalendarView();
        command = new CreateCalendarCommand(calendarManager, mockView);
    }
    
    @After
    public void tearDown() {
        // Clear all calendar names to avoid conflicts between tests
        CalendarNameValidator.clear();
    }

    @Test
    public void testGetName() {
        assertEquals("create calendar", command.getName());
    }

    @Test
    public void testSuccessfulCalendarCreation() throws Exception {
        String[] args = {"create", "--name", "TestCal", "--timezone", "America/New_York"};
        String result = command.execute(args);
        
        assertEquals("Calendar created: TestCal", result);
        assertTrue(calendarManager.hasCalendar("TestCal"));
        
        // Verify the calendar was created with the correct timezone
        Calendar calendar = calendarManager.getCalendar("TestCal");
        assertEquals("America/New_York", calendar.getTimeZone().getID());
    }

    @Test
    public void testInsufficientArguments() throws Exception {
        String[] args = {"create", "--name", "TestCal"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        assertFalse(calendarManager.hasCalendar("TestCal"));
    }

    @Test
    public void testMissingNameFlag() throws Exception {
        String[] args = {"create", "TestCal", "--timezone", "America/New_York"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        assertFalse(calendarManager.hasCalendar("TestCal"));
    }

    @Test
    public void testMissingTimezoneFlag() throws Exception {
        String[] args = {"create", "--name", "TestCal", "America/New_York"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Error"));
        assertFalse(calendarManager.hasCalendar("TestCal"));
    }

    @Test
    public void testInvalidTimezone() throws Exception {
        String[] args = {"create", "--name", "TestCal", "--timezone", "InvalidTZ"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Invalid timezone"));
        assertFalse(calendarManager.hasCalendar("TestCal"));
    }

    @Test
    public void testCreateCalendarWithSpecialCharacters() throws Exception {
        String[] args = {"create", "--name", "TestCal123", "--timezone", "Europe/London"};
        String result = command.execute(args);
        
        assertEquals("Calendar created: TestCal123", result);
        assertTrue(calendarManager.hasCalendar("TestCal123"));
    }

    @Test
    public void testCreateWithDifferentTimezones() throws Exception {
        // Create calendars with different timezones
        String[] args1 = {"create", "--name", "Cal1", "--timezone", "Asia/Tokyo"};
        String[] args2 = {"create", "--name", "Cal2", "--timezone", "Europe/Paris"};
        
        command.execute(args1);
        command.execute(args2);
        
        Calendar cal1 = calendarManager.getCalendar("Cal1");
        Calendar cal2 = calendarManager.getCalendar("Cal2");
        
        assertEquals("Asia/Tokyo", cal1.getTimeZone().getID());
        assertEquals("Europe/Paris", cal2.getTimeZone().getID());
    }

    @Test
    public void testCreateWithLongName() throws Exception {
        // Test creating a calendar with a very long name
        String longName = "ThisIsAReallyLongCalendarNameToTestBoundaries";
        String[] args = {"create", "--name", longName, "--timezone", "America/New_York"};
        String result = command.execute(args);
        
        assertEquals("Calendar created: " + longName, result);
        assertTrue(calendarManager.hasCalendar(longName));
    }
    
    @Test
    public void testCreateWithExtremeTzs() throws Exception {
        // Test creating calendars with extreme timezones
        String[] westArgs = {"create", "--name", "WestCal", "--timezone", "Pacific/Pago_Pago"}; // UTC-11
        String[] eastArgs = {"create", "--name", "EastCal", "--timezone", "Pacific/Kiritimati"}; // UTC+14
        
        String westResult = command.execute(westArgs);
        String eastResult = command.execute(eastArgs);
        
        assertEquals("Calendar created: WestCal", westResult);
        assertEquals("Calendar created: EastCal", eastResult);
        
        Calendar westCal = calendarManager.getCalendar("WestCal");
        Calendar eastCal = calendarManager.getCalendar("EastCal");
        
        assertEquals("Pacific/Pago_Pago", westCal.getTimeZone().getID());
        assertEquals("Pacific/Kiritimati", eastCal.getTimeZone().getID());
    }
    
    @Test
    public void testCreateMultipleSequentially() throws Exception {
        // Test creating multiple calendars one after another
        for (int i = 1; i <= 5; i++) {
            String name = "SeqCal" + i;
            String[] args = {"create", "--name", name, "--timezone", "UTC"};
            String result = command.execute(args);
            
            assertEquals("Calendar created: " + name, result);
            assertTrue(calendarManager.hasCalendar(name));
        }
        
        // Verify all calendars were created
        for (int i = 1; i <= 5; i++) {
            assertTrue(calendarManager.hasCalendar("SeqCal" + i));
        }
    }
} 
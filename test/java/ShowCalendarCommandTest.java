import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.calendar.ShowCalendarCommand;
import model.calendar.CalendarManager;
import model.calendar.CalendarRegistry;
import utilities.CalendarNameValidator;
import view.ICalendarView;

/**
 * Tests for the ShowCalendarCommand class.
 */
public class ShowCalendarCommandTest {
    private CalendarManager calendarManager;
    private ICalendarView mockView;
    private ShowCalendarCommand command;

    @Before
    public void setUp() {
        calendarManager = new CalendarManager.Builder().build();
        mockView = new MockCalendarView();
        command = new ShowCalendarCommand(calendarManager, mockView);
    }
    
    @After
    public void tearDown() {
        // Clear all calendar names to avoid conflicts between tests
        CalendarNameValidator.clear();
    }

    @Test
    public void testGetName() {
        assertEquals("show calendar", command.getName());
    }

    @Test
    public void testShowWithNoCalendars() throws Exception {
        String[] args = {};
        String result = command.execute(args);
        
        assertEquals("No active calendar", result);
    }

    @Test
    public void testShowWithOneCalendar() throws Exception {
        // Create a calendar
        calendarManager.createCalendar("TestCal", "America/New_York");
        
        String[] args = {};
        String result = command.execute(args);
        
        assertTrue(result.contains("Active calendar: TestCal"));
        assertTrue(result.contains("TestCal (active)"));
    }

    @Test
    public void testShowWithMultipleCalendars() throws Exception {
        // Create multiple calendars
        calendarManager.createCalendar("Cal1", "America/New_York");
        calendarManager.createCalendar("Cal2", "Europe/London");
        calendarManager.createCalendar("Cal3", "Asia/Tokyo");
        
        // Cal1 should be active by default (first created)
        
        String[] args = {};
        String result = command.execute(args);
        
        // Check for active calendar indication
        assertTrue(result.contains("Active calendar: Cal1"));
        
        // Check that all calendars are listed
        assertTrue(result.contains("Cal1 (active)"));
        assertTrue(result.contains("Cal2"));
        assertTrue(result.contains("Cal3"));
    }

    @Test
    public void testShowAfterChangingActiveCalendar() throws Exception {
        // Create multiple calendars
        calendarManager.createCalendar("Cal1", "America/New_York");
        calendarManager.createCalendar("Cal2", "Europe/London");
        
        // Change active calendar
        calendarManager.setActiveCalendar("Cal2");
        
        String[] args = {};
        String result = command.execute(args);
        
        // Check for active calendar indication
        assertTrue(result.contains("Active calendar: Cal2"));
        assertTrue(result.contains("Cal2 (active)"));
        assertTrue(result.contains("Cal1"));
        assertFalse(result.contains("Cal1 (active)"));
    }

    @Test
    public void testShowAfterRemovingActiveCalendar() throws Exception {
        // Create multiple calendars
        calendarManager.createCalendar("Cal1", "America/New_York");
        calendarManager.createCalendar("Cal2", "Europe/London");
        
        // Remove active calendar (Cal1)
        CalendarRegistry registry = calendarManager.getCalendarRegistry();
        registry.removeCalendar("Cal1");
        
        String[] args = {};
        String result = command.execute(args);
        
        // Cal2 should be made active automatically
        assertTrue(result.contains("Active calendar: Cal2"));
        assertTrue(result.contains("Cal2 (active)"));
        assertFalse(result.contains("Cal1"));
    }

    @Test
    public void testShowWithMultiplePages() throws Exception {
        // Create many calendars to test formatting with a large number of calendars
        for (int i = 1; i <= 10; i++) {
            calendarManager.createCalendar("Calendar" + i, "America/New_York");
        }
        
        String[] args = {};
        String result = command.execute(args);
        
        // Check that the first calendar is active
        assertTrue(result.contains("Active calendar: Calendar1"));
        assertTrue(result.contains("Calendar1 (active)"));
        
        // Check that all calendars are listed
        for (int i = 2; i <= 10; i++) {
            assertTrue(result.contains("Calendar" + i));
        }
    }

    @Test
    public void testIgnoringExtraArguments() throws Exception {
        // Create a calendar
        calendarManager.createCalendar("TestCal", "America/New_York");
        
        // Command should ignore extra arguments
        String[] args = {"extra", "arguments", "ignored"};
        String result = command.execute(args);
        
        assertTrue(result.contains("Active calendar: TestCal"));
    }
} 
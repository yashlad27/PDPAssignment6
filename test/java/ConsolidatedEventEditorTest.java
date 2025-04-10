import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import controller.command.edit.strategy.ConsolidatedEventEditor;
import controller.command.edit.strategy.EventEditor;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.exceptions.ConflictingEventException;
import model.exceptions.EventNotFoundException;
import model.exceptions.InvalidEventException;
import model.export.IDataExporter;

/**
 * Test class for ConsolidatedEventEditor.
 * Tests the different editing strategies, constructor validation,
 * and property editing functionality.
 */
public class ConsolidatedEventEditorTest {

    private ICalendar mockCalendar;
    private Event testEvent;
    private RecurringEvent testRecurringEvent;
    private UUID testEventId;
    private UUID testRecurringEventId;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;
    private List<Event> allEvents;
    
    @Before
    public void setUp() {
        // Create test events
        testEventId = UUID.randomUUID();
        testRecurringEventId = UUID.randomUUID();
        testStartTime = LocalDateTime.now().plusDays(1);
        testEndTime = testStartTime.plusHours(2);
        
        testEvent = new Event(
            "Test Event",
            testStartTime,
            testEndTime,
            "Test Description",
            "Test Location",
            true
        ) {
            @Override
            public UUID getId() {
                return testEventId;
            }
        };
        
        // Create repeat days set for recurring events
        Set<DayOfWeek> repeatDays = new HashSet<>();
        repeatDays.add(DayOfWeek.MONDAY);
        repeatDays.add(DayOfWeek.WEDNESDAY);
        repeatDays.add(DayOfWeek.FRIDAY);
        
        // For our test purposes, create a recurring event using the Builder
        testRecurringEvent = new RecurringEvent.Builder(
            "Recurring Test",
            testStartTime,
            testEndTime,
            repeatDays)
            .description("Test Description")
            .location("Test Location")
            .isPublic(true)
            .occurrences(5)
            .recurringId(testRecurringEventId)
            .build();
        
        // Create mock calendar and list of events
        allEvents = new ArrayList<>();
        allEvents.add(testEvent);
        allEvents.add(testRecurringEvent);
        
        // Create a second occurrence of the recurring event with a different date
        RecurringEvent secondOccurrence = new RecurringEvent.Builder(
            "Recurring Test", 
            testStartTime.plusDays(2), // 2 days later
            testEndTime.plusDays(2),
            repeatDays)
            .description("Test Description")
            .location("Test Location")
            .isPublic(true)
            .occurrences(5)
            .recurringId(testRecurringEventId) // Same ID to simulate same series
            .build();
        allEvents.add(secondOccurrence);
        
        mockCalendar = createMockCalendar(allEvents);
    }
    
    // Factory method tests
    
    @Test
    public void testCreateSingleEventEditor() {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "subject", "New Subject");
            
        assertNotNull("Editor should not be null", editor);
        assertTrue("Editor should be instance of ConsolidatedEventEditor", 
            editor instanceof ConsolidatedEventEditor);
    }
    
    @Test
    public void testCreateAllEventsEditor() {
        EventEditor editor = ConsolidatedEventEditor.createAllEventsEditor(
            testRecurringEventId, "Recurring Test", "description", "Updated Description");
            
        assertNotNull("Editor should not be null", editor);
        assertTrue("Editor should be instance of ConsolidatedEventEditor", 
            editor instanceof ConsolidatedEventEditor);
    }
    
    @Test
    public void testCreateSeriesFromDateEditor() {
        LocalDate fromDate = LocalDate.now();
        EventEditor editor = ConsolidatedEventEditor.createSeriesFromDateEditor(
            testRecurringEventId, "Recurring Test", "location", "New Location", fromDate);
            
        assertNotNull("Editor should not be null", editor);
        assertTrue("Editor should be instance of ConsolidatedEventEditor", 
            editor instanceof ConsolidatedEventEditor);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSeriesFromDateEditor_NullDate() {
        // This should throw IllegalArgumentException
        ConsolidatedEventEditor.createSeriesFromDateEditor(
            testRecurringEventId, "Recurring Test", "location", "New Location", null);
    }
    
    // Direct constructor tests
    
    @Test
    public void testDirectConstructor() {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Test Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", "New Name");
        
        assertNotNull("Editor should not be null", editor);
    }
    
    // Execute edit tests - Single event
    
    @Test
    public void testExecuteEdit_SingleEvent_Subject() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "subject", "Updated Event Name");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event subject should be updated", 
            "Updated Event Name", testEvent.getSubject());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_Description() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "description", "Updated description text");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event description should be updated", 
            "Updated description text", testEvent.getDescription());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_Location() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "location", "New Meeting Room");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event location should be updated", 
            "New Meeting Room", testEvent.getLocation());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_StartTime() throws Exception {
        LocalDateTime newStartTime = testStartTime.minusHours(1);
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "start", newStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event start time should be updated", 
            newStartTime, testEvent.getStartDateTime());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_EndTime() throws Exception {
        LocalDateTime newEndTime = testEndTime.plusHours(1);
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "end", newEndTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event end time should be updated", 
            newEndTime, testEvent.getEndDateTime());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_Privacy() throws Exception {
        // Test changing from public to private
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "privacy", "private");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertFalse("Event should be private", testEvent.isPublic());
        
        // Test changing back to public
        editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "privacy", "public");
            
        result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertTrue("Event should be public", testEvent.isPublic());
    }
    
    @Test(expected = EventNotFoundException.class)
    public void testExecuteEdit_SingleEvent_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            nonExistentId, "Nonexistent Event", "subject", "New Name");
            
        editor.executeEdit(mockCalendar);
    }
    
    @Test(expected = InvalidEventException.class)
    public void testExecuteEdit_SingleEvent_InvalidProperty() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "invalid_property", "Some Value");
            
        editor.executeEdit(mockCalendar);
    }
    
    // Additional tests for property updates
    
    @Test
    public void testExecuteEdit_SingleEvent_EmptyDescription() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "description", "");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event description should be empty", 
            "", testEvent.getDescription());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_EmptyLocation() throws Exception {
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "location", "");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event location should be empty", 
            "", testEvent.getLocation());
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_TitleAlias() throws Exception {
        // Test that "title" is an alias for "subject"
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "title", "New Title");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event subject should be updated (title is an alias)", 
            "New Title", testEvent.getSubject());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testExecuteEdit_SingleEvent_EmptySubject() throws Exception {
        // Test that empty subject is rejected
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "subject", "");
            
        editor.executeEdit(mockCalendar);
        // Should throw IllegalArgumentException
    }
    
    @Test
    public void testExecuteEdit_SingleEvent_BooleanPrivacyValues() throws Exception {
        // Test with "true" for public
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "privacy", "true");
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertTrue("Event should be public with 'true' value", testEvent.isPublic());
        
        // Test with "false" for private
        editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "privacy", "false");
            
        result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertFalse("Event should be private with 'false' value", testEvent.isPublic());
    }
    
    // Date parsing tests
    
    @Test
    public void testExecuteEdit_DateFormat_ISOLocalDate() throws Exception {
        // Test with ISO Local Date format (just the date part)
        LocalDate newDate = testStartTime.toLocalDate().minusDays(3);
        String dateStr = newDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "start", dateStr);
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event start date should be updated", 
            newDate, testEvent.getStartDateTime().toLocalDate());
        assertEquals("Event start time should be midnight", 
            0, testEvent.getStartDateTime().getHour());
        assertEquals("Event start time should be midnight", 
            0, testEvent.getStartDateTime().getMinute());
    }
    
    @Test
    public void testExecuteEdit_DateFormat_CustomPattern() throws Exception {
        // Test with custom date-time pattern "yyyy-MM-dd HH:mm"
        LocalDateTime newDateTime = testStartTime.minusDays(2).minusHours(3);
        String dateTimeStr = newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "start", dateTimeStr);
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event start date should be updated", 
            newDateTime.toLocalDate(), testEvent.getStartDateTime().toLocalDate());
        assertEquals("Event start hour should be updated", 
            newDateTime.getHour(), testEvent.getStartDateTime().getHour());
        assertEquals("Event start minute should be updated", 
            newDateTime.getMinute(), testEvent.getStartDateTime().getMinute());
    }
    
    // Edge cases
    
    @Test(expected = IllegalArgumentException.class)
    public void testExecuteEdit_StartTimeAfterEndTime() throws Exception {
        // Set start time to be after end time - this should be rejected
        LocalDateTime newStartTime = testEndTime.plusHours(1);
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "start", newStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // This should throw IllegalArgumentException
        editor.executeEdit(mockCalendar);
    }
    
    @Test
    public void testExecuteEdit_LongSubject() throws Exception {
        // Test with very long subject (but not too long)
        StringBuilder longSubject = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longSubject.append("a");
        }
        
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "subject", longSubject.toString());
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Long subject should be updated correctly", 
            longSubject.toString(), testEvent.getSubject());
    }
    
    @Test
    public void testExecuteEdit_LongDescription() throws Exception {
        // Test with very long description
        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longDesc.append("b");
        }
        
        EventEditor editor = ConsolidatedEventEditor.createSingleEventEditor(
            testEventId, "Test Event", "description", longDesc.toString());
            
        String result = editor.executeEdit(mockCalendar);
        
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Long description should be updated correctly", 
            longDesc.toString(), testEvent.getDescription());
    }
    
    // Test direct editing by subject and start time
    
    @Test
    public void testEditEvent_BySubjectAndTime() throws Exception {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Test Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "description", "Updated via direct edit");
            
        String result = editor.editEvent("Test Event", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "description", "Updated via direct edit");
            
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event description should be updated", 
            "Updated via direct edit", testEvent.getDescription());
    }
    
    @Test
    public void testEditEvent_WithQuotedParameters() throws Exception {
        // Test with quoted parameters which should be stripped
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "\"Test Event\"", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "\"description\"", "\"Updated with quotes\"");
            
        String result = editor.editEvent("\"Test Event\"", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "\"description\"", "\"Updated with quotes\"");
            
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event description should be updated with quotes removed", 
            "Updated with quotes", testEvent.getDescription());
    }
    
    @Test
    public void testEditEvent_SingleQuotes() throws Exception {
        // Test with single quoted parameters which should be stripped
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "'Test Event'", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "'description'", "'Updated with single quotes'");
            
        String result = editor.editEvent("'Test Event'", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "'description'", "'Updated with single quotes'");
            
        assertTrue("Result should indicate success", result.contains("Event updated"));
        assertEquals("Event description should be updated with single quotes removed", 
            "Updated with single quotes", testEvent.getDescription());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testEditEvent_NullCalendar() throws Exception {
        // This should throw IllegalArgumentException
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            null, "Test Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", "New Name");
            
        editor.editEvent("Test Event", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", "New Name");
    }
    
    @Test(expected = InvalidEventException.class)
    public void testEditEvent_NullProperty() throws Exception {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Test Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            null, "New Name");
            
        editor.editEvent("Test Event", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            null, "New Name");
    }
    
    @Test(expected = InvalidEventException.class)
    public void testEditEvent_NullValue() throws Exception {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Test Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", null);
            
        editor.editEvent("Test Event", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", null);
    }
    
    @Test(expected = EventNotFoundException.class)
    public void testEditEvent_EventNotFound() throws Exception {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Nonexistent Event", testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", "New Name");
            
        editor.editEvent("Nonexistent Event", 
            testStartTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "subject", "New Name");
    }
    
    @Test(expected = InvalidEventException.class)
    public void testEditEvent_InvalidDateTimeFormat() throws Exception {
        ConsolidatedEventEditor editor = new ConsolidatedEventEditor(
            mockCalendar, "Test Event", "invalid-datetime-format",
            "subject", "New Name");
            
        editor.editEvent("Test Event", "invalid-datetime-format", "subject", "New Name");
    }
    
    // Helper methods for testing
    
    private ICalendar createMockCalendar(final List<Event> events) {
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
            public Event findEvent(String subject, LocalDateTime startDateTime) throws EventNotFoundException {
                for (Event event : events) {
                    if (event.getSubject().equals(subject) && 
                            event.getStartDateTime().equals(startDateTime)) {
                        return event;
                    }
                }
                throw new EventNotFoundException("Event not found: " + subject);
            }

            @Override
            public List<Event> getAllEvents() {
                return new ArrayList<>(events);
            }
            
            @Override
            public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
                // In our test mock, assume success always
                // The event is already in our collection for testing
                return true;
            }

            @Override
            public TimeZone getTimeZone() {
                return TimeZone.getDefault();
            }
            
            // Other required methods (not directly used in our tests)
            @Override
            public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
                return true;
            }

            @Override
            public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                    String weekdays, LocalDate untilDate, boolean autoDecline) {
                return true;
            }

            @Override
            public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                    int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
                return true;
            }

            @Override
            public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                    LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
                return true;
            }

            @Override
            public List<Event> getEventsOnDate(LocalDate date) {
                List<Event> result = new ArrayList<>();
                for (Event event : events) {
                    if (event.getStartDateTime().toLocalDate().equals(date) ||
                            event.getEndDateTime().toLocalDate().equals(date)) {
                        result.add(event);
                    }
                }
                return result;
            }

            @Override
            public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
                List<Event> result = new ArrayList<>();
                for (Event event : events) {
                    LocalDate eventStart = event.getStartDateTime().toLocalDate();
                    LocalDate eventEnd = event.getEndDateTime().toLocalDate();
                    // Event overlaps with range
                    if (!(eventEnd.isBefore(startDate) || eventStart.isAfter(endDate))) {
                        result.add(event);
                    }
                }
                return result;
            }

            @Override
            public boolean isBusy(LocalDateTime dateTime) {
                for (Event event : events) {
                    if (!dateTime.isBefore(event.getStartDateTime()) && 
                            !dateTime.isAfter(event.getEndDateTime())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
                    String newValue) throws EventNotFoundException, InvalidEventException {
                Event event = findEvent(subject, startDateTime);
                // Testing mock - we're not actually implementing this
                return true;
            }

            @Override
            public boolean updateEvent(UUID eventId, Event updatedEvent) {
                for (Event event : events) {
                    if (event.getId().equals(eventId)) {
                        // In a real implementation, we would replace the event
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
                    String newValue) {
                return 1; // Mock return
            }

            @Override
            public int editAllEvents(String subject, String property, String newValue) {
                return 2; // Mock return
            }

            @Override
            public List<RecurringEvent> getAllRecurringEvents() {
                List<RecurringEvent> result = new ArrayList<>();
                for (Event event : events) {
                    if (event instanceof RecurringEvent) {
                        result.add((RecurringEvent) event);
                    }
                }
                return result;
            }

            @Override
            public String exportData(String filePath, IDataExporter exporter) throws IOException {
                return "test-export-path";
            }
        };
    }
} 
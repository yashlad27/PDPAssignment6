import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.command.event.ImportCalendarCommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import view.GUICalendarPanel;
import view.IGUIView;
import viewmodel.ExportImportViewModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for ImportCalendarCommand.
 * Tests importing calendar events from a CSV file.
 */
public class ImportCalendarCommandTest {

  private MockCalendar mockCalendar;
  private MockGUIView mockView;
  private ImportCalendarCommand command;
  private MockViewModel mockViewModel;
  private static final String TEST_FILE_PATH = "test_import.csv";
  private static final String TEST_CSV_CONTENT =
          "Subject,Start Date,Start Time,End Date,End Time,All Day,Description,Location,Public\n" +
                  "Team Meeting,2023-05-15,10:00,2023-05-15,11:00,false,Weekly sync,Conference Room A,true\n" +
                  "Holiday,2023-05-29,00:00,2023-05-29,23:59,true,Memorial Day,,true\n";

  @Before
  public void setUp() {
    mockCalendar = new MockCalendar();
    mockView = new MockGUIView();
    mockViewModel = new MockViewModel();
    command = new ImportCalendarCommand(mockCalendar);
    createTestCsvFile();
  }

  @After
  public void tearDown() {
    File testFile = new File(TEST_FILE_PATH);
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  private void createTestCsvFile() {
    try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
      writer.write(TEST_CSV_CONTENT);
    } catch (IOException e) {
      fail("Failed to create test CSV file: " + e.getMessage());
    }
  }

  @Test
  public void testConstructorWithCalendar() {
    ImportCalendarCommand cmd = new ImportCalendarCommand(mockCalendar);
    assertNotNull("Command should not be null", cmd);
    assertEquals("Command name should be 'import'", "import", cmd.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new ImportCalendarCommand(null);
  }

  @Test
  public void testConstructorWithViewModelAndView() {
    ImportCalendarCommand cmd = new ImportCalendarCommand(mockCalendar, mockViewModel, mockView);
    assertNotNull("Command should not be null", cmd);
    assertEquals("Command name should be 'import'", "import", cmd.getName());
  }

  @Test
  public void testGetName() {
    assertEquals("Command name should be 'import'", "import", command.getName());
  }

  @Test
  public void testExecuteWithMissingFilename() {
    String[] args = {};
    String result = command.execute(args);
    assertEquals("Error: Missing filename for import command", result);
  }

  @Test
  public void testExecuteWithNonExistentFile() {
    String[] args = {"non_existent_file.csv"};
    String result = command.execute(args);
    assertTrue(result.startsWith("Error: File not found:"));
  }

  @Test
  public void testExecuteWithValidFile() {
    String[] args = {TEST_FILE_PATH};
    String result = command.execute(args);
    assertTrue("Should indicate successful import", result.startsWith("Successfully imported"));
    assertTrue("Should have imported events", mockCalendar.getEvents().size() > 0);
  }

  @Test
  public void testImportFromFileDirectly() {
    File file = new File(TEST_FILE_PATH);
    String result = command.importFromFile(file);
    assertTrue("Should indicate successful import", result.startsWith("Successfully imported"));
    assertTrue("Should have imported events", mockCalendar.getEvents().size() > 0);
  }

  @Test
  public void testImportFromFileWithEmptyFile() {
    // Create an empty CSV file
    try (FileWriter writer = new FileWriter("empty.csv")) {
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day,Description,Location,Public\n");
    } catch (IOException e) {
      fail("Failed to create empty CSV file: " + e.getMessage());
    }

    File emptyFile = new File("empty.csv");
    String result = command.importFromFile(emptyFile);
    assertEquals("No events found in the CSV file", result);

    // Clean up
    emptyFile.delete();
  }

  @Test
  public void testImportFromFileUsingViewModel() {
    // Create command with view model
    ImportCalendarCommand cmdWithViewModel = new ImportCalendarCommand(
            mockCalendar, mockViewModel, mockView);

    mockViewModel.setImportResult(3); // Simulate 3 events imported

    File file = new File(TEST_FILE_PATH);
    String result = cmdWithViewModel.importFromFile(file);

    assertEquals("Successfully imported 3 events", result);
    assertTrue("View model should have been used for import", mockViewModel.wasImportCalled());
  }

  @Test
  public void testImportFromFileUsingViewModelWithNoEvents() {
    // Create command with view model
    ImportCalendarCommand cmdWithViewModel = new ImportCalendarCommand(
            mockCalendar, mockViewModel, mockView);

    mockViewModel.setImportResult(0); // Simulate no events imported

    File file = new File(TEST_FILE_PATH);
    String result = cmdWithViewModel.importFromFile(file);

    assertEquals("No events were imported", result);
    assertTrue("View model should have been used for import", mockViewModel.wasImportCalled());
  }

  @Test
  public void testImportWithConflictingEvents() {
    mockCalendar.setShouldThrowConflictException(true);

    File file = new File(TEST_FILE_PATH);
    String result = command.importFromFile(file);

    assertEquals("Failed to import any events", result);
  }

  @Test
  public void testImportWithViewUpdate() {
    // Create command with view but no view model
    ImportCalendarCommand cmdWithView = new ImportCalendarCommand(mockCalendar);

    // Set up a test field to access the view
    try {
      java.lang.reflect.Field viewField = ImportCalendarCommand.class.getDeclaredField("view");
      viewField.setAccessible(true);
      viewField.set(cmdWithView, mockView);
    } catch (Exception e) {
      fail("Failed to set view field: " + e.getMessage());
    }

    File file = new File(TEST_FILE_PATH);
    String result = cmdWithView.importFromFile(file);

    assertTrue("Should indicate successful import", result.startsWith("Successfully imported"));
    assertTrue("Calendar panel should have been updated", mockView.wasCalendarUpdated());
  }

  /**
   * Mock implementation of ICalendar for testing.
   */
  private static class MockCalendar implements ICalendar {
    private final List<Event> events = new ArrayList<>();
    private boolean shouldThrowIOException = false;
    private boolean shouldThrowConflictException = false;

    public List<Event> getEvents() {
      return events;
    }

    public void setShouldThrowIOException(boolean shouldThrow) {
      this.shouldThrowIOException = shouldThrow;
    }

    public void setShouldThrowConflictException(boolean shouldThrow) {
      this.shouldThrowConflictException = shouldThrow;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
      if (shouldThrowConflictException) {
        throw new ConflictingEventException("Event conflicts with existing event");
      }
      return events.add(event);
    }

    // Minimally implement required methods
    @Override
    public String getName() {
      return "MockCalendar";
    }

    @Override
    public void setName(String name) {
      // Not needed for this test
    }

    @Override
    public java.util.TimeZone getTimeZone() {
      return java.util.TimeZone.getDefault();
    }

    @Override
    public boolean addRecurringEvent(model.event.RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, java.time.LocalDate untilDate, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, java.time.LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description,
                                              String location, boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, java.time.LocalDate date, String weekdays,
                                                   java.time.LocalDate untilDate, boolean autoDecline,
                                                   String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(java.time.LocalDate date) {
      return new ArrayList<>();
    }

    @Override
    public List<Event> getEventsInRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
      return new ArrayList<>();
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return events;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property, String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property, String newValue) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) {
      return 0;
    }

    @Override
    public List<model.event.RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>();
    }

    @Override
    public String exportData(String filePath, model.export.IDataExporter exporter) throws IOException {
      if (shouldThrowIOException) {
        throw new IOException("Simulated IO exception");
      }
      return "exported_data";
    }

    @Override
    public boolean updateEvent(java.util.UUID eventId, Event updatedEvent) {
      return false;
    }
  }

  /**
   * Mock implementation of IGUIView for testing.
   */
  private static class MockGUIView implements IGUIView {
    private boolean calendarUpdated = false;
    private final MockCalendarPanel calendarPanel = new MockCalendarPanel();

    public boolean wasCalendarUpdated() {
      return calendarUpdated;
    }

    @Override
    public GUICalendarPanel getCalendarPanel() {
      return calendarPanel;
    }

    // Inner class for mock calendar panel
    private class MockCalendarPanel extends GUICalendarPanel {
      @Override
      public void updateCalendar(ICalendar calendar) {
        calendarUpdated = true;
      }
    }

    // Minimally implement other required methods
    @Override
    public void displayGUI() {
    }

    @Override
    public view.GUIEventPanel getEventPanel() {
      return null;
    }

    @Override
    public view.GUICalendarSelectorPanel getCalendarSelectorPanel() {
      return null;
    }

    @Override
    public view.GUIExportImportPanel getExportImportPanel() {
      return null;
    }

    @Override
    public viewmodel.CalendarViewModel getCalendarViewModel() {
      return null;
    }

    @Override
    public viewmodel.EventViewModel getEventViewModel() {
      return null;
    }

    @Override
    public viewmodel.ExportImportViewModel getExportImportViewModel() {
      return null;
    }

    @Override
    public void updateCalendarView(ICalendar calendar) {
    }

    @Override
    public void updateEventList(List<Event> events) {
    }

    @Override
    public void updateRecurringEventList(List<model.event.RecurringEvent> recurringEvents) {
    }

    @Override
    public void showEventDetails(Event event) {
    }

    @Override
    public void clearEventDetails() {
    }

    @Override
    public void updateCalendarList(List<String> calendarNames) {
    }

    @Override
    public String getSelectedCalendar() {
      return null;
    }

    @Override
    public java.time.LocalDate getSelectedDate() {
      return null;
    }

    @Override
    public void setSelectedDate(java.time.LocalDate date) {
    }

    @Override
    public void refreshView() {
    }

    @Override
    public void displayMessage(String message) {
    }

    @Override
    public void displayError(String error) {
    }
  }

  /**
   * Mock implementation of ExportImportViewModel for testing.
   */
  private static class MockViewModel extends ExportImportViewModel {
    private boolean importCalled = false;
    private int importResult = 0;

    public void setImportResult(int count) {
      this.importResult = count;
    }

    public boolean wasImportCalled() {
      return importCalled;
    }

    @Override
    public int importFromCSV(File file) {
      importCalled = true;
      return importResult;
    }
  }
} 
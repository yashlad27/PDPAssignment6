import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import controller.ImportExportController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import model.export.IDataExporter;
import view.GUIView;

/**
 * Test class for ImportExportController.
 * Tests the functionality of the ImportExportController for handling
 * import and export operations on calendar data.
 */
public class ImportExportControllerTest {

  private ImportExportController controller;
  private MockGUIView mockView;
  private MockCalendar mockCalendar;
  private static final String TEST_FILE_PATH = "test_export.csv";

  @Before
  public void setUp() {
    mockView = new MockGUIView();
    mockCalendar = new MockCalendar("Test Calendar");
    controller = new ImportExportController(mockCalendar, mockView);
  }

  @After
  public void tearDown() {
    // Delete any test files that were created
    File testFile = new File(TEST_FILE_PATH);
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  @Test
  public void testConstructorWithCalendarAndView() {
    assertNotNull("Controller should not be null", controller);
    assertEquals("Controller should have the correct calendar",
            mockCalendar, controller.getCurrentCalendar());
  }

  @Test
  public void testConstructorWithJustView() {
    ImportExportController newController = new ImportExportController(mockView);
    assertNotNull("Controller should not be null", newController);
    assertNull("Current calendar should be null initially", newController.getCurrentCalendar());
  }

  @Test
  public void testSetAndGetCurrentCalendar() {
    MockCalendar newCalendar = new MockCalendar("New Calendar");
    controller.setCurrentCalendar(newCalendar);
    assertEquals("Current calendar should be updated", newCalendar, controller.getCurrentCalendar());
  }

  @Test
  public void testOnExportWithNullFile() {
    controller.onExport(null);
    assertEquals("Should show error for null file",
            "Invalid file selected", mockView.getLastErrorMessage());
  }

  @Test
  public void testOnExportWithNullCalendar() {
    ImportExportController newController = new ImportExportController(mockView);
    newController.onExport(new File(TEST_FILE_PATH));
    assertEquals("Should show error for null calendar",
            "Please select a calendar first", mockView.getLastErrorMessage());
  }

  @Test
  public void testOnExportWithValidFileAndCalendar() {
    // Add some test events to the mock calendar
    Event event1 = new Event("Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            "Weekly team sync", "Conference Room A", true);
    mockCalendar.addTestEvent(event1);

    // Perform export
    File exportFile = new File(TEST_FILE_PATH);
    controller.onExport(exportFile);

    // Check results
    assertTrue("Export should create the file", exportFile.exists());
    assertTrue("Export should show success message",
            mockView.getLastDisplayMessage().contains("Exported 1 events"));
  }

  @Test
  public void testOnImportWithNullFile() {
    controller.onImport(null);
    assertEquals("Should show error for null file",
            "Invalid file selected", mockView.getLastErrorMessage());
  }

  @Test
  public void testOnImportWithNonExistentFile() {
    File nonExistentFile = new File("non_existent_file.csv");
    controller.onImport(nonExistentFile);
    assertEquals("Should show error for non-existent file",
            "Invalid file selected", mockView.getLastErrorMessage());
  }

  @Test
  public void testOnImportWithNullCalendar() {
    ImportExportController newController = new ImportExportController(mockView);

    // Create a test file to import
    File importFile = new File(TEST_FILE_PATH);
    try {
      importFile.createNewFile();
      newController.onImport(importFile);
      assertEquals("Should show error for null calendar",
              "Please select a calendar first", mockView.getLastErrorMessage());
    } catch (IOException e) {
      fail("Failed to create test file: " + e.getMessage());
    }
  }

  @Test
  public void testOnImportWithValidFileAndCalendar() {
    // Create a test file to import
    File importFile = new File(TEST_FILE_PATH);
    try {
      importFile.createNewFile();
      controller.onImport(importFile);
      // Import functionality is not implemented, so we expect a message saying so
      assertEquals("Should show message about import not being implemented",
              "Import functionality is not yet implemented.", mockView.getLastDisplayMessage());
    } catch (IOException e) {
      fail("Failed to create test file: " + e.getMessage());
    }
  }

  /**
   * Mock implementation of GUIView for testing purposes.
   */
  private static class MockGUIView extends GUIView {
    private String lastDisplayMessage;
    private String lastErrorMessage;

    public MockGUIView() {
      super(null);
    }

    @Override
    public void displayMessage(String message) {
      this.lastDisplayMessage = message;
      // For PIT testing, also print to console
      System.out.println("MockGUIView.displayMessage: " + message);
    }

    @Override
    public void showErrorMessage(String message) {
      this.lastErrorMessage = message;
      // For PIT testing, also print to console
      System.out.println("MockGUIView.showErrorMessage: " + message);
    }
    
    @Override
    public void displayError(String error) {
      this.lastErrorMessage = error;
      // For PIT testing, also print to console
      System.out.println("MockGUIView.displayError: " + error);
    }

    public String getLastDisplayMessage() {
      return lastDisplayMessage;
    }

    public String getLastErrorMessage() {
      return lastErrorMessage;
    }
  }

  /**
   * Mock implementation of ICalendar for testing purposes.
   */
  private static class MockCalendar implements ICalendar {
    private final String name;
    private final List<Event> events;
    private boolean shouldThrowIOException;

    public MockCalendar(String name) {
      this.name = name;
      this.events = new ArrayList<>();
      this.shouldThrowIOException = false;
    }

    public void addTestEvent(Event event) {
      events.add(event);
    }

    public void setShouldThrowIOException(boolean shouldThrow) {
      this.shouldThrowIOException = shouldThrow;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public List<Event> getAllEvents() {
      return events;
    }

    @Override
    public String exportData(String filePath, IDataExporter exporter) throws IOException {
      if (shouldThrowIOException) {
        throw new IOException("Simulated export failure");
      }
      return exporter.export(filePath, events);
    }

    // Remaining interface methods with minimal implementations
    @Override
    public void setName(String name) {
      // Not implemented for test
    }

    @Override
    public TimeZone getTimeZone() {
      return TimeZone.getDefault();
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) {
      return events.add(event);
    }

    @Override
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, LocalDate untilDate, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return new ArrayList<>();
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
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
    public List<RecurringEvent> getAllRecurringEvents() {
      return new ArrayList<>();
    }

    @Override
    public boolean updateEvent(UUID eventId, Event updatedEvent) {
      return false;
    }
  }
} 
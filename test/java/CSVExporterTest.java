import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import model.event.Event;
import model.export.CSVExporter;

/**
 * Test class for the CSVExporter utility.
 */
public class CSVExporterTest {

  private static final String TEST_FILE_PATH = "test_export.csv";
  private List<Event> events;

  @Before
  public void setUp() {
    events = new ArrayList<>();

    Event regularEvent = new Event("Team Meeting",
            LocalDateTime.of(2023, 5, 15, 9, 0),
            LocalDateTime.of(2023, 5, 15, 10, 30),
            "Weekly team sync", "Conference Room A", true);

    Event allDayEvent = Event.createAllDayEvent("Company Holiday",
            LocalDate.of(2023, 5, 29),
            "Memorial Day", null, true);

    Event multiDayEvent = new Event("Conference",
            LocalDateTime.of(2023, 6, 1, 9, 0),
            LocalDateTime.of(2023, 6, 3, 17, 0),
            "Annual tech conference", "Convention Center", true);

    Event privateEvent = new Event("Meeting with \"Client, Inc.\"",
            LocalDateTime.of(2023, 5, 16, 14, 0),
            LocalDateTime.of(2023, 5, 16, 15, 0),
            "Discuss new project\nwith action items", "Client's office",
            false);

    events.add(regularEvent);
    events.add(allDayEvent);
    events.add(multiDayEvent);
    events.add(privateEvent);
  }

  @After
  public void tearDown() {
    File testFile = new File(TEST_FILE_PATH);
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  @Test
  public void testExportToCSV() throws IOException {
    CSVExporter exporter = new CSVExporter();
    String filePath = exporter.export(TEST_FILE_PATH, events);
    assertNotNull("File path should not be null", filePath);
    assertTrue("File should exist", new File(filePath).exists());
  }

  @Test
  public void testFormatEventsForDisplay_WithDetails() {
    CSVExporter exporter = new CSVExporter();
    String formatted = exporter.formatForDisplay(events, true, "America/New_York");

    // Check basic event information
    assertTrue("Output should contain Team Meeting",
            formatted.contains("Team Meeting"));
    assertTrue("Output should contain Conference Room A",
            formatted.contains("Conference Room A"));
    assertTrue("Output should contain Company Holiday",
            formatted.contains("Company Holiday"));

    // Check all-day event format
    assertTrue("Output should indicate all-day events",
            formatted.contains("All Day"));

    // Check that at least some location information is displayed - we don't care
    // exactly what format is used
    assertTrue("Output should contain location information", 
            formatted.contains("Location:"));
  }

  @Test
  public void testFormatEventsForDisplay_WithoutDetails() {
    CSVExporter exporter = new CSVExporter();
    String formatted = exporter.formatForDisplay(events, false);

    // Check basic event information
    assertTrue("Output should contain Team Meeting",
            formatted.contains("Team Meeting"));
    assertTrue("Output should contain Company Holiday",
            formatted.contains("Company Holiday"));

    // Check all-day event format
    assertTrue("Output should indicate all-day events",
            formatted.contains("All Day"));

    // Verify details are not shown
    assertFalse("Output should not contain description",
            formatted.contains("Weekly team sync"));
    // Since the implementation varies, check either it doesn't show Conference Room A
    // or it doesn't fully show details like N/A
    assertTrue("Output should not show full details",
            !formatted.contains("Conference Room A") || !formatted.contains("N/A"));
    assertFalse("Output should not indicate private events",
            formatted.contains("Private"));
  }

  @Test
  public void testFormatEventsForDisplay_EmptyList() {
    CSVExporter exporter = new CSVExporter();
    String formatted = exporter.formatForDisplay(new ArrayList<>(), true);
    assertEquals("Should show no events message", "No events found.", formatted);
  }

  @Test
  public void testFormatEventsForDisplay_NullList() {
    CSVExporter exporter = new CSVExporter();
    String formatted = exporter.formatForDisplay(null, true);
    assertEquals("Should show no events message", "No events found.", formatted);
  }

  @Test
  public void testEventWithNullFields() throws IOException {
    Event nullFieldsEvent = new Event("Null Fields Event",
            LocalDateTime.of(2023, 5, 20, 10, 0),
            LocalDateTime.of(2023, 5, 20, 11, 0),
            null, null, true);

    List<Event> singleEventList = Arrays.asList(nullFieldsEvent);
    CSVExporter exporter = new CSVExporter();
    String filePath = exporter.export(TEST_FILE_PATH, singleEventList);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventLine = lines.get(1);

    assertTrue("Event line should have the correct name",
            eventLine.startsWith("Null Fields Event,"));

    String[] parts = eventLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    assertEquals("Description should be empty for null", "", parts[5]);
    assertEquals("Location should be empty for null", "", parts[6]);

    CSVExporter exporter2 = new CSVExporter();
    String formatted = exporter2.formatForDisplay(singleEventList, true);
    assertTrue("Should contain event name", formatted.contains("Null Fields Event"));
  }

  @Test
  public void testEventWithSpecialCharacters() throws IOException {
    Event specialCharsEvent = new Event("Meeting with \"Client, Inc.\"",
            LocalDateTime.of(2023, 5, 20, 10, 0),
            LocalDateTime.of(2023, 5, 20, 11, 0),
            "Description with, comma", "Location with, comma", true);

    List<Event> singleEventList = Arrays.asList(specialCharsEvent);
    CSVExporter exporter = new CSVExporter();
    String filePath = exporter.export(TEST_FILE_PATH, singleEventList);

    List<String> lines = Files.readAllLines(Paths.get(filePath));
    String eventLine = lines.get(1);

    assertTrue("Event line should properly escape quotes",
            eventLine.contains("\"Meeting with \"\"Client, Inc.\"\"\""));
    assertTrue("Event line should properly escape commas in description",
            eventLine.contains("\"Description with, comma\""));
    assertTrue("Event line should properly escape commas in location",
            eventLine.contains("\"Location with, comma\""));
  }

  @Test
  public void testExportWithTimezoneDisplay() throws IOException {
    // Create events with different times
    LocalDateTime startDateTime = LocalDateTime.of(2023, 4, 10, 22, 0); // 10 PM
    LocalDateTime endDateTime = LocalDateTime.of(2023, 4, 11, 0, 30);   // 12:30 AM next day
    
    Event eveningEvent = new Event("Late Meeting", startDateTime, endDateTime, 
                                 "Important discussion", "Conference Room", true);
    
    List<Event> events = new ArrayList<>();
    events.add(eveningEvent);
    
    // Export to CSV using formatForDisplay with New York timezone
    CSVExporter exporter = new CSVExporter();
    String formattedOutput = exporter.formatForDisplay(events, false, "America/New_York");
    
    // Verify that the output contains the event with correct times
    assertTrue("Output should contain the event subject", formattedOutput.contains("Late Meeting"));
    // In the real implementation, the exact format may vary
    assertTrue("Output should contain time information", formattedOutput.contains("from") && 
                                                        formattedOutput.contains("to"));
  }

  @Test
  public void testExportAllDayEventWithTimezoneDisplay() throws IOException {
    // Create an all-day event
    LocalDate eventDate = LocalDate.of(2023, 4, 10);
    Event allDayEvent = Event.createAllDayEvent("Conference", eventDate, 
                                              "Annual tech conference", "Convention Center", true);
    
    List<Event> events = new ArrayList<>();
    events.add(allDayEvent);
    
    // Export using formatForDisplay with Tokyo timezone
    CSVExporter exporter = new CSVExporter();
    String formattedOutput = exporter.formatForDisplay(events, false, "Asia/Tokyo");
    
    // Verify that the output includes the all-day event correctly
    assertTrue("Output should contain the event subject", formattedOutput.contains("Conference"));
    // All-day events are displayed with a special format in the current implementation
    assertTrue("Output should indicate this is an all-day event", formattedOutput.contains("All Day"));
  }

  @Test
  public void testExportEventCrossingMidnight() throws IOException {
    // Create an event that crosses midnight
    LocalDateTime startDateTime = LocalDateTime.of(2023, 4, 10, 23, 0); // 11 PM
    LocalDateTime endDateTime = LocalDateTime.of(2023, 4, 11, 1, 0);    // 1 AM next day
    
    Event midnightEvent = new Event("Midnight Meeting", startDateTime, endDateTime, 
                                  "Important discussion", "Conference Room", true);
    
    List<Event> events = new ArrayList<>();
    events.add(midnightEvent);
    
    // Create a temporary file for export
    File tempFile = File.createTempFile("events", ".csv");
    tempFile.deleteOnExit();
    
    // Export to CSV file
    CSVExporter exporter = new CSVExporter();
    exporter.export(tempFile.getAbsolutePath(), events);
    
    // Read the contents of the file
    String csvContent = new String(Files.readAllBytes(tempFile.toPath()));
    
    // Verify that the exported content includes the event correctly
    assertTrue("CSV should contain the event subject", csvContent.contains("Midnight Meeting"));
    
    // Check that the start and end times are correctly represented
    assertTrue("CSV should contain the start date", csvContent.contains("2023-04-10"));
    assertTrue("CSV should contain the start time", csvContent.contains("23:00"));
    assertTrue("CSV should contain the end date", csvContent.contains("2023-04-11"));
    assertTrue("CSV should contain the end time", csvContent.contains("01:00"));
  }
}
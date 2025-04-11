import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import model.calendar.timezone.TimezoneService;

/**
 * Unit tests for the TimezoneService class that handles timezone conversions.
 */
public class TimezoneServiceTest {
  private TimezoneService timezoneService;
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Before
  public void setUp() {
    timezoneService = new TimezoneService();
  }

  @Test
  public void testConvertToUTC() {
    // New York is UTC-5 (in standard time) or UTC-4 (in daylight saving time)
    LocalDateTime nyDateTime = LocalDateTime.parse("2023-01-15 12:00:00", formatter);
    LocalDateTime utcDateTime = timezoneService.convertToUTC(nyDateTime, "America/New_York");

    // In January, NY is on EST (UTC-5)
    assertEquals("2023-01-15 17:00:00", utcDateTime.format(formatter));

    // Test summer time (daylight saving)
    nyDateTime = LocalDateTime.parse("2023-07-15 12:00:00", formatter);
    utcDateTime = timezoneService.convertToUTC(nyDateTime, "America/New_York");

    // In July, NY is on EDT (UTC-4)
    assertEquals("2023-07-15 16:00:00", utcDateTime.format(formatter));
  }

  @Test
  public void testConvertFromUTC() {
    // Convert UTC time to New York time
    LocalDateTime utcDateTime = LocalDateTime.parse("2023-01-15 17:00:00", formatter);
    LocalDateTime nyDateTime = timezoneService.convertFromUTC(utcDateTime, "America/New_York");

    // In January, NY is on EST (UTC-5)
    assertEquals("2023-01-15 12:00:00", nyDateTime.format(formatter));

    // Test summer time (daylight saving)
    utcDateTime = LocalDateTime.parse("2023-07-15 16:00:00", formatter);
    nyDateTime = timezoneService.convertFromUTC(utcDateTime, "America/New_York");

    // In July, NY is on EDT (UTC-4)
    assertEquals("2023-07-15 12:00:00", nyDateTime.format(formatter));
  }

  @Test
  public void testGetAvailableTimezones() {
    String[] timezones = timezoneService.getAvailableTimezones();

    // Verify the array is not empty and contains some expected timezones
    assertNotNull(timezones);
    assertTrue(timezones.length > 0);
    assertTrue(Arrays.asList(timezones).contains("America/New_York"));
    assertTrue(Arrays.asList(timezones).contains("Europe/London"));
    assertTrue(Arrays.asList(timezones).contains("Asia/Tokyo"));
  }

  @Test
  public void testGetDefaultTimezone() {
    assertEquals("America/New_York", timezoneService.getDefaultTimezone());
  }

  @Test
  public void testGetSystemDefaultTimezone() {
    String systemDefaultTimezone = timezoneService.getSystemDefaultTimezone();
    assertNotNull(systemDefaultTimezone);
    assertEquals(TimeZone.getDefault().getID(), systemDefaultTimezone);
  }

  @Test
  public void testIsValidTimezone() {
    assertTrue(timezoneService.isValidTimezone("America/New_York"));
    assertTrue(timezoneService.isValidTimezone("Europe/London"));
    assertTrue(timezoneService.isValidTimezone("UTC"));

    assertFalse(timezoneService.isValidTimezone(null));
    assertFalse(timezoneService.isValidTimezone(""));
    assertFalse(timezoneService.isValidTimezone("   "));
    assertFalse(timezoneService.isValidTimezone("Invalid/Timezone"));
  }

  @Test
  public void testConvertTime() {
    // Convert from New York to Tokyo
    LocalDateTime nyDateTime = LocalDateTime.parse("2023-01-15 12:00:00", formatter);
    LocalDateTime tokyoDateTime = timezoneService.convertTime(nyDateTime, "America/New_York", "Asia/Tokyo");

    // Tokyo is UTC+9, NY in January is UTC-5, so difference is 14 hours
    assertEquals("2023-01-16 02:00:00", tokyoDateTime.format(formatter));

    // Convert from Tokyo to London
    LocalDateTime londonDateTime = timezoneService.convertTime(tokyoDateTime, "Asia/Tokyo", "Europe/London");

    // London is UTC+0, Tokyo is UTC+9, so difference is 9 hours
    assertEquals("2023-01-15 17:00:00", londonDateTime.format(formatter));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertToUTC_NullDateTime() {
    timezoneService.convertToUTC(null, "America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertToUTC_NullTimezone() {
    LocalDateTime dateTime = LocalDateTime.now();
    timezoneService.convertToUTC(dateTime, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertFromUTC_NullDateTime() {
    timezoneService.convertFromUTC(null, "America/New_York");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertFromUTC_NullTimezone() {
    LocalDateTime dateTime = LocalDateTime.now();
    timezoneService.convertFromUTC(dateTime, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTime_NullDateTime() {
    timezoneService.convertTime(null, "America/New_York", "Europe/London");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTime_InvalidSourceTimezone() {
    LocalDateTime dateTime = LocalDateTime.now();
    timezoneService.convertTime(dateTime, "Invalid/Timezone", "Europe/London");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConvertTime_InvalidTargetTimezone() {
    LocalDateTime dateTime = LocalDateTime.now();
    timezoneService.convertTime(dateTime, "America/New_York", "Invalid/Timezone");
  }

  @Test
  public void testGetConverter() {
    // Create a converter from New York to Tokyo
    TimezoneService.TimezoneConverter converter = timezoneService.getConverter("America/New_York", "Asia/Tokyo");

    // Test the converter
    LocalDateTime nyDateTime = LocalDateTime.parse("2023-01-15 12:00:00", formatter);
    LocalDateTime tokyoDateTime = converter.convert(nyDateTime);

    // Tokyo is UTC+9, NY in January is UTC-5, so difference is 14 hours
    assertEquals("2023-01-16 02:00:00", tokyoDateTime.format(formatter));
  }

  @Test
  public void testRoundTripConversion() {
    // Test full round trip: NY -> Tokyo -> NY should give us the original time
    LocalDateTime originalDateTime = LocalDateTime.parse("2023-01-15 12:00:00", formatter);
    LocalDateTime tokyoDateTime = timezoneService.convertTime(originalDateTime, "America/New_York", "Asia/Tokyo");
    LocalDateTime roundTripDateTime = timezoneService.convertTime(tokyoDateTime, "Asia/Tokyo", "America/New_York");

    assertEquals(originalDateTime, roundTripDateTime);
  }

  @Test
  public void testConvertDateTimeAcrossDifferentTimezones() {
    // Create timezone service with default timezone (America/New_York)
    TimezoneService service = new TimezoneService();
    
    // Test converting from New York to Tokyo
    LocalDateTime nyDateTime = LocalDateTime.of(2023, 4, 10, 20, 0); // 8 PM in New York
    String nyTz = "America/New_York";
    String tokyoTz = "Asia/Tokyo";
    
    LocalDateTime tokyoDateTime = service.convertTime(nyDateTime, nyTz, tokyoTz);
    
    // Tokyo is UTC+9, New York is UTC-4 or UTC-5 depending on DST
    // The difference is 13 or 14 hours, so 8 PM in NY is 9 or 10 AM next day in Tokyo
    assertTrue(tokyoDateTime.getDayOfMonth() == 11); // Should be next day
    assertTrue(tokyoDateTime.getHour() >= 9); // Should be morning in Tokyo
  }

  @Test
  public void testConvertDateTimeAroundMidnight() {
    // Create timezone service
    TimezoneService service = new TimezoneService();
    
    // Test event at 11:30 PM
    LocalDateTime lateNight = LocalDateTime.of(2023, 4, 10, 23, 30);
    String nyTz = "America/New_York";
    String laTz = "America/Los_Angeles";
    
    LocalDateTime westCoastTime = service.convertTime(lateNight, nyTz, laTz);
    
    // West coast is 3 hours behind NY, so 11:30 PM in NY is 8:30 PM in LA
    assertEquals(10, westCoastTime.getDayOfMonth());
    assertEquals(20, westCoastTime.getHour());
    assertEquals(30, westCoastTime.getMinute());
  }

  @Test
  public void testConvertDateTimeFromDifferentTimezoneToLocal() {
    TimezoneService service = new TimezoneService();
    
    // Set up a specific reference timezone
    String londonTz = "Europe/London";
    String nyTz = "America/New_York";
    
    // 3:00 PM in London
    LocalDateTime londonTime = LocalDateTime.of(2023, 4, 10, 15, 0);
    
    // Convert to the service's local timezone (assumed to be America/New_York)
    LocalDateTime localTime = service.convertTime(londonTime, londonTz, nyTz);
    
    // London is UTC+1 (during DST), NY is UTC-4 (during DST), so 5 hours difference
    // 3 PM in London should be 10 AM in New York
    assertEquals(10, localTime.getHour());
  }

  @Test
  public void testTimezoneOffsetCalculation() {
    TimezoneService service = new TimezoneService();
    
    // Test round-trip conversion to verify offset calculation is working correctly
    String nyTz = "America/New_York";
    String tokyoTz = "Asia/Tokyo";
    
    LocalDateTime original = LocalDateTime.of(2023, 4, 10, 12, 0); // Noon in NY
    LocalDateTime converted = service.convertTime(original, nyTz, tokyoTz);
    LocalDateTime roundTrip = service.convertTime(converted, tokyoTz, nyTz);
    
    // After round trip, time should be the same
    assertEquals(original, roundTrip);
    // Tokyo should be next day
    assertTrue(converted.getDayOfMonth() > original.getDayOfMonth() || 
           (original.getDayOfMonth() == 30 && converted.getDayOfMonth() == 1));
  }

  @Test
  public void testDateBoundaryHandlingInDifferentTimezones() {
    TimezoneService service = new TimezoneService();
    
    // 1:00 AM in New York
    LocalDateTime earlyMorningNY = LocalDateTime.of(2023, 4, 10, 1, 0);
    
    // Convert to Tokyo time (should be afternoon on the same day in Tokyo)
    String nyTz = "America/New_York";
    String tokyoTz = "Asia/Tokyo";
    LocalDateTime tokyoTime = service.convertTime(earlyMorningNY, nyTz, tokyoTz);
    
    // This should be same day in Tokyo, but later in the day
    assertEquals(10, tokyoTime.getDayOfMonth());
    assertTrue(tokyoTime.getHour() >= 14); // Should be afternoon in Tokyo
  }
} 
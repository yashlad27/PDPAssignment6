import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.TimeZone;

import model.calendar.timezone.TimezoneService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
} 
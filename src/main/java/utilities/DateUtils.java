package utilities;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for date-related operations.
 */
public class DateUtils {

  /**
   * Gets the start of the day (00:00:00) for a given date.
   *
   * @param date the date to get the start of day for
   * @return a new Date object set to the start of the given day
   */
  public static Date getStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  /**
   * Gets the end of the day (23:59:59) for a given date.
   *
   * @param date the date to get the end of day for
   * @return a new Date object set to the end of the given day
   */
  public static Date getEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }
} 
package view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

/**
 * Data transfer object that encapsulates all form data from the event panel.
 * This class contains no business logic, only data from UI components.
 * It serves as a clean interface between the view and controller layers.
 */
public class EventFormData {
  private final String subject;
  private final Date selectedDate;
  private final Date startTime;
  private final Date endTime;
  private final String location;
  private final String description;
  private final boolean isRecurring;
  private final boolean isAllDay;
  private final Set<DayOfWeek> weekdays;
  private final int occurrences;
  private final LocalDate untilDate;
  private final boolean isPrivate;
  private final boolean autoDecline;

  /**
   * Constructs a new EventFormData with all the form field values.
   *
   * @param subject      the event subject
   * @param selectedDate the selected date
   * @param startTime    the start time
   * @param endTime      the end time
   * @param location     the event location
   * @param description  the event description
   * @param isRecurring  whether the event is recurring
   * @param isAllDay     whether the event is an all-day event
   * @param weekdays     the weekdays for recurring events
   * @param occurrences  the number of occurrences for recurring events
   * @param untilDate    the end date for recurring events
   * @param isPrivate    whether the event is private
   * @param autoDecline  whether auto-decline is enabled for the event
   */
  public EventFormData(String subject, Date selectedDate, Date startTime, Date endTime,
                       String location, String description, boolean isRecurring,
                       boolean isAllDay, Set<DayOfWeek> weekdays, int occurrences,
                       LocalDate untilDate, boolean isPrivate, boolean autoDecline) {
    this.subject = subject;
    this.selectedDate = selectedDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.location = location;
    this.description = description;
    this.isRecurring = isRecurring;
    this.isAllDay = isAllDay;
    this.weekdays = weekdays;
    this.occurrences = occurrences;
    this.untilDate = untilDate;
    this.isPrivate = isPrivate;
    this.autoDecline = autoDecline;
  }

  // Getters for all fields
  public String getSubject() {
    return subject;
  }

  public Date getSelectedDate() {
    return selectedDate;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public String getLocation() {
    return location;
  }

  public String getDescription() {
    return description;
  }

  public boolean isRecurring() {
    return isRecurring;
  }

  public boolean isAllDay() {
    return isAllDay;
  }

  public Set<DayOfWeek> getWeekdays() {
    return weekdays;
  }

  public int getOccurrences() {
    return occurrences;
  }

  public LocalDate getUntilDate() {
    return untilDate;
  }

  /**
   * @return whether the event is private
   */
  public boolean isPrivateEvent() {
    return isPrivate;
  }

  /**
   * @return whether auto-decline is enabled for the event
   */
  public boolean isAutoDecline() {
    return autoDecline;
  }

  /**
   * Alias for getUntilDate() to match method name used in controller
   *
   * @return the end date for recurring events
   */
  public LocalDate getRecurringEndDate() {
    return untilDate;
  }
}

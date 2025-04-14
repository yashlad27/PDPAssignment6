package model.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a calendar event with properties like subject, start and end times, description,
 * location, and privacy setting. All times are stored in UTC.
 */
public class Event {

  private final UUID id;
  private String subject;
  private LocalDateTime startDateTime; // Stored in UTC
  private LocalDateTime endDateTime;   // Stored in UTC
  private String description;
  private String location;
  private boolean isPublic;
  private boolean isAllDay;

  /**
   * Constructs a new Event with the given parameters.
   *
   * @param subject       the subject/title of the event
   * @param startDateTime the start date and time in the calendar's timezone
   * @param endDateTime   the end date and time in the calendar's timezone, null if all-day event
   * @param description   a description of the event, can be null
   * @param location      the location of the event, can be null
   * @param isPublic      whether the event is public
   * @param isAllDay      whether the event is an all-day event
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
      String description, String location, boolean isPublic, boolean isAllDay) {
    this(UUID.randomUUID(), subject, startDateTime, endDateTime, description, location,
        isPublic, isAllDay);
  }

  /**
   * Constructs a new Event with the given parameters.
   *
   * @param subject       the subject/title of the event
   * @param startDateTime the start date and time in the calendar's timezone
   * @param endDateTime   the end date and time in the calendar's timezone, null if all-day event
   * @param description   a description of the event, can be null
   * @param location      the location of the event, can be null
   * @param isPublic      whether the event is public
   */
  public Event(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
      String description, String location, boolean isPublic) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.id = UUID.randomUUID();
    this.subject = subject;

    // Store times in UTC
    this.startDateTime = startDateTime;
    this.description = description != null ? description : "";
    this.location = location != null ? location : "";
    this.isPublic = isPublic;

    if (endDateTime == null) {
      this.isAllDay = true;
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(),
          LocalTime.of(23, 59, 59));
    } else {
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time must not be before start date/time");
      }
      this.isAllDay = false;
      this.endDateTime = endDateTime;
    }
  }

  /**
   * Creates an event with a specific ID (used for updating existing events).
   *
   * @param id            the UUID to use for this event
   * @param subject       the subject/title of the event
   * @param startDateTime the start date and time in the calendar's timezone
   * @param endDateTime   the end date and time in the calendar's timezone, null if all-day event
   * @param description   a description of the event, can be null
   * @param location      the location of the event, can be null
   * @param isPublic      whether the event is public
   * @param isAllDay      whether the event is an all-day event
   */
  public Event(UUID id, String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
      String description, String location, boolean isPublic, boolean isAllDay) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (id == null) {
      throw new IllegalArgumentException("Event ID cannot be null");
    }

    this.id = id;
    this.subject = subject;

    // Store times in UTC
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description != null ? description : "";
    this.location = location != null ? location : "";
    this.isPublic = isPublic;
    this.isAllDay = isAllDay;

    if (endDateTime == null) {
      // For all-day events, set end time to end of day
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(),
          LocalTime.of(23, 59, 59));
    } else {
      if (endDateTime.isBefore(startDateTime)) {
        throw new IllegalArgumentException("End date/time must not be before start date/time");
      }
      this.isAllDay = false;
      this.endDateTime = endDateTime;
    }
  }

  /**
   * Creates an all-day event for a specific date.
   *
   * @param subject     the subject/title of the event
   * @param date        the date of the all-day event
   * @param description a description of the event, can be null
   * @param location    the location of the event, can be null
   * @param isPublic    whether the event is public
   * @return a new all-day Event
   */
  public static Event createAllDayEvent(String subject, LocalDate date, String description,
      String location, boolean isPublic) {
    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(0, 0));
    LocalDateTime end = LocalDateTime.of(date, LocalTime.of(23, 59, 59));

    Event event = new Event(subject, start, end, description, location, isPublic);
    event.isAllDay = true;
    event.date = date;
    return event;
  }

  /**
   * Checks if this event conflicts with another event. Two events conflict if their time intervals
   * overlap.
   *
   * @param other the event to check for conflicts
   * @return true if there is a conflict, false otherwise
   */
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    // For zero-duration events (where start equals end):
    boolean thisIsZeroDuration = this.startDateTime.equals(this.endDateTime);
    boolean otherIsZeroDuration = other.startDateTime.equals(other.endDateTime);

    // If both are zero-duration events, they conflict only if they're at the exact same time
    if (thisIsZeroDuration && otherIsZeroDuration) {
      return this.startDateTime.equals(other.startDateTime);
    }

    // If this is a zero-duration event
    if (thisIsZeroDuration) {
      // It conflicts if it falls exactly at or between the other event's start and end times
      return (this.startDateTime.equals(other.startDateTime) ||
          this.startDateTime.equals(other.endDateTime) ||
          (this.startDateTime.isAfter(other.startDateTime) &&
              this.startDateTime.isBefore(other.endDateTime)));
    }

    // If the other is a zero-duration event
    if (otherIsZeroDuration) {
      // It conflicts if it falls exactly at or between this event's start and end times
      return (other.startDateTime.equals(this.startDateTime) ||
          other.startDateTime.equals(this.endDateTime) ||
          (other.startDateTime.isAfter(this.startDateTime) &&
              other.startDateTime.isBefore(this.endDateTime)));
    }

    // Standard overlap check for events with duration:
    // Events overlap if they share any time span
    return !(this.endDateTime.isBefore(other.startDateTime) ||
        this.startDateTime.isAfter(other.endDateTime));
  }

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if the event spans multiple days, false otherwise
   */
  public boolean spansMultipleDays() {
    return !startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
  }

  /**
   * Gets the unique identifier for this event.
   *
   * @return the UUID of this event
   */
  public UUID getId() {
    return id;
  }

  /**
   * Gets the subject of this event.
   *
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Sets the subject of this event.
   *
   * @param subject the new subject
   * @throws IllegalArgumentException if subject is null or empty
   */
  public void setSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
    this.subject = subject;
  }

  /**
   * Gets the start date and time in the calendar's timezone.
   *
   * @return the start date and time in the calendar's timezone
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Gets the end date and time in the calendar's timezone.
   *
   * @return the end date and time in the calendar's timezone
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Sets the start date and time in the calendar's timezone.
   *
   * @param startDateTime the new start date and time in the calendar's timezone
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }
    if (this.endDateTime != null && startDateTime.isAfter(this.endDateTime)) {
      throw new IllegalArgumentException("Start date/time cannot be after end date/time");
    }
    this.startDateTime = startDateTime;
  }

  /**
   * Sets the end date and time in the calendar's timezone.
   *
   * @param endDateTime the new end date and time in the calendar's timezone
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    if (endDateTime == null) {
      // Converting to all-day event
      this.isAllDay = true;
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(),
          LocalTime.of(23, 59, 59));
    } else {
      if (endDateTime.isBefore(this.startDateTime)) {
        throw new IllegalArgumentException("End date/time cannot be before start date/time");
      }
      this.endDateTime = endDateTime;
      this.isAllDay = false;
    }
  }

  /**
   * Gets the description of this event.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of this event.
   *
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks if this event is public.
   *
   * @return true if the event is public, false otherwise
   */
  public boolean isPublic() {
    return isPublic;
  }

  /**
   * Checks if this is an all-day event.
   *
   * @return true if this is an all-day event, false otherwise
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Sets whether this is an all-day event.
   *
   * @param isAllDay true if this should be an all-day event, false otherwise
   */
  public void setAllDay(boolean isAllDay) {
    this.isAllDay = isAllDay;

    if (isAllDay) {
      this.endDateTime = LocalDateTime.of(startDateTime.toLocalDate(),
          LocalTime.of(23, 59, 59));
    }
  }

  /**
   * Sets the description of this event.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the location of this event.
   *
   * @param location the new location
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * The date of an all-day event.
   */
  private LocalDate date;

  /**
   * Gets the date of this all-day event.
   *
   * @return the date of this event, or null if this is not an all-day event
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Sets the date of this all-day event.
   *
   * @param date the date of this all-day event
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  /**
   * Checks if this event is an all-day event.
   *
   * @return true if this is an all-day event, false otherwise
   */
  public boolean isAllDayEvent() {
    return date != null && startDateTime == null && endDateTime == null;
  }

  /**
   * Sets whether this event is public.
   *
   * @param isPublic true if the event is public, false otherwise
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Event event = (Event) o;
    return Objects.equals(subject, event.subject) && Objects.equals(startDateTime,
        event.startDateTime) && Objects.equals(endDateTime, event.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return "Event{" + "subject='" + subject + '\'' + ", startDateTime=" + startDateTime
        + ", endDateTime=" + endDateTime + ", isAllDay=" + isAllDay + ", location='" + (
        location != null ? location : "N/A") + '\'' + '}';
  }
}
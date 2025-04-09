package model.event;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Represents a recurring event that repeats on specified days of the week.
 * Extends the base Event class to add repetition functionality.
 */
public class RecurringEvent extends Event {

  private final Set<DayOfWeek> repeatDays;
  private final int occurrences;
  private final LocalDate endDate;
  private final UUID recurringId;
  private final boolean isAllDay;

  /**
   * @return true if this event spans the entire day
   */
  public boolean isAllDay() {
    return isAllDay;
  }

  /**
   * Constructs a new recurring event.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param description   the event description
   * @param location      the event location
   * @param isPublic      whether the event is public
   * @param repeatDays    the days of the week to repeat on
   * @param occurrences   the number of occurrences
   * @param endDate       the date until which to repeat
   * @param recurringId   the recurring event ID
   * @param isAllDay      whether the event is an all-day event
   */
  private RecurringEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                         String description, String location,
                         boolean isPublic, Set<DayOfWeek> repeatDays,
                         int occurrences, LocalDate endDate, UUID recurringId, boolean isAllDay) {
    super(subject, startDateTime, endDateTime, description, location, isPublic);
    this.repeatDays = new HashSet<>(repeatDays);
    this.occurrences = occurrences;
    this.endDate = endDate;
    this.recurringId = recurringId != null ? recurringId : UUID.randomUUID();
    this.isAllDay = isAllDay;

    if (isAllDay) {
      this.setAllDay(true);
    }
  }

  /**
   * Builder class for RecurringEvent.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Set<DayOfWeek> repeatDays;

    private String description = null;
    private String location = null;
    private boolean isPublic = true;
    private int occurrences = -1;
    private LocalDate endDate = null;
    private UUID recurringId = null;
    private boolean isAllDay = false;

    /**
     * Sets whether this is an all-day event.
     *
     * @param isAllDay true if this is an all-day event
     * @return This builder for method chaining
     */
    public Builder allDay(boolean isAllDay) {
      this.isAllDay = isAllDay;
      return this;
    }

    /**
     * Constructor for the builder with required params.
     *
     * @param subject       The name/title of the recurring event
     * @param startDateTime The start date and time of the first occurrence
     * @param endDateTime   The end date and time of the first occurrence
     * @param repeatDays    Set of days of the week on which the event repeats
     * @throws IllegalArgumentException if repeatDays is null or empty
     */
    public Builder(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                   Set<DayOfWeek> repeatDays) {
      this.subject = subject;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.repeatDays = repeatDays;
    }

    /**
     * Sets the description for the recurring event.
     *
     * @param description The description text for the event
     * @return This builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location for the recurring event.
     *
     * @param location The location text for the event
     * @return This builder for method chaining
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the visibility status of the recurring event.
     *
     * @param isPublic True if the event is public, false if private
     * @return This builder for method chaining
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Sets whether the recurring event is an all-day event.
     *
     * @param isAllDay True if the event is an all-day event, false otherwise
     * @return This builder for method chaining
     */
    public Builder isAllDay(boolean isAllDay) {
      this.isAllDay = isAllDay;
      return this;
    }

    /**
     * Sets the number of occurrences for the recurring event.
     * Setting occurrences will clear any previously set end date.
     *
     * @param occurrences Number of times the event should occur
     * @return This builder for method chaining
     */
    public Builder occurrences(int occurrences) {
      this.occurrences = occurrences;
      this.endDate = null;
      return this;
    }

    /**
     * Sets the end date for the recurring event.
     * Setting an end date will clear any previously set occurrences.
     *
     * @param endDate The last date on which the event can occur
     * @return This builder for method chaining
     */
    public Builder endDate(LocalDate endDate) {
      this.endDate = endDate;
      this.occurrences = -1;
      return this;
    }

    /**
     * Sets a specific recurring ID for the event series.
     * If not set, a random UUID will be generated.
     *
     * @param recurringId The UUID to use as the recurring event ID
     * @return This builder for method chaining
     */
    public Builder recurringId(UUID recurringId) {
      this.recurringId = recurringId;
      return this;
    }

    /**
     * Builds the RecurringEvent with the specified parameters.
     *
     * @return a new Recurring event.
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public RecurringEvent build() {
      validate();
      return new RecurringEvent(subject, startDateTime, endDateTime,
              description, location, isPublic, repeatDays, occurrences,
              endDate, recurringId, isAllDay);
    }

    /**
     * Validates the builder parameters.
     *
     * @throws IllegalArgumentException if parameters are invalid
     */
    private void validate() {
      validateCollection(repeatDays, "Repeat days cannot be null or empty.");

      // Check that exactly one of occurrences or endDate is specified
      if (isPositive(occurrences) && endDate != null) {
        throw new IllegalArgumentException("Cannot specify both occurrences and endDate");
      }

      if (!isPositive(occurrences) && endDate == null) {
        throw new IllegalArgumentException("Must specify either occurrences or endDate");
      }

      if (endDate != null && startDateTime.toLocalDate().isAfter(endDate)) {
        throw new IllegalArgumentException("End date must be after start date");
      }
    }

    /**
     * Checks if a value is positive (greater than zero).
     *
     * @param value the value to check
     * @return true if the value is positive
     */
    private boolean isPositive(int value) {
      return value > 0;
    }

    /**
     * Validates that a collection is not null or empty.
     *
     * @param collection   the collection to validate
     * @param errorMessage the error message to throw if invalid
     * @throws IllegalArgumentException if the collection is null or empty
     */
    private <T> void validateCollection(Collection<T> collection, String errorMessage) {
      if (collection == null || collection.isEmpty()) {
        throw new IllegalArgumentException(errorMessage);
      }
    }
  }

  /**
   * Gets all occurrences of this recurring event.
   *
   * @return a list of all occurrences of this recurring event
   */
  public List<Event> getAllOccurrences() {
    List<Event> occurrences = new ArrayList<>();
    LocalDateTime currentDate = getStartDateTime();
    int count = 0;
    
    // Continue until we've generated the requested number of occurrences
    // or reached the end date if specified
    while ((this.occurrences <= 0 || count < this.occurrences) && 
           (this.endDate == null || !currentDate.toLocalDate().isAfter(this.endDate))) {
      
      // If the current day is one of the repeat days, create an occurrence
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        Event occurrence = createOccurrence(currentDate);
        occurrences.add(occurrence);
        count++;
      }
      
      // Move to the next day
      currentDate = currentDate.plusDays(1);
    }
    
    return occurrences;
  }

  /**
   * Gets occurrences of this recurring event between the specified dates (inclusive).
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return a list of occurrences between the specified dates
   */
  public List<Event> getOccurrencesBetween(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }

    List<Event> occurrences = new ArrayList<>();
    LocalDateTime currentDateTime = getStartDateTime();
    LocalDate currentDate = currentDateTime.toLocalDate();

    // Adjust current date to start date if it's later
    if (startDate.isAfter(currentDate)) {
      currentDate = startDate;
      currentDateTime = currentDate.atTime(getStartDateTime().toLocalTime());
    }

    // Determine the end boundary
    LocalDate eventEndDate = endDate;

    // Use the earlier of our calculated end date and the requested end date
    LocalDate effectiveEndDate = (eventEndDate != null && eventEndDate.isBefore(endDate))
            ? eventEndDate : endDate;

    int count = 0;

    // Iterate through dates and collect occurrences
    while (!currentDate.isAfter(effectiveEndDate) && (this.occurrences <= 0
            || count < this.occurrences)) {
      if (repeatDays.contains(currentDateTime.getDayOfWeek())) {
        Event occurrence = createOccurrence(currentDateTime);
        occurrences.add(occurrence);
        count++;
      }
      currentDateTime = currentDateTime.plusDays(1);
      currentDate = currentDateTime.toLocalDate();
    }

    return occurrences;
  }

  // Cache of deterministic IDs to avoid regenerating them for the same date
  private static final java.util.Map<String, UUID> deterministicIdCache = new java.util.HashMap<>();

  private Event createOccurrence(LocalDateTime date) {
    // Create occurrence using UTC times directly
    LocalDateTime startTime = date.withHour(getStartDateTime().getHour())
            .withMinute(getStartDateTime().getMinute())
            .withSecond(getStartDateTime().getSecond());

    Duration duration = Duration.between(getStartDateTime(), getEndDateTime());
    LocalDateTime endTime = startTime.plus(duration);
    
    // Generate a deterministic UUID based on ONLY the recurring event ID and the DATE (not time)
    // This ensures all occurrences on the same day share exactly the same ID, preventing duplicates
    String dateOnlyString = date.toLocalDate().toString();
    String deterministicSeed = recurringId.toString() + "-" + dateOnlyString;
    
    // Use cached UUID if available
    UUID deterministicId;
    if (deterministicIdCache.containsKey(deterministicSeed)) {
      deterministicId = deterministicIdCache.get(deterministicSeed);
    } else {
      deterministicId = UUID.nameUUIDFromBytes(deterministicSeed.getBytes(StandardCharsets.UTF_8));
      deterministicIdCache.put(deterministicSeed, deterministicId);
    }
    
    Event occurrence = new Event(
            getSubject(),
            startTime,
            endTime,
            getDescription(),
            getLocation(),
            isPublic()
    );
    
    // Use reflection to set the id field on the new occurrence
    try {
      Field idField = Event.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(occurrence, deterministicId);
      
      // Also set recurringId field if it exists
      try {
        Field recurringIdField = Event.class.getDeclaredField("recurringId");
        recurringIdField.setAccessible(true);
        recurringIdField.set(occurrence, recurringId);
      } catch (NoSuchFieldException nsfe) {
        // Ignore if the field doesn't exist
      }
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to set deterministic ID on occurrence: " + e.getMessage());
    }
    
    return occurrence;
  }

  /**
   * Gets the ID specific to this recurring event series.
   *
   * @return the recurring event series ID
   */
  public UUID getRecurringId() {
    return recurringId;
  }

  /**
   * Gets the set of days on which this event repeats.
   *
   * @return a set of days of the week
   */
  public Set<DayOfWeek> getRepeatDays() {
    return repeatDays;
  }

  /**
   * Gets the number of occurrences of this recurring event.
   *
   * @return the number of occurrences, or -1 if based on end date
   */
  public int getOccurrences() {
    return occurrences;
  }

  /**
   * Gets the end date of this recurring event.
   *
   * @return the end date, or null if based on occurrences
   */
  /**
   * Gets the end date of the recurring event.
   *
   * @return the end date, or null if based on occurrences
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  /**
   * Gets the end date of the recurring event (alias for getEndDate()).
   *
   * @return the end date, or null if based on occurrences
   */
  public LocalDate getUntilDate() {
    return endDate;
  }
}
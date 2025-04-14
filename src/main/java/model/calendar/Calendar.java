package model.calendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import model.calendar.iterator.ConsolidatedIterator;
import model.event.Event;
import model.event.EventPropertyUpdater;
import model.event.RecurringEvent;
import model.exceptions.CalendarExceptions.ConflictingEventException;
import model.export.IDataExporter;
import utilities.DateTimeUtil;
import utilities.TimeZoneHandler;

/**
 * Implementation of the ICalendar interface that manages a calendar's events and operations. This
 * class provides comprehensive functionality for managing both single and recurring events,
 * including creation, modification, querying, and conflict detection.
 *
 * <p>Key Features: Manages both single and recurring events, Supports event conflict detection,
 * Provides flexible event querying (by date, range, or custom filters), Handles event property
 * updates, Supports calendar data export to CSV
 *
 * <p>The calendar maintains separate collections for single and recurring events,
 * with UUID-based indexing for efficient event lookup.
 */
public class Calendar implements ICalendar {

  private final List<Event> events;
  private final List<RecurringEvent> recurringEvents;
  private final Map<UUID, Event> eventById;
  private final Map<UUID, RecurringEvent> recurringEventById;
  private String name;
  private TimeZone timezone;
  private final Map<String, EventPropertyUpdater> propertyUpdaters;
  private final TimeZoneHandler timezoneHandler;

  /**
   * Constructs a new Calendar instance with default settings. Initializes empty event collections
   * and sets default values: - Name: "Default" - Timezone: "America/New_York" - Empty events and
   * recurring events lists - Empty event ID mappings - Initialized property updaters for event
   * modification
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.eventById = new HashMap<>();
    this.recurringEventById = new HashMap<>();
    this.name = "Default";
    this.timezone = TimeZone.getTimeZone("America/New_York");

    this.propertyUpdaters = new HashMap<>();
    initializePropertyUpdaters();
    this.timezoneHandler = new TimeZoneHandler();
  }

  /**
   * Constructs a new Calendar with the specified name and timezone. Initializes the event
   * collections, maps for accessing events by ID, property updaters for event modifications, and
   * timezone handling.
   *
   * @param name     the name of the calendar
   * @param timezone the timezone identifier for this calendar (e.g., "America/New_York")
   */
  public Calendar(String name, String timezone) {
    this.name = name;
    this.timezone = TimeZone.getTimeZone(timezone);
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.eventById = new HashMap<>();
    this.recurringEventById = new HashMap<>();

    this.propertyUpdaters = new HashMap<>();
    initializePropertyUpdaters();
    this.timezoneHandler = new TimeZoneHandler();
  }

  /**
   * Adds an event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline whether to automatically decline conflicting events
   * @return true if the event was added successfully
   * @throws ConflictingEventException if there is a conflict and autoDecline is false
   */
  @Override
  public boolean addEvent(Event event, boolean autoDecline) throws ConflictingEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    LocalDateTime startUTC = timezoneHandler.convertToUTC(event.getStartDateTime(),
        timezone.getID());
    LocalDateTime endUTC = timezoneHandler.convertToUTC(event.getEndDateTime(), timezone.getID());

    Event utcEvent = new Event(
        event.getSubject(),
        startUTC,
        endUTC,
        event.getDescription(),
        event.getLocation(),
        event.isPublic()
    );

    if (!autoDecline) {
      for (Event existingEvent : events) {
        if (utcEvent.conflictsWith(existingEvent)) {
          throw new ConflictingEventException("Event conflicts with existing event");
        }
      }
    }

    events.add(utcEvent);
    // Store the event in the eventById map for future lookup
    eventById.put(utcEvent.getId(), utcEvent);
    System.out.println("[DEBUG] Calendar.addEvent - Added event to map with ID: "
        + utcEvent.getId());

    return true;
  }

  /**
   * Adds a recurring event to the calendar with conflict checking for all occurrences.
   *
   * <p>The method performs the following steps:
   * 1. Validates the recurring event is not null 2. Generates all event occurrences 3. Checks each
   * occurrence for conflicts 4. If autoDecline is true, throws exception on any conflict 5. If
   * autoDecline is false, returns false on any conflict 6. Adds the recurring event and all its
   * occurrences to respective collections
   *
   * @param recurringEvent The recurring event to add, must not be null
   * @param autoDecline    If true, throws exception on conflict; if false, returns false
   * @return true if event was added successfully, false otherwise
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline)
      throws ConflictingEventException {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    List<Event> occurrences = recurringEvent.getAllOccurrences();

    List<Event> utcOccurrences = new ArrayList<>();
    for (Event occurrence : occurrences) {
      LocalDateTime startUTC = timezoneHandler.convertToUTC(occurrence.getStartDateTime(),
          timezone.getID());
      LocalDateTime endUTC = timezoneHandler.convertToUTC(occurrence.getEndDateTime(),
          timezone.getID());
      Event utcOccurrence = new Event(
          occurrence.getSubject(),
          startUTC,
          endUTC,
          occurrence.getDescription(),
          occurrence.getLocation(),
          occurrence.isPublic()
      );
      utcOccurrences.add(utcOccurrence);
    }

    for (Event utcOccurrence : utcOccurrences) {
      if (hasConflict(utcOccurrence)) {
        if (autoDecline) {
          throw new ConflictingEventException(
              "Cannot add recurring event '" + recurringEvent.getSubject()
                  + "' due to conflict with an existing event");
        }
        return false;
      }
    }

    recurringEvents.add(recurringEvent);
    recurringEventById.put(recurringEvent.getId(), recurringEvent);

    for (Event utcOccurrence : utcOccurrences) {
      events.add(utcOccurrence);
      eventById.put(utcOccurrence.getId(), utcOccurrence);
    }

    return true;
  }

  /**
   * Creates a recurring event that repeats on specified weekdays until a given end date.
   *
   * <p>Example weekdays format: "MWF" for Monday, Wednesday, Friday
   * Valid weekday codes: M (Monday), T (Tuesday), W (Wednesday), R (Thursday), F (Friday), S
   * (Saturday), U (Sunday)
   *
   * @param name        Event name/subject
   * @param start       Start date and time of the first occurrence
   * @param end         End date and time of the first occurrence
   * @param weekdays    String specifying which days of the week the event repeats on
   * @param untilDate   The last date on which the event can occur
   * @param autoDecline If true, throws exception on conflict; if false, returns false
   * @return true if event was created successfully
   * @throws ConflictingEventException if autoDecline is true and any occurrence conflicts
   */
  @Override
  public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
      String weekdays, LocalDate untilDate,
      boolean autoDecline) throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, start, end,
          repeatDays).isPublic(true).endDate(untilDate).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
      int occurrences,
      boolean autoDecline, String description,
      String location, boolean isPublic)
      throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay,
          repeatDays).description(description).location(location).isPublic(isPublic)
          .occurrences(occurrences).isAllDay(true).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
      LocalDate untilDate, boolean autoDecline,
      String description, String location,
      boolean isPublic)
      throws ConflictingEventException {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent.Builder(name, startOfDay, endOfDay,
          repeatDays).description(description).location(location).isPublic(isPublic)
          .endDate(untilDate).isAllDay(true).build();

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Finds an event by its subject and start date/time.
   *
   * @param subject       The event subject to search for
   * @param startDateTime The exact start date and time to match (in calendar's timezone)
   * @return The matching Event object, or null if no match is found
   */
  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      throw new IllegalArgumentException("Subject and start date/time cannot be null");
    }

    LocalDateTime utcStartTime = timezoneHandler.convertToUTC(startDateTime, timezone.getID());

    Event event = events.stream()
        .filter(e -> e.getSubject().equals(subject) &&
            e.getStartDateTime().equals(utcStartTime))
        .findFirst()
        .orElse(null);

    if (event != null) {
      return event;
    }

    for (RecurringEvent recurringEvent : recurringEvents) {
      if (recurringEvent.getSubject().equals(subject)) {
        List<Event> occurrences = recurringEvent.getAllOccurrences();
        for (Event occurrence : occurrences) {
          if (occurrence.getStartDateTime().equals(utcStartTime)) {
            return occurrence;
          }
        }
      }
    }

    return null;
  }

  /**
   * Retrieves all events in calendar.
   *
   * @return List of all events.
   */
  @Override
  public List<Event> getAllEvents() {
    ConsolidatedIterator.IEventIterator iterator = getEventIterator();
    List<Event> allEvents = new ArrayList<>();

    while (iterator.hasNext()) {
      allEvents.add(iterator.next());
    }

    return allEvents;
  }

  /**
   * Edits a specific event in calendar.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit (in calendar's timezone)
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return true if the operation is successful
   */
  @Override
  public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    Event eventToEdit = findEvent(subject, startDateTime);

    if (eventToEdit == null) {
      return false;
    }

    return updateEventProperty(eventToEdit, property, newValue);
  }

  /**
   * Edits a specific event in calendar for a given date.
   *
   * @param subject       the subject of the recurring events to edit
   * @param startDateTime the start date/time to begin editing from
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return the number of events edited
   */
  @Override
  public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    int count = 0;

    List<Event> matchingEvents = events.stream().filter(e -> e.getSubject().equals(subject)
        && !e.getStartDateTime().isBefore(startDateTime)).collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Edits multiple events at once.
   *
   * @param subject  the subject of the events to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return number of occurrences edited
   */
  @Override
  public int editAllEvents(String subject, String property, String newValue) {
    int count = 0;

    List<Event> matchingEvents = events.stream().filter(e -> e.getSubject().equals(subject))
        .collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Retrieves all recurring events in the calendar.
   *
   * @return a list of all recurring events
   */
  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  /**
   * Export all events of the calendar to a CSV file.
   *
   * @param filePath the path where the CSV file should be created
   * @return filePath of exported csv
   * @throws IOException if an I/O error occurs
   */
  @Override
  public String exportData(String filePath, IDataExporter exporter) throws IOException {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty");
    }
    if (exporter == null) {
      throw new IllegalArgumentException("Exporter cannot be null");
    }
    return exporter.export(filePath, events);
  }

  /**
   * Checks if an event conflicts with any existing event in the calendar.
   *
   * @param event the event to check for conflicts
   * @return true if there is a conflict, false otherwise
   */
  private boolean hasConflict(Event event) {
    EventFilter conflictFilter = existingEvent -> event.conflictsWith(existingEvent);
    ConsolidatedIterator.IEventIterator iterator = getFilteredEventIterator(conflictFilter);
    return iterator.hasNext();
  }

  /**
   * Updates a property of an event.
   *
   * @param event    the event to update
   * @param property the property to update
   * @param newValue the new value
   * @return true if the update was successful
   */
  private boolean updateEventProperty(Event event, String property, String newValue) {
    EventPropertyUpdater updater = propertyUpdaters.get(property.toLowerCase());
    if (updater == null) {
      return false;
    }

    try {
      return updater.update(event, newValue);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to get events for
   * @return a list of events on the specified date
   */
  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    Map<UUID, Event> eventsOnDateById = new HashMap<>();

    // Get events for a wider date range to catch timezone-affected events
    // We need to look at the day before and after to catch events that might be
    // on a different day in UTC
    LocalDate dayBefore = date.minusDays(1);
    LocalDate dayAfter = date.plusDays(1);

    // First get all events in the expanded date range
    List<Event> allEventsInRange = getEventsInRange(dayBefore, dayAfter);

    // Filter events based on both local date and UTC date
    LocalDate localDate = date;  // Target date in local timezone

    for (Event event : allEventsInRange) {
      // For events happening at day boundaries, we need to be lenient in our filtering
      if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
        LocalDate eventEndDate = event.getEndDateTime().toLocalDate();

        // Event is related to our date if:
        // 1. The event starts or ends on our target date (classic case)
        if (eventStartDate.equals(localDate) || eventEndDate.equals(localDate)) {
          eventsOnDateById.put(event.getId(), event);
        }
        // 2. The event spans across our target date (less common)
        else if (eventStartDate.isBefore(localDate) && eventEndDate.isAfter(localDate)) {
          eventsOnDateById.put(event.getId(), event);
        }
        // 3. Special case: when an event starts VERY late (e.g., 8pm-11pm), it might be stored
        // as midnight-3am the next day in UTC. So events at the "start" of the next day
        // might actually belong to our target date.
        else if (eventStartDate.equals(localDate.plusDays(1)) &&
            event.getStartDateTime().getHour() < 6) { // Early hours of next day
          eventsOnDateById.put(event.getId(), event);
        }
        // 4. Special case: when an event ends VERY early (e.g., 11pm-3am), it might
        // end on our target date. So events that end early on our target date might
        // actually be from the previous day.
        else if (eventEndDate.equals(localDate) &&
            event.getEndDateTime().getHour() < 6) { // Early hours of target day
          eventsOnDateById.put(event.getId(), event);
        }
        // 5. Special case for events that start exactly at midnight
        else if (eventStartDate.equals(localDate) &&
            event.getStartDateTime().getHour() == 0 &&
            event.getStartDateTime().getMinute() == 0) {
          // Always include events that start exactly at midnight of the target date
          eventsOnDateById.put(event.getId(), event);
        }
        // 6. Also check the day before for events crossing midnight
        else if (eventStartDate.equals(localDate.minusDays(1)) &&
            eventEndDate.equals(localDate) ||
            eventEndDate.isAfter(localDate)) {
          // For events that start the day before and cross midnight
          if (event.getStartDateTime().getHour() >= 23 ||
              event.getEndDateTime().getHour() <= 1) {
            eventsOnDateById.put(event.getId(), event);
          }
        }
      } else if (event.getDate() != null && event.getDate().equals(localDate)) {
        eventsOnDateById.put(event.getId(), event);
      }
    }

    // Handle recurring events
    for (RecurringEvent recurringEvent : this.recurringEvents) {
      List<Event> occurrences = recurringEvent.getOccurrencesBetween(date, date);
      for (Event occurrence : occurrences) {
        eventsOnDateById.put(occurrence.getId(), occurrence);
      }
    }

    return new ArrayList<>(eventsOnDateById.values());
  }

  /**
   * Gets all events in a date range.
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   * @return a list of events within the date range
   */
  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Dates cannot be null");
    }

    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }

    Map<UUID, Event> eventsInRangeById = new HashMap<>();

    // Use a more comprehensive filter that checks for any overlap with the date range
    EventFilter dateRangeFilter = event -> {
      if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
        LocalDate eventEndDate = event.getEndDateTime().toLocalDate();

        // Event overlaps with range if:
        // 1. Starts within the range, or
        // 2. Ends within the range, or
        // 3. Completely spans the range (starts before and ends after)
        boolean startsInRange =
            !eventStartDate.isBefore(startDate) && !eventStartDate.isAfter(endDate);
        boolean endsInRange = !eventEndDate.isBefore(startDate) && !eventEndDate.isAfter(endDate);
        boolean spansRange = eventStartDate.isBefore(startDate) && eventEndDate.isAfter(endDate);

        return startsInRange || endsInRange || spansRange;
      }
      return false;
    };

    ConsolidatedIterator.IEventIterator iterator = getFilteredEventIterator(dateRangeFilter);
    while (iterator.hasNext()) {
      Event event = iterator.next();
      eventsInRangeById.put(event.getId(), event);
    }

    // Process recurring events day by day
    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      for (RecurringEvent recurringEvent : this.recurringEvents) {
        List<Event> occurrences = recurringEvent.getOccurrencesBetween(currentDate, currentDate);
        for (Event occurrence : occurrences) {
          eventsInRangeById.put(occurrence.getId(), occurrence);
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    return new ArrayList<>(eventsInRangeById.values());
  }

  /**
   * Gets the name of this calendar.
   *
   * @return the calendar name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the timezone of this calendar.
   *
   * @return the timezone
   */
  @Override
  public TimeZone getTimeZone() {
    return timezone;
  }

  /**
   * Initializes the map of property updaters with lambda expressions for each property.
   */
  private void initializePropertyUpdaters() {
    EventPropertyUpdater subjectUpdater = (event, value) -> {
      try {
        event.setSubject(value);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    };

    propertyUpdaters.put("subject", subjectUpdater);
    propertyUpdaters.put("name", subjectUpdater);

    propertyUpdaters.put("description", (event, value) -> {
      event.setDescription(value);
      return true;
    });

    propertyUpdaters.put("location", (event, value) -> {
      event.setLocation(value);
      return true;
    });

    EventPropertyUpdater startTimeUpdater = (event, value) -> {
      try {
        LocalDateTime newStartTime;
        if (value.contains("T")) {
          newStartTime = DateTimeUtil.parseDateTime(value);
        } else {
          LocalTime newTime = LocalTime.parse(value);
          newStartTime = LocalDateTime.of(event.getStartDateTime().toLocalDate(), newTime);
        }
        event.setStartDateTime(newStartTime);
        return true;
      } catch (Exception e) {
        return false;
      }
    };
    propertyUpdaters.put("start", startTimeUpdater);
    propertyUpdaters.put("starttime", startTimeUpdater);
    propertyUpdaters.put("startdatetime", startTimeUpdater);

    EventPropertyUpdater endTimeUpdater = (event, value) -> {
      try {
        LocalDateTime newEndTime;
        if (value.contains("T")) {
          newEndTime = DateTimeUtil.parseDateTime(value);
        } else {
          LocalTime newTime = LocalTime.parse(value);
          newEndTime = LocalDateTime.of(event.getEndDateTime().toLocalDate(), newTime);
        }
        event.setEndDateTime(newEndTime);
        return true;
      } catch (Exception e) {
        return false;
      }
    };
    propertyUpdaters.put("end", endTimeUpdater);
    propertyUpdaters.put("endtime", endTimeUpdater);
    propertyUpdaters.put("enddatetime", endTimeUpdater);

    EventPropertyUpdater visibilityUpdater = (event, value) -> {
      boolean isPublic = value.equalsIgnoreCase("public")
          || value.equalsIgnoreCase("true");
      event.setPublic(isPublic);
      return true;
    };
    propertyUpdaters.put("visibility", visibilityUpdater);
    propertyUpdaters.put("ispublic", visibilityUpdater);
    propertyUpdaters.put("public", visibilityUpdater);

    propertyUpdaters.put("private", (event, value) -> {
      boolean isPrivate = value.equalsIgnoreCase("true")
          || value.equalsIgnoreCase("private");
      event.setPublic(!isPrivate);
      return true;
    });
  }

  /**
   * Gets events that match a specific filter.
   *
   * @param filter the filter to apply
   * @return a list of events that match the filter
   */
  public List<Event> getFilteredEvents(EventFilter filter) {
    // Use the iterator pattern to filter events
    ConsolidatedIterator.IEventIterator iterator = getFilteredEventIterator(filter);
    List<Event> result = new ArrayList<>();

    while (iterator.hasNext()) {
      result.add(iterator.next());
    }

    return result;
  }

  /**
   * Gets an iterator for all events in this calendar. This includes both regular and recurring
   * events.
   *
   * @return an iterator for all events
   */
  public ConsolidatedIterator.IEventIterator getEventIterator() {
    List<ConsolidatedIterator.IEventIterator> iterators = new ArrayList<>();
    iterators.add(ConsolidatedIterator.forEvents(events));
    iterators.add(ConsolidatedIterator.forRecurringEvents(recurringEvents,
        LocalDate.now(), LocalDate.now().plusYears(1)));
    return ConsolidatedIterator.composite(iterators);
  }

  /**
   * Gets an iterator for events that match a specific filter.
   *
   * @param filter the filter to apply
   * @return a filtered iterator
   */
  public ConsolidatedIterator.IEventIterator getFilteredEventIterator(EventFilter filter) {
    return ConsolidatedIterator.withFilter(getEventIterator(), filter);
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    TimeZoneHandler handler = new TimeZoneHandler();
    String systemTimezone = handler.getSystemDefaultTimezone();
    LocalDateTime utcDateTime = handler.convertToUTC(dateTime, systemTimezone);

    for (Event event : events) {
      if (isTimeWithinEventRange(utcDateTime, event)) {
        return true;
      }
    }

    for (RecurringEvent recurringEvent : recurringEvents) {
      if (isRecurringEventActiveAt(utcDateTime, recurringEvent)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Helper method to check if a given time falls within an event's time range.
   *
   * @param dateTime The time to check
   * @param event    The event to check against
   * @return true if the time is within the event's range
   */
  private boolean isTimeWithinEventRange(LocalDateTime dateTime, Event event) {
    if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
      return false;
    }

    // Check if the dateTime is within the event time range (inclusive of both start and end)
    return (dateTime.isEqual(event.getStartDateTime())
        || dateTime.isAfter(event.getStartDateTime()))
        && (dateTime.isEqual(event.getEndDateTime())
        || dateTime.isBefore(event.getEndDateTime()));
  }

  /**
   * Helper method to check if a recurring event is active at the given time.
   *
   * @param dateTime       The time to check
   * @param recurringEvent The recurring event to check against
   * @return true if the recurring event is active at the specified time
   */
  private boolean isRecurringEventActiveAt(LocalDateTime dateTime, RecurringEvent recurringEvent) {
    LocalDate targetDate = dateTime.toLocalDate();
    DayOfWeek targetDay = targetDate.getDayOfWeek();

    if (!recurringEvent.getRepeatDays().contains(targetDay)) {
      return false;
    }

    LocalDate recurringStartDate = recurringEvent.getStartDateTime().toLocalDate();
    if (targetDate.isBefore(recurringStartDate)) {
      return false;
    }

    if (recurringEvent.getEndDate() != null && targetDate.isAfter(recurringEvent.getEndDate())) {
      return false;
    }

    LocalTime targetTime = dateTime.toLocalTime();
    LocalTime eventStartTime = recurringEvent.getStartDateTime().toLocalTime();
    LocalTime eventEndTime = recurringEvent.getEndDateTime().toLocalTime();

    return (targetTime.equals(eventStartTime) || targetTime.isAfter(eventStartTime))
        && targetTime.isBefore(eventEndTime);
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Updates an existing event with a new version using the event's UUID.
   *
   * @param eventId      The UUID of the event to update
   * @param updatedEvent The new version of the event
   * @return true if the event was successfully updated, false otherwise
   * @throws ConflictingEventException if the updated event conflicts with existing events
   */
  @Override
  public boolean updateEvent(UUID eventId, Event updatedEvent) throws ConflictingEventException {
    if (eventId == null || updatedEvent == null) {
      System.out.println("[ERROR] Calendar.updateEvent - Null eventId or updatedEvent");
      return false;
    }

    System.out.println("[DEBUG] Calendar.updateEvent - Updating event with ID: " + eventId);
    System.out.println("[DEBUG] Calendar.updateEvent - Updated event details: Subject="
        + updatedEvent.getSubject()
        + ", Start=" + updatedEvent.getStartDateTime()
        + ", End=" + updatedEvent.getEndDateTime()
        + ", Location=" + updatedEvent.getLocation());

    Event existingEvent = eventById.get(eventId);
    if (existingEvent == null) {
      System.out.println("[ERROR] Calendar.updateEvent - Event not found with ID: " + eventId);
      return false; // Event not found
    }

    System.out.println("[DEBUG] Calendar.updateEvent - Found existing event: "
        + existingEvent.getSubject());

    // Store the existing event temporarily and remove it from collections
    events.remove(existingEvent);
    eventById.remove(eventId);

    try {
      // Check for conflicts with the updated event
      if (hasConflict(updatedEvent)) {
        // Restore the original event if there's a conflict
        events.add(existingEvent);
        eventById.put(eventId, existingEvent);
        System.out.println("[ERROR] Calendar.updateEvent - Conflict with existing events");
        throw new ConflictingEventException("The updated event conflicts with existing events");
      }

      // Use the updated event directly, but ensure we preserve the original ID
      Event newEvent = new Event(
          eventId, // Use the original event ID directly
          updatedEvent.getSubject(),
          updatedEvent.getStartDateTime(),
          updatedEvent.getEndDateTime(),
          updatedEvent.getDescription(),
          updatedEvent.getLocation(),
          updatedEvent.isPublic(),
          updatedEvent.isAllDay()
      );

      System.out.println("[DEBUG] Calendar.updateEvent - Created new event object: "
          + newEvent.getSubject()
          + ", ID=" + newEvent.getId()
          + ", Start=" + newEvent.getStartDateTime()
          + ", End=" + newEvent.getEndDateTime());

      // Add the updated event
      events.add(newEvent);
      eventById.put(eventId, newEvent);

      return true;
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      System.out.println("[ERROR] Exception in Calendar.updateEvent: " + e.getMessage());
      e.printStackTrace();
      events.add(existingEvent);
      eventById.put(eventId, existingEvent);
      return false;
    }
  }

  /**
   * Sets the name of the calendar.
   *
   * @param name the new name for the calendar
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the timezone of the calendar and updates all event times accordingly. When the timezone
   * changes, all events are converted to maintain the same wall-clock time in the new timezone.
   *
   * @param timezone the new timezone for the calendar
   */
  public void setTimezone(String timezone) {
    TimeZone oldTimezone = this.timezone;
    TimeZone newTimezone = TimeZone.getTimeZone(timezone);

    // Skip conversion if timezone isn't actually changing
    if (oldTimezone.getID().equals(newTimezone.getID())) {
      return;
    }

    // Update all single events
    for (Event event : events) {
      LocalDateTime oldStart = event.getStartDateTime();
      LocalDateTime oldEnd = event.getEndDateTime();

      // Convert from old timezone to new timezone
      ZoneId oldZone = oldTimezone.toZoneId();
      ZoneId newZone = newTimezone.toZoneId();

      // Convert keeping the same wall clock time
      ZonedDateTime zonedStart = oldStart.atZone(oldZone);
      ZonedDateTime zonedEnd = oldEnd.atZone(oldZone);

      LocalDateTime newStart = zonedStart.withZoneSameLocal(newZone).toLocalDateTime();
      LocalDateTime newEnd = zonedEnd.withZoneSameLocal(newZone).toLocalDateTime();

      // Update the event
      event.setStartDateTime(newStart);
      event.setEndDateTime(newEnd);
    }

    // Update all recurring events
    for (RecurringEvent recurringEvent : recurringEvents) {
      LocalDateTime oldStart = recurringEvent.getStartDateTime();
      LocalDateTime oldEnd = recurringEvent.getEndDateTime();

      // Convert from old timezone to new timezone
      ZoneId oldZone = oldTimezone.toZoneId();
      ZoneId newZone = newTimezone.toZoneId();

      // Convert keeping the same wall clock time
      ZonedDateTime zonedStart = oldStart.atZone(oldZone);
      ZonedDateTime zonedEnd = oldEnd.atZone(oldZone);

      LocalDateTime newStart = zonedStart.withZoneSameLocal(newZone).toLocalDateTime();
      LocalDateTime newEnd = zonedEnd.withZoneSameLocal(newZone).toLocalDateTime();

      // Update the recurring event
      recurringEvent.setStartDateTime(newStart);
      recurringEvent.setEndDateTime(newEnd);
    }

    // Finally update the calendar's timezone
    this.timezone = newTimezone;
    System.out.println(
        "Calendar timezone changed from " + oldTimezone.getID() + " to " + newTimezone.getID());
  }
}
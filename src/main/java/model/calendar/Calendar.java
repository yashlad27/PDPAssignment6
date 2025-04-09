package model.calendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import model.exceptions.ConflictingEventException;
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

    LocalDateTime startUTC = timezoneHandler.convertToUTC(event.getStartDateTime(), timezone.getID());
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
        if (existingEvent.getStartDateTime().isBefore(utcEvent.getEndDateTime()) &&
                utcEvent.getStartDateTime().isBefore(existingEvent.getEndDateTime())) {
          throw new ConflictingEventException("Event conflicts with existing event");
        }
      }
    }

    events.add(utcEvent);
    // Store the event in the eventById map for future lookup
    eventById.put(utcEvent.getId(), utcEvent);
    System.out.println("[DEBUG] Calendar.addEvent - Added event to map with ID: " + utcEvent.getId());
    
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
      LocalDateTime startUTC = timezoneHandler.convertToUTC(occurrence.getStartDateTime(), timezone.getID());
      LocalDateTime endUTC = timezoneHandler.convertToUTC(occurrence.getEndDateTime(), timezone.getID());
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
                                           String weekdays, LocalDate untilDate, boolean autoDecline) throws ConflictingEventException {
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

    List<Event> matchingEvents = events.stream().filter(
                    e -> e.getSubject().equals(subject) && !e.getStartDateTime().isBefore(startDateTime))
            .collect(Collectors.toList());

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
    
    // Create a map to track events by ID to avoid duplicates
    Map<UUID, Event> eventsOnDateById = new HashMap<>();
    
    // Get regular events for the date
    List<Event> regularEvents = getFilteredEvents(event -> {
      if (event.getStartDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();

        if (event.getEndDateTime() != null) {
          LocalDate eventEndDate = event.getEndDateTime().toLocalDate();
          return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
        } else {
          return eventStartDate.equals(date);
        }
      } else if (event.getDate() != null) {
        return event.getDate().equals(date);
      }
      return false;
    });
    
    // Add regular events to the map by ID to avoid duplicates
    for (Event event : regularEvents) {
      eventsOnDateById.put(event.getId(), event);
    }
    
    // Also check for recurring event occurrences on this date
    for (RecurringEvent recurringEvent : this.recurringEvents) {
      System.out.println("[DEBUG] Checking recurring event for date " + date + ": " + recurringEvent.getSubject());
      
      // Get occurrences just for the specified date
      List<Event> occurrences = recurringEvent.getOccurrencesBetween(date, date);
      System.out.println("[DEBUG] Found " + occurrences.size() + " occurrences of recurring event " + 
                         recurringEvent.getSubject() + " on " + date);
      
      // Add occurrences to the map by ID to avoid duplicates
      for (Event occurrence : occurrences) {
        eventsOnDateById.put(occurrence.getId(), occurrence);
      }
    }
    
    // Return the deduplicated events as a list
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

    // Create a map to track events by ID to avoid duplicates
    Map<UUID, Event> eventsInRangeById = new HashMap<>();

    // Use the iterator pattern to get regular events between dates
    EventFilter dateRangeFilter = event -> {
      if (event.getStartDateTime() != null) {
        LocalDate eventDate = event.getStartDateTime().toLocalDate();
        return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
      }
      return false;
    };

    // Get regular events in the date range
    ConsolidatedIterator.IEventIterator iterator = getFilteredEventIterator(dateRangeFilter);
    while (iterator.hasNext()) {
      Event event = iterator.next();
      eventsInRangeById.put(event.getId(), event);
    }
    
    // Check each recurring event for occurrences in the range
    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      for (RecurringEvent recurringEvent : this.recurringEvents) {
        // Get occurrences for the current day
        List<Event> occurrences = recurringEvent.getOccurrencesBetween(currentDate, currentDate);
        
        // Add each occurrence to the map by ID to avoid duplicates
        for (Event occurrence : occurrences) {
          eventsInRangeById.put(occurrence.getId(), occurrence);
        }
      }
      currentDate = currentDate.plusDays(1);
    }

    // Return the deduplicated list of events
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
   * Gets an iterator for all events in this calendar.
   * This includes both regular and recurring events.
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

    // First check if there are any regular events at this time
    for (Event event : events) {
      if (isTimeWithinEventRange(dateTime, event)) {
        return true;
      }
    }
    
    // Then check all recurring events
    for (RecurringEvent recurringEvent : recurringEvents) {
      if (isRecurringEventActiveAt(dateTime, recurringEvent)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Helper method to check if a given time falls within an event's time range.
   *
   * @param dateTime The time to check
   * @param event The event to check against
   * @return true if the time is within the event's range
   */
  private boolean isTimeWithinEventRange(LocalDateTime dateTime, Event event) {
    if (event.getStartDateTime() == null || event.getEndDateTime() == null) {
      return false;
    }
    
    // Check if the dateTime is within the event time range (inclusive of start, exclusive of end)
    return (dateTime.isEqual(event.getStartDateTime()) || dateTime.isAfter(event.getStartDateTime()))
           && dateTime.isBefore(event.getEndDateTime());
  }
  
  /**
   * Helper method to check if a recurring event is active at the given time.
   *
   * @param dateTime The time to check
   * @param recurringEvent The recurring event to check against
   * @return true if the recurring event is active at the specified time
   */
  private boolean isRecurringEventActiveAt(LocalDateTime dateTime, RecurringEvent recurringEvent) {
    LocalDate targetDate = dateTime.toLocalDate();
    DayOfWeek targetDay = targetDate.getDayOfWeek();
    
    // First check if this event repeats on this day of the week
    if (!recurringEvent.getRepeatDays().contains(targetDay)) {
      return false;
    }
    
    // Check date is on or after the start date of the recurring event
    LocalDate recurringStartDate = recurringEvent.getStartDateTime().toLocalDate();
    if (targetDate.isBefore(recurringStartDate)) {
      return false;
    }
    
    // Check if we've passed the end date (if one is specified)
    if (recurringEvent.getEndDate() != null && targetDate.isAfter(recurringEvent.getEndDate())) {
      return false;
    }
    
    // Check if the time falls within the event's time range
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
    System.out.println("[DEBUG] Calendar.updateEvent - Updated event details: Subject=" + updatedEvent.getSubject() + 
                       ", Start=" + updatedEvent.getStartDateTime() + 
                       ", End=" + updatedEvent.getEndDateTime() + 
                       ", Location=" + updatedEvent.getLocation());

    Event existingEvent = eventById.get(eventId);
    if (existingEvent == null) {
      System.out.println("[ERROR] Calendar.updateEvent - Event not found with ID: " + eventId);
      return false; // Event not found
    }

    System.out.println("[DEBUG] Calendar.updateEvent - Found existing event: " + existingEvent.getSubject());

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
              updatedEvent.isPublic()
      );

      System.out.println("[DEBUG] Calendar.updateEvent - Created new event object: " + newEvent.getSubject() + 
                         ", ID=" + newEvent.getId() + 
                         ", Start=" + newEvent.getStartDateTime() + 
                         ", End=" + newEvent.getEndDateTime());

      // Add the updated event
      events.add(newEvent);
      eventById.put(eventId, newEvent);

      return true;
    } catch (ConflictingEventException e) {
      throw e;
    } catch (Exception e) {
      System.out.println("[ERROR] Exception in Calendar.updateEvent: " + e.getMessage());
      e.printStackTrace();
      // Put the original event back if there's any other error
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
   * Sets the timezone of the calendar.
   *
   * @param timezone the new timezone for the calendar
   */
  public void setTimezone(String timezone) {
    this.timezone = TimeZone.getTimeZone(timezone);
  }
}
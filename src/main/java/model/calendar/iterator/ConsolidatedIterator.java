package model.calendar.iterator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import model.calendar.Calendar;
import model.calendar.EventFilter;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * A unified iterator implementation that consolidates all iterator functionality
 * into a single class. This replaces the separate interfaces and implementations
 * with a more cohesive, flexible solution that maintains SOLID principles through
 * clear separation of concerns within the class structure.
 */
public class ConsolidatedIterator {

  /**
   * Defines the iterator type enum for internal type differentiation.
   */
  public enum IteratorType {
    CALENDAR,
    EVENT_REGULAR,
    EVENT_RECURRING,
    EVENT_FILTERED,
    EVENT_COMPOSITE
  }

  /**
   * Interface for calendar iterators. Maintains the original interface contract.
   */
  public interface ICalendarIterator {
    /**
     * Checks if there are more calendars to iterate over.
     *
     * @return true if there are more calendars, false otherwise
     */
    boolean hasNext();

    /**
     * Returns the next calendar in the iteration.
     *
     * @return the next calendar
     * @throws NoSuchElementException if there are no more calendars
     */
    ICalendar next();

    /**
     * Resets the iterator to the beginning of the collection.
     */
    void reset();
  }

  /**
   * Interface for event iterators. Maintains the original interface contract.
   */
  public interface IEventIterator {
    /**
     * Checks if there are more events to iterate over.
     *
     * @return true if there are more events, false otherwise
     */
    boolean hasNext();

    /**
     * Returns the next event in the iteration.
     *
     * @return the next event
     * @throws NoSuchElementException if there are no more events
     */
    Event next();

    /**
     * Resets the iterator to the beginning of the collection.
     */
    void reset();
  }

  /**
   * Implementation of a calendar registry iterator.
   */
  public static class CalendarRegistryIterator implements ICalendarIterator {
    private final List<ICalendar> calendars;
    private int currentIndex = 0;

    /**
     * Creates a new CalendarRegistryIterator.
     *
     * @param calendarMap the map of calendars to iterate over
     */
    public CalendarRegistryIterator(Map<String, Calendar> calendarMap) {
      this.calendars = new ArrayList<>(calendarMap.values());
    }

    @Override
    public boolean hasNext() {
      return currentIndex < calendars.size();
    }

    @Override
    public ICalendar next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more calendars to iterate over");
      }
      return calendars.get(currentIndex++);
    }

    @Override
    public void reset() {
      currentIndex = 0;
    }
  }

  /**
   * Implementation of a regular event iterator.
   */
  public static class RegularEventIterator implements IEventIterator {
    private final List<Event> events;
    private int currentIndex = 0;

    /**
     * Creates a new RegularEventIterator.
     *
     * @param events the list of events to iterate over
     */
    public RegularEventIterator(List<Event> events) {
      this.events = new ArrayList<>(events);
    }

    @Override
    public boolean hasNext() {
      return currentIndex < events.size();
    }

    @Override
    public Event next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more events to iterate over");
      }
      return events.get(currentIndex++);
    }

    @Override
    public void reset() {
      currentIndex = 0;
    }
  }

  /**
   * Implementation of a recurring event iterator that expands recurring events
   * into individual occurrences within a date range.
   */
  public static class RecurringEventIterator implements IEventIterator {
    private final List<RecurringEvent> recurringEvents;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private List<Event> expandedEvents;
    private int currentIndex = 0;

    /**
     * Creates a new RecurringEventIterator.
     *
     * @param recurringEvents the list of recurring events to iterate over
     * @param startDate       the start date of the range
     * @param endDate         the end date of the range
     */
    public RecurringEventIterator(List<RecurringEvent> recurringEvents,
                                  LocalDate startDate,
                                  LocalDate endDate) {
      this.recurringEvents = new ArrayList<>(recurringEvents);
      this.startDate = startDate;
      this.endDate = endDate;
      expandEvents();
    }

    private void expandEvents() {
      expandedEvents = new ArrayList<>();
      for (RecurringEvent recurringEvent : recurringEvents) {
        expandedEvents.addAll(recurringEvent.getOccurrencesBetween(startDate, endDate));
      }
    }

    @Override
    public boolean hasNext() {
      return currentIndex < expandedEvents.size();
    }

    @Override
    public Event next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more recurring events to iterate over");
      }
      return expandedEvents.get(currentIndex++);
    }

    @Override
    public void reset() {
      currentIndex = 0;
    }
  }

  /**
   * Implementation of a filtered event iterator that applies a filter to an underlying iterator.
   */
  public static class FilteredEventIterator implements IEventIterator {
    private final IEventIterator baseIterator;
    private final EventFilter filter;
    private Event nextEvent;

    /**
     * Creates a new FilteredEventIterator.
     *
     * @param baseIterator the underlying iterator to filter
     * @param filter       the filter to apply
     */
    public FilteredEventIterator(IEventIterator baseIterator, EventFilter filter) {
      this.baseIterator = baseIterator;
      this.filter = filter;
      findNext();
    }

    private void findNext() {
      nextEvent = null;
      while (baseIterator.hasNext()) {
        Event event = baseIterator.next();
        if (filter.matches(event)) {
          nextEvent = event;
          break;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return nextEvent != null;
    }

    @Override
    public Event next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more filtered events to iterate over");
      }
      Event result = nextEvent;
      findNext();
      return result;
    }

    @Override
    public void reset() {
      baseIterator.reset();
      findNext();
    }
  }

  /**
   * Implementation of a composite event iterator that combines multiple iterators.
   */
  public static class CompositeEventIterator implements IEventIterator {
    private final List<IEventIterator> iterators;
    private int currentIteratorIndex = 0;

    /**
     * Creates a new CompositeEventIterator.
     *
     * @param iterators the list of iterators to combine
     */
    public CompositeEventIterator(List<IEventIterator> iterators) {
      this.iterators = new ArrayList<>(iterators);
    }

    @Override
    public boolean hasNext() {
      while (currentIteratorIndex < iterators.size()) {
        if (iterators.get(currentIteratorIndex).hasNext()) {
          return true;
        }
        currentIteratorIndex++;
      }
      return false;
    }

    @Override
    public Event next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more events in composite iterator");
      }
      return iterators.get(currentIteratorIndex).next();
    }

    @Override
    public void reset() {
      for (IEventIterator iterator : iterators) {
        iterator.reset();
      }
      currentIteratorIndex = 0;
    }
  }

  /**
   * Creates a calendar registry iterator.
   *
   * @param calendarMap the map of calendars to iterate over
   * @return a new calendar registry iterator
   */
  public static ICalendarIterator forCalendarRegistry(Map<String, Calendar> calendarMap) {
    return new CalendarRegistryIterator(calendarMap);
  }

  /**
   * Creates a regular event iterator.
   *
   * @param events the list of events to iterate over
   * @return a new regular event iterator
   */
  public static IEventIterator forEvents(List<Event> events) {
    return new RegularEventIterator(events);
  }

  /**
   * Creates a recurring event iterator.
   *
   * @param recurringEvents the list of recurring events to iterate over
   * @param startDate       the start date of the range
   * @param endDate         the end date of the range
   * @return a new recurring event iterator
   */
  public static IEventIterator forRecurringEvents(List<RecurringEvent> recurringEvents,
                                                  LocalDate startDate,
                                                  LocalDate endDate) {
    return new RecurringEventIterator(recurringEvents, startDate, endDate);
  }

  /**
   * Creates a filtered event iterator.
   *
   * @param baseIterator the underlying iterator to filter
   * @param filter       the filter to apply
   * @return a new filtered event iterator
   */
  public static IEventIterator withFilter(IEventIterator baseIterator, EventFilter filter) {
    return new FilteredEventIterator(baseIterator, filter);
  }

  /**
   * Creates a composite event iterator.
   *
   * @param iterators the list of iterators to combine
   * @return a new composite event iterator
   */
  public static IEventIterator composite(List<IEventIterator> iterators) {
    return new CompositeEventIterator(iterators);
  }
}

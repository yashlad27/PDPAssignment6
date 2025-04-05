package model.calendar.iterator;

import model.calendar.EventFilter;
import model.event.Event;
import model.event.RecurringEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A consolidated implementation of event iterators that combines the functionality
 * of various iterator types (regular, recurring, filtered, and composite).
 * This class replaces multiple separate iterator implementations with a single class
 * that provides factory methods for creating different types of iterators.
 */
public class ConsolidatedEventIterator implements EventIterator {
    /**
     * Iterator type enumeration.
     */
    public enum IteratorType {
        REGULAR,
        RECURRING,
        FILTERED,
        COMPOSITE
    }

    private final IteratorType type;
    
    // For all iterator types
    private int currentIndex = 0;
    
    // For REGULAR iterator
    private List<Event> events;
    
    // For RECURRING iterator
    private List<RecurringEvent> recurringEvents;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Event> expandedEvents;
    
    // For FILTERED iterator
    private EventIterator baseIterator;
    private EventFilter filter;
    private Event nextEvent;
    
    // For COMPOSITE iterator
    private List<EventIterator> iterators;
    private int currentIteratorIndex = 0;

    /**
     * Private constructor used by factory methods.
     * 
     * @param type the type of iterator to create
     */
    private ConsolidatedEventIterator(IteratorType type) {
        this.type = type;
    }
    
    /**
     * Factory method for creating a regular event iterator.
     * 
     * @param events the list of events to iterate over
     * @return a new consolidated iterator configured for regular events
     */
    public static ConsolidatedEventIterator createRegularIterator(List<Event> events) {
        ConsolidatedEventIterator iterator = new ConsolidatedEventIterator(IteratorType.REGULAR);
        iterator.events = events;
        return iterator;
    }
    
    /**
     * Factory method for creating a recurring event iterator with a date range.
     * 
     * @param recurringEvents the list of recurring events to iterate over
     * @param startDate the start date for expanding recurring events
     * @param endDate the end date for expanding recurring events
     * @return a new consolidated iterator configured for recurring events
     */
    public static ConsolidatedEventIterator createRecurringIterator(
            List<RecurringEvent> recurringEvents, LocalDate startDate, LocalDate endDate) {
        ConsolidatedEventIterator iterator = new ConsolidatedEventIterator(IteratorType.RECURRING);
        iterator.recurringEvents = recurringEvents;
        iterator.startDate = startDate;
        iterator.endDate = endDate;
        iterator.expandEvents();
        return iterator;
    }
    
    /**
     * Factory method for creating a recurring event iterator for the next 30 days.
     * 
     * @param recurringEvents the list of recurring events to iterate over
     * @return a new consolidated iterator configured for recurring events
     */
    public static ConsolidatedEventIterator createRecurringIterator(List<RecurringEvent> recurringEvents) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);
        return createRecurringIterator(recurringEvents, startDate, endDate);
    }
    
    /**
     * Factory method for creating a filtered event iterator.
     * 
     * @param baseIterator the base iterator to filter
     * @param filter the filter to apply
     * @return a new consolidated iterator configured for filtered events
     */
    public static ConsolidatedEventIterator createFilteredIterator(EventIterator baseIterator, EventFilter filter) {
        ConsolidatedEventIterator iterator = new ConsolidatedEventIterator(IteratorType.FILTERED);
        iterator.baseIterator = baseIterator;
        iterator.filter = filter;
        iterator.findNext();
        return iterator;
    }
    
    /**
     * Factory method for creating a composite event iterator.
     * 
     * @param iterators the list of iterators to combine
     * @return a new consolidated iterator configured as a composite iterator
     */
    public static ConsolidatedEventIterator createCompositeIterator(List<EventIterator> iterators) {
        ConsolidatedEventIterator iterator = new ConsolidatedEventIterator(IteratorType.COMPOSITE);
        iterator.iterators = iterators;
        return iterator;
    }
    
    @Override
    public boolean hasNext() {
        switch (type) {
            case REGULAR:
                return currentIndex < events.size();
                
            case RECURRING:
                return currentIndex < expandedEvents.size();
                
            case FILTERED:
                return nextEvent != null;
                
            case COMPOSITE:
                // If current iterator has next, return true
                if (currentIteratorIndex < iterators.size() && 
                    iterators.get(currentIteratorIndex).hasNext()) {
                    return true;
                }
                
                // Otherwise, find the next iterator that has elements
                for (int i = currentIteratorIndex + 1; i < iterators.size(); i++) {
                    if (iterators.get(i).hasNext()) {
                        currentIteratorIndex = i;
                        return true;
                    }
                }
                
                return false;
                
            default:
                return false;
        }
    }
    
    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more events to iterate over");
        }
        
        switch (type) {
            case REGULAR:
                return events.get(currentIndex++);
                
            case RECURRING:
                return expandedEvents.get(currentIndex++);
                
            case FILTERED:
                Event current = nextEvent;
                findNext();
                return current;
                
            case COMPOSITE:
                return iterators.get(currentIteratorIndex).next();
                
            default:
                throw new NoSuchElementException("Invalid iterator type");
        }
    }
    
    @Override
    public void reset() {
        currentIndex = 0;
        currentIteratorIndex = 0;
        
        if (type == IteratorType.FILTERED) {
            baseIterator.reset();
            findNext();
        } else if (type == IteratorType.COMPOSITE) {
            for (EventIterator iterator : iterators) {
                iterator.reset();
            }
        }
    }
    
    /**
     * Expands recurring events into individual occurrences.
     * Used by the recurring event iterator.
     */
    private void expandEvents() {
        expandedEvents = new ArrayList<>();
        
        for (RecurringEvent recurringEvent : recurringEvents) {
            List<Event> occurrences = recurringEvent.getOccurrencesBetween(startDate, endDate);
            expandedEvents.addAll(occurrences);
        }
    }
    
    /**
     * Finds the next event that matches the filter.
     * Used by the filtered event iterator.
     */
    private void findNext() {
        nextEvent = null;
        while (baseIterator.hasNext()) {
            Event candidate = baseIterator.next();
            if (filter.matches(candidate)) {
                nextEvent = candidate;
                break;
            }
        }
    }
}

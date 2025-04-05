package model.calendar.iterator;

import model.calendar.EventFilter;
import model.event.Event;

import java.util.NoSuchElementException;

/**
 * An iterator that filters events based on a specified criteria.
 * This allows for efficient iteration over events that match specific conditions.
 */
public class FilteredEventIterator implements EventIterator {
    private final EventIterator baseIterator;
    private final EventFilter filter;
    private Event nextEvent;
    
    /**
     * Creates a new FilteredEventIterator.
     *
     * @param baseIterator the base iterator to filter
     * @param filter the filter to apply
     */
    public FilteredEventIterator(EventIterator baseIterator, EventFilter filter) {
        this.baseIterator = baseIterator;
        this.filter = filter;
        findNext();
    }
    
    /**
     * Finds the next event that matches the filter.
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
    
    @Override
    public boolean hasNext() {
        return nextEvent != null;
    }
    
    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more events matching the filter");
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

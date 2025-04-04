package model.calendar.iterator;

import model.event.Event;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator implementation for regular (non-recurring) events.
 */
public class RegularEventIterator implements EventIterator {
    private final List<Event> events;
    private int currentIndex = 0;
    
    /**
     * Creates a new RegularEventIterator.
     *
     * @param events the list of events to iterate over
     */
    public RegularEventIterator(List<Event> events) {
        this.events = events;
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

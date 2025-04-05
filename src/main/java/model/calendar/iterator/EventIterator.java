package model.calendar.iterator;

import model.event.Event;

/**
 * Interface for iterating over events in a calendar.
 * Provides a unified way to access different types of events.
 */
public interface EventIterator {
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
     * @throws java.util.NoSuchElementException if there are no more events
     */
    Event next();

    /**
     * Resets the iterator to the beginning of the collection.
     */
    void reset();
}

package model.calendar.iterator;

import model.calendar.ICalendar;

/**
 * Interface for iterating over calendars in a calendar manager.
 * Provides a unified way to access different calendars.
 */
public interface CalendarIterator {
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
     * @throws java.util.NoSuchElementException if there are no more calendars
     */
    ICalendar next();

    /**
     * Resets the iterator to the beginning of the collection.
     */
    void reset();
}

package model.calendar.iterator;

import model.event.Event;
import model.event.RecurringEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator implementation for recurring events.
 * Expands recurring events into their individual occurrences.
 */
public class RecurringEventIterator implements EventIterator {
    private final List<RecurringEvent> recurringEvents;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private List<Event> expandedEvents;
    private int currentIndex = 0;
    
    /**
     * Creates a new RecurringEventIterator that expands recurring events
     * within a specified date range.
     *
     * @param recurringEvents the list of recurring events to iterate over
     * @param startDate the start date of the range to expand events for
     * @param endDate the end date of the range to expand events for
     */
    public RecurringEventIterator(List<RecurringEvent> recurringEvents, 
                                 LocalDate startDate, 
                                 LocalDate endDate) {
        this.recurringEvents = recurringEvents;
        this.startDate = startDate;
        this.endDate = endDate;
        expandEvents();
    }
    
    /**
     * Creates a new RecurringEventIterator that expands recurring events
     * for the next 30 days from today.
     *
     * @param recurringEvents the list of recurring events to iterate over
     */
    public RecurringEventIterator(List<RecurringEvent> recurringEvents) {
        this.recurringEvents = recurringEvents;
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusDays(30); // Default to next 30 days
        expandEvents();
    }
    
    /**
     * Expands recurring events into their individual occurrences.
     */
    private void expandEvents() {
        expandedEvents = new ArrayList<>();
        
        for (RecurringEvent recurringEvent : recurringEvents) {
            List<Event> occurrences = recurringEvent.getOccurrencesBetween(startDate, endDate);
            expandedEvents.addAll(occurrences);
        }
    }
    
    @Override
    public boolean hasNext() {
        return currentIndex < expandedEvents.size();
    }
    
    @Override
    public Event next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more recurring event occurrences");
        }
        return expandedEvents.get(currentIndex++);
    }
    
    @Override
    public void reset() {
        currentIndex = 0;
    }
}

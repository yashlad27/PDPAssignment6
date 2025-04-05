package model.calendar.iterator;

import model.calendar.Calendar;
import model.calendar.ICalendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An iterator implementation for calendars in the CalendarRegistry.
 */
public class CalendarRegistryIterator implements CalendarIterator {
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

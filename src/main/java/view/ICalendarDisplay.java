package view;

import model.calendar.ICalendar;
import java.util.List;

/**
 * Interface for calendar display functionality.
 * This interface defines methods for displaying and updating calendar-related information.
 */
public interface ICalendarDisplay {
    /**
     * Updates the calendar view.
     *
     * @param calendar the calendar to display
     */
    void updateCalendarView(ICalendar calendar);

    /**
     * Updates the calendar list.
     *
     * @param calendarNames the list of calendar names
     */
    void updateCalendarList(List<String> calendarNames);

    /**
     * Sets the selected calendar.
     *
     * @param calendarName the name of the calendar to select
     */
    void setSelectedCalendar(String calendarName);

    /**
     * Gets the selected calendar.
     *
     * @return the name of the selected calendar
     */
    String getSelectedCalendar();
} 
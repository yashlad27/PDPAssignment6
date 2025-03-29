package controller.command;

import model.calendar.CalendarManager;
import model.exceptions.CalendarNotFoundException;

/**
 * Command for selecting a calendar to use.
 */
public class UseCalendarCommand implements Command {
    private final CalendarManager calendarManager;

    public UseCalendarCommand(CalendarManager calendarManager) {
        this.calendarManager = calendarManager;
    }

    @Override
    public String getName() {
        return "use";
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "Error: Insufficient arguments for use calendar command";
        }
        String calendarName = args[2];

        try {
            calendarManager.setActiveCalendar(calendarName);
            return "Now using calendar: '" + calendarName + "'";
        } catch (CalendarNotFoundException e) {
            return e.getMessage();
        }
    }
} 
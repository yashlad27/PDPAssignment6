package view.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import model.event.Event;
import view.display.CalendarDisplayManager;

/**
 * Handles all event-related logic for the calendar panel.
 * Separates concerns according to the Single Responsibility Principle.
 */
public class CalendarEventHandler {
  private final CalendarDisplayManager displayManager;
  private final Map<LocalDate, List<Event>> eventsByDate;
  private CalendarEventListener listener;

  /**
   * Interface for calendar event callbacks.
   */
  public interface CalendarEventListener {
    /**
     * Called when a date is selected.
     *
     * @param date the selected date
     */
    void onDateSelected(LocalDate date);
    
    /**
     * Called when an event is selected.
     *
     * @param event the selected event
     */
    void onEventSelected(Event event);
    
    /**
     * Called when the month is changed.
     *
     * @param yearMonth the new year-month
     */
    void onMonthChanged(YearMonth yearMonth);
    
    /**
     * Called when the print button is clicked for an event.
     *
     * @param event the event to print
     */
    void onPrintEvent(Event event);
  }

  /**
   * Creates a new CalendarEventHandler.
   *
   * @param displayManager the calendar display manager
   */
  public CalendarEventHandler(CalendarDisplayManager displayManager) {
    this.displayManager = displayManager;
    this.eventsByDate = new HashMap<>();
  }

  /**
   * Sets the calendar event listener.
   *
   * @param listener the listener to set
   */
  public void setListener(CalendarEventListener listener) {
    this.listener = listener;
  }

  /**
   * Creates an action listener for date button clicks.
   *
   * @return the action listener
   */
  public ActionListener createDateButtonListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String dateStr = e.getActionCommand();
        LocalDate date = LocalDate.parse(dateStr);
        
        // Update the selected date in the display manager
        displayManager.setSelectedDate(date);
        
        // Notify the listener
        if (listener != null) {
          listener.onDateSelected(date);
        }
      }
    };
  }

  /**
   * Creates an action listener for the previous month button.
   *
   * @return the action listener
   */
  public ActionListener createPrevMonthListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        YearMonth currentMonth = displayManager.getCurrentMonth();
        YearMonth newMonth = currentMonth.minusMonths(1);
        
        // Update the calendar grid
        displayManager.updateCalendarGrid(newMonth, createDateButtonListener());
        
        // Update the date buttons with events
        displayManager.updateDateButtons(eventsByDate);
        
        // Notify the listener
        if (listener != null) {
          listener.onMonthChanged(newMonth);
        }
      }
    };
  }

  /**
   * Creates an action listener for the next month button.
   *
   * @return the action listener
   */
  public ActionListener createNextMonthListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        YearMonth currentMonth = displayManager.getCurrentMonth();
        YearMonth newMonth = currentMonth.plusMonths(1);
        
        // Update the calendar grid
        displayManager.updateCalendarGrid(newMonth, createDateButtonListener());
        
        // Update the date buttons with events
        displayManager.updateDateButtons(eventsByDate);
        
        // Notify the listener
        if (listener != null) {
          listener.onMonthChanged(newMonth);
        }
      }
    };
  }

  /**
   * Creates a hyperlink listener for event links.
   *
   * @return the hyperlink listener
   */
  public HyperlinkListener createEventHyperlinkListener() {
    return new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          String href = e.getDescription();
          
          // Handle print event links
          if (href.startsWith("print:")) {
            String eventId = href.substring(6);
            Event event = findEventById(eventId);
            
            if (event != null && listener != null) {
              listener.onPrintEvent(event);
            }
          }
          // Handle event selection links
          else {
            String eventId = href;
            Event event = findEventById(eventId);
            
            if (event != null && listener != null) {
              listener.onEventSelected(event);
            }
          }
        }
      }
    };
  }

  /**
   * Updates the events map with events for a date.
   *
   * @param date the date
   * @param events the events for the date
   */
  public void updateEvents(LocalDate date, List<Event> events) {
    if (date != null && events != null) {
      eventsByDate.put(date, events);
      
      // Update the date buttons to show event indicators
      displayManager.updateDateButtons(eventsByDate);
    }
  }

  /**
   * Updates the events map with events for multiple dates.
   *
   * @param eventMap the map of dates to events
   */
  public void updateAllEvents(Map<LocalDate, List<Event>> eventMap) {
    if (eventMap != null) {
      eventsByDate.clear();
      eventsByDate.putAll(eventMap);
      
      // Update the date buttons to show event indicators
      displayManager.updateDateButtons(eventsByDate);
    }
  }

  /**
   * Finds an event by its ID.
   *
   * @param eventId the event ID (in the format "subject-startDateTime")
   * @return the event, or null if not found
   */
  private Event findEventById(String eventId) {
    for (List<Event> events : eventsByDate.values()) {
      for (Event event : events) {
        String currentEventId = event.getSubject() + "-" + event.getStartDateTime().toString();
        if (currentEventId.equals(eventId)) {
          return event;
        }
      }
    }
    return null;
  }

  /**
   * Gets the events for a date.
   *
   * @param date the date
   * @return the events for the date
   */
  public List<Event> getEventsForDate(LocalDate date) {
    return eventsByDate.get(date);
  }

  /**
   * Gets all events.
   *
   * @return map of dates to events
   */
  public Map<LocalDate, List<Event>> getAllEvents() {
    return eventsByDate;
  }
}

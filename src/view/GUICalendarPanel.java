package view;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Panel class that displays the calendar view and handles calendar-related interactions.
 */
public class GUICalendarPanel extends JPanel {
  private final JLabel monthLabel;
  private final JPanel calendarGrid;
  private final Map<LocalDate, JButton> dateButtons;
  private final Map<LocalDate, List<Event>> eventsByDate;
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private CalendarPanelListener listener;

  /**
   * Interface for calendar panel events.
   */
  public interface CalendarPanelListener {
    void onDateSelected(LocalDate date);

    void onEventSelected(Event event);
  }

  /**
   * Constructs a new GUICalendarPanel.
   */
  public GUICalendarPanel() {
    setLayout(new BorderLayout(5, 5));
    setBorder(BorderFactory.createTitledBorder("Calendar"));

    // Initialize components
    monthLabel = new JLabel("", SwingConstants.CENTER);
    calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
    dateButtons = new HashMap<>();
    eventsByDate = new HashMap<>();
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();

    // Set up layout
    setupLayout();
    updateCalendarDisplay();
  }

  /**
   * Sets up the layout of the calendar panel.
   */
  private void setupLayout() {
    // Navigation panel
    JPanel navigationPanel = new JPanel(new BorderLayout());
    JButton prevButton = new JButton("←");
    JButton nextButton = new JButton("→");
    JButton todayButton = new JButton("Today");

    prevButton.addActionListener(e -> navigateToPreviousMonth());
    nextButton.addActionListener(e -> navigateToNextMonth());
    todayButton.addActionListener(e -> navigateToToday());

    navigationPanel.add(prevButton, BorderLayout.WEST);
    navigationPanel.add(monthLabel, BorderLayout.CENTER);
    navigationPanel.add(nextButton, BorderLayout.EAST);
    navigationPanel.add(todayButton, BorderLayout.SOUTH);

    // Calendar grid
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Add components to panel
    add(navigationPanel, BorderLayout.NORTH);
    add(new JScrollPane(calendarGrid), BorderLayout.CENTER);
  }

  /**
   * Updates the calendar display.
   */
  private void updateCalendarDisplay() {
    // Update month label
    monthLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

    // Clear existing buttons
    calendarGrid.removeAll();
    dateButtons.clear();

    // Add day headers
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      calendarGrid.add(new JLabel(day, SwingConstants.CENTER));
    }

    // Add empty cells for days before the first day of the month
    int firstDayOfMonth = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
    for (int i = 0; i < firstDayOfMonth; i++) {
      calendarGrid.add(new JLabel(""));
    }

    // Add day buttons
    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton button = createDateButton(date);
      calendarGrid.add(button);
    }

    // Refresh the panel
    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  /**
   * Creates a button for a specific date.
   *
   * @param date the date for the button
   * @return the created button
   */
  private JButton createDateButton(LocalDate date) {
    JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
    button.setToolTipText(date.toString());

    // Set button appearance based on whether it has events
    if (eventsByDate.containsKey(date)) {
      button.setBackground(new Color(200, 255, 200));
      button.setOpaque(true);
    }

    // Set button appearance based on whether it's selected
    if (date.equals(selectedDate)) {
      button.setBackground(new Color(150, 200, 255));
      button.setOpaque(true);
    }

    button.addActionListener(e -> {
      selectedDate = date;
      updateCalendarDisplay();
      if (listener != null) {
        listener.onDateSelected(date);
      }
    });

    dateButtons.put(date, button);
    return button;
  }

  /**
   * Updates the calendar with new data.
   *
   * @param calendar the calendar to display
   */
  public void updateCalendar(ICalendar calendar) {
    // This method will be called when the calendar data changes
    // The actual update of events will be handled by updateEvents and updateRecurringEvents
  }

  /**
   * Updates the list of events.
   *
   * @param events the list of events to display
   */
  public void updateEvents(List<Event> events) {
    eventsByDate.clear();
    for (Event event : events) {
      LocalDate date = event.getStartDateTime().toLocalDate();
      eventsByDate.computeIfAbsent(date, k -> List.of()).add(event);
    }
    updateCalendarDisplay();
  }

  /**
   * Updates the list of recurring events.
   *
   * @param recurringEvents the list of recurring events to display
   */
  public void updateRecurringEvents(List<RecurringEvent> recurringEvents) {
    // Add recurring events to the eventsByDate map
    for (RecurringEvent event : recurringEvents) {
      LocalDate startDate = event.getStartDateTime().toLocalDate();
      LocalDate endDate = event.getUntilDate();

      for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
        if (event.getWeekdays().contains(date.getDayOfWeek())) {
          eventsByDate.computeIfAbsent(date, k -> List.of()).add(event);
        }
      }
    }
    updateCalendarDisplay();
  }

  /**
   * Gets the currently selected date.
   *
   * @return the selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Sets the selected date.
   *
   * @param date the date to select
   */
  public void setSelectedDate(LocalDate date) {
    if (date != null) {
      selectedDate = date;
      currentMonth = YearMonth.from(date);
      updateCalendarDisplay();
    }
  }

  /**
   * Navigates to the previous month.
   */
  public void navigateToPreviousMonth() {
    currentMonth = currentMonth.minusMonths(1);
    updateCalendarDisplay();
  }

  /**
   * Navigates to the next month.
   */
  public void navigateToNextMonth() {
    currentMonth = currentMonth.plusMonths(1);
    updateCalendarDisplay();
  }

  /**
   * Navigates to today's date.
   */
  public void navigateToToday() {
    LocalDate today = LocalDate.now();
    setSelectedDate(today);
  }

  /**
   * Adds a listener for calendar panel events.
   *
   * @param listener the listener to add
   */
  public void addCalendarPanelListener(CalendarPanelListener listener) {
    this.listener = listener;
  }

  /**
   * Refreshes the calendar display.
   */
  public void refresh() {
    updateCalendarDisplay();
  }
} 
package view;

import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import controller.command.edit.strategy.EventEditor;
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
  private final JButton statusButton;
  private final JEditorPane eventListArea;
  private final JSpinner startDateSpinner;
  private final JSpinner endDateSpinner;
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private CalendarPanelListener listener;

  /**
   * Interface for calendar panel events.
   */
  public interface CalendarPanelListener {
    void onDateSelected(LocalDate date);

    void onEventSelected(Event event);

    void onRecurringEventSelected(RecurringEvent event);

    void onStatusRequested(LocalDate date);

    void onEventsListRequested(LocalDate date);

    void onDateRangeSelected(LocalDate startDate, LocalDate endDate);
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
    statusButton = new JButton("Check Status");
    eventListArea = new JEditorPane();
    eventListArea.setEditable(false);
    eventListArea.setContentType("text/html");
    startDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());
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

    // Status and event list panel
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createTitledBorder("Status and Events"));
    
    statusButton.addActionListener(e -> {
      if (listener != null) {
        listener.onStatusRequested(selectedDate);
      }
    });
    
    JButton listEventsButton = new JButton("List Events");
    listEventsButton.addActionListener(e -> {
      if (listener != null) {
        listener.onEventsListRequested(selectedDate);
      }
    });

    // Date range selector
    JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dateRangePanel.add(new JLabel("Start Date:"));
    dateRangePanel.add(startDateSpinner);
    dateRangePanel.add(new JLabel("End Date:"));
    dateRangePanel.add(endDateSpinner);
    
    JButton showRangeButton = new JButton("Show Range");
    showRangeButton.addActionListener(e -> {
      if (listener != null) {
        LocalDate startDate = ((java.util.Date) startDateSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        LocalDate endDate = ((java.util.Date) endDateSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        listener.onDateRangeSelected(startDate, endDate);
      }
    });
    dateRangePanel.add(showRangeButton);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(statusButton);
    buttonPanel.add(listEventsButton);
    
    statusPanel.add(buttonPanel, BorderLayout.NORTH);
    statusPanel.add(dateRangePanel, BorderLayout.CENTER);
    statusPanel.add(new JScrollPane(eventListArea), BorderLayout.SOUTH);

    // Add components to panel
    add(navigationPanel, BorderLayout.NORTH);
    add(new JScrollPane(calendarGrid), BorderLayout.CENTER);
    add(statusPanel, BorderLayout.SOUTH);
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
      JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
      dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
      calendarGrid.add(dayLabel);
    }

    // Calculate the first day of the month (0 = Sunday, 6 = Saturday)
    LocalDate firstDay = currentMonth.atDay(1);
    int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
    int offset = (firstDayOfWeek == 7) ? 0 : firstDayOfWeek;

    // Add empty cells for days before the first day of the month
    for (int i = 0; i < offset; i++) {
      JLabel emptyLabel = new JLabel("");
      emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      calendarGrid.add(emptyLabel);
    }

    // Add day buttons
    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton button = createDateButton(date);
      calendarGrid.add(button);
    }

    // Add empty cells for remaining days to maintain grid structure
    int totalCells = 42; // 6 rows × 7 columns
    int remainingCells = totalCells - (offset + daysInMonth);
    for (int i = 0; i < remainingCells; i++) {
      JLabel emptyLabel = new JLabel("");
      emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      calendarGrid.add(emptyLabel);
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
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    // Set button appearance based on whether it has events
    if (eventsByDate.containsKey(date)) {
      button.setBackground(new Color(200, 255, 200));
      button.setOpaque(true);
      button.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 100)));

      // Add a small dot indicator for busy days
      JPanel dot = new JPanel();
      dot.setPreferredSize(new Dimension(6, 6));
      dot.setBackground(new Color(100, 200, 100));
      dot.setOpaque(true);
      dot.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      
      // Create a custom button with the dot
      JButton customButton = new JButton() {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);
          dot.setBounds(getWidth() - 10, getHeight() - 10, 6, 6);
          dot.paint(g);
        }
      };
      customButton.setText(String.valueOf(date.getDayOfMonth()));
      customButton.setToolTipText(date.toString());
      customButton.setFocusPainted(false);
      customButton.setBackground(new Color(200, 255, 200));
      customButton.setOpaque(true);
      customButton.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 100)));
      
      // Add mouse listener to show event details on hover
      customButton.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
          List<Event> events = eventsByDate.get(date);
          if (!events.isEmpty()) {
            StringBuilder tooltip = new StringBuilder("<html>Events:<br>");
            for (Event event : events) {
              tooltip.append("- ").append(event.getSubject())
                    .append(" (").append(event.getStartDateTime().toLocalTime())
                    .append(" - ").append(event.getEndDateTime().toLocalTime())
                    .append(")<br>");
            }
            tooltip.append("</html>");
            customButton.setToolTipText(tooltip.toString());
          }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
          customButton.setToolTipText(date.toString());
        }
      });

      button = customButton;
    }

    // Set button appearance based on whether it's selected
    if (date.equals(selectedDate)) {
      button.setBackground(new Color(150, 200, 255));
      button.setOpaque(true);
      button.setBorder(BorderFactory.createLineBorder(new Color(50, 150, 255), 2));
      button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    // Add hover effect
    final JButton finalButton = button;
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        if (!date.equals(selectedDate)) {
          finalButton.setBackground(new Color(220, 240, 255));
          finalButton.setOpaque(true);
        }
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent evt) {
        if (!date.equals(selectedDate)) {
          if (eventsByDate.containsKey(date)) {
            finalButton.setBackground(new Color(200, 255, 200));
          } else {
            finalButton.setBackground(new JButton().getBackground());
            finalButton.setOpaque(false);
          }
        }
      }
    });

    button.addActionListener(e -> {
      selectedDate = date;
      updateCalendarDisplay();
      if (listener != null) {
        listener.onDateSelected(date);

        // Notify about events on the selected date
        if (eventsByDate.containsKey(date)) {
          List<Event> events = eventsByDate.get(date);
          for (Event event : events) {
            if (event instanceof RecurringEvent) {
              listener.onRecurringEventSelected((RecurringEvent) event);
            } else {
              listener.onEventSelected(event);
            }
          }
        }
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
    try {
      // Get events directly from calendar
      List<Event> events = calendar.getAllEvents();
      updateEvents(events);

      // Get recurring events directly from calendar
      List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
      updateRecurringEvents(recurringEvents);
    } catch (Exception e) {
      // Handle any errors appropriately
      System.err.println("Error updating calendar: " + e.getMessage());
    }
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
      eventsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
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
      LocalDate endDate = event.getEndDate();

      for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
        if (event.getRepeatDays().contains(date.getDayOfWeek())) {
          eventsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
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

  /**
   * Updates the event list area with events for a specific date.
   *
   * @param date the date to show events for
   */
  public void updateEventList(LocalDate date) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><b>Events for ").append(date).append(":</b><br><br>");
    
    if (eventsByDate.containsKey(date)) {
      List<Event> events = eventsByDate.get(date);
      for (Event event : events) {
        sb.append("• ").append(event.getSubject())
          .append(" <i>(").append(event.getStartDateTime().toLocalTime())
          .append(" - ").append(event.getEndDateTime().toLocalTime())
          .append(")</i><br>");
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append("  Location: ").append(event.getLocation()).append("<br>");
        }
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
          sb.append("  ").append(event.getDescription()).append("<br>");
        }
        sb.append("<br>");
      }
    } else {
      sb.append("<i>No events scheduled for this date.</i>");
    }
    
    sb.append("</html>");
    eventListArea.setContentType("text/html");
    eventListArea.setText(sb.toString());
  }

  /**
   * Updates the event list area with events in a date range.
   *
   * @param startDate the start date
   * @param endDate the end date
   * @param events the list of events in the range
   */
  public void updateEventListRange(LocalDate startDate, LocalDate endDate, List<Event> events) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><b>Events from ").append(startDate).append(" to ").append(endDate).append(":</b><br><br>");
    
    if (!events.isEmpty()) {
      for (Event event : events) {
        sb.append("• ").append(event.getSubject())
          .append(" <i>(").append(event.getStartDateTime().toLocalDate())
          .append(" ").append(event.getStartDateTime().toLocalTime())
          .append(" - ").append(event.getEndDateTime().toLocalTime())
          .append(")</i><br>");
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append("  Location: ").append(event.getLocation()).append("<br>");
        }
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
          sb.append("  ").append(event.getDescription()).append("<br>");
        }
        sb.append("<br>");
      }
    } else {
      sb.append("<i>No events scheduled in this date range.</i>");
    }
    
    sb.append("</html>");
    eventListArea.setContentType("text/html");
    eventListArea.setText(sb.toString());
  }

  /**
   * Updates the status display.
   *
   * @param isBusy whether the selected date is busy
   */
  public void updateStatus(boolean isBusy) {
    String status = isBusy ? "Busy" : "Available";
    JOptionPane.showMessageDialog(
        this,
        "Status: " + status,
        "Calendar Status",
        JOptionPane.INFORMATION_MESSAGE
    );
  }
} 
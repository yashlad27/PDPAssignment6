package view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
  private JButton statusButton;
  private JEditorPane eventListArea;
  private JSpinner startDateSpinner;
  private JSpinner endDateSpinner;
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private CalendarPanelListener listener;
  private ICalendar selectedCalendar;
  private JLabel monthYearLabel;
  private LocalDate currentDate;
  private static final int CELL_WIDTH = 78;
  private static final int CELL_HEIGHT = 60;
  private static final int GRID_WIDTH = 550;
  private static final int GRID_HEIGHT = 400;
  private static final Color HEADER_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_LIGHT_COLOR = new Color(0xe6f2ff);
  private static final Color BORDER_COLOR = new Color(0xcccccc);
  private static final Color TEXT_COLOR = new Color(0x333333);

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
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Initialize fields
    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    dateButtons = new HashMap<>();
    eventsByDate = new HashMap<>();

    // Create components
    monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));

    // Initialize calendar grid with proper constraints
    calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2)); // Set rows to 0 to allow flexible number of rows
    calendarGrid.setBackground(Color.WHITE);

    statusButton = new JButton("Check Status");
    eventListArea = new JEditorPane();
    eventListArea.setEditable(false);
    eventListArea.setContentType("text/html");
    startDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());
    currentDate = LocalDate.now();

    // Create and add components
    add(createNavigationPanel(), BorderLayout.NORTH);
    add(createCalendarPanel(), BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.SOUTH);

    // Add window resize listener
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
          updateCalendarDisplay();
          revalidate();
          repaint();
        });
      }
    });

    // Initial update after all components are created and added
    SwingUtilities.invokeLater(() -> {
      updateCalendarDisplay();
      revalidate();
      repaint();
    });
  }

  private JPanel createNavigationPanel() {
    JPanel navigationPanel = new JPanel(new BorderLayout(10, 0));
    navigationPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    navigationPanel.setBackground(Color.WHITE);

    // Month/Year label with consistent styling
    monthYearLabel = new JLabel("", SwingConstants.CENTER);
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 18));
    monthYearLabel.setForeground(TEXT_COLOR);
    monthYearLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    // Navigation buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    buttonPanel.setBackground(Color.WHITE);

    JButton prevButton = createNavigationButton("←");
    prevButton.addActionListener(e -> navigateToPreviousMonth());

    JButton nextButton = createNavigationButton("→");
    nextButton.addActionListener(e -> navigateToNextMonth());

    JButton todayButton = createStyledButton("Today");
    todayButton.addActionListener(e -> navigateToToday());

    buttonPanel.add(prevButton);
    buttonPanel.add(monthYearLabel);
    buttonPanel.add(nextButton);
    buttonPanel.add(Box.createHorizontalStrut(20));
    buttonPanel.add(todayButton);

    navigationPanel.add(buttonPanel, BorderLayout.CENTER);
    return navigationPanel;
  }

  private JPanel createCalendarPanel() {
    JPanel mainCalendarPanel = new JPanel(new BorderLayout(0, 5));
    mainCalendarPanel.setBackground(Color.WHITE);

    // Create day headers in a separate panel
    JPanel headerPanel = new JPanel(new GridLayout(1, 7, 0, 0));
    headerPanel.setBackground(HEADER_LIGHT_COLOR);
    headerPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      label.setForeground(TEXT_COLOR);
      label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
      headerPanel.add(label);
    }

    // Configure the existing calendar grid
    calendarGrid.setLayout(new GridLayout(0, 7, 0, 0));
    calendarGrid.setBackground(Color.WHITE);
    calendarGrid.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

    // Create a panel to hold both the header and grid
    JPanel calendarView = new JPanel(new BorderLayout());
    calendarView.add(headerPanel, BorderLayout.NORTH);
    calendarView.add(calendarGrid, BorderLayout.CENTER);
    calendarView.setPreferredSize(new Dimension(GRID_WIDTH, GRID_HEIGHT));

    mainCalendarPanel.add(calendarView, BorderLayout.CENTER);

    // Add action buttons with improved styling and centered layout
    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
    actionPanel.setBackground(Color.WHITE);
    actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    // Create buttons with consistent styling using ButtonStyler
    JButton checkStatusButton = new JButton("Check Status");
    JButton listEventsButton = new JButton("List Events");
    JButton showRangeButton = new JButton("Show Range");
    
    // Apply consistent styling
    ButtonStyler.applyPrimaryStyle(checkStatusButton);
    ButtonStyler.applyPrimaryStyle(listEventsButton);
    ButtonStyler.applyPrimaryStyle(showRangeButton);
    
    // Set consistent size
    Dimension buttonSize = new Dimension(120, 30);
    checkStatusButton.setPreferredSize(buttonSize);
    listEventsButton.setPreferredSize(buttonSize);
    showRangeButton.setPreferredSize(buttonSize);
    
    // Add action listeners
    checkStatusButton.addActionListener(e -> {
        if (listener != null) {
            listener.onStatusRequested(selectedDate);
        }
    });
    
    listEventsButton.addActionListener(e -> {
        if (listener != null) {
            listener.onEventsListRequested(selectedDate);
        }
    });
    
    showRangeButton.addActionListener(e -> {
        if (listener != null) {
            // Use a default end date (7 days from selected date) for range
            LocalDate endDate = selectedDate.plusDays(7);
            listener.onDateRangeSelected(selectedDate, endDate);
        }
    });

    actionPanel.add(checkStatusButton);
    actionPanel.add(listEventsButton);
    actionPanel.add(showRangeButton);

    mainCalendarPanel.add(actionPanel, BorderLayout.SOUTH);

    return mainCalendarPanel;
  }

  private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
    controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    // Event list area
    eventListArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));
    JScrollPane eventScroll = new JScrollPane(eventListArea);
    eventScroll.setPreferredSize(new Dimension(0, 150));

    controlPanel.add(eventScroll, BorderLayout.CENTER);
    
    // Action buttons panel at the bottom
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    
    // Status button
    statusButton = createStyledButton("Check Status");
    statusButton.addActionListener(e -> {
      if (listener != null && selectedDate != null) {
        listener.onStatusRequested(selectedDate);
      }
    });
    
    // List events button
    JButton listEventsButton = createStyledButton("List Events");
    listEventsButton.addActionListener(e -> {
      if (listener != null && selectedDate != null) {
        listener.onEventsListRequested(selectedDate);
      }
    });
    
    // Date range panel with spinners and button
    JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    
    // Configure date spinners
    startDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
    JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
    startDateSpinner.setEditor(startEditor);
    endDateSpinner.setEditor(endEditor);
    
    // Set default values to current date and a week later
    Calendar calendar = Calendar.getInstance();
    startDateSpinner.setValue(calendar.getTime());
    calendar.add(Calendar.DAY_OF_MONTH, 7);
    endDateSpinner.setValue(calendar.getTime());
    
    // Size the spinners appropriately
    Dimension spinnerSize = new Dimension(120, 30);
    startDateSpinner.setPreferredSize(spinnerSize);
    endDateSpinner.setPreferredSize(spinnerSize);
    
    // Show range button
    JButton showRangeButton = createStyledButton("Show Range");
    showRangeButton.addActionListener(e -> {
      if (listener != null) {
        // Convert java.util.Date to LocalDate
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        listener.onDateRangeSelected(start, end);
      }
    });
    
    // Add components to range panel
    rangePanel.add(new JLabel("From:"));
    rangePanel.add(startDateSpinner);
    rangePanel.add(new JLabel("To:"));
    rangePanel.add(endDateSpinner);
    rangePanel.add(showRangeButton);
    
    // Add buttons to button panel
    buttonPanel.add(statusButton);
    buttonPanel.add(listEventsButton);
    
    // We don't need these buttons here as they already exist below the calendar grid
    // The buttons below the calendar grid are sufficient

    return controlPanel;
  }

  private JButton createNavigationButton(String text) {
    JButton button = new JButton(text);
    button.setFont(button.getFont().deriveFont(Font.BOLD, 16));
    button.setForeground(HEADER_COLOR);
    button.setBackground(Color.WHITE);
    button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    button.setPreferredSize(new Dimension(30, 30));
    button.setFocusPainted(false);
    return button;
  }

  private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setBackground(HEADER_LIGHT_COLOR);
    button.setForeground(TEXT_COLOR);
    button.setBorder(BorderFactory.createLineBorder(HEADER_COLOR));
    button.setPreferredSize(new Dimension(100, 30));
    button.setFocusPainted(false);
    return button;
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (currentMonth != null) {
      SwingUtilities.invokeLater(() -> {
        updateCalendarDisplay();
        revalidate();
        repaint();
      });
    }
  }

  private void updateCalendarDisplay() {
    if (!SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(this::updateCalendarDisplay);
      return;
    }

    // Update month label
    monthYearLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

    // Clear existing cells
    calendarGrid.removeAll();
    dateButtons.clear();

    // Calculate the first day of the month
    LocalDate firstDay = currentMonth.atDay(1);
    int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
    int offset = (firstDayOfWeek == 7) ? 0 : firstDayOfWeek;

    // Add empty cells for days before the first day of the month
    for (int i = 0; i < offset; i++) {
      JPanel emptyCell = createEmptyCell();
      calendarGrid.add(emptyCell);
    }

    // Add day buttons for the current month
    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton button = createDateButton(date);
      calendarGrid.add(button);
      dateButtons.put(date, button);
    }

    // Fill remaining cells to complete the 6-week calendar
    int totalCells = 42; // 6 weeks × 7 days
    int filledCells = offset + daysInMonth;
    int remainingCells = totalCells - filledCells;

    for (int i = 0; i < remainingCells; i++) {
      JPanel emptyCell = createEmptyCell();
      calendarGrid.add(emptyCell);
    }

    // Refresh the panel
    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  private JPanel createEmptyCell() {
    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(Color.WHITE);
    cell.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    cell.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
    return cell;
  }

  private JButton createDateButton(LocalDate date) {
    JButton button = new JButton();
    button.setLayout(new BorderLayout());

    // Create date label that appears in the top-left corner
    JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    dateLabel.setForeground(TEXT_COLOR);
    dateLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

    // Create events panel for displaying event indicators
    JPanel eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    eventsPanel.setOpaque(false);

    // Style the button
    button.setBackground(Color.WHITE);
    button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    button.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));

    // Add components to button
    button.add(dateLabel, BorderLayout.NORTH);
    button.add(eventsPanel, BorderLayout.CENTER);

    // Highlight today's date
    if (date.equals(LocalDate.now())) {
      button.setBackground(HEADER_LIGHT_COLOR);
    }

    // Highlight selected date
    if (date.equals(selectedDate)) {
      button.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));
    }

    // Add click listener
    button.addActionListener(e -> {
      selectedDate = date;
      if (listener != null) {
        listener.onDateSelected(date);
      }
      updateCalendarDisplay();
    });

    // If there are events on this date, add indicators
    if (eventsByDate.containsKey(date)) {
      List<Event> events = eventsByDate.get(date);
      for (int i = 0; i < Math.min(events.size(), 2); i++) {
        Event event = events.get(i);
        JPanel eventIndicator = createEventIndicator(event);
        eventsPanel.add(eventIndicator);
        eventsPanel.add(Box.createVerticalStrut(2));
      }

      // If there are more events, add a "+X more" indicator
      if (events.size() > 2) {
        JLabel moreLabel = new JLabel("+" + (events.size() - 2) + " more");
        moreLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        moreLabel.setForeground(TEXT_COLOR);
        eventsPanel.add(moreLabel);
      }
    }

    return button;
  }

  private JPanel createEventIndicator(Event event) {
    JPanel indicator = new JPanel();
    indicator.setBackground(HEADER_COLOR);
    indicator.setPreferredSize(new Dimension(70, 15));
    indicator.setBorder(BorderFactory.createLineBorder(HEADER_COLOR.darker(), 1));
    indicator.setLayout(new BorderLayout());

    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.PLAIN, 9));
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

    indicator.add(titleLabel, BorderLayout.CENTER);
    return indicator;
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
   * @param endDate   the end date
   * @param events    the list of events in the range
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

  /**
   * Gets the currently selected calendar.
   *
   * @return the selected calendar, or null if no calendar is selected
   */
  public ICalendar getSelectedCalendar() {
    return selectedCalendar;
  }
} 
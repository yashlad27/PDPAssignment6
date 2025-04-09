package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import utilities.TimeZoneHandler;

/**
 * Panel class that displays the calendar view and handles calendar-related interactions.
 */
public class GUICalendarPanel extends JPanel {
  private final JPanel calendarGrid;
  private final Map<LocalDate, JButton> dateButtons;
  private final Map<LocalDate, List<Event>> eventsByDate;
  private JButton statusButton;
  private JEditorPane eventListArea;
  private JSpinner startDateSpinner;
  private JSpinner endDateSpinner;
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private ICalendar selectedCalendar;
  private ICalendar currentCalendar;
  private CalendarPanelListener listener;
  private JLabel monthYearLabel;
  private Event currentSelectedEvent = null;
  private static final int CELL_WIDTH = 78;
  private static final int CELL_HEIGHT = 60;
  private static final int GRID_WIDTH = 550;
  private static final int GRID_HEIGHT = 400;
  private static final Color HEADER_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_LIGHT_COLOR = new Color(0xe6f2ff);
  private static final Color BORDER_COLOR = new Color(0xcccccc);
  private static final Color TEXT_COLOR = new Color(0x333333);
  private JLabel calendarNameLabel;

  /**
   * Interface for calendar panel listeners.
   */
  public interface CalendarPanelListener {
    /**
     * Called when a date is selected.
     *
     * @param date the selected date
     */
    void onDateSelected(LocalDate date);

    /**
     * Called when a status check is requested.
     *
     * @param date the date to check status for
     */
    void onStatusRequested(LocalDate date);

    /**
     * Called when events list is requested for a date.
     *
     * @param date the date to get events for
     */
    void onEventsListRequested(LocalDate date);

    /**
     * Called when a date range is selected.
     *
     * @param startDate the start date
     * @param endDate   the end date
     */
    void onDateRangeSelected(LocalDate startDate, LocalDate endDate);

    /**
     * Called when an event edit is requested.
     *
     * @param event the event to edit
     */
    void onEditEvent(Event event);

    /**
     * Called when an event print is requested.
     *
     * @param event the event to print
     */
    void onPrintEvent(Event event);

    /**
     * Called when a recurring event is selected.
     *
     * @param event the recurring event that was selected
     */
    void onRecurringEventSelected(RecurringEvent event);

    /**
     * Called when an event is selected.
     *
     * @param event the event that was selected
     */
    void onEventSelected(Event event);
  }

  /**
   * Constructs a new GUICalendarPanel.
   */
  public GUICalendarPanel() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    currentMonth = YearMonth.now();
    selectedDate = LocalDate.now();
    dateButtons = new HashMap<>();
    eventsByDate = new HashMap<>();

    JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 16));

    calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));
    calendarGrid.setBackground(Color.WHITE);

    statusButton = new JButton("Check Status");
    eventListArea = new JEditorPane();
    eventListArea.setEditable(false);
    eventListArea.setContentType("text/html");
    startDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());

    add(createNavigationPanel(), BorderLayout.NORTH);
    add(createCalendarPanel(), BorderLayout.CENTER);
    add(createControlPanel(), BorderLayout.SOUTH);

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

    monthYearLabel = new JLabel("", SwingConstants.CENTER);
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 18));
    monthYearLabel.setForeground(TEXT_COLOR);
    monthYearLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

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

    calendarGrid.setLayout(new GridLayout(0, 7, 0, 0));
    calendarGrid.setBackground(Color.WHITE);
    calendarGrid.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

    JPanel calendarView = new JPanel(new BorderLayout());
    calendarView.add(headerPanel, BorderLayout.NORTH);
    calendarView.add(calendarGrid, BorderLayout.CENTER);
    calendarView.setPreferredSize(new Dimension(GRID_WIDTH, GRID_HEIGHT));

    mainCalendarPanel.add(calendarView, BorderLayout.CENTER);

    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
    actionPanel.setBackground(Color.WHITE);
    actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    JButton checkStatusButton = new JButton("Check Status");
    ButtonStyler.applyPrimaryStyle(checkStatusButton);

    Dimension buttonSize = new Dimension(120, 30);
    checkStatusButton.setPreferredSize(buttonSize);

    checkStatusButton.addActionListener(e -> {
      if (listener != null) {
        listener.onStatusRequested(selectedDate);
      }
    });

    // Create the calendar info box
    JPanel calendarInfoPanel = new JPanel(new BorderLayout(5, 0));
    calendarInfoPanel.setBackground(HEADER_LIGHT_COLOR);
    calendarInfoPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(HEADER_COLOR),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    
    JLabel calendarLabel = new JLabel("Current Calendar:");
    calendarLabel.setFont(new Font("Arial", Font.BOLD, 12));
    calendarLabel.setForeground(TEXT_COLOR);
    
    JLabel calendarNameLabel = new JLabel("None selected");
    calendarNameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    calendarNameLabel.setForeground(TEXT_COLOR);
    
    calendarInfoPanel.add(calendarLabel, BorderLayout.WEST);
    calendarInfoPanel.add(this.calendarNameLabel = calendarNameLabel, BorderLayout.CENTER);
    
    // Add components to the action panel
    actionPanel.add(checkStatusButton);
    actionPanel.add(calendarInfoPanel);

    mainCalendarPanel.add(actionPanel, BorderLayout.SOUTH);

    return mainCalendarPanel;
  }

  private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
    controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    eventListArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory
            .createLineBorder(new Color(0xDDDDDD)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    JScrollPane eventScroll = new JScrollPane(eventListArea);
    eventScroll.setPreferredSize(new Dimension(0, 150));

    controlPanel.add(eventScroll, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

    statusButton = createStyledButton("Check Status");
    statusButton.addActionListener(e -> {
      if (listener != null && selectedDate != null) {
        listener.onStatusRequested(selectedDate);
      }
    });

    buttonPanel.add(statusButton);
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

    monthYearLabel.setText(currentMonth.getMonth().toString() + " " + currentMonth.getYear());

    calendarGrid.removeAll();
    dateButtons.clear();

    LocalDate firstDay = currentMonth.atDay(1);
    int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
    int offset = (firstDayOfWeek == 7) ? 0 : firstDayOfWeek;

    for (int i = 0; i < offset; i++) {
      JPanel emptyCell = createEmptyCell();
      calendarGrid.add(emptyCell);
    }

    int daysInMonth = currentMonth.lengthOfMonth();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      JButton button = createDateButton(date);
      calendarGrid.add(button);
      dateButtons.put(date, button);
    }

    int totalCells = 42; // 6 weeks × 7 days
    int filledCells = offset + daysInMonth;
    int remainingCells = totalCells - filledCells;

    for (int i = 0; i < remainingCells; i++) {
      JPanel emptyCell = createEmptyCell();
      calendarGrid.add(emptyCell);
    }

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

    JLabel dateLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    dateLabel.setForeground(TEXT_COLOR);
    dateLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

    JPanel eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    eventsPanel.setOpaque(false);

    button.setBackground(Color.WHITE);
    button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    button.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));

    button.add(dateLabel, BorderLayout.NORTH);
    button.add(eventsPanel, BorderLayout.CENTER);

    if (date.equals(LocalDate.now())) {
      button.setBackground(HEADER_LIGHT_COLOR);
    }

    if (date.equals(selectedDate)) {
      button.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));
    }

    button.addActionListener(e -> {
      selectedDate = date;
      if (eventsByDate.containsKey(date) && !eventsByDate.get(date).isEmpty()) {
        Event firstEvent = eventsByDate.get(date).get(0);
        currentSelectedEvent = firstEvent;
        // Event auto-selected
        if (listener != null) {
          listener.onEventSelected(firstEvent);
        }
      } else {
        currentSelectedEvent = null;
        // No events to select
      }
      if (listener != null) {
        listener.onDateSelected(date);
      }
      updateCalendarDisplay();
      updateEventList(date);
    });

    // Add event indicators to the date button
    if (eventsByDate.containsKey(date)) {
      List<Event> events = eventsByDate.get(date);

      // Deduplicate events based on subject to prevent multiple indicators for recurring events
      Map<String, Event> uniqueEventsBySubject = new HashMap<>();
      for (Event event : events) {
        uniqueEventsBySubject.putIfAbsent(event.getSubject(), event);
      }
      List<Event> uniqueEvents = new ArrayList<>(uniqueEventsBySubject.values());

      // Display up to two events
      for (int i = 0; i < Math.min(uniqueEvents.size(), 2); i++) {
        Event event = uniqueEvents.get(i);
        JPanel eventIndicator = createEventIndicator(event);
        eventsPanel.add(eventIndicator);
        eventsPanel.add(Box.createVerticalStrut(2));
      }

      // Show how many more unique events there are
      if (uniqueEvents.size() > 2) {
        JLabel moreLabel = new JLabel("+ " + (uniqueEvents.size() - 2) + " more");
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
      this.currentCalendar = calendar;

      List<Event> events = calendar.getAllEvents();
      updateEvents(events);

      List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
      updateRecurringEvents(recurringEvents);

      if (selectedDate != null) {
        updateEventList(selectedDate);
      }
    } catch (Exception e) {
      System.err.println("Error updating calendar: " + e.getMessage());
    }
  }

  /**
   * Clears all events from the calendar view.
   * This is useful when switching between calendars to ensure no events from the previous calendar remain.
   */
  public void clearEvents() {
    eventsByDate.clear();
    updateCalendarDisplay();

    if (eventListArea != null) {
      eventListArea.setText("");
    }
  }

  public void updateEvents(List<Event> events) {
    // Track which dates are being updated in this operation
    Set<LocalDate> datesToUpdate = new HashSet<>();
    for (Event event : events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      datesToUpdate.add(eventDate);
    }

    // Clear events only for dates that we're updating
    if (currentCalendar != null && !eventsByDate.isEmpty()) {
      for (LocalDate date : datesToUpdate) {
        eventsByDate.put(date, new ArrayList<>());
      }
    } else {
      eventsByDate.clear();
    }

    // Add each event to its corresponding date in the map
    for (Event event : events) {
      // Ensure we're using the correct date from the event
      LocalDate eventDate = event.getStartDateTime().toLocalDate();

      // Get or create the list for this date
      List<Event> dateEvents = eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>());

      // Avoid duplicate events
      if (!containsEventWithSameId(dateEvents, event)) {
        dateEvents.add(event);
      }
    }

    // Force immediate refresh of calendar display to show new events
    SwingUtilities.invokeLater(() -> {
      updateCalendarDisplay();

      // Update the event list for the currently selected date if it has events
      if (selectedDate != null) {
        updateEventList(selectedDate);
      }

      repaint();
      revalidate();
    });
  }

  /**
   * Check if a list of events already contains a duplicate of the given event.
   * A duplicate is defined as an event with the same ID OR the same subject on the same day,
   * regardless of time. This is especially important for recurring events to prevent duplicates.
   *
   * @param events The list of events to check
   * @param event  The event to look for
   * @return true if a duplicate event exists in the list
   */
  private boolean containsEventWithSameId(List<Event> events, Event event) {
    // Just check for exact ID matches - simpler approach to avoid duplicates
    for (Event e : events) {
      if (e.getId().equals(event.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Updates the list of recurring events.
   *
   * @param recurringEvents the list of recurring events to display
   */
  public void updateRecurringEvents(List<RecurringEvent> recurringEvents) {
    for (RecurringEvent event : recurringEvents) {
      LocalDate startDate = event.getStartDateTime().toLocalDate();
      LocalDate endDate = event.getEndDate();

      for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
        if (event.getRepeatDays().contains(date.getDayOfWeek())) {
          // Get or create the list for this date
          List<Event> dateEvents = eventsByDate.computeIfAbsent(date, k -> new ArrayList<>());

          // Only add the event if it's not a duplicate
          if (!containsEventWithSameId(dateEvents, event)) {
            dateEvents.add(event);
          }
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
  public void setListener(CalendarPanelListener listener) {
    this.listener = listener;
  }

  /**
   * Adds a calendar panel listener.
   *
   * @param listener the listener to add
   */
  public void addCalendarPanelListener(CalendarPanelListener listener) {
    setListener(listener);
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
    // Updating event list for date
    if (currentCalendar == null) {
      System.out.println("[DEBUG] No calendar selected");
      displayMessageInEventList("No calendar selected");
      return;
    }

    try {
      System.out.println("[DEBUG] Updating events for date " + date + " with " + (eventsByDate.containsKey(date) ? eventsByDate.get(date).size() : 0) + " events");
      boolean hasEvents = eventsByDate.containsKey(date) && !eventsByDate.get(date).isEmpty();

      if (!hasEvents) {
        displayMessageInEventList("No events for " + date);
        return;
      }

      // Get events for the selected date
      List<Event> eventsOnDate = new ArrayList<>(eventsByDate.get(date));
      
      // Remove duplicate recurring events
      eventsOnDate = dedupRecurringEvents(eventsOnDate);

      // Format the event list as HTML for better styling
      StringBuilder html = new StringBuilder();
      html.append("<html><body style='font-family: Arial; font-size: 10pt;'>");
      html.append("<h3>Events for date ").append(date).append(" (").append(eventsOnDate.size()).append(" events)</h3>");
      html.append("<div style='padding: 5px;'>");

      for (Event event : eventsOnDate) {
        String subject = event.getSubject();
        String startTime = event.getStartDateTime().toLocalTime().toString();
        String endTime = event.getEndDateTime().toLocalTime().toString();
        String location = event.getLocation() != null ? event.getLocation() : "";

        // Format the event display
        html.append("<div style='margin-bottom: 10px;'>");
        html.append("<span style='font-weight: bold;'>").append(subject).append("</span><br>");
        html.append(startTime).append(" - ").append(endTime);
        
        if (!location.isEmpty()) {
          html.append("<br><span style='color: #666;'>").append(location).append("</span>");
        }
        
        html.append("</div>");
      }

      html.append("</div></body></html>");
      eventListArea.setText(html.toString());
      eventListArea.setCaretPosition(0);
    } catch (Exception e) {
      System.err.println("Error updating event list: " + e.getMessage());
      displayMessageInEventList("Error loading events: " + e.getMessage());
    }
  }
  
  /**
   * Removes duplicate recurring events with the same subject and start time.
   * Keeps only one instance of each recurring event for display purposes.
   * 
   * @param events The list of events to deduplicate
   * @return A new list with duplicates removed
   */
  private List<Event> dedupRecurringEvents(List<Event> events) {
    // Use a map to track events by subject to prevent duplicates
    Map<String, Event> uniqueEvents = new HashMap<>();
    
    // Keep only one event per subject+time combination
    for (Event event : events) {
      String key = event.getSubject() + "-" + event.getStartDateTime().toLocalTime();
      if (!uniqueEvents.containsKey(key)) {
        uniqueEvents.put(key, event);
      }
    }
    
    // Return the deduplicated events
    return new ArrayList<>(uniqueEvents.values());
  }

  /**
   * Creates a panel to display a single event with edit, copy, and print buttons.
   * <p> Creates an event panel for displaying an event.
   *
   * @param event the event to display
   * @return a panel containing the event details
   */
  private JPanel createEventPanel(Event event) {
    final Event eventInstance = event;
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(Color.WHITE);

    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(titleLabel);
    detailsPanel.add(Box.createVerticalStrut(3));

    // Convert times from UTC to local timezone for display
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

    // Convert start and end times from UTC to local time
    LocalDateTime localStartTime = timezoneHandler.convertFromUTC(event.getStartDateTime(), systemTimezone);
    LocalDateTime localEndTime = timezoneHandler.convertFromUTC(event.getEndDateTime(), systemTimezone);

    JLabel timeLabel = new JLabel(localStartTime.toLocalTime() + " - " + localEndTime.toLocalTime());
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    timeLabel.setForeground(Color.DARK_GRAY);
    timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(timeLabel);
    detailsPanel.add(Box.createVerticalStrut(3));

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      JLabel descLabel = new JLabel(event.getDescription());
      descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
      descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(descLabel);
      detailsPanel.add(Box.createVerticalStrut(3));
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      JLabel locLabel = new JLabel(event.getLocation());
      locLabel.setFont(new Font("Arial", Font.ITALIC, 12));
      locLabel.setForeground(Color.DARK_GRAY);
      locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(locLabel);
    }

    panel.add(detailsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    buttonPanel.setBackground(Color.WHITE);

    JButton editButton = new JButton("Edit");
    editButton.setBackground(HEADER_COLOR);
    editButton.setForeground(Color.WHITE);
    editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    editButton.setFocusPainted(false);
    editButton.addActionListener(e -> {
      System.out.println("[DEBUG] Edit button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("edit", "edit");
    });
    buttonPanel.add(editButton);

    JButton copyButton = new JButton("Copy");
    copyButton.setBackground(HEADER_COLOR);
    copyButton.setForeground(Color.WHITE);
    copyButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    copyButton.setFocusPainted(false);
    copyButton.addActionListener(e -> {
      System.out.println("[DEBUG] Copy button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("copy", "copy");
    });
    buttonPanel.add(copyButton);

    JButton printButton = new JButton("Print");
    printButton.setBackground(HEADER_COLOR);
    printButton.setForeground(Color.WHITE);
    printButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    printButton.setFocusPainted(false);
    printButton.addActionListener(e -> {
      System.out.println("[DEBUG] Print button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("print", "print");
    });
    buttonPanel.add(printButton);

    panel.add(buttonPanel, BorderLayout.SOUTH);

    return panel;
  }

  private void highlightEvent(JPanel eventPanel) {
    if (eventPanel.getParent() instanceof JPanel) {
      JPanel parent = (JPanel) eventPanel.getParent();
      for (Component comp : parent.getComponents()) {
        if (comp instanceof JPanel && comp != eventPanel) {
          comp.setBackground(Color.WHITE);
        }
      }
    }

    eventPanel.setBackground(HEADER_LIGHT_COLOR);
  }


  private void handleEventAction(String eventId, String action) {
    Event targetEvent = currentSelectedEvent;

    System.out.println("[DEBUG] Current selected event ID: " + (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
    System.out.println("[DEBUG] Event action requested: " + action);

    if (targetEvent != null && listener != null) {
      System.out.println("[DEBUG] Selected event: " + targetEvent.getSubject() + " with ID: " + targetEvent.getId());

      switch (action) {
        case "edit":
          System.out.println("[DEBUG] Sending edit event to listener: " + targetEvent.getSubject());
          listener.onEditEvent(targetEvent);
          break;
        case "print":
          System.out.println("[DEBUG] Sending print event to listener: " + targetEvent.getSubject());
          listener.onPrintEvent(targetEvent);
          break;
      }
    } else {
      System.out.println("[ERROR] No event selected or listener not set");
    }
  }

  private void displayMessageInEventList(String message) {
    JPanel messagePanel = new JPanel(new BorderLayout());
    messagePanel.setBackground(Color.WHITE);

    JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
    messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    messagePanel.add(messageLabel, BorderLayout.CENTER);

    if (eventListArea.getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) eventListArea.getParent();
      if (viewport.getParent() instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        Container parent = scrollPane.getParent();
        if (parent != null) {
          int index = -1;
          for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == scrollPane) {
              index = i;
              break;
            }
          }
          if (index >= 0) {
            JScrollPane newScrollPane = new JScrollPane(messagePanel);
            newScrollPane.setBorder(null);
            parent.remove(scrollPane);
            parent.add(newScrollPane, index);
            parent.revalidate();
            parent.repaint();
          }
        }
      }
    }
  }

  /**
   * Updates the event list area with events in a date range.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @param events    the list of events in the range
   */
  public void updateEventListRange(LocalDate startDate, LocalDate endDate, List<Event> events) {
    if (events == null || events.isEmpty()) {
      displayMessageInEventList("No events found between " + startDate + " and " + endDate);
      return;
    }

    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);

    JLabel titleLabel = new JLabel("Events from " + startDate + " to " + endDate);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    eventsContainer.add(titleLabel);

    for (Event event : events) {
      String currentEventId = event.getSubject().replace(' ', '_') + "-" + event.getStartDateTime().toString();
      System.out.println("[DEBUG] Using event ID format: " + currentEventId);
      System.out.println("[DEBUG] Creating event entry with ID: " + currentEventId);

      JPanel eventPanel = new JPanel(new BorderLayout(5, 5));
      eventPanel.setBackground(Color.WHITE);
      eventPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), BorderFactory.createEmptyBorder(8, 8, 8, 8)));

      JPanel detailsPanel = new JPanel();
      detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
      detailsPanel.setBackground(Color.WHITE);

      JLabel subjectLabel = new JLabel(event.getSubject());
      subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
      subjectLabel.setForeground(HEADER_COLOR);
      subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(subjectLabel);
      detailsPanel.add(Box.createVerticalStrut(3));

      String dateTimeStr = event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - " + event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
      JLabel dateTimeLabel = new JLabel(dateTimeStr);
      dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
      dateTimeLabel.setForeground(Color.DARK_GRAY);
      dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(dateTimeLabel);
      detailsPanel.add(Box.createVerticalStrut(3));

      if (event.getDescription() != null && !event.getDescription().isEmpty()) {
        JLabel descLabel = new JLabel(event.getDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(descLabel);
        detailsPanel.add(Box.createVerticalStrut(3));
      }

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        JLabel locLabel = new JLabel(event.getLocation());
        locLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        locLabel.setForeground(Color.DARK_GRAY);
        locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(locLabel);
      }

      eventPanel.add(detailsPanel, BorderLayout.CENTER);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
      buttonPanel.setBackground(Color.WHITE);

      JButton editButton = new JButton("Edit");
      editButton.setBackground(HEADER_COLOR);
      editButton.setForeground(Color.WHITE);
      editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      editButton.setFocusPainted(false);
      final String eventIdForEdit = currentEventId;
      editButton.addActionListener(e -> {
        System.out.println("[DEBUG] Edit button clicked for event in range view: " + eventIdForEdit);
        highlightEvent(eventPanel);
        handleEventAction(eventIdForEdit, "edit");
      });
      buttonPanel.add(editButton);


      JButton printButton = new JButton("Print");
      printButton.setBackground(HEADER_COLOR);
      printButton.setForeground(Color.WHITE);
      printButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      printButton.setFocusPainted(false);
      final String eventIdForPrint = currentEventId;
      printButton.addActionListener(e -> {
        System.out.println("[DEBUG] Print button clicked for event in range view: " + eventIdForPrint);
        highlightEvent(eventPanel);
        handleEventAction(eventIdForPrint, "print");
      });
      buttonPanel.add(printButton);

      eventPanel.add(buttonPanel, BorderLayout.SOUTH);

      eventsContainer.add(eventPanel);
      eventsContainer.add(Box.createVerticalStrut(10));
    }

    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(Color.WHITE);

    if (eventListArea.getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) eventListArea.getParent();
      if (viewport.getParent() instanceof JScrollPane) {
        JScrollPane currentScrollPane = (JScrollPane) viewport.getParent();
        Container parent = currentScrollPane.getParent();
        if (parent != null) {
          int index = -1;
          for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == currentScrollPane) {
              index = i;
              break;
            }
          }
          if (index >= 0) {
            parent.remove(currentScrollPane);
            parent.add(scrollPane, index);
            parent.revalidate();
            parent.repaint();

            SwingUtilities.invokeLater(() -> {
              parent.revalidate();
              parent.repaint();
              scrollPane.revalidate();
              scrollPane.repaint();
            });
          }
        }
      }
    }
  }

  /**
   * Returns the currently selected event.
   *
   * @return the currently selected event, or null if none is selected
   */
  public Event getCurrentSelectedEvent() {
    return currentSelectedEvent;
  }

  /**
   * Sets the currently selected event.
   *
   * @param event the event to set as selected
   */
  public void setCurrentSelectedEvent(Event event) {
    this.currentSelectedEvent = event;
  }

  /**
   * Updates the events for a specific date.
   *
   * @param date   the date to update events for
   * @param events the list of events on that date
   */
  public void updateDateEvents(LocalDate date, List<Event> events) {
    if (date == null) return;

    eventsByDate.put(date, new ArrayList<>(events));

    boolean isBusy = events != null && !events.isEmpty();
    updateDateStatus(date, isBusy, events != null ? events.size() : 0);
    if (date.equals(selectedDate)) {
      updateEventList(date);
    }
  }

  /**
   * Gets the currently selected calendar.
   *
   * @return the selected calendar, or null if no calendar is selected
   */
  public ICalendar getSelectedCalendar() {
    return selectedCalendar;
  }

  /**
   * Updates the status display.
   *
   * @param isBusy whether the selected date is busy
   */
  public void updateStatus(boolean isBusy) {
    String status = isBusy ? "Busy" : "Available";
    JOptionPane.showMessageDialog(this,
            "Status: " + status, "Calendar Status", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Updates the status for a specific date.
   *
   * @param date       the date to update status for
   * @param isBusy     whether the date has events
   * @param eventCount the number of events on that date
   */
  public void updateDateStatus(LocalDate date, boolean isBusy, int eventCount) {
    if (date == null) return;

    JButton dateButton = dateButtons.get(date);
    if (dateButton != null) {
      if (isBusy) {
        dateButton.setBackground(new Color(255, 240, 240));
        dateButton.setText("<html>" + date.getDayOfMonth()
                + "<br><span style='color:red;font-size:8pt'>"
                + eventCount
                + " event"
                + (eventCount > 1 ? "s" : "") + "</span></html>");
      } else {
        if (date.equals(selectedDate)) {
          dateButton.setBackground(HEADER_LIGHT_COLOR);
        } else {
          dateButton.setBackground(Color.WHITE);
        }
        dateButton.setText(String.valueOf(date.getDayOfMonth()));
      }
    }
  }

  /**
   * Sets the selected date range and highlights it in the calendar.
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   */
  public void setSelectedDateRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) return;

    for (JButton button : dateButtons.values()) {
      button.setBackground(Color.WHITE);
    }

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      JButton button = dateButtons.get(currentDate);
      if (button != null) {
        button.setBackground(HEADER_LIGHT_COLOR);
      }
      currentDate = currentDate.plusDays(1);
    }
    this.selectedDate = startDate;
  }

  public void updateCalendarName(String calendarName) {
    if (calendarNameLabel != null) {
      calendarNameLabel.setText(calendarName);
    }
  }
}
package view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
// Removed unused imports

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
      
      // Automatically select the first event on this date if available
      if (eventsByDate.containsKey(date) && !eventsByDate.get(date).isEmpty()) {
        Event firstEvent = eventsByDate.get(date).get(0);
        currentSelectedEvent = firstEvent;
        System.out.println("[DEBUG] Auto-selected event: " + firstEvent.getSubject() + " from date: " + date);
        
        // Also notify the listener about the selected event
        if (listener != null) {
          listener.onEventSelected(firstEvent);
        }
      } else {
        // Clear current selection if no events on this date
        currentSelectedEvent = null;
        System.out.println("[DEBUG] No events found on date: " + date + ", clearing current selection");
      }
      
      if (listener != null) {
        listener.onDateSelected(date);
      }
      updateCalendarDisplay();
      
      // Update the event list to reflect the selection
      updateEventList(date);
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
      // Store reference to current calendar
      this.currentCalendar = calendar;
      
      // Get events directly from calendar
      List<Event> events = calendar.getAllEvents();
      updateEvents(events);

      // Get recurring events directly from calendar
      List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
      updateRecurringEvents(recurringEvents);
      
      // If we have a selected date, update the event list for that date
      if (selectedDate != null) {
        updateEventList(selectedDate);
      }
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
  /**
   * Clears all events from the calendar view.
   * This is useful when switching between calendars to ensure no events from the previous calendar remain.
   */
  public void clearEvents() {
    System.out.println("[DEBUG] Clearing all events from calendar view");
    eventsByDate.clear();
    updateCalendarDisplay();
    
    // Also clear any event list displays
    if (eventListArea != null) {
      eventListArea.setText("");
    }
  }

  public void updateEvents(List<Event> events) {
    // Keep the existing events if this is an update rather than a full refresh
    if (currentCalendar != null && !eventsByDate.isEmpty()) {
      // Only clear dates that are being updated
      for (Event event : events) {
        LocalDate date = event.getStartDateTime().toLocalDate();
        eventsByDate.put(date, new ArrayList<>());
      }
    } else {
      eventsByDate.clear();
    }
    
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
    System.out.println("[DEBUG] Updating event list for date: " + date);
    // Use currentCalendar instead of selectedCalendar for consistency
    if (currentCalendar == null) {
      System.out.println("[DEBUG] No calendar selected");
      displayMessageInEventList("No calendar selected");
      return;
    }

    // Get events for this date from the map
    List<Event> events = eventsByDate.getOrDefault(date, new ArrayList<>());
    System.out.println("[DEBUG] Found " + events.size() + " events for date " + date);
    
    if (events.isEmpty()) {
      System.out.println("[DEBUG] No events for date " + date);
      currentSelectedEvent = null; // Clear selection if no events
      displayMessageInEventList("No events for " + date);
      return;
    }
    
    // Auto-select first event if nothing is currently selected
    if (currentSelectedEvent == null && !events.isEmpty()) {
      currentSelectedEvent = events.get(0);
      System.out.println("[DEBUG] Auto-selected first event: " + currentSelectedEvent.getSubject());
    }

    // Create a panel to hold all event panels
    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);
    
    // Add a title label
    JLabel titleLabel = new JLabel("Events for " + date);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    eventsContainer.add(titleLabel);
    
    // Create and add event panels
    for (Event event : events) {
      // Generate a unique ID for each event for consistency
      String currentEventId = event.getSubject().replace(' ', '_') + "-" + event.getStartDateTime().toString();
      System.out.println("[DEBUG] Using event ID format: " + currentEventId);
      System.out.println("[DEBUG] Creating event entry with ID: " + currentEventId);
      
      // Create a panel for this event
      JPanel eventPanel = createEventPanel(event);
      eventsContainer.add(eventPanel);
      eventsContainer.add(Box.createVerticalStrut(10)); // Add spacing between events
    }
    
    // Replace the content in the eventListArea with our new component
    // We'll create a viewport to the panel and set it as the content
    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(Color.WHITE);
    
    // Remove any existing components and add our scroll pane
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
          }
        }
      }
    }
    
    System.out.println("[DEBUG] Event list updated with " + events.size() + " events using native Swing components");
  }
  
  /**
   * Creates a panel to display a single event with edit, copy, and print buttons.
   * 
   * Creates an event panel for displaying an event.
   *
   * @param event the event to display
   * @return a panel containing the event details
   */
  private JPanel createEventPanel(Event event) {
    // Store the event ID for reference
    final Event eventInstance = event; // Keep a direct reference to the event object
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)
    ));
    
    // Create a panel for event details
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(Color.WHITE);
    
    // Event title
    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(titleLabel);
    detailsPanel.add(Box.createVerticalStrut(3));
    
    // Event time
    JLabel timeLabel = new JLabel(event.getStartDateTime().toLocalTime() + " - " + 
        event.getEndDateTime().toLocalTime());
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    timeLabel.setForeground(Color.DARK_GRAY);
    timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(timeLabel);
    detailsPanel.add(Box.createVerticalStrut(3));
    
    // Event description
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      JLabel descLabel = new JLabel(event.getDescription());
      descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
      descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(descLabel);
      detailsPanel.add(Box.createVerticalStrut(3));
    }
    
    // Event location
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      JLabel locLabel = new JLabel(event.getLocation());
      locLabel.setFont(new Font("Arial", Font.ITALIC, 12));
      locLabel.setForeground(Color.DARK_GRAY);
      locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(locLabel);
    }
    
    panel.add(detailsPanel, BorderLayout.CENTER);
    
    // Create a panel for buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    buttonPanel.setBackground(Color.WHITE);
    
    // Edit button
    JButton editButton = new JButton("Edit");
    editButton.setBackground(HEADER_COLOR);
    editButton.setForeground(Color.WHITE);
    editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    editButton.setFocusPainted(false);
    editButton.addActionListener(e -> {
      System.out.println("[DEBUG] Edit button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      // Store the event as the currently selected one FIRST, then perform action
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + 
          (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("edit", "edit");
    });
    buttonPanel.add(editButton);
    
    // Copy button
    JButton copyButton = new JButton("Copy");
    copyButton.setBackground(HEADER_COLOR);
    copyButton.setForeground(Color.WHITE);
    copyButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    copyButton.setFocusPainted(false);
    copyButton.addActionListener(e -> {
      System.out.println("[DEBUG] Copy button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      // Store the event as the currently selected one FIRST, then perform action
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + 
          (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("copy", "copy");
    });
    buttonPanel.add(copyButton);
    
    // Print button
    JButton printButton = new JButton("Print");
    printButton.setBackground(HEADER_COLOR);
    printButton.setForeground(Color.WHITE);
    printButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    printButton.setFocusPainted(false);
    printButton.addActionListener(e -> {
      System.out.println("[DEBUG] Print button clicked for event: " + event.getSubject());
      highlightEvent(panel);
      // Store the event as the currently selected one FIRST, then perform action
      currentSelectedEvent = eventInstance;
      System.out.println("[DEBUG] Stored current event reference: " + 
          (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
      handleEventAction("print", "print");
    });
    buttonPanel.add(printButton);
    
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    return panel;
  }
  
  private void highlightEvent(JPanel eventPanel) {
    // Clear any existing highlight
    if (eventPanel.getParent() instanceof JPanel) {
      JPanel parent = (JPanel) eventPanel.getParent();
      for (Component comp : parent.getComponents()) {
        if (comp instanceof JPanel && comp != eventPanel) {
          comp.setBackground(Color.WHITE);
        }
      }
    }
    
    // Set the selected event highlight
    eventPanel.setBackground(HEADER_LIGHT_COLOR);
  }
  
  // The event action handler - uses the stored reference to the currently selected event
  
  private void handleEventAction(String eventId, String action) {
    // Use the directly stored event reference instead of looking it up by ID
    Event targetEvent = currentSelectedEvent;
    
    System.out.println("[DEBUG] Current selected event ID: " + 
        (currentSelectedEvent != null ? currentSelectedEvent.getId() : "null"));
    System.out.println("[DEBUG] Event action requested: " + action);
    
    if (targetEvent != null && listener != null) {
      System.out.println("[DEBUG] Selected event: " + targetEvent.getSubject() +
                        " with ID: " + targetEvent.getId());
      
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
    
    // Replace the current content with the message panel
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
    // If no events found, display a message
    if (events == null || events.isEmpty()) {
      displayMessageInEventList("No events found between " + startDate + " and " + endDate);
      return;
    }

    // Create a panel to hold all event panels
    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);
    
    // Add a title label
    JLabel titleLabel = new JLabel("Events from " + startDate + " to " + endDate);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    eventsContainer.add(titleLabel);
    
    // Create and add event panels
    for (Event event : events) {
      // Generate a unique ID for each event for consistency
      String currentEventId = event.getSubject().replace(' ', '_') + "-" + event.getStartDateTime().toString();
      System.out.println("[DEBUG] Using event ID format: " + currentEventId);
      System.out.println("[DEBUG] Creating event entry with ID: " + currentEventId);
      
      // Create a panel for this event with more date information since this is a range view
      JPanel eventPanel = new JPanel(new BorderLayout(5, 5));
      eventPanel.setBackground(Color.WHITE);
      eventPanel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(BORDER_COLOR),
          BorderFactory.createEmptyBorder(8, 8, 8, 8)
      ));
      
      // Create a panel for event details
      JPanel detailsPanel = new JPanel();
      detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
      detailsPanel.setBackground(Color.WHITE);
      
      // Event title
      JLabel subjectLabel = new JLabel(event.getSubject());
      subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
      subjectLabel.setForeground(HEADER_COLOR);
      subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(subjectLabel);
      detailsPanel.add(Box.createVerticalStrut(3));
      
      // Event date and time - for range view, include the full date
      String dateTimeStr = event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + 
                          " - " + event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
      JLabel dateTimeLabel = new JLabel(dateTimeStr);
      dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
      dateTimeLabel.setForeground(Color.DARK_GRAY);
      dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(dateTimeLabel);
      detailsPanel.add(Box.createVerticalStrut(3));
      
      // Event description
      if (event.getDescription() != null && !event.getDescription().isEmpty()) {
        JLabel descLabel = new JLabel(event.getDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(descLabel);
        detailsPanel.add(Box.createVerticalStrut(3));
      }
      
      // Event location
      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        JLabel locLabel = new JLabel(event.getLocation());
        locLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        locLabel.setForeground(Color.DARK_GRAY);
        locLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(locLabel);
      }
      
      eventPanel.add(detailsPanel, BorderLayout.CENTER);
      
      // Create a panel for buttons
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
      buttonPanel.setBackground(Color.WHITE);
      
      // Edit button
      JButton editButton = new JButton("Edit");
      editButton.setBackground(HEADER_COLOR);
      editButton.setForeground(Color.WHITE);
      editButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      editButton.setFocusPainted(false);
      final String eventIdForEdit = currentEventId; // Create final copy for lambda
      editButton.addActionListener(e -> {
        System.out.println("[DEBUG] Edit button clicked for event in range view: " + eventIdForEdit);
        highlightEvent(eventPanel);
        handleEventAction(eventIdForEdit, "edit");
      });
      buttonPanel.add(editButton);
      
      
      // Print button
      JButton printButton = new JButton("Print");
      printButton.setBackground(HEADER_COLOR);
      printButton.setForeground(Color.WHITE);
      printButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      printButton.setFocusPainted(false);
      final String eventIdForPrint = currentEventId; // Create final copy for lambda
      printButton.addActionListener(e -> {
        System.out.println("[DEBUG] Print button clicked for event in range view: " + eventIdForPrint);
        highlightEvent(eventPanel);
        handleEventAction(eventIdForPrint, "print");
      });
      buttonPanel.add(printButton);
      
      eventPanel.add(buttonPanel, BorderLayout.SOUTH);
      
      eventsContainer.add(eventPanel);
      eventsContainer.add(Box.createVerticalStrut(10)); // Add spacing between events
    }
    
    // Replace the content in the eventListArea with our new component
    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(Color.WHITE);
    
    // Replace the existing component with our scroll pane
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
            
            // Force UI refresh to ensure visibility
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
    
    // Don't update the eventListArea reference directly since it's a different type
    // The eventListArea will be replaced in the UI hierarchy
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
   * @param date the date to update events for
   * @param events the list of events on that date
   */
  public void updateDateEvents(LocalDate date, List<Event> events) {
    if (date == null) return;
    
    // Store the events for this date
    eventsByDate.put(date, new ArrayList<>(events));
    
    // Update the button for this date
    boolean isBusy = events != null && !events.isEmpty();
    updateDateStatus(date, isBusy, events != null ? events.size() : 0);
    
    // If this is the selected date, update the event list
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
  JOptionPane.showMessageDialog(
          this,
          "Status: " + status,
          "Calendar Status",
          JOptionPane.INFORMATION_MESSAGE
  );
}

/**
 * Updates the status for a specific date.
 *
 * @param date the date to update status for
 * @param isBusy whether the date has events
 * @param eventCount the number of events on that date
 */
public void updateDateStatus(LocalDate date, boolean isBusy, int eventCount) {
  if (date == null) return;
  
  // Update the button for this date with styling to show status
  JButton dateButton = dateButtons.get(date);
  if (dateButton != null) {
    // Apply visual indicator of busy status
    if (isBusy) {
      dateButton.setBackground(new Color(255, 240, 240)); // Light red background for busy dates
      dateButton.setText("<html>" + date.getDayOfMonth() + "<br><span style='color:red;font-size:8pt'>" + eventCount + " event" + (eventCount > 1 ? "s" : "") + "</span></html>");
    } else {
      if (date.equals(selectedDate)) {
        dateButton.setBackground(HEADER_LIGHT_COLOR);  // Selected date background
      } else {
        dateButton.setBackground(Color.WHITE);  // Normal background
      }
      dateButton.setText(String.valueOf(date.getDayOfMonth()));
    }
  }
}

/**
 * Sets the selected date range and highlights it in the calendar.
 *
 * @param startDate the start date of the range
 * @param endDate the end date of the range
 */
public void setSelectedDateRange(LocalDate startDate, LocalDate endDate) {
  if (startDate == null || endDate == null) return;
  
  // Clear previous selections
  for (JButton button : dateButtons.values()) {
    button.setBackground(Color.WHITE);
  }
  
  // Highlight the selected date range
  LocalDate currentDate = startDate;
  while (!currentDate.isAfter(endDate)) {
    JButton button = dateButtons.get(currentDate);
    if (button != null) {
      button.setBackground(HEADER_LIGHT_COLOR);
    }
    currentDate = currentDate.plusDays(1);
  }
  
  // Set the selected date to the start date
  this.selectedDate = startDate;
}

}
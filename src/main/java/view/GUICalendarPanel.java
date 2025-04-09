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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

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
    JButton listEventsButton = new JButton("List Events");
    JButton showRangeButton = new JButton("Show Range");

    ButtonStyler.applyPrimaryStyle(checkStatusButton);
    ButtonStyler.applyPrimaryStyle(listEventsButton);
    ButtonStyler.applyPrimaryStyle(showRangeButton);

    Dimension buttonSize = new Dimension(120, 30);
    checkStatusButton.setPreferredSize(buttonSize);
    listEventsButton.setPreferredSize(buttonSize);
    showRangeButton.setPreferredSize(buttonSize);

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

    eventListArea.setBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)),
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

    JButton listEventsButton = createStyledButton("List Events");
    listEventsButton.addActionListener(e -> {
      if (listener != null && selectedDate != null) {
        listener.onEventsListRequested(selectedDate);
      }
    });

    JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

    startDateSpinner = new JSpinner(new SpinnerDateModel());
    endDateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
    JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
    startDateSpinner.setEditor(startEditor);
    endDateSpinner.setEditor(endEditor);

    Calendar calendar = Calendar.getInstance();
    startDateSpinner.setValue(calendar.getTime());
    calendar.add(Calendar.DAY_OF_MONTH, 7);
    endDateSpinner.setValue(calendar.getTime());

    Dimension spinnerSize = new Dimension(120, 30);
    startDateSpinner.setPreferredSize(spinnerSize);
    endDateSpinner.setPreferredSize(spinnerSize);

    JButton showRangeButton = createStyledButton("Show Range");
    showRangeButton.addActionListener(e -> {
      if (listener != null) {
        Date startDate = (Date) startDateSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        listener.onDateRangeSelected(start, end);
      }
    });

    rangePanel.add(new JLabel("From:"));
    rangePanel.add(startDateSpinner);
    rangePanel.add(new JLabel("To:"));
    rangePanel.add(endDateSpinner);
    rangePanel.add(showRangeButton);

    buttonPanel.add(statusButton);
    buttonPanel.add(listEventsButton);
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

      // Create a map to track events by subject to prevent duplicates
      Map<String, Event> uniqueEventsBySubject = new HashMap<>();

      // Only keep one event per subject (the first one we encounter)
      for (Event event : events) {
        if (!uniqueEventsBySubject.containsKey(event.getSubject())) {
          uniqueEventsBySubject.put(event.getSubject(), event);
        }
      }

      // Get the list of unique events by subject
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
   * Clears all events from the calendar view. This is useful when switching between calendars to
   * ensure no events from the previous calendar remain.
   */
  public void clearEvents() {
    eventsByDate.clear();
    updateCalendarDisplay();

    if (eventListArea != null) {
      eventListArea.setText("");
    }
  }

  /**
   * Updates the events displayed on the calendar.
   *
   * @param events the list of events to display
   */
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
   * Check if a list of events already contains a duplicate of the given event. A duplicate is
   * defined as an event with the same ID OR the same subject on the same day, regardless of time.
   * This is especially important for recurring events to prevent duplicates.
   *
   * @param events The list of events to check
   * @param event  The event to look for
   * @return true if a duplicate event exists in the list
   */
  private boolean containsEventWithSameId(List<Event> events, Event event) {
    // Exact ID match check (original behavior)
    for (Event e : events) {
      if (e.getId().equals(event.getId())) {
        return true;
      }

      // For recurring events, we consider events with the same subject on the same day as duplicates
      // Ignore time differences completely
      if (e.getSubject().equals(event.getSubject()) && isSameDay(e.getStartDateTime(),
          event.getStartDateTime())) {

        // Detected duplicate event with same subject on same day
        return true;
      }
    }
    return false;
  }

  /**
   * Check if two LocalDateTime objects represent the same day.
   *
   * @param dt1 First datetime
   * @param dt2 Second datetime
   * @return true if both represent the same date (ignoring time)
   */
  private boolean isSameDay(LocalDateTime dt1, LocalDateTime dt2) {
    return dt1.toLocalDate().equals(dt2.toLocalDate());
  }

  // Note: We removed the isSameTime method as it's no longer needed for duplicate detection

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

    List<Event> events = eventsByDate.getOrDefault(date, new ArrayList<>());
    // Found events for date

    if (events.isEmpty()) {
      // No events for date
      currentSelectedEvent = null;
      displayMessageInEventList("No events for " + date);
      return;
    }

    if (currentSelectedEvent == null && !events.isEmpty()) {
      currentSelectedEvent = events.get(0);
      System.out.println("[DEBUG] Auto-selected first event: " + currentSelectedEvent.getSubject());
    }

    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);

    JLabel titleLabel = new JLabel("Events for " + date);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(HEADER_COLOR);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    eventsContainer.add(titleLabel);

    for (Event event : events) {
      String currentEventId =
          event.getSubject().replace(' ', '_') + "-" + event.getStartDateTime().toString();
      System.out.println("[DEBUG] Using event ID format: " + currentEventId);
      System.out.println("[DEBUG] Creating event entry with ID: " + currentEventId);

      JPanel eventPanel = createEventPanel(event);
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
          }
        }
      }
    }

    System.out.println("[DEBUG] Event list updated with " + events.size()
        + " events using native Swing components");
  }

  /**
   * Creates a panel to display a single event with edit, copy, and print buttons.
   *
   * <p>Creates an event panel for displaying an event.
   *
   * @param event the event to display
   * @return a panel containing the event details
   */
  private JPanel createEventPanel(Event event) {
    final Event eventInstance = event;
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 8, 8, 8)));

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
    LocalDateTime localStartTime = timezoneHandler.convertFromUTC(event.getStartDateTime(),
        systemTimezone);
    LocalDateTime localEndTime = timezoneHandler.convertFromUTC(event.getEndDateTime(),
        systemTimezone);

    JLabel timeLabel = new JLabel(
        localStartTime.toLocalTime() + " - " + localEndTime.toLocalTime());
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
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null
          ? currentSelectedEvent.getId() : "null"));
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
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null
          ? currentSelectedEvent.getId() : "null"));
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
      System.out.println("[DEBUG] Stored current event reference: " + (currentSelectedEvent != null
          ? currentSelectedEvent.getId() : "null"));
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

    System.out.println("[DEBUG] Current selected event ID: " + (currentSelectedEvent != null
        ? currentSelectedEvent.getId() : "null"));
    System.out.println("[DEBUG] Event action requested: " + action);

    if (targetEvent != null && listener != null) {
      System.out.println("[DEBUG] Selected event: " + targetEvent.getSubject() + " with ID: "
          + targetEvent.getId());

      switch (action) {
        case "edit":
          System.out.println("[DEBUG] Sending edit event to listener: " + targetEvent.getSubject());
          listener.onEditEvent(targetEvent);
          break;
        case "print":
          System.out.println(
              "[DEBUG] Sending print event to listener: " + targetEvent.getSubject());
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
      String currentEventId =
          event.getSubject().replace(' ', '_') + "-" + event.getStartDateTime().toString();
      System.out.println("[DEBUG] Using event ID format: " + currentEventId);
      System.out.println("[DEBUG] Creating event entry with ID: " + currentEventId);

      JPanel eventPanel = new JPanel(new BorderLayout(5, 5));
      eventPanel.setBackground(Color.WHITE);
      eventPanel.setBorder(
          BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR),
              BorderFactory.createEmptyBorder(8, 8, 8, 8)));

      JPanel detailsPanel = new JPanel();
      detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
      detailsPanel.setBackground(Color.WHITE);

      JLabel subjectLabel = new JLabel(event.getSubject());
      subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
      subjectLabel.setForeground(HEADER_COLOR);
      subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(subjectLabel);
      detailsPanel.add(Box.createVerticalStrut(3));

      String dateTimeStr =
          event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - "
              + event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
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
        System.out.println(
            "[DEBUG] Edit button clicked for event in range view: " + eventIdForEdit);
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
        System.out.println(
            "[DEBUG] Print button clicked for event in range view: " + eventIdForPrint);
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
    if (date == null) {
      return;
    }

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
    JOptionPane.showMessageDialog(this, "Status: " + status, "Calendar Status",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Updates the status for a specific date.
   *
   * @param date       the date to update status for
   * @param isBusy     whether the date has events
   * @param eventCount the number of events on that date
   */
  public void updateDateStatus(LocalDate date, boolean isBusy, int eventCount) {
    if (date == null) {
      return;
    }

    JButton dateButton = dateButtons.get(date);
    if (dateButton != null) {
      if (isBusy) {
        dateButton.setBackground(new Color(255, 240, 240));
        dateButton.setText(
            "<html>" + date.getDayOfMonth() + "<br><span style='color:red;font-size:8pt'>"
                + eventCount + " event" + (eventCount > 1 ? "s" : "") + "</span></html>");
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
    if (startDate == null || endDate == null) {
      return;
    }

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

}
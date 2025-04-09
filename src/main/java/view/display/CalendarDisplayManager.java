package view.display;

import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import model.event.Event;
import utilities.TimeZoneHandler;

/**
 * Responsible for calendar UI rendering logic.
 * Follows Single Responsibility Principle by focusing only on display concerns.
 */
public class CalendarDisplayManager {
  private static final int CELL_WIDTH = 78;
  private static final int CELL_HEIGHT = 60;
  private static final int GRID_WIDTH = 550;
  private static final int GRID_HEIGHT = 400;
  private static final Color HEADER_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_LIGHT_COLOR = new Color(0xe6f2ff);
  private static final Color BORDER_COLOR = new Color(0xcccccc);
  private static final Color TEXT_COLOR = new Color(0x333333);

  private final Map<LocalDate, JButton> dateButtons;
  private JPanel calendarGrid;
  private JLabel monthYearLabel;
  private LocalDate selectedDate;
  private YearMonth currentMonth;

  /**
   * Creates a new CalendarDisplayManager.
   */
  public CalendarDisplayManager() {
    this.dateButtons = new HashMap<>();
    this.selectedDate = LocalDate.now();
    this.currentMonth = YearMonth.from(selectedDate);
  }

  /**
   * Initializes the calendar grid panel.
   *
   * @return the calendar grid panel
   */
  public JPanel initializeCalendarGrid() {
    calendarGrid = new JPanel(new java.awt.GridLayout(0, 7, 2, 2));
    calendarGrid.setPreferredSize(new Dimension(GRID_WIDTH, GRID_HEIGHT));
    calendarGrid.setBackground(Color.WHITE);
    return calendarGrid;
  }

  /**
   * Creates a label for the month and year.
   *
   * @return the month year label
   */
  public JLabel createMonthYearLabel() {
    monthYearLabel = new JLabel("", JLabel.CENTER);
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 18));
    monthYearLabel.setForeground(HEADER_COLOR);
    return monthYearLabel;
  }

  /**
   * Creates a button with the specified label and style.
   *
   * @param label the button label
   * @return the created button
   */
  public JButton createStyledButton(String label) {
    JButton button = new JButton(label);
    button.setBackground(HEADER_COLOR);
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    return button;
  }

  /**
   * Updates the calendar grid to display the specified month.
   *
   * @param yearMonth          the year and month to display
   * @param dateButtonListener the listener for date button clicks
   */
  public void updateCalendarGrid(YearMonth yearMonth, ActionListener dateButtonListener) {
    this.currentMonth = yearMonth;
    dateButtons.clear();
    calendarGrid.removeAll();

    monthYearLabel.setText(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

    String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String dayOfWeek : daysOfWeek) {
      JLabel label = new JLabel(dayOfWeek, JLabel.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 12));
      label.setForeground(HEADER_COLOR);
      calendarGrid.add(label);
    }

    LocalDate firstOfMonth = yearMonth.atDay(1);

    int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < dayOfWeekValue; i++) {
      calendarGrid.add(createEmptyCell());
    }

    for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
      LocalDate date = yearMonth.atDay(day);
      JButton button = createDateButton(date);
      button.addActionListener(dateButtonListener);
      calendarGrid.add(button);
      dateButtons.put(date, button);
    }

    int remainingCells = 42 - (dayOfWeekValue + yearMonth.lengthOfMonth());
    for (int i = 0; i < remainingCells; i++) {
      calendarGrid.add(createEmptyCell());
    }

    // Refresh the panel
    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  /**
   * Creates an empty cell for the calendar grid.
   *
   * @return the empty cell
   */
  private JPanel createEmptyCell() {
    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(Color.WHITE);
    cell.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    cell.setPreferredSize(new Dimension(CELL_WIDTH, CELL_HEIGHT));
    return cell;
  }

  /**
   * Creates a button for the specified date.
   *
   * @param date the date for the button
   * @return the date button
   */
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

    button.setActionCommand(date.toString());
    return button;
  }

  /**
   * Updates the date buttons to show event indicators.
   *
   * @param eventsByDate map of dates to events
   */
  public void updateDateButtons(Map<LocalDate, List<Event>> eventsByDate) {
    for (Map.Entry<LocalDate, JButton> entry : dateButtons.entrySet()) {
      LocalDate date = entry.getKey();
      JButton button = entry.getValue();

      List<Event> events = eventsByDate.get(date);
      boolean hasEvents = events != null && !events.isEmpty();

      if (hasEvents) {
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
        eventsPanel.setOpaque(false);

        int count = Math.min(events.size(), 3);
        for (int i = 0; i < count; i++) {
          JPanel indicator = new JPanel();
          indicator.setBackground(HEADER_COLOR);
          indicator.setPreferredSize(new Dimension(40, 4));
          eventsPanel.add(indicator);
          eventsPanel.add(Box.createVerticalStrut(2));
        }

        if (events.size() > 3) {
          JLabel countLabel = new JLabel("+" + (events.size() - 3) + " more");
          countLabel.setFont(new Font("Arial", Font.PLAIN, 10));
          countLabel.setForeground(HEADER_COLOR);
          eventsPanel.add(countLabel);
        }

        for (int i = 0; i < button.getComponentCount(); i++) {
          if (button.getComponent(i) instanceof JPanel &&
                  button.getComponent(i) != button.getComponent(0)) {
            button.remove(i);
            break;
          }
        }
        button.add(eventsPanel, BorderLayout.CENTER);
      }

      if (date.equals(selectedDate)) {
        button.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));
      } else {
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
      }
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  /**
   * Updates the selected date.
   *
   * @param date the new selected date
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;

    // Update button borders
    for (Map.Entry<LocalDate, JButton> entry : dateButtons.entrySet()) {
      LocalDate buttonDate = entry.getKey();
      JButton button = entry.getValue();

      if (buttonDate.equals(date)) {
        button.setBorder(BorderFactory.createLineBorder(HEADER_COLOR, 2));
      } else {
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
      }
    }
  }

  /**
   * Updates the event list with events for the specified date.
   *
   * @param eventListArea the editor pane to update
   * @param date          the date to show events for
   * @param events        the list of events
   */
  public void updateEventList(JEditorPane eventListArea, LocalDate date, List<Event> events) {
    if (date == null) {
      eventListArea.setText("No date selected");
      return;
    }

    if (events == null || events.isEmpty()) {
      eventListArea.setText("No events for " + date);
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("<html><body style='font-family:Arial; font-size:12px;'>")
            .append("<h3 style='color:#4a86e8;'>Events for ").append(date).append("</h3>");

    // Create a TimeZoneHandler to convert times
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

    for (Event event : events) {
      // Convert event times from UTC to local timezone for display
      LocalDateTime localStartTime = timezoneHandler.convertFromUTC(event.getStartDateTime(),
              systemTimezone);
      LocalDateTime localEndTime = timezoneHandler.convertFromUTC(event.getEndDateTime(),
              systemTimezone);

      String currentEventId = event.getSubject() + "-" + event.getStartDateTime().toString();
      sb.append("<div id='").append(currentEventId)
              .append("' style='margin-bottom:10px; " +
                      "padding:5px; border:1px solid #cccccc; border-radius:3px;'>")
              .append("<b style='color:#4a86e8;'>").append(event.getSubject()).append("</b><br>")
              .append("<span style='color:#666;'>").append(localStartTime.toLocalTime())
              .append(" - ").append(localEndTime.toLocalTime()).append("</span><br>")
              .append("<span>").append(event.getDescription()).append("</span><br>")
              .append("<span style='color:#666;'>").append(event.getLocation() != null
                      ? event.getLocation() : "").append("</span>")
              .append("<div style='margin-top:5px;'>")
              .append("<button onclick='printEvent(\"").append(currentEventId)
              .append("\")' style='background-color:#4a86e8; color:white; border:none;" +
                      " padding:5px 10px; cursor:pointer;'>Print</button>")
              .append("</div></div>");
    }

    sb.append("<script>")
            .append("function printEvent(id) { window.location.href='print:' + id; }\n")
            .append("</script>")
            .append("</body></html>");

    eventListArea.setContentType("text/html");
    eventListArea.setText(sb.toString());
  }

  /**
   * Updates the event list with events in a date range.
   *
   * @param eventListArea the editor pane to update
   * @param startDate     the start date of the range
   * @param endDate       the end date of the range
   * @param events        the list of events in the range
   */
  public void updateEventListRange(JEditorPane eventListArea, LocalDate startDate,
                                   LocalDate endDate, List<Event> events) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html><body style='font-family:Arial; font-size:12px;'>")
            .append("<h3 style='color:#4a86e8;'>Events from ").append(startDate).append(" to ")
            .append(endDate).append(":</h3>");

    for (Event event : events) {
      String currentEventId = event.getSubject() + "-" + event.getStartDateTime().toString();
      sb.append("<div id='").append(currentEventId)
              .append("' style='margin-bottom:10px; padding:5px; border:1px solid #cccccc;" +
                      " border-radius:3px;'>")
              .append("<b style='color:#4a86e8;'>").append(event.getSubject()).append("</b><br>")
              .append("<span style='color:#666;'>")
              .append(event.getStartDateTime().format(DateTimeFormatter
                      .ofPattern("yyyy-MM-dd HH:mm")))
              .append(" - ").append(event.getEndDateTime()
                      .format(DateTimeFormatter.ofPattern("HH:mm"))).append("</span><br>")
              .append("<span>").append(event.getDescription() != null
                      ? event.getDescription() : "").append("</span><br>")
              .append("<span style='color:#666;'>").append(event.getLocation() != null
                      ? event.getLocation() : "").append("</span>")
              .append("<div style='margin-top:5px;'>")
              .append("<button onclick='printEvent(\"").append(currentEventId)
              .append("\")' style='background-color:#4a86e8; color:white; border:none;" +
                      " padding:5px 10px; cursor:pointer;'>Print</button>")
              .append("</div></div>");
    }

    sb.append("<script>")
            .append("function printEvent(id) { window.location.href='print:' + id; }\n")
            .append("</script>")
            .append("</body></html>");

    eventListArea.setContentType("text/html");
    eventListArea.setText(sb.toString());
  }

  /**
   * Gets the current month.
   *
   * @return the current month
   */
  public YearMonth getCurrentMonth() {
    return currentMonth;
  }

  /**
   * Gets the selected date.
   *
   * @return the selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }
}
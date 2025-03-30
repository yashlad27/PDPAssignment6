package view;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import javax.swing.*;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

/**
 * Main GUI view class that integrates all GUI components and implements the IGUIView interface.
 * This class provides the main window and layout for the calendar application.
 */
public class GUIView extends JFrame implements IGUIView {
  private final GUICalendarPanel calendarPanel;
  private final GUIEventPanel eventPanel;
  private final GUICalendarSelectorPanel calendarSelectorPanel;
  private final GUIExportImportPanel exportImportPanel;
  private final JTextArea messageArea;

  /**
   * Constructs a new GUIView.
   */
  public GUIView() {
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);

    // Initialize components
    calendarPanel = new GUICalendarPanel();
    eventPanel = new GUIEventPanel();
    calendarSelectorPanel = new GUICalendarSelectorPanel();
    exportImportPanel = new GUIExportImportPanel();
    messageArea = new JTextArea(3, 40);
    messageArea.setEditable(false);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);

    // Set up layout
    setupLayout();

    // Make the window visible
    setVisible(true);
  }

  /**
   * Sets up the layout of the GUI components.
   */
  private void setupLayout() {
    // Main panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Left panel for calendar selector and export/import
    JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
    leftPanel.add(calendarSelectorPanel, BorderLayout.NORTH);
    leftPanel.add(exportImportPanel, BorderLayout.CENTER);
    leftPanel.setPreferredSize(new Dimension(250, 0));

    // Center panel for calendar view
    JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
    centerPanel.add(calendarPanel, BorderLayout.CENTER);
    centerPanel.add(new JScrollPane(messageArea), BorderLayout.SOUTH);

    // Right panel for event panel
    JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
    rightPanel.add(eventPanel, BorderLayout.CENTER);
    rightPanel.setPreferredSize(new Dimension(300, 0));

    // Add panels to main panel
    mainPanel.add(leftPanel, BorderLayout.WEST);
    mainPanel.add(centerPanel, BorderLayout.CENTER);
    mainPanel.add(rightPanel, BorderLayout.EAST);

    // Add main panel to frame
    add(mainPanel);
  }

  /**
   * Gets the calendar panel.
   *
   * @return the calendar panel
   */
  public GUICalendarPanel getCalendarPanel() {
    return calendarPanel;
  }

  /**
   * Gets the event panel.
   *
   * @return the event panel
   */
  public GUIEventPanel getEventPanel() {
    return eventPanel;
  }

  /**
   * Gets the calendar selector panel.
   *
   * @return the calendar selector panel
   */
  public GUICalendarSelectorPanel getCalendarSelectorPanel() {
    return calendarSelectorPanel;
  }

  /**
   * Gets the export/import panel.
   *
   * @return the export/import panel
   */
  public GUIExportImportPanel getExportImportPanel() {
    return exportImportPanel;
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {
    calendarPanel.updateCalendar(calendar);
  }

  @Override
  public void updateEventList(List<Event> events) {
    calendarPanel.updateEvents(events);
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    calendarPanel.updateRecurringEvents(recurringEvents);
  }

  @Override
  public void showEventDetails(Event event) {
    eventPanel.displayEvent(event);
  }

  @Override
  public void clearEventDetails() {
    eventPanel.clearForm();
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    calendarSelectorPanel.updateCalendars(calendarNames);
  }

  @Override
  public void setSelectedCalendar(String calendarName) {
    calendarSelectorPanel.setSelectedCalendar(calendarName);
  }

  @Override
  public String getSelectedCalendar() {
    return calendarSelectorPanel.getSelectedCalendar();
  }

  @Override
  public LocalDate getSelectedDate() {
    return calendarPanel.getSelectedDate();
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    calendarPanel.setSelectedDate(date);
  }

  @Override
  public void refreshView() {
    calendarPanel.refresh();
    eventPanel.refresh();
    calendarSelectorPanel.refresh();
    exportImportPanel.refresh();
  }

  @Override
  public void displayMessage(String message) {
    messageArea.append(message + "\n");
    messageArea.setCaretPosition(messageArea.getDocument().getLength());
  }

  @Override
  public void displayError(String error) {
    messageArea.append("Error: " + error + "\n");
    messageArea.setCaretPosition(messageArea.getDocument().getLength());
  }
} 
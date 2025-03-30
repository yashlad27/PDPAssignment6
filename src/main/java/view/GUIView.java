package view;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import javax.swing.*;

import controller.CalendarController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;
import model.exceptions.ConflictingEventException;
import model.exceptions.InvalidEventException;
import model.exceptions.EventNotFoundException;

/**
 * Main GUI view class that integrates all GUI components and implements the IGUIView interface.
 * This class provides the main window and layout for the calendar application.
 */
public class GUIView extends JFrame implements IGUIView,
        CalendarViewModel.CalendarViewModelListener,
        EventViewModel.EventViewModelListener,
        ExportImportViewModel.ExportImportViewModelListener,
        ICalendarView {
  private final GUICalendarPanel calendarPanel;
  private final GUIEventPanel eventPanel;
  private final GUICalendarSelectorPanel calendarSelectorPanel;
  private final GUIExportImportPanel exportImportPanel;
  private final JTextArea messageArea;
  private final CalendarViewModel calendarViewModel;
  private final EventViewModel eventViewModel;
  private final ExportImportViewModel exportImportViewModel;

  /**
   * Constructs a new GUIView.
   *
   * @param controller the calendar controller
   */
  public GUIView(CalendarController controller) {
    System.out.println("Creating GUIView...");
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);

    // Initialize ViewModels
    calendarViewModel = new CalendarViewModel();
    eventViewModel = new EventViewModel(controller);
    exportImportViewModel = new ExportImportViewModel();

    // Initialize components
    calendarPanel = new GUICalendarPanel();
    eventPanel = new GUIEventPanel();
    calendarSelectorPanel = new GUICalendarSelectorPanel();
    exportImportPanel = new GUIExportImportPanel();
    messageArea = new JTextArea(3, 40);
    messageArea.setEditable(false);
    messageArea.setLineWrap(true);
    messageArea.setWrapStyleWord(true);

    // Set up listeners
    setupListeners();

    // Set up layout
    setupLayout();
    System.out.println("GUIView created successfully.");
  }

  /**
   * Shows the GUI window.
   */
  public void displayGUI() {
    System.out.println("Displaying GUI window...");
    pack();
    setVisible(true);
  }

  /**
   * Sets up the listeners for all ViewModels.
   */
  private void setupListeners() {
    calendarViewModel.addListener(this);
    eventViewModel.addListener(this);
    exportImportViewModel.addListener(this);
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

    // Create split panes for resizable panels
    JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    leftSplitPane.setResizeWeight(0.2);
    leftSplitPane.setOneTouchExpandable(true);

    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, rightPanel);
    mainSplitPane.setResizeWeight(0.8);
    mainSplitPane.setOneTouchExpandable(true);

    // Add main split pane to frame
    mainPanel.add(mainSplitPane, BorderLayout.CENTER);
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

  /**
   * Gets the calendar view model.
   *
   * @return the calendar view model
   */
  public CalendarViewModel getCalendarViewModel() {
    return calendarViewModel;
  }

  /**
   * Gets the event view model.
   *
   * @return the event view model
   */
  public EventViewModel getEventViewModel() {
    return eventViewModel;
  }

  /**
   * Gets the export/import view model.
   *
   * @return the export/import view model
   */
  public ExportImportViewModel getExportImportViewModel() {
    return exportImportViewModel;
  }

  @Override
  public void updateCalendarView(ICalendar calendar) {
    try {
      calendarViewModel.setCurrentCalendar(calendar);
    } catch (ConflictingEventException | InvalidEventException | EventNotFoundException e) {
      displayError("Failed to update calendar view: " + e.getMessage());
    }
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
    eventViewModel.setSelectedEvent(event);
  }

  @Override
  public void clearEventDetails() {
    eventPanel.clearForm();
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    calendarViewModel.updateCalendarList(calendarNames);
  }

  public void setSelectedCalendar(String calendarName) {
    calendarViewModel.setSelectedCalendarName(calendarName);
  }

  @Override
  public String getSelectedCalendar() {
    return calendarViewModel.getSelectedCalendarName();
  }

  @Override
  public LocalDate getSelectedDate() {
    return calendarViewModel.getSelectedDate();
  }

  @Override
  public void setSelectedDate(LocalDate date) {
    calendarViewModel.setSelectedDate(date);
  }

  @Override
  public void refreshView() {
    calendarViewModel.refresh();
    eventViewModel.refresh();
    exportImportViewModel.refresh();
  }

  @Override
  public String readCommand() {
    // This method is not used in GUI mode
    // It's required by ICalendarView interface but not needed for GUI
    return "";
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

  // CalendarViewModelListener implementation
  @Override
  public void onCalendarChanged(ICalendar calendar) {
    calendarPanel.updateCalendar(calendar);
    exportImportViewModel.setCurrentCalendar(calendar);
  }

  @Override
  public void onDateSelected(LocalDate date) {
    calendarPanel.setSelectedDate(date);
  }

  @Override
  public void onEventsUpdated(List<Event> events) {
    calendarPanel.updateEvents(events);
  }

  @Override
  public void onRecurringEventsUpdated(List<RecurringEvent> recurringEvents) {
    calendarPanel.updateRecurringEvents(recurringEvents);
  }

  @Override
  public void onCalendarListUpdated(List<String> calendarNames) {
    calendarSelectorPanel.updateCalendars(calendarNames);
  }

  @Override
  public void onError(String error) {
    displayError(error);
  }

  // EventViewModelListener implementation
  @Override
  public void onEventSelected(Event event) {
    eventPanel.displayEvent(event);
  }

  @Override
  public void onRecurringEventSelected(RecurringEvent event) {
    eventPanel.displayRecurringEvent(event);
  }

  @Override
  public void onEventCreated(Event event) {
    displayMessage("Event created successfully");
  }

  @Override
  public void onRecurringEventCreated(RecurringEvent event) {
    displayMessage("Recurring event created successfully");
  }

  @Override
  public void onEventUpdated(Event event) {
    displayMessage("Event updated successfully");
  }

  @Override
  public void onRecurringEventUpdated(RecurringEvent event) {
    displayMessage("Recurring event updated successfully");
  }

  @Override
  public void onImportSuccess() {
    displayMessage("Calendar imported successfully");
  }

  @Override
  public void onExportSuccess() {
    displayMessage("Calendar exported successfully");
  }
} 
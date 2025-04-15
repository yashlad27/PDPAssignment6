package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import controller.CalendarController;
import controller.GUIController;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import utilities.TimeZoneHandler;
import viewmodel.CalendarViewModel;
import viewmodel.EventViewModel;
import viewmodel.ExportImportViewModel;

/**
 * Main GUI view class that integrates all GUI components and implements both ICalendarView,
 * IGUIView, and CalendarViewFeatures interfaces.
 * This class provides the main window and layout for the calendar application.
 */
public class GUIView extends JFrame implements ICalendarView, IGUIView, CalendarViewFeatures {
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color BORDER_COLOR = new Color(0xcccccc);
  private static final int SIDEBAR_WIDTH = 180;

  private final GUICalendarPanel calendarPanel;
  private final GUIEventPanel eventPanel;
  private final GUICalendarSelectorPanel calendarSelectorPanel;
  private final GUIExportImportPanel exportImportPanel;
  private JPanel eventListResultsPanel;
  private final JTextArea messageArea;
  private final CalendarController controller;
  private CalendarViewModel calendarViewModel;
  private EventViewModel eventViewModel;
  private ExportImportViewModel exportImportViewModel;
  private GUIController guiController;

  /**
   * Constructs a new GUIView.
   *
   * @param controller the calendar controller
   */
  public GUIView(CalendarController controller) {
    this.controller = controller;
    System.out.println("Creating GUIView...");

    // Initialize view models
    calendarViewModel = new CalendarViewModel();
    eventViewModel = new EventViewModel(controller);
    exportImportViewModel = new ExportImportViewModel();

    // Set up the main window
    setTitle("Calendar Application");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(800, 600));

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

    // Set up listeners
    setupListeners();

    pack();
    setLocationRelativeTo(null);
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
    // Set up calendar selector listener for calendar selection and creation
    calendarSelectorPanel.addCalendarSelectorListener(new GUICalendarSelectorPanel
            .CalendarSelectorListener() {
      @Override
      public void onCalendarSelected(ICalendar calendar) {
        System.out.println("[DEBUG] Calendar selected: " + (calendar != null
                ? calendar.toString() : "null"));
        if (calendar != null) {
          controller.setSelectedCalendar(calendar);
          eventPanel.clearForm();
          refreshView();
        }
      }

      @Override
      public void onCalendarSelected(String calendarName) {
        System.out.println("[DEBUG] Calendar selected by name: " + calendarName);
        if (calendarName != null) {
          controller.setSelectedCalendarByName(calendarName);
          eventPanel.clearForm();
          refreshView();
        }
      }

      @Override
      public void onCalendarCreated(String name, String timezone) {
        System.out.println("[DEBUG] Attempting to create calendar: " + name
                + " with timezone: " + timezone);
        try {
          boolean success = controller.createCalendar(name, timezone);
          System.out.println("[DEBUG] Calendar creation result: " + (success
                  ? "success" : "failed"));
          if (success) {
            controller.updateCalendarList();
            controller.setSelectedCalendarByName(name);
            displayMessage("Calendar created successfully: " + name);
            refreshView();
          } else {
            showErrorMessage("Could not create calendar");
          }
        } catch (Exception ex) {
          System.out.println("[DEBUG] Calendar creation error: " + ex.getMessage());
          showErrorMessage("Could not create calendar: " + ex.getMessage());
        }
      }
    });

    // Set up event panel listener
    eventPanel.addEventPanelListener(new GUIEventPanel.EventPanelListener() {
      @Override
      public void onEventSaved(EventFormData formData) {
        System.out.println("[DEBUG] Attempting to save event. Recurring: "
                + formData.isRecurring());

        try {
          ICalendar currentCalendar = controller.getCurrentCalendar();
          System.out.println("[DEBUG] Current calendar: " + (currentCalendar != null
                  ? currentCalendar.toString() : "null"));

          if (currentCalendar == null) {
            showErrorMessage("Please select a calendar first");
            return;
          }

          // Pass the form data to the GUI controller
          if (guiController != null) {
            guiController.onEventSaved(formData);
          } else {
            showErrorMessage("GUI Controller not initialized");
          }
        } catch (Exception e) {
          showErrorMessage("Error creating event: " + e.getMessage());
          e.printStackTrace();
        }
      }

      @Override
      public void onEventCancelled() {
        System.out.println("[DEBUG] Event creation cancelled");
        eventPanel.clearForm();
      }

      @Override
      public void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime,
                                LocalDateTime targetEndDateTime) {
        System.out.println("[DEBUG] Event copy requested to calendar: " + targetCalendarName);
        try {
          // Get the current event from the event panel
          Event currentEvent = eventPanel.getCurrentEvent();
          if (currentEvent == null) {
            showErrorMessage("No event selected to copy");
            return;
          }

          // Call the controller to handle the copy operation
          if (guiController != null) {
            boolean success = guiController.copyEvent(currentEvent, targetCalendarName,
                    targetStartDateTime, targetEndDateTime);
            if (success) {
              showInfoMessage("Event copied successfully");
              eventPanel.clearForm();
              refreshView(); // Refresh to show the copied event
            } else {
              showErrorMessage("Failed to copy event");
            }
          } else {
            showErrorMessage("Controller not initialized");
          }
        } catch (Exception e) {
          System.out.println("[DEBUG] Event copy error: " + e.getMessage());
          showErrorMessage("Error copying event: " + e.getMessage());
        }
      }

      @Override
      public List<String> getAvailableCalendarNames() {
        System.out.println("[DEBUG] Getting available calendar names");
        List<String> calendarNames = new ArrayList<>();
        try {
          // Get calendar names from the controller
          if (controller != null) {
            Set<String> names = controller.getCalendarNames();
            if (names != null) {
              calendarNames = new ArrayList<>(names);
              System.out.println("[DEBUG] Found " + calendarNames.size()
                      + " calendars from controller");
            }
          }
        } catch (Exception ex) {
          System.out.println("[DEBUG] Error getting calendar names: " + ex.getMessage());
        }
        return calendarNames;
      }


      @Override
      public void onEventUpdated(EventFormData formData) {
        System.out.println("[DEBUG] Attempting to update event. Recurring: "
                + formData.isRecurring());

        try {
          ICalendar currentCalendar = controller.getCurrentCalendar();
          if (currentCalendar == null) {
            showErrorMessage("Please select a calendar first");
            return;
          }

          // Pass the form data to the GUI controller
          if (guiController != null) {
            guiController.onEventUpdated(formData);
          } else {
            showErrorMessage("GUI Controller not initialized");
          }
        } catch (Exception e) {
          System.out.println("[DEBUG] Event update error: " + e.getMessage());
          showErrorMessage("Error updating event: " + e.getMessage());
          e.printStackTrace();

          // Still try to refresh the view even after error
          refreshView();
          // Update the event list for the selected date
          LocalDate selectedDate = calendarPanel.getSelectedDate();
          System.out.println("[DEBUG] Selected date for refresh: " + selectedDate);
          if (selectedDate != null) {
            try {
              ICalendar currentCalendar = controller.getCurrentCalendar();
              System.out.println("[DEBUG] Current calendar for refresh: "
                      + (currentCalendar != null ? currentCalendar.toString() : "null"));
              if (currentCalendar != null) {
                List<Event> events = currentCalendar.getEventsOnDate(selectedDate);
                System.out.println("[DEBUG] Events found for date: " + events.size());
                calendarPanel.updateEvents(currentCalendar.getAllEvents());
                calendarPanel.updateRecurringEvents(currentCalendar.getAllRecurringEvents());
                calendarPanel.updateEventList(selectedDate);
                System.out.println("[DEBUG] View updated with events");
              }
            } catch (Exception ex) {
              System.out.println("[DEBUG] Error updating event list: " + ex.getMessage());
              showErrorMessage("Error updating event list: " + ex.getMessage());
            }
          }
        }
      }
    });

    // Set up event listeners for calendar panel
    calendarPanel.addCalendarPanelListener(new GUICalendarPanel.CalendarPanelListener() {
      @Override
      public void onDateSelected(LocalDate date) {
        if (date != null) {
          eventPanel.setDate(date);
        }
      }

      @Override
      public void onEventSelected(Event event) {
        if (event != null) {
          eventPanel.displayEvent(event);
        }
      }

      @Override
      public void onRecurringEventSelected(RecurringEvent event) {
        if (event != null) {
          eventPanel.displayRecurringEvent(event);
        }
      }

      @Override
      public void onStatusRequested(LocalDate date) {
        // Status handling is done in the panel itself
      }

      @Override
      public void onEventsListRequested(LocalDate date) {
        calendarPanel.updateEventList(date);
      }

      @Override
      public void onDateRangeSelected(LocalDate startDate, LocalDate endDate) {
        calendarPanel.updateEventListRange(startDate, endDate, null);
      }

      @Override
      public void onEditEvent(Event event) {
        System.out.println("[DEBUG] Edit event requested in view: " + event.getSubject());
        if (guiController != null) {
          guiController.editEvent(event);
        }
      }

      @Override
      public void onPrintEvent(Event event) {
        System.out.println("[DEBUG] Print event requested in view: " + event.getSubject());
        if (guiController != null) {
          guiController.printEvent(event);
        }
      }
    });

    // Set up export/import listeners
    exportImportPanel.addExportImportListener(new GUIExportImportPanel.ExportImportListener() {
      @Override
      public void onImport(File file) {
        if (file != null) {
          try {
            controller.importCalendarFromCSV(file.getAbsolutePath());
            // Success message will be shown by the ExportImportViewModel
          } catch (Exception ex) {
            exportImportPanel.showErrorMessage("Import failed: " + ex.getMessage());
          }
        }
      }

      @Override
      public void onExport(File file) {
        if (file != null) {
          try {
            controller.exportCalendarToCSV(file.getAbsolutePath());
            exportImportPanel.showExportSuccess();
          } catch (Exception ex) {
            exportImportPanel.showErrorMessage("Export failed: " + ex.getMessage());
          }
        }
      }
    });
  }

  /**
   * Sets up the layout of the GUI components.
   */
  private void setupLayout() {
    // Custom title bar panel with consistent styling
    JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    titleBar.setBackground(THEME_COLOR);
    titleBar.setPreferredSize(new Dimension(getWidth(), 40));
    titleBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JLabel titleLabel = new JLabel("Calendar Application");
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
    titleBar.add(titleLabel);

    // Main content panel with proper spacing
    JPanel contentPanel = new JPanel();
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    contentPanel.setLayout(new BorderLayout(10, 10));

    // Create the split pane with proper settings
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setBorder(null);
    mainSplitPane.setDividerSize(5);
    mainSplitPane.setResizeWeight(0.0); // Fix left panel width
    mainSplitPane.setMinimumSize(new Dimension(800, 500));

    // Left sidebar with proper constraints
    JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
    leftPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
    leftPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));
    leftPanel.setBackground(new Color(0xf8f8f8));
    leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

    // Calendar selector at top of left panel
    calendarSelectorPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 200));
    calendarSelectorPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 150));

    // Import/Export in the center of left panel (moved up from bottom)
    exportImportPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 200));
    exportImportPanel.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 150));

    // Create a vertical panel to hold components in the left sidebar
    JPanel leftComponentsPanel = new JPanel();
    leftComponentsPanel.setLayout(new BoxLayout(leftComponentsPanel, BoxLayout.Y_AXIS));
    leftComponentsPanel.setBackground(new Color(0xf8f8f8));

    // Add components to left panel in vertical order
    leftComponentsPanel.add(calendarSelectorPanel);
    leftComponentsPanel.add(Box.createVerticalStrut(10)); // Add spacing
    leftComponentsPanel.add(exportImportPanel);
    leftComponentsPanel.add(Box.createVerticalGlue()); // Push everything up

    leftPanel.add(leftComponentsPanel, BorderLayout.CENTER);

    // Right side with a horizontal split between calendar and event panel
    JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

    // Create a secondary split pane to separate calendar and event panel horizontally
    JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    rightSplitPane.setBorder(null);
    rightSplitPane.setDividerSize(5);
    rightSplitPane.setResizeWeight(0.7); // Give more weight to calendar

    // Create a panel for the calendar and event list results
    JPanel calendarContainer = new JPanel(new BorderLayout(0, 5));

    // Initialize event list results panel to appear under the calendar
    eventListResultsPanel = new JPanel(new BorderLayout());
    eventListResultsPanel.setBackground(Color.WHITE);
    eventListResultsPanel.setBorder(BorderFactory.createTitledBorder("Event List Results"));
    eventListResultsPanel.setPreferredSize(new Dimension(550, 150));

    // Event list results will be populated dynamically
    // No static text needed here

    // Create scroll pane for event list results
    JScrollPane eventListScrollPane = new JScrollPane(eventListResultsPanel);
    eventListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    eventListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    eventListScrollPane.setBorder(null);

    // Create a panel for the main calendar
    JPanel calendarGridPanel = new JPanel(new BorderLayout());
    calendarGridPanel.add(calendarPanel, BorderLayout.CENTER);

    // Add the calendar and event list results to the container
    calendarContainer.add(calendarGridPanel, BorderLayout.CENTER);
    calendarContainer.add(eventListScrollPane, BorderLayout.SOUTH);
    calendarContainer.setPreferredSize(new Dimension(550, 0));

    // Event panel with fixed width
    eventPanel.setPreferredSize(new Dimension(350, 0));
    JScrollPane eventScrollPane = new JScrollPane(eventPanel);
    eventScrollPane.setBorder(null);

    // Add components to right split pane
    rightSplitPane.setLeftComponent(calendarContainer);
    rightSplitPane.setRightComponent(eventScrollPane);

    rightPanel.add(rightSplitPane, BorderLayout.CENTER);

    // Set up main split pane
    mainSplitPane.setLeftComponent(leftPanel);
    mainSplitPane.setRightComponent(rightPanel);

    // Add components to main window
    contentPanel.add(mainSplitPane, BorderLayout.CENTER);
    add(titleBar, BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);

    // Add window resize listener
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
          revalidate();
          repaint();
        });
      }
    });
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
   * Shows an enhanced event edit dialog with more comprehensive options.
   *
   * @param event       the event to edit
   * @param isRecurring whether the event is recurring
   */
  public void showEventEditDialog(Event event, boolean isRecurring) {
    // Use the interface type rather than the concrete implementation
    view.dialog.IEventEditDialog dialog = new view.dialog.EnhancedEventEditDialog(this,
            event, isRecurring);
    boolean confirmed = dialog.showDialog();

    if (confirmed) {
      // Get updated values from the dialog
      String updatedSubject = dialog.getSubject();
      String updatedLocation = dialog.getEventLocation();
      String updatedDescription = dialog.getDescription();
      LocalDateTime updatedStartDateTime = dialog.getStartDateTime();
      LocalDateTime updatedEndDateTime = dialog.getEndDateTime();
      boolean updatedPrivate = dialog.isPrivate();
      boolean updatedAllDay = dialog.isAllDay();

      // Note: Recurring flag is captured but not implemented in this version
      // This would be used in a future enhancement

      // Create a new event with the updated values
      Event updatedEvent = new Event(updatedSubject, updatedStartDateTime, updatedEndDateTime,
              updatedDescription, updatedLocation, !updatedPrivate);

      // Handle all-day events by setting appropriate start and end times
      if (updatedAllDay) {
        // Update to full-day time range if marked as all-day event
        updatedEvent.setStartDateTime(updatedStartDateTime.toLocalDate().atStartOfDay());
        updatedEvent.setEndDateTime(updatedStartDateTime.toLocalDate().atTime(23,
                59, 59));
      }
      // Notify the controller that the event has been updated
      if (guiController != null) {
        try {
          guiController.onEventUpdated(updatedEvent);
        } catch (Exception e) {
          // If an exception occurs (likely ConflictingEventException), show error to user
          showErrorMessage("Cannot update event: " + e.getMessage() +
                  "\n\nEvents cannot conflict with each other. Editing an existing event " +
                  "that would create a conflict with another existing event is not allowed.");
        }
      }
    }
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

  /**
   * Updates the status for a specific date.
   *
   * @param date       the date for which to update status
   * @param isBusy     whether the date has events
   * @param eventCount the number of events on that date
   */
  public void updateStatus(LocalDate date, boolean isBusy, int eventCount) {
    System.out.println("[DEBUG] Updating status for date " + date + ": "
            + (isBusy ? "Busy" : "Available") + ", " + eventCount + " events");
    calendarPanel.updateDateStatus(date, isBusy, eventCount);
  }

  /**
   * Updates the events for a specific date.
   *
   * @param date   the date for which to update events
   * @param events the list of events on that date
   */
  public void updateEvents(LocalDate date, List<Event> events) {
    System.out.println("[DEBUG] Updating events for date " + date + " with "
            + (events != null ? events.size() : 0) + " events");
    calendarPanel.updateDateEvents(date, events);
    updateEventList(events);
  }

  /**
   * Updates the event list results panel to display events under the calendar grid.
   * This keeps the create/edit panel on the right side intact.
   *
   * @param startDate The start date of the range (or a single date)
   * @param endDate   The end date of the range (same as startDate for single day)
   * @param events    The list of events to display
   */
  @Override
  public void updateEventListResultsPanel(LocalDate startDate, LocalDate endDate,
                                          List<Event> events) {
    // Clear previous content
    eventListResultsPanel.removeAll();

    if (events == null || events.isEmpty()) {
      // If no events, display a message
      JLabel noEventsLabel = new JLabel("No events for " +
              (startDate.equals(endDate) ? "date " + startDate : "range "
                      + startDate + " to " + endDate));
      noEventsLabel.setHorizontalAlignment(SwingConstants.CENTER);
      noEventsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
      eventListResultsPanel.add(noEventsLabel, BorderLayout.CENTER);
    } else {
      // Create a panel to display the events in a list format
      JPanel listPanel = new JPanel();
      listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
      listPanel.setBackground(Color.WHITE);

      // Add a header
      JLabel headerLabel = new JLabel("Events for " +
              (startDate.equals(endDate) ? "date " + startDate : "range "
                      + startDate + " to " + endDate) +
              " (" + events.size() + " events)");
      headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
      headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
      listPanel.add(headerLabel);

      // Add each event to the list
      for (Event event : events) {
        JPanel eventItemPanel = createEventItemPanel(event);
        eventItemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        listPanel.add(eventItemPanel);
        listPanel.add(Box.createVerticalStrut(5)); // Add spacing between events
      }

      // Add the list panel to a scroll pane in the results panel
      JScrollPane scrollPane = new JScrollPane(listPanel);
      scrollPane.setBorder(null);
      scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
      eventListResultsPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // Update the UI
    eventListResultsPanel.revalidate();
    eventListResultsPanel.repaint();
  }

  /**
   * Creates a panel for displaying a single event in the event list results.
   *
   * @param event The event to display
   * @return A panel containing the event details
   */
  private JPanel createEventItemPanel(Event event) {
    JPanel panel = new JPanel(new BorderLayout(10, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
    ));

    // Left side - subject and time
    JPanel detailsPanel = new JPanel();
    detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    detailsPanel.setBackground(Color.WHITE);

    // Subject with bold font
    JLabel subjectLabel = new JLabel(event.getSubject());
    subjectLabel.setFont(new Font("Arial", Font.BOLD, 14));
    subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(subjectLabel);

    // Date and time - Convert from UTC to local timezone for display
    TimeZoneHandler timezoneHandler = new TimeZoneHandler();
    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

    // Convert start and end times from UTC to local time
    LocalDateTime localStartDateTime = timezoneHandler.convertFromUTC(event.getStartDateTime(),
            systemTimezone);
    LocalDateTime localEndDateTime = timezoneHandler.convertFromUTC(event.getEndDateTime(),
            systemTimezone);

    String dateTimeStr = "" + localStartDateTime.toLocalDate() +
            " " + localStartDateTime.toLocalTime() +
            " - " + localEndDateTime.toLocalTime();
    JLabel timeLabel = new JLabel(dateTimeStr);
    timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    detailsPanel.add(timeLabel);

    // Description (if present)
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      JLabel descLabel = new JLabel(event.getDescription());
      descLabel.setFont(new Font("Arial", Font.ITALIC, 12));
      descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      detailsPanel.add(descLabel);
    }

    panel.add(detailsPanel, BorderLayout.CENTER);

    // Right side - Edit button
    JButton editButton = new JButton("Edit");
    editButton.addActionListener(e -> {
      // When clicked, display this event in the event panel for editing
      eventPanel.displayEvent(event);
      eventPanel.setPanelMode(GUIEventPanel.PanelMode.EDIT);
    });
    panel.add(editButton, BorderLayout.EAST);

    return panel;
  }

  /**
   * Updates the calendar view.
   *
   * @param calendar the calendar to display
   */
  @Override
  public void updateCalendarView(ICalendar calendar) {
    if (calendar != null) {
      calendarPanel.updateCalendar(calendar);
      calendarSelectorPanel.setSelectedCalendar(calendar.toString());
    }
  }

  @Override
  public void updateEventList(List<Event> events) {
    if (events != null && !events.isEmpty()) {
      // Update calendar panel events
      calendarPanel.updateEvents(events);

      // Get the date of the first event
      LocalDate eventDate = events.get(0).getStartDateTime().toLocalDate();

      // Display the events in the results panel under the calendar
      updateEventListResultsPanel(eventDate, eventDate, events);

      // If we have events, select the first one to display its details in the right panel
      Event firstEvent = events.get(0);
      System.out.println("[DEBUG] Auto-selected first event: " + firstEvent.getSubject());
      eventPanel.displayEvent(firstEvent);

      // Update the calendar panel with the date of the first event
      calendarPanel.updateEventList(eventDate);
    } else {
      // Clear the event panel if no events
      eventPanel.clearForm();

      // Show "no events" in the results panel
      updateEventListResultsPanel(LocalDate.now(), LocalDate.now(), null);
    }
  }

  /**
   * Refreshes the calendar view with current data.
   */
  public void refreshCalendarView() {
    calendarPanel.repaint();
    calendarPanel.revalidate();
  }

  /**
   * Refreshes the event view with current data.
   */
  public void refreshEventView() {
    if (eventPanel != null) {
      eventPanel.refreshView();
    }
  }

  /**
   * Refreshes all calendar and event views.
   */
  public void refreshPanels() {
    refreshCalendarView();
    refreshEventView();
  }

  @Override
  public void updateRecurringEventList(List<RecurringEvent> recurringEvents) {
    if (recurringEvents != null) {
      calendarPanel.updateRecurringEvents(recurringEvents);
    }
  }

  @Override
  public void showEventDetails(Event event) {
    System.out.println("[DEBUG] GUIView.showEventDetails called for event: "
            + (event != null ? event.getSubject() : "null"));
    eventPanel.displayEvent(event);
  }

  @Override
  public void clearEventDetails() {
    eventPanel.clearForm();
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    calendarSelectorPanel.updateCalendarList(calendarNames);
  }

  @Override
  public void setSelectedCalendar(String calendarName) {
    calendarSelectorPanel.setSelectedCalendar(calendarName);
  }

  @Override
  public String getSelectedCalendar() {
    return calendarSelectorPanel.getSelectedCalendarName();
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
    SwingUtilities.invokeLater(() -> {
      calendarPanel.refresh();
      eventPanel.refresh();
      calendarSelectorPanel.refresh();
      revalidate();
      repaint();
    });
  }

  @Override
  public String readCommand() {
    // Not used in GUI mode
    return null;
  }

  @Override
  public void displayMessage(String message) {
    messageArea.setText(message);
  }

  @Override
  public void displayError(String error) {
    JOptionPane.showMessageDialog(
            this,
            error,
            "Error",
            JOptionPane.ERROR_MESSAGE
    );
  }

  /**
   * Shows an error message dialog.
   *
   * @param message the error message to display
   */
  public void showErrorMessage(String message) {
    displayError(message);
  }

  /**
   * Shows an information message dialog.
   *
   * @param message the information message to display
   */
  public void showInfoMessage(String message) {
    JOptionPane.showMessageDialog(
            this,
            message,
            "Information",
            JOptionPane.INFORMATION_MESSAGE
    );
  }

  public void onEventSelected(Event event) {
    eventPanel.displayEvent(event);
  }

  public void onRecurringEventSelected(RecurringEvent event) {
    eventPanel.displayRecurringEvent(event);
  }

  public void onEventCreated(Event event) {
    displayMessage("Event created successfully");
  }

  public void onRecurringEventCreated(RecurringEvent event) {
    displayMessage("Recurring event created successfully");
  }

  public void onEventUpdated(Event event) {
    displayMessage("Event updated successfully");
  }

  public void onRecurringEventUpdated(RecurringEvent event) {
    displayMessage("Recurring event updated successfully");
  }

  /**
   * Handles the successful import of calendar events.
   * This method displays a success message, shows a popup notification,
   * and refreshes the calendar view to display the newly imported events.
   * It updates the calendar panel with all events from the current calendar
   * and forces a complete refresh of the UI.
   */
  public void onImportSuccess(String message) {
    System.out.println("[DEBUG] GUIView.onImportSuccess called with message: " + message);
    displayMessage(message);

    // Show success popup
    System.out.println("[DEBUG] Showing import success popup");
    exportImportPanel.showImportSuccess(message);

    // Refresh the calendar view to show the imported events
    LocalDate currentDate = calendarPanel.getSelectedDate();
    System.out.println("[DEBUG] Refreshing calendar view after import for date: " + currentDate);

    // Get the current calendar's events
    if (calendarViewModel.getCurrentCalendar() != null) {
      // Get all events for the current calendar
      List<Event> events = calendarViewModel.getCurrentCalendar().getAllEvents();
      System.out.println("[DEBUG] Found " + events.size() + " events in calendar after import");

      // Update the calendar panel with all events
      calendarPanel.updateEvents(events);

      // Force a complete refresh of the calendar display
      calendarPanel.updateCalendar(calendarViewModel.getCurrentCalendar());
      System.out.println("[DEBUG] Calendar panel updated with imported events");
      
      // Get events for the currently selected date
      List<Event> eventsForCurrentDate = calendarViewModel.getCurrentCalendar().getEventsOnDate(currentDate);
      System.out.println("[DEBUG] Found " + eventsForCurrentDate.size() + " events for current date " + currentDate);
      
      // Update the event list results panel with events for the current date
      updateEventListResultsPanel(currentDate, currentDate, eventsForCurrentDate);
      System.out.println("[DEBUG] Event list results panel updated with events for date: " + currentDate);

      // Refresh the entire view
      refreshView();
    }
  }

  /**
   * Updates the selected date in the view.
   *
   * @param date the date to update to
   */
  @Override
  public void updateSelectedDate(LocalDate date) {
    if (date != null) {
      calendarPanel.setSelectedDate(date);
      eventPanel.setDate(date);
    }
  }
}
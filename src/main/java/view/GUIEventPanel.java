package view;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.event.Event;
import model.event.RecurringEvent;
import utilities.TimeZoneHandler;

/**
 * Panel class that handles event creation, editing, and display.
 */
public class GUIEventPanel extends JPanel {
  // Panel modes
  public enum PanelMode {
    CREATE("Create Event"),
    EDIT("Edit Event"),
    COPY("Copy Event"),
    VIEW("View Event");

    private final String displayName;

    PanelMode(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  private PanelMode currentMode = PanelMode.CREATE;
  private final JTextField subjectField;
  private final JTextArea descriptionArea;
  private final JSpinner dateSpinner;
  private final JSpinner startTimeSpinner;
  private final JSpinner endTimeSpinner;
  private final JTextField locationField;
  private final JCheckBox recurringCheckBox;
  private final JPanel recurringOptionsPanel;
  private final ArrayList<JCheckBox> weekdayCheckboxes;
  private final JSpinner occurrencesSpinner;
  private final JSpinner untilDateSpinner;
  private final JButton saveButton;
  private final JButton cancelButton;
  private final JButton editButton;
  private final JButton copyButton;

  // Mode indicator label
  private final JLabel modeLabel;

  // Copy options components
  private JPanel copyOptionsPanel;
  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;
  private JSpinner targetStartTimeSpinner;
  private JSpinner targetEndTimeSpinner;

  // Current event being edited or copied
  private Event currentEvent;
  private EventPanelListener listener;
  private final Map<JComponent, JLabel> errorLabels;
  private final JPanel recurringPanel;
  private final JCheckBox allDayCheckBox;
  private JCheckBox privateEventCheckBox;
  private JCheckBox autoDeclineCheckBox;
  private final TimeZoneHandler timezoneHandler;

  // Define consistent colors
  private static final Color HEADER_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_LIGHT_COLOR = new Color(0xe6f2ff);
  private static final Color TEXT_COLOR = new Color(0x333333);

  /**
   * Data class to hold event information.
   */
  public static class EventData {
    public String subject;
    public String location;
    public String description;
    public LocalTime startTime;
    public LocalTime endTime;
    public boolean isRecurring;
    public Set<DayOfWeek> weekdays;
    public int occurrences;
    public LocalDate untilDate;
    public LocalDate date;

    public EventData() {
      this.date = LocalDate.now();
      this.startTime = LocalTime.of(9, 0);
      this.endTime = LocalTime.of(10, 0);
      this.isRecurring = false;
      this.occurrences = 1;
    }
  }

  /**
   * Interface for event panel events.
   * This interface defines callbacks for the controller to handle UI events.
   * It uses the EventFormData class to transfer data from the view to the controller
   * without exposing any business logic in the view layer.
   */
  public interface EventPanelListener {
    /**
     * Called when a new event is saved.
     *
     * @param formData the form data collected from UI components
     */
    void onEventSaved(EventFormData formData);

    /**
     * Called when event creation/editing is cancelled.
     */
    void onEventCancelled();

    /**
     * Called when an existing event is updated.
     *
     * @param formData the form data collected from UI components
     */
    void onEventUpdated(EventFormData formData);

    /**
     * Called when an event is copied.
     *
     * @param targetCalendarName  the name of the target calendar
     * @param targetStartDateTime the target start date/time
     * @param targetEndDateTime   the target end date/time
     */
    void onEventCopied(String targetCalendarName, LocalDateTime targetStartDateTime, LocalDateTime targetEndDateTime);

    List<String> getAvailableCalendarNames();
  }

  /**
   * Constructs a new GUIEventPanel.
   */
  public GUIEventPanel() {
    // Initialize components
    subjectField = new JTextField(20);
    dateSpinner = new JSpinner(new SpinnerDateModel());
    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    locationField = new JTextField(20);
    descriptionArea = new JTextArea(5, 20);
    allDayCheckBox = new JCheckBox("All Day Event");
    recurringCheckBox = new JCheckBox("Recurring Event");
    privateEventCheckBox = new JCheckBox("Private Event");
    autoDeclineCheckBox = new JCheckBox("Auto-decline");
    weekdayCheckboxes = new ArrayList<>();
    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    untilDateSpinner = new JSpinner(new SpinnerDateModel());
    saveButton = new JButton("Save");
    cancelButton = new JButton("Cancel");
    editButton = new JButton("Edit");
    copyButton = new JButton("Copy");
    
    // Hide edit and copy buttons as per requirements
    editButton.setVisible(false);
    copyButton.setVisible(false);

    // Explicitly set all buttons to be visible
    saveButton.setVisible(true);
    cancelButton.setVisible(true);
    copyButton.setVisible(true);

    System.out.println("[DEBUG] All buttons created and set to visible");
    recurringOptionsPanel = new JPanel(new GridLayout(0, 1));
    recurringPanel = new JPanel(new BorderLayout());
    errorLabels = new HashMap<>();
    timezoneHandler = new TimeZoneHandler();

    // Initialize mode label
    modeLabel = new JLabel(PanelMode.CREATE.getDisplayName());
    modeLabel.setFont(modeLabel.getFont().deriveFont(Font.BOLD, 14f));
    modeLabel.setForeground(HEADER_COLOR);

    // Initialize copy options components
    copyOptionsPanel = new JPanel(new GridBagLayout());
    copyOptionsPanel.setBorder(BorderFactory.createTitledBorder("Copy Options"));
    targetCalendarCombo = new JComboBox<>();
    targetDateSpinner = new JSpinner(new SpinnerDateModel());
    targetStartTimeSpinner = new JSpinner(new SpinnerDateModel());
    targetEndTimeSpinner = new JSpinner(new SpinnerDateModel());

    // Configure copy options spinners
    targetDateSpinner.setEditor(new JSpinner.DateEditor(targetDateSpinner, "MM/dd/yyyy"));
    targetStartTimeSpinner.setEditor(new JSpinner.DateEditor(targetStartTimeSpinner, "HH:mm"));
    targetEndTimeSpinner.setEditor(new JSpinner.DateEditor(targetEndTimeSpinner, "HH:mm"));

    // Hide copy options panel by default
    copyOptionsPanel.setVisible(false);

    // Configure spinners
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
    endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

    // Configure text area
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    // Setup components
    setupComponents();
    setupLayout();
    setupListeners();
  }

  private void setupComponents() {
    // Configure date spinner
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy"));
    ((JSpinner.DefaultEditor) dateSpinner.getEditor()).getTextField().setEditable(true);

    // Configure time spinners
    startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "HH:mm"));
    endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));
    ((JSpinner.DefaultEditor) startTimeSpinner.getEditor()).getTextField().setEditable(true);
    ((JSpinner.DefaultEditor) endTimeSpinner.getEditor()).getTextField().setEditable(true);

    // Configure description area
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    // Configure recurring options
    String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    for (String weekday : weekdays) {
      JCheckBox checkbox = new JCheckBox(weekday);
      weekdayCheckboxes.add(checkbox);
      recurringOptionsPanel.add(checkbox);
    }

    JPanel occurrencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    occurrencesPanel.add(new JLabel("Number of occurrences:"));
    occurrencesPanel.add(occurrencesSpinner);
    recurringOptionsPanel.add(occurrencesPanel);

    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    untilPanel.add(new JLabel("Until date:"));
    untilPanel.add(untilDateSpinner);
    recurringOptionsPanel.add(untilPanel);

    // Initially hide recurring options
    recurringOptionsPanel.setVisible(false);

    // Initialize error labels
    errorLabels.clear();
    JLabel subjectErrorLabel = new JLabel("");
    subjectErrorLabel.setForeground(Color.RED);
    subjectErrorLabel.setVisible(false);
    errorLabels.put(subjectField, subjectErrorLabel);

    JLabel timeErrorLabel = new JLabel("");
    timeErrorLabel.setForeground(Color.RED);
    timeErrorLabel.setVisible(false);
    errorLabels.put(endTimeSpinner, timeErrorLabel);

    JLabel weekdayErrorLabel = new JLabel("");
    weekdayErrorLabel.setForeground(Color.RED);
    weekdayErrorLabel.setVisible(false);
    errorLabels.put(weekdayCheckboxes.get(0), weekdayErrorLabel);

    JLabel untilDateErrorLabel = new JLabel("");
    untilDateErrorLabel.setForeground(Color.RED);
    untilDateErrorLabel.setVisible(false);
    errorLabels.put(untilDateSpinner, untilDateErrorLabel);
  }

  private void setupCopyOptionsPanel() {
    // Set up the copy options panel
    copyOptionsPanel = new JPanel(new GridBagLayout());
    copyOptionsPanel.setBorder(BorderFactory.createTitledBorder("Copy Options"));
    
    // Create components
    JLabel targetCalendarLabel = new JLabel("Target Calendar:");
    targetCalendarCombo = new JComboBox<>();
    
    JLabel targetDateLabel = new JLabel("Target Date:");
    targetDateSpinner = new JSpinner(new SpinnerDateModel());
    targetDateSpinner.setEditor(new JSpinner.DateEditor(targetDateSpinner, "MM/dd/yyyy"));
    
    JLabel targetStartTimeLabel = new JLabel("Start Time:");
    targetStartTimeSpinner = new JSpinner(new SpinnerDateModel());
    targetStartTimeSpinner.setEditor(new JSpinner.DateEditor(targetStartTimeSpinner, "HH:mm"));
    
    JLabel targetEndTimeLabel = new JLabel("End Time:");
    targetEndTimeSpinner = new JSpinner(new SpinnerDateModel());
    targetEndTimeSpinner.setEditor(new JSpinner.DateEditor(targetEndTimeSpinner, "HH:mm"));
    
    // Create copy options for recurring events
    JPanel copyTypePanel = new JPanel();
    copyTypePanel.setLayout(new BoxLayout(copyTypePanel, BoxLayout.Y_AXIS));
    copyTypePanel.setBorder(BorderFactory.createTitledBorder("Copy Options"));
    
    ButtonGroup copyTypeGroup = new ButtonGroup();
    JRadioButton singleEventRadio = new JRadioButton("Copy as single event", true);
    JRadioButton allEventsRadio = new JRadioButton("Copy all occurrences");
    JRadioButton rangeEventsRadio = new JRadioButton("Copy date range");
    
    copyTypeGroup.add(singleEventRadio);
    copyTypeGroup.add(allEventsRadio);
    copyTypeGroup.add(rangeEventsRadio);
    
    copyTypePanel.add(singleEventRadio);
    copyTypePanel.add(allEventsRadio);
    copyTypePanel.add(rangeEventsRadio);
    
    // Add components to panel
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    copyOptionsPanel.add(targetCalendarLabel, gbc);
    
    gbc.gridx = 1;
    gbc.gridwidth = 2;
    copyOptionsPanel.add(targetCalendarCombo, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    copyOptionsPanel.add(targetDateLabel, gbc);
    
    gbc.gridx = 1;
    gbc.gridwidth = 2;
    copyOptionsPanel.add(targetDateSpinner, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    copyOptionsPanel.add(targetStartTimeLabel, gbc);
    
    gbc.gridx = 1;
    copyOptionsPanel.add(targetStartTimeSpinner, gbc);
    
    gbc.gridx = 0;
    gbc.gridy = 3;
    copyOptionsPanel.add(targetEndTimeLabel, gbc);
    
    gbc.gridx = 1;
    copyOptionsPanel.add(targetEndTimeSpinner, gbc);
    
    // Add copy type options for recurring events
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 3;
    copyOptionsPanel.add(copyTypePanel, gbc);
    
    // Add listeners to radio buttons
    singleEventRadio.addActionListener(e -> {
      System.out.println("[DEBUG] Copy as single event selected");
      targetDateSpinner.setEnabled(true);
      targetStartTimeSpinner.setEnabled(true);
      targetEndTimeSpinner.setEnabled(true);
    });
    
    allEventsRadio.addActionListener(e -> {
      System.out.println("[DEBUG] Copy all occurrences selected");
      // When copying all occurrences, we don't need to specify a target date/time
      targetDateSpinner.setEnabled(false);
      targetStartTimeSpinner.setEnabled(false);
      targetEndTimeSpinner.setEnabled(false);
    });
    
    rangeEventsRadio.addActionListener(e -> {
      System.out.println("[DEBUG] Copy date range selected");
      targetDateSpinner.setEnabled(true);
      targetStartTimeSpinner.setEnabled(true);
      targetEndTimeSpinner.setEnabled(true);
    });
    
    // Hide by default
    copyOptionsPanel.setVisible(false);
  }

  private void setupListeners() {
    // Edit button
    editButton.addActionListener(e -> {
      System.out.println("[DEBUG] Edit button clicked for event: " + (currentEvent != null ? currentEvent.getSubject() : "null"));
      if (currentEvent != null) {
        setPanelMode(PanelMode.EDIT);
        enableFormEditing(true);
        
        // Make sure the form is populated with the current event data
        subjectField.setText(currentEvent.getSubject());
        locationField.setText(currentEvent.getLocation());
        descriptionArea.setText(currentEvent.getDescription());
        
        // Set date and time values
        LocalDateTime startDateTime = currentEvent.getStartDateTime();
        LocalDateTime endDateTime = currentEvent.getEndDateTime();
        
        // Update date spinner
        Calendar cal = Calendar.getInstance();
        cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
        dateSpinner.setValue(cal.getTime());
        
        // Update time spinners
        cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
        cal.set(Calendar.MINUTE, startDateTime.getMinute());
        startTimeSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
        cal.set(Calendar.MINUTE, endDateTime.getMinute());
        endTimeSpinner.setValue(cal.getTime());
        
        // Set all day checkbox
        allDayCheckBox.setSelected(currentEvent.isAllDay());
        startTimeSpinner.setEnabled(!currentEvent.isAllDay());
        endTimeSpinner.setEnabled(!currentEvent.isAllDay());
        
        // Handle recurring event
        if (currentEvent instanceof RecurringEvent) {
          RecurringEvent recurringEvent = (RecurringEvent) currentEvent;
          recurringCheckBox.setSelected(true);
          recurringOptionsPanel.setVisible(true);
          
          // Set weekdays
          Set<DayOfWeek> weekdays = recurringEvent.getRepeatDays();
          for (int i = 0; i < weekdayCheckboxes.size(); i++) {
            weekdayCheckboxes.get(i).setSelected(weekdays.contains(DayOfWeek.of((i + 1) % 7 + 1)));
          }
          
          // Set occurrences and until date
          occurrencesSpinner.setValue(recurringEvent.getOccurrences());
          Calendar untilCal = Calendar.getInstance();
          untilCal.set(recurringEvent.getEndDate().getYear(), 
                      recurringEvent.getEndDate().getMonthValue() - 1, 
                      recurringEvent.getEndDate().getDayOfMonth());
          untilDateSpinner.setValue(untilCal.getTime());
        } else {
          recurringCheckBox.setSelected(false);
          recurringOptionsPanel.setVisible(false);
        }
        
        System.out.println("[DEBUG] Event form populated for editing");
      } else {
        System.out.println("[DEBUG] Cannot edit: No event selected");
      }
    });

    // Copy button
    copyButton.addActionListener(e -> {
      System.out.println("[DEBUG] Copy button clicked for event: " + (currentEvent != null ? currentEvent.getSubject() : "null"));
      if (currentEvent != null) {
        setPanelMode(PanelMode.COPY);
        
        // Populate the copy options panel with available calendars
        if (listener != null) {
          // Request available calendars from the controller
          List<String> calendarNames = new ArrayList<>();
          try {
            // Get calendar names from the listener
            calendarNames = listener.getAvailableCalendarNames();
            setAvailableCalendars(calendarNames);
            System.out.println("[DEBUG] Available calendars for copy: " + calendarNames);
          } catch (Exception ex) {
            System.out.println("[DEBUG] Error getting calendar names: " + ex.getMessage());
          }
        }
        
        // Set target date/time to match the current event
        Calendar cal = Calendar.getInstance();
        LocalDateTime startDateTime = currentEvent.getStartDateTime();
        LocalDateTime endDateTime = currentEvent.getEndDateTime();
        
        cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
        targetDateSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
        cal.set(Calendar.MINUTE, startDateTime.getMinute());
        targetStartTimeSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
        cal.set(Calendar.MINUTE, endDateTime.getMinute());
        targetEndTimeSpinner.setValue(cal.getTime());
        
        // Show copy options panel
        copyOptionsPanel.setVisible(true);
        System.out.println("[DEBUG] Copy options panel displayed");
      } else {
        System.out.println("[DEBUG] Cannot copy: No event selected");
      }
    });

    // Add document listeners to text fields for real-time validation
    subjectField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkSubject();
      }

      public void removeUpdate(DocumentEvent e) {
        checkSubject();
      }

      public void insertUpdate(DocumentEvent e) {
        checkSubject();
      }

      private void checkSubject() {
        System.out.println("[DEBUG] Subject field changed: " + subjectField.getText());
      }
    });

    locationField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkLocation();
      }

      public void removeUpdate(DocumentEvent e) {
        checkLocation();
      }

      public void insertUpdate(DocumentEvent e) {
        checkLocation();
      }

      private void checkLocation() {
        System.out.println("[DEBUG] Location field changed: " + locationField.getText());
      }
    });

    descriptionArea.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        checkDescription();
      }

      public void removeUpdate(DocumentEvent e) {
        checkDescription();
      }

      public void insertUpdate(DocumentEvent e) {
        checkDescription();
      }

      private void checkDescription() {
        System.out.println("[DEBUG] Description changed: " + descriptionArea.getText());
      }
    });

    // Add change listeners to spinners
    dateSpinner.addChangeListener(e -> {
      Date date = (Date) dateSpinner.getValue();
      System.out.println("[DEBUG] Date changed: " + date);
    });

    startTimeSpinner.addChangeListener(e -> {
      Date time = (Date) startTimeSpinner.getValue();
      System.out.println("[DEBUG] Start time changed: " + time);
    });

    endTimeSpinner.addChangeListener(e -> {
      Date time = (Date) endTimeSpinner.getValue();
      System.out.println("[DEBUG] End time changed: " + time);
    });

    // Recurring checkbox
    recurringCheckBox.addActionListener(e -> {
      boolean isRecurring = recurringCheckBox.isSelected();
      System.out.println("[DEBUG] Recurring checkbox changed: " + isRecurring);
      recurringOptionsPanel.setVisible(isRecurring);

      // Log weekday selections if recurring is enabled
      if (isRecurring) {
        for (JCheckBox cb : weekdayCheckboxes) {
          cb.addActionListener(we -> System.out.println("[DEBUG] Weekday changed - " + cb.getText() + ": " + cb.isSelected()));
        }
      }

      revalidate();
      repaint();
    });

    // Private Event checkbox
    privateEventCheckBox.addActionListener(e -> {
      boolean isPrivate = privateEventCheckBox.isSelected();
      System.out.println("[DEBUG] Private Event checkbox changed: " + isPrivate);
    });

    // Auto-decline checkbox
    autoDeclineCheckBox.addActionListener(e -> {
      boolean autoDecline = autoDeclineCheckBox.isSelected();
      System.out.println("[DEBUG] Auto-decline checkbox changed: " + autoDecline);
    });

    // Occurrences spinner
    occurrencesSpinner.addChangeListener(e -> {
      int occurrences = (Integer) occurrencesSpinner.getValue();
      System.out.println("[DEBUG] Occurrences changed: " + occurrences);
    });

    // Until date spinner
    untilDateSpinner.addChangeListener(e -> {
      Date untilDate = (Date) untilDateSpinner.getValue();
      System.out.println("[DEBUG] Until date changed: " + untilDate);
    });

    // Save button
    saveButton.addActionListener(e -> {
      System.out.println("[DEBUG] Save button clicked");
      System.out.println("[DEBUG] Current form state:");
      System.out.println("[DEBUG] Subject: " + subjectField.getText());
      System.out.println("[DEBUG] Location: " + locationField.getText());
      System.out.println("[DEBUG] Description: " + descriptionArea.getText());
      System.out.println("[DEBUG] Date: " + dateSpinner.getValue());
      System.out.println("[DEBUG] Start Time: " + startTimeSpinner.getValue());
      System.out.println("[DEBUG] End Time: " + endTimeSpinner.getValue());
      System.out.println("[DEBUG] All Day: " + allDayCheckBox.isSelected());
      System.out.println("[DEBUG] Recurring: " + recurringCheckBox.isSelected());
      System.out.println("[DEBUG] Private: " + privateEventCheckBox.isSelected());
      System.out.println("[DEBUG] Auto-decline: " + autoDeclineCheckBox.isSelected());

      if (validateForm()) {
        System.out.println("[DEBUG] Form validation passed");
        if (listener != null) {
          // Collect form data without any business logic
          EventFormData formData = collectFormData();

          System.out.println("[DEBUG] Creating " + (formData.isRecurring() ? "recurring" : "single") + " event");

          // Notify listeners with the form data
          listener.onEventSaved(formData);

          JOptionPane.showMessageDialog(this,
                  String.format("Event '%s' created successfully at %s",
                          subjectField.getText().trim(),
                          locationField.getText().trim().isEmpty() ? "no location" : locationField.getText().trim()),
                  "Success",
                  JOptionPane.INFORMATION_MESSAGE);
          System.out.println("[DEBUG] Event saved, clearing form");
          clearForm();
        } else {
          System.out.println("[DEBUG] No event listener attached!");
        }
      } else {
        System.out.println("[DEBUG] Form validation failed");
      }
    });

    // Cancel button
    cancelButton.addActionListener(e -> {
      System.out.println("[DEBUG] Cancel button clicked, clearing form");
      clearForm();
      if (listener != null) {
        listener.onEventCancelled();
      }
      System.out.println("[DEBUG] Form cleared");
    });
  }

  private boolean validateForm() {
    System.out.println("[DEBUG] Validating form");
    boolean isValid = true;
    clearErrorLabels();

    // Validate subject
    if (subjectField.getText().trim().isEmpty()) {
      System.out.println("[DEBUG] Validation error: Subject is required");
      showError(subjectField, "Subject is required");
      isValid = false;
    }

    // Validate date and times
    Date selectedDate = (Date) dateSpinner.getValue();
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    // Convert to LocalDateTime in system timezone
    String systemTimezone = timezoneHandler.getSystemDefaultTimezone();

    LocalDateTime startDateTime = selectedDate.toInstant()
            .atZone(ZoneId.of(systemTimezone))
            .toLocalDateTime()
            .withHour(startTime.toInstant().atZone(ZoneId.of(systemTimezone)).getHour())
            .withMinute(startTime.toInstant().atZone(ZoneId.of(systemTimezone)).getMinute());

    LocalDateTime endDateTime = selectedDate.toInstant()
            .atZone(ZoneId.of(systemTimezone))
            .toLocalDateTime()
            .withHour(endTime.toInstant().atZone(ZoneId.of(systemTimezone)).getHour())
            .withMinute(endTime.toInstant().atZone(ZoneId.of(systemTimezone)).getMinute());

    System.out.println("[DEBUG] Start time: " + startDateTime);
    System.out.println("[DEBUG] End time: " + endDateTime);

    if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
      System.out.println("[DEBUG] Validation error: End time must be after start time");
      showError(endTimeSpinner, "End time must be after start time");
      isValid = false;
    }

    // Validate recurring event options
    if (recurringCheckBox.isSelected()) {
      boolean anyWeekdaySelected = weekdayCheckboxes.stream().anyMatch(JCheckBox::isSelected);
      if (!anyWeekdaySelected) {
        System.out.println("[DEBUG] Validation error: Select at least one weekday");
        showError(weekdayCheckboxes.get(0), "Select at least one weekday");
        isValid = false;
      }

      Date untilDate = (Date) untilDateSpinner.getValue();
      if (untilDate.before(selectedDate)) {
        System.out.println("[DEBUG] Validation error: Until date must be after start date");
        showError(untilDateSpinner, "Until date must be after start date");
        isValid = false;
      }
    }

    return isValid;
  }

  private void showError(JComponent component, String message) {
    JLabel errorLabel = errorLabels.get(component);
    if (errorLabel != null) {
      errorLabel.setText(message);
      errorLabel.setVisible(true);
    }
  }

  private void clearErrorLabels() {
    errorLabels.values().forEach(label -> label.setVisible(false));
  }

  /**
   * Enables or disables form editing.
   *
   * @param enabled true to enable editing, false to disable
   */
  private void enableFormEditing(boolean enabled) {
    subjectField.setEditable(enabled);
    locationField.setEditable(enabled);
    descriptionArea.setEditable(enabled);
    dateSpinner.setEnabled(enabled);
    startTimeSpinner.setEnabled(enabled);
    endTimeSpinner.setEnabled(enabled);
    allDayCheckBox.setEnabled(enabled);
    recurringCheckBox.setEnabled(enabled);
    privateEventCheckBox.setEnabled(enabled);
    autoDeclineCheckBox.setEnabled(enabled);

    // Enable/disable recurring options if visible
    if (recurringOptionsPanel.isVisible()) {
      for (JCheckBox cb : weekdayCheckboxes) {
        cb.setEnabled(enabled);
      }
      occurrencesSpinner.setEnabled(enabled);
      untilDateSpinner.setEnabled(enabled);
    }
  }

  /**
   * Sets the panel mode.
   *
   * @param mode the mode to set
   */
  public void setPanelMode(PanelMode mode) {
    System.out.println("[DEBUG] Setting panel mode to: " + mode);
    this.currentMode = mode;
    modeLabel.setText(mode.getDisplayName());

    // Show/hide copy options based on mode
    copyOptionsPanel.setVisible(mode == PanelMode.COPY);
    if (mode == PanelMode.COPY) {
      System.out.println("[DEBUG] Showing copy options panel");
    }

    // Make all buttons visible all the time
    editButton.setVisible(true);
    copyButton.setVisible(true);
    saveButton.setVisible(true);
    cancelButton.setVisible(true);

    // Set appropriate button text and form editing state based on mode
    if (mode == PanelMode.VIEW) {
      enableFormEditing(false);
      saveButton.setText("Save");
    } else if (mode == PanelMode.CREATE) {
      saveButton.setText("Save");
      enableFormEditing(true);
    } else if (mode == PanelMode.EDIT) {
      saveButton.setText("Update");
      enableFormEditing(true);
    } else if (mode == PanelMode.COPY) {
      saveButton.setText("Copy");
      enableFormEditing(true);
    }

    System.out.println("[DEBUG] After setPanelMode - Save button visibility: " + saveButton.isVisible());
    System.out.println("[DEBUG] After setPanelMode - Cancel button visibility: " + cancelButton.isVisible());
    System.out.println("[DEBUG] After setPanelMode - Edit button visibility: " + editButton.isVisible());
    System.out.println("[DEBUG] After setPanelMode - Copy button visibility: " + copyButton.isVisible());

    revalidate();
    repaint();
  }

  /**
   * Gets the current panel mode.
   *
   * @return the current mode
   */
  public PanelMode getPanelMode() {
    return currentMode;
  }

  /**
   * Sets the available calendars for the target calendar combo box.
   *
   * @param calendarNames the names of available calendars
   */
  public void setAvailableCalendars(List<String> calendarNames) {
    targetCalendarCombo.removeAllItems();
    for (String name : calendarNames) {
      targetCalendarCombo.addItem(name);
    }
  }

  /**
   * Gets the selected target calendar name.
   *
   * @return the selected target calendar name
   */
  public String getTargetCalendarName() {
    return (String) targetCalendarCombo.getSelectedItem();
  }

  /**
   * Gets the target date and time for the copied event.
   *
   * @return the target date and time
   */
  public LocalDateTime getTargetDateTime() {
    // Get date and time from target spinners
    Date selectedDate = (Date) targetDateSpinner.getValue();
    Date startTime = (Date) targetStartTimeSpinner.getValue();

    // Combine date and time
    return selectedDate.toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .withHour(startTime.toInstant().atZone(ZoneOffset.UTC).getHour())
            .withMinute(startTime.toInstant().atZone(ZoneOffset.UTC).getMinute());
  }

  /**
   * Gets the target end date and time for the copied event.
   *
   * @return the target end date and time
   */
  public LocalDateTime getTargetEndDateTime() {
    // Get date and time from target spinners
    Date selectedDate = (Date) targetDateSpinner.getValue();
    Date endTime = (Date) targetEndTimeSpinner.getValue();

    // Combine date and time
    return selectedDate.toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .withHour(endTime.toInstant().atZone(ZoneOffset.UTC).getHour())
            .withMinute(endTime.toInstant().atZone(ZoneOffset.UTC).getMinute());
  }

  /**
   * Gets the current event being edited or copied.
   *
   * @return the current event
   */
  public Event getCurrentEvent() {
    return currentEvent;
  }

  /**
   * Displays an event in the form.
   *
   * @param event the event to display
   */
  public void displayEvent(Event event) {
    System.out.println("[DEBUG] displayEvent called for event: " + event.getSubject());
    System.out.println("[DEBUG] Event details: Start=" + event.getStartDateTime() + 
                     ", End=" + event.getEndDateTime() + 
                     ", Location=" + event.getLocation() + 
                     ", AllDay=" + event.isAllDay() + 
                     ", Type=" + (event instanceof RecurringEvent ? "Recurring" : "Single"));
    
    // Set the panel to view mode
    setPanelMode(PanelMode.VIEW);

    // No need to explicitly set button visibility here as setPanelMode now makes all buttons visible

    System.out.println("[DEBUG] displayEvent - After setPanelMode - Edit button visibility: " + editButton.isVisible());
    System.out.println("[DEBUG] displayEvent - After setPanelMode - Copy button visibility: " + copyButton.isVisible());
    System.out.println("[DEBUG] displayEvent - After setPanelMode - Save button visibility: " + saveButton.isVisible());

    // Force repaint to ensure buttons are visible
    revalidate();
    repaint();
    this.currentEvent = event;
    subjectField.setText(event.getSubject());
    locationField.setText(event.getLocation());
    descriptionArea.setText(event.getDescription());
    dateSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    startTimeSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    endTimeSpinner.setValue(Date.from(event.getEndDateTime().toInstant(ZoneOffset.UTC)));
    recurringCheckBox.setSelected(false);
    recurringOptionsPanel.setVisible(false);
    allDayCheckBox.setSelected(event.isAllDay());
    startTimeSpinner.setEnabled(!event.isAllDay());
    endTimeSpinner.setEnabled(!event.isAllDay());

    // Also set the target date/time spinners for copy mode
    targetDateSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    targetStartTimeSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
    targetEndTimeSpinner.setValue(Date.from(event.getEndDateTime().toInstant(ZoneOffset.UTC)));
  }

  /**
   * Displays a recurring event in the form.
   *
   * @param event the recurring event to display
   */
  public void displayRecurringEvent(RecurringEvent event) {
    displayEvent(event);
    recurringCheckBox.setSelected(true);
    recurringOptionsPanel.setVisible(true);

    // Get the weekdays from the event
    Set<DayOfWeek> weekdays = event.getRepeatDays();

    // Find indices of the weekdays in the list
    int[] indices = new int[weekdays.size()];
    int index = 0;
    for (int i = 0; i < weekdayCheckboxes.size(); i++) {
      if (weekdays.contains(DayOfWeek.of(i + 1))) {
        indices[index++] = i;
      }
    }

    weekdayCheckboxes.forEach(cb -> cb.setSelected(false));
    for (int i : indices) {
      weekdayCheckboxes.get(i).setSelected(true);
    }
    occurrencesSpinner.setValue(event.getOccurrences());
    untilDateSpinner.setValue(Date.from(event.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
  }

  /**
   * Sets the current date.
   *
   * @param date the date to set
   */
  public void setDate(LocalDate date) {
    if (date != null) {
      dateSpinner.setValue(Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
  }

  /**
   * Clears the form.
   */
  public void clearForm() {
    System.out.println("[DEBUG] Clearing form");
    subjectField.setText("");
    locationField.setText("");
    descriptionArea.setText("");
    recurringCheckBox.setSelected(false);
    recurringOptionsPanel.setVisible(false);
    allDayCheckBox.setSelected(false);
    startTimeSpinner.setEnabled(true);
    endTimeSpinner.setEnabled(true);
    
    // Reset date and time spinners to current values
    Calendar cal = Calendar.getInstance();
    dateSpinner.setValue(cal.getTime());
    cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) / 15) * 15); // Round to nearest 15 minutes
    startTimeSpinner.setValue(cal.getTime());
    cal.add(Calendar.HOUR_OF_DAY, 1);
    endTimeSpinner.setValue(cal.getTime());
    System.out.println("[DEBUG] Form fields reset to default values");
    
    // Reset to create mode
    setPanelMode(PanelMode.CREATE);

    // No need to explicitly set button visibility here as setPanelMode now makes all buttons visible

    System.out.println("[DEBUG] clearForm - After setPanelMode - Save button visibility: " + saveButton.isVisible());
    System.out.println("[DEBUG] clearForm - After setPanelMode - Edit button visibility: " + editButton.isVisible());
    System.out.println("[DEBUG] clearForm - After setPanelMode - Copy button visibility: " + copyButton.isVisible());
    
    // Reset current event
    currentEvent = null;
    System.out.println("[DEBUG] Current event reset to null");

    revalidate();
    repaint();
  }

  /**
   * Adds a listener for event panel events.
   *
   * @param listener the listener to add
   */
  public void addEventPanelListener(EventPanelListener listener) {
    this.listener = listener;
  }

  /**
   * Refreshes the event panel.
   */
  public void refresh() {
    clearForm();
  }

  /**
   * Updates an event in the form.
   *
   * @param event the event to update
   */
  /**
   * Collects form data and notifies the listener to update an event.
   * This method only collects UI data and delegates the actual event processing to the controller.
   */
  /**
   * Updates the event list with the provided events.
   *
   * @param events the list of events to display
   */
  public void updateEventList(List<Event> events) {
    if (events == null || events.isEmpty()) {
      // Clear the event list display
      clearEventDisplay();
      return;
    }
    
    // Update the event display with the new events
    displayEvents(events);
  }
  
  /**
   * Displays a list of events in the event panel.
   *
   * @param events the list of events to display
   */
  private void displayEvents(List<Event> events) {
    // Clear current display
    clearEventDisplay();
    
    // Create a panel to hold all event items
    JPanel eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);
    
    // Add each event to the display
    for (Event event : events) {
      JPanel eventItem = createEventDisplayItem(event);
      eventsContainer.add(eventItem);
      eventsContainer.add(Box.createVerticalStrut(5));
    }
    
    // Add the events container to the scroll pane
    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    // Add to the panel
    removeAll();
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    revalidate();
    repaint();
  }
  
  /**
   * Creates a display item for an event.
   *
   * @param event the event to create a display for
   * @return a panel containing the event information
   */
  private JPanel createEventDisplayItem(Event event) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    panel.setBackground(Color.WHITE);
    
    // Create a label for the event title
    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
    panel.add(titleLabel, BorderLayout.NORTH);
    
    // Create a formatted string for the event details
    StringBuilder details = new StringBuilder();
    details.append("<html>");
    
    // Add time details
    String startTime = event.getStartDateTime().toLocalTime().toString();
    String endTime = event.getEndDateTime().toLocalTime().toString();
    details.append(String.format("<b>Time:</b> %s - %s<br/>", startTime, endTime));
    
    // Add location if present
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      details.append(String.format("<b>Location:</b> %s<br/>", event.getLocation()));
    }
    
    // Add description if present
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      details.append(String.format("<b>Description:</b> %s<br/>", event.getDescription()));
    }
    
    details.append("</html>");
    
    // Create a label for the event details
    JLabel detailsLabel = new JLabel(details.toString());
    panel.add(detailsLabel, BorderLayout.CENTER);
    
    return panel;
  }
  
  /**
   * Clears the event display area.
   */
  private void clearEventDisplay() {
    removeAll();
    setLayout(new BorderLayout());
    JLabel emptyLabel = new JLabel("No events to display");
    emptyLabel.setHorizontalAlignment(JLabel.CENTER);
    add(emptyLabel, BorderLayout.CENTER);
    revalidate();
    repaint();
  }

  public void updateEvent(Event event) {
    // Only collect form data and pass it to the controller
    EventFormData formData = collectFormData();

    // Notify the appropriate listener based on the current mode
    if (listener != null) {
      if (currentMode == PanelMode.EDIT) {
        // Pass the current event ID through the form data
        // The controller will use the current event for updating
        listener.onEventUpdated(formData);
      } else if (currentMode == PanelMode.COPY) {
        listener.onEventCopied(getTargetCalendarName(), getTargetDateTime(), getTargetEndDateTime());
      } else {
        listener.onEventSaved(formData);
      }
    }
  }

  /**
   * Collects all data from the form fields into a data transfer object.
   * This method does not contain any business logic, only UI data collection.
   *
   * @return EventFormData containing all the form field values
   */
  public EventFormData collectFormData() {
    // Get date and time from spinners
    Date selectedDate = (Date) dateSpinner.getValue();
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    String subject = subjectField.getText().trim();
    String location = locationField.getText().trim();
    String description = descriptionArea.getText().trim();
    boolean isRecurring = recurringCheckBox.isSelected();
    boolean isAllDay = allDayCheckBox.isSelected();
    boolean isPrivate = privateEventCheckBox.isSelected();
    boolean autoDecline = autoDeclineCheckBox.isSelected();

    // Collect recurring event data if applicable
    Set<DayOfWeek> weekdays = null;
    int occurrences = 0;
    LocalDate untilDate = null;

    if (isRecurring) {
      weekdays = weekdayCheckboxes.stream()
              .filter(JCheckBox::isSelected)
              .map(cb -> DayOfWeek.of((weekdayCheckboxes.indexOf(cb) + 1)))
              .collect(Collectors.toSet());
      occurrences = (Integer) occurrencesSpinner.getValue();
      untilDate = ((Date) untilDateSpinner.getValue()).toInstant()
              .atZone(ZoneOffset.UTC)
              .toLocalDate();
    }

    return new EventFormData(
            subject,
            selectedDate,
            startTime,
            endTime,
            location,
            description,
            isRecurring,
            isAllDay,
            weekdays,
            occurrences,
            untilDate,
            isPrivate,
            autoDecline
    );
  }

  private void handleRecurringCheckbox() {
    recurringPanel.setVisible(recurringCheckBox.isSelected());
    if (recurringCheckBox.isSelected()) {
      // Add recurring options
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(5, 5, 5, 5);

      gbc.gridx = 0;
      gbc.gridy = 0;
      recurringPanel.add(new JLabel("Repeat every:"), gbc);

      gbc.gridx = 1;
      JSpinner repeatSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
      recurringPanel.add(repeatSpinner, gbc);

      gbc.gridx = 2;
      String[] units = {"Day", "Week", "Month", "Year"};
      JComboBox<String> unitCombo = new JComboBox<>(units);
      recurringPanel.add(unitCombo, gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      recurringPanel.add(new JLabel("Until:"), gbc);

      gbc.gridx = 1;
      gbc.gridwidth = 2;
      JSpinner untilSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor untilEditor = new JSpinner.DateEditor(untilSpinner, "yyyy-MM-dd");
      untilSpinner.setEditor(untilEditor);
      recurringPanel.add(untilSpinner, gbc);
    }
    revalidate();
    repaint();
  }

  /**
   * Refreshes the event panel view.
   */
  public void refreshView() {
    System.out.println("[DEBUG] Refreshing event panel view");
    revalidate();
    repaint();
  }

  /**
   * Handles the selection of an event from the event list.
   * Updates the form with the selected event's data.
   */
  private void handleEventSelection() {
    if (currentEvent != null) {
      System.out.println("[DEBUG] Handling event selection: " + currentEvent.getSubject());
      // Populate form fields with event data
      subjectField.setText(currentEvent.getSubject());
      locationField.setText(currentEvent.getLocation());
      descriptionArea.setText(currentEvent.getDescription());

      // Set date and time values
      LocalDateTime startDateTime = currentEvent.getStartDateTime();
      LocalDateTime endDateTime = currentEvent.getEndDateTime();
      System.out.println("[DEBUG] Event times: Start=" + startDateTime + ", End=" + endDateTime);

      // Update date spinner
      Calendar cal = Calendar.getInstance();
      cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
      dateSpinner.setValue(cal.getTime());

      // Update time spinners
      cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
      cal.set(Calendar.MINUTE, startDateTime.getMinute());
      startTimeSpinner.setValue(cal.getTime());

      cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
      cal.set(Calendar.MINUTE, endDateTime.getMinute());
      endTimeSpinner.setValue(cal.getTime());

      // Set recurring checkbox
      boolean isRecurring = currentEvent instanceof RecurringEvent;
      recurringCheckBox.setSelected(isRecurring);
      handleRecurringCheckbox(); // Update recurring panel visibility

      // Set panel mode to VIEW
      setPanelMode(PanelMode.VIEW);
    } else {
      // Clear form if no event is selected
      clearForm();
      setPanelMode(PanelMode.CREATE);
    }
  }

  /**
   * Saves the current event using the form data.
   * Collects data from form fields and notifies the listener.
   */
  private void saveEvent() {
    try {
      System.out.println("[DEBUG] Save event button clicked, current mode: " + currentMode);
      if (!validateForm()) {
        System.out.println("[DEBUG] Form validation failed");
        return;
      }
      System.out.println("[DEBUG] Form validation successful");

      // Collect data from form fields
      EventFormData formData = collectFormData();
      System.out.println("[DEBUG] Form data collected: Subject=" + formData.getSubject() + 
                       ", Location=" + formData.getLocation() + 
                       ", Description length=" + formData.getDescription().length() + 
                       ", Recurring=" + formData.isRecurring() + 
                       ", AllDay=" + formData.isAllDay());

      // Notify listener based on current panel mode
      if (currentMode == PanelMode.EDIT && currentEvent != null) {
        // We're editing an existing event
        System.out.println("[DEBUG] Editing existing event: " + currentEvent.getSubject());
        if (listener != null) {
          // Pass the current event ID through the form data
          // The controller will use the current event for updating
          listener.onEventUpdated(formData);
        }
      } else if (currentMode == PanelMode.COPY && currentEvent != null) {
        // We're copying an event
        System.out.println("[DEBUG] Copying event: " + currentEvent.getSubject());
        if (listener != null) {
          String targetCalendarName = getTargetCalendarName();
          LocalDateTime targetStartDateTime = getTargetDateTime();
          LocalDateTime targetEndDateTime = getTargetEndDateTime();
          System.out.println("[DEBUG] Copy details: Target calendar=" + targetCalendarName + 
                           ", Start=" + targetStartDateTime + 
                           ", End=" + targetEndDateTime);
          listener.onEventCopied(targetCalendarName, targetStartDateTime, targetEndDateTime);
          
          // After copying, reset the form and update the view
          clearForm();
          setPanelMode(PanelMode.CREATE);
        }
      } else {
        // We're creating a new event
        System.out.println("[DEBUG] Creating new event");
        if (listener != null) {
          listener.onEventSaved(formData);
        }
      }

      // Clear the form after saving
      clearForm();
      currentEvent = null;
      setPanelMode(PanelMode.CREATE);

    } catch (Exception e) {
      System.out.println("[ERROR] Failed to save event: " + e.getMessage());
      e.printStackTrace();
      // Display error to user
      JOptionPane.showMessageDialog(this,
              "Error saving event: " + e.getMessage(),
              "Save Error",
              JOptionPane.ERROR_MESSAGE);
    }
  }

  private void setupLayout() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Event Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(HEADER_LIGHT_COLOR);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    headerPanel.add(modeLabel, BorderLayout.WEST);
    add(headerPanel, BorderLayout.NORTH);

    // Create a panel for the form
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Add components to the form panel
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Subject:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;
    formPanel.add(subjectField, gbc);
    gbc.gridy = 1;
    formPanel.add(errorLabels.get(subjectField), gbc);

    // Date picker
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    formPanel.add(new JLabel("Date:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;
    formPanel.add(dateSpinner, gbc);

    // Time pickers - using a dedicated panel for better layout
    JPanel timePanel = new JPanel(new GridLayout(2, 1, 0, 5));
    timePanel.setOpaque(false);

    // Start time row
    JPanel startTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    startTimeRow.setOpaque(false);
    JLabel startTimeLabel = new JLabel("Start Time:");
    startTimeLabel.setPreferredSize(new Dimension(80, startTimeLabel.getPreferredSize().height));
    startTimeRow.add(startTimeLabel);

    // Set fixed width for spinner
    startTimeSpinner.setPreferredSize(new Dimension(100, startTimeSpinner.getPreferredSize().height));
    startTimeRow.add(startTimeSpinner);

    // End time row
    JPanel endTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    endTimeRow.setOpaque(false);
    JLabel endTimeLabel = new JLabel("End Time:");
    endTimeLabel.setPreferredSize(new Dimension(80, endTimeLabel.getPreferredSize().height));
    endTimeRow.add(endTimeLabel);

    // Set fixed width for spinner
    endTimeSpinner.setPreferredSize(new Dimension(100, endTimeSpinner.getPreferredSize().height));
    endTimeRow.add(endTimeSpinner);

    // Add to time panel
    timePanel.add(startTimeRow);
    timePanel.add(endTimeRow);

    // Add time panel to form
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 2;
    formPanel.add(timePanel, gbc);

    // Reset gridwidth for subsequent components
    gbc.gridwidth = 1;

    // Time error label
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    formPanel.add(errorLabels.get(endTimeSpinner), gbc);

    // Location field
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 2;
    formPanel.add(locationField, gbc);

    // Description area
    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 2;
    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setPreferredSize(new Dimension(0, 60));
    formPanel.add(descScrollPane, gbc);

    // Checkboxes - two per row for space efficiency
    gbc.gridx = 0;
    gbc.gridy = 9;
    gbc.gridwidth = 1;
    formPanel.add(styleCheckbox(allDayCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(recurringCheckBox), gbc);

    gbc.gridx = 0;
    gbc.gridy = 10;
    formPanel.add(styleCheckbox(privateEventCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(autoDeclineCheckBox), gbc);

    // Recurring options panel
    gbc.gridx = 0;
    gbc.gridy = 11;
    gbc.gridwidth = 2;
    formPanel.add(recurringOptionsPanel, gbc);
    recurringOptionsPanel.setVisible(false);

    // Recurring error labels
    gbc.gridx = 0;
    gbc.gridy = 12;
    formPanel.add(errorLabels.get(weekdayCheckboxes.get(0)), gbc);
    gbc.gridy = 13;
    formPanel.add(errorLabels.get(untilDateSpinner), gbc);

    // Setup copy options panel
    GridBagConstraints copyGbc = new GridBagConstraints();
    copyGbc.fill = GridBagConstraints.HORIZONTAL;
    copyGbc.insets = new Insets(5, 5, 5, 5);

    copyGbc.gridx = 0;
    copyGbc.gridy = 0;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Target Calendar:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.gridwidth = 2;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetCalendarCombo, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 1;
    copyGbc.gridwidth = 1;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Target Date:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.gridwidth = 2;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetDateSpinner, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 2;
    copyGbc.gridwidth = 1;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("Start Time:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetStartTimeSpinner, copyGbc);

    copyGbc.gridx = 0;
    copyGbc.gridy = 3;
    copyGbc.anchor = GridBagConstraints.EAST;
    copyOptionsPanel.add(new JLabel("End Time:"), copyGbc);

    copyGbc.gridx = 1;
    copyGbc.anchor = GridBagConstraints.WEST;
    copyOptionsPanel.add(targetEndTimeSpinner, copyGbc);

    // Add copy options panel to the form panel
    gbc.gridx = 0;
    gbc.gridy = 14;
    gbc.gridwidth = 3;
    formPanel.add(copyOptionsPanel, gbc);

    // Create a dedicated button panel with fixed layout to ensure all buttons are visible
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 4, 10, 5));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add all buttons to the panel
    buttonPanel.add(saveButton);
    buttonPanel.add(editButton);
    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);

    // Set preferred size for buttons to prevent squishing
    Dimension buttonSize = new Dimension(100, 30);
    editButton.setPreferredSize(buttonSize);
    copyButton.setPreferredSize(buttonSize);
    saveButton.setPreferredSize(buttonSize);
    cancelButton.setPreferredSize(buttonSize);

    // Ensure all buttons are visible
    editButton.setVisible(true);
    copyButton.setVisible(true);
    saveButton.setVisible(true);
    cancelButton.setVisible(true);

    System.out.println("[DEBUG] setupLayout - Edit button visibility: " + editButton.isVisible());
    System.out.println("[DEBUG] setupLayout - Copy button visibility: " + copyButton.isVisible());
    System.out.println("[DEBUG] setupLayout - Save button visibility: " + saveButton.isVisible());
    System.out.println("[DEBUG] setupLayout - Cancel button visibility: " + cancelButton.isVisible());

    // Set a fixed width for the form panel to prevent horizontal scrolling
    int fixedWidth = 400; // Fixed width for the form panel
    formPanel.setPreferredSize(new Dimension(fixedWidth, formPanel.getPreferredSize().height));
    formPanel.setMinimumSize(new Dimension(fixedWidth, 100));
    formPanel.setMaximumSize(new Dimension(fixedWidth, Short.MAX_VALUE));

    // Configure scroll pane to completely disable horizontal scrolling
    JScrollPane scrollPane = new JScrollPane(formPanel);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.getViewport().setMinimumSize(new Dimension(fixedWidth, 100));
    scrollPane.getViewport().setPreferredSize(new Dimension(fixedWidth, formPanel.getPreferredSize().height));

    // Disable horizontal scrolling at the viewport level
    scrollPane.getViewport().setView(formPanel);

    add(scrollPane, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private JCheckBox styleCheckbox(JCheckBox checkbox) {
    checkbox.setFont(new Font("Arial", Font.PLAIN, 11));
    checkbox.setForeground(TEXT_COLOR);
    checkbox.setOpaque(false);
    return checkbox;
  }
}
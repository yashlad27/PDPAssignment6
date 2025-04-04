package view;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
  private static final Color BORDER_COLOR = new Color(0xcccccc);
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
   */
  public interface EventPanelListener {
    void onEventSaved(String[] eventData, boolean isRecurring);

    void onEventCancelled();

    void onEventUpdated(String[] args, boolean isRecurring);
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
    recurringOptionsPanel = new JPanel(new GridLayout(0, 1));
    recurringPanel = new JPanel(new BorderLayout());
    errorLabels = new HashMap<>();
    timezoneHandler = new TimeZoneHandler();

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

  private void setupListeners() {
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
        weekdayCheckboxes.forEach(cb ->
                cb.addActionListener(we -> System.out.println("[DEBUG] Weekday changed - " + cb.getText() + ": " + cb.isSelected()))
        );
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
          String[] eventArgs;
          boolean isRecurring = recurringCheckBox.isSelected();
          System.out.println("[DEBUG] Creating " + (isRecurring ? "recurring" : "single") + " event");

          // Get date and time from spinners
          Date selectedDate = (Date) dateSpinner.getValue();
          Date startTime = (Date) startTimeSpinner.getValue();
          Date endTime = (Date) endTimeSpinner.getValue();
          System.out.println("[DEBUG] Selected date: " + selectedDate);
          System.out.println("[DEBUG] Start time: " + startTime);
          System.out.println("[DEBUG] End time: " + endTime);

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

          System.out.println("[DEBUG] Start DateTime: " + startDateTime);
          System.out.println("[DEBUG] End DateTime: " + endDateTime);

          String subject = subjectField.getText().trim();
          String location = locationField.getText().trim();
          String description = descriptionArea.getText().trim();
          System.out.println("[DEBUG] Event details - Subject: " + subject + ", Location: " + location);

          if (isRecurring) {
            Set<DayOfWeek> weekdays = weekdayCheckboxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> DayOfWeek.of((weekdayCheckboxes.indexOf(cb) + 1) % 7 + 1))
                    .collect(java.util.stream.Collectors.toSet());
            int occurrences = (Integer) occurrencesSpinner.getValue();
            LocalDate untilDate = ((Date) untilDateSpinner.getValue()).toInstant()
                    .atZone(ZoneId.of(systemTimezone))
                    .toLocalDate();
            System.out.println("[DEBUG] Recurring details - Weekdays: " + weekdays + ", Occurrences: " + occurrences + ", Until: " + untilDate);

            eventArgs = new String[]{
                    "series_from_date",
                    "create",
                    subject,
                    startDateTime.toString(),
                    String.format("%s,%s,%s,%s,%d,%s,%s",
                            subject,
                            startDateTime,
                            endDateTime,
                            location,
                            occurrences,
                            weekdays,
                            untilDate)
            };
          } else {
            eventArgs = new String[]{
                    "single",
                    "create",
                    subject,
                    startDateTime.toString(),
                    String.format("%s,%s,%s,%s,%s",
                            subject,
                            startDateTime,
                            endDateTime,
                            location,
                            description)
            };
          }
          System.out.println("[DEBUG] Calling onEventSaved with args: " + String.join(", ", eventArgs));
          listener.onEventSaved(eventArgs, isRecurring);
          JOptionPane.showMessageDialog(this,
                  String.format("Event '%s' created successfully at %s",
                          subject,
                          location.isEmpty() ? "no location" : location),
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
      subjectField.setText("");
      locationField.setText("");
      descriptionArea.setText("");
      dateSpinner.setValue(new java.util.Date());
      startTimeSpinner.setValue(new java.util.Date());
      endTimeSpinner.setValue(new java.util.Date());
      allDayCheckBox.setSelected(false);
      recurringCheckBox.setSelected(false);
      privateEventCheckBox.setSelected(false);
      autoDeclineCheckBox.setSelected(false);
      recurringOptionsPanel.setVisible(false);
      System.out.println("[DEBUG] Form cleared");
    });
  }

  private boolean validateForm() {
    boolean isValid = true;
    clearErrorLabels();

    // Validate subject
    if (subjectField.getText().trim().isEmpty()) {
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
      showError(endTimeSpinner, "End time must be after start time");
      isValid = false;
    }

    // Validate recurring event options
    if (recurringCheckBox.isSelected()) {
      boolean anyWeekdaySelected = weekdayCheckboxes.stream().anyMatch(JCheckBox::isSelected);
      if (!anyWeekdaySelected) {
        showError(weekdayCheckboxes.get(0), "Select at least one weekday");
        isValid = false;
      }

      Date untilDate = (Date) untilDateSpinner.getValue();
      if (untilDate.before(selectedDate)) {
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
   * Displays an event in the form.
   *
   * @param event the event to display
   */
  public void displayEvent(Event event) {
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
    subjectField.setText("");
    descriptionArea.setText("");
    locationField.setText("");
    recurringCheckBox.setSelected(false);
    recurringOptionsPanel.setVisible(false);
    weekdayCheckboxes.forEach(cb -> cb.setSelected(false));
    occurrencesSpinner.setValue(1);
    allDayCheckBox.setSelected(false);
    startTimeSpinner.setEnabled(true);
    endTimeSpinner.setEnabled(true);
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
  public void updateEvent(Event event) {
    // Get date and time from spinners
    Date selectedDate = (Date) dateSpinner.getValue();
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    // Combine date and time
    LocalDateTime startDateTime = selectedDate.toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .withHour(startTime.toInstant().atZone(ZoneOffset.UTC).getHour())
            .withMinute(startTime.toInstant().atZone(ZoneOffset.UTC).getMinute());

    LocalDateTime endDateTime = selectedDate.toInstant()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .withHour(endTime.toInstant().atZone(ZoneOffset.UTC).getHour())
            .withMinute(endTime.toInstant().atZone(ZoneOffset.UTC).getMinute());

    String subject = subjectField.getText().trim();
    String location = locationField.getText().trim();
    String description = descriptionArea.getText().trim();
    boolean isRecurring = recurringCheckBox.isSelected();

    String[] args;
    if (isRecurring) {
      Set<DayOfWeek> weekdays = weekdayCheckboxes.stream()
              .filter(JCheckBox::isSelected)
              .map(cb -> DayOfWeek.of((weekdayCheckboxes.indexOf(cb) + 1)))
              .collect(Collectors.toSet());
      int occurrences = (Integer) occurrencesSpinner.getValue();
      LocalDate untilDate = ((Date) untilDateSpinner.getValue()).toInstant()
              .atZone(ZoneOffset.UTC)
              .toLocalDate();

      args = new String[]{"series_from_date", "edit",
              subject,
              startDateTime.toString(),
              String.format("%s,%s,%s,%s,%d,%s,%s",
                      subject,
                      startDateTime,
                      endDateTime,
                      location,
                      occurrences,
                      weekdays,
                      untilDate)
      };
    } else {
      args = new String[]{"single", "edit",
              subject,
              startDateTime.toString(),
              String.format("%s,%s,%s,%s",
                      subject,
                      startDateTime,
                      endDateTime,
                      location)
      };
    }

    if (listener != null) {
      listener.onEventUpdated(args, isRecurring);
    }
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

  private void handleEventSelection() {
    // Implementation of handleEventSelection method
  }

  private void saveEvent() {
    // Implementation of saveEvent method
  }

  private void setupLayout() {
    setLayout(new BorderLayout(10, 10));
    setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Event Details"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    // Create a main panel with a vertical layout
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

    // Event details form
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(new Color(0xf8f8f8));
    formPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 3, 3, 3); // Reduced insets for more compact layout
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 1.0;

    // Subject field
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Subject:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    formPanel.add(subjectField, gbc);

    // Subject error label
    gbc.gridx = 0;
    gbc.gridy = 2;
    formPanel.add(errorLabels.get(subjectField), gbc);

    // Date picker
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Date:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
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
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    formPanel.add(timePanel, gbc);

    // Reset gridwidth for subsequent components
    gbc.gridwidth = 1;

    // Time error label
    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 2;
    formPanel.add(errorLabels.get(endTimeSpinner), gbc);

    // Location field
    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 1;
    formPanel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 0;
    gbc.gridy = 9;
    gbc.gridwidth = 2;
    formPanel.add(locationField, gbc);

    // Description area
    gbc.gridx = 0;
    gbc.gridy = 10;
    gbc.gridwidth = 2;
    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setPreferredSize(new Dimension(0, 60));
    formPanel.add(descScrollPane, gbc);

    // Checkboxes - two per row for space efficiency
    gbc.gridx = 0;
    gbc.gridy = 11;
    gbc.gridwidth = 1;
    formPanel.add(styleCheckbox(allDayCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(recurringCheckBox), gbc);

    gbc.gridx = 0;
    gbc.gridy = 12;
    formPanel.add(styleCheckbox(privateEventCheckBox), gbc);

    gbc.gridx = 1;
    formPanel.add(styleCheckbox(autoDeclineCheckBox), gbc);

    // Recurring options panel
    gbc.gridx = 0;
    gbc.gridy = 13;
    gbc.gridwidth = 2;
    formPanel.add(recurringOptionsPanel, gbc);
    recurringOptionsPanel.setVisible(false);

    // Recurring error labels
    gbc.gridx = 0;
    gbc.gridy = 14;
    formPanel.add(errorLabels.get(weekdayCheckboxes.get(0)), gbc);
    gbc.gridy = 15;
    formPanel.add(errorLabels.get(untilDateSpinner), gbc);

    // Buttons panel
    gbc.gridx = 0;
    gbc.gridy = 16;
    gbc.gridwidth = 2;
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    buttonPanel.setOpaque(false);

    // Style buttons using ButtonStyler for consistent styling
    ButtonStyler.applyPrimaryStyle(saveButton);
    ButtonStyler.applySecondaryStyle(cancelButton);

    // Set consistent button size
    Dimension buttonSize = new Dimension(100, 32);
    saveButton.setPreferredSize(buttonSize);
    cancelButton.setPreferredSize(buttonSize);

    // Add buttons to panel with proper spacing
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(saveButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalGlue());
    formPanel.add(buttonPanel, gbc);

    // Add panels to main panel
    mainPanel.add(formPanel);

    // Add main panel to this panel
    add(new JScrollPane(mainPanel), BorderLayout.CENTER);
  }

  private JCheckBox styleCheckbox(JCheckBox checkbox) {
    checkbox.setFont(new Font("Arial", Font.PLAIN, 11));
    checkbox.setForeground(TEXT_COLOR);
    checkbox.setOpaque(false);
    return checkbox;
  }
} 
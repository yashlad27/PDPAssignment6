package view;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.calendar.ICalendar;
import model.event.Event;

/**
 * Enhanced dialog for copying events between calendars with timezone handling.
 */
public class EnhancedEventCopyDialog extends JDialog {
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color HEADER_COLOR = new Color(0x4a86e8);

  private final Event event;
  private final List<ICalendar> calendars;
  private boolean copyConfirmed = false;

  // Form components
  private JComboBox<String> calendarComboBox;
  private JSpinner dateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;

  // Copy data
  private String targetCalendarName;
  private LocalDateTime targetStartDateTime;
  private LocalDateTime targetEndDateTime;

  /**
   * Constructs a new EnhancedEventCopyDialog.
   *
   * @param parent    the parent frame
   * @param event     the event to copy
   * @param calendars the list of available calendars
   */
  public EnhancedEventCopyDialog(Frame parent, Event event, List<ICalendar> calendars) {
    super(parent, "Copy Event", true);
    this.event = event;
    this.calendars = calendars;

    initComponents();
    layoutComponents();

    pack();
    setLocationRelativeTo(parent);
    setResizable(true);
    setMinimumSize(new Dimension(500, 400));
  }

  /**
   * Initializes the dialog components.
   */
  private void initComponents() {
    // Calendar selector
    calendarComboBox = new JComboBox<>();
    for (ICalendar calendar : calendars) {
      String calendarName = ((model.calendar.Calendar) calendar).getName();
      calendarComboBox.addItem(calendarName);
    }

    // Date and time spinners
    dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);

    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);

    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endTimeEditor);

    // Initialize with event data
    Calendar cal = Calendar.getInstance();

    // Set date
    LocalDateTime startDateTime = event.getStartDateTime();
    cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
    dateSpinner.setValue(cal.getTime());

    // Set start time
    cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
    cal.set(Calendar.MINUTE, startDateTime.getMinute());
    startTimeSpinner.setValue(cal.getTime());

    // Set end time
    LocalDateTime endDateTime = event.getEndDateTime();
    cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
    cal.set(Calendar.MINUTE, endDateTime.getMinute());
    endTimeSpinner.setValue(cal.getTime());

    // Buttons
    JButton copyButton = new JButton("Copy");
    copyButton.setBackground(HEADER_COLOR);
    copyButton.setForeground(Color.WHITE);
    copyButton.addActionListener(e -> {
      if (validateForm()) {
        gatherCopyData();
        copyConfirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
  }

  /**
   * Lays out the dialog components.
   */
  private void layoutComponents() {
    // Main panel with padding
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Title
    JLabel titleLabel = new JLabel("Copy Event: " + event.getSubject());
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(THEME_COLOR);
    gbc.gridwidth = 2;
    mainPanel.add(titleLabel, gbc);

    // Add separator
    gbc.gridy++;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(new JSeparator(), gbc);

    // Source event info
    gbc.gridy++;
    JPanel sourcePanel = new JPanel(new GridLayout(3, 1));
    sourcePanel.setBorder(BorderFactory.createTitledBorder("Source Event"));

    JLabel subjectLabel = new JLabel("Subject: " + event.getSubject());
    JLabel startLabel = new JLabel("Start: " + event.getStartDateTime().toString());
    JLabel endLabel = new JLabel("End: " + event.getEndDateTime().toString());

    sourcePanel.add(subjectLabel);
    sourcePanel.add(startLabel);
    sourcePanel.add(endLabel);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(sourcePanel, gbc);

    // Target calendar
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Target Calendar:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(calendarComboBox, gbc);

    // Target date
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Target Date:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(dateSpinner, gbc);

    // Target start time
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("Start Time:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(startTimeSpinner, gbc);

    // Target end time
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    mainPanel.add(createLabel("End Time:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(endTimeSpinner, gbc);

    // Info label about timezone conversion
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 5, 10, 5);
    JLabel infoLabel = new JLabel("<html>Times will be automatically adjusted for the target calendar's timezone.<br>The event will maintain the same time relative to the timezone.</html>");
    infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));
    infoLabel.setForeground(Color.DARK_GRAY);
    mainPanel.add(infoLabel, gbc);

    // Buttons
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton copyButton = new JButton("Copy");
    copyButton.setBackground(HEADER_COLOR);
    copyButton.setForeground(Color.WHITE);
    copyButton.addActionListener(e -> {
      if (validateForm()) {
        gatherCopyData();
        copyConfirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);

    // Add conflict info panel
    JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(10, 15, 0, 15));
    JTextArea infoText = new JTextArea(
            "Note: Events cannot conflict with each other. A conflict with any instance " +
                    "of a recurring event is treated as a conflict with the recurring event itself, and is prohibited.");
    infoText.setEditable(false);
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    infoText.setBackground(new Color(0xFFFAE6));
    infoText.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFFCC80), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    infoPanel.add(infoText, BorderLayout.CENTER);

    // Main layout
    setLayout(new BorderLayout());
    add(infoPanel, BorderLayout.NORTH);
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Creates a styled label.
   *
   * @param text the label text
   * @return the styled label
   */
  private JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Arial", Font.PLAIN, 12));
    return label;
  }

  /**
   * Validates the form input.
   *
   * @return true if the form is valid, false otherwise
   */
  private boolean validateForm() {
    if (calendarComboBox.getSelectedItem() == null) {
      JOptionPane.showMessageDialog(this,
              "Please select a target calendar",
              "Validation Error",
              JOptionPane.ERROR_MESSAGE);
      calendarComboBox.requestFocus();
      return false;
    }

    // Validate times
    Date startDate = (Date) startTimeSpinner.getValue();
    Date endDate = (Date) endTimeSpinner.getValue();

    Calendar startCal = Calendar.getInstance();
    startCal.setTime(startDate);

    Calendar endCal = Calendar.getInstance();
    endCal.setTime(endDate);

    if (endCal.before(startCal)) {
      JOptionPane.showMessageDialog(this,
              "End time must be after start time",
              "Validation Error",
              JOptionPane.ERROR_MESSAGE);
      endTimeSpinner.requestFocus();
      return false;
    }

    return true;
  }

  /**
   * Gathers the copy data from the form fields.
   */
  private void gatherCopyData() {
    targetCalendarName = (String) calendarComboBox.getSelectedItem();

    // Convert date and time to LocalDateTime
    Date dateValue = (Date) dateSpinner.getValue();
    Date startTimeValue = (Date) startTimeSpinner.getValue();
    Date endTimeValue = (Date) endTimeSpinner.getValue();

    Calendar dateCal = Calendar.getInstance();
    dateCal.setTime(dateValue);

    Calendar startTimeCal = Calendar.getInstance();
    startTimeCal.setTime(startTimeValue);

    Calendar endTimeCal = Calendar.getInstance();
    endTimeCal.setTime(endTimeValue);

    LocalDate date = LocalDate.of(
            dateCal.get(Calendar.YEAR),
            dateCal.get(Calendar.MONTH) + 1,
            dateCal.get(Calendar.DAY_OF_MONTH)
    );

    LocalTime startTime = LocalTime.of(
            startTimeCal.get(Calendar.HOUR_OF_DAY),
            startTimeCal.get(Calendar.MINUTE)
    );

    LocalTime endTime = LocalTime.of(
            endTimeCal.get(Calendar.HOUR_OF_DAY),
            endTimeCal.get(Calendar.MINUTE)
    );

    targetStartDateTime = LocalDateTime.of(date, startTime);
    targetEndDateTime = LocalDateTime.of(date, endTime);
  }

  /**
   * Shows the dialog and returns whether the copy was confirmed.
   *
   * @return true if the copy was confirmed, false otherwise
   */
  public boolean showDialog() {
    setVisible(true);
    return copyConfirmed;
  }

  /**
   * Gets the name of the target calendar.
   *
   * @return the name of the target calendar
   */
  public String getTargetCalendarName() {
    return targetCalendarName;
  }

  /**
   * Gets the target start date and time.
   *
   * @return the target start date and time
   */
  public LocalDateTime getTargetStartDateTime() {
    return targetStartDateTime;
  }

  /**
   * Gets the target end date and time.
   *
   * @return the target end date and time
   */
  public LocalDateTime getTargetEndDateTime() {
    return targetEndDateTime;
  }
}

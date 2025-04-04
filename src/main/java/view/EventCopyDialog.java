package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import model.calendar.ICalendar;
import model.event.Event;

/**
 * Dialog for copying events between calendars.
 */
public class EventCopyDialog extends JDialog {
  private JComboBox<String> calendarComboBox;
  private JSpinner dateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JButton copyButton;
  private JButton cancelButton;
  private final Event event;
  private final List<ICalendar> calendars;
  private boolean copyConfirmed = false;
  private String targetCalendarName;
  private LocalDateTime targetDateTime;
  private LocalDateTime targetEndDateTime;
  
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  // Date time formatter for displaying dates and times

  /**
   * Constructs a new EventCopyDialog.
   *
   * @param parent    the parent frame or component
   * @param event     the event to copy
   * @param calendars the list of available calendars
   */
  public EventCopyDialog(Object parent, Event event, List<ICalendar> calendars) {
    super(parent instanceof JFrame ? (JFrame) parent : new JFrame(), "Copy Event", true);
    this.event = event;
    this.calendars = calendars;
    
    initComponents();
    layoutComponents();
    
    setSize(400, 200);
    setLocationRelativeTo(parent instanceof JFrame ? (JFrame) parent : null);
  }

  /**
   * Initializes the dialog components.
   */
  private void initComponents() {
    // Calendar combo box
    calendarComboBox = new JComboBox<>();
    for (ICalendar calendar : calendars) {
      String calendarName = ((model.calendar.Calendar) calendar).getName();
      calendarComboBox.addItem(calendarName);
    }
    
    // Date and time spinners
    dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);
    dateSpinner.setValue(new Date()); // Set to current date
    
    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);
    startTimeSpinner.setValue(new Date()); // Set to current time
    
    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endTimeEditor);
    // Set end time to 1 hour after start time
    Date endTime = new Date();
    endTime.setTime(endTime.getTime() + 3600000); // Add 1 hour in milliseconds
    endTimeSpinner.setValue(endTime);
    
    // Style buttons
    copyButton = new JButton("Copy");
    copyButton.setBackground(THEME_COLOR);
    copyButton.setForeground(Color.WHITE);
    copyButton.setFocusPainted(false);
    
    cancelButton = new JButton("Cancel");
    
    // Add action listeners
    copyButton.addActionListener(e -> {
      if (calendarComboBox.getSelectedItem() != null) {
        targetCalendarName = (String) calendarComboBox.getSelectedItem();
        
        // Get the selected date and times
        Date selectedDate = (Date) dateSpinner.getValue();
        Date selectedStartTime = (Date) startTimeSpinner.getValue();
        Date selectedEndTime = (Date) endTimeSpinner.getValue(); // Store for duration calculation
        
        // Convert to LocalDateTime
        LocalDate localDate = new java.sql.Date(selectedDate.getTime()).toLocalDate();
        LocalTime localStartTime = new java.sql.Time(selectedStartTime.getTime()).toLocalTime();
        LocalTime localEndTime = new java.sql.Time(selectedEndTime.getTime()).toLocalTime();
        
        // Create the target date and time from the selected date and times
        targetDateTime = LocalDateTime.of(localDate, localStartTime);
        targetEndDateTime = LocalDateTime.of(localDate, localEndTime);
        
        copyConfirmed = true;
        dispose();
      } else {
        JOptionPane.showMessageDialog(this, "Please select a target calendar", "Error", JOptionPane.ERROR_MESSAGE);
      }
    });
    
    cancelButton.addActionListener(e -> dispose());
  }

  /**
   * Lays out the dialog components.
   */
  private void layoutComponents() {
    // Create main panel with a nice border
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(10, 10, 10, 10),
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcccccc)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        )
    ));
    
    // Set up grid bag constraints
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    
    // Add a title label with the event subject
    JLabel titleLabel = new JLabel("Copy Event: " + event.getSubject());
    titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 14f));
    titleLabel.setForeground(THEME_COLOR);
    gbc.gridwidth = 2;
    mainPanel.add(titleLabel, gbc);
    
    // Add a separator
    gbc.gridy++;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 5, 10, 5);
    JSeparator separator = new JSeparator();
    mainPanel.add(separator, gbc);
    
    // Reset insets and grid width
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.gridwidth = 1;
    
    // Source event details
    gbc.gridy++;
    mainPanel.add(new JLabel("Source:"), gbc);
    
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    JLabel sourceLabel = new JLabel(event.getStartDateTime().toLocalDate() + " " + 
                             event.getStartDateTime().toLocalTime() + " - " + 
                             event.getEndDateTime().toLocalTime());
    mainPanel.add(sourceLabel, gbc);
    
    // Target calendar selection
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Target Calendar:"), gbc);
    
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(calendarComboBox, gbc);
    
    // Target date selection
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Date:"), gbc);
    
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(dateSpinner, gbc);
    
    // Target time selection
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("Start Time:"), gbc);
    
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(startTimeSpinner, gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    mainPanel.add(new JLabel("End Time:"), gbc);
    
    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    mainPanel.add(endTimeSpinner, gbc);
    
    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);
    
    // Main layout
    setLayout(new BorderLayout());
    add(mainPanel, BorderLayout.CENTER);
    add(buttonPanel, BorderLayout.SOUTH);
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
   * Gets the target date and time for the copied event.
   *
   * @return the target date and time
   */
  public LocalDateTime getTargetDateTime() {
    return targetDateTime;
  }
  
  /**
   * Gets the target end date and time for the copied event.
   *
   * @return the target end date and time
   */
  public LocalDateTime getTargetEndDateTime() {
    return targetEndDateTime;
  }
}

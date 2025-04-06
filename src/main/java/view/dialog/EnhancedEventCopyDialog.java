package view.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import model.calendar.ICalendar;
import model.event.Event;

/**
 * Enhanced dialog for copying events between calendars with improved UI and validation.
 * This implementation follows the SOLID principles:
 * - Single Responsibility: Only handles event copying UI
 * - Open/Closed: Extends AbstractEventDialog and implements IEventCopyDialog
 * - Liskov Substitution: Can be used anywhere an IEventCopyDialog is needed
 * - Interface Segregation: Implements only methods relevant to event copying
 * - Dependency Inversion: GUI components depend on dialog interfaces, not implementations
 */
public class EnhancedEventCopyDialog extends AbstractEventDialog implements IEventCopyDialog {
    
    private final List<ICalendar> calendars;
    
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
     * @param parent    the parent component
     * @param event     the event to copy
     * @param calendars the list of available calendars
     */
    public EnhancedEventCopyDialog(Component parent, Event event, List<ICalendar> calendars) {
        super(parent, "Copy Event", event);
        this.calendars = calendars;
    }
    
    @Override
    protected void initComponents() {
        // Calendar combo box
        calendarComboBox = new JComboBox<>();
        for (ICalendar calendar : calendars) {
            calendarComboBox.addItem(calendar.getName());
        }
        
        // Date spinner
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        
        // Time spinners
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startTimeEditor);
        
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endTimeEditor);
    }
    
    @Override
    protected void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        GridBagConstraints gbc = createGBC();
        
        // Add header
        JLabel headerLabel = new JLabel("Copy Event");
        headerLabel.setFont(headerLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        headerLabel.setForeground(HEADER_COLOR);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(headerLabel, gbc);
        
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
        
        mainPanel.add(sourcePanel, gbc);
        
        // Target calendar section
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JSeparator(), gbc);
        
        gbc.gridy++;
        JPanel targetPanel = new JPanel(new GridBagLayout());
        targetPanel.setBorder(BorderFactory.createTitledBorder("Target Calendar"));
        
        GridBagConstraints targetGbc = createGBC();
        
        // Target calendar
        targetPanel.add(new JLabel("Calendar:*"), targetGbc);
        targetGbc.gridx = 1;
        targetGbc.fill = GridBagConstraints.HORIZONTAL;
        targetGbc.weightx = 1.0;
        targetPanel.add(calendarComboBox, targetGbc);
        
        // Target date
        targetGbc.gridx = 0;
        targetGbc.gridy++;
        targetGbc.weightx = 0.0;
        targetPanel.add(new JLabel("Date:*"), targetGbc);
        targetGbc.gridx = 1;
        targetGbc.fill = GridBagConstraints.HORIZONTAL;
        targetGbc.weightx = 1.0;
        targetPanel.add(dateSpinner, targetGbc);
        
        // Start time
        targetGbc.gridx = 0;
        targetGbc.gridy++;
        targetGbc.weightx = 0.0;
        targetPanel.add(new JLabel("Start Time:*"), targetGbc);
        targetGbc.gridx = 1;
        targetGbc.fill = GridBagConstraints.HORIZONTAL;
        targetGbc.weightx = 1.0;
        targetPanel.add(startTimeSpinner, targetGbc);
        
        // End time
        targetGbc.gridx = 0;
        targetGbc.gridy++;
        targetGbc.weightx = 0.0;
        targetPanel.add(new JLabel("End Time:*"), targetGbc);
        targetGbc.gridx = 1;
        targetGbc.fill = GridBagConstraints.HORIZONTAL;
        targetGbc.weightx = 1.0;
        targetPanel.add(endTimeSpinner, targetGbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(targetPanel, gbc);
        
        // Create buttons
        JButton copyButton = new JButton("Copy");
        copyButton.setBackground(HEADER_COLOR);
        copyButton.setForeground(Color.WHITE);
        copyButton.addActionListener(e -> {
            if (validateForm()) {
                gatherCopyData();
                actionConfirmed = true;
                dispose();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel(copyButton, cancelButton);
        
        // Create info panel with conflict warning
        JPanel infoPanel = createInfoPanel(
            "Note: Events cannot conflict with each other. A conflict with any instance " +
            "of a recurring event is treated as a conflict with the recurring event itself, and is prohibited.");
        
        // Main layout
        setLayout(new BorderLayout());
        add(infoPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    @Override
    protected void setupListeners() {
        // No special listeners needed for copy dialog
    }
    
    @Override
    protected void loadEventData() {
        // Set default target calendar to the first in the list
        if (calendarComboBox.getItemCount() > 0) {
            calendarComboBox.setSelectedIndex(0);
        }
        
        // Set default date to the event's date
        LocalDateTime startDateTime = event.getStartDateTime();
        Calendar cal = Calendar.getInstance();
        cal.set(startDateTime.getYear(), startDateTime.getMonthValue() - 1, startDateTime.getDayOfMonth());
        dateSpinner.setValue(cal.getTime());
        
        // Set default times to the event's times
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, startDateTime.getHour());
        cal.set(Calendar.MINUTE, startDateTime.getMinute());
        startTimeSpinner.setValue(cal.getTime());
        
        LocalDateTime endDateTime = event.getEndDateTime();
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, endDateTime.getHour());
        cal.set(Calendar.MINUTE, endDateTime.getMinute());
        endTimeSpinner.setValue(cal.getTime());
    }
    
    /**
     * Validates the form data.
     *
     * @return true if the form data is valid, false otherwise
     */
    private boolean validateForm() {
        // Validate calendar selection
        if (calendarComboBox.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a target calendar", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            calendarComboBox.requestFocus();
            return false;
        }
        
        // Validate start time is before end time
        Date startTime = (Date) startTimeSpinner.getValue();
        Date endTime = (Date) endTimeSpinner.getValue();
        
        if (startTime.after(endTime)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            endTimeSpinner.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Gathers the copy data from the form fields.
     */
    private void gatherCopyData() {
        // Get target calendar name
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
    
    @Override
    public String getTargetCalendarName() {
        return targetCalendarName;
    }
    
    @Override
    public LocalDateTime getTargetStartDateTime() {
        return targetStartDateTime;
    }
    
    @Override
    public LocalDateTime getTargetEndDateTime() {
        return targetEndDateTime;
    }
}

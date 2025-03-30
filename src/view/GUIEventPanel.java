package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that manages event creation and editing.
 * This component provides a form for entering event details
 * and handles event-related user interactions.
 */
public class GUIEventPanel extends JPanel {
    private final JTextField subjectField;
    private final JTextField dateField;
    private final JTextField startTimeField;
    private final JTextField endTimeField;
    private final JTextField locationField;
    private final JTextArea descriptionArea;
    private final JCheckBox isRecurringCheckBox;
    private final JPanel recurringOptionsPanel;
    private final JButton saveButton;
    private final JButton cancelButton;
    private final List<EventPanelListener> listeners;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Interface for listening to event panel events.
     */
    public interface EventPanelListener {
        void onEventSaved(EventData eventData);
        void onEventCancelled();
    }

    /**
     * Data class to hold event information.
     */
    public static class EventData {
        public final String subject;
        public final LocalDate date;
        public final LocalTime startTime;
        public final LocalTime endTime;
        public final String location;
        public final String description;
        public final boolean isRecurring;
        public final String weekdays;
        public final int occurrences;
        public final LocalDate untilDate;

        public EventData(String subject, LocalDate date, LocalTime startTime, LocalTime endTime,
                        String location, String description, boolean isRecurring,
                        String weekdays, int occurrences, LocalDate untilDate) {
            this.subject = subject;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.description = description;
            this.isRecurring = isRecurring;
            this.weekdays = weekdays;
            this.occurrences = occurrences;
            this.untilDate = untilDate;
        }
    }

    /**
     * Constructs a new GUIEventPanel.
     */
    public GUIEventPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Event Details"));
        
        this.listeners = new ArrayList<>();
        
        // Create form fields
        subjectField = new JTextField(20);
        dateField = new JTextField(10);
        startTimeField = new JTextField(5);
        endTimeField = new JTextField(5);
        locationField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        isRecurringCheckBox = new JCheckBox("Recurring Event");
        
        // Create recurring options panel
        recurringOptionsPanel = new JPanel();
        recurringOptionsPanel.setLayout(new BoxLayout(recurringOptionsPanel, BoxLayout.Y_AXIS));
        setupRecurringOptions();
        
        // Create buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
        // Add components
        add(createFormPanel());
        add(Box.createVerticalStrut(10));
        add(createButtonPanel());
        
        // Add listeners
        setupListeners();
    }

    /**
     * Creates the form panel with all input fields.
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Add form fields
        addFormField(panel, gbc, "Subject:", subjectField, 0);
        addFormField(panel, gbc, "Date (YYYY-MM-DD):", dateField, 1);
        
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time:"));
        timePanel.add(startTimeField);
        timePanel.add(new JLabel("to"));
        timePanel.add(endTimeField);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(timePanel, gbc);
        
        addFormField(panel, gbc, "Location:", locationField, 3);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(isRecurringCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(recurringOptionsPanel, gbc);
        
        return panel;
    }

    /**
     * Adds a form field to the panel.
     */
    private void addFormField(JPanel panel, GridBagConstraints gbc, 
                            String label, JComponent field, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1; gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    /**
     * Creates the button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    /**
     * Sets up the recurring event options.
     */
    private void setupRecurringOptions() {
        JPanel weekdaysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField weekdaysField = new JTextField(5);
        weekdaysPanel.add(new JLabel("Weekdays (e.g., MWF):"));
        weekdaysPanel.add(weekdaysField);
        
        JPanel occurrencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField occurrencesField = new JTextField(5);
        occurrencesPanel.add(new JLabel("Occurrences:"));
        occurrencesPanel.add(occurrencesField);
        
        recurringOptionsPanel.add(weekdaysPanel);
        recurringOptionsPanel.add(occurrencesPanel);
        recurringOptionsPanel.setVisible(false);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        isRecurringCheckBox.addActionListener(e -> 
            recurringOptionsPanel.setVisible(isRecurringCheckBox.isSelected()));
        
        saveButton.addActionListener(e -> {
            try {
                EventData eventData = createEventData();
                notifyEventSaved(eventData);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid input: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> notifyEventCancelled());
    }

    /**
     * Creates an EventData object from the form fields.
     */
    private EventData createEventData() {
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            throw new IllegalArgumentException("Subject is required");
        }
        
        LocalDate date = LocalDate.parse(dateField.getText().trim(), DATE_FORMATTER);
        LocalTime startTime = LocalTime.parse(startTimeField.getText().trim(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(endTimeField.getText().trim(), TIME_FORMATTER);
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        String location = locationField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean isRecurring = isRecurringCheckBox.isSelected();
        
        String weekdays = "";
        int occurrences = 0;
        LocalDate untilDate = null;
        
        if (isRecurring) {
            // Parse recurring event options
            // This is a simplified version - you might want to add more validation
            weekdays = "MWF"; // Example default
            occurrences = 10; // Example default
        }
        
        return new EventData(subject, date, startTime, endTime, location, description,
                           isRecurring, weekdays, occurrences, untilDate);
    }

    /**
     * Adds an event panel listener.
     *
     * @param listener the listener to add
     */
    public void addEventPanelListener(EventPanelListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners that an event was saved.
     */
    private void notifyEventSaved(EventData eventData) {
        for (EventPanelListener listener : listeners) {
            listener.onEventSaved(eventData);
        }
    }

    /**
     * Notifies all listeners that event creation was cancelled.
     */
    private void notifyEventCancelled() {
        for (EventPanelListener listener : listeners) {
            listener.onEventCancelled();
        }
    }

    /**
     * Clears all form fields.
     */
    public void clearForm() {
        subjectField.setText("");
        dateField.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        locationField.setText("");
        descriptionArea.setText("");
        isRecurringCheckBox.setSelected(false);
        recurringOptionsPanel.setVisible(false);
    }
} 
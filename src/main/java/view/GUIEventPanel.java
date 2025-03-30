package view;

import model.event.Event;
import model.event.RecurringEvent;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Panel class that handles event creation, editing, and display.
 */
public class GUIEventPanel extends JPanel {
    private final JTextField subjectField;
    private final JTextField locationField;
    private final JTextField descriptionField;
    private final JSpinner dateSpinner;
    private final JSpinner startTimeSpinner;
    private final JSpinner endTimeSpinner;
    private final JCheckBox recurringCheckBox;
    private final JPanel recurringPanel;
    private final JList<DayOfWeek> weekdaysList;
    private final JSpinner occurrencesSpinner;
    private final JSpinner untilDateSpinner;
    private final JButton saveButton;
    private final JButton cancelButton;
    private LocalDate currentDate;
    private EventPanelListener listener;
    private final Map<JComponent, JLabel> errorLabels;

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
        void onEventSaved(String[] args, boolean isRecurring);
        void onEventCancelled();
        void onEventUpdated(String[] args, boolean isRecurring);
        void onEventDeleted(String[] args, boolean isRecurring);
    }

    /**
     * Constructs a new GUIEventPanel.
     */
    public GUIEventPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Event Details"));

        // Initialize components
        subjectField = new JTextField();
        locationField = new JTextField();
        descriptionField = new JTextField();
        dateSpinner = new JSpinner(new SpinnerDateModel());
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        recurringCheckBox = new JCheckBox("Recurring Event");
        weekdaysList = new JList<>(DayOfWeek.values());
        occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        untilDateSpinner = new JSpinner(new SpinnerDateModel());
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        recurringPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        errorLabels = new HashMap<>();

        // Configure spinners
        configureSpinners();

        // Set up layout
        setupLayout();
        setupListeners();
    }

    private void configureSpinners() {
        // Configure date spinner
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // Configure time spinners
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(timeEditor);
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "HH:mm"));

        // Set default values
        LocalDateTime now = LocalDateTime.now();
        dateSpinner.setValue(Date.from(now.toInstant(ZoneOffset.UTC)));
        startTimeSpinner.setValue(Date.from(now.withHour(9).withMinute(0).toInstant(ZoneOffset.UTC)));
        endTimeSpinner.setValue(Date.from(now.withHour(10).withMinute(0).toInstant(ZoneOffset.UTC)));
    }

    /**
     * Sets up the layout of the event panel.
     */
    private void setupLayout() {
        // Main form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Subject field with error label
        JPanel subjectPanel = new JPanel(new BorderLayout());
        subjectPanel.add(new JLabel("Subject:"), BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        JLabel subjectError = new JLabel("❌");
        subjectError.setForeground(Color.RED);
        subjectError.setVisible(false);
        subjectPanel.add(subjectError, BorderLayout.EAST);
        errorLabels.put(subjectField, subjectError);
        formPanel.add(subjectPanel);

        // Location field
        formPanel.add(locationField);

        // Description field
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);

        // Date and time fields with error labels
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.add(new JLabel("Date:"), BorderLayout.WEST);
        datePanel.add(dateSpinner, BorderLayout.CENTER);
        JLabel dateError = new JLabel("❌");
        dateError.setForeground(Color.RED);
        dateError.setVisible(false);
        datePanel.add(dateError, BorderLayout.EAST);
        errorLabels.put(dateSpinner, dateError);
        formPanel.add(datePanel);

        JPanel startTimePanel = new JPanel(new BorderLayout());
        startTimePanel.add(new JLabel("Start Time:"), BorderLayout.WEST);
        startTimePanel.add(startTimeSpinner, BorderLayout.CENTER);
        JLabel startTimeError = new JLabel("❌");
        startTimeError.setForeground(Color.RED);
        startTimeError.setVisible(false);
        startTimePanel.add(startTimeError, BorderLayout.EAST);
        errorLabels.put(startTimeSpinner, startTimeError);
        formPanel.add(startTimePanel);

        JPanel endTimePanel = new JPanel(new BorderLayout());
        endTimePanel.add(new JLabel("End Time:"), BorderLayout.WEST);
        endTimePanel.add(endTimeSpinner, BorderLayout.CENTER);
        JLabel endTimeError = new JLabel("❌");
        endTimeError.setForeground(Color.RED);
        endTimeError.setVisible(false);
        endTimePanel.add(endTimeError, BorderLayout.EAST);
        errorLabels.put(endTimeSpinner, endTimeError);
        formPanel.add(endTimePanel);

        // Recurring event panel
        recurringPanel.setBorder(BorderFactory.createTitledBorder("Recurring Event Options"));
        recurringPanel.setVisible(false);

        // Weekdays selection with error label
        JPanel weekdaysPanel = new JPanel(new BorderLayout());
        weekdaysPanel.add(new JLabel("Repeat on:"), BorderLayout.NORTH);
        weekdaysList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        weekdaysPanel.add(new JScrollPane(weekdaysList), BorderLayout.CENTER);
        JLabel weekdaysError = new JLabel("❌");
        weekdaysError.setForeground(Color.RED);
        weekdaysError.setVisible(false);
        weekdaysPanel.add(weekdaysError, BorderLayout.EAST);
        errorLabels.put(weekdaysList, weekdaysError);

        // Occurrences/Until date selection
        JPanel occurrencesPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        occurrencesPanel.add(new JLabel("Number of occurrences:"));
        occurrencesPanel.add(occurrencesSpinner);
        occurrencesPanel.add(new JLabel("Until date:"));
        occurrencesPanel.add(untilDateSpinner);

        recurringPanel.add(weekdaysPanel);
        recurringPanel.add(occurrencesPanel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add all components to the main panel
        add(formPanel, BorderLayout.NORTH);
        add(recurringPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        // Recurring checkbox listener
        recurringCheckBox.addActionListener(e -> {
            recurringPanel.setVisible(recurringCheckBox.isSelected());
            validateForm();
        });

        // Time validation
        startTimeSpinner.addChangeListener(e -> validateForm());
        endTimeSpinner.addChangeListener(e -> validateForm());

        // Weekdays selection listener
        weekdaysList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                validateForm();
            }
        });

        // Occurrences/Until date validation
        occurrencesSpinner.addChangeListener(e -> validateForm());
        untilDateSpinner.addChangeListener(e -> validateForm());

        // Save button listener
        saveButton.addActionListener(e -> {
            if (validateForm()) {
                EventData data = createEventDataFromForm();
                if (data != null) {
                    String[] args = createEventArgs(data);
                    if (args != null) {
                        listener.onEventSaved(args, data.isRecurring);
                    }
                }
            }
        });

        // Cancel button listener
        cancelButton.addActionListener(e -> {
            if (listener != null) {
                listener.onEventCancelled();
            }
        });
    }

    /**
     * Validates the form data.
     *
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        boolean isValid = true;
        clearErrorLabels();

        // Validate subject
        if (subjectField.getText().trim().isEmpty()) {
            showError(subjectField, "Subject is required");
            isValid = false;
        }

        // Validate time
        Date startTime = (Date) startTimeSpinner.getValue();
        Date endTime = (Date) endTimeSpinner.getValue();
        if (startTime.after(endTime)) {
            showError(startTimeSpinner, "Start time must be before end time");
            showError(endTimeSpinner, "End time must be after start time");
            isValid = false;
        }

        // Validate recurring event options if enabled
        if (recurringCheckBox.isSelected()) {
            if (weekdaysList.getSelectedValuesList().isEmpty()) {
                showError(weekdaysList, "Select at least one weekday");
                isValid = false;
            }

            // Validate occurrences/until date
            Date untilDate = (Date) untilDateSpinner.getValue();
            if (untilDate.before(startTime)) {
                showError(untilDateSpinner, "Until date must be after start date");
                isValid = false;
            }
        }

        // Update save button state
        saveButton.setEnabled(isValid);

        return isValid;
    }

    private void showError(JComponent component, String message) {
        JLabel errorLabel = errorLabels.get(component);
        if (errorLabel != null) {
            errorLabel.setToolTipText(message);
            errorLabel.setVisible(true);
        }
    }

    private void clearErrorLabels() {
        for (JLabel label : errorLabels.values()) {
            label.setVisible(false);
            label.setToolTipText(null);
        }
    }

    /**
     * Creates event data from the form.
     *
     * @return the event data, or null if validation fails
     */
    private EventData createEventDataFromForm() {
        if (!validateForm()) {
            return null;
        }

        EventData data = new EventData();
        data.subject = subjectField.getText().trim();
        data.location = locationField.getText().trim();
        data.description = descriptionField.getText().trim();

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

        data.date = startDateTime.toLocalDate();
        data.startTime = startDateTime.toLocalTime();
        data.endTime = endDateTime.toLocalTime();

        // Set recurring event data
        data.isRecurring = recurringCheckBox.isSelected();
        if (data.isRecurring) {
            data.weekdays = Set.copyOf(weekdaysList.getSelectedValuesList());
            data.occurrences = (Integer) occurrencesSpinner.getValue();
            data.untilDate = ((Date) untilDateSpinner.getValue()).toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();
        }

        return data;
    }

    /**
     * Creates an EventData object from the form fields.
     *
     * @return the created EventData object
     */
    private EventData createEventData() {
        EventData data = new EventData();
        data.subject = subjectField.getText();
        data.location = locationField.getText();
        data.description = descriptionField.getText();
        data.startTime = LocalTime.of(
            ((java.util.Date) startTimeSpinner.getValue()).getHours(),
            ((java.util.Date) startTimeSpinner.getValue()).getMinutes()
        );
        data.endTime = LocalTime.of(
            ((java.util.Date) endTimeSpinner.getValue()).getHours(),
            ((java.util.Date) endTimeSpinner.getValue()).getMinutes()
        );
        data.isRecurring = recurringCheckBox.isSelected();
        data.date = currentDate;
        
        if (data.isRecurring) {
            data.weekdays = Set.copyOf(weekdaysList.getSelectedValuesList());
            data.occurrences = (Integer) occurrencesSpinner.getValue();
            data.untilDate = LocalDate.ofEpochDay(((java.util.Date) untilDateSpinner.getValue()).getTime());
        }

        return data;
    }

    /**
     * Creates event arguments based on the event data.
     *
     * @param data the event data
     * @return the event arguments
     */
    private String[] createEventArgs(EventData data) {
        if (data.isRecurring) {
            return new String[]{"series_from_date", "create", 
                data.subject, 
                data.date.atTime(data.startTime).toString(),
                String.format("%s,%s,%s,%s,%d,%s,%s", 
                    data.subject,
                    data.date.atTime(data.startTime),
                    data.date.atTime(data.endTime),
                    data.location,
                    data.occurrences,
                    data.weekdays,
                    data.untilDate)
            };
        } else {
            return new String[]{"single", "create",
                data.subject,
                data.date.atTime(data.startTime).toString(),
                String.format("%s,%s,%s,%s",
                    data.subject,
                    data.date.atTime(data.startTime),
                    data.date.atTime(data.endTime),
                    data.location)
            };
        }
    }

    /**
     * Creates update event arguments based on the event data.
     *
     * @param data the event data
     * @param property the property to update
     * @param newValue the new value
     * @return the event arguments
     */
    private String[] createUpdateEventArgs(EventData data, String property, String newValue) {
        if (data.isRecurring) {
            return new String[]{"series_from_date", property,
                data.subject,
                data.date.atTime(data.startTime).toString(),
                newValue
            };
        } else {
            return new String[]{"single", property,
                data.subject,
                data.date.atTime(data.startTime).toString(),
                newValue
            };
        }
    }

    /**
     * Creates delete event arguments based on the event data.
     *
     * @param data the event data
     * @return the event arguments
     */
    private String[] createDeleteEventArgs(EventData data) {
        if (data.isRecurring) {
            return new String[]{"series_from_date", "delete",
                data.subject,
                data.date.atTime(data.startTime).toString()
            };
        } else {
            return new String[]{"single", "delete",
                data.subject,
                data.date.atTime(data.startTime).toString()
            };
        }
    }

    /**
     * Displays an event in the form.
     *
     * @param event the event to display
     */
    public void displayEvent(Event event) {
        subjectField.setText(event.getSubject());
        locationField.setText(event.getLocation());
        descriptionField.setText(event.getDescription());
        dateSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
        startTimeSpinner.setValue(Date.from(event.getStartDateTime().toInstant(ZoneOffset.UTC)));
        endTimeSpinner.setValue(Date.from(event.getEndDateTime().toInstant(ZoneOffset.UTC)));
        recurringCheckBox.setSelected(false);
        recurringPanel.setVisible(false);
    }

    /**
     * Displays a recurring event in the form.
     *
     * @param event the recurring event to display
     */
    public void displayRecurringEvent(RecurringEvent event) {
        displayEvent(event);
        recurringCheckBox.setSelected(true);
        recurringPanel.setVisible(true);
        
        // Get the weekdays from the event
        Set<DayOfWeek> weekdays = event.getRepeatDays();
        
        // Find indices of the weekdays in the list
        int[] indices = new int[weekdays.size()];
        int index = 0;
        for (int i = 0; i < weekdaysList.getModel().getSize(); i++) {
            if (weekdays.contains(weekdaysList.getModel().getElementAt(i))) {
                indices[index++] = i;
            }
        }
        
        weekdaysList.setSelectedIndices(indices);
        occurrencesSpinner.setValue(event.getOccurrences());
        untilDateSpinner.setValue(Date.from(event.getEndDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

    /**
     * Sets the current date.
     *
     * @param date the date to set
     */
    public void setDate(LocalDate date) {
        this.currentDate = date;
        if (date != null) {
            dateSpinner.setValue(Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
    }

    /**
     * Sets the list of events for the current date.
     *
     * @param events the list of events
     */
    public void setEvents(List<Event> events) {
        // This method can be used to display a list of events for the selected date
        // Implementation depends on how you want to show the list
    }

    /**
     * Clears the form.
     */
    public void clearForm() {
        subjectField.setText("");
        locationField.setText("");
        descriptionField.setText("");
        dateSpinner.setValue(Date.from(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC)));
        startTimeSpinner.setValue(Date.from(LocalTime.of(9, 0).atDate(LocalDate.now()).toInstant(ZoneOffset.UTC)));
        endTimeSpinner.setValue(Date.from(LocalTime.of(10, 0).atDate(LocalDate.now()).toInstant(ZoneOffset.UTC)));
        recurringCheckBox.setSelected(false);
        recurringPanel.setVisible(false);
        weekdaysList.clearSelection();
        occurrencesSpinner.setValue(1);
        untilDateSpinner.setValue(Date.from(LocalDate.now().plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
        clearErrorLabels();
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
        EventData data = createEventData();
        if (event instanceof RecurringEvent) {
            RecurringEvent recurringEvent = (RecurringEvent) event;
            data.isRecurring = true;
            data.weekdays = recurringEvent.getRepeatDays();
            data.occurrences = recurringEvent.getOccurrences();
            data.untilDate = recurringEvent.getEndDate();
        }
        
        // Update each property using the strategy pattern
        String[] args = createUpdateEventArgs(data, "subject", data.subject);
        listener.onEventUpdated(args, data.isRecurring);
        
        args = createUpdateEventArgs(data, "startDateTime", data.date.atTime(data.startTime).toString());
        listener.onEventUpdated(args, data.isRecurring);
        
        args = createUpdateEventArgs(data, "endDateTime", data.date.atTime(data.endTime).toString());
        listener.onEventUpdated(args, data.isRecurring);
        
        args = createUpdateEventArgs(data, "location", data.location);
        listener.onEventUpdated(args, data.isRecurring);
        
        args = createUpdateEventArgs(data, "description", data.description);
        listener.onEventUpdated(args, data.isRecurring);
    }

    /**
     * Deletes an event.
     *
     * @param event the event to delete
     */
    public void deleteEvent(Event event) {
        EventData data = createEventData();
        if (event instanceof RecurringEvent) {
            data.isRecurring = true;
        }
        
        String[] args = createDeleteEventArgs(data);
        listener.onEventDeleted(args, data.isRecurring);
    }
} 
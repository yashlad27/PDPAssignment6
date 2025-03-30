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
import java.util.ArrayList;

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
    private final List<JCheckBox> weekdayCheckboxes;
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
        void onEventSaved(String[] eventData, boolean isRecurring);
        void onEventCancelled();
        void onEventUpdated(String[] args, boolean isRecurring);
    }

    /**
     * Constructs a new GUIEventPanel.
     */
    public GUIEventPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Event Details"));

        // Initialize components
        subjectField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

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

        locationField = new JTextField(20);
        recurringCheckBox = new JCheckBox("Recurring Event");
        
        // Initialize recurring options
        recurringOptionsPanel = new JPanel();
        recurringOptionsPanel.setLayout(new BoxLayout(recurringOptionsPanel, BoxLayout.Y_AXIS));
        recurringOptionsPanel.setBorder(BorderFactory.createTitledBorder("Recurring Options"));
        recurringOptionsPanel.setVisible(false);

        // Weekday checkboxes
        JPanel weekdayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weekdayPanel.add(new JLabel("Repeat on: "));
        weekdayCheckboxes = new ArrayList<>();
        String[] weekdays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String day : weekdays) {
            JCheckBox checkbox = new JCheckBox(day);
            weekdayCheckboxes.add(checkbox);
            weekdayPanel.add(checkbox);
        }
        recurringOptionsPanel.add(weekdayPanel);

        // Occurrences spinner
        JPanel occurrencesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        occurrencesPanel.add(new JLabel("Number of occurrences: "));
        occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 52, 1));
        occurrencesPanel.add(occurrencesSpinner);
        recurringOptionsPanel.add(occurrencesPanel);

        // Until date spinner
        JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        untilPanel.add(new JLabel("Until date: "));
        untilDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor untilDateEditor = new JSpinner.DateEditor(untilDateSpinner, "yyyy-MM-dd");
        untilDateSpinner.setEditor(untilDateEditor);
        untilPanel.add(untilDateSpinner);
        recurringOptionsPanel.add(untilPanel);

        // Buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        errorLabels = new HashMap<>();

        setupLayout();
        setupListeners();
    }

    private void setupLayout() {
        // Main form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Subject
        JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subjectPanel.add(new JLabel("Subject:"));
        subjectPanel.add(subjectField);
        formPanel.add(subjectPanel);

        // Description
        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(new JLabel("Description:"));
        descriptionPanel.add(new JScrollPane(descriptionArea));
        formPanel.add(descriptionPanel);

        // Date and time - using GridBagLayout for better control
        JPanel dateTimePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 2, 2, 5);

        // Date field
        gbc.gridx = 0;
        gbc.gridy = 0;
        dateTimePanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateTimePanel.add(dateSpinner, gbc);

        // Start time field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        dateTimePanel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateTimePanel.add(startTimeSpinner, gbc);

        // End time field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        dateTimePanel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateTimePanel.add(endTimeSpinner, gbc);

        formPanel.add(dateTimePanel);

        // Location
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(new JLabel("Location:"));
        locationPanel.add(locationField);
        formPanel.add(locationPanel);

        // Recurring checkbox
        JPanel recurringPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recurringPanel.add(recurringCheckBox);
        formPanel.add(recurringPanel);

        // Recurring options
        formPanel.add(recurringOptionsPanel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add to main panel with scroll support
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set preferred sizes for spinners to prevent stretching
        Dimension spinnerSize = new Dimension(120, 25);
        dateSpinner.setPreferredSize(spinnerSize);
        startTimeSpinner.setPreferredSize(spinnerSize);
        endTimeSpinner.setPreferredSize(spinnerSize);
    }

    private void setupListeners() {
        recurringCheckBox.addActionListener(e -> {
            recurringOptionsPanel.setVisible(recurringCheckBox.isSelected());
            revalidate();
            repaint();
        });

        saveButton.addActionListener(e -> {
            if (validateForm()) {
                if (listener != null) {
                    String[] eventArgs;
                    boolean isRecurring = recurringCheckBox.isSelected();
                    
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

                    if (isRecurring) {
                        Set<DayOfWeek> weekdays = weekdayCheckboxes.stream()
                            .filter(JCheckBox::isSelected)
                            .map(cb -> DayOfWeek.of((weekdayCheckboxes.indexOf(cb) + 1)))
                            .collect(java.util.stream.Collectors.toSet());
                        int occurrences = (Integer) occurrencesSpinner.getValue();
                        LocalDate untilDate = ((Date) untilDateSpinner.getValue()).toInstant()
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate();

                        eventArgs = new String[]{"series_from_date", "create", 
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
                        eventArgs = new String[]{"single", "create",
                            subject,
                            startDateTime.toString(),
                            String.format("%s,%s,%s,%s",
                                subject,
                                startDateTime,
                                endDateTime,
                                location)
                        };
                    }
                    listener.onEventSaved(eventArgs, isRecurring);
                }
            }
        });

        cancelButton.addActionListener(e -> {
            if (listener != null) {
                listener.onEventCancelled();
            }
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
        Date startTime = (Date) startTimeSpinner.getValue();
        Date endTime = (Date) endTimeSpinner.getValue();
        if (endTime.before(startTime)) {
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
            Date selectedDate = (Date) dateSpinner.getValue();
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
        descriptionArea.setText("");
        locationField.setText("");
        recurringCheckBox.setSelected(false);
        recurringOptionsPanel.setVisible(false);
        weekdayCheckboxes.forEach(cb -> cb.setSelected(false));
        occurrencesSpinner.setValue(1);
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
                .collect(java.util.stream.Collectors.toSet());
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
} 
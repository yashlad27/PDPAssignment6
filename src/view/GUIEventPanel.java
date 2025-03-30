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

/**
 * Panel class that handles event creation, editing, and display.
 */
public class GUIEventPanel extends JPanel {
    private final JTextField subjectField;
    private final JTextField locationField;
    private final JTextField descriptionField;
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
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        recurringCheckBox = new JCheckBox("Recurring Event");
        weekdaysList = new JList<>(DayOfWeek.values());
        occurrencesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        untilDateSpinner = new JSpinner(new SpinnerDateModel());
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        recurringPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        // Set up layout
        setupLayout();
        setupListeners();
    }

    /**
     * Sets up the layout of the event panel.
     */
    private void setupLayout() {
        // Main form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Add form fields
        formPanel.add(new JLabel("Subject:"));
        formPanel.add(subjectField);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(new JLabel("Start Time:"));
        formPanel.add(startTimeSpinner);
        formPanel.add(new JLabel("End Time:"));
        formPanel.add(endTimeSpinner);

        // Set up recurring panel
        recurringPanel.setBorder(BorderFactory.createTitledBorder("Recurring Options"));
        recurringPanel.add(new JLabel("Weekdays:"));
        recurringPanel.add(new JScrollPane(weekdaysList));
        recurringPanel.add(new JLabel("Occurrences:"));
        recurringPanel.add(occurrencesSpinner);
        recurringPanel.add(new JLabel("Until Date:"));
        recurringPanel.add(untilDateSpinner);
        recurringPanel.setVisible(false);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to panel
        add(formPanel, BorderLayout.NORTH);
        add(recurringPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        recurringCheckBox.addActionListener(e -> {
            recurringPanel.setVisible(recurringCheckBox.isSelected());
        });

        saveButton.addActionListener(e -> {
            if (listener != null) {
                EventData eventData = createEventData();
                String[] args = createEventArgs(eventData);
                listener.onEventSaved(args, eventData.isRecurring);
            }
        });

        cancelButton.addActionListener(e -> {
            if (listener != null) {
                listener.onEventCancelled();
            }
        });
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
        startTimeSpinner.setValue(Date.from(LocalTime.of(9, 0).atDate(LocalDate.now()).toInstant(ZoneOffset.UTC)));
        endTimeSpinner.setValue(Date.from(LocalTime.of(10, 0).atDate(LocalDate.now()).toInstant(ZoneOffset.UTC)));
        recurringCheckBox.setSelected(false);
        recurringPanel.setVisible(false);
        weekdaysList.clearSelection();
        occurrencesSpinner.setValue(1);
        untilDateSpinner.setValue(Date.from(LocalDate.now().plusMonths(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
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
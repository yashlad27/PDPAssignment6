package view;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A panel that manages calendar selection and creation.
 * This component allows users to select from existing calendars,
 * create new calendars, and view calendar timezone information.
 */
public class GUICalendarSelectorPanel extends JPanel {
    private final JPanel calendarListPanel;
    private final JButton addCalendarButton;
    private final List<CalendarSelectionListener> listeners;
    private final Set<String> calendarNames;
    private String selectedCalendar;

    /**
     * Interface for listening to calendar selection events.
     */
    public interface CalendarSelectionListener {
        void onCalendarSelected(String calendarName);
        void onCalendarCreated(String calendarName, String timezone);
    }

    /**
     * Constructs a new GUICalendarSelectorPanel.
     */
    public GUICalendarSelectorPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Calendars"));
        
        this.listeners = new ArrayList<>();
        this.calendarNames = new TreeSet<>();
        this.calendarListPanel = new JPanel();
        this.calendarListPanel.setLayout(new BoxLayout(calendarListPanel, BoxLayout.Y_AXIS));
        
        // Create add calendar button
        this.addCalendarButton = new JButton("+ Add Calendar");
        addCalendarButton.addActionListener(e -> showAddCalendarDialog());
        
        // Add components
        add(calendarListPanel);
        add(Box.createVerticalStrut(10));
        add(addCalendarButton);
        
        // Add default calendar
        addCalendar("Default", ZoneId.systemDefault().toString());
    }

    /**
     * Shows a dialog for creating a new calendar.
     */
    private void showAddCalendarDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   "Add New Calendar", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Calendar name field
        JTextField nameField = new JTextField(20);
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Calendar Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        dialog.add(nameField, gbc);
        
        // Timezone selector
        JComboBox<String> timezoneCombo = new JComboBox<>(getAvailableTimezones());
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Timezone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        dialog.add(timezoneCombo, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String timezone = (String) timezoneCombo.getSelectedItem();
            if (!name.isEmpty()) {
                addCalendar(name, timezone);
                dialog.dispose();
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Gets a list of available timezone IDs.
     *
     * @return array of timezone IDs
     */
    private String[] getAvailableTimezones() {
        return ZoneId.getAvailableZoneIds().toArray(new String[0]);
    }

    /**
     * Adds a new calendar to the selector.
     *
     * @param name the name of the calendar
     * @param timezone the timezone of the calendar
     */
    public void addCalendar(String name, String timezone) {
        if (calendarNames.add(name)) {
            JPanel calendarPanel = new JPanel(new BorderLayout());
            JRadioButton radioButton = new JRadioButton(name);
            JLabel timezoneLabel = new JLabel("(" + timezone + ")");
            timezoneLabel.setForeground(Color.GRAY);
            
            ButtonGroup group = new ButtonGroup();
            group.add(radioButton);
            
            radioButton.addActionListener(e -> {
                selectedCalendar = name;
                notifyCalendarSelected(name);
            });
            
            calendarPanel.add(radioButton, BorderLayout.WEST);
            calendarPanel.add(timezoneLabel, BorderLayout.EAST);
            
            calendarListPanel.add(calendarPanel);
            calendarListPanel.revalidate();
            calendarListPanel.repaint();
            
            notifyCalendarCreated(name, timezone);
        }
    }

    /**
     * Adds a calendar selection listener.
     *
     * @param listener the listener to add
     */
    public void addCalendarSelectionListener(CalendarSelectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners that a calendar was selected.
     *
     * @param calendarName the name of the selected calendar
     */
    private void notifyCalendarSelected(String calendarName) {
        for (CalendarSelectionListener listener : listeners) {
            listener.onCalendarSelected(calendarName);
        }
    }

    /**
     * Notifies all listeners that a calendar was created.
     *
     * @param calendarName the name of the created calendar
     * @param timezone the timezone of the created calendar
     */
    private void notifyCalendarCreated(String calendarName, String timezone) {
        for (CalendarSelectionListener listener : listeners) {
            listener.onCalendarCreated(calendarName, timezone);
        }
    }

    /**
     * Gets the currently selected calendar.
     *
     * @return the name of the selected calendar
     */
    public String getSelectedCalendar() {
        return selectedCalendar;
    }
} 
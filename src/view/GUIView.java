package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Swing-based implementation of the ICalendarView interface.
 * This class provides a graphical user interface for the calendar application
 * using Java Swing components.
 */
public class GUIView implements ICalendarView {
    private final JFrame mainFrame;
    private final JTextArea messageArea;
    private final BlockingQueue<String> commandQueue;
    private final JTextField commandInput;
    private final JButton submitButton;
    private final GUICalendarPanel calendarPanel;
    private final JPanel leftSidebar;
    private final JPanel mainContent;
    private final JPanel eventDetailsPanel;

    /**
     * Constructs a new GUIView.
     * Initializes the main window and its components.
     */
    public GUIView() {
        this.commandQueue = new LinkedBlockingQueue<>();
        this.mainFrame = new JFrame("Calendar Application");
        this.messageArea = new JTextArea(10, 40);
        this.commandInput = new JTextField(40);
        this.submitButton = new JButton("Submit");
        this.calendarPanel = new GUICalendarPanel();
        this.leftSidebar = new JPanel();
        this.mainContent = new JPanel();
        this.eventDetailsPanel = new JPanel();

        initializeUI();
    }

    /**
     * Initializes the user interface components and layout.
     */
    private void initializeUI() {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setSize(800, 600);

        // Create left sidebar
        leftSidebar.setPreferredSize(new Dimension(200, 0));
        leftSidebar.setLayout(new BoxLayout(leftSidebar, BoxLayout.Y_AXIS));
        leftSidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add calendar selector
        JPanel calendarSelector = new JPanel();
        calendarSelector.setLayout(new BoxLayout(calendarSelector, BoxLayout.Y_AXIS));
        calendarSelector.setBorder(BorderFactory.createTitledBorder("Calendars"));
        
        JButton addCalendarButton = new JButton("+ Add Calendar");
        calendarSelector.add(addCalendarButton);
        leftSidebar.add(calendarSelector);

        // Add import/export panel
        JPanel importExportPanel = new JPanel();
        importExportPanel.setLayout(new BoxLayout(importExportPanel, BoxLayout.Y_AXIS));
        importExportPanel.setBorder(BorderFactory.createTitledBorder("Import/Export"));
        
        JButton importButton = new JButton("Import from CSV");
        JButton exportButton = new JButton("Export to CSV");
        importExportPanel.add(importButton);
        importExportPanel.add(exportButton);
        leftSidebar.add(importExportPanel);

        // Create main content area
        mainContent.setLayout(new BorderLayout());
        mainContent.add(calendarPanel, BorderLayout.CENTER);

        // Create event details panel
        eventDetailsPanel.setLayout(new BoxLayout(eventDetailsPanel, BoxLayout.Y_AXIS));
        eventDetailsPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));
        eventDetailsPanel.setPreferredSize(new Dimension(0, 150));
        
        // Add event detail fields
        JTextField subjectField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField startTimeField = new JTextField();
        JTextField endTimeField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField descriptionField = new JTextField();
        
        eventDetailsPanel.add(new JLabel("Subject:"));
        eventDetailsPanel.add(subjectField);
        eventDetailsPanel.add(new JLabel("Date:"));
        eventDetailsPanel.add(dateField);
        eventDetailsPanel.add(new JLabel("Start Time:"));
        eventDetailsPanel.add(startTimeField);
        eventDetailsPanel.add(new JLabel("End Time:"));
        eventDetailsPanel.add(endTimeField);
        eventDetailsPanel.add(new JLabel("Location:"));
        eventDetailsPanel.add(locationField);
        eventDetailsPanel.add(new JLabel("Description:"));
        eventDetailsPanel.add(descriptionField);
        
        mainContent.add(eventDetailsPanel, BorderLayout.SOUTH);

        // Create message area
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        messageScrollPane.setPreferredSize(new Dimension(0, 100));
        mainContent.add(messageScrollPane, BorderLayout.NORTH);

        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(commandInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        // Add action listener for submit button
        submitButton.addActionListener(e -> {
            String command = commandInput.getText().trim();
            if (!command.isEmpty()) {
                commandQueue.offer(command);
                commandInput.setText("");
            }
        });

        // Add action listener for enter key in command input
        commandInput.addActionListener(e -> submitButton.doClick());

        // Add components to main frame
        mainFrame.add(leftSidebar, BorderLayout.WEST);
        mainFrame.add(mainContent, BorderLayout.CENTER);
        mainFrame.add(inputPanel, BorderLayout.SOUTH);

        mainFrame.setLocationRelativeTo(null);
    }

    /**
     * Shows the main window.
     */
    public void show() {
        mainFrame.setVisible(true);
    }

    @Override
    public String readCommand() {
        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @Override
    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    @Override
    public void displayError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append("ERROR: " + errorMessage + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }
} 
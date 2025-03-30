package view;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that manages calendar data import and export operations.
 * This component provides buttons for importing from and exporting to CSV files.
 */
public class GUIExportImportPanel extends JPanel {
    private final JButton importButton;
    private final JButton exportButton;
    private final List<ExportImportListener> listeners;
    private static final FileNameExtensionFilter CSV_FILTER = 
        new FileNameExtensionFilter("CSV Files (*.csv)", "csv");

    /**
     * Interface for listening to export/import events.
     */
    public interface ExportImportListener {
        void onImportRequested(File file);
        void onExportRequested(File file);
    }

    /**
     * Constructs a new GUIExportImportPanel.
     */
    public GUIExportImportPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Import/Export"));
        
        this.listeners = new ArrayList<>();
        
        // Create buttons
        this.importButton = new JButton("Import from CSV");
        this.exportButton = new JButton("Export to CSV");
        
        // Add components
        add(importButton);
        add(Box.createVerticalStrut(5));
        add(exportButton);
        
        // Add listeners
        setupListeners();
    }

    /**
     * Sets up event listeners for the buttons.
     */
    private void setupListeners() {
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(CSV_FILTER);
            fileChooser.setDialogTitle("Import Calendar Data");
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    notifyImportRequested(selectedFile);
                }
            }
        });
        
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(CSV_FILTER);
            fileChooser.setDialogTitle("Export Calendar Data");
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    // Ensure the file has a .csv extension
                    if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
                        selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
                    }
                    notifyExportRequested(selectedFile);
                }
            }
        });
    }

    /**
     * Adds an export/import listener.
     *
     * @param listener the listener to add
     */
    public void addExportImportListener(ExportImportListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners that an import was requested.
     */
    private void notifyImportRequested(File file) {
        for (ExportImportListener listener : listeners) {
            listener.onImportRequested(file);
        }
    }

    /**
     * Notifies all listeners that an export was requested.
     */
    private void notifyExportRequested(File file) {
        for (ExportImportListener listener : listeners) {
            listener.onExportRequested(file);
        }
    }

    /**
     * Shows a success message for an import operation.
     */
    public void showImportSuccess() {
        JOptionPane.showMessageDialog(this, "Calendar imported successfully");
    }

    /**
     * Shows a success message for an export operation.
     */
    public void showExportSuccess() {
        JOptionPane.showMessageDialog(this, "Calendar exported successfully");
    }

    /**
     * Shows an error message.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
} 
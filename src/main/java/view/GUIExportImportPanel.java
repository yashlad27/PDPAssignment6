package view;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A panel that manages calendar data import and export operations.
 * This component provides buttons for importing from and exporting to CSV files.
 */
public class GUIExportImportPanel extends JPanel {
  private final JButton importButton;
  private final JButton exportButton;
  private final JFileChooser fileChooser;
  private File importFile;
  private File exportFile;
  private JLabel importFileLabel;
  private JLabel exportFileLabel;
  private JLabel statusLabel;
  private ExportImportListener listener;
  private final List<ExportImportListener> listeners;

  /**
   * Interface for listening to export/import events.
   */
  public interface ExportImportListener {
    void onImport(File file);

    void onExport(File file);
  }

  /**
   * Constructs a new GUIExportImportPanel.
   */
  public GUIExportImportPanel() {
    setBorder(BorderFactory.createTitledBorder("Import/Export"));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Set alignment for centering components
    setAlignmentX(Component.CENTER_ALIGNMENT);

    // Create a panel for import section
    JPanel importPanel = new JPanel();
    importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.Y_AXIS));
    importPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    importPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel importLabel = new JLabel("Import from CSV");
    importLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    importButton = new JButton("Choose File...");
    ButtonStyler.applyPrimaryStyle(importButton);
    importButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    importButton.setMaximumSize(new Dimension(150, importButton.getPreferredSize().height));

    importPanel.add(importLabel);
    importPanel.add(Box.createVerticalStrut(5));
    importPanel.add(importButton);

    // Create a panel for export section
    JPanel exportPanel = new JPanel();
    exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
    exportPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    exportPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel exportLabel = new JLabel("Export to CSV");
    exportLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    exportButton = new JButton("Export Calendar");
    ButtonStyler.applyPrimaryStyle(exportButton);
    exportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    exportButton.setMaximumSize(new Dimension(150, exportButton.getPreferredSize().height));

    exportPanel.add(exportLabel);
    exportPanel.add(Box.createVerticalStrut(5));
    exportPanel.add(exportButton);

    // Initialize file labels
    importFileLabel = new JLabel(" ");
    importFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    importFileLabel.setForeground(Color.GRAY);
    importPanel.add(Box.createVerticalStrut(5));
    importPanel.add(importFileLabel);

    exportFileLabel = new JLabel(" ");
    exportFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    exportFileLabel.setForeground(Color.GRAY);
    exportPanel.add(Box.createVerticalStrut(5));
    exportPanel.add(exportFileLabel);

    // Initialize status label
    statusLabel = new JLabel(" ");
    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    statusLabel.setForeground(Color.GRAY);

    // Add panels to main panel
    add(importPanel);
    add(Box.createVerticalStrut(10));
    add(exportPanel);
    add(Box.createVerticalStrut(5));
    add(statusLabel);
    add(Box.createVerticalGlue());

    // Initialize file chooser
    fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    // Set minimum and preferred sizes
    setMinimumSize(new Dimension(160, 200));
    setPreferredSize(new Dimension(180, 250));

    this.listeners = new ArrayList<>();
    setupListeners();
  }

  /**
   * Sets up event listeners for the buttons.
   */
  private void setupListeners() {
    // Set up the Choose File button for import
    importButton.addActionListener(e -> {
      System.out.println("[DEBUG] Import Choose File button clicked");
      fileChooser.setDialogTitle("Select CSV File to Import");

      // Set up file filter for CSV files
      FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
      fileChooser.setFileFilter(filter);

      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        importFile = fileChooser.getSelectedFile();
        System.out.println("[DEBUG] Selected import file: " + importFile.getAbsolutePath());
        importFileLabel.setText(importFile.getName());

        // Automatically trigger the import process after file selection
        if (importFile != null && listener != null) {
          try {
            System.out.println("[DEBUG] Automatically starting import process for file: " + importFile.getAbsolutePath());

            // Confirm import with user
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Import events from " + importFile.getName() + "?",
                    "Confirm Import",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
              System.out.println("[DEBUG] User confirmed import, proceeding...");
              setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

              // Call the listener to handle the import
              listener.onImport(importFile);

              setCursor(Cursor.getDefaultCursor());
              // The success message will be shown by the viewmodel callback
            } else {
              System.out.println("[DEBUG] User cancelled import");
            }
          } catch (Exception ex) {
            System.err.println("[ERROR] Import failed: " + ex.getMessage());
            ex.printStackTrace();
            setCursor(Cursor.getDefaultCursor());
            showStatus("Import failed: " + ex.getMessage(), false);
            showError("Import failed: " + ex.getMessage());
          }
        }
      }
    });

    exportButton.addActionListener(e -> {
      System.out.println("[DEBUG] Export Calendar button clicked");
      fileChooser.setDialogTitle("Save Calendar Data as CSV");
      
      // Set up file filter for CSV files
      FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
      fileChooser.setFileFilter(filter);
      
      // Set the default directory to the project root
      fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      
      if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        exportFile = fileChooser.getSelectedFile();
        // Ensure the file has .csv extension
        if (!exportFile.getName().toLowerCase().endsWith(".csv")) {
          exportFile = new File(exportFile.getParentFile(), exportFile.getName() + ".csv");
        }
        
        System.out.println("[DEBUG] Selected export file: " + exportFile.getAbsolutePath());
        exportFileLabel.setText(exportFile.getName());
        
        // Automatically start the export process after file selection
        if (exportFile != null) {
          try {
            System.out.println("[DEBUG] Preparing to export calendar data to: " + exportFile.getAbsolutePath());
            System.out.println("[DEBUG] File exists before export: " + exportFile.exists());
            System.out.println("[DEBUG] File parent directory: " + exportFile.getParentFile().getAbsolutePath());
            System.out.println("[DEBUG] File can write: " + exportFile.getParentFile().canWrite());
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Notify all listeners
            int listenerCount = listeners.size();
            System.out.println("[DEBUG] Notifying " + listenerCount + " export listeners");
            
            if (listenerCount == 0) {
              System.out.println("[WARNING] No export listeners registered - export may not work");
            }
            
            for (int i = 0; i < listeners.size(); i++) {
              ExportImportListener l = listeners.get(i);
              System.out.println("[DEBUG] Calling export listener #" + (i+1) + ": " + l.getClass().getName());
              l.onExport(exportFile);
            }
            
            // Verify file was created
            System.out.println("[DEBUG] Export operation completed");
            System.out.println("[DEBUG] File exists after export: " + exportFile.exists());
            if (exportFile.exists()) {
              System.out.println("[DEBUG] Export file size: " + exportFile.length() + " bytes");
            }
            
            // Show success message
            setCursor(Cursor.getDefaultCursor());
            showStatus("Export successful: " + exportFile.getName(), true);
            JOptionPane.showMessageDialog(
                this,
                "Calendar exported successfully to:\n" + exportFile.getAbsolutePath(),
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE);
          } catch (Exception ex) {
            System.err.println("[ERROR] Export failed: " + ex.getMessage());
            ex.printStackTrace();
            setCursor(Cursor.getDefaultCursor());
            showStatus("Export failed: " + ex.getMessage(), false);
            showError("Export failed: " + ex.getMessage());
          }
        }
      }
    });
    
    // Import functionality is now handled directly in the Choose File button handler
  }

  /**
   * Adds an export/import listener.
   *
   * @param listener the listener to add
   */
  public void addExportImportListener(ExportImportListener listener) {
    this.listener = listener;
    this.listeners.add(listener);
  }

  /**
   * Shows a success message for an import operation with details on the number of events imported.
   *
   * @param message The success message with details
   */
  public void showImportSuccess(String message) {
    JOptionPane.showMessageDialog(this, message, "Import Successful", JOptionPane.INFORMATION_MESSAGE);
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

  /**
   * Shows an error message.
   *
   * @param message the error message to display
   */
  public void showErrorMessage(String message) {
    showError(message);
  }

  private void showStatus(String message, boolean success) {
    statusLabel.setText(message);
    statusLabel.setForeground(success ? new Color(46, 125, 50) : new Color(198, 40, 40));
    Timer timer = new Timer(3000, e -> {
      statusLabel.setText(" ");
      statusLabel.setForeground(Color.GRAY);
    });
    timer.setRepeats(false);
    timer.start();
  }

  public void addImportListener(ActionListener listener) {
    importButton.addActionListener(listener);
  }

  public void addExportListener(ActionListener listener) {
    exportButton.addActionListener(listener);
  }

  public File showFileChooser(boolean forImport) {
    int result = forImport ?
            fileChooser.showOpenDialog(this) :
            fileChooser.showSaveDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    }
    return null;
  }
} 
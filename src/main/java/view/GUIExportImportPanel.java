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
  private static final Color THEME_COLOR = new Color(0x4a86e8);
  private static final Color THEME_LIGHT = new Color(0xe6f2ff);
  private static final Color BORDER_COLOR = new Color(0xcccccc);

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
  private static final FileNameExtensionFilter CSV_FILTER =
          new FileNameExtensionFilter("CSV Files (*.csv)", "csv");

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

    // Add panels to main panel
    add(importPanel);
    add(Box.createVerticalStrut(10));
    add(exportPanel);
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
    importButton.addActionListener(e -> {
      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        importFile = fileChooser.getSelectedFile();
        importFileLabel.setText(importFile.getName());
        importButton.setEnabled(true);
      }
    });

    exportButton.addActionListener(e -> {
      if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        exportFile = fileChooser.getSelectedFile();
        if (!exportFile.getName().toLowerCase().endsWith(".csv")) {
          exportFile = new File(exportFile.getParentFile(), exportFile.getName() + ".csv");
        }
        exportFileLabel.setText(exportFile.getName());
        exportButton.setEnabled(true);
      }
    });

    importButton.addActionListener(e -> {
      if (importFile != null && listener != null) {
        try {
          listener.onImport(importFile);
          showStatus("Import successful", true);
        } catch (Exception ex) {
          showStatus("Import failed: " + ex.getMessage(), false);
        }
      }
    });

    exportButton.addActionListener(e -> {
      if (exportFile != null && listener != null) {
        try {
          listener.onExport(exportFile);
          showStatus("Export successful", true);
        } catch (Exception ex) {
          showStatus("Export failed: " + ex.getMessage(), false);
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
    this.listener = listener;
    this.listeners.add(listener);
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
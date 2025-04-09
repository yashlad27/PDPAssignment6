package view.dialog;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.event.Event;

/**
 * Abstract base class for event dialogs that implements common functionality.
 * This follows the Single Responsibility Principle by extracting common behavior
 * and the Template Method pattern for dialog setup.
 */
public abstract class AbstractEventDialog extends JDialog implements IEventDialog {

  protected static final Color THEME_COLOR = new Color(0x4a86e8);
  protected static final Color HEADER_COLOR = new Color(0x4a86e8);
  protected static final Color INFO_BACKGROUND = new Color(0xFFFAE6);
  protected static final Color INFO_BORDER = new Color(0xFFCC80);

  protected boolean actionConfirmed = false;
  protected final Event event;

  /**
   * Constructs a new AbstractEventDialog.
   *
   * @param parent the parent component
   * @param title  the dialog title
   * @param event  the event to edit/copy
   */
  public AbstractEventDialog(Component parent, String title, Event event) {
    super(JOptionPane.getFrameForComponent(parent), title, true);
    this.event = event;

    // Common initialization
    initDialog();
  }

  /**
   * Initializes the dialog.
   * Template method pattern: defines the skeleton of the dialog setup.
   */
  protected void initDialog() {
    setSize(500, 600);
    setLocationRelativeTo(getParent());
    setResizable(true);

    initComponents();
    layoutComponents();
    setupListeners();
    loadEventData();
  }

  /**
   * Initializes the dialog components.
   */
  protected abstract void initComponents();

  /**
   * Lays out the dialog components.
   */
  protected abstract void layoutComponents();

  /**
   * Sets up event listeners for the dialog components.
   */
  protected abstract void setupListeners();

  /**
   * Loads event data into the dialog components.
   */
  protected abstract void loadEventData();

  /**
   * Creates an information panel with conflict prevention message.
   *
   * @param message the message to display
   * @return the created panel
   */
  protected JPanel createInfoPanel(String message) {
    JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(10, 15, 0, 15));

    JTextArea infoText = new JTextArea(message);
    infoText.setEditable(false);
    infoText.setLineWrap(true);
    infoText.setWrapStyleWord(true);
    infoText.setBackground(INFO_BACKGROUND);
    infoText.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INFO_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    infoPanel.add(infoText, BorderLayout.CENTER);
    return infoPanel;
  }

  /**
   * Creates a button panel with the specified buttons.
   *
   * @param confirmButton the confirm button (save/copy)
   * @param cancelButton  the cancel button
   * @return the created panel
   */
  protected JPanel createButtonPanel(JButton confirmButton, JButton cancelButton) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(confirmButton);
    buttonPanel.add(cancelButton);
    return buttonPanel;
  }

  /**
   * Creates standard GridBagConstraints with common settings.
   *
   * @return the created constraints
   */
  protected GridBagConstraints createGBC() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);
    return gbc;
  }

  /**
   * Shows the dialog and returns whether the action was confirmed.
   *
   * @return true if the action was confirmed, false otherwise
   */
  @Override
  public boolean showDialog() {
    setVisible(true);
    return actionConfirmed;
  }
}

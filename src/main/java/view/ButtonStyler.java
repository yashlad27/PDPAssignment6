package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Utility class for styling buttons consistently across the application.
 */
public class ButtonStyler {
  private static final String EDIT_EVENT_TITLE = "Edit Event";
  private static final String EDIT_EVENT_MESSAGE = "Event is being edited";
  private static final Color PRIMARY_COLOR = new Color(0x4a86e8);
  private static final Color PRIMARY_HOVER_COLOR = new Color(0x2962ff);
  private static final Color SECONDARY_COLOR = new Color(0xf0f0f0);
  private static final Color SECONDARY_HOVER_COLOR = new Color(0xe0e0e0);
  private static final Color TEXT_LIGHT = Color.WHITE;
  private static final Color TEXT_DARK = new Color(0x333333);

  /**
   * Applies a modern primary button style (blue).
   *
   * @param button the button to style
   * @return the styled button
   */
  public static JButton applyPrimaryStyle(JButton button) {
    return applyStyle(button, PRIMARY_COLOR, PRIMARY_HOVER_COLOR, TEXT_LIGHT, true);
  }

  /**
   * Applies a modern secondary button style (light gray).
   *
   * @param button the button to style
   * @return the styled button
   */
  public static JButton applySecondaryStyle(JButton button) {
    return applyStyle(button, SECONDARY_COLOR, SECONDARY_HOVER_COLOR, TEXT_DARK, false);
  }

  /**
   * Shows an edit event popup message.
   *
   * @param parent the parent frame
   */
  public static void showEditEventPopup(JFrame parent) {
    JOptionPane.showMessageDialog(parent,
            EDIT_EVENT_MESSAGE,
            EDIT_EVENT_TITLE,
            JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Applies a custom button style with specified colors.
   *
   * @param button     the button to style
   * @param baseColor  the base button color
   * @param hoverColor the hover button color
   * @param textColor  the text color
   * @param isBold     whether the text should be bold
   * @return the styled button
   */
  public static JButton applyStyle(JButton button, Color baseColor, Color hoverColor,
                                   Color textColor, boolean isBold) {
    System.out.println("[DEBUG-BUTTON] Styling button: " + button.getText());

    button.setBackground(baseColor);
    button.setForeground(textColor);
    button.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, 12));
    button.setFocusPainted(false);

    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(true);

    button.putClientProperty("JButton.buttonType", "square");

    button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));

    button.setPreferredSize(new Dimension(100, 32));
    button.setMinimumSize(new Dimension(100, 32));

    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        System.out.println("[DEBUG-BUTTON] Mouse entered: " + button.getText());
        button.setBackground(hoverColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hoverColor, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
      }

      @Override
      public void mouseExited(MouseEvent e) {
        System.out.println("[DEBUG-BUTTON] Mouse exited: " + button.getText());
        button.setBackground(baseColor);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseColor, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
      }

      @Override
      public void mousePressed(MouseEvent e) {
        System.out.println("[DEBUG-BUTTON] Button pressed: " + button.getText());
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        System.out.println("[DEBUG-BUTTON] Button released: " + button.getText());
      }
    });

    return button;
  }
}

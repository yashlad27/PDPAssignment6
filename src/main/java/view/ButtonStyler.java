package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

/**
 * Utility class for styling buttons consistently across the application.
 */
public class ButtonStyler {
  // Theme colors
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

    // Set basic properties
    button.setBackground(baseColor);
    button.setForeground(textColor);
    button.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, 12));
    button.setFocusPainted(false);

    // These properties are critical for button visibility
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(true);

    // Fix for certain look and feels that might override these settings
    button.putClientProperty("JButton.buttonType", "square");

    // Apply border with matching color - using a compound border for better visibility
    button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));

    // Set preferred size for consistent button dimensions
    button.setPreferredSize(new Dimension(100, 32));
    button.setMinimumSize(new Dimension(100, 32));

    // Add hover effect with debug statements
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

  /**
   * Creates a styled primary button.
   *
   * @param text the button text
   * @return a new styled primary button
   */
  public static JButton createPrimaryButton(String text) {
    return applyPrimaryStyle(new JButton(text));
  }

  /**
   * Creates a styled secondary button.
   *
   * @param text the button text
   * @return a new styled secondary button
   */
  public static JButton createSecondaryButton(String text) {
    return applySecondaryStyle(new JButton(text));
  }
}

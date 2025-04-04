package view;

import java.awt.*;

import javax.swing.border.Border;

public class RoundRectBorder implements Border {
  private final Color color;
  private final int radius;
  private final Insets insets;

  public RoundRectBorder(Color color, int radius) {
    this.color = color;
    this.radius = radius;
    this.insets = new Insets(8, 16, 8, 16);
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(c.getBackground());

    // Fill the background with rounded corners
    g2.fillRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);

    // Draw the border
    g2.setColor(color);
    g2.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);

    g2.dispose();
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return insets;
  }

  @Override
  public boolean isBorderOpaque() {
    return true;
  }
} 
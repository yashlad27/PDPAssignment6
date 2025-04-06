package utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Utility class to handle popup dialogs for the calendar application.
 */
public class PopupImageHandler {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 150;
    private static final int DISPLAY_TIME_MS = 1500; // Time to display popup (1.5 seconds)
    private static final Color EDIT_COLOR = new Color(0x4a86e8); // Blue color for edit popups
    private static final Color COPY_COLOR = new Color(0x2e8b57); // Green color for copy popups
    
    /**
     * Shows a styled text popup dialog that disappears after a short time.
     * @param parent The parent frame for the dialog
     * @param title The title for the popup dialog
     * @param message The message to display in the popup
     * @param bgColor The background color for the popup
     * @param width The width of the dialog
     * @param height The height of the dialog
     */
    public static void showTextPopup(JFrame parent, String title, String message, Color bgColor, int width, int height) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[DEBUG] Creating text popup: " + title);
                
                // Create a dialog
                JDialog dialog = new JDialog(parent, title, false); // Non-modal
                dialog.setSize(width, height);
                dialog.setLocationRelativeTo(parent);
                dialog.setUndecorated(true); // Remove window decorations for a cleaner look
                
                // Create main panel with background color
                JPanel mainPanel = new JPanel(new BorderLayout());
                mainPanel.setBackground(bgColor);
                mainPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
                
                // Create header panel for title
                JPanel headerPanel = new JPanel(new BorderLayout());
                headerPanel.setBackground(bgColor.darker());
                headerPanel.setPreferredSize(new Dimension(width, 30));
                
                // Create title label
                JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                titleLabel.setForeground(Color.WHITE);
                titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                headerPanel.add(titleLabel, BorderLayout.CENTER);
                
                // Create message label
                JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
                messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                messageLabel.setForeground(Color.WHITE);
                messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
                
                // Add components to main panel
                mainPanel.add(headerPanel, BorderLayout.NORTH);
                mainPanel.add(messageLabel, BorderLayout.CENTER);
                dialog.add(mainPanel);
                
                System.out.println("[DEBUG] Dialog components created");
                
                // Set up auto-close timer
                Timer timer = new Timer(DISPLAY_TIME_MS, e -> {
                    System.out.println("[DEBUG] Popup timer expired, closing dialog");
                    dialog.dispose();
                });
                timer.setRepeats(false);
                timer.start();
                System.out.println("[DEBUG] Started auto-close timer: " + DISPLAY_TIME_MS + "ms");
                
                // Show the dialog
                dialog.setVisible(true);
                System.out.println("[DEBUG] Dialog is now visible");
                
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to show text popup: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Shows a text popup dialog with default dimensions.
     * @param parent The parent frame for the dialog
     * @param title The title for the popup dialog
     * @param message The message to display
     * @param bgColor The background color
     */
    public static void showTextPopup(JFrame parent, String title, String message, Color bgColor) {
        showTextPopup(parent, title, message, bgColor, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Shows the edit event popup.
     * @param parent The parent frame for the dialog
     */
    public static void showEditEventPopup(JFrame parent) {
        System.out.println("[DEBUG] Showing edit event popup, parent frame: " + (parent != null ? parent.getTitle() : "null"));
        showTextPopup(parent, "Edit Event", "Editing event...", EDIT_COLOR);
    }
    
    /**
     * Shows the copy event popup.
     * @param parent The parent frame for the dialog
     */
    public static void showCopyEventPopup(JFrame parent) {
        System.out.println("[DEBUG] Showing copy event popup, parent frame: " + (parent != null ? parent.getTitle() : "null"));
        showTextPopup(parent, "Copy Event", "Copying event...", COPY_COLOR);
    }
}

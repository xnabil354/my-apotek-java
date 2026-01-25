package com.example.My.Apotek.desktop;

public class DesktopLauncher {

    public static void main(String[] args) {
        System.out.println("🚀 Starting MyApotek Desktop App...");

        // Setup simple Logging to File for Debugging
        try {
            java.util.logging.FileHandler fh = new java.util.logging.FileHandler("MyApotek_Debug.log");
            java.util.logging.Logger.getLogger("").addHandler(fh);
        } catch (Exception e) {
        }

        java.awt.EventQueue.invokeLater(() -> {
            try {
                System.setProperty("java.awt.headless", "false");
                MyApotekApp app = new MyApotekApp();
                app.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Error Fatal: " + t.getMessage(),
                        "Crash Report", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}

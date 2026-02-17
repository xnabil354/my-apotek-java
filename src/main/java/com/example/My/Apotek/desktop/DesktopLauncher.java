package com.example.My.Apotek.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class DesktopLauncher {

    public static void main(String[] args) {
        System.out.println("Starting MyApotek Desktop App...");
        try {
            java.util.logging.FileHandler fh = new java.util.logging.FileHandler("MyApotek_Debug.log");
            java.util.logging.Logger.getLogger("").addHandler(fh);
        } catch (Exception e) {
        }

        try {
            FlatLightLaf.setup();
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
            }
        }
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);

        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("java.awt.headless", "false");
                MyApotekApp app = new MyApotekApp();
                if (!app.showLoginDialog()) {
                    System.exit(0);
                    return;
                }
                app.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error Fatal: " + t.getMessage(),
                        "Crash Report", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}

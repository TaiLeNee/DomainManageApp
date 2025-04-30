package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class SupportPanel extends JPanel {
    public SupportPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Hỗ trợ", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
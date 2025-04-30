package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {
    public ProfilePanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Thông tin cá nhân", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {
    public HomePanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Trang chính", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
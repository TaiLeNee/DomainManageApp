package view.UserView.panels;

import javax.swing.*;
import java.awt.*;

public class OrdersPanel extends JPanel {
    public OrdersPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Đơn hàng", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.CENTER);
    }
}
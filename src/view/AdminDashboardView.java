package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboardView extends JFrame {

    private JPanel left;
    private JPanel right;

    AdminDashboardView() {
        left = new JPanel();
        right = new JPanel();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1920, 1080);
        this.setLayout(new BorderLayout());
        this.setTitle("Admin Dashboard");

        //Thiết lập panel trái
        left.setPreferredSize(new Dimension(366,1080));
        left.setBackground(new Color(0, 102,102));
        left.setLayout(new BorderLayout());

        //----TIÊU ĐỀ----
        // Panel chứa tiêu đề
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 102, 102));
        titlePanel.setPreferredSize(new Dimension(336, 150));
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Tiêu đề ADMIN
        JLabel title = new JLabel("ADMIN");
        title.setFont(new Font("Segoe UI Black", Font.BOLD, 48));
        title.setForeground(Color.WHITE);

        //Thêm title vào panel
        titlePanel.add(title);
        left.add(titlePanel, BorderLayout.NORTH);

        //--4 BUTTON CHỨC NĂNG--
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        wrapper.setBackground(new Color(0, 102, 102));
        //Panel chứa nút bấm
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(336, 500));
        buttonPanel.setBackground(new Color(0, 102, 102));
        buttonPanel.setLayout(new GridLayout(4, 1, 20, 20));

        //Tạo 4 button
        JButton button1 = new JButton("Phần mở rộng tên miền");
        JButton button2 = new JButton("Quản lý tên miền");
        JButton button3 = new JButton("Quản lý đơn hàng");
        JButton button4 = new JButton("Báo cáo & thống kê");

        //Kích thước và màu sắc của button và thêm button vào panel
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 24);
        Color buttonbg = new Color(0,102,102);
        Color buttontext = Color.WHITE;

        JButton[] buttons = {button1, button2, button3, button4};
        for(JButton button : buttons) {
            button.setFont(buttonFont);
            button.setBackground(buttonbg);
            button.setForeground(buttontext);
            button.setBorder(null);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(280, 60));

            //Đổi màu chữ khi di chuột vào button
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setForeground(Color.BLACK);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setForeground(buttontext);
                }
            });

            buttonPanel.add(button);
        }

        wrapper.add(buttonPanel);
        left.add(wrapper, BorderLayout.CENTER);

        //--BUTTON ĐĂNG XUẤT--

        //Tạo panel chứa button đăng xuất
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(new Color(0,102,102));
        logoutPanel.setPreferredSize(new Dimension(336, 80));
        logoutPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        //Tạo nút bấm đăng xuất (tương tự 4 button trên nhưng nhỏ hơn)
        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(0,102,102));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(null);
        logoutButton.setPreferredSize(new Dimension(200, 40));

        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setForeground(Color.WHITE);
            }
        });

        logoutPanel.add(logoutButton);
        left.add(logoutPanel, BorderLayout.SOUTH);

        this.add(left, BorderLayout.WEST);
        this.add(right, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new AdminDashboardView().setVisible(true));
    }
}

package usermanagement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class menu extends JFrame implements ActionListener {
	public JButton user, cprof, pslot, logout;
	private String username;

	private final Font buttonFont = new Font("Times New Roman", Font.BOLD, 17);

	public menu() {
		this(null);
	}

	public menu(String username) {
		this.username = username;

		Container con = getContentPane();
		con.setLayout(null);
		con.setBackground(Color.GRAY);

		user = new JButton("CUSTOMER PROFILE");
		cprof = new JButton("CAR PROFILE");
		pslot = new JButton("PARKING SLOT");
		logout = new JButton("LOGOUT");

		user.addActionListener(this);
		cprof.addActionListener(this);
		pslot.addActionListener(this);
		logout.addActionListener(this);

		user.setBounds(10, 90, 250, 30);
		cprof.setBounds(10, 130, 250, 30);
		pslot.setBounds(10, 170, 250, 30);
		logout.setBounds(800, 10, 170, 30);

		user.setFont(buttonFont);
		cprof.setFont(buttonFont);
		pslot.setFont(buttonFont);
		logout.setFont(buttonFont);

		con.add(user);
		con.add(cprof);
		con.add(pslot);
		con.add(logout);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == logout) {
			int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
					JOptionPane.YES_NO_OPTION);
			if (dialog == JOptionPane.YES_OPTION) {
				login app = new login();
				app.setTitle("User Login");
				app.setSize(450, 600);
				app.setVisible(true);
				app.setLocationRelativeTo(null);
				dispose();
			}
			return;
		}

		if (e.getSource() == user) {
			user_profile app = new user_profile(username);
			app.setTitle("Customer Profile");
			app.setSize(900, 600);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
			return;
		}

		if (e.getSource() == cprof) {
			car_profile app = new car_profile(username);
			app.setTitle("Car Profile");
			app.setSize(800, 450);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
			return;
		}

		if (e.getSource() == pslot) {
			JOptionPane.showMessageDialog(this, "Parking slot reservation screen is not available yet.");
		}
	}

	public static void main(String[] args) {
		menu app = new menu();
		app.setTitle("User Menu");
		app.setSize(1000, 400);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

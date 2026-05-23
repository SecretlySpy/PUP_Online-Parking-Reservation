package usermanagement;

import DatabaseConnection.ConnectionDB;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class login extends JFrame implements ActionListener {
	public JLabel unam, pas, lblClock;
	public JTextField t1;
	public JButton login, res, reg1;
	public JCheckBox show;
	public JPasswordField pfpwd;
	public Connection conn = null;

	private final Font f1 = new Font("Times New Roman", Font.BOLD, 17);
	private final Font f2 = new Font("Arial", Font.ITALIC, 13);
	private final Font f3 = new Font("Times New Roman", Font.PLAIN, 15);

	public login() {
		conn = ConnectionDB.getConnection();

		Container con = getContentPane();
		con.setLayout(null);

		con.add(lblClock = new JLabel(""));
		lblClock.setBounds(170, 10, 260, 30);
		lblClock.setFont(f3);

		JLabel title = new JLabel("User Login");
		title.setBounds(170, 70, 160, 30);
		title.setFont(new Font("Times New Roman", Font.BOLD, 22));
		con.add(title);

		unam = new JLabel("User Name");
		pas = new JLabel("Password");
		t1 = new JTextField();
		pfpwd = new JPasswordField();
		login = new JButton("Login");
		show = new JCheckBox("Show Password");
		res = new JButton("Reset");
		reg1 = new JButton("Register!");

		login.addActionListener(this);
		show.addActionListener(this);
		res.addActionListener(this);
		reg1.addActionListener(this);

		unam.setBounds(50, 150, 100, 20);
		pas.setBounds(50, 220, 100, 20);
		t1.setBounds(150, 150, 200, 20);
		pfpwd.setBounds(150, 220, 200, 20);
		show.setBounds(150, 250, 150, 20);
		login.setBounds(50, 300, 150, 20);
		res.setBounds(200, 300, 150, 20);
		reg1.setBounds(125, 350, 150, 20);

		unam.setFont(f1);
		pas.setFont(f1);
		t1.setFont(f1);
		pfpwd.setFont(f1);
		login.setFont(f2);
		res.setFont(f2);
		show.setFont(f2);
		reg1.setFont(f2);

		unam.setForeground(Color.BLACK);
		pas.setForeground(Color.BLACK);

		con.add(unam);
		con.add(pas);
		con.add(t1);
		con.add(pfpwd);
		con.add(login);
		con.add(res);
		con.add(show);
		con.add(reg1);

		Clock();
	}

	private void Clock() {
		Thread clock = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Calendar cal = new GregorianCalendar();
						lblClock.setText("Time " + cal.get(Calendar.HOUR) + " : " + cal.get(Calendar.MINUTE) + " : "
								+ cal.get(Calendar.SECOND) + " Date " + cal.get(Calendar.YEAR) + " / "
								+ (cal.get(Calendar.MONTH) + 1) + " / " + cal.get(Calendar.DAY_OF_MONTH));
						sleep(1000);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		};
		clock.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reg1) {
			reg app = new reg();
			app.setTitle("Registration Form");
			app.setSize(1000, 750);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
			return;
		}

		if (e.getSource() == login) {
			loginUser();
			return;
		}

		if (e.getSource() == res) {
			t1.setText("");
			pfpwd.setText("");
			return;
		}

		if (e.getSource() == show) {
			pfpwd.setEchoChar(show.isSelected() ? (char) 0 : '*');
		}
	}

	private void loginUser() {
		if (conn == null) {
			JOptionPane.showMessageDialog(this, "Database connection is not available.");
			return;
		}

		String username = t1.getText().trim();
		String password = new String(pfpwd.getPassword());

		if (username.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter your username and password.");
			return;
		}

		try {
			String sql = "select Username from useraccount where Username=? and Password=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String loggedInUsername = rs.getString("Username");
				rs.close();
				ps.close();

				JOptionPane.showMessageDialog(this, "Login successful.");
				menu app = new menu(loggedInUsername);
				app.setTitle("User Menu");
				app.setSize(1000, 400);
				app.setVisible(true);
				app.setLocationRelativeTo(null);
				dispose();
			} else {
				rs.close();
				ps.close();
				JOptionPane.showMessageDialog(this, "Invalid username or password.");
				t1.setText("");
				pfpwd.setText("");
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Unable to log in: " + ex.getMessage());
		}
	}

	public static void main(String[] args) {
		login app = new login();
		app.setTitle("User Login");
		app.setSize(450, 600);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

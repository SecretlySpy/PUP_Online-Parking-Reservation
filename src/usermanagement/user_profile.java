package usermanagement;

import DatabaseConnection.ConnectionDB;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class user_profile extends JFrame implements ActionListener {
	private JTable table;
	private JButton back, logout;
	private String username;
	private Connection conn;

	public user_profile() {
		this(null);
	}

	public user_profile(String username) {
		this.username = username;
		conn = ConnectionDB.getConnection();

		Container con = getContentPane();
		con.setLayout(null);

		table = new JTable();
		table.setRowHeight(28);
		table.setFont(new Font("Arial", Font.PLAIN, 13));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(20, 80, 840, 360);
		con.add(scrollPane);

		back = new JButton("Back to Menu");
		back.setBounds(20, 20, 140, 35);
		back.addActionListener(this);
		con.add(back);

		logout = new JButton("Logout");
		logout.setBounds(720, 20, 140, 35);
		logout.addActionListener(this);
		con.add(logout);

		loadProfile();
	}

	private void loadProfile() {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("First Name");
		model.addColumn("Middle Name");
		model.addColumn("Last Name");
		model.addColumn("Email");
		model.addColumn("Gender");
		model.addColumn("Birthdate");
		model.addColumn("Occupation");
		model.addColumn("Address");
		model.addColumn("Mobile #");
		model.addColumn("Username");

		if (conn == null) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Database connection is not available.");
			return;
		}

		try {
			String sql = "select FirstName,MiddleName,LastName,Email,Gender,Birthdate,Occupation,Address,MobileNumber,Username "
					+ "from useraccount";
			if (username != null && !username.trim().isEmpty()) {
				sql += " where Username=?";
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			if (username != null && !username.trim().isEmpty()) {
				ps.setString(1, username);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("FirstName"), rs.getString("MiddleName"),
						rs.getString("LastName"), rs.getString("Email"), rs.getString("Gender"),
						rs.getString("Birthdate"), rs.getString("Occupation"), rs.getString("Address"),
						rs.getString("MobileNumber"), rs.getString("Username") });
			}
			rs.close();
			ps.close();
			table.setModel(model);
		} catch (Exception ex) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Unable to load profile: " + ex.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == back) {
			menu app = new menu(username);
			app.setTitle("User Menu");
			app.setSize(1000, 400);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
			return;
		}

		if (e.getSource() == logout) {
			login app = new login();
			app.setTitle("User Login");
			app.setSize(450, 600);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
		}
	}

	public static void main(String[] args) {
		user_profile app = new user_profile();
		app.setTitle("Customer Profile");
		app.setSize(900, 600);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

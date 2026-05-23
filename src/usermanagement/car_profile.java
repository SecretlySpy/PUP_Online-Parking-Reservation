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

public class car_profile extends JFrame implements ActionListener {
	private JTable table;
	private JButton back, logout;
	private String username;
	private Connection conn;

	public car_profile() {
		this(null);
	}

	public car_profile(String username) {
		this.username = username;
		conn = ConnectionDB.getConnection();

		Container con = getContentPane();
		con.setLayout(null);

		table = new JTable();
		table.setRowHeight(28);
		table.setFont(new Font("Arial", Font.PLAIN, 13));
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(20, 80, 740, 220);
		con.add(scrollPane);

		back = new JButton("Back to Menu");
		back.setBounds(20, 20, 140, 35);
		back.addActionListener(this);
		con.add(back);

		logout = new JButton("Logout");
		logout.setBounds(620, 20, 140, 35);
		logout.addActionListener(this);
		con.add(logout);

		loadCarProfile();
	}

	private void loadCarProfile() {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Username");
		model.addColumn("Plate #");
		model.addColumn("Brand");
		model.addColumn("Color");
		model.addColumn("Type");

		if (conn == null) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Database connection is not available.");
			return;
		}

		try {
			String sql = "select Username,PlateNumber,Brand,Color,Type from useraccount";
			if (username != null && !username.trim().isEmpty()) {
				sql += " where Username=?";
			}
			PreparedStatement ps = conn.prepareStatement(sql);
			if (username != null && !username.trim().isEmpty()) {
				ps.setString(1, username);
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("Username"), rs.getString("PlateNumber"),
						rs.getString("Brand"), rs.getString("Color"), rs.getString("Type") });
			}
			rs.close();
			ps.close();
			table.setModel(model);
		} catch (Exception ex) {
			table.setModel(model);
			JOptionPane.showMessageDialog(this, "Unable to load car profile: " + ex.getMessage());
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
		car_profile app = new car_profile();
		app.setTitle("Car Profile");
		app.setSize(800, 450);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

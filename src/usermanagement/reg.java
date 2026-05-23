package usermanagement;

import DatabaseConnection.ConnectionDB;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class reg extends JFrame implements ActionListener {
	private JTextField tfnam, tmnam, tlnam, tmno, temail, tunam;
	private JTextField tplatno, tbrand, tcolor, toccupation, tunit, tstreet, tdistrict, tcity;
	private JPasswordField tpass, trpass;
	private JRadioButton male, female;
	private ButtonGroup gengp;
	private JComboBox<String> date, month, year, carType;
	private JCheckBox term;
	private JButton sub, reset1, col;
	private JTextArea tout;
	private JLabel lblClock;
	private Connection conn;

	private final Font labelFont = new Font("Times New Roman", Font.PLAIN, 15);
	private final Font sectionFont = new Font("Times New Roman", Font.BOLD, 20);
	private final Font smallFont = new Font("Times New Roman", Font.PLAIN, 13);

	public reg() {
		conn = ConnectionDB.getConnection();

		Container con = getContentPane();
		con.setLayout(null);

		JLabel title = new JLabel("Archim's TechCorner");
		title.setBounds(40, 10, 220, 30);
		con.add(title);

		con.add(lblClock = new JLabel(""));
		lblClock.setBounds(720, 10, 260, 30);
		lblClock.setFont(smallFont);

		JLabel custdet = new JLabel("----------------------------------------------------CUSTOMER PROFILE--------------------------------------------------");
		custdet.setBounds(10, 55, 1200, 50);
		custdet.setFont(sectionFont);
		con.add(custdet);

		addLabel(con, "First Name:", 40, 110, 100, 30);
		tfnam = addTextField(con, 150, 110, 300, 30);

		addLabel(con, "Middle Name:", 40, 150, 100, 30);
		tmnam = addTextField(con, 150, 150, 300, 30);

		addLabel(con, "Last Name:", 40, 190, 100, 30);
		tlnam = addTextField(con, 150, 190, 300, 30);

		addLabel(con, "E-mail:", 40, 230, 100, 30);
		temail = addTextField(con, 150, 230, 300, 30);

		addLabel(con, "Password:", 40, 270, 100, 30);
		tpass = addPasswordField(con, 150, 270, 300, 30);

		addLabel(con, "Gender:", 500, 110, 100, 30);
		male = new JRadioButton("Male");
		male.setFont(labelFont);
		male.setSelected(true);
		male.setBounds(630, 110, 80, 30);
		male.setActionCommand("male");
		con.add(male);

		female = new JRadioButton("Female");
		female.setFont(labelFont);
		female.setBounds(750, 110, 90, 30);
		female.setActionCommand("female");
		con.add(female);

		gengp = new ButtonGroup();
		gengp.add(male);
		gengp.add(female);

		addLabel(con, "Birthdate:", 500, 150, 100, 30);
		month = new JComboBox<String>(new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
				"Oct", "Nov", "Dec" });
		month.setBounds(630, 150, 80, 30);
		con.add(month);

		date = new JComboBox<String>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25",
				"26", "27", "28", "29", "30", "31" });
		date.setBounds(730, 150, 80, 30);
		con.add(date);

		year = new JComboBox<String>(buildYears());
		year.setBounds(830, 150, 80, 30);
		con.add(year);

		addLabel(con, "Mobile #:", 500, 190, 100, 30);
		JLabel countryCode = new JLabel("+63");
		countryCode.setBounds(630, 190, 30, 30);
		con.add(countryCode);
		tmno = addTextField(con, 670, 190, 260, 30);

		addLabel(con, "Username:", 500, 230, 100, 30);
		tunam = addTextField(con, 630, 230, 300, 30);

		addLabel(con, "Repeat Password:", 500, 270, 120, 30);
		trpass = addPasswordField(con, 630, 270, 300, 30);

		JLabel addressSection = new JLabel("------------------------ADDRESS AND WORK INFORMATION------------------------");
		addressSection.setBounds(10, 315, 620, 40);
		addressSection.setFont(sectionFont);
		con.add(addressSection);

		addLabel(con, "Occupation:", 40, 360, 100, 30);
		toccupation = addTextField(con, 150, 360, 300, 30);

		addLabel(con, "Unit:", 40, 400, 100, 30);
		tunit = addTextField(con, 150, 400, 300, 30);

		addLabel(con, "Street:", 40, 440, 100, 30);
		tstreet = addTextField(con, 150, 440, 300, 30);

		addLabel(con, "District:", 40, 480, 100, 30);
		tdistrict = addTextField(con, 150, 480, 300, 30);

		addLabel(con, "City/Province:", 40, 520, 100, 30);
		tcity = addTextField(con, 150, 520, 300, 30);

		JLabel carprof = new JLabel("------------------------CAR PROFILE------------------------");
		carprof.setBounds(500, 315, 520, 40);
		carprof.setFont(sectionFont);
		con.add(carprof);

		addLabel(con, "PLATE #:", 540, 360, 100, 30);
		tplatno = addTextField(con, 650, 360, 300, 30);

		addLabel(con, "Brand:", 540, 400, 100, 30);
		tbrand = addTextField(con, 650, 400, 300, 30);

		addLabel(con, "Color:", 540, 440, 100, 30);
		tcolor = addTextField(con, 650, 440, 150, 30);

		col = new JButton("Choose Color");
		col.setBounds(820, 440, 130, 30);
		col.setFont(labelFont);
		col.addActionListener(this);
		con.add(col);

		addLabel(con, "Type of Car:", 540, 480, 100, 30);
		carType = new JComboBox<String>(new String[] { "Select -/-", "Hatchback", "Sedan", "MPV", "SUV", "Crossover",
				"Coupe", "Convertible", "Sports Car", "Utility", "Compact", "Mini Van", "Van", "Pick-Up Truck" });
		carType.setBounds(650, 480, 150, 30);
		con.add(carType);

		term = new JCheckBox("Accept Terms And Conditions.");
		term.setFont(labelFont);
		term.setBounds(150, 565, 300, 30);
		con.add(term);

		sub = new JButton("Submit");
		sub.setBounds(150, 610, 100, 25);
		sub.addActionListener(this);
		con.add(sub);

		reset1 = new JButton("Reset");
		reset1.setBounds(300, 610, 100, 25);
		reset1.addActionListener(this);
		con.add(reset1);

		tout = new JTextArea();
		tout.setBounds(650, 525, 300, 145);
		tout.setLineWrap(true);
		tout.setWrapStyleWord(true);
		tout.setEditable(false);
		con.add(tout);

		Clock();
	}

	private void addLabel(Container con, String text, int x, int y, int width, int height) {
		JLabel label = new JLabel(text);
		label.setBounds(x, y, width, height);
		label.setFont(labelFont);
		con.add(label);
	}

	private JTextField addTextField(Container con, int x, int y, int width, int height) {
		JTextField textField = new JTextField();
		textField.setBounds(x, y, width, height);
		textField.setFont(labelFont);
		con.add(textField);
		return textField;
	}

	private JPasswordField addPasswordField(Container con, int x, int y, int width, int height) {
		JPasswordField passwordField = new JPasswordField();
		passwordField.setBounds(x, y, width, height);
		passwordField.setFont(labelFont);
		con.add(passwordField);
		return passwordField;
	}

	private String[] buildYears() {
		String[] years = new String[91];
		for (int i = 0; i < years.length; i++) {
			years[i] = String.valueOf(1930 + i);
		}
		return years;
	}

	private void Clock() {
		Thread clock = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Calendar cal = new GregorianCalendar();
						int day = cal.get(Calendar.DAY_OF_MONTH);
						int currentMonth = cal.get(Calendar.MONTH) + 1;
						int currentYear = cal.get(Calendar.YEAR);
						int second = cal.get(Calendar.SECOND);
						int minute = cal.get(Calendar.MINUTE);
						int hour = cal.get(Calendar.HOUR);
						lblClock.setText("Time " + hour + " : " + minute + " : " + second + " Date " + currentYear
								+ " / " + currentMonth + " / " + day);
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
		if (e.getSource() == col) {
			Color selectedColor = JColorChooser.showDialog(this, "Choose Car Color", Color.WHITE);
			if (selectedColor != null) {
				tcolor.setText(selectedColor.getRed() + "," + selectedColor.getGreen() + "," + selectedColor.getBlue());
			}
			return;
		}

		if (e.getSource() == reset1) {
			clearForm();
			return;
		}

		if (e.getSource() == sub) {
			submitRegistration();
		}
	}

	private void submitRegistration() {
		String password = new String(tpass.getPassword());
		String repeatPassword = new String(trpass.getPassword());

		if (!term.isSelected()) {
			JOptionPane.showMessageDialog(this, "Please accept the terms and conditions.");
			return;
		}

		if (isBlank(tfnam) || isBlank(tmnam) || isBlank(tlnam) || isBlank(temail) || isBlank(tmno) || isBlank(tunam)
				|| password.trim().isEmpty() || repeatPassword.trim().isEmpty() || isBlank(toccupation) || isBlank(tunit)
				|| isBlank(tstreet) || isBlank(tdistrict) || isBlank(tcity) || isBlank(tplatno) || isBlank(tbrand)
				|| isBlank(tcolor) || carType.getSelectedIndex() == 0) {
			JOptionPane.showMessageDialog(this, "Please fill up all required fields.");
			return;
		}

		if (!password.equals(repeatPassword)) {
			JOptionPane.showMessageDialog(this, "Passwords do not match.");
			return;
		}

		if (conn == null) {
			JOptionPane.showMessageDialog(this, "Database connection is not available.");
			return;
		}

		String birthdate = month.getSelectedItem() + " " + date.getSelectedItem() + ", " + year.getSelectedItem();
		String address = buildAddress();

		try {
			String query = "insert into useraccount "
					+ "(FirstName,MiddleName,LastName,Email,Password,RepeatPassword,Gender,Birthdate,Occupation,Address,"
					+ "MobileNumber,Username,PlateNumber,Brand,Color,Type) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, tfnam.getText().trim());
			ps.setString(2, tmnam.getText().trim());
			ps.setString(3, tlnam.getText().trim());
			ps.setString(4, temail.getText().trim());
			ps.setString(5, password);
			ps.setString(6, repeatPassword);
			ps.setString(7, gengp.getSelection().getActionCommand());
			ps.setString(8, birthdate);
			ps.setString(9, toccupation.getText().trim());
			ps.setString(10, address);
			ps.setString(11, tmno.getText().trim());
			ps.setString(12, tunam.getText().trim());
			ps.setString(13, tplatno.getText().trim());
			ps.setString(14, tbrand.getText().trim());
			ps.setString(15, tcolor.getText().trim());
			ps.setString(16, (String) carType.getSelectedItem());
			ps.executeUpdate();
			ps.close();

			tout.setText("CUSTOMER INFORMATION\nName: " + tfnam.getText().trim() + " " + tmnam.getText().trim() + " "
					+ tlnam.getText().trim() + "\nOccupation: " + toccupation.getText().trim() + "\nAddress: "
					+ address + "\n\nCAR INFORMATION\nPlate #: " + tplatno.getText().trim() + "\nBrand: "
					+ tbrand.getText().trim() + "\nColor: " + tcolor.getText().trim() + "\nType: "
					+ carType.getSelectedItem());

			JOptionPane.showMessageDialog(this, "Registration successfully saved.");
			login app = new login();
			app.setTitle("User Login");
			app.setSize(450, 600);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			dispose();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Unable to save registration: " + ex.getMessage());
		}
	}

	private boolean isBlank(JTextField textField) {
		return textField.getText().trim().isEmpty();
	}

	private String buildAddress() {
		return tunit.getText().trim() + " " + tstreet.getText().trim() + " " + tdistrict.getText().trim()
				+ " District, " + tcity.getText().trim();
	}

	private void clearForm() {
		tfnam.setText("");
		tmnam.setText("");
		tlnam.setText("");
		tmno.setText("");
		temail.setText("");
		tunam.setText("");
		tpass.setText("");
		trpass.setText("");
		tplatno.setText("");
		tbrand.setText("");
		tcolor.setText("");
		toccupation.setText("");
		tunit.setText("");
		tstreet.setText("");
		tdistrict.setText("");
		tcity.setText("");
		tout.setText("");
		term.setSelected(false);
		male.setSelected(true);
		date.setSelectedIndex(0);
		month.setSelectedIndex(0);
		year.setSelectedIndex(0);
		carType.setSelectedIndex(0);
	}

	public static void main(String[] args) {
		reg app = new reg();
		app.setTitle("Registration Form");
		app.setSize(1000, 750);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

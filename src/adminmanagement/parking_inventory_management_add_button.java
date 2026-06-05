package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppOptions;
import app.AppTheme;
import com.toedter.calendar.JDateChooser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Data-entry screen for adding reservations to the inventory table.
 */
public class parking_inventory_management_add_button extends JFrame implements ActionListener {
	private JTextField idField;
	private JTextField fullNameField;
	private JTextField emailField;
	private JTextField mobileField;
	private JTextField plateField;
	private JTextField brandField;
	private JTextField colorField;
	private JTextField slotField;
	private JTextField hoursField;
	private JTextField timeParkField;
	private JTextField timeDepartureField;
	private JRadioButton maleButton;
	private JRadioButton femaleButton;
	private ButtonGroup genderGroup;
	private JComboBox<String> dayBox;
	private JComboBox<String> monthBox;
	private JComboBox<String> yearBox;
	private JComboBox<String> carTypeBox;
	private JDateChooser reservationDateChooser;
	private JDateChooser parkedDateChooser;
	private JButton colorButton;
	private JButton submitButton;
	private JButton resetButton;
	private JButton backButton;
	private JTextArea summaryArea;
	private Connection conn;

	public parking_inventory_management_add_button() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Add Parking Reservation", "Capture customer, vehicle, and slot details.",
				buildContent()));
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(18, 0));
		content.add(buildForm(), BorderLayout.CENTER);
		content.add(buildSummary(), BorderLayout.EAST);
		return content;
	}

	private JPanel buildForm() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		int row = 0;
		form.add(AppTheme.sectionLabel("Customer Details"), wideConstraints(row++));
		idField = addField(form, "Reservation ID", row++);
		fullNameField = addField(form, "Full Name", row++);
		emailField = addField(form, "Email", row++);
		mobileField = addField(form, "Mobile Number", row++);
		addGenderRow(form, row++);
		addBirthdateRow(form, row++);

		form.add(AppTheme.sectionLabel("Car Details"), wideConstraints(row++));
		plateField = addField(form, "Plate Number", row++);
		brandField = addField(form, "Brand", row++);
		addColorRow(form, row++);
		addCarTypeRow(form, row++);

		form.add(AppTheme.sectionLabel("Parking Details"), wideConstraints(row++));
		addDateChooserRow(form, "Reservation Date", true, row++);
		addDateChooserRow(form, "Parked Date", false, row++);
		slotField = addField(form, "Slot Number", row++);
		hoursField = addField(form, "Total Hours", row++);
		timeParkField = addField(form, "Time of Park", row++);
		timeDepartureField = addField(form, "Time of Departure", row++);
		addActions(form, row);

		return form;
	}

	private JTextField addField(JPanel form, String labelText, int row) {
		JLabel label = AppTheme.label(labelText);
		JTextField field = AppTheme.textField(labelText);
		label.setLabelFor(field);
		form.add(label, labelConstraints(row));
		form.add(field, fieldConstraints(row));
		return field;
	}

	private void addGenderRow(JPanel form, int row) {
		form.add(AppTheme.label("Gender"), labelConstraints(row));

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		maleButton = new JRadioButton("Male");
		femaleButton = new JRadioButton("Female");
		maleButton.setOpaque(false);
		femaleButton.setOpaque(false);
		maleButton.setSelected(true);
		maleButton.setActionCommand("male");
		femaleButton.setActionCommand("female");

		genderGroup = new ButtonGroup();
		genderGroup.add(maleButton);
		genderGroup.add(femaleButton);

		panel.add(maleButton);
		panel.add(femaleButton);
		form.add(panel, fieldConstraints(row));
	}

	private void addBirthdateRow(JPanel form, int row) {
		form.add(AppTheme.label("Birthdate"), labelConstraints(row));

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		monthBox = new JComboBox<String>(new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
				"Sep", "Oct", "Nov", "Dec" });
		dayBox = new JComboBox<String>(buildDays());
		yearBox = new JComboBox<String>(AppOptions.birthYearsThroughCurrentYear());

		panel.add(monthBox, AppTheme.constraints(0, 0));
		panel.add(dayBox, AppTheme.constraints(1, 0));
		panel.add(yearBox, AppTheme.constraints(2, 0));
		form.add(panel, fieldConstraints(row));
	}

	private void addColorRow(JPanel form, int row) {
		form.add(AppTheme.label("Color"), labelConstraints(row));

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		colorField = AppTheme.textField("Vehicle color");
		colorButton = AppTheme.secondaryButton("Choose");
		colorButton.addActionListener(this);
		panel.add(colorField, AppTheme.constraints(0, 0));
		panel.add(colorButton, AppTheme.constraints(1, 0));
		form.add(panel, fieldConstraints(row));
	}

	private void addCarTypeRow(JPanel form, int row) {
		form.add(AppTheme.label("Type of Car"), labelConstraints(row));
		carTypeBox = new JComboBox<String>(AppOptions.carTypes());
		form.add(carTypeBox, fieldConstraints(row));
	}

	private void addDateChooserRow(JPanel form, String labelText, boolean reservationDate, int row) {
		form.add(AppTheme.label(labelText), labelConstraints(row));
		JDateChooser chooser = new JDateChooser();
		chooser.setDate(new Date());
		if (reservationDate) {
			reservationDateChooser = chooser;
		} else {
			parkedDateChooser = chooser;
		}
		form.add(chooser, fieldConstraints(row));
	}

	private void addActions(JPanel form, int row) {
		JPanel actions = new JPanel();
		actions.setOpaque(false);

		submitButton = AppTheme.primaryButton("Submit");
		resetButton = AppTheme.secondaryButton("Reset");
		backButton = AppTheme.secondaryButton("Back to Inventory");

		submitButton.addActionListener(this);
		resetButton.addActionListener(this);
		backButton.addActionListener(this);

		actions.add(submitButton);
		actions.add(resetButton);
		actions.add(backButton);
		form.add(actions, wideConstraints(row));
	}

	private JTextArea buildSummary() {
		summaryArea = new JTextArea(24, 28);
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		summaryArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
		summaryArea.setText("Reservation summary will appear here after submission.");
		return summaryArea;
	}

	private GridBagConstraints labelConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(0, row);
		constraints.weightx = 0;
		return constraints;
	}

	private GridBagConstraints fieldConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(1, row);
		constraints.weightx = 1;
		return constraints;
	}

	private GridBagConstraints wideConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(0, row);
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		return constraints;
	}

	private String[] buildDays() {
		String[] days = new String[31];
		for (int index = 0; index < days.length; index++) {
			days[index] = String.valueOf(index + 1);
		}
		return days;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == submitButton) {
			submitReservation();
		} else if (source == resetButton) {
			clearForm();
		} else if (source == backButton) {
			openInventory();
		} else if (source == colorButton) {
			chooseColor();
		}
	}

	private void chooseColor() {
		Color selectedColor = JColorChooser.showDialog(this, "Choose Car Color", Color.WHITE);
		if (selectedColor != null) {
			colorField.setText(selectedColor.getRed() + "," + selectedColor.getGreen() + "," + selectedColor.getBlue());
		}
	}

	private void submitReservation() {
		if (!isFormValid()) {
			return;
		}
		if (conn == null) {
			AppTheme.showError(this, "Database connection is not available.", null);
			return;
		}

		String birthdate = monthBox.getSelectedItem() + " " + dayBox.getSelectedItem() + ", " + yearBox.getSelectedItem();
		String reservationDate = formatDate(reservationDateChooser.getDate());
		String parkedDate = formatDate(parkedDateChooser.getDate());
		String sql = "insert into inventory "
				+ "(ID,FullName,Email,Gender,MobileNumber,Birthdate,PlateNumber,Brand,Color,Type,DOR,DOP,SlotNumber,THP,TP,TD) "
				+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, idField.getText().trim());
			statement.setString(2, fullNameField.getText().trim());
			statement.setString(3, emailField.getText().trim());
			statement.setString(4, genderGroup.getSelection().getActionCommand());
			statement.setString(5, mobileField.getText().trim());
			statement.setString(6, birthdate);
			statement.setString(7, plateField.getText().trim());
			statement.setString(8, brandField.getText().trim());
			statement.setString(9, colorField.getText().trim());
			statement.setString(10, (String) carTypeBox.getSelectedItem());
			statement.setString(11, reservationDate);
			statement.setString(12, parkedDate);
			statement.setString(13, slotField.getText().trim());
			statement.setString(14, hoursField.getText().trim());
			statement.setString(15, timeParkField.getText().trim());
			statement.setString(16, timeDepartureField.getText().trim());
			statement.executeUpdate();

			summaryArea.setText("Reservation saved:\nID: " + idField.getText().trim() + "\nCustomer: "
					+ fullNameField.getText().trim() + "\nPlate: " + plateField.getText().trim() + "\nSlot: "
					+ slotField.getText().trim() + "\nReservation Date: " + reservationDate);
			openInventory();
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to save parking reservation.", error);
		}
	}

	private boolean isFormValid() {
		boolean blankText = isBlank(idField) || isBlank(fullNameField) || isBlank(emailField) || isBlank(mobileField)
				|| isBlank(plateField) || isBlank(brandField) || isBlank(colorField) || isBlank(slotField)
				|| isBlank(hoursField) || isBlank(timeParkField) || isBlank(timeDepartureField);
		if (blankText || carTypeBox.getSelectedIndex() == 0 || reservationDateChooser.getDate() == null
				|| parkedDateChooser.getDate() == null) {
			summaryArea.setText("Please fill in every reservation field before submitting.");
			return false;
		}
		return true;
	}

	private boolean isBlank(JTextField field) {
		return field.getText().trim().isEmpty();
	}

	private String formatDate(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	private void clearForm() {
		idField.setText("");
		fullNameField.setText("");
		emailField.setText("");
		mobileField.setText("");
		plateField.setText("");
		brandField.setText("");
		colorField.setText("");
		slotField.setText("");
		hoursField.setText("");
		timeParkField.setText("");
		timeDepartureField.setText("");
		maleButton.setSelected(true);
		dayBox.setSelectedIndex(0);
		monthBox.setSelectedIndex(0);
		yearBox.setSelectedIndex(0);
		carTypeBox.setSelectedIndex(0);
		reservationDateChooser.setDate(new Date());
		parkedDateChooser.setDate(new Date());
		summaryArea.setText("Reservation summary will appear here after submission.");
	}

	private void openInventory() {
		park_inventory_management app = new park_inventory_management();
		AppTheme.showFrame(app, "Parking Inventory Management", 1120, 720);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			parking_inventory_management_add_button app = new parking_inventory_management_add_button();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Add Parking Reservation", 1180, 760);
		});
	}
}

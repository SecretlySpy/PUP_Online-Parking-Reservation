package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppOptions;
import app.AppTheme;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Registration screen for administrator accounts.
 */
public class reg extends JFrame implements ActionListener {
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JTextField lastNameField;
	private JTextField mobileField;
	private JTextField emailField;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JPasswordField repeatPasswordField;
	private JRadioButton maleButton;
	private JRadioButton femaleButton;
	private ButtonGroup genderGroup;
	private JComboBox<String> dayBox;
	private JComboBox<String> monthBox;
	private JComboBox<String> yearBox;
	private JCheckBox termsBox;
	private JButton submitButton;
	private JButton resetButton;
	private JButton backButton;
	private JTextArea summaryArea;
	private Connection conn;

	public reg() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Admin Registration", "Create a new administrator account.", buildContent()));
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

		firstNameField = addField(form, "First Name", 0);
		middleNameField = addField(form, "Middle Name", 1);
		lastNameField = addField(form, "Last Name", 2);
		emailField = addField(form, "Email", 3);
		mobileField = addField(form, "Mobile Number", 4);
		usernameField = addField(form, "Username", 5);
		passwordField = addPasswordField(form, "Password", 6);
		repeatPasswordField = addPasswordField(form, "Repeat Password", 7);
		addGenderRow(form, 8);
		addBirthdateRow(form, 9);
		addTermsRow(form, 10);
		addActions(form, 11);

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

	private JPasswordField addPasswordField(JPanel form, String labelText, int row) {
		JLabel label = AppTheme.label(labelText);
		JPasswordField field = AppTheme.passwordField(labelText);
		label.setLabelFor(field);
		form.add(label, labelConstraints(row));
		form.add(field, fieldConstraints(row));
		return field;
	}

	private void addGenderRow(JPanel form, int row) {
		form.add(AppTheme.label("Gender"), labelConstraints(row));

		JPanel genderPanel = new JPanel();
		genderPanel.setOpaque(false);
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

		genderPanel.add(maleButton);
		genderPanel.add(femaleButton);
		form.add(genderPanel, fieldConstraints(row));
	}

	private void addBirthdateRow(JPanel form, int row) {
		form.add(AppTheme.label("Birthdate"), labelConstraints(row));

		JPanel birthdatePanel = new JPanel(new GridBagLayout());
		birthdatePanel.setOpaque(false);
		monthBox = new JComboBox<String>(new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
				"Sep", "Oct", "Nov", "Dec" });
		dayBox = new JComboBox<String>(buildDays());
		yearBox = new JComboBox<String>(AppOptions.birthYearsThroughCurrentYear());

		birthdatePanel.add(monthBox, AppTheme.constraints(0, 0));
		birthdatePanel.add(dayBox, AppTheme.constraints(1, 0));
		birthdatePanel.add(yearBox, AppTheme.constraints(2, 0));
		form.add(birthdatePanel, fieldConstraints(row));
	}

	private void addTermsRow(JPanel form, int row) {
		termsBox = new JCheckBox("Accept Terms and Conditions");
		termsBox.setOpaque(false);

		GridBagConstraints constraints = fieldConstraints(row);
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		form.add(termsBox, constraints);
	}

	private void addActions(JPanel form, int row) {
		JPanel actions = new JPanel();
		actions.setOpaque(false);

		submitButton = AppTheme.primaryButton("Submit");
		resetButton = AppTheme.secondaryButton("Reset");
		backButton = AppTheme.secondaryButton("Back to Login");

		submitButton.addActionListener(this);
		resetButton.addActionListener(this);
		backButton.addActionListener(this);

		actions.add(submitButton);
		actions.add(resetButton);
		actions.add(backButton);

		GridBagConstraints constraints = fieldConstraints(row);
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		form.add(actions, constraints);
	}

	private JTextArea buildSummary() {
		summaryArea = new JTextArea(16, 26);
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		summaryArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
		summaryArea.setText("Registration summary will appear here after validation.");
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
			submitRegistration();
		} else if (source == resetButton) {
			clearForm();
		} else if (source == backButton) {
			openLogin();
		}
	}

	private void submitRegistration() {
		String password = new String(passwordField.getPassword());
		String repeatPassword = new String(repeatPasswordField.getPassword());

		if (!termsBox.isSelected()) {
			summaryArea.setText("Please accept the terms and conditions.");
			return;
		}
		if (isBlank(firstNameField) || isBlank(middleNameField) || isBlank(lastNameField) || isBlank(emailField)
				|| isBlank(mobileField) || isBlank(usernameField) || password.trim().isEmpty()
				|| repeatPassword.trim().isEmpty()) {
			summaryArea.setText("Please fill in every admin registration field.");
			return;
		}
		if (!password.equals(repeatPassword)) {
			summaryArea.setText("Passwords do not match.");
			return;
		}
		if (conn == null) {
			AppTheme.showError(this, "Database connection is not available.", null);
			return;
		}

		String birthdate = monthBox.getSelectedItem() + " " + dayBox.getSelectedItem() + ", "
				+ yearBox.getSelectedItem();
		String sql = "insert into adminaccount "
				+ "(FirstName,MiddleName,LastName,Email,Gender,Birthdate,MobileNumber,Username,Password,RepeatPassword) "
				+ "values (?,?,?,?,?,?,?,?,?,?)";

		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			statement.setString(1, firstNameField.getText().trim());
			statement.setString(2, middleNameField.getText().trim());
			statement.setString(3, lastNameField.getText().trim());
			statement.setString(4, emailField.getText().trim());
			statement.setString(5, genderGroup.getSelection().getActionCommand());
			statement.setString(6, birthdate);
			statement.setString(7, mobileField.getText().trim());
			statement.setString(8, usernameField.getText().trim());
			statement.setString(9, password);
			statement.setString(10, repeatPassword);
			statement.executeUpdate();

			summaryArea.setText("Admin saved:\n" + firstNameField.getText().trim() + " "
					+ lastNameField.getText().trim() + "\n" + emailField.getText().trim() + "\n"
					+ usernameField.getText().trim());
			openLogin();
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to save admin registration.", error);
		}
	}

	private boolean isBlank(JTextField field) {
		return field.getText().trim().isEmpty();
	}

	private void clearForm() {
		firstNameField.setText("");
		middleNameField.setText("");
		lastNameField.setText("");
		emailField.setText("");
		mobileField.setText("");
		usernameField.setText("");
		passwordField.setText("");
		repeatPasswordField.setText("");
		termsBox.setSelected(false);
		maleButton.setSelected(true);
		dayBox.setSelectedIndex(0);
		monthBox.setSelectedIndex(0);
		yearBox.setSelectedIndex(0);
		summaryArea.setText("Registration summary will appear here after validation.");
	}

	private void openLogin() {
		login app = new login();
		AppTheme.showFrame(app, "Admin Login", 540, 640);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			reg app = new reg();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Admin Registration Form", 1100, 560);
		});
	}
}

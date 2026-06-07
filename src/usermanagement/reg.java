package usermanagement;

import DatabaseConnection.ConnectionDB;
import app.AppOptions;
import app.AppTheme;
import app.FormValidator;
import app.PasswordSecurity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Customer sign-up screen for creating a profile, address, and vehicle record.
 */
public class reg extends JFrame implements ActionListener {
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JTextField lastNameField;
	private JTextField mobileField;
	private JTextField emailField;
	private JTextField usernameField;
	private JTextField plateField;
	private JTextField brandField;
	private JTextField colorField;
	private JTextField occupationField;
	private JTextField unitField;
	private JTextField streetField;
	private JTextField districtField;
	private JTextField cityField;
	private JPasswordField passwordField;
	private JPasswordField repeatPasswordField;
	private JRadioButton maleButton;
	private JRadioButton femaleButton;
	private ButtonGroup genderGroup;
	private JComboBox<String> dayBox;
	private JComboBox<String> monthBox;
	private JComboBox<String> yearBox;
	private JComboBox<String> carTypeBox;
	private JCheckBox termsBox;
	private JButton submitButton;
	private JButton resetButton;
	private JButton colorButton;
	private JButton backButton;
	private JTextArea summaryArea;
	private JLabel statusLabel;
	private JLabel colorPreview;
	private JPanel sectionsPanel;
	private JPanel personalSection;
	private JPanel addressSection;
	private JPanel carSection;
	private JPanel summarySection;
	private boolean compactLayout;
	private Connection conn;

	public reg() {
		AppTheme.install();
		conn = ConnectionDB.getConnection();
		setContentPane(AppTheme.shell("Create Customer Account",
				"Set up your customer profile, address details, and vehicle information.", buildContent()));
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 18));
		content.add(buildHeaderRow(), BorderLayout.NORTH);
		content.add(buildFormScrollPane(), BorderLayout.CENTER);
		content.add(buildActions(), BorderLayout.SOUTH);
		content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				updateSectionLayout(content.getWidth() < 900);
			}
		});
		return content;
	}

	private JPanel buildHeaderRow() {
		JPanel header = new JPanel(new BorderLayout(12, 0));
		header.setOpaque(false);
		header.add(AppTheme.sectionLabel("Sign-Up Details"), BorderLayout.WEST);
		header.add(AppTheme.clockLabel(), BorderLayout.EAST);
		return header;
	}

	private JScrollPane buildFormScrollPane() {
		sectionsPanel = new JPanel(new GridBagLayout());
		sectionsPanel.setOpaque(false);

		personalSection = buildPersonalSection();
		addressSection = buildAddressSection();
		carSection = buildCarSection();
		summarySection = buildSummarySection();
		updateSectionLayout(false);

		JScrollPane scrollPane = new JScrollPane(sectionsPanel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}

	private JPanel buildPersonalSection() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		firstNameField = addTextField(form, "First Name", 0);
		middleNameField = addTextField(form, "Middle Name", 1);
		lastNameField = addTextField(form, "Last Name", 2);
		emailField = addTextField(form, "Email", 3);
		mobileField = addMobileRow(form, 4);
		usernameField = addTextField(form, "Username", 5);
		passwordField = addPasswordField(form, "Password", 6);
		repeatPasswordField = addPasswordField(form, "Repeat Password", 7);
		addGenderRow(form, 8);
		addBirthdateRow(form, 9);

		return sectionPanel("Customer Profile", form);
	}

	private JPanel buildAddressSection() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		occupationField = addTextField(form, "Occupation", 0);
		unitField = addTextField(form, "Unit", 1);
		streetField = addTextField(form, "Street", 2);
		districtField = addTextField(form, "District", 3);
		cityField = addTextField(form, "City/Province", 4);

		return sectionPanel("Address and Work Information", form);
	}

	private JPanel buildCarSection() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		plateField = addTextField(form, "Plate Number", 0);
		brandField = addTextField(form, "Brand", 1);
		addColorRow(form, 2);
		addCarTypeRow(form, 3);

		return sectionPanel("Car Profile", form);
	}

	private JPanel buildSummarySection() {
		summaryArea = new JTextArea(5, 24);
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		summaryArea.setFont(AppTheme.BODY_FONT);
		summaryArea.setForeground(AppTheme.MUTED_TEXT);
		summaryArea.setBackground(AppTheme.SURFACE_ALT);
		summaryArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.BORDER),
				BorderFactory.createEmptyBorder(12, 12, 12, 12)));
		summaryArea.setText("Registration summary will appear here after the form is submitted.");

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);
		wrapper.add(summaryArea, BorderLayout.CENTER);
		return sectionPanel("Registration Summary", wrapper);
	}

	private JPanel sectionPanel(String title, JPanel body) {
		JPanel section = new JPanel(new BorderLayout(0, 12));
		section.setOpaque(false);
		section.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
				AppTheme.BORDER), BorderFactory.createEmptyBorder(14, 0, 4, 0)));
		section.add(AppTheme.sectionLabel(title), BorderLayout.NORTH);
		section.add(body, BorderLayout.CENTER);
		return section;
	}

	private JTextField addTextField(JPanel form, String labelText, int row) {
		JTextField field = AppTheme.textField(labelText);
		field.setColumns(24);
		addRow(form, labelText, field, row);
		return field;
	}

	private JPasswordField addPasswordField(JPanel form, String labelText, int row) {
		JPasswordField field = AppTheme.passwordField(labelText);
		field.setColumns(24);
		addRow(form, labelText, field, row);
		return field;
	}

	private void addRow(JPanel form, String labelText, JTextField field, int row) {
		JLabel label = AppTheme.label(labelText);
		label.setLabelFor(field);
		form.add(label, labelConstraints(row));
		form.add(field, fieldConstraints(row));
	}

	private JTextField addMobileRow(JPanel form, int row) {
		JTextField field = AppTheme.textField("Mobile Number");
		field.setColumns(18);

		JPanel mobilePanel = new JPanel(new BorderLayout(8, 0));
		mobilePanel.setOpaque(false);
		JLabel prefixLabel = AppTheme.label("+63");
		prefixLabel.setForeground(AppTheme.MUTED_TEXT);
		mobilePanel.add(prefixLabel, BorderLayout.WEST);
		mobilePanel.add(field, BorderLayout.CENTER);

		addComponentRow(form, "Mobile Number", mobilePanel, row);
		return field;
	}

	private void addGenderRow(JPanel form, int row) {
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

		JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		genderPanel.setOpaque(false);
		genderPanel.add(maleButton);
		genderPanel.add(femaleButton);
		addComponentRow(form, "Gender", genderPanel, row);
	}

	private void addBirthdateRow(JPanel form, int row) {
		monthBox = comboBox("Birth Month",
				new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
						"Dec" });
		dayBox = comboBox("Birth Day", buildDays());
		yearBox = comboBox("Birth Year", AppOptions.birthYearsThroughCurrentYear());

		JPanel birthdatePanel = new JPanel(new GridBagLayout());
		birthdatePanel.setOpaque(false);
		birthdatePanel.add(monthBox, comboConstraints(0));
		birthdatePanel.add(dayBox, comboConstraints(1));
		birthdatePanel.add(yearBox, comboConstraints(2));
		addComponentRow(form, "Birthdate", birthdatePanel, row);
	}

	private void addColorRow(JPanel form, int row) {
		colorField = AppTheme.textField("Vehicle Color RGB");
		colorField.setColumns(12);
		colorButton = AppTheme.secondaryButton("Choose Color");
		colorButton.addActionListener(this);

		colorPreview = new JLabel(" ");
		colorPreview.setOpaque(true);
		colorPreview.setBackground(Color.WHITE);
		colorPreview.setPreferredSize(new Dimension(34, 34));
		colorPreview.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));

		JPanel colorPanel = new JPanel(new GridBagLayout());
		colorPanel.setOpaque(false);

		GridBagConstraints fieldConstraints = comboConstraints(0);
		fieldConstraints.weightx = 1;
		colorPanel.add(colorField, fieldConstraints);

		GridBagConstraints buttonConstraints = comboConstraints(1);
		buttonConstraints.weightx = 0;
		colorPanel.add(colorButton, buttonConstraints);

		GridBagConstraints previewConstraints = comboConstraints(2);
		previewConstraints.weightx = 0;
		colorPanel.add(colorPreview, previewConstraints);

		addComponentRow(form, "Color", colorPanel, row);
	}

	private void addCarTypeRow(JPanel form, int row) {
		carTypeBox = comboBox("Type of Car", AppOptions.carTypes());
		addComponentRow(form, "Type of Car", carTypeBox, row);
	}

	private void addComponentRow(JPanel form, String labelText, java.awt.Component component, int row) {
		JLabel label = AppTheme.label(labelText);
		label.setLabelFor(component);
		form.add(label, labelConstraints(row));
		form.add(component, fieldConstraints(row));
	}

	private JComboBox<String> comboBox(String accessibleName, String[] values) {
		JComboBox<String> comboBox = new JComboBox<String>(values);
		comboBox.setFont(AppTheme.BODY_FONT);
		comboBox.setForeground(AppTheme.TEXT);
		comboBox.setBackground(Color.WHITE);
		comboBox.getAccessibleContext().setAccessibleName(accessibleName);
		return comboBox;
	}

	private JPanel buildActions() {
		JPanel footer = new JPanel(new BorderLayout(12, 0));
		footer.setOpaque(false);
		footer.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

		JPanel statusPanel = new JPanel(new GridBagLayout());
		statusPanel.setOpaque(false);

		termsBox = new JCheckBox("Accept Terms and Conditions");
		termsBox.setOpaque(false);
		statusPanel.add(termsBox, footerConstraints(0));

		statusLabel = AppTheme.label("Complete all required fields to create your customer account.");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		statusPanel.add(statusLabel, footerConstraints(1));
		footer.add(statusPanel, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		actions.setOpaque(false);

		backButton = AppTheme.secondaryButton("Back to Login");
		resetButton = AppTheme.secondaryButton("Reset");
		submitButton = AppTheme.primaryButton("Submit");

		backButton.addActionListener(this);
		resetButton.addActionListener(this);
		submitButton.addActionListener(this);

		actions.add(backButton);
		actions.add(resetButton);
		actions.add(submitButton);
		footer.add(actions, BorderLayout.EAST);
		return footer;
	}

	private GridBagConstraints footerConstraints(int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(row == 0 ? 0 : 4, 0, 0, 0);
		return constraints;
	}

	private void updateSectionLayout(boolean compact) {
		if (sectionsPanel.getComponentCount() > 0 && compactLayout == compact) {
			return;
		}

		compactLayout = compact;
		sectionsPanel.removeAll();
		sectionsPanel.add(personalSection, sectionConstraints(0, 0, compact ? 1 : 2, false));

		if (compact) {
			sectionsPanel.add(addressSection, sectionConstraints(0, 1, 1, false));
			sectionsPanel.add(carSection, sectionConstraints(0, 2, 1, false));
			sectionsPanel.add(summarySection, sectionConstraints(0, 3, 1, false));
		} else {
			sectionsPanel.add(addressSection, sectionConstraints(0, 1, 1, true));
			sectionsPanel.add(carSection, sectionConstraints(1, 1, 1, false));
			sectionsPanel.add(summarySection, sectionConstraints(0, 2, 2, false));
		}

		sectionsPanel.revalidate();
		sectionsPanel.repaint();
	}

	private GridBagConstraints labelConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(0, row);
		constraints.anchor = GridBagConstraints.EAST;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.insets = new Insets(6, 0, 6, 12);
		return constraints;
	}

	private GridBagConstraints fieldConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(1, row);
		constraints.weightx = 1;
		constraints.insets = new Insets(6, 0, 6, 0);
		return constraints;
	}

	private GridBagConstraints comboConstraints(int column) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, column == 0 ? 0 : 8, 0, 0);
		return constraints;
	}

	private GridBagConstraints sectionConstraints(int column, int row, int gridWidth, boolean rightGap) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.gridwidth = gridWidth;
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(0, 0, 18, rightGap ? 18 : 0);
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
		if (source == colorButton) {
			chooseColor();
		} else if (source == resetButton) {
			clearForm();
		} else if (source == submitButton) {
			submitRegistration();
		} else if (source == backButton) {
			openLogin();
		}
	}

	private void chooseColor() {
		Color selectedColor = JColorChooser.showDialog(this, "Choose Car Color", colorPreview.getBackground());
		if (selectedColor != null) {
			colorField.setText(selectedColor.getRed() + "," + selectedColor.getGreen() + ","
					+ selectedColor.getBlue());
			colorPreview.setBackground(selectedColor);
		}
	}

	private void submitRegistration() {
		char[] password = passwordField.getPassword();
		char[] repeatPassword = repeatPasswordField.getPassword();
		try {
			String validationError = validateForm(password, repeatPassword);

			if (validationError != null) {
				showStatus(validationError);
				return;
			}

			if (conn == null) {
				AppTheme.showError(this,
						"Database connection is not available. Check the MySQL container or local service.", null);
				return;
			}

			String birthdate = monthBox.getSelectedItem() + " " + dayBox.getSelectedItem() + ", "
					+ yearBox.getSelectedItem();
			String address = buildAddress();
			String passwordHash = PasswordSecurity.hash(password);
			String sql = "insert into useraccount "
					+ "(FirstName,MiddleName,LastName,Email,Password,RepeatPassword,Gender,Birthdate,Occupation,Address,"
					+ "MobileNumber,Username,PlateNumber,Brand,Color,Type) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setString(1, firstNameField.getText().trim());
				statement.setString(2, middleNameField.getText().trim());
				statement.setString(3, lastNameField.getText().trim());
				statement.setString(4, emailField.getText().trim());
				statement.setString(5, passwordHash);
				statement.setString(6, passwordHash);
				statement.setString(7, genderGroup.getSelection().getActionCommand());
				statement.setString(8, birthdate);
				statement.setString(9, occupationField.getText().trim());
				statement.setString(10, address);
				statement.setString(11, mobileField.getText().trim());
				statement.setString(12, usernameField.getText().trim());
				statement.setString(13, plateField.getText().trim());
				statement.setString(14, brandField.getText().trim());
				statement.setString(15, colorField.getText().trim());
				statement.setString(16, (String) carTypeBox.getSelectedItem());
				statement.executeUpdate();

				summaryArea.setForeground(AppTheme.TEXT);
				summaryArea.setText("Customer saved:\n" + firstNameField.getText().trim() + " "
						+ middleNameField.getText().trim() + " " + lastNameField.getText().trim() + "\n"
						+ emailField.getText().trim() + "\n" + address + "\n\nVehicle:\n"
						+ plateField.getText().trim() + " - " + brandField.getText().trim() + "\n"
						+ carTypeBox.getSelectedItem());
				JOptionPane.showMessageDialog(this, "Registration successfully saved.");
				openLogin();
			} catch (Exception error) {
				AppTheme.showError(this, "Unable to save registration.", error);
			}
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to secure the registration password.", error);
		} finally {
			PasswordSecurity.clear(password);
			PasswordSecurity.clear(repeatPassword);
		}
	}

	private String validateForm(char[] password, char[] repeatPassword) {
		String error = FormValidator.firstError(FormValidator.required(firstNameField.getText(), "First name"),
				FormValidator.required(middleNameField.getText(), "Middle name"),
				FormValidator.required(lastNameField.getText(), "Last name"),
				FormValidator.required(emailField.getText(), "Email"), FormValidator.email(emailField.getText()),
				FormValidator.required(mobileField.getText(), "Mobile number"),
				FormValidator.required(usernameField.getText(), "Username"),
				password.length == 0 ? "Password is required." : null,
				repeatPassword.length == 0 ? "Repeat password is required." : null,
				FormValidator.required(occupationField.getText(), "Occupation"),
				FormValidator.required(unitField.getText(), "Unit"), FormValidator.required(streetField.getText(),
						"Street"),
				FormValidator.required(districtField.getText(), "District"),
				FormValidator.required(cityField.getText(), "City/Province"),
				FormValidator.required(plateField.getText(), "Plate number"),
				FormValidator.required(brandField.getText(), "Brand"),
				FormValidator.required(colorField.getText(), "Color"));

		if (error != null) {
			return error;
		}
		String strengthError = PasswordSecurity.strengthError(password);
		if (strengthError != null) {
			return strengthError;
		}
		if (!PasswordSecurity.matches(password, repeatPassword)) {
			return "Passwords do not match.";
		}
		if (carTypeBox.getSelectedIndex() == 0) {
			return "Choose a type of car.";
		}
		if (!termsBox.isSelected()) {
			return "Accept the terms and conditions to continue.";
		}
		return null;
	}

	private String buildAddress() {
		return unitField.getText().trim() + " " + streetField.getText().trim() + " "
				+ districtField.getText().trim() + " District, " + cityField.getText().trim();
	}

	private void showStatus(String message) {
		statusLabel.setText(message);
		summaryArea.setForeground(AppTheme.DANGER);
		summaryArea.setText(message);
	}

	private void clearForm() {
		firstNameField.setText("");
		middleNameField.setText("");
		lastNameField.setText("");
		mobileField.setText("");
		emailField.setText("");
		usernameField.setText("");
		passwordField.setText("");
		repeatPasswordField.setText("");
		plateField.setText("");
		brandField.setText("");
		colorField.setText("");
		occupationField.setText("");
		unitField.setText("");
		streetField.setText("");
		districtField.setText("");
		cityField.setText("");
		termsBox.setSelected(false);
		maleButton.setSelected(true);
		dayBox.setSelectedIndex(0);
		monthBox.setSelectedIndex(0);
		yearBox.setSelectedIndex(0);
		carTypeBox.setSelectedIndex(0);
		colorPreview.setBackground(Color.WHITE);
		statusLabel.setText("Complete all required fields to create your customer account.");
		summaryArea.setForeground(AppTheme.MUTED_TEXT);
		summaryArea.setText("Registration summary will appear here after the form is submitted.");
		firstNameField.requestFocusInWindow();
	}

	private void openLogin() {
		login app = new login();
		AppTheme.showFrame(app, "User Login", 520, 620);
		dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			reg app = new reg();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Registration Form", 1000, 750);
		});
	}
}

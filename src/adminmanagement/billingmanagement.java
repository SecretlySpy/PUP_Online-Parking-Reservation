package adminmanagement;

import app.AppTheme;
import com.toedter.calendar.JDateChooser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Billing calculator for parking reservations.
 */
public class billingmanagement extends JFrame implements ActionListener {
	private JTextField invoiceField;
	private JTextField customerField;
	private JTextField contactField;
	private JTextField emailField;
	private JTextField addressField;
	private JTextField plateField;
	private JTextField brandField;
	private JTextField slotField;
	private JTextField vatField;
	private JTextField hoursField;
	private JTextField rateField;
	private JComboBox<String> paymentBox;
	private JDateChooser parkedDateChooser;
	private JButton calculateButton;
	private JButton submitButton;
	private JButton menuButton;
	private JButton logoutButton;
	private JTextArea summaryArea;
	private JLabel totalLabel;

	public billingmanagement() {
		AppTheme.install();
		setContentPane(AppTheme.shell("Billing Management", "Calculate parking charges and prepare a billing summary.",
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
		invoiceField = addField(form, "Invoice #", row++);
		customerField = addField(form, "Customer Name", row++);
		contactField = addField(form, "Customer No.", row++);
		emailField = addField(form, "Email", row++);
		addressField = addField(form, "Address", row++);

		form.add(AppTheme.sectionLabel("Car and Billing Details"), wideConstraints(row++));
		plateField = addField(form, "Plate #", row++);
		brandField = addField(form, "Brand", row++);
		slotField = addField(form, "Slot #", row++);
		addDateRow(form, row++);
		addPaymentRow(form, row++);
		vatField = addField(form, "VAT", row++);
		hoursField = addField(form, "No. of Hours", row++);
		rateField = addField(form, "Park Rate", row++);
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

	private void addDateRow(JPanel form, int row) {
		form.add(AppTheme.label("Date Parked"), labelConstraints(row));
		parkedDateChooser = new JDateChooser();
		parkedDateChooser.setDate(new Date());
		form.add(parkedDateChooser, fieldConstraints(row));
	}

	private void addPaymentRow(JPanel form, int row) {
		form.add(AppTheme.label("Payment Method"), labelConstraints(row));
		paymentBox = new JComboBox<String>(new String[] { "Select -/-", "Cash", "GCash", "7/11 Clique",
				"Credit/Debit Card", "Paymaya", "Beep" });
		form.add(paymentBox, fieldConstraints(row));
	}

	private void addActions(JPanel form, int row) {
		JPanel actions = new JPanel();
		actions.setOpaque(false);

		calculateButton = AppTheme.primaryButton("Total");
		submitButton = AppTheme.secondaryButton("Submit");
		menuButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");

		calculateButton.addActionListener(this);
		submitButton.addActionListener(this);
		menuButton.addActionListener(this);
		logoutButton.addActionListener(this);

		actions.add(calculateButton);
		actions.add(submitButton);
		actions.add(menuButton);
		actions.add(logoutButton);
		form.add(actions, wideConstraints(row));
	}

	private JPanel buildSummary() {
		JPanel panel = new JPanel(new BorderLayout(0, 12));
		panel.setOpaque(false);

		totalLabel = AppTheme.sectionLabel("Total: 0.00");
		summaryArea = new JTextArea(20, 28);
		summaryArea.setEditable(false);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		summaryArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
		summaryArea.setText("Billing summary will appear here.");

		panel.add(totalLabel, BorderLayout.NORTH);
		panel.add(summaryArea, BorderLayout.CENTER);
		return panel;
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
		return constraints;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == calculateButton) {
			calculateTotal();
		} else if (source == submitButton) {
			writeSummary();
		} else if (source == menuButton) {
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		} else if (source == logoutButton) {
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		}
	}

	private void calculateTotal() {
		try {
			double vat = parseMoney(vatField);
			double hours = parseMoney(hoursField);
			double rate = parseMoney(rateField);
			double total = vat + (hours * rate);
			totalLabel.setText(String.format("Total: %.2f", total));
		} catch (NumberFormatException error) {
			summaryArea.setText("Enter numeric values for VAT, hours, and park rate.");
		}
	}

	private double parseMoney(JTextField field) {
		String value = field.getText().trim();
		if (value.isEmpty()) {
			return 0;
		}
		return Double.parseDouble(value);
	}

	private void writeSummary() {
		calculateTotal();
		String date = parkedDateChooser.getDate() == null ? "N/A"
				: new SimpleDateFormat("yyyy-MM-dd").format(parkedDateChooser.getDate());
		summaryArea.setText("Invoice #: " + invoiceField.getText().trim() + "\nCustomer: "
				+ customerField.getText().trim() + "\nContact: " + contactField.getText().trim() + "\nEmail: "
				+ emailField.getText().trim() + "\nAddress: " + addressField.getText().trim() + "\nPlate #: "
				+ plateField.getText().trim() + "\nBrand: " + brandField.getText().trim() + "\nSlot #: "
				+ slotField.getText().trim() + "\nDate Parked: " + date + "\nPayment: "
				+ paymentBox.getSelectedItem() + "\n" + totalLabel.getText());
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			billingmanagement app = new billingmanagement();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Billing Management", 1000, 700);
		});
	}
}

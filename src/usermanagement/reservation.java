package usermanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.ParkingSlot;
import app.ReservationRepository;
import com.toedter.calendar.JCalendar;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * Customer reservation workflow with live slot availability and scheduled booking.
 */
public class reservation extends JFrame implements ActionListener {
	private String username;
	private ReservationRepository repository;
	private JComboBox<String> floorBox;
	private JComboBox<String> typeBox;
	private JComboBox<String> availabilityBox;
	private JComboBox<String> startHourBox;
	private JComboBox<String> endHourBox;
	private JCalendar calendar;
	private JTable slotTable;
	private JTextField nameField;
	private JTextField emailField;
	private JTextField phoneField;
	private JTextField plateField;
	private JLabel statusLabel;
	private JButton searchButton;
	private JButton bookButton;
	private JButton historyButton;
	private JButton menuButton;
	private Timer refreshTimer;

	public reservation(String username) {
		AppTheme.install();
		this.username = username;
		Connection conn = ConnectionDB.getConnection();
		this.repository = new ReservationRepository(conn);
		setContentPane(AppTheme.shell("Book Parking Slot", "Search real-time availability and reserve a time range.",
				buildContent()));
		loadCustomerDefaults();
		loadSlots();
		refreshTimer = new Timer(5000, event -> loadSlots());
		refreshTimer.start();
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(buildBody(), BorderLayout.CENTER);
		content.add(buildStatus(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		toolbar.setOpaque(false);

		floorBox = new JComboBox<String>(new String[] { "All", "Ground", "Second", "Third" });
		typeBox = new JComboBox<String>(
				new String[] { "All", "Standard", "Compact", "Accessible", "EV", "Motorcycle" });
		availabilityBox = new JComboBox<String>(new String[] { "All", "Available", "Reserved", "Occupied",
				"Maintenance" });
		startHourBox = new JComboBox<String>(hours());
		endHourBox = new JComboBox<String>(hours());
		startHourBox.setSelectedItem("08:00");
		endHourBox.setSelectedItem("09:00");
		searchButton = AppTheme.primaryButton("Search");

		searchButton.addActionListener(this);

		toolbar.add(AppTheme.label("Floor"));
		toolbar.add(floorBox);
		toolbar.add(AppTheme.label("Type"));
		toolbar.add(typeBox);
		toolbar.add(AppTheme.label("Availability"));
		toolbar.add(availabilityBox);
		toolbar.add(AppTheme.label("From"));
		toolbar.add(startHourBox);
		toolbar.add(AppTheme.label("To"));
		toolbar.add(endHourBox);
		toolbar.add(searchButton);
		return toolbar;
	}

	private JPanel buildBody() {
		JPanel body = new JPanel(new BorderLayout(16, 0));
		body.setOpaque(false);

		calendar = new JCalendar();
		calendar.addPropertyChangeListener("calendar", evt -> loadSlots());
		body.add(calendar, BorderLayout.WEST);

		body.add(buildTable(), BorderLayout.CENTER);
		body.add(buildCustomerForm(), BorderLayout.EAST);
		return body;
	}

	private JScrollPane buildTable() {
		slotTable = new JTable();
		AppTheme.styleResponsiveTable(slotTable);
		return new JScrollPane(slotTable);
	}

	private JPanel buildCustomerForm() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);

		nameField = addField(form, "Customer Name", 0);
		emailField = addField(form, "Email", 1);
		phoneField = addField(form, "Phone", 2);
		plateField = addField(form, "Vehicle Plate", 3);
		form.add(buildActionPanel(), actionConstraints(4));
		return form;
	}

	private JPanel buildActionPanel() {
		JPanel actions = new JPanel(new GridLayout(0, 1, 0, 10));
		actions.setOpaque(false);

		bookButton = AppTheme.primaryButton("Reserve");
		historyButton = AppTheme.secondaryButton("History");
		menuButton = AppTheme.secondaryButton("Back");

		bookButton.addActionListener(this);
		historyButton.addActionListener(this);
		menuButton.addActionListener(this);

		actions.add(bookButton);
		actions.add(historyButton);
		actions.add(menuButton);
		return actions;
	}

	private JTextField addField(JPanel form, String labelText, int row) {
		JLabel label = AppTheme.label(labelText);
		JTextField field = AppTheme.textField(labelText);
		field.setColumns(18);
		label.setLabelFor(field);
		form.add(label, labelConstraints(row));
		form.add(field, fieldConstraints(row));
		return field;
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

	private GridBagConstraints actionConstraints(int row) {
		GridBagConstraints constraints = AppTheme.constraints(0, row);
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		return constraints;
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Live availability refreshes every 5 seconds.");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadCustomerDefaults() {
		try {
			String[] defaults = repository.customerDefaults(username);
			nameField.setText(defaults[0]);
			emailField.setText(defaults[1]);
			phoneField.setText(defaults[2]);
			plateField.setText(defaults[3]);
		} catch (Exception error) {
			statusLabel.setText("Customer defaults could not be loaded.");
		}
	}

	private void loadSlots() {
		DefaultTableModel model = new DefaultTableModel(
				new String[] { "Slot #", "Floor", "Type", "Availability", "Reservation Code" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		try {
			List<ParkingSlot> slots = repository.findSlots((String) floorBox.getSelectedItem(),
					(String) typeBox.getSelectedItem(), (String) availabilityBox.getSelectedItem(), startTime(),
					endTime());
			for (ParkingSlot slot : slots) {
				model.addRow(new Object[] { slot.getSlotId(), slot.getFloor(), slot.getSlotType(),
						slot.getAvailability(), slot.getReservationCode() });
			}
			slotTable.setModel(model);
			statusLabel.setText(slots.size() + " slot(s) shown for the selected schedule.");
		} catch (Exception error) {
			slotTable.setModel(model);
			AppTheme.showError(this, "Unable to load slot availability.", error);
		}
	}

	private void bookSelectedSlot() {
		int selectedRow = slotTable.getSelectedRow();
		if (selectedRow < 0) {
			statusLabel.setText("Select an available slot first.");
			return;
		}

		String availability = String.valueOf(slotTable.getValueAt(selectedRow, 3));
		if (!"Available".equalsIgnoreCase(availability)) {
			statusLabel.setText("Only available slots can be booked.");
			return;
		}

		int slotId = Integer.parseInt(String.valueOf(slotTable.getValueAt(selectedRow, 0)));
		try {
			String code = repository.createReservation(username, slotId, startTime(), endTime(), nameField.getText(),
					emailField.getText(), phoneField.getText(), plateField.getText());
			statusLabel.setText("Reservation confirmed: " + code);
			loadSlots();
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to create reservation.", error);
		}
	}

	private LocalDateTime startTime() {
		return selectedDateTime((String) startHourBox.getSelectedItem());
	}

	private LocalDateTime endTime() {
		return selectedDateTime((String) endHourBox.getSelectedItem());
	}

	private LocalDateTime selectedDateTime(String hour) {
		Date date = calendar.getDate() == null ? new Date() : calendar.getDate();
		String[] parts = hour.split(":");
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]));
	}

	private String[] hours() {
		String[] values = new String[24];
		for (int index = 0; index < values.length; index++) {
			values[index] = String.format("%02d:00", index);
		}
		return values;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == searchButton) {
			loadSlots();
		} else if (source == bookButton) {
			bookSelectedSlot();
		} else if (source == historyButton) {
			openHistory();
		} else if (source == menuButton) {
			openMenu();
		}
	}

	private void openHistory() {
		stopRefresh();
		reservation_history app = new reservation_history(username);
		AppTheme.showFrame(app, "Reservation History", 1080, 640);
		dispose();
	}

	private void openMenu() {
		stopRefresh();
		menu app = new menu(username);
		AppTheme.showFrame(app, "User Menu", 980, 540);
		dispose();
	}

	private void stopRefresh() {
		if (refreshTimer != null) {
			refreshTimer.stop();
		}
	}

	@Override
	public void dispose() {
		stopRefresh();
		super.dispose();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			reservation app = new reservation("Harayuri");
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Book Parking Slot", 1180, 720);
		});
	}
}

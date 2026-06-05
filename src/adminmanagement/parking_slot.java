package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.ParkingSlot;
import app.ReservationRepository;
import app.SessionContext;
import com.toedter.calendar.JDateChooser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * Admin live slot dashboard with search, availability filtering, and slot status management.
 */
public class parking_slot extends JFrame implements ActionListener {
	private ReservationRepository repository;
	private JComboBox<String> floorBox;
	private JComboBox<String> typeBox;
	private JComboBox<String> availabilityBox;
	private JComboBox<String> startHourBox;
	private JComboBox<String> endHourBox;
	private JComboBox<String> baseStatusBox;
	private JDateChooser dateChooser;
	private JTable table;
	private JButton menuButton;
	private JButton logoutButton;
	private JButton refreshButton;
	private JButton reserveButton;
	private JButton updateStatusButton;
	private JLabel statusLabel;
	private Timer refreshTimer;

	public parking_slot() {
		AppTheme.install();
		repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Parking Slots", "Live slot availability by floor, type, and schedule.",
				buildContent()));
		loadSlots();
		refreshTimer = new Timer(5000, event -> loadSlots());
		refreshTimer.start();
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(buildTable(), BorderLayout.CENTER);
		content.add(buildStatus(), BorderLayout.SOUTH);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		toolbar.setOpaque(false);

		menuButton = AppTheme.secondaryButton("Back to Menu");
		logoutButton = AppTheme.dangerButton("Logout");
		reserveButton = AppTheme.primaryButton("Reserve Slot");
		refreshButton = AppTheme.primaryButton("Refresh");
		updateStatusButton = AppTheme.secondaryButton("Update Slot");
		floorBox = new JComboBox<String>(new String[] { "All", "Ground", "Second", "Third" });
		typeBox = new JComboBox<String>(
				new String[] { "All", "Standard", "Compact", "Accessible", "EV", "Motorcycle" });
		availabilityBox = new JComboBox<String>(new String[] { "All", "Available", "Reserved", "Occupied",
				"Maintenance" });
		baseStatusBox = new JComboBox<String>(new String[] { "available", "maintenance" });
		dateChooser = new JDateChooser(new Date());
		startHourBox = new JComboBox<String>(hours());
		endHourBox = new JComboBox<String>(hours());
		startHourBox.setSelectedItem("08:00");
		endHourBox.setSelectedItem("09:00");

		menuButton.addActionListener(this);
		logoutButton.addActionListener(this);
		reserveButton.addActionListener(this);
		refreshButton.addActionListener(this);
		updateStatusButton.addActionListener(this);

		toolbar.add(menuButton);
		toolbar.add(logoutButton);
		toolbar.add(reserveButton);
		toolbar.add(AppTheme.label("Floor"));
		toolbar.add(floorBox);
		toolbar.add(AppTheme.label("Type"));
		toolbar.add(typeBox);
		toolbar.add(AppTheme.label("Availability"));
		toolbar.add(availabilityBox);
		toolbar.add(AppTheme.label("Date"));
		toolbar.add(dateChooser);
		toolbar.add(AppTheme.label("From"));
		toolbar.add(startHourBox);
		toolbar.add(AppTheme.label("To"));
		toolbar.add(endHourBox);
		toolbar.add(refreshButton);
		toolbar.add(AppTheme.label("Set"));
		toolbar.add(baseStatusBox);
		toolbar.add(updateStatusButton);
		return toolbar;
	}

	private JScrollPane buildTable() {
		table = new JTable();
		AppTheme.styleResponsiveTable(table);
		return new JScrollPane(table);
	}

	private JLabel buildStatus() {
		statusLabel = AppTheme.label("Live slot availability refreshes every 5 seconds.");
		statusLabel.setForeground(AppTheme.MUTED_TEXT);
		return statusLabel;
	}

	private void loadSlots() {
		DefaultTableModel model = new DefaultTableModel(
				new String[] { "Slot #", "Floor", "Type", "Base Status", "Availability", "Reservation Code" }, 0) {
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
				model.addRow(new Object[] { slot.getSlotId(), slot.getFloor(), slot.getSlotType(), slot.getBaseStatus(),
						slot.getAvailability(), slot.getReservationCode() });
			}
			table.setModel(model);
			statusLabel.setText(slots.size() + " slot row(s) shown for the selected schedule.");
		} catch (Exception error) {
			table.setModel(model);
			AppTheme.showError(this, "Unable to load slot availability.", error);
		}
	}

	private void updateSelectedSlot() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			statusLabel.setText("Select a slot before updating its status.");
			return;
		}

		int slotId = Integer.parseInt(String.valueOf(table.getValueAt(selectedRow, 0)));
		try {
			repository.updateSlotBaseStatus(slotId, (String) baseStatusBox.getSelectedItem(), SessionContext.username());
			loadSlots();
			statusLabel.setText("Slot " + slotId + " updated.");
		} catch (Exception error) {
			AppTheme.showError(this, "Unable to update slot.", error);
		}
	}

	private LocalDateTime startTime() {
		return selectedDateTime((String) startHourBox.getSelectedItem());
	}

	private LocalDateTime endTime() {
		return selectedDateTime((String) endHourBox.getSelectedItem());
	}

	private LocalDateTime selectedDateTime(String hour) {
		Date date = dateChooser.getDate() == null ? new Date() : dateChooser.getDate();
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
		if (source == refreshButton) {
			loadSlots();
		} else if (source == logoutButton) {
			stopRefresh();
			login app = new login();
			AppTheme.showFrame(app, "Admin Login", 540, 640);
			dispose();
		} else if (source == menuButton) {
			stopRefresh();
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		} else if (source == reserveButton) {
			stopRefresh();
			parking_inventory_management_add_button app = new parking_inventory_management_add_button();
			AppTheme.showFrame(app, "Add Parking Reservation", 1180, 760);
			dispose();
		} else if (source == updateStatusButton) {
			updateSelectedSlot();
		}
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
			parking_slot app = new parking_slot();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Parking Slot", 1180, 720);
		});
	}
}

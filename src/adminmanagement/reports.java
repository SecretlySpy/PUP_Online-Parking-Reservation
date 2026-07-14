package adminmanagement;

import DatabaseConnection.ConnectionDB;
import app.AppTheme;
import app.ReservationRepository;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Admin report screen for system activity and reservation counts.
 */
public class reports extends JFrame implements ActionListener {
	private ReservationRepository repository;
	private JLabel slotsLabel;
	private JLabel availableLabel;
	private JLabel reservedLabel;
	private JLabel occupiedLabel;
	private JLabel completedLabel;
	private JLabel cancelledLabel;
	private JTable activityTable;
	private JButton refreshButton;
	private JButton exportButton;
	private JButton menuButton;

	public reports() {
		AppTheme.install();
		repository = new ReservationRepository(ConnectionDB.getConnection());
		setContentPane(AppTheme.shell("Reports", "Monitor availability, reservation status, and system activity.",
				buildContent()));
		loadReport();
	}

	private JPanel buildContent() {
		JPanel content = AppTheme.card();
		content.setLayout(new BorderLayout(0, 16));
		content.add(buildToolbar(), BorderLayout.NORTH);
		content.add(buildMetrics(), BorderLayout.WEST);
		content.add(buildActivityTable(), BorderLayout.CENTER);
		return content;
	}

	private JPanel buildToolbar() {
		JPanel toolbar = new JPanel();
		toolbar.setOpaque(false);

		refreshButton = AppTheme.primaryButton("Refresh");
		exportButton = AppTheme.primaryButton("Export CSV");
		menuButton = AppTheme.secondaryButton("Back to Menu");
		
		refreshButton.addActionListener(this);
		exportButton.addActionListener(this);
		menuButton.addActionListener(this);

		toolbar.add(refreshButton);
		toolbar.add(exportButton);
		toolbar.add(menuButton);
		return toolbar;
	}

	private JPanel buildMetrics() {
		JPanel metrics = new JPanel(new GridLayout(0, 1, 8, 8));
		metrics.setOpaque(false);

		slotsLabel = AppTheme.sectionLabel("Slots: 0");
		availableLabel = AppTheme.sectionLabel("Available Now: 0");
		reservedLabel = AppTheme.sectionLabel("Reserved: 0");
		occupiedLabel = AppTheme.sectionLabel("Occupied: 0");
		completedLabel = AppTheme.sectionLabel("Completed: 0");
		cancelledLabel = AppTheme.sectionLabel("Cancelled: 0");

		metrics.add(slotsLabel);
		metrics.add(availableLabel);
		metrics.add(reservedLabel);
		metrics.add(occupiedLabel);
		metrics.add(completedLabel);
		metrics.add(cancelledLabel);
		return metrics;
	}

	private JScrollPane buildActivityTable() {
		activityTable = new JTable();
		AppTheme.styleResponsiveTable(activityTable);
		return new JScrollPane(activityTable);
	}

	private void loadReport() {
		DefaultTableModel model = new DefaultTableModel(new String[] { "Actor", "Type", "Message", "Created" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		try {
			Map<String, Integer> counts = repository.reportCounts();
			slotsLabel.setText("Slots: " + counts.get("slots"));
			availableLabel.setText("Available Now: " + counts.get("available_now"));
			reservedLabel.setText("Reserved: " + counts.get("reserved"));
			occupiedLabel.setText("Occupied: " + counts.get("occupied"));
			completedLabel.setText("Completed: " + counts.get("completed"));
			cancelledLabel.setText("Cancelled: " + counts.get("cancelled"));

			List<String[]> rows = repository.recentActivity();
			for (String[] row : rows) {
				model.addRow(row);
			}
			activityTable.setModel(model);
		} catch (Exception error) {
			activityTable.setModel(model);
			AppTheme.showError(this, "Unable to load reports.", error);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == refreshButton) {
			loadReport();
		} else if (source == exportButton) {
			exportReport();
		} else if (source == menuButton) {
			menu app = new menu();
			AppTheme.showFrame(app, "Admin Menu", 1060, 600);
			dispose();
		}
	}

	private void exportReport() {
		javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
		chooser.setDialogTitle("Save Report");
		chooser.setSelectedFile(new java.io.File("Report.csv"));
		if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
			try (java.io.FileWriter writer = new java.io.FileWriter(chooser.getSelectedFile())) {
				writer.write("Metrics\n");
				writer.write(slotsLabel.getText() + "\n");
				writer.write(availableLabel.getText() + "\n");
				writer.write(reservedLabel.getText() + "\n");
				writer.write(occupiedLabel.getText() + "\n");
				writer.write(completedLabel.getText() + "\n");
				writer.write(cancelledLabel.getText() + "\n\n");
				
				writer.write("Recent Activity\n");
				writer.write("Actor,Type,Message,Created\n");
				javax.swing.table.TableModel model = activityTable.getModel();
				for (int i = 0; i < model.getRowCount(); i++) {
					for (int j = 0; j < model.getColumnCount(); j++) {
						String val = String.valueOf(model.getValueAt(i, j)).replace("\"", "\"\"");
						writer.write("\"" + val + "\"");
						if (j < model.getColumnCount() - 1) writer.write(",");
					}
					writer.write("\n");
				}
				javax.swing.JOptionPane.showMessageDialog(this, "Report exported successfully.");
			} catch (Exception ex) {
				AppTheme.showError(this, "Failed to export report", ex);
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			reports app = new reports();
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			AppTheme.showFrame(app, "Reports", 1080, 660);
		});
	}
}

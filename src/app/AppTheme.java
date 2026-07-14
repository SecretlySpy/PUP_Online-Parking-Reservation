package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;

/**
 * Provides a single visual language for the Swing screens: spacing, typography,
 * colors, table styling, and frame defaults.
 */
public final class AppTheme {
	public static Color BACKGROUND = new Color(245, 247, 250);
	public static Color SURFACE = Color.WHITE;
	public static Color SURFACE_ALT = new Color(238, 242, 247);
	public static Color TEXT = new Color(30, 41, 59);
	public static Color MUTED_TEXT = new Color(100, 116, 139);
	public static Color BORDER = new Color(203, 213, 225);
	public static Color PRIMARY = new Color(128, 0, 32);
	public static Color PRIMARY_HOVER = new Color(104, 0, 26);
	public static Color ACCENT = new Color(245, 158, 11);
	public static Color SUCCESS = new Color(22, 101, 52);
	public static Color DANGER = new Color(185, 28, 28);

	public static boolean isDarkMode = false;

	public static void toggleTheme() {
		isDarkMode = !isDarkMode;
		if (isDarkMode) {
			BACKGROUND = new Color(15, 23, 42);
			SURFACE = new Color(30, 41, 59);
			SURFACE_ALT = new Color(51, 65, 85);
			TEXT = new Color(248, 250, 252);
			MUTED_TEXT = new Color(148, 163, 184);
			BORDER = new Color(71, 85, 105);
			PRIMARY = new Color(225, 29, 72);
			PRIMARY_HOVER = new Color(190, 18, 60);
		} else {
			BACKGROUND = new Color(245, 247, 250);
			SURFACE = Color.WHITE;
			SURFACE_ALT = new Color(238, 242, 247);
			TEXT = new Color(30, 41, 59);
			MUTED_TEXT = new Color(100, 116, 139);
			BORDER = new Color(203, 213, 225);
			PRIMARY = new Color(128, 0, 32);
			PRIMARY_HOVER = new Color(104, 0, 26);
		}
		FIELD_BORDER = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(8, 10, 8, 10));
	}

	public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
	public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	public static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 18);
	public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
	public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

	private static Border FIELD_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(BORDER),
			BorderFactory.createEmptyBorder(8, 10, 8, 10));

	private AppTheme() {
		// Utility class; instances are not needed.
	}

	public static void install() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
			// The platform look and feel is optional; the explicit component styling below still applies.
		}

		UIManager.put("Label.font", BODY_FONT);
		UIManager.put("Button.font", BUTTON_FONT);
		UIManager.put("TextField.font", BODY_FONT);
		UIManager.put("PasswordField.font", BODY_FONT);
		UIManager.put("ComboBox.font", BODY_FONT);
		UIManager.put("CheckBox.font", BODY_FONT);
		UIManager.put("RadioButton.font", BODY_FONT);
		UIManager.put("Table.font", BODY_FONT);
		UIManager.put("TableHeader.font", BUTTON_FONT);
		UIManager.put("OptionPane.messageFont", BODY_FONT);
		UIManager.put("OptionPane.buttonFont", BUTTON_FONT);
		UIManager.put("Button.disabledText", MUTED_TEXT);
	}

	public static JPanel shell(String title, String subtitle, JComponent content) {
		JPanel root = new JPanel(new BorderLayout(0, 18));
		root.setBackground(BACKGROUND);
		root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
		root.add(header(title, subtitle), BorderLayout.NORTH);
		root.add(content, BorderLayout.CENTER);
		return root;
	}

	public static JPanel header(String title, String subtitle) {
		JPanel header = new JPanel(new BorderLayout(12, 4));
		header.setOpaque(false);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(TEXT);
		header.add(titleLabel, BorderLayout.NORTH);

		if (subtitle != null && !subtitle.trim().isEmpty()) {
			JLabel subtitleLabel = new JLabel(subtitle);
			subtitleLabel.setFont(SUBTITLE_FONT);
			subtitleLabel.setForeground(MUTED_TEXT);
			header.add(subtitleLabel, BorderLayout.CENTER);
		}

		return header;
	}

	public static JPanel card() {
		JPanel panel = new JPanel();
		panel.setBackground(SURFACE);
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(20, 20, 20, 20)));
		return panel;
	}

	public static GridBagConstraints constraints(int column, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.insets = new Insets(8, 8, 8, 8);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		return constraints;
	}

	public static JLabel label(String text) {
		JLabel label = new JLabel(text);
		label.setFont(BODY_FONT);
		label.setForeground(TEXT);
		return label;
	}

	public static JLabel sectionLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(SECTION_FONT);
		label.setForeground(TEXT);
		return label;
	}

	public static JTextField textField(String accessibleName) {
		JTextField field = new JTextField();
		styleTextField(field);
		field.getAccessibleContext().setAccessibleName(accessibleName);
		return field;
	}

	public static JPasswordField passwordField(String accessibleName) {
		JPasswordField field = new JPasswordField();
		field.setFont(BODY_FONT);
		field.setForeground(TEXT);
		field.setBorder(FIELD_BORDER);
		field.getAccessibleContext().setAccessibleName(accessibleName);
		return field;
	}

	public static void styleTextField(JTextField field) {
		field.setFont(BODY_FONT);
		field.setForeground(TEXT);
		field.setBorder(FIELD_BORDER);
	}

	public static JButton primaryButton(String text) {
		return button(text, PRIMARY, Color.WHITE);
	}

	public static JButton secondaryButton(String text) {
		return button(text, SURFACE_ALT, TEXT);
	}

	public static JButton dangerButton(String text) {
		return button(text, DANGER, Color.WHITE);
	}

	public static JButton moduleButton(String text) {
		JButton button = button(text, SURFACE_ALT, TEXT);
		button.setFont(new Font("Segoe UI", Font.BOLD, 15));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setPreferredSize(new Dimension(220, 76));
		button.setMinimumSize(new Dimension(180, 64));
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(BORDER),
				BorderFactory.createEmptyBorder(18, 20, 18, 20)));
		return button;
	}

	private static JButton button(String text, Color background, Color foreground) {
		JButton button = new ThemeButton(text, background, foreground);
		button.setFont(BUTTON_FONT);
		button.setBackground(background);
		button.setForeground(foreground);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(background.darker()),
				BorderFactory.createEmptyBorder(9, 14, 9, 14)));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.getAccessibleContext().setAccessibleName(text);
		return button;
	}

	private static class ThemeButton extends JButton {
		private Color normalBackground;
		private Color normalForeground;

		ThemeButton(String text, Color background, Color foreground) {
			super(text);
			this.normalBackground = background;
			this.normalForeground = foreground;
			setContentAreaFilled(false);
			setOpaque(false);
			setRolloverEnabled(true);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			Graphics2D graphics2D = (Graphics2D) graphics.create();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2D.setColor(resolveBackground());
			graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
			graphics2D.dispose();

			setForeground(isEnabled() ? normalForeground : MUTED_TEXT);
			super.paintComponent(graphics);
		}

		private Color resolveBackground() {
			if (!isEnabled()) {
				return new Color(229, 231, 235);
			}
			if (getModel().isPressed()) {
				return normalBackground.darker();
			}
			if (getModel().isRollover()) {
				return blend(normalBackground, Color.WHITE, 0.12f);
			}
			return normalBackground;
		}
	}

	private static Color blend(Color first, Color second, float ratio) {
		float inverse = 1f - ratio;
		int red = Math.round(first.getRed() * inverse + second.getRed() * ratio);
		int green = Math.round(first.getGreen() * inverse + second.getGreen() * ratio);
		int blue = Math.round(first.getBlue() * inverse + second.getBlue() * ratio);
		return new Color(red, green, blue);
	}

	public static void styleTable(JTable table) {
		table.setFont(BODY_FONT);
		table.setForeground(TEXT);
		table.setRowHeight(34);
		table.setGridColor(new Color(226, 232, 240));
		table.setShowVerticalLines(false);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JTableHeader header = table.getTableHeader();
		header.setFont(BUTTON_FONT);
		header.setForeground(TEXT);
		header.setBackground(SURFACE_ALT);
		header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
	}

	public static void styleResponsiveTable(JTable table) {
		styleTable(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		if (!Boolean.TRUE.equals(table.getClientProperty("app.responsiveColumns"))) {
			table.putClientProperty("app.responsiveColumns", Boolean.TRUE);
			table.addPropertyChangeListener("model",
					event -> SwingUtilities.invokeLater(() -> resizeResponsiveColumns(table)));
			table.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent event) {
					resizeResponsiveColumns(table);
				}
			});
		}
		SwingUtilities.invokeLater(() -> resizeResponsiveColumns(table));
	}

	public static void refreshResponsiveTable(JTable table) {
		resizeResponsiveColumns(table);
	}

	private static void resizeResponsiveColumns(JTable table) {
		if (table.getColumnCount() == 0) {
			return;
		}

		int targetWidth = responsiveTableWidth(table);
		if (targetWidth <= 0) {
			return;
		}

		TableColumnModel columnModel = table.getColumnModel();
		int[] preferredWidths = new int[columnModel.getColumnCount()];
		int totalPreferredWidth = 0;
		for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
			preferredWidths[columnIndex] = preferredColumnWidth(table, columnIndex);
			totalPreferredWidth += preferredWidths[columnIndex];
		}

		int remainingWidth = targetWidth;
		for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
			TableColumn column = columnModel.getColumn(columnIndex);
			int width = preferredWidths[columnIndex];
			if (totalPreferredWidth > 0) {
				width = Math.max(52, (int) Math.round((double) preferredWidths[columnIndex] * targetWidth
						/ totalPreferredWidth));
			}
			if (columnIndex == columnModel.getColumnCount() - 1) {
				width = Math.max(52, remainingWidth);
			}
			column.setMinWidth(52);
			column.setPreferredWidth(width);
			remainingWidth -= width;
		}
	}

	private static int responsiveTableWidth(JTable table) {
		if (table.getParent() instanceof JViewport) {
			return ((JViewport) table.getParent()).getExtentSize().width;
		}
		return table.getWidth();
	}

	private static int preferredColumnWidth(JTable table, int columnIndex) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		int width = table.getTableHeader().getDefaultRenderer()
				.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, -1, columnIndex)
				.getPreferredSize().width + 24;
		int sampleRows = Math.min(table.getRowCount(), 25);
		for (int rowIndex = 0; rowIndex < sampleRows; rowIndex++) {
			int contentWidth = table.prepareRenderer(table.getCellRenderer(rowIndex, columnIndex), rowIndex,
					columnIndex).getPreferredSize().width + 24;
			width = Math.max(width, contentWidth);
		}
		return Math.min(Math.max(width, 72), 260);
	}

	public static JLabel clockLabel() {
		JLabel clock = new JLabel();
		clock.setFont(SUBTITLE_FONT);
		clock.setForeground(MUTED_TEXT);
		clock.setHorizontalAlignment(SwingConstants.RIGHT);
		SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");
		Timer timer = new Timer(1000, event -> clock.setText(format.format(new Date())));
		timer.setInitialDelay(0);
		timer.start();
		return clock;
	}

	public static void showFrame(JFrame frame, String title, int width, int height) {
		install();
		frame.setTitle(title);
		frame.setMinimumSize(new Dimension(Math.min(width, 720), Math.min(height, 520)));
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void showError(Component parent, String message, Exception exception) {
		String detail = exception == null ? message : message + "\n\nDetails: " + exception.getMessage();
		JOptionPane.showMessageDialog(parent, detail, "Unable to continue", JOptionPane.ERROR_MESSAGE);
	}
}

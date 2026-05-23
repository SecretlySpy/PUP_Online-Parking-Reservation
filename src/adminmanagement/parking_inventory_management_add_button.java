package adminmanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import DatabaseConnection.ConnectionDB;
import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class parking_inventory_management_add_button extends JFrame implements ActionListener {

	public JLabel custdet, carprof, id, fnam, lnam, bdate, mno, email, platno, brand, color, gender;
	public JTextField tid, tfnam, tlnam, tmno, temail, tplatno, tbrand, tcolor;
	public JRadioButton male, female;
	public ButtonGroup gengp;
	public JComboBox<?> date, month, year;
	public Connection conn = null;
	public Statement st = null;
	public PreparedStatement ps = null;
	public ResultSet rs = null;
	public DefaultTableModel dm = null;
	public ResultSetMetaData rsmd = null;
	Font f1 = new Font("Times New Roman", Font.PLAIN, 15);
	Font f2 = new Font("Times New Roman", Font.BOLD, 20);
	Font f3 = new Font("Times New Roman", Font.PLAIN, 13);
	Font f4 = new Font("Times New Roman", Font.CENTER_BASELINE, 15);
	// Color c1 = new Color.

	private JLabel lblClock;
	private ImageIcon icon;
	private JLabel label;
	private JLabel photo;
	private JLabel title;
	private JLabel or;
	private JButton col;
	private JLabel type;
	private JComboBox<?> combo;
	private JButton sub;
	private JButton reset1;
	private JTextArea tout;
	private JTextArea tout1;
	public com.toedter.calendar.JDateChooser Date, Date1;
	public JLabel dpark, dres, tot, slot, time, time1;
	public JTextField ttot, tslot, ttime, ttime1;

	public void Clock() {
		Thread clock = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						Calendar cal = new GregorianCalendar();
						int day = cal.get(Calendar.DAY_OF_MONTH);
						int month = cal.get(Calendar.MONTH);
						int year = cal.get(Calendar.YEAR);

						int second = cal.get(Calendar.SECOND);
						int minute = cal.get(Calendar.MINUTE);
						int hour = cal.get(Calendar.HOUR);

						lblClock.setText("Time " + hour + " : " + minute + " : " + second + " Date " + year + " / "
								+ (month + 1) + " / " + day);
						sleep(1000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		clock.start();
	}

	public parking_inventory_management_add_button() {
		conn = ConnectionDB.getConnection();
		icon = new ImageIcon("src/m.jpg");

		Container con = getContentPane();
		con.setLayout(null);

		photo = new JLabel() {
			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, null);
				super.paintComponent(g);
			}
		};

		photo.setOpaque(false);
		con.add(photo);
		photo.setBounds(10, 10, 100, 100);

		con.add(lblClock = new JLabel(""));
		lblClock.setBounds(930, 10, 230, 30);
		lblClock.setFont(f3);

		Date = new JDateChooser();
		Date.setBounds(750, 110, 200, 30);
		con.add(Date);

		Date1 = new JDateChooser();
		Date1.setBounds(750, 150, 200, 30);
		con.add(Date1);

		title = new JLabel("Archim's TechCorner");
		title.setBounds(100, -10, 200, 100);
		con.add(title);

		custdet = new JLabel(
				"------------------------CUSTOMER DETAILS------------------------               ----------------PARKING DETAILS--------------");
		custdet.setBounds(10, 60, 1200, 50);
		custdet.setFont(f2);
		con.add(custdet);

		id = new JLabel("ID:");
		id.setBounds(40, 110, 50, 30);
		id.setFont(f1);
		con.add(id);

		fnam = new JLabel("Full Name:");
		fnam.setBounds(40, 150, 80, 30);
		fnam.setFont(f1);
		con.add(fnam);

		lnam = new JLabel("E-mail:");
		lnam.setBounds(40, 190, 80, 30);
		lnam.setFont(f1);
		con.add(lnam);

		gender = new JLabel("Gender:");
		gender.setBounds(40, 230, 80, 30);
		gender.setFont(f1);
		con.add(gender);

		mno = new JLabel("Mobile #:");
		mno.setBounds(40, 270, 80, 30);
		mno.setFont(f1);
		con.add(mno);

		carprof = new JLabel(
				"------------------------CAR DETAILS------------------------------                                        SUMMARY PROFILE");
		carprof.setBounds(10, 330, 1200, 50);
		carprof.setFont(f2);
		con.add(carprof);

		platno = new JLabel("PLATE #:");
		platno.setBounds(40, 370, 80, 30);
		platno.setFont(f1);
		con.add(platno);

		brand = new JLabel("Brand:");
		brand.setBounds(40, 410, 80, 30);
		brand.setFont(f1);
		con.add(brand);

		color = new JLabel("Color:");
		color.setBounds(40, 450, 80, 30);
		color.setFont(f1);
		con.add(color);

		or = new JLabel("or");
		or.setBounds(310, 450, 20, 30);
		or.setFont(f1);
		con.add(or);

		col = new JButton("Choose Color");
		col.setBounds(330, 450, 120, 30);
		col.setFont(f1);
		col.addActionListener(this);
		con.add(col);

		type = new JLabel("Type of Car:");
		type.setBounds(40, 490, 100, 30);
		type.setFont(f1);
		con.add(type);

		String Select[] = { "Select -/-", "Hatchback", " Sedan", "MPV", "SUV", "Crossover", " Coupe", "Convertible",
				"Sports Car", "Utility", "Compact", "Mini Van", "Van", "Pick-Up Truck" };
		combo = new JComboBox(Select);
		combo.setFont(new Font("Arial", Font.PLAIN, 15));
		combo.setBounds(150, 490, 100, 30);
		con.add(combo);

		sub = new JButton("Submit");
		sub.setFont(new Font("Arial", Font.PLAIN, 15));
		sub.setBounds(150, 570, 100, 20);
		sub.addActionListener(this);
		con.add(sub);

		reset1 = new JButton("Reset");
		reset1.setFont(new Font("Arial", Font.PLAIN, 15));
		reset1.setBounds(300, 570, 100, 20);
		reset1.addActionListener(this);
		con.add(reset1);

		tout = new JTextArea();
		tout.setFont(new Font("Arial", Font.PLAIN, 15));
		tout.setBounds(550, 370, 250, 300);
		tout.setLineWrap(true);
		tout.setEditable(false);
		con.add(tout);

		tout1 = new JTextArea();
		tout1.setFont(new Font("Arial", Font.PLAIN, 15));
		tout1.setBounds(800, 370, 350, 300);
		tout1.setLineWrap(true);
		tout1.setEditable(false);
		con.add(tout1);

		tid = new JTextField();
		tid.setBounds(150, 110, 200, 30);
		tid.setFont(f1);
		con.add(tid);

		tfnam = new JTextField();
		tfnam.setBounds(150, 150, 300, 30);
		tfnam.setFont(f1);
		con.add(tfnam);

		tlnam = new JTextField();
		tlnam.setBounds(150, 190, 300, 30);
		tlnam.setFont(f1);
		con.add(tlnam);

		tmno = new JTextField("+63");
		tmno.setBounds(150, 270, 300, 30);
		tmno.setFont(f1);
		con.add(tmno);

		tplatno = new JTextField();
		tplatno.setBounds(150, 370, 300, 30);
		tplatno.setFont(f1);
		con.add(tplatno);

		tbrand = new JTextField();
		tbrand.setBounds(150, 410, 300, 30);
		tbrand.setFont(f1);
		con.add(tbrand);

		tcolor = new JTextField();
		tcolor.setBounds(150, 450, 150, 30);
		tcolor.setFont(f1);
		con.add(tcolor);

		male = new JRadioButton("Male");
		male.setFont(new Font("Arial", Font.PLAIN, 15));
		male.setSelected(true);
		male.setBounds(150, 230, 80, 30);
		male.setActionCommand("male");
		con.add(male);

		female = new JRadioButton("Female");
		female.setFont(new Font("Arial", Font.PLAIN, 15));
		female.setSelected(true);
		female.setBounds(230, 230, 80, 30);
		female.setActionCommand("female");
		con.add(female);

		gengp = new ButtonGroup();
		gengp.add(male);
		gengp.add(female);

		bdate = new JLabel("Birthdate:");
		bdate.setFont(new Font("Arial", Font.PLAIN, 15));
		bdate.setBounds(40, 310, 80, 30);
		con.add(bdate);

		String dates[] = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
				"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
		date = new JComboBox(dates);
		date.setFont(new Font("Arial", Font.PLAIN, 15));
		date.setBounds(250, 310, 80, 30);
		con.add(date);

		String months[] = { "Jan", "feb", "Mar", "Apr", "May", "Jun", "July", "Aug", "Sup", "Oct", "Nov", "Dec" };
		month = new JComboBox(months);
		month.setFont(new Font("Arial", Font.PLAIN, 15));
		month.setBounds(150, 310, 80, 30);
		con.add(month);

		String years[] = { "1930", "1931", "1932", "1933", "1934", "1935", "1936", "1937", "1938", "1939", "1940",
				"1941", "1942", "1943", "1944", "1945", "1946", "1947", "1948", "1949", "1950", "1951", "1952", "1953",
				"1954", "1955", "1956", "1957", "1958", "1959", "1960", "1961", "1962", "1963", "1964", "1965", "1966",
				"1967", "1968", "1969", "1970", "1971", "1972", "1973", "1974", "1975", "1976", "1977", "1978", "1979",
				"1980", "1981", "1982", "1983", "1984", "1985", "1986", "1987", "1988", "1989", "1990", "1991", "1992",
				"1993", "1994", "1995", "1996", "1997", "1998", "1999", "2000", "2001", "2002", "2003", "2004", "2005",
				"2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018",
				"2019", "2020" };
		year = new JComboBox(years);
		year.setFont(new Font("Arial", Font.PLAIN, 15));
		year.setBounds(350, 310, 80, 30);
		con.add(year);

		dpark = new JLabel("Date of Parked:");
		dpark.setFont(f1);
		dpark.setBounds(580, 150, 150, 30);
		con.add(dpark);

		dres = new JLabel("Date of Reservation:");
		dres.setFont(f1);
		dres.setBounds(580, 110, 150, 30);
		con.add(dres);

		time = new JLabel("Time of Park:");
		time.setFont(f1);
		time.setBounds(580, 270, 150, 30);
		con.add(time);

		time1 = new JLabel("Time of Departue:");
		time1.setFont(f1);
		time1.setBounds(580, 310, 150, 30);
		con.add(time1);

		tot = new JLabel("Total Hours of Parked:");
		tot.setFont(f1);
		tot.setBounds(580, 230, 150, 30);
		con.add(tot);

		slot = new JLabel("Slot #:");
		slot.setFont(f1);
		slot.setBounds(580, 190, 150, 30);
		con.add(slot);

		ttime = new JTextField();
		ttime.setBounds(750, 190, 150, 30);
		con.add(ttime);

		ttime1 = new JTextField();
		ttime1.setBounds(750, 230, 150, 30);
		con.add(ttime1);

		ttot = new JTextField();
		ttot.setBounds(750, 270, 150, 30);
		con.add(ttot);

		tslot = new JTextField();
		tslot.setBounds(750, 310, 150, 30);
		con.add(tslot);

		Clock();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == sub) {

			String data1;

			String data = "           CUSTOMER DETAILS     " + "\n" + "ID # :" + tid.getText() + "\n" + "Full Name : "
					+ tfnam.getText() + "\n" + "E-mail :" + tlnam.getText() + "\n" + "Mobile # : " + tmno.getText()
					+ "\n";

			if (male.isSelected())
				data1 = "Gender : Male" + "\n";
			else
				data1 = "Gender : Female" + "\n";

			String data2 = "Date of Birth : " + (String) date.getSelectedItem() + "/" + (String) month.getSelectedItem()
					+ "/" + (String) year.getSelectedItem() + "\n" + "\n" + "             CAR DETAILS     " + "\n"
					+ "PLATE # :" + tplatno.getText() + "\n" + "Brand :" + tbrand.getText() + "\n"
					+ "Color of the Car :" + tcolor.getText() + "\n";

			String data3 = "Type of Car : " + (String) combo.getSelectedItem()

					+ "\n";

			String datas = "           PARKING DETAILS     " + "\n" + "Date of Reservation :" + Date.getDate() + "\n"
					+ "Date of Parked :" + Date1.getDate() + "\n" + "Slot # :" + ttime.getText() + "\n"
					+ "Total Hours Parked : " + ttime1.getText() + "\n" + "Time of Park : " + ttot.getText() + "\n"
					+ "Time of Departure : " + tslot.getText() + "\n";
			tout.setText(data + data1 + data2 + data3);
			tout.setEditable(false);
			tout1.setText(datas);
			tout1.setEditable(false);
			
			try {
				String query = "insert into inventory (ID,FullName,Email,Gender,MobileNumber,Birthdate,PlateNumber,Brand,Color,Type,DOR,DOP,SlotNumber,THP,TP,TD) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

				ps = conn.prepareStatement(query);
				ps.setString(1, tid.getText());
				ps.setString(2, fnam.getText());
				ps.setString(3, tlnam.getText());
				ps.setString(4, gengp.getSelection().getActionCommand());
				ps.setString(5, tmno.getText());
				ps.setString(6, (String) date.getSelectedItem());
				ps.setString(6, (String) month.getSelectedItem());
				ps.setString(6, (String) year.getSelectedItem());
				ps.setString(7, tplatno.getText());
				ps.setString(8, tbrand.getText());
				ps.setString(9, tcolor.getText());
				ps.setString(10, (String) combo.getSelectedItem());
				ps.setString(11, (String) Date.getDateFormatString());
				ps.setString(12, (String) Date1.getDateFormatString());
				ps.setString(13, (String) ttime.getText());
				ps.setString(14, (String) ttime1.getText());
				ps.setString(15, (String) tslot.getText());
				ps.setString(16, (String) ttot.getText());
				ps.execute();
				ps.close();
				JOptionPane.showMessageDialog(null, "Data Saved");
				JOptionPane.showMessageDialog(this, "Add Successfully");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			tout.setText("");
			tout1.setText("");
		}
			
			
			// pabalik ng parking inventory
			park_inventory_management app = new park_inventory_management();
			app.setTitle("Parking Inventory Management");
			app.setVisible(true);
			app.setSize(1000, 700);
			app.setVisible(true);
			app.setLocationRelativeTo(null);
			parking_inventory_management_add_button.this.dispose();

		
		

		if (e.getSource() == reset1) {
			String def = "";
			tid.setText(def);
			tfnam.setText(def);
			tout.setText(def);
			date.setSelectedIndex(0);
			month.setSelectedIndex(0);
			year.setSelectedIndex(0);
			combo.setSelectedIndex(0);
		}

	}

//main
	static parking_inventory_management_add_button app = new parking_inventory_management_add_button();

	public static void main(String[] args) {
		app.setTitle("Parking Inventory Management");
		app.setSize(1200, 750);
		app.setVisible(true);
		app.setLocationRelativeTo(null);
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}

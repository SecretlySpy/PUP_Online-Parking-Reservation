package adminmanagement;

import javax.swing.*;

import com.toedter.calendar.JDateChooser;

import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class billingmanagement extends JFrame implements ActionListener{
	
	public JLabel photo, title, invoc, cusdet, cusnam, contact, email, add, cardet,
				  platno, brand, slotno, date, paymeth, tax, hour, parkrate;
	
	public JTextField tinvoc, tcusnam, tcontact, temail, tadd, tplatno, tbrand, tslotno,
					  ttax, tdiscount, tparkrate;
	
	public JComboBox <?> pay;
	public JButton submit, logout, total, menu1;
	
	Font f3 = new Font("Times New Roman", Font.PLAIN,15);
	
	private com.toedter.calendar.JDateChooser Date; 
	private ImageIcon icon;
	private JLabel label;
	private JLabel lblClock;
	
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

	
public billingmanagement() { 
	
	icon = new ImageIcon("src/m.jpg");

	Container con = getContentPane();
	con.setLayout(null);
	
	con.add(lblClock = new JLabel(""));
	lblClock.setBounds(720,10,300,120);
	lblClock.setFont(f3);

	
	photo= new JLabel() {
	  public void paintComponent(Graphics g) {
	    g.drawImage(icon.getImage(), 0, 0, null);
	    super.paintComponent(g);
	  }
	};
	
	photo.setOpaque(false);
	con.add(photo);
	
	
	
		Date = new JDateChooser();
		//photo = new JLabel ("/billingmanagement/imahe/m.jpg");
		title = new JLabel ("Archim's TechCorner");
		invoc = new JLabel ("Invoice #:");
		cusdet = new JLabel ("----------------------------CUSTOMER DETAILS-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		cusnam = new JLabel ("Customer Name:");
		contact = new JLabel ("Customer No.:");
		email = new JLabel ("E-mail:");
		add = new JLabel ("Address:");
		cardet = new JLabel ("----------------------------CAR DETAILS------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		platno = new JLabel ("Plate #:");
		brand = new JLabel ("Brand:");
		slotno = new JLabel ("Slot #:");
		date = new JLabel ("Date of Parked:");
		paymeth = new JLabel ("Payment Method:");
		tax = new JLabel ("VAT:");
		hour = new JLabel ("No. of Hour/s:");
		parkrate = new JLabel ("Park Rate:");
		
		tinvoc = new JTextField ();
		tcusnam = new JTextField ();
		tcontact = new JTextField ("+63 ");
		temail = new JTextField ();
		tadd = new JTextField ();
		tplatno = new JTextField ();
		tbrand = new JTextField ();
		tslotno = new JTextField ();
	
		ttax = new JTextField ();
		tdiscount = new JTextField ();
		tparkrate = new JTextField ();
		
		String Select[] 
				= { "Select -/-","Cash", "GCash", "7/11 Clique", "Credit/Debit Card", 
					"Paymaya", "Beep"};
		pay = new JComboBox(Select); 
		
		submit = new JButton("Submit");
		logout = new JButton("Logout");
		total = new JButton("Total");
		menu1 = new JButton ("Back to Menu");
		
		submit.addActionListener(this);
		logout.addActionListener(this);
		total.addActionListener(this);
		menu1.addActionListener(this);
		
		 
	     //photo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/billingmanagement/imahe/m.jpg"))); 
	     photo.setBounds(10,10,100,100);
		title.setBounds(100,-10,200,100);
		invoc.setBounds(10,100,200,10);
		cusdet.setBounds(10,150,1000,10);
		cusnam.setBounds(10,200,100,10);
		contact.setBounds(10,250,100,10);
		email.setBounds(10,300,100,10);
		add.setBounds(10,350,100,10);
		cardet.setBounds(10,400,1000,10);
		platno.setBounds(10,450,100,10);
		brand.setBounds(10,500,100,10);
		slotno.setBounds(10,550,100,10);
		date.setBounds(550,100,100,10);
		paymeth.setBounds(550,200,100,20);
		tax.setBounds(550,450,100,10);
		hour.setBounds(550,500,150,10);
		parkrate.setBounds(550,550,100,10);
	
		tinvoc.setBounds(150,90,300,30);
		tcusnam.setBounds(150,190,300,30);
		tcontact.setBounds(150,240,300,30);
		temail.setBounds(150,290,300,30);
		tadd.setBounds(150,340,300,30);
		tplatno.setBounds(150,440,300,30);
		tbrand.setBounds(150,490,300,30);
		tslotno.setBounds(150,540,300,30);
		Date.setBounds(650,90,300,30);
		ttax.setBounds(650,440,300,30);
		tdiscount.setBounds(650,490,300,30);
		tparkrate.setBounds(650,540,300,30);
	
		pay.setBounds(670,200,200,30);
	
		logout.setBounds(850,10,100,40);
		menu1.setBounds(690,10,130,40);
		total.setBounds(650,600,100,40);
		submit.setBounds(720,250,100,40);
		
	   // con.add(photo);
		con.add(title);
		con.add(invoc);
		con.add(cusdet);
		con.add(cusnam);
		con.add(contact);
		con.add(email);
		con.add(add);
		con.add(cardet);
		con.add(platno);
		con.add(brand);
		con.add(slotno);
		con.add(date);
		con.add(paymeth);
		con.add(tax);
		con.add(hour);
		con.add(parkrate);
		
		con.add(tinvoc);
		con.add(tcusnam);
		con.add(tcontact);
		con.add(temail);
		con.add(tadd);
		con.add(tplatno);
		con.add(tbrand);
		con.add(tslotno);
		con.add(Date);
		con.add(ttax);
		con.add(tdiscount);
		con.add(tparkrate);
		
		con.add(pay);
		
		con.add(submit);
		con.add(logout);
		con.add(total);
		con.add(menu1);
		
		 Clock();
	}

@Override
public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	//paglogout
		if (e.getSource() == logout) {
			int dialog = JOptionPane.showConfirmDialog(null, "Are you sure you want to LOGOUT?", "WARNING",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialog == JOptionPane.YES_OPTION) {
				 //pabalik ng login
				        login app = new login();
				        app.setTitle("Admin Login");
				        app.setVisible(true);
				        app.setSize(450,600); 
				        app.setVisible(true);
				    	app.setLocationRelativeTo(null);
				        billingmanagement.this.dispose();
				     
			}
			if (dialog == JOptionPane.NO_OPTION) {
				billingmanagement.this.show();
			}
		}
		
	//para sa date
		
		//pagbalik sa menu
	    if (e.getSource() == menu1) {
	    	//menu
	    	  menu app = new menu();
		        app.setTitle("Admin Menu");
		        app.setVisible(true);
		        app.setSize(1000,400); 
		        app.setVisible(true);
		    	app.setLocationRelativeTo(null);
		    	billingmanagement.this.dispose();
	    }
	
}
	
	




static billingmanagement app = new billingmanagement();
public static void main(String[] args) {
	app.setTitle("Billing Management");
	app.setSize(1000, 700);
	app.setVisible(true);
	app.setLocationRelativeTo(null);
	app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
}
}


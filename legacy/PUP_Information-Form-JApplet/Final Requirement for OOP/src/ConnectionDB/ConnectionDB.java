
package ConnectionDB;
import java.sql.*;
public class ConnectionDB {
static String driver = "com.mysql.cj.jdbc.Driver";
static String url ="jdbc:mysql://localhost:3306/informationdb";
static String username = "root";
static String password = "kakashijirachisohaer";
public static Connection getConnection(){
 Connection conn=null;
 try{
 Class.forName(driver);
 conn=DriverManager.getConnection(url,username,password);
 }catch(Exception er){System.out.println(er);}
 return conn;
}
}
package Model.MySqlDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDatabase {
    private static Connection connection;
    public static String connectString;
    public static String userName;
    public static String password;

    public MySqlDatabase(){
        connectString = "jdbc:mysql://localhost/jdbctest?autoReconect=true&useSSL=false";
        userName = "root";
        password = "root";
    }

    public void ConnectToDatabase(){
        try {
            connection = DriverManager.getConnection(connectString, userName, password);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
}

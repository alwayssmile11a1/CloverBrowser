package Model.MySqlDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDatabase {
    private static MySqlDatabase instance;
    public static MySqlDatabase getInstance(){
        if (instance == null)
            instance = new MySqlDatabase();
        return instance;
    }
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    private String connectString;

    public String getConnectString() {
        return connectString;
    }

    private String userName;

    public String getUserName() {
        return userName;
    }

    private String password;

    public String getPassword() {
        return password;
    }

    public MySqlDatabase(){
        connectString = "jdbc:mysql://localhost/webhistory?autoReconect=true&useSSL=false";
        userName = "root";
        password = "root";
        ConnectToDatabase();
    }

    public void ConnectToDatabase(){
        try {
            connection = DriverManager.getConnection(connectString, userName, password);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void Disconnect(){
            try {
                connection.close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
    }
}

/*
package Model.MySqlDatabase;

import java.sql.*;

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
                instance = null;

            }
            catch (SQLException e){
                e.printStackTrace();
            }
    }

    public boolean CreateTableIfUnexists(){
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema = 'webhistory' AND table_name = 'history' LIMIT 1");
            if (resultSet.next()){
                return true;
            }

        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
*/

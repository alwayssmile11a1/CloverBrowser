package Model.SqliteDatabase;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SQLiteDatabase {
    private static SQLiteDatabase instance;
    public static SQLiteDatabase getInstance(){
        if (instance == null)
            instance = new SQLiteDatabase();
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

    public SQLiteDatabase(){
        connectString = "jdbc:sqlite:webhistory.db";
        ConnectToDatabase();
    }

    public void ConnectToDatabase(){
        try {
            connection = DriverManager.getConnection(connectString);
            System.out.println("Connection to SQLite has been established.");
            boolean createTable = CreateTableIfUnexists();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            try{
                if (connection == null)
                    connection.close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
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

    public String getTableName(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMyyyy");
        return "month"+simpleDateFormat.format(new Date());
    }

    public boolean CreateTableIfUnexists(){
        try{
            String query = "create table if not exists " + getTableName() + "(\n" +
                    "url varchar(100),\n" +
                    "accessdate date,\n" +
                    "accesstime time,\n" +
                    "title varchar(100),\n" +
                    "domain varchar(30),\n" +
                    "constraint PK_HISTORY primary key (url, accessdate, accesstime)\n" +
                    ");";
            Statement statement = connection.createStatement();
            statement.execute(query);
            System.out.println("CreateTable successfully");
            return true;
        }
        catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return false;
    }
}

package Model.SqliteDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SqliteDatabaseBookmarks {
    private static SqliteDatabaseBookmarks instance;
    public static SqliteDatabaseBookmarks getInstance(){
        if (instance == null)
            instance = new SqliteDatabaseBookmarks();
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

    public SqliteDatabaseBookmarks(){
        connectString = "jdbc:sqlite:bookmarks.db";
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

    public boolean CreateTableIfUnexists(){
        try{
            String query = "create table if not exists 'bookmarks'" + "(\n" +
                    "title varchar(100),\n"+
                    "url varchar(100),\n" +
                    "constraint PK_HISTORY primary key (url)\n" +
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

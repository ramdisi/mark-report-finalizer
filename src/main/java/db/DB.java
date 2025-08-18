package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private Connection conn;
    private DB(){
        //avoid instance generating
    }
    public Connection getConnection (){
        if (conn == null){
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mark-report-finalizer");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return conn;
    }
}

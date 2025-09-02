package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static Connection conn;
    private DB(){
        //avoid instance generating
    }
    public static Connection getConnection (){
        if (conn == null){
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:mark_report_finalizer.db");
                conn.prepareStatement("create table IF NOT EXISTS exam (examNo char(4),date date,subject char(30),teacherName char(80),title char(100),primary key(examNo,subject) )").execute();
                conn.prepareStatement("create table IF NOT EXISTS student (name char(80),regNo char(10),examNo char(4),primary key (regNo,examNo) )").execute();
                conn.prepareStatement("create table IF NOT EXISTS subject_marks(regNo char(10),subject char(30),mark double(6,2),examNo char(4),primary key(regNo,subject,examNo) )").execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return conn;
    }
}

package controller;
import db.DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewMarkSheetController {

    @FXML
    private DatePicker date_dateOfExam;

    @FXML
    private TextField txt_examTitle;

    @FXML
    private TextArea txt_subjectNames;

    @FXML
    private TextField txt_teacherName;

    private Stage stage = new Stage();

    @FXML
    void btn_createMarkSheet_OnClick(ActionEvent event) {
        Connection con = DB.getConnection();
        String examNo;
        try {
            ResultSet resultSet = con.prepareStatement("select examNo from exam order by examNo desc limit 1").executeQuery();
            if (resultSet.next()){
                examNo = String.format("E%03d",Integer.parseInt(resultSet.getString("examNo").substring(1))+1);
            }else{
                examNo="E001";
            }
            String [] subjectArray = txt_subjectNames.getText().split(",");
            for (String subject : subjectArray){
                PreparedStatement preparedStatement = con.prepareStatement("insert into exam values(?,?,?,?,?)");
                preparedStatement.setString(1,examNo);
                preparedStatement.setObject(2,date_dateOfExam.getValue());
                preparedStatement.setString(3,subject);
                preparedStatement.setString(4,txt_teacherName.getText());
                preparedStatement.setString(5,txt_examTitle.getText());
                preparedStatement.execute();
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/add-student-mark-window.fxml"))));
                stage.show();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

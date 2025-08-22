package controller;

import db.DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.dto.Subject;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddStudentMarkController implements Initializable {
    @FXML
    private Button btn_addDetails;

    @FXML
    private Button btn_addMarks;

    @FXML
    private Button btn_finish;

    @FXML
    private TableColumn<?, ?> column_SubjectsMarks;

    @FXML
    private TableColumn<?, ?> column_name;

    @FXML
    private TableColumn<?, ?> column_regNo;

    @FXML
    private TableView<?> marks_tableView;

    @FXML
    private Label label_subjectMarks;

    @FXML
    private Label label_examNo;

    @FXML
    private TextField txt_name;

    @FXML
    private TextField txt_regNo;

    @FXML
    private TextField txt_subjectMark;

    private Connection con;

    private String examNo;

    private ResultSet resultSet;

    private ArrayList<Subject> subjectArrayList= new ArrayList<>();

    private int currentSubjectIndex;

    @FXML
    void btn_addDetails_OnAction(ActionEvent event) {
        String name = txt_name.getText();
        String regNo = txt_regNo.getText();
        try {
            con.prepareStatement("insert into student values ('"+name+"','"+regNo+"','"+examNo+"')").execute();
            for (int i = 0; i < subjectArrayList.size(); i++) {
                con.prepareStatement("insert into subject_marks values ('"+regNo+"','"+subjectArrayList.get(i).getName()+"',"+subjectArrayList.get(i).getMarks()+")").execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        txt_subjectMark.setText(null);
        txt_name.setText(null);
        txt_regNo.setText(null);
        btn_addDetails.setDisable(true);
        btn_addMarks.setDisable(false);
        currentSubjectIndex=0;
        label_subjectMarks.setText("Marks For "+subjectArrayList.getFirst().getName());
        for (int i = 0; i < subjectArrayList.size(); i++) {
            subjectArrayList.get(i).setMarks(null);//set null for another student
        }
    }

    @FXML
    void btn_addMarksSubject_OnAction(ActionEvent event) {
        Double marks;
        try {
            marks = Double.parseDouble(txt_subjectMark.getText());
        } catch (NumberFormatException e) {
            marks=null;
        }
        subjectArrayList.get(currentSubjectIndex).setMarks(marks);
        if (currentSubjectIndex == subjectArrayList.size()-1){
            btn_addMarks.setDisable(true);
            btn_addDetails.setDisable(false);
        }else{
            label_subjectMarks.setText("Marks For "+subjectArrayList.get(++currentSubjectIndex).getName());
            txt_subjectMark.setText(null);
        }

    }

    @FXML
    void btn_finish_OnAction(ActionEvent event) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DB.getConnection();
        try {
            resultSet = con.prepareStatement("select examNo,subject from exam where examNo = (select examNo from exam order by examNo desc limit 1)").executeQuery();//get last exam Details
            while (resultSet.next()){
                examNo = resultSet.getString("examNo");
                subjectArrayList.add(new Subject(null,resultSet.getString("subject")));
            }
            label_subjectMarks.setText("Marks For "+subjectArrayList.getFirst().getName());
            label_examNo.setText("Exam Number : "+examNo);
            btn_addDetails.setDisable(true);
            currentSubjectIndex = 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

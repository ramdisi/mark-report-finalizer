package controller;

import db.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.dto.StudentMarks;
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
    private TableView<StudentMarks> marks_tableView;

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

    private ObservableList<StudentMarks> studentMarksObservableList = FXCollections.observableArrayList();

    private static Stage currentStage;

    @FXML
    void btn_addDetails_OnAction(ActionEvent event) {
        String name = txt_name.getText();
        String regNo = txt_regNo.getText();
        try {
            con.prepareStatement("insert into student values ('"+name+"','"+regNo+"','"+examNo+"')").execute();
            for (int i = 0; i < subjectArrayList.size(); i++) {
                con.prepareStatement("insert into subject_marks values ('"+regNo+"','"+subjectArrayList.get(i).getName()+"',"+subjectArrayList.get(i).getMarks()+",'"+examNo+"')").execute();
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
        loadTable();
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
        currentStage.close();
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

    private void loadTable(){
        try {
            resultSet = con.prepareStatement("select s.regNo,name,subject,mark from subject_marks as sm,student as s where sm.regNo=s.regNo and sm.examNo='" + examNo+"'").executeQuery();
            while (resultSet.next()){
                int index = isExists(resultSet.getString("regNo"));
                String mark = resultSet.getObject("mark")==null?"absent":resultSet.getString("mark");
                if (index==-1) {
                    studentMarksObservableList.add(new StudentMarks(
                            resultSet.getString("regNo"),
                            resultSet.getString("subject") + " : " + mark,
                            resultSet.getString("name")
                    ));
                }else {
                    studentMarksObservableList.get(index).setSubjectMarkString(resultSet.getString("subject"),mark);
                }
            }
            column_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            column_regNo.setCellValueFactory(new PropertyValueFactory<>("regNo"));
            column_SubjectsMarks.setCellValueFactory(new PropertyValueFactory<>("subjectMarkString"));
            marks_tableView.setItems(studentMarksObservableList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private int isExists(String regNo){
        for (int i = 0; i < studentMarksObservableList.size(); i++) {
            if (studentMarksObservableList.get(i).getRegNo().equals(regNo)){
                return i;
            }
        }
        return -1;
    }
    public static void setCurrentStage(Stage stage){
        currentStage = stage;
    }
}

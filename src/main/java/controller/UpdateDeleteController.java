package controller;

import db.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.dto.StudentMarks;
import model.dto.Subject;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class UpdateDeleteController implements Initializable {

    @FXML
    private Button btn_addMarks;

    @FXML
    private Button btn_delete;

    @FXML
    private Button btn_save;

    @FXML
    private ComboBox<String> cbox_exams;

    @FXML
    private TableColumn<?, ?> column_name;

    @FXML
    private TableColumn<?, ?> column_regNo;

    @FXML
    private TableColumn<?, ?> column_subjectMarks;

    @FXML
    private Label label_subjectMarks;

    @FXML
    private RadioButton radio_addNew;

    @FXML
    private RadioButton radio_editSelected;

    @FXML
    private TableView<StudentMarks> tableview_editedDetails;

    @FXML
    private TextField txt_name;

    @FXML
    private TextField txt_regNo;

    @FXML
    private TextField txt_subjectMarks;

    @FXML
    private ToggleGroup user_preference;

    private Connection con;

    private ObservableList<StudentMarks> studentMarksObservableList = FXCollections.observableArrayList();

    private ResultSet resultSet;

    private String selectedExamNo,selectedRegNo;

    private ArrayList<Subject> subjectArrayList= new ArrayList<>();

    private ArrayList<Subject> selectedSubjectArrayList= new ArrayList<>();

    private int currentSubjectIndex;
    private MouseEvent event;

    @FXML
    void btn_onAction_delete(ActionEvent event) {
        try {
            con.prepareStatement("delete from student where regNo='"+selectedRegNo+"' and examNo='"+selectedExamNo+"'").execute();
            con.prepareStatement("delete from subject_marks where regNo='"+selectedRegNo+"' and examNo='"+selectedExamNo+"'").execute();
            loadTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btn_onAction_addMarks(ActionEvent event) {
        Double marks;
        try {
            marks = Double.parseDouble(txt_subjectMarks.getText());
        } catch (NumberFormatException | NullPointerException e) {
            marks=null;
        }
        selectedSubjectArrayList.get(currentSubjectIndex).setMarks(marks);
        if (currentSubjectIndex == selectedSubjectArrayList.size()-1){
            btn_addMarks.setDisable(true);
        }else{
            label_subjectMarks.setText("Marks For "+selectedSubjectArrayList.get(++currentSubjectIndex).getName());
            txt_subjectMarks.setText(selectedSubjectArrayList.get(currentSubjectIndex).getMarks()==null?"Absent":selectedSubjectArrayList.get(currentSubjectIndex).getMarks().toString());
        }
    }

    @FXML
    void btn_onAction_save(ActionEvent event) {
        String name = txt_name.getText();
        if (radio_addNew.isSelected()){
            String regNo = txt_regNo.getText();
            try {
                con.prepareStatement("insert into student values ('"+name+"','"+regNo+"','"+selectedExamNo+"')").execute();
                for (int i = 0; i < selectedSubjectArrayList.size(); i++) {
                    con.prepareStatement("insert into subject_marks values ('"+regNo+"','"+subjectArrayList.get(i).getName()+"',"+subjectArrayList.get(i).getMarks()+",'"+selectedExamNo+"')").execute();
                }
                loadTable();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                con.prepareStatement("update student set name='" + name + "' where regNo='" + selectedRegNo + "' and examNo='" + selectedExamNo + "'").executeUpdate();
                for (int i = 0; i < selectedSubjectArrayList.size(); i++) {
                    con.prepareStatement("update subject_marks set mark=" + selectedSubjectArrayList.get(i).getMarks() + " where regNo='" + selectedRegNo + "' and examNo='" + selectedExamNo + "' and subject='" + selectedSubjectArrayList.get(i).getName() + "'").executeUpdate();
                }
                cbox_onAction_selectExam(null);//use this method bcz loadTable doesnt work
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void cbox_onAction_selectExam(ActionEvent event) {
        tableview_editedDetails.getItems().clear();
        selectedExamNo = cbox_exams.getValue().split(" ")[0];
        loadTable();
    }

    @FXML
    void onMouseClick_savePreference(MouseEvent event) {
        if (radio_addNew.isSelected()){
            txt_regNo.setEditable(true);
            txt_subjectMarks.setText(null);
            txt_name.setText(null);
            txt_regNo.setText(null);
            selectedSubjectArrayList = (ArrayList<Subject>)subjectArrayList.clone();
        }else {
            txt_regNo.setEditable(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DB.getConnection();
        try {
            resultSet = con.prepareStatement("select distinct examNo,title from exam").executeQuery();
            ObservableList<String> cboxObservableList = FXCollections.observableArrayList();
            while(resultSet.next()){
                cboxObservableList.add(resultSet.getString("examNo")+" - "+resultSet.getString("title"));
            }
            cbox_exams.setItems(cboxObservableList);
            radio_addNew.setDisable(true);
            radio_editSelected.setDisable(true);
            btn_delete.setDisable(true);
            btn_addMarks.setDisable(true);
            btn_save.setDisable(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTable(){
        try {
            subjectArrayList.clear();
            resultSet = con.prepareStatement("select s.regNo,name,subject,mark from subject_marks as sm,student as s where sm.regNo=s.regNo and sm.examNo='" + selectedExamNo+"'").executeQuery();
            while (resultSet.next()){
                int index = isExists(resultSet.getString("regNo"));
                String mark = resultSet.getObject("mark")==null?"Absent":resultSet.getString("mark");
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
            resultSet = con.prepareStatement("select subject from exam where examNo='"+selectedExamNo+"'").executeQuery();
            while(resultSet.next()){
                subjectArrayList.add(new Subject(null,resultSet.getString("subject")));
            }
            selectedSubjectArrayList = (ArrayList<Subject>)subjectArrayList.clone();
            radio_addNew.setDisable(false);
            radio_editSelected.setDisable(false);
            btn_delete.setDisable(false);
            btn_addMarks.setDisable(false);
            btn_save.setDisable(false);
            radio_addNew.setSelected(true);
            txt_regNo.setEditable(true);
            txt_subjectMarks.setText(null);
            txt_name.setText(null);
            txt_regNo.setText(null);
            label_subjectMarks.setText("Marks For "+subjectArrayList.get(currentSubjectIndex=0).getName());
            column_name.setCellValueFactory(new PropertyValueFactory<>("name"));
            column_regNo.setCellValueFactory(new PropertyValueFactory<>("regNo"));
            column_subjectMarks.setCellValueFactory(new PropertyValueFactory<>("subjectMarkString"));
            tableview_editedDetails.setItems(studentMarksObservableList);
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

    public void table_onClicked_selectDetails(MouseEvent mouseEvent) {
        selectedRegNo = tableview_editedDetails.getSelectionModel().getSelectedItem().getRegNo();
        if (radio_editSelected.isSelected()) {
            selectedSubjectArrayList.clear();
            try {
                resultSet = con.prepareStatement("select name,sm.regNo,subject,mark from student as s,subject_marks as sm where sm.examNo ='" + selectedExamNo + "' and sm.regNo='" + selectedRegNo + "' and s.regNo=sm.regNo and s.examNo=sm.examNo").executeQuery();
                while (resultSet.next()) {
                    txt_regNo.setText(resultSet.getString("regNo"));
                    txt_name.setText(resultSet.getString("name"));
                    selectedSubjectArrayList.add(new Subject((Double) resultSet.getObject("mark"), resultSet.getString("subject")));
                }
                label_subjectMarks.setText("Marks For " + selectedSubjectArrayList.getFirst().getName());
                txt_subjectMarks.setText(selectedSubjectArrayList.get(currentSubjectIndex).getMarks()==null?"Absent":selectedSubjectArrayList.get(currentSubjectIndex).getMarks().toString());
                currentSubjectIndex = 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

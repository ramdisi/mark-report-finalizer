package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewMarkSheetController {

    @FXML
    private DatePicker date_dateOfExam;

    @FXML
    private TextField txt_examTitle;

    @FXML
    private TextArea txt_subjectNames;

    @FXML
    private TextField txt_teacherName;

    @FXML
    void btn_createMarkSheet_OnClick(ActionEvent event) {

    }

}

package controller;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;
import db.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import model.dto.Exam;
import model.dto.RankedStudent;
import model.dto.Subject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PDFGenerateController implements Initializable {

    @FXML
    private Button btn_generatePDF;

    @FXML
    private TableColumn<?, ?> column_date;

    @FXML
    private TableColumn<?, ?> column_subjects;

    @FXML
    private TableColumn<?, ?> column_teacherName;

    @FXML
    private TableColumn<?, ?> column_title;

    @FXML
    private Label lable_filePath;

    @FXML
    private TableView<Exam> tableView_examTable;

    private Connection con;

    private ResultSet resultSet;

    private ObservableList<Exam> examObserverList = FXCollections.observableArrayList();

    private Exam selectedExam;

    private ObservableList<RankedStudent> rankedStudentObservableList = FXCollections.observableArrayList();

    @FXML
    void btn_onAction_generatePDF(ActionEvent event) {
        lable_filePath.setText("Please wait ... ");
        fetchAndRankStudents();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        try {
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(selectedExam.getTitle().toUpperCase()+" - "+selectedExam.getDate()+" -  "+selectedExam.getTeacherName().toUpperCase());
            contentStream.endText();
            float margin = 50;
            float yStart = 700;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            BaseTable table = new BaseTable(yPosition, yStart, 20, tableWidth, margin, document, page, true, true);
            Row<PDPage> headerRow = table.createRow(15f);
            headerRow.createCell(10, "Regestration No");
            headerRow.createCell(10, "student Name");
            for (String subjectName : selectedExam.getSubjects().split(",")){
                headerRow.createCell(10, subjectName);
            }
            headerRow.createCell(10, "Total");
            headerRow.createCell(10, "Rank");
            int rank = 1;
            for (RankedStudent student : rankedStudentObservableList){
                Row<PDPage> row = table.createRow(12f);
                row.createCell(10, student.getRegNo());
                row.createCell(10, student.getName());
                for(Subject subjectMarks : student.getSubjectMarks()){
                    row.createCell(10, subjectMarks.getMarks()==null?"Absent":subjectMarks.getMarks().toString());
                }
                row.createCell(10, student.getTotal().toString());
                row.createCell(10, rank+++"");
            }
            table.draw();
            contentStream.close();
            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File file = new File(desktopPath, selectedExam.getDate()+"-"+selectedExam.getTitle()+" Result Sheet.pdf");
            document.save(file);
            document.close();
            lable_filePath.setText("Your file is ready as named : "+selectedExam.getDate()+"-"+selectedExam.getTitle()+" Result Sheet.pdf"+" on your Desktop Folder");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void fetchAndRankStudents() {
        try {
            resultSet = con.prepareStatement("select name,s.regNo,subject,mark from student as s,subject_marks as sm where s.regNo=sm.regNo and s.examNo=sm.examNo and sm.examNo='"+selectedExam.getExamNo()+"'").executeQuery();
            while(resultSet.next()){
                int index = isStudentExists(resultSet.getString("regNo"));
                if(index==-1){
                    rankedStudentObservableList.add(new RankedStudent(
                            resultSet.getString("regNo"),
                            resultSet.getString("name"),
                            FXCollections.observableArrayList(new Subject((Double) resultSet.getObject("mark"),resultSet.getString("subject"))),
                            0.0
                    ));
                }else {
                    rankedStudentObservableList.get(index).addSubjectMarks(new Subject((Double) resultSet.getObject("mark"),resultSet.getString("subject")));
                }
            }
            for (int i = 0; i < rankedStudentObservableList.size()-1; i++) {
                for (Subject subject : rankedStudentObservableList.get(i).getSubjectMarks()) {
                    Double mark = subject.getMarks()==null?0.0:subject.getMarks();
                    rankedStudentObservableList.get(i).setTotal(rankedStudentObservableList.get(i).getTotal()+mark);
                }
            }
            for (int j = rankedStudentObservableList.size()-1; j >0 ; j--) {
                for (int i = 0; i < j-1; i++) {
                    if (rankedStudentObservableList.get(i).getTotal()<rankedStudentObservableList.get(i+1).getTotal()){
                        RankedStudent temp = rankedStudentObservableList.get(i+1);
                        rankedStudentObservableList.set(i+1,rankedStudentObservableList.get(i));
                        rankedStudentObservableList.set(i,temp);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void table_onMouseClick_selectExam(MouseEvent event) {
        selectedExam  = tableView_examTable.getSelectionModel().getSelectedItem();
        lable_filePath.setText(selectedExam.getDate()+" "+selectedExam.getTitle()+" is Selected to Generate");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        con = DB.getConnection();
        try {
            resultSet = con.prepareStatement("select examNo,date,subject,teacherName,title from exam").executeQuery();
            while(resultSet.next()){
                int index = isExamExists(resultSet.getString("examNo"));
                if (index==-1){
                    examObserverList.add(new Exam(
                            resultSet.getString("examNo"),
                            resultSet.getString("teacherName"),
                            resultSet.getString("title"),
                            resultSet.getString("subject"),
                            resultSet.getString("date")
                    ));
                }else{
                    examObserverList.get(index).setSubjects(resultSet.getString("subject"));
                }
            }
            column_date.setCellValueFactory(new PropertyValueFactory<>("date"));
            column_subjects.setCellValueFactory(new PropertyValueFactory<>("subjects"));
            column_title.setCellValueFactory(new PropertyValueFactory<>("title"));
            column_teacherName.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
            tableView_examTable.setItems(examObserverList);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private int isExamExists(String examNo){
        for (int i = 0; i < examObserverList.size(); i++) {
            if (examObserverList.get(i).getExamNo().equals(examNo)){
                return i;
            }
        }
        return -1;
    }
    private int isStudentExists(String regNo){
        for (int i = 0; i < rankedStudentObservableList.size(); i++) {
            if (rankedStudentObservableList.get(i).getRegNo().equals(regNo)){
                return i;
            }
        }
        return -1;
    }
}

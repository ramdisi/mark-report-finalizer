package model.dto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Setter
@Getter
@ToString
public class RankedStudent {
    private String regNo,name;
    private ObservableList<Subject> subjectMarks;
    private Double total;
    //private Double zScore;
    public void addSubjectMarks(Subject subject){
        subjectMarks.add(subject);
    }
}

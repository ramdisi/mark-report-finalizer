package model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Exam {
    private String examNo,teacherName,title,subjects,date = "";
    public void setSubjects(String subject){
        subjects+=","+subject;
    }
}

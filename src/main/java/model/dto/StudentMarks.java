package model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentMarks {
    private String regNo;
    private String subjectMarkString = "";
    private String name;
    public void setSubjectMarkString(String subject,String mark){
        subjectMarkString+= " , "+subject+" : "+mark;
    }
}

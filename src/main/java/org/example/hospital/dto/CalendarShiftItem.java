package org.example.hospital.dto;

public class CalendarShiftItem {

    private Long departmentId;
    private String departmentName;
    private String summary;
    private Integer headcount;

    public CalendarShiftItem(Long departmentId,
                             String departmentName,
                             String summary,
                             Integer headcount) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.summary = summary;
        this.headcount = headcount;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getHeadcount() {
        return headcount;
    }
}

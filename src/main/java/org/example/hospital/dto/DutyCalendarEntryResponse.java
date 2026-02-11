package  org.example.hospital.dto;

import java.time.LocalDate;

public class DutyCalendarEntryResponse {

    private Long id;
    private LocalDate date;
    private Long departmentId;
    private String departmentName;
    private String summary;
    private Integer headcount;

    public DutyCalendarEntryResponse(Long id,
                                     LocalDate date,
                                     Long departmentId,
                                     String departmentName,
                                     String summary,
                                     Integer headcount) {
        this.id = id;
        this.date = date;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.summary = summary;
        this.headcount = headcount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
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

package org.example.hospital.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "duty_calendar_entries")
public class DutyCalendarEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 256, nullable = false)
    private String summary;

    @Column(nullable = false)
    private Integer headcount = 0;

    protected DutyCalendarEntry() {
    }

    public DutyCalendarEntry(LocalDate date, Department department, String summary, Integer headcount) {
        this.date = date;
        this.department = department;
        this.summary = summary;
        this.headcount = headcount == null ? 0 : headcount;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Department getDepartment() {
        return department;
    }

    public String getSummary() {
        return summary;
    }

    public Integer getHeadcount() {
        return headcount;
    }
}

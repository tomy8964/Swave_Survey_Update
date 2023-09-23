package com.example.surveyanswer.survey.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class DateManagement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Date_id")
    private long id;

    @Column(name = "survey_start_date")
    @NotNull
    private Date startDate;

    @Column(name = "survey_deadline")
    @NotNull
    private Date deadline;

    @Column(name = "survey_enable")
    private boolean isEnabled;

    @Builder
    public DateManagement(Date startDate, Date deadline, Boolean isEnabled) {
        this.startDate = startDate;
        this.deadline = deadline;
        this.isEnabled = isEnabled;
    }

    // RequestDto -> Entity
    public static DateManagement dateRequestToEntity(Date start, Date end) {
        return DateManagement.builder()
                .startDate(start)
                .deadline(end)
                .build();
    }
}

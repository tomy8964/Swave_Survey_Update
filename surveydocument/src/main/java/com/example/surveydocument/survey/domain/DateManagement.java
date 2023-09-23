package com.example.surveydocument.survey.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
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
    private Boolean isEnabled;
    @Builder
    public DateManagement(Boolean isEnabled, @NotNull Date startDate, @NotNull Date deadline) {
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

package com.example.surveydocument.survey.repository.date;

import com.example.surveydocument.survey.domain.DateManagement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DateRepository extends JpaRepository<DateManagement, Long> {
}

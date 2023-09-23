package com.example.surveydocument.survey.repository.choice;

import com.example.surveydocument.survey.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ChoiceRepository extends JpaRepository<Choice, Long>, ChoiceRepositoryCustom {
}

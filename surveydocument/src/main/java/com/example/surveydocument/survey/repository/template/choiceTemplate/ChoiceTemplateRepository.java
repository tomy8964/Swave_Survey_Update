package com.example.surveydocument.survey.repository.template.choiceTemplate;

import com.example.surveydocument.survey.domain.Choice;
import com.example.surveydocument.survey.domain.ChoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceTemplateRepository extends JpaRepository<ChoiceTemplate, Long>, ChoiceTemplateRepositoryCustom {

}

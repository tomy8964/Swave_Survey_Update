//package com.example.surveydocument.survey.service;
//
//import org.hibernate.cfg.Environment;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionManager;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//import org.springframework.transaction.annotation.TransactionManagementConfigurer;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableTransactionManagement
//public class TransactionManagerConfig {
//    private PlatformTransactionManager transactionManager;
//
//    public void setTransactionManager(PlatformTransactionManager transactionManager){
//        this.transactionManager = transactionManager;
//    }
//
//    public void transactionCode() throws Exception {
//        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
//
//        try {
//            // execute some SQL statements...
//            this.transactionManager.commit(status);
//        } catch (RuntimeException e) {
//            this.transactionManager.rollback(status);
//            throw e;
//        }
//    }
//}

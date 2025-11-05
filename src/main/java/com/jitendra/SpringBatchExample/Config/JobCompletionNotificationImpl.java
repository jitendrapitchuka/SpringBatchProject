package com.jitendra.SpringBatchExample.Config;

import java.util.logging.Logger;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationImpl implements JobExecutionListener {

    private Logger logger = Logger.getLogger(JobCompletionNotificationImpl.class.getName());

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Job Started: " + jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("Job Ended: " + jobExecution.getJobInstance().getJobName() + " with status: " + jobExecution.getStatus());
    }

}

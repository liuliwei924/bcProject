package org.bc.web.action.schedule;

import org.bc.web.action.schedule.task.FumiTaskJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {

	@Autowired
	public FumiTaskJob fumiTaskJob;
	
	@Scheduled(cron="0 0/1 * * * ?")
	public void fumiTask(){
		fumiTaskJob.execJob();
	}
}

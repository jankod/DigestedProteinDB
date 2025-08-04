package hr.pbf.digestdb.workflow.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Data
@Slf4j
public class JobLauncher {

	private final JobContext context;
	private List<JobExecution<?>> jobs = new ArrayList<>();

	private ExecutorService executorService;

	public JobLauncher(JobContext context) {
		this.context = context;
		executorService = Executors.newFixedThreadPool(8);
	}

	public void runAll() {
		if(executorService.isShutdown() || executorService.isTerminated()) {
			throw new RuntimeException("Executor service is shutdown.");
		}

		for(JobExecution<?> jobExecution : jobs) {
			if(jobExecution.getStatus() == JobExecution.Status.NOT_STARTED) {
				jobExecution.setStatus(JobExecution.Status.STARTED);
			} else {
				continue;
			}
			Future<?> future = executorService.submit(jobExecution);
			jobExecution.setFuture(future);
		}
	}

	public void addJob(Job job) {
		jobs.add(new JobExecution<>(context, job));
	}

	public boolean waitForAll() throws InterruptedException {
		return executorService.awaitTermination(4, TimeUnit.SECONDS);
	}



}

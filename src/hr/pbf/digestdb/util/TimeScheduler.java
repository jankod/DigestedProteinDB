package hr.pbf.digestdb.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeScheduler {

	private static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

	public static void runEveryHour(Runnable r) {
		
		ses.scheduleAtFixedRate(r, 0, 1, TimeUnit.HOURS);
	}

	public static void runEvery10Minutes(Runnable r) {
		ses.scheduleAtFixedRate(r, 0, 10, TimeUnit.MINUTES);
	}

	public static void stopAll() {
		ses.shutdownNow();
	}

	public static void runEvery1Minutes(Runnable r) {
		ses.scheduleAtFixedRate(r, 0, 1, TimeUnit.MINUTES);		
	}

}

package br.com.cas10.pgman.worker

import groovy.util.logging.Log4j;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.cas10.pgman.service.PGManagerDAO;
import br.com.cas10.pgman.service.PostgresqlService;
import br.com.cas10.pgman.service.SchedulerService;

@Component
@Log4j
class CleanerWorker extends Worker {

	@Autowired
	private SchedulerService schedulerService;

	CleanerWorker() {
		super("cleaner", "0 0 0 * * ?")
	}

	@Override
	public void run() {
		log.info("Cleaner Worker - Running");
		for (Worker worker : schedulerService.getWorkers()) {
			worker.clean();
		}
	}
}
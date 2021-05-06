package com.pfizer.equip.computeservice.containers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wha.docker.client.DockerClient;
import com.wha.docker.client.DockerClientImpl;
import com.wha.docker.engine.EngineResponse;

public class ContainerCloser {
	private static Logger log = LoggerFactory.getLogger(ContainerCloser.class);
	private LinkedBlockingQueue<ContainerRun> runs = new LinkedBlockingQueue<>();
	private DockerClient dockerClient;
	private ExecutorService service;
	private boolean running = false;
	
	public ContainerCloser() throws MalformedURLException {
		dockerClient = new DockerClientImpl();
		service = Executors.newCachedThreadPool();
	}
	
	public boolean add(String containerId, String userId) {
		return add(new ContainerRun(containerId, userId));
	}
	
	public boolean add(ContainerRun run) {
		return runs.add(run);
	}
	
	public void start() {
		if (!running) {
			service.execute(() -> {
				while (true) {
					process();
				}
			});
			running = true;
		}
	}
	
	public void stop() {
		if (!service.isShutdown()) {
			service.shutdown();
		}
		running = false;
	}
	
	private void process() {
		try {
			ContainerRun run = runs.take();
			service.execute(() -> processRun(run));
		} catch (InterruptedException e) {
			// Log that there was an exception
			log.error("An exception occurred: ", e);
		}
	}

	private void processRun(ContainerRun run) {
		try {
			// Unpause the container
			dockerClient.unpause(run.getContainerId());
		} catch (IOException | GeneralSecurityException e) {
			// Log that there was an exception
			log.error("An exception occurred unpausing container : ", e);
		}
		try {
			// Stop the container
			EngineResponse er = dockerClient.stop(run.getContainerId());
			int status = er.getStatus().getCode();
			if (status == 404) {
				String warning = String.format("Container %s not found", run.getContainerId());
				log.warn(warning);
				return;
			} else if (status == 304) {
				String warning = String.format("Container %s is not running", run.getContainerId());
				log.warn(warning);
			} else if (status < 200 || status > 299) {
				// Log that there was a problem
				String err = String.format("Docker returned %d : %s", status, er.getStatus().getText()); 
				log.error(err);
				// return run to the queue
				runs.add(run);
				return;
			} 
			// Remove the container
			er = dockerClient.rm(run.getContainerId());
			status = er.getStatus().getCode();
			if (status < 200 || status > 299) {
				if (status == 404) {
					String warning = String.format("Container %s not found", run.getContainerId());
					log.warn(warning);
				} else {
					// Log that there was a problem
					String err = String.format("Docker returned %d : %s", status, er.getStatus().getText()); 
					log.error(err);
					// return run to the queue
					runs.add(run);
				}
			}
		} catch (IOException | GeneralSecurityException e) {
			// Log that there was an exception
			log.error("An exception occurred: ", e);
			// return run to the queue
			runs.add(run);
		}
	}
}

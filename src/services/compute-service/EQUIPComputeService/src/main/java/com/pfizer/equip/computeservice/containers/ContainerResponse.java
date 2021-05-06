package com.pfizer.equip.computeservice.containers;

import java.io.InputStream;
import java.util.concurrent.Future;

import com.wha.docker.engine.EngineResponse;
import com.wha.docker.engine.EngineResponseStatus;
import com.wha.docker.engine.OkResponseCallback;

public class ContainerResponse extends EngineResponse {
	private EngineResponse engineResponse;
	private String stdin;

	public ContainerResponse(EngineResponse engineResponse) {
		this.engineResponse = engineResponse;
	}
	
	public Object getContent() {
		return engineResponse.getContent();
	}

	public Object getContentLength() {
		return engineResponse.getContentLength();
	}

	public Object getContentType() {
		return engineResponse.getContentType();
	}

	public Object getHeaders() {
		return engineResponse.getHeaders();
	}

	public String getMimeType() {
		return engineResponse.getMimeType();
	}

	public OkResponseCallback getResponseCallback() {
		return engineResponse.getResponseCallback();
	}

	public EngineResponseStatus getStatus() {
		return engineResponse.getStatus();
	}

	public InputStream getStream() {
		return engineResponse.getStream();
	}

	public Future<?> getTaskFuture() {
		return engineResponse.getTaskFuture();
	}

	public String getStdin() {
		return stdin;
	}

	public void setStdin(String stdin) {
		this.stdin = stdin;
	}
	
}

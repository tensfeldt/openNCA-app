package com.pfizer.pgrd.equip.dataframe.dto;

public class Batch extends Assembly {
	public static final String ENTITY_TYPE = Assembly.BATCH_TYPE;
	private String stdOut;
	private String stdErr;
	private String stdIn;
	
	public Batch() {
		super();
		this.setAssemblyType(Assembly.BATCH_TYPE);
		this.setEntityType(Batch.ENTITY_TYPE);
	}

	public String getStdOut() {
		return stdOut;
	}

	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}

	public String getStdErr() {
		return stdErr;
	}

	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}

	public String getStdIn() {
		return stdIn;
	}

	public void setStdIn(String stdIn) {
		this.stdIn = stdIn;
	}
}

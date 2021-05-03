package com.pfizer.equip.computeservice.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

import com.pfizer.equip.computeservice.dto.Executor;
import com.pfizer.equip.computeservice.dto.WorkingExecutionDto;
import com.pfizer.equip.computeservice.exception.ComputeDataAccessException;
import com.pfizer.equip.computeservice.exception.ComputeException;
import com.pfizer.equip.computeservice.sql.ComputeSql;


public class ComputeDatabaseDAO {
	private static final String EQUIP_DATASOURCE = "java:/datasources/equipDS";

	public Connection getConnection() throws NamingException, SQLException {
		Context context = new InitialContext();
        DataSource ds = (DataSource) context.lookup(EQUIP_DATASOURCE);
        return ds.getConnection();
	}

	public List<Executor> getExecutionFromId(String id) throws NamingException, SQLException, ComputeException, ComputeDataAccessException {
		Connection conn = getConnection();
		try {
			ComputeSql sql = new ComputeSql(conn);
			return convertToExecutions(sql.getExecutionsFromId(id));
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	public List<Executor> getExecutionFromName(String name) throws NamingException, SQLException, ComputeException, ComputeDataAccessException {
		Connection conn = getConnection();
		try {
			ComputeSql sql = new ComputeSql(conn);
			return convertToExecutions(sql.getExecutionsFromName(name));
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	public List<Executor> getAllExecutions() throws NamingException, SQLException, ComputeException, ComputeDataAccessException {
		Connection conn = getConnection();
		try {
			ComputeSql sql = new ComputeSql(conn);
			return convertToExecutions(sql.getAllExecutions());
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	public List<Executor> convertToExecutions(List<WorkingExecutionDto> workingExecutions) {
		
		Map<String, Executor> executionsMap = new HashMap<>();
		
		for(WorkingExecutionDto we: workingExecutions) {
			if(executionsMap.containsKey(we.getId())) {
				List<String> tempStringList = executionsMap.get(we.getId()).getScriptType();
				tempStringList.add(we.getScriptType());
				executionsMap.get(we.getId()).setScriptType(tempStringList);
			}
			else {
				Executor e = new Executor();
				
				List<String> scriptType = new ArrayList<>();
				scriptType.add(we.getScriptType());
								
				e.setId(we.getId());
				e.setName(we.getName());
				e.setRepositoryUrl(we.getRepositoryUrl());
				e.setType(we.getType());
				e.setVersion(we.getVersion());
				e.setScriptType(scriptType);
				e.setCommand(we.getCommand());
				e.setDisabled(we.getDisabled().equalsIgnoreCase("T"));
				e.setDescription(we.getDescription());
				executionsMap.put(we.getId(), e);
			}
		}
		
		List<Executor> executions = new ArrayList<>(executionsMap.values());
		return executions;
	}

}

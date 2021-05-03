package com.pfizer.equip.computeservice.sql;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.pfizer.equip.computeservice.dao.DaoQueryLoader;
import com.pfizer.equip.computeservice.dto.WorkingExecutionDto;
import com.pfizer.equip.computeservice.exception.ComputeDataAccessException;
import com.pfizer.equip.computeservice.exception.ComputeException;

public class ComputeSql extends BaseResultSetHandlerSql<WorkingExecutionDto>{

	public ComputeSql(Connection conn)
	{
		super(conn, new BeanListHandler<WorkingExecutionDto>(WorkingExecutionDto.class));
	}
	
	
	public List<WorkingExecutionDto> getExecutionsFromId(String id)
			throws ComputeException, ComputeDataAccessException
	{			
		
		String sql = DaoQueryLoader.getQuery("SELECT_ENGINE_BY_ID");
		List<WorkingExecutionDto> executions = getList(sql, new Object[] { id });
		
		return executions;
	}
	
	public List<WorkingExecutionDto> getExecutionsFromName(String name)
			throws ComputeException, ComputeDataAccessException
	{			
		
		String sql = DaoQueryLoader.getQuery("SELECT_ENGINE_BY_NAME");
		List<WorkingExecutionDto> executions = getList(sql, new Object[] { name });
		
		return executions;
	}
	
	public List<WorkingExecutionDto> getAllExecutions()
			throws ComputeException, ComputeDataAccessException
	{			
		
		String sql = DaoQueryLoader.getQuery("SELECT_ALL_ENGINES");
		List<WorkingExecutionDto> executions = getList(sql);
		
		return executions;
	}

}

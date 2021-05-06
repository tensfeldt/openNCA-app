package com.pfizer.pgrd.equip.modeshape.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pfizer.pgrd.equip.modeshape.utils.DateUtils;

public class Row {
	private Map<String, List<String>> data;
	
	public Row() {
		this.data = new HashMap<String, List<String>>();
	}
	
	public Map<String, List<String>> getData() {
		return this.data;
	}
	
	public String getString(String columnName) {
		String s = null;
		List<String> l = this.getStringList(columnName);
		if(l != null && l.size() == 1) {
			s = l.get(0);
		}
		
		return s;
	}
	
	public String[] getStringArray(String columnName) {
		String[] a = null;
		List<String> l = this.getStringList(columnName);
		if(l != null) {
			a = l.toArray(new String[0]);
		}
		
		return a;
	}
	
	public List<String> getStringList(String columnName) {
		List<String> s = null;
		if(this.data.containsKey(columnName)) {
			s = this.data.get(columnName);
		}
		else {
			//throw new IllegalArgumentException("No column with name '" + columnName + "' could be found.");
		}
		
		return s;
	}
	
	public Double getDouble(String columnName) {
		List<Double> l = this.getDoubleList(columnName);
		Double d = null;
		if(l != null && l.size() == 1) {
			d = l.get(0);
		}
		
		return d;
	}
	
	public Double[] getDoubleArray(String columnName) {
		List<Double> l = this.getDoubleList(columnName);
		Double[] a = null;
		if(l != null) {
			a = l.toArray(new Double[0]);
		}
		
		return a;
	}
	
	public List<Double> getDoubleList(String columnName) {
		List<Double> list = null;
		List<String> l = this.getStringList(columnName);
		if(l != null) {
			list = new ArrayList<>();
			for(String s : l) {
				if(s != null) {
					list.add(Double.parseDouble(s));
				}
			}
		}
		
		return list;
	}
	
	public Long getLong(String columnName) {
		List<Long> list = this.getLongList(columnName);
		Long l = null;
		if(list != null && list.size() == 1) {
			l = list.get(0);
		}
		
		return l;
	}
	
	public Long[] getLongArray(String columnName) {
		List<Long> l = this.getLongList(columnName);
		Long[] a = null;
		if(l != null) {
			a = l.toArray(new Long[0]);
		}
		
		return a;
	}
	
	public List<Long> getLongList(String columnName) {
		List<String> l = this.getStringList(columnName);
		List<Long> list = null;
		if(l != null) {
			list = new ArrayList<>();
			for(String s : l) {
				if(s != null) {
					list.add(Long.parseLong(s));
				}
			}
		}
		
		return list;
	}
	
	public Boolean getBoolean(String columnName) {
		List<Boolean> list = this.getBooleanList(columnName);
		Boolean b = null;
		if(list != null && list.size() == 1) {
			b = list.get(0);
		}
		
		return b;
	}
	
	public Boolean[] getBooleanArray(String columnName) {
		List<Boolean> lb = this.getBooleanList(columnName);
		Boolean[] ba = null;
		if(lb != null) {
			ba = lb.toArray(new Boolean[0]);
		}
		
		return ba;
	}
	
	public List<Boolean> getBooleanList(String columnName) {
		List<String> sl = this.getStringList(columnName);
		List<Boolean> list = null;
		if(sl != null) {
			list = new ArrayList<>();
			for(String s : sl) {
				if(s != null) {
					list.add(Boolean.parseBoolean(s));
				}
			}
		}
		
		return list;
	}
	
	public Date getDate(String columnName) {
		List<Date> list = this.getDateList(columnName);
		Date d = null;
		if(list != null && list.size() == 1) {
			d = list.get(0);
		}
		
		return d;
	}
	
	public Date[] getDateArray(String columnName) {
		List<Date> list = this.getDateList(columnName);
		Date[] a = null;
		if(list != null) {
			a = list.toArray(new Date[0]);
		}
		
		return a;
	}
	
	public List<Date> getDateList(String columnName) {
		List<String> ds = this.getStringList(columnName);
		List<Date> list = null;
		if(ds != null) {
			list = new ArrayList<>();
			for(String s : ds) {
				if(s != null) {
					Date d = DateUtils.parseDate(s);
					list.add(d);
				}
			}
		}
		
		return list;
	}
}
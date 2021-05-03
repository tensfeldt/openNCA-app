package com.pfizer.pgrd.equip.dataframe.dto.atr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Protocol {
	private String protocol;
	private String title;
	private Date setupDate;
	private String setupBy;
	private List<ProtocolContact> contacts = new ArrayList<>();
	private List<Alias> aliases = new ArrayList<>();
	
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getSetupDate() {
		return setupDate;
	}
	public void setSetupDate(Date setupDate) {
		this.setupDate = setupDate;
	}
	public String getSetupBy() {
		return setupBy;
	}
	public void setSetupBy(String setupBy) {
		this.setupBy = setupBy;
	}
	public List<ProtocolContact> getContacts() {
		return contacts;
	}
	public void setContacts(List<ProtocolContact> contacts) {
		this.contacts = contacts;
	}
	public List<Alias> getAliases() {
		return aliases;
	}
	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}
	
	public static class Alias {
		private String type;
		private String alias;
		
		public Alias() {
			this(null, null);
		}
		
		public Alias(String type, String alias) {
			this.type = type;
			this.alias = alias;
		}
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getAlias() {
			return alias;
		}
		public void setAlias(String alias) {
			this.alias = alias;
		}
	}
}


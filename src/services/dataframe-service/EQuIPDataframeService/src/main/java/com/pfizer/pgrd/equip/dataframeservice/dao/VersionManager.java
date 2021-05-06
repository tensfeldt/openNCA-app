package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.List;

import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipID;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipVersionable;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;

/**
 * This class exposes methods for handling all processes for version control, such as auditing and notification.
 * @author QUINTJ16
 *
 */
public class VersionManager {
	private static final String PUT = "PUT", GET = "GET";
	
	public void lockEntity(String entityId, String userId) {
		EquipObject entity = this.getEntity(entityId);
		this.lockEntity(entity, userId);
	}
	
	public void lockEntity(EquipObject entity, String userId) {
		if(entity == null || userId == null) {
			return;
		}
		
		
	}
	
	public void lockEntity(List<EquipObject> entities, String userId) {
		
	}
	
	public void unlockEntity(String entityId, String userId) {
		
	}
	
	public void unlockEntity(EquipObject entity, String userId) {
		
	}
	
	public void unlockEntity(List<EquipObject> entities, String userId) {
		
	}
	
	public void deleteEntity(String entityId, String userId) {
		
	}
	
	public void deleteEntity(EquipObject entity, String userId) {
		
	}
	
	public void deleteEntity(List<EquipObject> entities, String userId) {
		
	}
	
	public void undeleteEntity(String entityId, String userId) {
		
	}
	
	public void undeleteEntity(EquipObject entity, String userId) {
		
	}
	
	public void undeleteEntity(List<EquipObject> entities, String userId) {
		
	}
	
	public void commitEntity(String nodeId, String userId) {
		ModeShapeDAO msDao = new ModeShapeDAO();
		EquipObject node = msDao.getEquipObject(nodeId);
		this.commitEntity(node, userId);
	}
	
	public void commitEntity(List<EquipObject> entity, String userId) {
		
	}
	
	/**
	 * Commits the provided node, performing all necessary actions.
	 * @param node
	 */
	public void commitEntity(EquipObject entity, String userId) {
		// We only need to peform actions if the node is not committed, not deleted, and not superseded.
		if(entity instanceof EquipVersionable) {
			EquipVersionable node = (EquipVersionable) entity;
			if(node != null && !node.isCommitted() && !node.isDeleteFlag() && !node.getVersionSuperSeded()) {
				// COMMIT THE NODE
				node.setCommitted(true);
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty(ModeShapeNode.CommonProperties.COMMIT_FLAG, true);
				pp.addProperty(ModeShapeNode.CommonProperties.LOCKED_BY_USER, null);
				pp.addProperty(ModeShapeNode.CommonProperties.LOCK_FLAG, false);
				
				ModeShapeDAO msDao = new ModeShapeDAO();
				msDao.updateNode(((EquipObject)node).getId(), pp, true);
				
				// HANDLE SIBLINGS
				if(node instanceof EquipID) {
					String equipId = ((EquipID) node).getEquipId();
					String nodeId = ((EquipObject) node).getId();
					
					EquipIDDAO eidDao = ModeShapeDAO.getEquipIDDAO();
					List<EquipObject> nodes = eidDao.getItem(equipId);
					
					// Go through each node with the same EQUIP ID.
					// If the node has the same version number as the one we're committing (but is not the one we're committing),
					// delete it.
					for(EquipObject n : nodes) {
						EquipVersionable vn = (EquipVersionable) n;
						if(vn.getVersionNumber() == node.getVersionNumber() && !n.getId().equals(nodeId)) {
							vn.setDeleteFlag(true);
							pp = new PropertiesPayload();
							pp.addProperty(ModeShapeNode.CommonProperties.DELETE_FLAG, true);
							
							msDao.updateNode(n.getId(), pp);
						}
					}
				}
				
				// CREATE AUDIT
				// TODO Create audit entry
				
				// SEND NOTIFICATIONS
				// TODO Send notifications during commit
				if(node instanceof Analysis) {
					Analysis an = (Analysis) node;
				}
				else if(node instanceof Assembly) {
					Assembly a = (Assembly) node;
				}
				else if(node instanceof Dataframe) {
					Dataframe df = (Dataframe) node;
				}
			}
		}
	}
	
	public void supersedeEntity(String nodeId, String userId) {
		ModeShapeDAO msDao = new ModeShapeDAO();
		EquipObject node = msDao.getEquipObject(nodeId);
		this.supersedeEntity(node, userId);
	}
	
	/**
	 * Supersedes the provided node, performing all necessary actions.
	 * @param node
	 */
	public void supersedeEntity(EquipObject node, String userId) {
		// TODO Supersede node
	}
	
	public void supersedeEntity(List<EquipObject> entities, String userId) {
		
	}
	
	public void updateEntity(String entityId, String userId, PropertiesPayload properties) {
		
	}
	
	public void updateEntity(EquipObject entity, String userId, PropertiesPayload properties) {
		
	}
	
	private EquipObject getEntity(String entityId) {
		if(entityId != null) {
			ModeShapeDAO msDao = new ModeShapeDAO();
			return msDao.getEquipObject(entityId);
		}
		
		return null;
	}
	
	/**
	 * Returns the {@link EquipVersionable} object whose EQUIP ID matches the one provided and who is committed and not deleted.
	 * @param equipId
	 * @return {@link EquipVersionable}
	 */
	public EquipVersionable getLatestNode(String equipId) {
		EquipIDDAO eiDao = ModeShapeDAO.getEquipIDDAO();
		List<EquipObject> nodes = eiDao.getItem(equipId);
		
		return this.getLatestNode(nodes);
	}
	
	private EquipVersionable getLatestNode(List<EquipObject> nodes) {
		EquipVersionable max = null;
		if(nodes != null) {
			for(EquipObject node : nodes) {
				if(node instanceof EquipVersionable) {
					EquipVersionable ev = (EquipVersionable) node;
					
					// If the node is committed, not superseded, not deleted, and has a greater version number, set it to the max
					if(max == null || (ev.getVersionNumber() > max.getVersionNumber() && !ev.getVersionSuperSeded() && ev.isCommitted() && !ev.isDeleteFlag())) {
						max = ev;
					}
				}
			}
		}
		
		return max;
	}
	
	/**
	 * Returns the next version number in the history of the node's EQUIP ID.
	 * @param nodeId
	 * @return {@code long}
	 */
	public long getNextVersionNumberByNodeId(String nodeId) {
		ModeShapeDAO msDao = new ModeShapeDAO();
		EquipObject node = msDao.getEquipObject(nodeId);
		if(node != null && node instanceof EquipVersionable) {
			this.getNextVersionNumber((EquipVersionable) node);
		}
		
		return 1;
	}
	
	/**
	 * Returns the next version number in the history of the provided EQUIP ID.
	 * @param equipId
	 * @return {@code long}
	 */
	public long getNextVersionNumberByEquipId(String equipId) {
		return 1;
	}
	
	/**
	 * Returns the next version number in the provided node's EQUIP ID history.
	 * @param node
	 * @return {@code long}
	 */
	public long getNextVersionNumber(EquipVersionable node) {
		if(node instanceof EquipID) {
			String equipId = ((EquipID) node).getEquipId();
			if(equipId != null) {
				EquipIDDAO eidDao = ModeShapeDAO.getEquipIDDAO();
				List<EquipObject> nodes = eidDao.getItem(equipId);
				
				EquipObject max = null;
				
			}
		}
		
		return 1;
	}
}

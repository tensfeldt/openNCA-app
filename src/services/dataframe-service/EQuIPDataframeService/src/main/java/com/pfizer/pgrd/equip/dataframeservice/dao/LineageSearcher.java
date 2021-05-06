package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;

public class LineageSearcher {
	private static final int NODE_ID = 0, EQUIP_ID = 1;
	private LineageSearcher() {}
	
	/**
	 * Returns all paths from the provided lineage that lead to the specified node ID, ending with the node itself.
	 * @param lineage
	 * @param nodeId
	 * @return {@link List}<{@link AssemblyLineageItem}>
	 */
	public static final List<AssemblyLineageItem> getPaths(List<AssemblyLineageItem> lineage, String nodeId) {
		return LineageSearcher.getPaths(lineage, nodeId, NODE_ID);
	}
	
	/**
	 * Returns all paths, starting with the provided lineage root, that lead to the specified node ID, ending with the node itself. Returns 
	 * {@code null} if no paths exist.
	 * @param lineage
	 * @param nodeId
	 * @return {@link AssemblyLineageItem}
	 */
	public static final AssemblyLineageItem getPaths(AssemblyLineageItem lineage, String nodeId) {
		return LineageSearcher.getPaths(lineage, nodeId, NODE_ID);
	}
	
	/**
	 * Returns all paths from the provided lineage that lead to the specified EQUIP ID, ending with the node itself.
	 * @param lineage
	 * @param equipId
	 * @return {@link List}<{@link AssemblyLineageItem}>
	 */
	public static final List<AssemblyLineageItem> getPathsByEquipId(List<AssemblyLineageItem> lineage, String equipId) {
		return LineageSearcher.getPaths(lineage, equipId, EQUIP_ID);
	}
	
	/**
	 * Returns all paths, starting with the provided lineage root, that lead to the specified EQUIP ID, ending with the node itself. Returns 
	 * {@code null} if no paths exist.
	 * @param lineage
	 * @param equipId
	 * @return {@link AssemblyLineageItem}
	 */
	public static final AssemblyLineageItem getPathsByEquipId(AssemblyLineageItem lineage, String equipId) {
		return LineageSearcher.getPaths(lineage, equipId, EQUIP_ID);
	}
	
	/**
	 * Returns a <@link Map> object of all {@link LinegaeItem} objects within the provided lineage using their node ID as the key.
	 * @param lineage
	 * @return {@link Map}<{@link String}, {@link LineageItem}>
	 */
	public static final Map<String, LineageItem> createTable(AssemblyLineageItem lineage) {
		return LineageSearcher.createEquipIdTable(Arrays.asList(lineage));
	}
	
	/**
	 * Returns a <@link Map> object of all {@link LinegaeItem} objects within the provided lineage using their node ID as the key.
	 * @param lineage
	 * @return {@link Map}<{@link String}, {@link LineageItem}>
	 */
	public static final Map<String, LineageItem> createTable(List<AssemblyLineageItem> lineage) {
		return LineageSearcher.createTable(lineage, NODE_ID);
	}
	
	/**
	 * Returns a <@link Map> object of all {@link LinegaeItem} objects within the provided lineage using their EQUIP ID as the key.
	 * @param lineage
	 * @return {@link Map}<{@link String}, {@link LineageItem}>
	 */
	public static final Map<String, LineageItem> createEquipIdTable(AssemblyLineageItem lineage) {
		return LineageSearcher.createEquipIdTable(Arrays.asList(lineage));
	}
	
	/**
	 * Returns a {@link Map} object of all {@link LinegaeItem} objects within the provided lineage using their EQUIP ID as the key.
	 * @param lineage
	 * @return {@link Map}<{@link String}, {@link LineageItem}>
	 */
	public static final Map<String, LineageItem> createEquipIdTable(List<AssemblyLineageItem> lineage) {
		return LineageSearcher.createTable(lineage, EQUIP_ID);
	}
	
	/**
	 * Returns all paths starting with the provided {@link AssemblyLineageItem} object that contain the provided EQUIP ID.
	 * @param root
	 * @param equipId
	 * @return {@link AssemblyLineageItem}
	 */
	public static final AssemblyLineageItem getContainingPathsByEquipId(AssemblyLineageItem root, String equipId) {
		LineageItem path = LineageSearcher.traverse(root, equipId, EQUIP_ID, false, true);
		if(path != null) {
			return (AssemblyLineageItem)path;
		}
		
		return null;
	}
	
	/**
	 * Returns all paths using the provided {@link AssemblyLineageItem} objects as roots that contain the provided EQUIP ID.
	 * @param roots
	 * @param equipId
	 * @return {@link List}<{@link AssemblyLineageItem}>
	 */
	public static final List<AssemblyLineageItem> getContainingPathsByEquipId(List<AssemblyLineageItem> roots, String equipId) {
		Map<String, List<AssemblyLineageItem>> map = LineageSearcher.getContainingPathsByEquipId(roots, Arrays.asList(equipId));
		List<AssemblyLineageItem> rroots = map.get(equipId);
		if(rroots == null) {
			rroots = new ArrayList<>();
		}
		
		return rroots;
	}
	
	/**
	 * Returns all paths starting with the provided {@link AssemblyLineageItem} object that contain at least one of the provided EQUIP IDs. 
	 * The keys of the resulting {@link Map} are the EQUIP IDs.
	 * @param root
	 * @param equipIds
	 * @return {@link Map}<{@link String}, {@link AssemblyLineageItem}>
	 */
	public static final Map<String, AssemblyLineageItem> getContainingPathsByEquipId(AssemblyLineageItem root, List<String> equipIds) {
		Map<String, AssemblyLineageItem> rmap = new HashMap<>();
		Map<String, List<AssemblyLineageItem>> map = LineageSearcher.getContainingPathsByEquipId(Arrays.asList(root), equipIds);
		for(String equipId : equipIds) {
			List<AssemblyLineageItem> roots = map.get(equipId);
			if(roots != null && roots.size() == 1) {
				rmap.put(equipId, roots.get(0));
			}
		}
		
		return rmap;
	}
	
	/**
	 * Returns all paths using the provided {@link AssemblyLineageItem} objects as roots that contain at least one of the provided EQUIP IDs. 
	 * The keys of the resulting {@link Map} are the EQUIP IDs.
	 * @param roots
	 * @param equipIds
	 * @return {@link Map}<{@link String}, {@link List}<{@link AssemblyLineageItem}>>
	 */
	public static final Map<String, List<AssemblyLineageItem>> getContainingPathsByEquipId(List<AssemblyLineageItem> roots, List<String> equipIds) {
		Map<String, List<AssemblyLineageItem>> map = new HashMap<>();
		if(roots != null && equipIds != null) {
			for(String equipId : equipIds) {
				List<AssemblyLineageItem> paths = new ArrayList<>();
				for(AssemblyLineageItem root : roots) {
					if(root != null) {
						 AssemblyLineageItem path = LineageSearcher.getContainingPathsByEquipId(root, equipId);
						 if(path != null) {
							 paths.add(path);
						 }
					}
				}
				
				map.put(equipId, paths);
			}
		}
		
		return map;
	}
	
	public static final LineageItem getItemAsRoot(List<AssemblyLineageItem> lineage, String nodeId) {
		return LineageSearcher.getAsRoot(lineage, nodeId, NODE_ID);
	}
	
	public static final LineageItem getItemAsRootByEquipId(List<AssemblyLineageItem> lineage, String equipId) {
		return LineageSearcher.getAsRoot(lineage, equipId, EQUIP_ID);
	}
	
	/**
	 * Returns all leaf nodes of the provided {@link AssemblyLineageItem}.
	 * @param root
	 * @return {@link List}<{@link LineageItem}>
	 */
	public static final List<LineageItem> getLeaves(AssemblyLineageItem root) {
		List<LineageItem> leaves = new ArrayList<>();
		if(root != null) {
			LineageSearcher.getLeaves(root, leaves);
		}
		
		return leaves;
	}
	
	/**
	 * 
	 * @param roots
	 * @return
	 */
	public static final Map<String, List<LineageItem>> getLeavesByEquipId(List<AssemblyLineageItem> roots) {
		Map<String, List<LineageItem>> map = new HashMap<>();
		if(roots != null) {
			for(AssemblyLineageItem root : roots) {
				List<LineageItem> leaves = LineageSearcher.getLeaves(root);
				map.put(root.getEquipId(), leaves);
			}
		}
		
		return map;
	}
	
	/**
	 * Returns a {@link List} object of {@link DataframeLineageItem} objects that are the first promoted data transformations of each path.
	 * @param roots
	 * @return {@link List}<{@link DataframeLineageItem}>
	 */
	public static final List<DataframeLineageItem> getFirstPromotedDataTransformations(List<AssemblyLineageItem> roots) {
		Finder finder = new Finder() {
			private List<LineageItem> results = new ArrayList<>();
			
			@Override
			public boolean check(LineageItem item) {
				boolean cont = true;
				if(item instanceof DataframeLineageItem) {
					DataframeLineageItem dli = (DataframeLineageItem)item;
					if(dli.getDataframeType().equalsIgnoreCase(Dataframe.DATA_TRANSFORMATION_TYPE) && dli.getPromotionStatus().equalsIgnoreCase("promoted")) {
						this.results.add(dli);
						cont = false;
					}
				}
				
				return cont;
			}

			@Override
			public List<LineageItem> getResults() {
				return this.results;
			}
			
		};
		
		for(AssemblyLineageItem root : roots) {
			LineageSearcher.traverse(root, finder);
		}
		
		List<DataframeLineageItem> list = new ArrayList<>();
		for(LineageItem item : finder.getResults()) {
			list.add((DataframeLineageItem)item);
		}
		
		return list;
	}
	
	/**
	 * Returns the {@link LineageItem} object within the provided lineage (including attachments) whose UUID matches the one provided. Returns {@code null} if no such object could be found.
	 * @param lineage
	 * @param id
	 * @return {@link LineageItem}
	 */
	public static final LineageItem getItem(List<AssemblyLineageItem> lineage, String id) {
		LineageItem item = null;
		if(lineage != null) {
			for(AssemblyLineageItem root : lineage) {
				if(root != null) {
					item = LineageSearcher.quickFind(root, id);
					if(item != null) {
						break;
					}
				}
			}
		}
		
		return item;
	}
	
	private static final LineageItem quickFind(LineageItem item, String id) {
		if(item != null) {
			if(item.getId().equals(id)) {
				return item;
			}
			
			for(DataframeLineageItem attachment : item.getAttachments()) {
				if(attachment.getId().equals(id)) {
					return attachment;
				}
			}
			
			if(item instanceof AssemblyLineageItem) {
				AssemblyLineageItem ali = (AssemblyLineageItem)item;
				for(DataframeLineageItem member : ali.getMemberDataframes()) {
					if(member.getId().equals(id)) {
						return member;
					}
				}
			}
			
			LineageItem match = null;
			for(AssemblyLineageItem childAssembly : item.getChildAssemblies()) {
				match = LineageSearcher.quickFind(childAssembly, id);
			}
			
			if(match == null) {
				for(DataframeLineageItem childDataframe : item.getChildDataframes()) {
					match = LineageSearcher.quickFind(childDataframe, id);
				}
			}
			
			return match;
		}
		
		return null;
	}
	
	private static final void getLeaves(LineageItem item, List<LineageItem> leaves) {
		if(item != null) {
			if(item.getChildAssemblies().isEmpty() && item.getChildDataframes().isEmpty()) {
				leaves.add(item);
			}
			else {
				for(AssemblyLineageItem c : item.getChildAssemblies()) {
					LineageSearcher.getLeaves(c, leaves);
				}
				for(DataframeLineageItem c : item.getChildDataframes()) {
					LineageSearcher.getLeaves(c, leaves);
				}
			}
		}
	}
	
	private static final Map<String, LineageItem> createTable(List<AssemblyLineageItem> lineage, int keyType) {
		Map<String, LineageItem> map = new HashMap<>();
		if(lineage != null) {
			for(AssemblyLineageItem ali : lineage) {
				LineageSearcher.populateTable(map, ali, keyType);
			}
		}
		
		return map;
	}
	
	private static final void populateTable(Map<String, LineageItem> map, LineageItem item, int keyType) {
		if(map != null && item != null) {
			String id = item.getId();
			if(keyType == EQUIP_ID) {
				id = item.getEquipId();
			}
			
			map.put(id, item);
			for(AssemblyLineageItem ali : item.getChildAssemblies()) {
				LineageSearcher.populateTable(map, ali, keyType);
			}
			for(DataframeLineageItem dli : item.getChildDataframes()) {
				LineageSearcher.populateTable(map, dli, keyType);
			}
			
			if(item instanceof AssemblyLineageItem) {
				AssemblyLineageItem ali = (AssemblyLineageItem) item;
				if(ali.getAssemblyType().equalsIgnoreCase(Assembly.BATCH_TYPE)) {
					for(DataframeLineageItem dli : ali.getMemberDataframes()) {
						map.put(dli.getEquipId(), dli);
					}
				}
			}
		}
	}
	
	private static final List<AssemblyLineageItem> getPaths(List<AssemblyLineageItem> lineage, String id, int idType) {
		List<AssemblyLineageItem> paths = new ArrayList<>();
		if(lineage != null && id != null) {
			for(AssemblyLineageItem root : lineage) {
				if(root != null) {
					AssemblyLineageItem path = LineageSearcher.getPaths(root, id, idType);
					if(path != null) {
						paths.add(path);
					}
				}
			}
		}
		
		return paths;
	}
	
	private static final AssemblyLineageItem getPaths(AssemblyLineageItem lineage, String id, int idType) {
		AssemblyLineageItem path = null;
		if(lineage != null && id != null) {
			LineageItem p = LineageSearcher.traverse(lineage, id, idType);
			if(p != null) {
				path = (AssemblyLineageItem) p;
			}
		}
		
		return path;
	}
	
	private static final LineageItem getAsRoot(List<AssemblyLineageItem> lineage, String id, int idType) {
		for(AssemblyLineageItem root : lineage) {
			LineageItem item = LineageSearcher.getAsRoot(root, id, idType);
			if(item != null) {
				return item;
			}
		}
		
		return null;
	}
	
	private static final LineageItem getAsRoot(AssemblyLineageItem lineage, String id, int idType) {
		return LineageSearcher.traverse(lineage, id, idType, true, false);
	}
	
	private static final LineageItem traverse(LineageItem item, String id, int idType) {
		return LineageSearcher.traverse(item, id, idType, false, false);
	}
	
	private static final List<LineageItem> traverse(LineageItem item, Finder finder) {
		LineageSearcher.traverse(item, null, -1, false, false, finder);
		return finder.getResults();
	}
	
	private static final LineageItem traverse(LineageItem item, String id, int idType, boolean asRoot, boolean fullPath) {
		return LineageSearcher.traverse(item, id, idType, asRoot, fullPath, null);
	}
	
	private static final LineageItem traverse(LineageItem item, String id, int idType, boolean asRoot, boolean fullPath, Finder finder) {
		LineageItem clone = null;
		
		boolean cont = true;
		// If we're at the node, clone it and stop
		if((idType == NODE_ID && item.getId().equals(id)) || (idType == EQUIP_ID && item.getEquipId().equalsIgnoreCase(id))) {
			if(fullPath || asRoot) {
				clone = item.deepClone();
			}
			else {
				clone = item.clone(false);
			}
		}
		else if(finder != null) {
			cont = finder.check(item);
		}
		
		if(cont) {
			// If we're not at the node, go through all of our children to see if they are on 
			// the path to the node. If any of our children our on the path to the node, then 
			// we're on the path to the node and we clone.
			List<LineageItem> includedChildren = new ArrayList<>();
			for(AssemblyLineageItem ali : item.getChildAssemblies()) {
				LineageItem cc = LineageSearcher.traverse(ali, id, idType, asRoot, fullPath, finder);
				if(cc != null && finder == null) {
					if(asRoot) {
						clone = cc;
					}
					else {
						includedChildren.add((AssemblyLineageItem)cc);
					}
				}
			}
			for(DataframeLineageItem dli : item.getChildDataframes()) {
				LineageItem cc = LineageSearcher.traverse(dli, id, idType, asRoot, fullPath, finder);
				if(cc != null && finder == null) {
					if(asRoot) {
						clone = cc;
					}
					else {
						includedChildren.add((DataframeLineageItem)cc);
					}
				}
			}
			
			// At least one of our children is on the path to the node
			if(includedChildren.size() > 0) {
				if(item instanceof AssemblyLineageItem) {
					clone = ((AssemblyLineageItem)item).clone(false);
				}
				else if(item instanceof DataframeLineageItem) {
					clone = ((DataframeLineageItem)item).clone(false);
				}
				
				if(clone != null) {
					for(LineageItem li : includedChildren) {
						if(li instanceof AssemblyLineageItem) {
							clone.getChildAssemblies().add((AssemblyLineageItem)li);
						}
						else if(li instanceof DataframeLineageItem) {
							clone.getChildDataframes().add((DataframeLineageItem)li);
						}
					}
				}
			}
		}
		
		return clone;
	}
}

interface Finder {
	/**
	 * Adds the provided {@link LineageItem} object to the result set if it meets the implemented criteria. Returns {@code true} if the traversal 
	 * of the lineage branch should continue; {@code false} otherwise.
	 * @param item
	 * @return {@code boolean}
	 */
	public boolean check(LineageItem item);
	
	/**
	 * Returns the {@link LineageItem} objects within the result set.
	 * @return {@link List}<{@link LineageItem}>
	 */
	public List<LineageItem> getResults();
}
package com.pfizer.pgrd.equip.modeshape.node;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.modeshape.utils.DateUtils;

public class ModeShapeNode {
	private static Gson equipGson;
	protected static final Gson GENERIC_GSON = new Gson();

	@Expose
	@SerializedName(JCRProperties.JCR_PRIMARY_TYPE)
	private String primaryType = "nt:unstructured";

	@SerializedName(JCRProperties.JCR_SELF)
	private String self;

	@SerializedName(JCRProperties.JCR_UP)
	private String up;
	
	@SerializedName(JCRProperties.JCR_ID)
	private String jcrId;
	
	@Expose
	@SerializedName(JCRProperties.JCR_CHILDREN)
	private List<ModeShapeNode> children = new ArrayList<>();

	@SerializedName(JCRProperties.JCR_CREATED_BY)
	private String jcrCreatedBy;

	@SerializedName(JCRProperties.JCR_CREATED)
	private Date jcrCreated;

	@SerializedName(JCRProperties.JCR_VERSION_HISTORY)
	private String jcrVersionHistory;

	@SerializedName(JCRProperties.JCR_IS_CHECKED_OUT)
	private boolean jcrIsCheckedOut;

	@SerializedName(JCRProperties.JCR_BASE_VERSION)
	private String jcrBaseVersion;

	@SerializedName(JCRProperties.JCR_PREDECESSORS)
	private List<String> jcrPredecessors = new ArrayList<>();

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getPrimaryType() {
		return primaryType;
	}

	protected void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}

	public String getSelf() {
		return self;
	}

	protected void setSelf(String self) {
		this.self = self;
	}

	public String getUp() {
		return up;
	}

	protected void setUp(String up) {
		this.up = up;
	}

	public String getJcrId() {
		return jcrId;
	}

	protected void setJcrId(String id) {
		this.jcrId = id;
	}

	public List<ModeShapeNode> getChildren() {
		return children;
	}

	protected void setChildren(List<ModeShapeNode> children) {
		this.children = children;
	}

	public String getJcrCreatedBy() {
		return jcrCreatedBy;
	}

	protected void setJcrCreatedBy(String jcrCreatedBy) {
		this.jcrCreatedBy = jcrCreatedBy;
	}

	public Date getJcrCreated() {
		return jcrCreated;
	}

	protected void setJcrCreated(Date jcrCreated) {
		this.jcrCreated = jcrCreated;
	}

	public String getJcrVersionHistory() {
		return jcrVersionHistory;
	}

	protected void setJcrVersionHistory(String jcrVersionHistory) {
		this.jcrVersionHistory = jcrVersionHistory;
	}

	public boolean isJcrIsCheckedOut() {
		return jcrIsCheckedOut;
	}

	protected void setJcrIsCheckedOut(boolean jcrIsCheckedOut) {
		this.jcrIsCheckedOut = jcrIsCheckedOut;
	}

	public String getJcrBaseVersion() {
		return jcrBaseVersion;
	}

	protected void setJcrBaseVersion(String jcrBaseVersion) {
		this.jcrBaseVersion = jcrBaseVersion;
	}

	public List<String> getJcrPredecessors() {
		return jcrPredecessors;
	}

	protected void setJcrPredecessors(List<String> jcrPredecessors) {
		this.jcrPredecessors = jcrPredecessors;
	}
	
	/**
	 * Returns all objects in the list of children that are an instance of the
	 * provided class.
	 * 
	 * @param c
	 *            the object type
	 * @return {@link List}<{@code T}>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModeShapeNode> List<T> getChildren(Class<T> c) {
		List<T> list = new ArrayList<>();
		for (ModeShapeNode child : this.children) {
			Class childClass = child.getClass();
			if (childClass == c) {
				list.add((T) child);
			}
		}

		return list;
	}

	/**
	 * Returns all objects in the list of children whose node name matches
	 * (case-sensitive) the provided node name.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return {@link List}<{@link ModeShapeNode}> the children
	 */
	public List<ModeShapeNode> getChildren(String nodeName) {
		List<ModeShapeNode> list = new ArrayList<>();
		for (ModeShapeNode child : this.children) {
			if (child.nodeName.equals(nodeName)) {
				list.add(child);
			}
		}

		return list;
	}

	/**
	 * Returns a {@link ModeShapeNode} whose node name matched the provided node
	 * name. Returns {@code null} if no objects are found or if more than one object
	 * is found.
	 * 
	 * @param nodeName
	 *            the node name
	 * @return {@link ModeShapeNode}
	 */
	protected ModeShapeNode getChild(String nodeName) {
		ModeShapeNode child = null;
		List<ModeShapeNode> list = this.getChildren(nodeName);
		if (list.size() == 1) {
			child = list.get(0);
		}

		return child;
	}

	protected void addChild(ModeShapeNode child) {
		this.getChildren().add(child);
	}

	protected void replaceChild(String nodeName, ModeShapeNode child) {
		if (child != null) {
			child.setNodeName(nodeName);
		}

		ModeShapeNode c = this.getChild(nodeName);
		if (c != null) {
			this.children.remove(c);
		}

		this.addChild(child);
	}

	protected <T extends ModeShapeNode> T getChild(Class<T> c) {
		T child = null;
		List<T> e = this.getChildren(c);
		if (e != null && e.size() == 1) {
			child = e.get(0);
		}

		return child;
	}

	protected <T extends ModeShapeNode> void replaceChild(Class<T> c, T child) {
		T e = this.getChild(c);
		if (e != null) {
			this.getChildren().remove(e);
		}

		this.getChildren().add(child);
	}

	protected <T extends ModeShapeNode> void replaceChildren(String nodeName, List<T> children) {
		List<ModeShapeNode> list = this.getChildren(nodeName);
		this.getChildren().removeAll(list);
		this.getChildren().addAll(children);
	}

	protected <T extends ModeShapeNode> void replaceChildren(Class<T> c, List<T> children) {
		List<T> e = this.getChildren(c);
		this.getChildren().removeAll(e);

		this.getChildren().addAll(children);
	}

	/**
	 * Returns a JSON representation of this object as a {@link String}, including
	 * all of its children.
	 * 
	 * @return {@link String} the JSON representation of this object
	 */
	public String marshal() {
		return this.marshal(true, true);
	}

	/**
	 * Returns a JSON representation of this object as a {@link String}. If
	 * includeChildren is {@code true}, the children will be included in the
	 * representation.
	 * 
	 * @param includeChildren
	 *            whether to include the children or not
	 * @return {@link String} the JSON representation of this object
	 */
	public String marshal(boolean includeChildren) {
		return this.marshal(true, includeChildren);
	}

	/**
	 * Returns a JSON representation of this object as a {@link String}. The object
	 * will be placed in a wrapper object using its node name if includeWrapper is
	 * {@code true}. If includeChildren is {@code true}, the children will be
	 * included in the representation.
	 * 
	 * @param includeChildren
	 *            whether to include the children or not
	 * @param includeWrapper
	 *            whether to place in a wrapper object or not
	 * @return {@link String} the JSON representation of this object
	 */
	public String marshal(boolean includeChildren, boolean includeWrapper) {
		ModeShapeNode.initGson();
		
		JsonElement node = this.toNode(includeChildren, includeWrapper);
		return equipGson.toJson(node);
	}

	/**
	 * Returns a {@link JsonElement} rerpresenting this object, including its
	 * children.
	 * 
	 * @return {@link JsonElement} the representation of this object
	 */
	protected JsonElement toNode() {
		return this.toNode(true, true);
	}

	/**
	 * Returns a {@link JsonElement} rerpresenting this object. If includeChildren
	 * is {@code true}, the children will be included in the representation.
	 * 
	 * @param includeChildren
	 *            whether to include the children or not
	 * @return {@link JsonElement} the representation of this object
	 */
	protected JsonElement toNode(boolean includeChildren) {
		return this.toNode(includeChildren, true);
	}

	/**
	 * Returns a {@link JsonElement} rerpresenting this object. The object will be
	 * placed in a wrapper object using its node name if includeWrapper is
	 * {@code true}. If includeChildren is {@code true}, the children will be
	 * included in the representation.
	 * 
	 * @param includeChildren
	 *            whether to include the children or not
	 * @param includeWrapper
	 *            whether to place in a wrapper object or not
	 * @return {@link JsonElement} the representation of this object
	 */
	protected JsonElement toNode(boolean includeChildren, boolean includeWrapper) {
		List<ModeShapeNode> tempChildren = this.children;
		this.children = null;
		JsonElement node = equipGson.toJsonTree(this);
		this.children = tempChildren;
		
		if (includeChildren && !this.children.isEmpty()) {
			JsonArray array = new JsonArray();
			for (ModeShapeNode child : this.children) {
				JsonElement childNode = child.toNode(includeChildren, true);
				array.add(childNode);
			}
			
			node.getAsJsonObject().add(JCRProperties.JCR_CHILDREN, array);
		}
		
		if (includeWrapper) {
			JsonObject wrapper = new JsonObject();
			String wrapperName = this.nodeName;
			if(wrapperName == null) {
				wrapperName = this.generateNodeName();
			}
			
			wrapper.add(wrapperName, node);
			
			node = wrapper;
		}

		return node;
	}
	
	public String generateNodeName() {
		return this.getNodeName();
	}
	
	public static ModeShapeNode unmarshal(String json) {
		return ModeShapeNode.unmarshal(json, ModeShapeNode.class);
	}

	/**
	 * Returns an object of {@code class T} deserialized from the provided JSON.
	 * 
	 * @param json
	 *            the JSON
	 * @param classOfT
	 *            the class to return
	 * @return {@code T} the object deserialized from the JSON
	 */
	public static <T extends ModeShapeNode> T unmarshal(String json, Class<T> classOfT) {
		ModeShapeNode.initGson();
		T object = equipGson.fromJson(json, classOfT);
		return object;
	}
	
	public static <T extends ModeShapeNode> T[] unmarshal(String json, T[] a) {
		ModeShapeNode.initGson();
		
		@SuppressWarnings("unchecked")
		T[] n = (T[]) equipGson.fromJson(json, a.getClass());
		return n;
	}
	
	public EquipObject toEquipObject() {
		EquipObject eo = new EquipObject();
		eo.setId(this.getJcrId());
		
		return eo;
	}
	
	public static ModeShapeNode fromEquipObject(EquipObject eo) {
		ModeShapeNode node = null;
		if(eo != null) {
			node = new ModeShapeNode();
			node.setJcrId(eo.getId());
		}
		
		return node;
	}

	/**
	 * Initializes the custom {@link Gson} instance that will be used for all
	 * serialization/deserialization of objects and JSON.
	 */
	private static void initGson() {
		if (equipGson == null) {
			GsonBuilder gb = new GsonBuilder();
			gb.registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter());
			gb.registerTypeHierarchyAdapter(ModeShapeNode.class, new ModeShapeNodeAdapter());
			gb.registerTypeHierarchyAdapter(Date.class, new DateUtils());
			gb.excludeFieldsWithoutExposeAnnotation();
			gb.setPrettyPrinting();
			equipGson = gb.create();
		}
	}
}

/**
 * This class is designed to make all empty arrays {@code null} during
 * serialization.
 * 
 * @author QUINTJ16
 *
 */
class CollectionAdapter implements JsonSerializer<Collection<?>> {
	@Override
	public JsonElement serialize(Collection<?> src, Type srcType, JsonSerializationContext context) {
		JsonArray array = null;
		if (src != null && !src.isEmpty()) {
			array = new JsonArray();
			for (Object child : src) {
				JsonElement element = context.serialize(child);
				array.add(element);
			}
		}

		return array;
	}
}

/**
 * This class is used within a {@link Gson} instance to handle the non-standard
 * JSON returned from the ModeShape REST service.
 * 
 * @author QUINTJ16
 *
 */
class ModeShapeNodeAdapter implements JsonDeserializer<ModeShapeNode> {
	private static final Gson GENERIC_GSON = new Gson();

	@SuppressWarnings("unchecked")
	@Override
	public ModeShapeNode deserialize(JsonElement ele, Type eleType, JsonDeserializationContext context) {
		ModeShapeNode node = null;
		if (ele != null && !ele.isJsonNull()) {
			EquipDTOMapping.init();

			JsonObject object = ele.getAsJsonObject();
			if (object != null && !object.isJsonNull()) {
				JsonElement ptEle = object.get(JCRProperties.JCR_PRIMARY_TYPE);
				if (ptEle != null && !ptEle.isJsonNull()) {
					String primaryType = ptEle.getAsString();
					JsonElement childrenProperty = object.get(JCRProperties.JCR_CHILDREN);
					object.add(JCRProperties.JCR_CHILDREN, null);

					Class mapped = EquipDTOMapping.getMappedClass(primaryType);
					if(mapped == null) {
						mapped = ModeShapeNode.class;
					}
					node = (ModeShapeNode) GENERIC_GSON.fromJson(object, mapped);

					node.setChildren(new ArrayList<>());
					if (childrenProperty != null && !childrenProperty.isJsonNull()) {
						JsonObject childrenObject = childrenProperty.getAsJsonObject();
						Set<String> keys = childrenObject.keySet();
						for (String key : keys) {
							JsonObject childObject = childrenObject.getAsJsonObject(key);
							JsonElement typeProperty = childObject.get(JCRProperties.JCR_PRIMARY_TYPE);
							
							ModeShapeNode child = null;
							Class<ModeShapeNode> childClass = ModeShapeNode.class;
							if (typeProperty != null) {
								String childType = typeProperty.getAsString();
								childClass = EquipDTOMapping.getMappedClass(childType);
							}
							
							if(childClass != null) {
								child = context.deserialize(childObject, childClass);
								child.setNodeName(key);
								node.getChildren().add(child);
							}
						}
					}
				} else {
					node = GENERIC_GSON.fromJson(object, ModeShapeNode.class);
				}
			}
		}

		return node;
	}
}

/**
 * A class that contains all standard JCR properties.
 * 
 * @author QUINTJ16
 *
 */
class JCRProperties {
	private JCRProperties() {
	}

	public static final String JCR_ID = "id";
	public static final String JCR_CHILDREN = "children";
	public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";
	public static final String JCR_CREATED = "jcr:created";
	public static final String JCR_CREATED_BY = "jcr:createdBy";
	public static final String JCR_SELF = "self";
	public static final String JCR_UP = "up";
	public static final String JCR_VERSION_HISTORY = "jcr:versionHistory";
	public static final String JCR_BASE_VERSION = "jcr:baseVersion";
	public static final String JCR_PREDECESSORS = "jcr:predecessors";
	public static final String JCR_IS_CHECKED_OUT = "jcr:isCheckedOut";
}

/**
 * This class contains mappings between ModeShape node primary types and their
 * DTO class equivalents.
 * 
 * @author QUINTJ16
 *
 */
class EquipDTOMapping {
	private static Map<String, Class> equipMap;

	private EquipDTOMapping() {
	}

	public static void init() {
		if (equipMap == null) {
			equipMap = new HashMap<>();
			equipMap.put(DataframeNode.PRIMARY_TYPE, DataframeNode.class);
			equipMap.put(MetadatumNode.PRIMARY_TYPE, MetadatumNode.class);
			equipMap.put(AssemblyNode.PRIMARY_TYPE, AssemblyNode.class);
			equipMap.put(CommentNode.PRIMARY_TYPE, CommentNode.class);
			equipMap.put(DatasetNode.PRIMARY_TYPE, DatasetNode.class);
			equipMap.put(LibraryReferenceNode.PRIMARY_TYPE, LibraryReferenceNode.class);
			equipMap.put(PromotionNode.PRIMARY_TYPE, PromotionNode.class);
			equipMap.put(PublishItemNode.PRIMARY_TYPE, PublishItemNode.class);
			equipMap.put(PublishStatusChangeNode.PRIMARY_TYPE, PublishStatusChangeNode.class);
			equipMap.put(QCChecklistItemNode.PRIMARY_TYPE, QCChecklistItemNode.class);
			equipMap.put(QCChecklistSummaryItemNode.PRIMARY_TYPE, QCChecklistSummaryItemNode.class);
			equipMap.put(QCRequestNode.PRIMARY_TYPE, QCRequestNode.class);
			equipMap.put(QCWorkflowItemNode.PRIMARY_TYPE, QCWorkflowItemNode.class);
			equipMap.put(ReportingEventItemNode.PRIMARY_TYPE, ReportingEventItemNode.class);
			equipMap.put(ReportingEventStatusChangeNode.PRIMARY_TYPE, ReportingEventStatusChangeNode.class);
			equipMap.put(ScriptNode.PRIMARY_TYPE, ScriptNode.class);
			equipMap.put(NTFileNode.PRIMARY_TYPE, NTFileNode.class);
			equipMap.put(NTResourceNode.PRIMARY_TYPE, NTResourceNode.class);
			equipMap.put(ColumnNode.PRIMARY_TYPE, ColumnNode.class);
			equipMap.put(RowNode.PRIMARY_TYPE, RowNode.class);
			equipMap.put(AnalysisNode.PRIMARY_TYPE, AnalysisNode.class);
		}
	}

	public static Class getMappedClass(String primaryType) {
		return equipMap.get(primaryType);
	}
}
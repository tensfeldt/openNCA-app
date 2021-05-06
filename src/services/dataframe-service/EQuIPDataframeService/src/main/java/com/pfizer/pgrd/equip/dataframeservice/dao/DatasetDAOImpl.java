package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.ComplexData;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframeservice.dto.DataframeDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.DatasetDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dto.NTFile;
import com.pfizer.pgrd.equip.dataframeservice.dto.NTResource;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;

public class DatasetDAOImpl extends ModeShapeDAO implements DatasetDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(DatasetDAOImpl.class);

	@Override
	public Dataset getDataset(String datasetId) {
		Dataset dataset = null;
		if(datasetId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			DatasetDTO dto = client.getNode(DatasetDTO.class, datasetId);
			if(dto != null) {
				dataset = dto.toDataset();
			}
		}
		
		return dataset;
	}

	@Override
	public List<Dataset> getDataset(List<String> datasetIds) {
		List<Dataset> list = new ArrayList<>();
		if(datasetIds != null) {
			list = this.getDataset(datasetIds.toArray(new String[0]));
		}
		
		return list;
	}

	@Override
	public List<Dataset> getDataset(String[] datasetIds) {
		List<Dataset> list = new ArrayList<>();
		if(datasetIds != null) {
			for(String id : datasetIds) {
				Dataset set = this.getDataset(id);
				if(set != null) {
					list.add(set);
				}
			}
		}
		
		return list;
	}

	@Override
	public Dataset getDatasetByDataframe(String dataframeId) {
		Dataset dataset = null;
		if(dataframeId != null) {
			ModeShapeClient client = getModeShapeClient();
			DataframeDTO parent = client.getNode(DataframeDTO.class, dataframeId);
			if(parent != null && parent.getDataset() != null) {
				dataset = parent.getDataset().toDataset();
			}
		}
		
		return dataset;
	}
	
	@Override
	public Dataset insertDataset(String dataframeId, Dataset dataset) {
		Dataset rset = null;
		if (dataset != null && dataframeId != null) {
			ModeShapeClient client = getModeShapeClient();
			DataframeDTO parent = client.getNode(DataframeDTO.class, dataframeId);
			if(parent != null) {
				String path = parent.getSelf() + "/equip:dataset";
				DatasetDTO dto = new DatasetDTO(dataset);
				
				try {
					dto = client.postNode(dto, path, true);
					
					if(dto != null) {
						rset = dto.toDataset();
					}
				}
				catch(ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon dataset insert");					
				}
			}
		}

		return rset;
	}
	

	@Override
	public ComplexData getData(String complexDataId) {
		ComplexData cd = null;
		if (complexDataId != null) {
			ModeShapeClient client = getModeShapeClient();
			NTFile node = client.getComplexData(complexDataId);
			if (node != null) {
				NTResource contentNode = node.getContent();
				if(contentNode != null) {
					Object[] vals = client.getBinaryWithHeaders(contentNode.getJcrId());
					if(vals != null) {
						byte[] content = (byte[]) vals[1];
						cd = new ComplexData();
						cd.setBytes(content);
						cd.setMimeType(contentNode.getJcrMimeType());
						if(cd.getMimeType() == null) {
							cd.setMimeType("application/octet-stream");
						}
						
						@SuppressWarnings("unchecked")
						Map<String, List<String>> headers = (Map<String, List<String>>) vals[0];
						List<String> contentTypes = headers.get("Content-Type");
						if(contentTypes != null) {
							String[] parts = contentTypes.get(0).split(";");
							for(String ct : parts) {
								String[] v = ct.split("=");
								String key = v[0];
								if(key.equalsIgnoreCase("charset")) {
									cd.setEncoding(v[1]);
								}
							}
						}
					}
				}
			}
		}
		
		return cd;
	}
	
	@Override
	public boolean insertData(String datasetId, byte[] data) {
		boolean success = false;
		if (data != null && datasetId != null) {
			ModeShapeClient client = getModeShapeClient();
			DatasetDTO dto = client.getNode(DatasetDTO.class, datasetId);
			if(dto != null) {
				PropertiesPayload pp = new PropertiesPayload();
				pp.addProperty("equip:dataSize", data.length);
				
				try {
					client.updateNode(datasetId, pp);
					success = client.uploadBinary(datasetId, "equip:complexData", data);
				}
				catch(ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon data insert");					
				}
			}
		}
		
		return success;
	}

	// return the dataframe associated with this complex data id
	@Override
	public Dataframe getParentDataframe(String complexDataId) {
		Dataframe dataframe = null;
		
		ModeShapeClient client = getModeShapeClient();
		ModeShapeNode fileNode = client.getNode(complexDataId);
		if(fileNode != null) {
			DatasetDTO datasetNode = client.getNodeByPath(DatasetDTO.class, fileNode.getUp(), false);
			if(datasetNode != null) {
				DataframeDTO ddto = client.getNodeByPath(DataframeDTO.class, datasetNode.getUp(), true);
				if(ddto != null) {
					dataframe = ddto.toDataframe();
				}
			}
		}

		return dataframe;
	}
	// return the dataframe associated with the dataset id
	@Override
	public Dataframe getParentDataframeFromDataset(String datasetId) {
		Dataframe dataframe = null;
		
		ModeShapeClient client = getModeShapeClient();
		ModeShapeNode datasetNode = client.getNode(datasetId);
		if(datasetNode != null) {
			DataframeDTO ddto = client.getNodeByPath(DataframeDTO.class, datasetNode.getUp(), false);
			if(ddto != null) {
				dataframe = ddto.toDataframe();
			}
		}
		
		return dataframe;
	}
	
}

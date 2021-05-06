package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.dto.MetadatumDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;

public class MetadataDAOImpl extends ModeShapeDAO implements MetadataDAO {
	private static Logger lOGGER = LoggerFactory.getLogger(MetadataDAOImpl.class);

	@Override
	public Metadatum getMetadata(String metadatumId) {
		Metadatum md = null;
		if (metadatumId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			MetadatumDTO dto = client.getNode(MetadatumDTO.class, metadatumId);
			if (dto != null) {
				md = dto.toMetadatum();
			}
		}

		return md;
	}

	@Override
	public List<Metadatum> getMetadata(List<String> metadataIds) {
		List<Metadatum> md = new ArrayList<>();
		if (metadataIds != null) {
			String[] ida = new String[metadataIds.size()];
			metadataIds.toArray(ida);

			md = getMetadata(ida);
		}

		return md;
	}

	@Override
	public List<Metadatum> getMetadata(String[] metadataIds) {
		List<Metadatum> md = new ArrayList<>();
		if (metadataIds != null) {
			for (String id : metadataIds) {
				Metadatum m = getMetadata(id);
				md.add(m);
			}
		}

		return md;
	}

	@Override
	public List<Metadatum> getMetadataByDataframe(String dataframeId) {
		List<Metadatum> md = new ArrayList<>();
		if (dataframeId != null) {
			DataframeDAO dao = getDataframeDAO();
			Dataframe df = dao.getDataframe(dataframeId);
			if (df != null) {
				md = df.getMetadata();
			}
		}

		return md;
	}

	@Override
	public Metadatum insertMetadata(Metadatum metadatum, String parentId) {
		List<Metadatum> list = new ArrayList<>();
		list.add(metadatum);

		list = this.insertMetadata(list, parentId);
		return list.get(0);
	}

	@Override
	public List<Metadatum> insertMetadata(List<Metadatum> metadata, String parentId) {
		List<Metadatum> list = new ArrayList<>();
		if (metadata != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ModeShapeNode parent = client.getNode(parentId);
			if (parent != null) {
				String path = parent.getSelf() + "/equip:metadatum";
				for (Metadatum md : metadata) {
					MetadatumDTO dto = new MetadatumDTO(md);

					try {
						dto = client.postNode(dto, path, true);
						list.add(dto.toMetadatum());
					} catch (ModeShapeAPIException maie) {
						lOGGER.error("", maie);
						throw new RuntimeException("Persistence layer exception upon metadata insert");
					}
				}
			}
		}

		return list;
	}

	@Override
	public Metadatum updateMetadata(Metadatum metadatum, String nodeId) {
		Metadatum updatedMetadatum = null;
		if (metadatum != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ModeShapeNode node = client.getNode(nodeId);
			if (node != null) {
				MetadatumDTO dto = new MetadatumDTO(metadatum);
				
				try {
					String updateId = client.updateNode(dto, nodeId);
					if (updateId != "") {
						updatedMetadatum = dto.toMetadatum();
					}
				}
				catch(ModeShapeAPIException maie) {
					lOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon metadata update");					
				}
			}
		}

		return updatedMetadatum;
	}

}
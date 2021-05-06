package com.pfizer.pgrd.equip.dataframeservice.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframeservice.dto.CommentDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.MetadatumDTO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.modeshape.rest.ModeShapeAPIException;
import com.pfizer.pgrd.modeshape.rest.ModeShapeClient;

public class CommentDAOImpl extends ModeShapeDAO implements CommentDAO {
	private static Logger LOGGER = LoggerFactory.getLogger(CommentDAOImpl.class);

	@Override
	public Comment getComment(String commentId) {
		Comment c = null;
		if (commentId != null) {
			ModeShapeClient client = this.getModeShapeClient();
			CommentDTO dto = client.getNode(CommentDTO.class, commentId);
			if(dto != null) {
				c = dto.toComment();
			}
		}

		return c;
	}

	@Override
	public List<Comment> getComment(List<String> commentIds) {
		List<Comment> comments = new ArrayList<>();
		if (commentIds != null) {
			String[] ida = commentIds.toArray(new String[0]);
			comments = getComment(ida);
		}

		return comments;
	}

	@Override
	public List<Comment> getComment(String[] commentIds) {
		List<Comment> comments = new ArrayList<>();
		if (commentIds != null) {
			for (String id : commentIds) {
				Comment c = getComment(id);
				if (c != null) {
					comments.add(c);
				}
			}
		}

		return comments;
	}

	@Override
	public List<Comment> getCommentByDataframe(String dataframeId) {
		List<Comment> comments = new ArrayList<>();
		if(dataframeId != null) {
			DataframeDAO dfDao = this.getDataframeDAO();
			Dataframe df = dfDao.getDataframe(dataframeId);
			
			if(df != null) {
				comments = df.getComments();
			}
		}
		
		return comments;
	}
	
	@Override
	public Comment insertComment(Comment comment, String parentId) {
		if (comment != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ModeShapeNode parent = client.getNode(parentId);
			if(parent != null) {
				CommentDTO dto = new CommentDTO(comment);
				String path = parent.getSelf() + "/equip:comment";
				
				try {
					dto = client.postNode(dto, path, true);
					comment = dto.toComment();
				}
				catch(ModeShapeAPIException maie) {
					LOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon comment insert");									
				}
			}
		}
		
		return comment;
	}
	
	@Override
	public boolean updateComment(Comment comment, String commentId) {
		boolean success = false;
		if (comment != null && commentId != null) {
			ModeShapeClient client = getModeShapeClient();
			CommentDTO dto = client.getNode(CommentDTO.class, commentId);
			if(dto != null) {
				dto.populate(comment);
				dto.setModifiedBy(System.getProperty("username"));
				dto.setModified(new Date());
				
				try {
					String response = client.updateNode(dto, commentId);
					if(response != null) {
						success = true;
					}
				}
				catch(ModeShapeAPIException maie) {
					LOGGER.error("", maie);
					throw new RuntimeException("Persistence layer exception upon comment update");					
				}
			}
		}
		
		return success;
	}

	@Override
	public List<Comment> insertComment(List<Comment> comments, String parentId) {
		List<Comment> list = new ArrayList<>();
		if (comments != null) {
			ModeShapeClient client = this.getModeShapeClient();
			ModeShapeNode parent = client.getNode(parentId);
			if (parent != null) {
				String path = parent.getSelf() + "/equip:comment";
				for (Comment c : comments) {
					CommentDTO dto = new CommentDTO(c);

					try {
						dto = client.postNode(dto, path, true);
						list.add(dto.toComment());
					} catch (ModeShapeAPIException maie) {
						throw new RuntimeException("Persistence layer exception upon metadata insert");
					}
				}
			}
		}

		return list;
	}
}
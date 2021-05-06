package com.pfizer.pgrd.equip.dataframeservice.resource.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeRootResource;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.libraryservice.client.LibraryServiceClient;
import com.pfizer.pgrd.equip.services.libraryservice.dto.LibraryResponse;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class BatchResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchResource.class);
	
	public static final Route createBatch = new Route() {
		
		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				// Check the authorization header.
				String userId = request.headers(ServiceBaseResource.AUTH_HEADER);
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}
				
				// Authorize the user.
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges(Assembly.BATCH_TYPE, "POST", userId);
				isOk = true;
				if (!isOk) {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userId + " does not have privileges to post " + Assembly.BATCH_TYPE);
				}
				
				// Read the payload.
				String emptyPayloadError = "No request body was provided.";
				String body = request.body();
				if(body == null) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, emptyPayloadError);
				}
				
				body = body.trim();
				if(body.isEmpty()) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, emptyPayloadError);
				}
				
				// If we need to generate test data.
				if(body.toLowerCase().startsWith("generate")) {
					body = BatchResource.generateDummyData(body, userId);
				}
				
				List<Batch> list = null;
				try {
					//LOGGER.info("<<BATCH CREATION>> JSON:\n" + body);
					list = unmarshalObject(body, Batch.class);
				}
				catch(Exception e) {
					e.printStackTrace();
					String errorMessage = "Error reading request body as Batch object(s).";
					LOGGER.error(errorMessage, e);
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, errorMessage);
				}
				
				// Validation
				String validationError = BatchResource.validate(list, userId);
				if(validationError != null) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "One or more batches has failed validation with error: " + validationError);
				}
				
				// Begin inserting.
				ModeShapeDAO msDao = new ModeShapeDAO();
				String error = null;
				List<Batch> returnList = new ArrayList<>();
				
				for(Batch batch : list) {
					try {
						boolean newVersion = batch.getEquipId() != null && !batch.getEquipId().trim().isEmpty();
						List<Dataframe> existingMembers = new ArrayList<>();
						if(newVersion) {
							AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
							List<Assembly> existingBatches = aDao.getAssemblysByEquipId(batch.getEquipId());
							Batch existingBatch = null;
							for(Assembly a : existingBatches) {
								if(existingBatch == null || (a.isCommitted() && a.getVersionNumber() > existingBatch.getVersionNumber()) && !a.isDeleteFlag()) {
									existingBatch = (Batch) aDao.getAssembly(a.getId());
								}
							}
							
							if(existingBatch != null) {
								DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
								existingMembers = dDao.getDataframe(existingBatch.getDataframeIds());
							}
						}
						
						if(batch.getScripts().isEmpty()) {
							for(Dataframe df : batch.getMemberDataframes()) {
								if(df.getScript() != null) {
									batch.getScripts().add(df.getScript());
									break;
								}
							}
						}
						
						if(batch.getScripts().isEmpty()) {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No script was provided in the batch nor any of its members.");
						}
						
						ServiceBaseResource.setCreatedInfo(batch, userId);
						
						// Insert the batch, applying all normal assembly creation processes.
						List<Dataframe> currentMembers = batch.getMemberDataframes();
						ServiceBaseResource.setSubInfo(batch, userId);
						batch = (Batch) AssemblyBaseResource.fullInsert(batch, userId);
						
						// To maintain performance, we will create up to maxThreads to handle the insertion of member dataframes, plus the main thread.
						int maxThreads = 10;
						double dif = (double) currentMembers.size() / (double) maxThreads;
						int memsPerThread = (int) Math.ceil(dif);
						
						List<MemberHandler> threads = new ArrayList<>();
						MemberHandler currentHandler = null;
						List<Dataframe> mainThreadMembers = new ArrayList<>();
						for(int i = 0; i < currentMembers.size(); i++) {
							Dataframe df = currentMembers.get(i);
							df.setBatchId(batch.getId());
							if(df.getScript() == null) {
								df.setScript(batch.getScripts().get(0));
							}
							
							ServiceBaseResource.setCreatedInfo(df, userId);
							ServiceBaseResource.setSubInfo(df, userId);
							
							// If we are under or at the members per thread limit, have the main thread work on this member.
							// Otherwise, have a separate thread work on this member.
							if(i < memsPerThread) {
								mainThreadMembers.add(df);
								continue;
							}
							else if(i % memsPerThread == 0) {
								currentHandler = new MemberHandler(userId, existingMembers);
								threads.add(currentHandler);
							}
							
							currentHandler.currentMembers.add(df);
						}
						
						// Start any other threads
						for(MemberHandler thread : threads) {
							thread.start();
						}
						
						// Have the main thread handle any members it was assigned.
						List<Dataframe> members = new ArrayList<>();
						for(Dataframe df : mainThreadMembers) {
							try {
								df = BatchResource.handleMember(df, existingMembers, userId);
								members.add(df);
							}
							catch(Exception e) { 
								e.printStackTrace();
								if(e instanceof HaltException) {
									HaltException he = (HaltException) e;
									error = he.body();
								}
								else {
									error = e.getMessage();
								}
							}
						}
						
						// Join the other threads and retrieve their results.
						for(MemberHandler thread : threads) {
							try {
								thread.join();
								if(thread.error != null && error == null) {
									if(thread.error instanceof HaltException) {
										HaltException he = (HaltException) thread.error;
										error = he.body();
									}
									else {
										error = thread.error.getMessage();
									}
								}
								else {
									members.addAll(thread.createdMembers);
								}
							}
							catch(Exception e) {
								e.printStackTrace();
							}
						}
						
						// Update the batch with the members.
						for(Dataframe df : members) {
							batch.getDataframeIds().add(df.getId());
							batch.getMemberDataframes().add(df);
						}
						
						/*
						// Insert each member dataframe, applying all normal dataframe creation processes.
						for(Dataframe df : currentMembers) {
							df.setBatchId(batch.getId());
							try {
								for(Dataframe ov : existingMembers) {
									if(df.getOutputFileName().equals(ov.getOutputFileName())) {
										df.setEquipId(ov.getEquipId());
										break;
									}
								}
								
								if(df.getScript() == null) {
									df.setScript(batch.getScripts().get(0));
								}
								
								df = DataframeRootResource.fullInsert(df, userId);
								members.add(df);
								batch.getDataframeIds().add(df.getId());
								batch.getMemberDataframes().add(df);
							}
							catch(Exception e) {
								e.printStackTrace();
								error = e.getMessage();
								break;
							}
						}
						*/
						
						// Update the batch with all of the IDs of the member dataframes.
						PropertiesPayload pp = new PropertiesPayload();
						pp.addProperty("equip:dataframeIds", batch.getDataframeIds());
						msDao.updateNode(batch.getId(), pp);
						
						returnList.add(batch);
					}
					catch(Exception e) {
						e.printStackTrace();
						error = e.getMessage();
					}
					
					if(error != null) {
						break;
					}
				}
				
				// Need to rollback if there was an error.
				if(error != null) {
					System.out.println("ERROR WHEN CREATING BATCH: " + error);
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, error);
				}
				
				json = BatchResource.returnJSON(returnList, response);
			}
			catch(HaltException he) {
				throw he;
			}
			catch(Exception e) {
				e.printStackTrace();
				LOGGER.error("Error when POSTing a Batch.", e);
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
			
			return json;
		}
		
	};
	
	private static class MemberHandler extends Thread {
		public List<Dataframe> currentMembers = new ArrayList<>();
		public List<Dataframe> existingMembers = new ArrayList<>();
		public List<Dataframe> createdMembers = new ArrayList<>();
		public String userId;
		public Exception error;
		
		public MemberHandler(String userId, List<Dataframe> existingMembers) {
			this.userId = userId;
			this.existingMembers = existingMembers;
		}
		
		@Override
		public void run() {
			for(Dataframe df : currentMembers) {
				try {
					df = BatchResource.handleMember(df, existingMembers, userId);
					this.createdMembers.add(df);
				}
				catch(Exception e) {
					this.error = e;
					break;
				}
			}
		}
	}
	
	private static final Dataframe handleMember(Dataframe df, List<Dataframe> existingMembers, String userId) throws ServiceCallerException {
		boolean match = false;
		for(Dataframe ov : existingMembers) {
			if(df.getOutputFileName().equals(ov.getOutputFileName())) {
				df.setEquipId(ov.getEquipId());
				match = true;
				break;
			}
		}
		
		if(!match) {
			df.overrideEquipId(null);
		}
		
		return DataframeRootResource.fullInsert(df, userId);
	}
	
	private static final String generateDummyData(String body, String userId) throws ServiceCallerException {
		String[] cmds = body.split(";");
		String json = "[]";
		if(cmds.length == 4) {
			String studyId = cmds[1].trim();
			String parentDfId = cmds[2].trim();
			int number = Integer.parseInt(cmds[3].trim());
			
			Batch batch = new Batch();
			batch.setStudyIds(Arrays.asList(studyId));
			batch.getParentIds().add(parentDfId);
			
			LibraryServiceClient lsc = new LibraryServiceClient();
			lsc.setHost(Props.getLibraryServiceServer());
			lsc.setPort(Props.getLibraryServicePort());
			lsc.setSystemId("nca");
			lsc.setUser(userId);
			LibraryResponse lresp = lsc.getGlobalSystemScriptByName("no-op.R");
			
			LibraryReference lr = new LibraryReference();
			lr.setLibraryRef(lresp.getArtifactId());
			Script script = new Script();
			script.setScriptBody(lr);
			batch.getScripts().add(script);
			
			for(int i = 0; i < number; i++) {
				Dataframe df = new Dataframe();
				df.setDataframeType(Dataframe.DATA_TRANSFORMATION_TYPE);
				df.setPromotionStatus("Not Promoted");
				df.setDataStatus("Draft");
				df.setDataBlindingStatus("Blinded");
				df.setRestrictionStatus("Unrestricted");
				df.setOutputFileName("fakeFile" + (i+1) + ".txt");
				df.setScript(script);
				df.setCommitted(true);
				
				Dataset dataset = new Dataset();
				dataset.setStdOut("complete");
				df.setDataset(dataset);
				
				batch.getMemberDataframes().add(df);
			}
			
			json = ServiceBaseResource.returnJSON(batch, null);
		}
		
		return json;
	}
	
	public static final String validate(Batch batch, String userId) throws ServiceCallerException {
		if(batch != null) {
			return BatchResource.validate(Arrays.asList(batch), userId);
		}
		
		return null;
	}
	
	public static final String validate(List<Batch> batches, String userId) throws ServiceCallerException {
		if(batches != null) {
			AuthorizationDAO auth = new AuthorizationDAO();
			Map<String, Boolean> privsMap = new HashMap<>();
			for(Batch batch : batches) {
				batch.setAssemblyType(Assembly.BATCH_TYPE);
				
				String validationError = AssemblyBaseResource.validate(batch);
				if(validationError != null) {
					return validationError;
				}
				
				Script script = batch.getScripts().get(0);
				for(Dataframe df : batch.getMemberDataframes()) {
					df.setStudyIds(batch.getStudyIds());
					df.setScript(script);
					if(df.getDataframeIds() == null || df.getDataframeIds().isEmpty()) {
						df.setDataframeIds(new ArrayList<>());
						ModeShapeDAO msDao = new ModeShapeDAO();
						for(String id : batch.getParentIds()) {
							EquipObject eo = msDao.getEquipObject(id);
							if(eo instanceof Dataframe) {
								df.getDataframeIds().add(eo.getId());
							}
						}
					}
					
					validationError = DataframeRootResource.validate(df);
					if(validationError != null) {
						return validationError;
					}
					
					Boolean hasPriv = privsMap.get(df.getDataframeType());
					if(hasPriv == null) {
						hasPriv = auth.checkPrivileges(df.getDataframeType(), "POST", userId);
						privsMap.put(df.getDataframeType(), hasPriv);
					}
					
					if(!hasPriv) {
						return "User " + userId + " does not have sufficient privileges to create a dataframe of type " + df.getDataframeType() + ".";
					}
				}
			}
		}
		
		return null;
	}
	
	public static final Route getBatchById = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String returnJson = "";
			try {
				String userId = request.headers("IAMPFIZERUSERCN");
				if(userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "No user ID was provided.");
				}
				
				String id = request.params(":id");
				AssemblyDAO aDao = ModeShapeDAO.getAssemblyDAO();
				Assembly a = aDao.getAssembly(id);
				if(a == null) {
					Spark.halt(HTTPStatusCodes.NOT_FOUND, "No batch with ID " + id + " could be found.");
				}
				if(!(a instanceof Batch)) {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Entity " + id + " is not a batch.");
				}
				
				boolean includeMembers = false;
				String im = request.queryParams("includeMembers");
				if(im != null) {
					List<String> vals = Arrays.asList("y", "yes", "true");
					if(vals.contains(im.toLowerCase())) {
						includeMembers = true;
					}
				}
				
				Batch b = (Batch) a;
				Dataframe df = null;
				if(!b.getDataframeIds().isEmpty()) {
					DataframeDAO dDao = ModeShapeDAO.getDataframeDAO();
					if(includeMembers) {
						List<Dataframe> dataframes = dDao.getDataframe(b.getDataframeIds(), true);
						b.setMemberDataframes(dataframes);
						if(!b.getMemberDataframes().isEmpty()) {
							df = b.getMemberDataframes().get(0);
						}
					}
					else {
						String dfId = b.getDataframeIds().get(0);
						df = dDao.getDataframe(dfId);
					}
				}
				
				if(df != null && df.getDataset() != null) {
					Dataset ds = df.getDataset();
					b.setStdErr(ds.getStdErr());
					b.setStdIn(ds.getStdIn());
					b.setStdOut(ds.getStdOut());
				}
				
				List<String> cleanParents = new ArrayList<>();
				ModeShapeDAO msDao = new ModeShapeDAO();
				for(String pid : b.getParentIds()) {
					EquipObject eo = msDao.getEquipObject(pid);
					if(eo instanceof Dataframe) {
						cleanParents.add(eo.getId());
						b.getParentDataframeIds().add(eo.getId());
					}
					else if(eo instanceof Assembly) {
						b.getParentAssemblyIds().add(eo.getId());
					}
				}
				b.setParentIds(cleanParents);
				
				returnJson = BatchResource.returnJSON(b, response);
			}
			catch(HaltException he) {
				throw he;
			}
			catch(Exception e) {
				e.printStackTrace();
				LOGGER.error("Error when retrieving batch by ID.", e);
				Spark.halt(HTTPStatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
			}
			
			return returnJson;
		}
		
	};
}

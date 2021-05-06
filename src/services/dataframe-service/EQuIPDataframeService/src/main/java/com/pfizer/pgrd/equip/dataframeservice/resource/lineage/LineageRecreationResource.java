package com.pfizer.pgrd.equip.dataframeservice.resource.lineage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageReExecutionParameters;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyUtils;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AuthorizationDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.ModeShapeNode;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.LineageDAO;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.CopyException;
import com.pfizer.pgrd.equip.dataframeservice.exceptions.ServiceExceptionHandler;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.ServiceBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyBaseResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.dataframe.DataframeRootResource;
import com.pfizer.pgrd.equip.dataframeservice.util.EquipIdCalculator;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPContentTypes;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPHeaders;
import com.pfizer.pgrd.equip.dataframeservice.util.HTTPStatusCodes;

import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.computeservice.dto.Parameter;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class LineageRecreationResource extends ServiceBaseResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(LineageResource.class);
	private static ComputeServiceClient csClient = null;

	/**
	 * This {@link Route} re-executes all scripts on all data within the lineage
	 * containing the provided element ID. The re-execution will either start at the
	 * indicated element or its child, depending on the provided parameters, using
	 * the data contained in a specified dataframe.
	 */
	public static final Route reExecute = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			return LineageRecreationResource.reExecute(request, response, false);
		}

	};

	public static final Route versionLineage = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			return LineageRecreationResource.reExecute(request, response, true);
		}

	};

	private static final Object reExecute(Request request, Response response, boolean asNewVersion) throws Exception {
		String json = null;
		Set<String> studyIds = new HashSet<String>();
		
		try {
			Map<String, String> rexMap = new HashMap<>();

			String userId = request.headers("IAMPFIZERUSERCN");
			if (userId != null) {
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges("lineage reexecute", "PUT", userId);

				if (isOk) {
					String paramsJson = request.body();
					if (paramsJson != null) {
						GsonBuilder gb = new GsonBuilder();
						gb.setPrettyPrinting();
						Gson gson = gb.create();

						LineageReExecutionParameters params = gson.fromJson(paramsJson,
								LineageReExecutionParameters.class);
						if (params != null) {
							if (params.getEnvironment() != null && params.getNewDataIds() != null
									&& !params.getNewDataIds().isEmpty()
									&& (params.getStartId() != null || asNewVersion)) {
								ModeShapeDAO bDao = new ModeShapeDAO();

								String startId = params.getStartId();
								if (asNewVersion) {
									startId = request.params(":id");
								}

								EquipObject startNode = bDao.getEquipObject(startId);
								if (startNode != null) {
									DataframeDAO dDao = getDataframeDAO();
									List<Dataframe> newData = dDao.getDataframe(params.getNewDataIds(), false);
									if (newData != null && !newData.isEmpty()) {
										LineageDAO lDao = getLineageDAO();
										lDao.setAuthUserId(userId);
										List<LineageItem> lineage = lDao.getLineage(startId);

										// Now that we have the lineage, we traverse the tree and re-execute every point
										LineageRecreationResource.csClient = new ComputeServiceClient();
										LineageRecreationResource.csClient.setHost(Props.getComputeServiceServer());
										LineageRecreationResource.csClient.setPort(Props.getComputeServicePort());
										LineageRecreationResource.csClient.setUser(userId);

										LineageItem first = lineage.get(0);
										LineageRexecutioner rex = new LineageRexecutioner();
										rex.setAsNewVersion(asNewVersion);
										rex.setSeedDataframes(params.getNewDataIds());
										rex.rex(userId, first, params.skipInitialItem());
										
										/*if (params.skipInitialItem()) {
											for (DataframeLineageItem dfItem : first.getChildDataframes()) {
												LineageRecreationResource.rex(userId, dfItem, params.getNewDataIds(),
														asNewVersion, rexMap);
											}
											for (AssemblyLineageItem aItem : first.getChildAssemblies()) {
												LineageRecreationResource.rex(userId, aItem, params.getNewDataIds(),
														asNewVersion, rexMap);
											}
										} else {
											LineageRecreationResource.rex(userId, first, params.getNewDataIds(),
													asNewVersion, rexMap);
										}*/

										json = "Complete.";
										for(String log : rex.getLog()) {
											json += " " + log;
										}
										
										// get all study ids to update related protocol
										for (Dataframe df : newData) {
											studyIds.addAll(df.getStudyIds());
										}
										if (studyIds.isEmpty() == false) {
											// call opmeta service to update modification time on associated protocol
											try {
												OpmetaServiceClient osc = new OpmetaServiceClient();
												osc.setHost(Props.getOpmetaServiceServer());
												osc.setPort(Props.getOpmetaSerivcePort());
												for (String studyId : studyIds) {
													LOGGER.info(
															"LineageRecreationResource: update protocol for study id="
																	+ studyId);
													osc.updateProtocolModifiedDate(userId, studyId);
												}
											} catch (Exception err) {
												LOGGER.warn(
														"LineageRecreationResource: Error updating protocol modification time",
														err);
											}
										}
									} else {
										Spark.halt(HTTPStatusCodes.NOT_FOUND, "No dataframe with ID '"
												+ params.getNewDataIds() + "' could be found.");
									}
								} else {
									Spark.halt(HTTPStatusCodes.NOT_FOUND,
											"No start object with ID '" + params.getStartId() + "' could be found.");
								}
							} else {
								Spark.halt(HTTPStatusCodes.BAD_REQUEST, "Not all parameters were provided.");
							}
						} else {
							Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No parameters were provided.");
						}
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No parameters were provided.");
					}

					response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.PLAIN_TEXT);
				} else {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userId + " does not have privileges to recreate lineage");
				}
			} else {
				Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
			}
		} catch (Exception ex) {
			ServiceExceptionHandler.handleException(ex);
		}

		return json;
	}

	private static void rex(String copier, LineageItem item, List<String> newDataIds, boolean asNewVersion,
			Map<String, String> rexMap) throws Exception {
		if (item != null && item.isVersionComitted()) {
			if (item instanceof AssemblyLineageItem) {
				LineageRecreationResource.rexAssembly(copier, (AssemblyLineageItem) item, newDataIds,
						new ArrayList<String>(), asNewVersion, rexMap);
			} else if (item instanceof DataframeLineageItem) {
				LineageRecreationResource.rexDataframe(copier, (DataframeLineageItem) item, newDataIds,
						new ArrayList<String>(), asNewVersion, rexMap);
			}
		}
	}

	private static void rexAssembly(String copier, AssemblyLineageItem item, List<String> newDataIds,
			List<String> newAssemblyIds, boolean asNewVersion, Map<String, String> rexMap) throws Exception {
		if (item != null && item.isVersionComitted()) {
			AssemblyDAO adao = ServiceBaseResource.getAssemblyDAO();
			Assembly assembly = adao.getAssembly(item.getId());
			if (assembly != null) {
				assembly.setParentIds(new ArrayList<>());
				if (newAssemblyIds != null) {
					assembly.getParentIds().addAll(newAssemblyIds);
				}
				if (newDataIds != null) {
					assembly.getParentIds().addAll(newDataIds);
				}
				if (copier != null) {
					assembly.setCreatedBy(copier);
				}

				if (!asNewVersion) {
					assembly.setEquipId(null);
				} else {
					EntityVersioningResource.supersedeAction(assembly, copier);
				}
				
				AssemblyBaseResource.applyVersionIncrementingLogic(assembly);

				if (assembly instanceof Analysis) {
					Analysis analysis = (Analysis) assembly;
					// Retrieve the previously computed concentration data
					if (analysis.getDataframeIds().size() == 1) {
						String oldConcId = analysis.getDataframeIds().get(0);
						String newConcId = rexMap.get(oldConcId);
						if (newConcId != null) {
							analysis.getDataframeIds().set(0, newConcId);
						}
					}

					// Copy MCT
					if (analysis.getModelConfigurationDataframeId() != null) {
						Dataframe mctCopy = CopyUtils.copyDataframe(copier, analysis.getModelConfigurationDataframeId(),
								asNewVersion);
						if (mctCopy != null) {
							analysis.setModelConfigurationDataframeId(mctCopy.getId());
						}
					}

					// Compute KEL
					if (!analysis.getDataframeIds().isEmpty() && newDataIds.size() == 1
							&& analysis.getKelFlagsDataframeId() != null) {
						Analysis temp = new Analysis();
						temp.setDataframeIds(newDataIds);

						CopyUtils.copyKEL(copier, temp, analysis, asNewVersion);
						analysis.setKelFlagsDataframeId(temp.getKelFlagsDataframeId());

						// If KEL was computed, compute Primary Parameters
						if (analysis.getKelFlagsDataframeId() != null && analysis.getParametersDataframeId() != null) {
							DataframeDAO ddao = ServiceBaseResource.getDataframeDAO();
							Dataframe oldParams = ddao.getDataframe(analysis.getParametersDataframeId(), false);
							if (oldParams != null && oldParams.getScript() != null) {
								List<String> dids = new ArrayList<>();
								dids.add(newDataIds.get(0));
								dids.add(analysis.getModelConfigurationDataframeId());
								dids.add(analysis.getKelFlagsDataframeId());

								dids = LineageRecreationResource.computeDataframe(oldParams, dids,
										new ArrayList<String>(), asNewVersion);
								if (dids.size() == 1) {
									analysis.setParametersDataframeId(dids.get(0));
								}
							}
						}
						else {
							analysis.setParametersDataframeId(null);
						}
					}
				}

				String oldId = assembly.getId();
				long oldVersion = assembly.getVersionNumber();
				Assembly newAssembly = null;
				if (assembly instanceof Analysis) {
					AnalysisDAO anDao = ModeShapeDAO.getAnalysisDAO();
					newAssembly = anDao.insertAnalysis((Analysis) assembly);
				} else {
					newAssembly = adao.insertAssembly(assembly);
				}

				rexMap.put(oldId, newAssembly.getId());

				CommentDAO cdao = ModeShapeDAO.getCommentDAO();
				cdao.insertComment(CopyUtils.createReExecuteComment(copier, assembly.getEquipId(), oldVersion),
						newAssembly.getId());

				for (DataframeLineageItem dfitem : item.getMemberDataframes()) {
					LineageRecreationResource.rexDataframe(copier, dfitem, new ArrayList<String>(),
							new ArrayList<String>(), asNewVersion, rexMap);
				}
				for (AssemblyLineageItem ali : item.getChildAssemblies()) {
					LineageRecreationResource.rexAssembly(copier, ali, new ArrayList<String>(), new ArrayList<String>(),
							asNewVersion, rexMap);
				}
				for (DataframeLineageItem dli : item.getChildDataframes()) {
					LineageRecreationResource.rexDataframe(copier, dli, new ArrayList<String>(),
							new ArrayList<String>(), asNewVersion, rexMap);
				}
			}
		}
	}

	private static void rexDataframe(String copier, DataframeLineageItem item, List<String> newDataIds,
			List<String> newAssemblyIds, boolean asNewVersion, Map<String, String> rexMap) throws Exception {
		if (item != null && item.isVersionComitted()) {
			DataframeDAO ddao = ServiceBaseResource.getDataframeDAO();
			Dataframe dataframe = ddao.getDataframe(item.getId(), false);
			if (dataframe != null && !dataframe.isDeleteFlag()) {
				List<String> dfIds = LineageRecreationResource.computeDataframe(dataframe, newDataIds, newAssemblyIds,
						asNewVersion);

				if (dfIds.size() == 1) {
					String computedDataframeId = dfIds.get(0);
					rexMap.put(item.getId(), computedDataframeId);

					CommentDAO cdao = ModeShapeDAO.getCommentDAO();
					cdao.insertComment(CopyUtils.createReExecuteComment(copier, dataframe.getEquipId(),
							dataframe.getVersionNumber()), computedDataframeId);

					for (AssemblyLineageItem aitem : item.getChildAssemblies()) {
						List<List<String>> parentIds = LineageRecreationResource.separateParents(aitem.getParentIds());
						List<String> parentDataframeIds = parentIds.get(0);
						List<String> parentAssemblyIds = parentIds.get(1);

						LineageRecreationResource.replaceString(dataframe.getId(), computedDataframeId,
								parentDataframeIds);
						LineageRecreationResource.rexAssembly(copier, aitem, parentDataframeIds, parentAssemblyIds,
								asNewVersion, rexMap);
					}

					for (DataframeLineageItem dfitem : item.getChildDataframes()) {
						List<String> nd = dfitem.getParentDataframeIds();
						LineageRecreationResource.replaceString(dataframe.getId(), computedDataframeId, nd);
						LineageRecreationResource.rexDataframe(copier, dfitem, nd, dfitem.getParentAssemblyIds(),
								asNewVersion, rexMap);
					}
				}
			}
		}
	}

	private static List<String> computeDataframe(Dataframe dataframe, List<String> newDataIds,
			List<String> newAssemblyIds, boolean asNewVersion) throws Exception {
		List<String> computedIds = new ArrayList<>();
		if (dataframe != null && !dataframe.isDeleteFlag()) {
			if (dataframe.getScript() != null) {
				Script script = dataframe.getScript();
				ComputeParameters params = new ComputeParameters();
				params.setScriptId(script.getScriptBody().getLibraryRef());
				params.setEnvironment("Server");
				params.setComputeContainer("equip-r-base");
				if (script.getComputeContainer() != null) {
					params.setComputeContainer(script.getComputeContainer());
				}

				params.setUser(LineageRecreationResource.csClient.getUser());
				params.setDataframeIds(newDataIds);
				params.setDataframeType(dataframe.getDataframeType());
				params.setAssemblyIds(newAssemblyIds);
				if (asNewVersion) {
					params.setEquipId(dataframe.getEquipId());
				}

				if (dataframe.getDataset() != null) {
					Dataset dataset = dataframe.getDataset();
					List<String> pms = dataset.getParameters();

					GsonBuilder gb = new GsonBuilder();
					gb.registerTypeHierarchyAdapter(Parameter.class, new Parameter.ParameterAdapter());
					gb.setPrettyPrinting();

					Gson gson = gb.create();
					for (String json : pms) {
						Parameter p = gson.fromJson(json, Parameter.class);
						if (!p.getKey().startsWith("INPUT")) {
							params.getParameters().add(p);
						}
					}
				}

				if (script.getScriptCriteria() != null) {
					GsonBuilder gb = new GsonBuilder();
					gb.registerTypeHierarchyAdapter(Parameter.class, new Parameter.ParameterAdapter());
					gb.setPrettyPrinting();

					Gson gson = gb.create();
					String scriptCriteria = script.getScriptCriteria();
					ComputeParameters originalParams = gson.fromJson(scriptCriteria, ComputeParameters.class);
					if (originalParams.getComputeContainer() != null) {
						params.setComputeContainer(originalParams.getComputeContainer());
					}
					for (Parameter p : originalParams.getParameters()) {
						params.getParameters().add(p);
					}
				}

				ComputeResult result = LineageRecreationResource.csClient.compute(params);
				if (result.getStderr() == null || result.getStderr().isEmpty()) {
					computedIds = result.getDataframeIds();
				} else {
					throw new Exception("Error when re-executing dataframe script " + dataframe.getScript().getId()
							+ " on data " + newDataIds + ": stdErr=" + result.getStderr());
				}
			} else {
				computedIds.add(dataframe.getId());
			}
		}

		return computedIds;
	}

	private static List<List<String>> separateParents(List<String> parentIds) {
		List<List<String>> list = new ArrayList<>();
		list.add(new ArrayList<String>());
		list.add(new ArrayList<String>());

		if (parentIds != null) {
			ModeShapeDAO mdao = new ModeShapeDAO();
			for (String pid : parentIds) {
				EquipObject eo = mdao.getEquipObject(pid);
				if (eo != null) {
					if (eo instanceof Dataframe) {
						list.get(0).add(pid);
					} else if (eo instanceof Assembly) {
						list.get(1).add(pid);
					}
				}
			}
		}

		return list;
	}

	private static void replaceString(String o, String n, List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			if (s.equals(o)) {
				list.set(i, n);
			}
		}
	}

	public static final Route copyLineage = new Route() {

		@Override
		public Object handle(Request request, Response response) throws Exception {
			String json = null;
			try {
				// check authorization first
				String userId = request.headers("IAMPFIZERUSERCN");
				if (userId == null) {
					Spark.halt(HTTPStatusCodes.UNAUTHORIZED, "User cannot be determined");
				}

				// check for privileges to do this:
				AuthorizationDAO auth = new AuthorizationDAO();
				boolean isOk = auth.checkPrivileges("lineage copy", "GET", userId);

				if (!isOk) {
					Spark.halt(HTTPStatusCodes.FORBIDDEN,
							"User " + userId + " does not have privileges to copy lineage");
				}

				String id = request.params(":startId");
				if (id != null) {
					LineageDAO dao = getLineageDAO();
					dao.setAuthUserId(userId);
					LineageItem copy = dao.copyLineage(userId, id);

					if (copy != null) {
						List<String> studyIds = copy.getStudyIds();
						/*
						 * if (copy instanceof AssemblyLineageItem) { studyIds =
						 * ((AssemblyLineageItem)copy).getStudyIds(); } else if (copy instanceof
						 * DataframeLineageItem) { studyIds =
						 * ((DataframeLineageItem)copy).getStudyIds(); }
						 */

						if (studyIds != null && studyIds.isEmpty() == false) {
							// call opmeta service to update modification time on associated protocol
							try {
								OpmetaServiceClient osc = new OpmetaServiceClient();
								osc.setHost(Props.getOpmetaServiceServer());
								osc.setPort(Props.getOpmetaSerivcePort());
								for (String studyId : studyIds) {
									LOGGER.info("LineageRecreationResource: update protocl for study id=" + studyId);
									osc.updateProtocolModifiedDate(userId, studyId);
								}
							} catch (Exception err) {
								LOGGER.warn(
										"LineageRecreationResource: Error updating protocol modification time for node "
												+ id,
										err);
							}
						}
						
						json = marshalObject(copy);
						response.header(HTTPHeaders.CONTENT_TYPE, HTTPContentTypes.JSON);
					} else {
						Spark.halt(HTTPStatusCodes.BAD_REQUEST, "The lineage could not be copied.");
					}
				} else {
					Spark.halt(HTTPStatusCodes.BAD_REQUEST, "No entity ID was provided.");
				}
			} catch (CopyException ce) {
				Spark.halt(HTTPStatusCodes.CONFLICT, ce.getMessage());
			} catch (Exception e) {
				ServiceExceptionHandler.handleException(e);
			}

			return json;
		}

	};
}
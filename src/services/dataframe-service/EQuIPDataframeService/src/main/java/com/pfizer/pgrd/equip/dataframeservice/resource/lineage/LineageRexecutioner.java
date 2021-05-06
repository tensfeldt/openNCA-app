package com.pfizer.pgrd.equip.dataframeservice.resource.lineage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pfizer.pgrd.equip.dataframe.dto.Analysis;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Batch;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.AssemblyLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.DataframeLineageItem;
import com.pfizer.pgrd.equip.dataframe.dto.lineage.LineageItem;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.copyutils.CopyUtils;
import com.pfizer.pgrd.equip.dataframeservice.dao.AnalysisDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.AssemblyDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.CommentDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.PropertiesPayload;
import com.pfizer.pgrd.equip.dataframeservice.resource.EntityVersioningResource;
import com.pfizer.pgrd.equip.dataframeservice.resource.assembly.AssemblyBaseResource;
import com.pfizer.pgrd.equip.services.computeservice.client.ComputeServiceClient;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeParameters;
import com.pfizer.pgrd.equip.services.computeservice.dto.ComputeResult;
import com.pfizer.pgrd.equip.services.computeservice.dto.Parameter;

public class LineageRexecutioner {
	private boolean asNewVersion;
	private Map<String, EquipObject> REX_MAP = new HashMap<>();
	private DataframeDAO DDAO;
	private AssemblyDAO ADAO;
	private String REXER;
	private ComputeServiceClient COMPUTE_CLIENT;
	private List<String> log = new ArrayList<>();

	private List<String> seedDataframes = new ArrayList<>();
	private List<String> seedAssemblies = new ArrayList<>();
	
	public List<String> getLog() {
		return this.log;
	}

	public List<LineageItem> rex(String rexer, LineageItem item) throws Exception {
		return this.rex(rexer, item, false);
	}

	public List<LineageItem> rex(String rexer, LineageItem item, boolean skipParent) throws Exception {
		List<LineageItem> copies = new ArrayList<>();
		if (rexer != null) {
			if (item != null && item.userHasAccess()) {
				this.log = new ArrayList<>();
				this.REX_MAP = new HashMap<>();
				this.DDAO = ModeShapeDAO.getDataframeDAO();
				this.ADAO = ModeShapeDAO.getAssemblyDAO();
				this.REXER = rexer;

				this.COMPUTE_CLIENT = new ComputeServiceClient();
				this.COMPUTE_CLIENT.setHost(Props.getComputeServiceServer());
				this.COMPUTE_CLIENT.setPort(Props.getComputeServicePort());
				this.COMPUTE_CLIENT.setUser(rexer);

				if (!skipParent) {
					LineageItem copy = this.rex(item);
					copies.add(copy);
				} else if (!this.seedDataframes.isEmpty()) {
					ModeShapeDAO msDao = new ModeShapeDAO();
					String newId = this.seedDataframes.get(0);
					EquipObject eo = msDao.getEquipObject(newId);
					if (eo != null) {
						this.REX_MAP.put(item.getId(), eo);
					}
				}

				List<LineageItem> childCopies = this.rexChildren(item);
				if (skipParent) {
					copies = childCopies;
				}
			}
		} else {
			throw new ReExecutionException("No user ID was provided.");
		}

		return copies;
	}
	
	private LineageItem rex(LineageItem item) throws Exception {
		if (item instanceof DataframeLineageItem) {
			return this.rexDataframe((DataframeLineageItem) item, true);
		} else if (item instanceof AssemblyLineageItem) {
			return this.rexAssembly((AssemblyLineageItem) item, true);
		}

		return null;
	}

	private List<LineageItem> rexChildren(LineageItem item) throws Exception {
		List<LineageItem> childCopies = new ArrayList<>();
		if (item != null) {
			this.rexChildren(Arrays.asList(item));
		}

		return childCopies;
	}

	private List<LineageItem> rexChildren(List<LineageItem> items) throws Exception {
		List<LineageItem> childCopies = new ArrayList<>();
		if (items != null) {
			List<LineageItem> orderedLevel = CopyUtils.prioritizeChildren(items);
			if (!orderedLevel.isEmpty()) {
				for (LineageItem child : orderedLevel) {
					if (child instanceof DataframeLineageItem) {
						LineageItem dli = this.rexDataframe((DataframeLineageItem) child, false);
						if(dli instanceof AssemblyLineageItem) {
							if(this.asNewVersion) {
								throw new ReExecutionException("When re-executing dataframe " + child.getEquipId() + " v" + child.getEquipVersion() +" (" + child.getId() + ") a batch was returned (" + dli.getEquipId() + " / " + dli.getId() + "), but was not expected.");
							}
						}
						
						if (dli != null) {
							childCopies.add(dli);
						}
					} else if (child instanceof AssemblyLineageItem) {
						LineageItem ali = this.rexAssembly((AssemblyLineageItem) child, false);
						if (ali != null) {
							childCopies.add(ali);
						}
					}
				}

				this.rexChildren(orderedLevel);
			}
		}

		return childCopies;
	}

	private LineageItem rexDataframe(DataframeLineageItem item, boolean useSeeds) throws Exception {
		LineageItem nitem = null;
		if (item.userHasAccess()) {
			Dataframe dataframe = (Dataframe) this.REX_MAP.get(item.getId());
			if (dataframe != null) {
				nitem = (DataframeLineageItem) this.updateParents(dataframe, useSeeds);
			} else if (item.isVersionComitted()) {
				Parents parents = this.getParents(item, useSeeds);
				dataframe = this.DDAO.getDataframe(item.getId(), false);
				
				if (dataframe != null && !dataframe.isDeleteFlag()) {
					List<EquipObject> results = this.computeDataframe(dataframe, parents.parentDataframes, parents.parentAssemblies, null, null, false);
					nitem = this.handlResult(item, results);
					if(nitem instanceof AssemblyLineageItem) {
						results = new ArrayList<>();
						List<Dataframe> dfs = this.DDAO.getDataframe(((AssemblyLineageItem) nitem).getDataframeIds());
						for(Dataframe df : dfs) {
							results.add(df);
						}
					}
					
					this.matchBatchMember(dataframe, results);
				}
			} else {
				throw new ReExecutionException(
						"Dataframe " + item.getEquipId() + " (" + item.getId() + ") is not committed.");
			}
		}
		else {
			this.log.add("User does not have access to dataframe " + item.getEquipId() + " v" + item.getEquipVersion() + "; did not re-execute.");
		}

		return nitem;
	}

	private LineageItem updateParents(EquipObject eo, boolean useSeeds) {
		LineageItem item = null;
		if (eo != null) {
			PropertiesPayload pp = new PropertiesPayload();
			if (eo instanceof Dataframe) {
				Dataframe df = (Dataframe) eo;
				DataframeLineageItem dli = DataframeLineageItem.fromDataframe(df);
				Parents p = this.getParents(dli, useSeeds);

				pp.addProperty("equip:dataframeIds", p.parentDataframes);
				pp.addProperty("equip:assemblyIds", p.parentAssemblies);

				dli.setParentAssemblyIds(p.parentAssemblies);
				dli.setParentDataframeIds(p.parentDataframes);
				item = dli;
			} else if (eo instanceof Assembly) {
				Assembly a = (Assembly) eo;
				AssemblyLineageItem ali = AssemblyLineageItem.fromAssembly(a);
				Parents p = this.getParents(ali, useSeeds);

				List<String> parents = p.parentAssemblies;
				parents.addAll(p.parentDataframes);
				pp.addProperty("equip:parentIds", parents);

				ali.setParentIds(parents);
				item = ali;
			}

			ModeShapeDAO msDao = new ModeShapeDAO();
			msDao.updateNode(eo.getId(), pp);
		}

		return item;
	}

	private List<EquipObject> computeDataframe(Dataframe dataframe, List<String> newDataIds, List<String> newAssemblyIds, List<String> additionalEquipIds, AssemblyLineageItem batch, boolean dontBatch) throws Exception {
		List<EquipObject> results = new ArrayList<>();
		if (dataframe != null && !dataframe.isDeleteFlag()) {
			if (dataframe.getScript() != null) {
				Script script = dataframe.getScript();
				ComputeParameters params = new ComputeParameters();
				params.setScriptId(script.getScriptBody().getLibraryRef());
				params.setEnvironment("Server");
				params.setComputeContainer("equip-r-base");
				params.setDontBatch(dontBatch);
				
				if (script.getComputeContainer() != null) {
					params.setComputeContainer(script.getComputeContainer());
				}

				params.setUser(this.COMPUTE_CLIENT.getUser());
				params.setDataframeIds(newDataIds);
				params.setDataframeType(dataframe.getDataframeType());
				if (dataframe.getDataframeType().equalsIgnoreCase(Dataframe.PRIMARY_PARAMETERS_TYPE)) {
					params.addDataframeType(Dataframe.ESTIMATED_CONCENTRATION_DATA_TYPE);
					params.addDataframeType(Dataframe.KEL_FLAGS_TYPE);
				}
				params.setAssemblyIds(newAssemblyIds);
				if (this.asNewVersion) {
					if(batch != null) {
						params.setEquipId(batch.getEquipId());
						params.setBatch(true);
					}
					else {
						params.setEquipId(dataframe.getEquipId());
					}
					
					if (additionalEquipIds != null) {
						params.getEquipIds().addAll(additionalEquipIds);
					}
				}

				if (dataframe.getDataset() != null) {
					Dataset dataset = dataframe.getDataset();
					List<String> pms = dataset.getParameters();

					GsonBuilder gb = new GsonBuilder();
					gb.registerTypeHierarchyAdapter(Parameter.class, new Parameter.ParameterAdapter());
					gb.setPrettyPrinting();
					
					Gson gson = gb.create();
					for (String json : pms) {
						Parameter p = null;
						try {
							p = gson.fromJson(json, Parameter.class);
						}
						catch(Exception e) {
							e.printStackTrace();
							throw e;
						}
						
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
				
				ComputeResult result = this.COMPUTE_CLIENT.compute(params);
				if (result.getStderr() == null || result.getStderr().isEmpty()) {
					List<Dataframe> dataframes = this.DDAO.getDataframe(result.getDataframeIds(), false);
					results.addAll(dataframes);
					
					if(result.getBatchId() != null) {
						Assembly a = this.ADAO.getAssembly(result.getBatchId());
						results.add(a);
					}
				} else {
					throw new ReExecutionException(
							"Error when re-executing dataframe script " + dataframe.getScript().getId() + " on data "
									+ newDataIds + ": StdOut=" + result.getStdout());
				}
			} else {
				results.add(dataframe);
			}
		}

		return results;
	}

	private LineageItem rexAssembly(AssemblyLineageItem item, boolean useSeeds) throws Exception {
		LineageItem nitem = null;
		if (item.userHasAccess()) {
			// First, check to see if this assembly has already been re-executed.
			Assembly assembly = (Assembly) this.REX_MAP.get(item.getId());
			if (assembly != null) {
				nitem = (AssemblyLineageItem) this.updateParents(assembly, useSeeds);
			} else if (item.isVersionComitted()) {
				// If the assembly was not already re-executed, we need to re-execute it.
				assembly = this.ADAO.getAssembly(item.getId());
				if (assembly != null) {
					Parents parents = this.getParents(item, useSeeds);
					assembly.setParentIds(new ArrayList<>());
					assembly.getParentIds().addAll(parents.parentDataframes);
					assembly.getParentIds().addAll(parents.parentAssemblies);
					assembly.setCreatedBy(this.REXER);

					if (!asNewVersion) {
						assembly.setEquipId(null);
					} else {
						EntityVersioningResource.supersedeAction(assembly, this.REXER);
						assembly.setVersionSuperSeded(false);
					}
					AssemblyBaseResource.applyVersionIncrementingLogic(assembly);
					
					List<String> oldMembers = assembly.getDataframeIds();
					List<String> dataframeMembers = new ArrayList<>();
					for (String id : assembly.getDataframeIds()) {
						EquipObject eo = this.REX_MAP.get(id);
						if (eo != null) {
							dataframeMembers.add(eo.getId());
						} else {
							dataframeMembers.add(id);
						}
					}

					List<String> assemblyMembers = new ArrayList<>();
					for (String id : assembly.getAssemblyIds()) {
						EquipObject eo = this.REX_MAP.get(id);
						if (eo != null) {
							assemblyMembers.add(eo.getId());
						} else {
							assemblyMembers.add(id);
						}
					}
					
					assembly.setDataframeIds(dataframeMembers);
					assembly.setAssemblyIds(assemblyMembers);
					
					if(assembly instanceof Batch) {
						if(assembly.getDataframeIds().isEmpty()) {
							throw new ReExecutionException("Batch " + item.getEquipId()  + " v" + item.getEquipVersion() + " (" + item.getId() + ") has no member dataframe IDs.");
						}
						
						List<Dataframe> members = this.DDAO.getDataframe(assembly.getDataframeIds(), false);
						Dataframe member = members.get(0);
						AssemblyLineageItem batchItem = null;
						if(this.asNewVersion) {
							batchItem = item;
						}
						
						List<EquipObject> results = this.computeDataframe(member, parents.parentDataframes, parents.parentAssemblies, null, batchItem, false);
						nitem = this.handlResult(item, results);
						if(nitem instanceof AssemblyLineageItem) {
							results = new ArrayList<>();
							List<Dataframe> dfs = this.DDAO.getDataframe(((AssemblyLineageItem) nitem).getDataframeIds());
							for(Dataframe df : dfs) {
								results.add(df);
							}
						}
						this.matchBatchMember(members, results);
						
						return nitem;
					}
					else if (assembly instanceof Analysis) {
						Analysis analysis = (Analysis) assembly;
						// Copy MCT
						if (analysis.getModelConfigurationDataframeId() != null) {
							Dataframe mctCopy = CopyUtils.copyDataframe(this.REXER,
									analysis.getModelConfigurationDataframeId(), this.asNewVersion);
							if (mctCopy != null) {
								analysis.setModelConfigurationDataframeId(mctCopy.getId());
								try {
									EntityVersioningResource.put.handle(mctCopy.getId(), "commit", this.REXER, null,
											null);
								} catch (Exception e) {
								}
							}
						}

						// Compute KEL
						if (analysis.getDataframeIds().size() == 1 && analysis.getKelFlagsDataframeId() != null) {
							Analysis temp = new Analysis();
							temp.setDataframeIds(oldMembers);
							temp.setKelFlagsDataframeId(item.getKelFlagsDataframeId());

							CopyUtils.copyKEL(this.REXER, temp, analysis, asNewVersion);
							analysis.setKelFlagsDataframeId(temp.getKelFlagsDataframeId());

							// If KEL was computed, compute Primary Parameters
							if (analysis.getKelFlagsDataframeId() != null
									&& analysis.getParametersDataframeId() != null) {
								Dataframe oldParams = this.DDAO.getDataframe(analysis.getParametersDataframeId(),
										false);
								if (oldParams != null && oldParams.getScript() != null) {
									List<String> dids = new ArrayList<>();
									dids.add(parents.parentDataframes.get(0));
									dids.add(analysis.getModelConfigurationDataframeId());
									dids.add(analysis.getKelFlagsDataframeId());
									
									List<String> additionalIds = null;
									if (this.asNewVersion) {
										additionalIds = new ArrayList<>();
										additionalIds.add("");
										Dataframe kel = this.DDAO.getDataframe(analysis.getKelFlagsDataframeId());
										if (kel != null) {
											additionalIds.add(kel.getEquipId());
										}
									}

									List<EquipObject> results = this.computeDataframe(oldParams, dids, new ArrayList<String>(), additionalIds, null, true);
									if (!results.isEmpty()) {
										analysis.setParametersDataframeId(results.get(0).getId());
										try {
											EntityVersioningResource.put.handle(analysis.getParametersDataframeId(),
													"commit", this.REXER, null, null);
										} catch (Exception e) {
										}

										analysis.setEstimatedConcDataframeId(results.get(1).getId());
										try {
											EntityVersioningResource.put.handle(analysis.getEstimatedConcDataframeId(),
													"commit", this.REXER, null, null);
										} catch (Exception e) {
										}
									}
								}
							} else {
								analysis.setParametersDataframeId(null);
							}

							if (analysis.getKelFlagsDataframeId() != null) {
								try {
									EntityVersioningResource.put.handle(analysis.getKelFlagsDataframeId(), "commit",
											this.REXER, null, null);
								} catch (Exception e) {
								}
							}
						}
					}

					String oldId = assembly.getId();
					assembly.setCreated(new Date());
					assembly.setCreatedBy(this.REXER);
					assembly.setModifiedDate(assembly.getCreated());
					assembly.setModifiedBy(assembly.getCreatedBy());
					
					Assembly newAssembly = null;
					if (assembly instanceof Analysis) {
						AnalysisDAO anDao = ModeShapeDAO.getAnalysisDAO();
						newAssembly = anDao.insertAnalysis((Analysis) assembly);
					} else {
						newAssembly = this.ADAO.insertAssembly(assembly);
					}

					if (newAssembly != null) {
						this.REX_MAP.put(oldId, newAssembly);
						nitem = AssemblyLineageItem.fromAssembly(newAssembly);

						CommentDAO cdao = ModeShapeDAO.getCommentDAO();
						cdao.insertComment(
								CopyUtils.createReExecuteComment(this.REXER, item.getEquipId(), item.getEquipVersion()),
								newAssembly.getId());
					}
				}
			} else {
				throw new ReExecutionException(
						"Assembly " + item.getEquipId() + " (" + item.getId() + ") is not committed.");
			}
		}
		else {
			this.log.add("User does not have access to " + item.getAssemblyType() + " " + item.getEquipId() + " v" + item.getEquipVersion() + "; did not re-execute.");
		}

		return nitem;
	}
	
	private void matchBatchMember(Dataframe original, List<EquipObject> newMembers) {
		String ofn = original.getOutputFileName();
		for(EquipObject eo : newMembers) {
			if(eo instanceof Dataframe) {
				Dataframe nm = (Dataframe) eo;
				if(ofn != null && ofn.equals(nm.getOutputFileName())) {
					this.REX_MAP.put(original.getId(), nm);
					return;
				}
			}
		}
	}
	
	private void matchBatchMember(List<Dataframe> originals, List<EquipObject> newMembers) {
		for(Dataframe df : originals) {
			this.matchBatchMember(df, newMembers);
		}
	}
	
	private LineageItem handlResult(LineageItem original, List<EquipObject> results) {
		if (results.size() == 1) {
			EquipObject result = results.get(0);
			
			// The result could be a batch or a dataframe.
			LineageItem nitem = null;
			if(result instanceof Assembly) {
				nitem = AssemblyLineageItem.fromAssembly((Assembly) result);
			}
			else {
				nitem = DataframeLineageItem.fromDataframe((Dataframe) result);
			}
			
			this.REX_MAP.put(original.getId(), result);
			CommentDAO cdao = ModeShapeDAO.getCommentDAO();
			cdao.insertComment(
					CopyUtils.createReExecuteComment(this.REXER, original.getEquipId(), original.getEquipVersion()),
					result.getId());
			
			return nitem;
		}
		
		return null;
	}

	private Parents getParents(DataframeLineageItem item, boolean useSeeds) {
		Parents p = new Parents();

		for (String id : item.getParentAssemblyIds()) {
			EquipObject n = this.REX_MAP.get(id);
			if (n != null) {
				p.parentAssemblies.add(n.getId());
			} else {
				p.parentAssemblies.add(id);
			}
		}

		for (String id : item.getParentDataframeIds()) {
			EquipObject n = this.REX_MAP.get(id);
			if (n != null) {
				p.parentDataframes.add(n.getId());
			} else {
				p.parentDataframes.add(id);
			}
		}

		if (useSeeds) {
			if (!this.seedDataframes.isEmpty()) {
				p.parentDataframes = this.seedDataframes;
			}
			if (!this.seedAssemblies.isEmpty()) {
				p.parentAssemblies = this.seedAssemblies;
			}
		}

		return p;
	}
	
	/**
	 * Fetches the parents of the provided item. The method will first check to see if the parents have been re-executed and return the new IDs if the have.
	 * @param item
	 * @param useSeeds
	 * @return {@link Parents}
	 */
	private Parents getParents(AssemblyLineageItem item, boolean useSeeds) {
		Parents p = new Parents();
		ModeShapeDAO msDao = new ModeShapeDAO();
		for (String id : item.getParentIds()) {
			EquipObject eo = this.REX_MAP.get(id);
			if (eo == null) {
				eo = msDao.getEquipObject(id);
			}

			if (eo != null) {
				if (eo instanceof Dataframe) {
					p.parentDataframes.add(eo.getId());
				} else if (eo instanceof Assembly) {
					p.parentAssemblies.add(eo.getId());
				}
			}
		}

		if (useSeeds) {
			if (!this.seedDataframes.isEmpty()) {
				p.parentDataframes = this.seedDataframes;
			}
			if (!this.seedAssemblies.isEmpty()) {
				p.parentAssemblies = this.seedAssemblies;
			}
		}
		
		return p;
	}

	public boolean isAsNewVersion() {
		return asNewVersion;
	}

	public void setAsNewVersion(boolean asNewVersion) {
		this.asNewVersion = asNewVersion;
	}

	public List<String> getSeedDataframes() {
		return seedDataframes;
	}

	public void setSeedDataframes(List<String> seedDataframes) {
		this.seedDataframes = seedDataframes;
	}

	public List<String> getSeedAssemblies() {
		return seedAssemblies;
	}

	public void setSeedAssemblies(List<String> seedAssemblies) {
		this.seedAssemblies = seedAssemblies;
	}
}

class Parents {
	public List<String> parentDataframes = new ArrayList<>();
	public List<String> parentAssemblies = new ArrayList<>();
}

class ReExecutionException extends Exception {
	public ReExecutionException() {
		super();
	}

	public ReExecutionException(String message) {
		super(message);
	}
}
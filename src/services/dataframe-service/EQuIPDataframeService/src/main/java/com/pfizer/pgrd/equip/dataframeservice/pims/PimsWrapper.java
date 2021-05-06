package com.pfizer.pgrd.equip.dataframeservice.pims;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pfizer.pgrd.cordis.cds.dto.pims.PimsDemography;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsDose;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsPharmacokinetics;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsPkLabInfo;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsStudy;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsStudyLocations;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsSubjectDisposition;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsTreatment;
import com.pfizer.pgrd.cordis.cds.dto.pims.PimsVitalSigns;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsDemographyRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsDoseRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsPharmacokineticsRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsPkLabInfoRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsStudyLocationsRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsStudyRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsSubjectDispositionRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsTreatmentRepresentation;
import com.pfizer.pgrd.cordis.cds.dto.representation.pims.PimsVitalSignsRepresentation;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframeservice.application.Props;
import com.pfizer.pgrd.equip.dataframeservice.dao.DataframeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dao.ModeShapeDAO;
import com.pfizer.pgrd.equip.dataframeservice.dto.DemographyVitalSigns;
import com.pfizer.pgrd.equip.services.client.ServiceCallerException;
import com.pfizer.pgrd.equip.services.opmeta.client.OpmetaServiceClient;
import com.pfizer.pgrd.rest.client.RestClientException;
import com.pfizer.pgrd.rest.client.RestServiceCaller;
import com.pfizer.pgrd.rest.client.RestServiceResult;

public class PimsWrapper {
	private static Logger log = LoggerFactory.getLogger(PimsWrapper.class);
	
	private String userId;
	private String studyAlias;
	

	//Note that CDS puts the version number in the URI so we need to store it as a property so we can change it if we move to a new version.
	private static final String CDS_PIMS_API_URI = "http://" + Props.getPIMSServerNameAndPort() + "/" + Props.getCDSServiceName() + "/api/pims";

	//each URI is either a data uri or a reference uri
	private static final String PIMS_BASE_REFERENCE_URI = CDS_PIMS_API_URI + "/reference";
	private static final String PIMS_BASE_DATA_URI = CDS_PIMS_API_URI + "/data";

	//used internal to certain URIs
	private static final String PIMS_STUDY = "/study";
		
	//suffixes.
	private static final String PIMS_LOCATIONS_SUFFIX = "/locations?all";
	private static final String PIMS_TREATMENTS_SUFFIX = "/treatment/restricted?all";
	private static final String PIMS_PK_DATA_SUFFIX = "/pharmacokinetics?all";
	private static final String PIMS_LAB_INFO_SUFFIX = "/pk-lab-info?all&$filter=";
	public  static final String PIMS_LAB_INFO_LABEL_TYPE_EQUAL_S = "labelType%20EQ%20S";
	public  static final String PIMS_LAB_INFO_LABEL_TYPE_EQUAL_C = "labelType%20EQ%20C";
	private static final String DOSEP_SUFFIX = "dose/unblinded"; //the ?all is added in a function below for this one
	private static final String SUBJECT_DISPOSITION_SUFFIX = "/subject-disposition?all";
	private static final String STUDY_SUFFIX_ASIDE_FROM_STUDY_ID = "/study";

	public Boolean getLockFlag(String studyId, String location) throws Exception {
		PimsStudy study = getPimsStudy(studyId,location);
		
		if( study == null ){
			return null;
		}
		
		return study.isHardLock();
	}
	
	//Example URI
	//https://cordis-dev.pfizer.com/CORDISClinicalDataServiceV1/api/pims/reference/new-haven/study/A0531083
	public PimsStudy getPimsStudy(String studyId, String location) throws Exception {
		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);		
		//User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		String uri = PIMS_BASE_REFERENCE_URI + "/" + location + STUDY_SUFFIX_ASIDE_FROM_STUDY_ID + "/" + studyId;
		RestServiceResult rsr = makePimsCall(uri,PimsOAuthTokenAccess.getAccessToken());

		return rsr.getObject(PimsStudyRepresentation.class);
	}
		
	//returns number of rows in the csv, the csv file is returned through the input parameter "csv"
	public int getPIMSDemographyAndVitalSignsData(String studyId, StringBuffer csv, PimsStudyLocations locations) throws Exception {
		//User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());

		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);		
		List<String> locList = locations.getLocations();
		List<PimsDemography> demographies = getPimsDemographies(this.getStudyAlias(), locList, locations.getDataStandard());
		List<PimsVitalSigns> vitalSigns = getPimsVitalSigns(this.getStudyAlias(), locList, locations.getDataStandard());
		List<DemographyVitalSigns> demographyVitalSignsList = mergeDemographiesAndVitalSignsBasedUponSiteIdAndSubject(demographies, vitalSigns);
		return listDemographyVitalSignsToCSV(demographyVitalSignsList, csv);
	}

	private int listDemographyVitalSignsToCSV(List<DemographyVitalSigns> list, StringBuffer csv) {
		int rowCount = 0;

		addDemographyVitalSignsHeader(csv);
		
		//to be used by front end, we add all columns and let the front end filter
		//�	STUDY, SITEID, SUBJID, HT, WT, AGEDERU, AGEDER, WTUNI, WTRAW, HTUNI, HTRAW, RACEOTH, RACES, SEX, RACIALD, ETHNIC, COLLDATE		
		
		for(DemographyVitalSigns dv : list ){
			nullCheck(csv, dv.getDemography().getAgedrv());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getAgedrvu());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getAlcqtyf());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getAlcstat());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getBcmethod());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getBcother());
			csv.append(",");
//Do not expose birth date
//			nullCheck(csv, dv.getDemography().getBirthdtf());
//			csv.append(",");
			nullCheck(csv, dv.getDemography().getCountry());
			csv.append(",");
//			nullCheck(csv, dv.getDemography().getDataStandard());
//			csv.append(",");
			nullCheck(csv, dv.getDemography().getDmdtf());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getEthdes());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getEthnic());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getHormsta());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getPrvsite());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getPrvstud());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getPrvsubj());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRace());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRaceoth());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRaciald());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRpchbio());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRpnchbio());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRpnchro());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRpperf01());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getRpsxact());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getSex());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getSiteid());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getSmoksta());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getStudyid());
			csv.append(",");
			nullCheck(csv, dv.getDemography().getSubjid());
			csv.append(",");
//			nullCheck(csv, dv.getDemography().getSubjinit());
//			csv.append(",");
			nullCheck(csv, dv.getHeight());
			csv.append(",");
			nullCheck(csv, dv.getHeightUnit());
			csv.append(",");
			nullCheck(csv, dv.getWeight());
			csv.append(",");
			nullCheck(csv, dv.getWeightUnit());
			
			csv.append("\r\n");
			rowCount++;
		}
		
		return rowCount;
	}

	private void nullCheck(StringBuffer csv, String string) {
		if(string == null || string.toUpperCase().equals("NULL")){
			csv.append("");
		}
		else{
			String s = string;
			
			// If this string represents a number, leave it alone.
			// Otherwise, escape certain characters and surround it with double quotes.
			try {
				Double.parseDouble(s);
			}
			catch(Exception e) {
				s = "\"" + s.replaceAll("\"", "\"\"") + "\"";
			}
			
			csv.append(s);
		}
	}

	private void addDemographyVitalSignsHeader(StringBuffer csv) {
		csv.append("Agedrv".toUpperCase());
		csv.append(",");
		csv.append("Agedrvu".toUpperCase());
		csv.append(",");
		csv.append("Alcqtyf".toUpperCase());
		csv.append(",");
		csv.append("Alcstat".toUpperCase());
		csv.append(",");
		csv.append("Bcmethod".toUpperCase());
		csv.append(",");
		csv.append("Bcother".toUpperCase());
		csv.append(",");
//Do not expose birth date
//		csv.append("Birthdtf".toUpperCase());
//		csv.append(",");
		csv.append("Country".toUpperCase());
		csv.append(",");
//		csv.append("DataStandard".toUpperCase());
//		csv.append(",");
		csv.append("Dmdtf".toUpperCase());
		csv.append(",");
		csv.append("Ethdes".toUpperCase());
		csv.append(",");
		csv.append("Ethnic".toUpperCase());
		csv.append(",");
		csv.append("Hormsta".toUpperCase());
		csv.append(",");
		csv.append("Prvsite".toUpperCase());
		csv.append(",");
		csv.append("Prvstud".toUpperCase());
		csv.append(",");
		csv.append("Prvsubj".toUpperCase());
		csv.append(",");
		csv.append("Race".toUpperCase());
		csv.append(",");
		csv.append("Raceoth".toUpperCase());
		csv.append(",");
		csv.append("Raciald".toUpperCase());
		csv.append(",");
		csv.append("Rpchbio".toUpperCase());
		csv.append(",");
		csv.append("Rpnchbio".toUpperCase());
		csv.append(",");
		csv.append("Rpnchro".toUpperCase());
		csv.append(",");
		csv.append("Rpperf01".toUpperCase());
		csv.append(",");
		csv.append("Rpsxact".toUpperCase());
		csv.append(",");
		csv.append("Sex".toUpperCase());
		csv.append(",");
		csv.append("Siteid".toUpperCase());
		csv.append(",");
		csv.append("Smoksta".toUpperCase());
		csv.append(",");
		csv.append("Studyid".toUpperCase());
		csv.append(",");
		csv.append("Subjid".toUpperCase());
		csv.append(",");
//		csv.append("Subjinit".toUpperCase());
//		csv.append(",");
		csv.append("Height".toUpperCase());
		csv.append(",");
		csv.append("HeightUnit".toUpperCase());
		csv.append(",");
		csv.append("Weight".toUpperCase());
		csv.append(",");
		csv.append("WeightUnit".toUpperCase());
		
		csv.append("\r\n");
	}

	private List<DemographyVitalSigns> mergeDemographiesAndVitalSignsBasedUponSiteIdAndSubject( List<PimsDemography> demographies, List<PimsVitalSigns> allVitalSigns) {
		//the requirement is that if there are duplicate vital signs, choose the first one that contains height and weight information and ignore the rest.
		//if there are no vital signs records, or if none of them contain height and weight information then return null information for height and weight.
		List<DemographyVitalSigns> retVal = new ArrayList<DemographyVitalSigns>();
		
		Map<String,PimsDemography> mapDemography = new HashMap<String,PimsDemography>();
		
		for( PimsDemography demography : demographies){
			String key = demography.getSiteid() + demography.getSubjid();
			mapDemography.put(key, demography);
		}

		Map<String,List<PimsVitalSigns>> mapVitalSigns = new HashMap<String,List<PimsVitalSigns>>();
		
		for( PimsVitalSigns vitalSigns : allVitalSigns){
			String key = vitalSigns.getSiteid() + vitalSigns.getSubjid();
			List<PimsVitalSigns> listVitalSigns = mapVitalSigns.get(key) == null ? new ArrayList<PimsVitalSigns>() : mapVitalSigns.get(key);
			listVitalSigns.add(vitalSigns);
			mapVitalSigns.put(key, listVitalSigns);
		}
		
		for(String key : mapDemography.keySet()){
			DemographyVitalSigns dv = new DemographyVitalSigns();
			dv.setDemography(mapDemography.get(key));
			List<PimsVitalSigns> list = mapVitalSigns.get(key);
			boolean found = false;
			
			if(list != null) {
				for( PimsVitalSigns vitalSigns : list ){	
					if( vitalSigns.getHeightf() != null && !vitalSigns.getHeightf().isEmpty() &&
						vitalSigns.getHtunit()  != null && !vitalSigns.getHtunit().isEmpty()  &&
						vitalSigns.getWeightf() != null && !vitalSigns.getWeightf().isEmpty() &&
						vitalSigns.getWtunit()  != null && !vitalSigns.getWtunit().isEmpty() ){
						dv.setHeight(vitalSigns.getHeightf());
						dv.setHeightUnit(vitalSigns.getHtunit());
						dv.setWeight(vitalSigns.getWeightf());
						dv.setWeightUnit(vitalSigns.getWtunit());
						found = true;
					}
									
					if(found){
						break;
					}
				}
			}
			
			if(!found){
				dv.setHeight("");
				dv.setHeightUnit("");
				dv.setWeight("");
				dv.setWeightUnit("");
			}		
			
			retVal.add(dv);
		}
				
		return retVal;
	}

	private List<PimsVitalSigns> getPimsVitalSigns(String studyId, List<String> locList, String dataStandard) throws Exception {
		//example URI
		//https://cordis-dev.pfizer.com/CORDISClinicalDataServiceV1/api/pims/data/brussels/study/C2541003/cdisc" + "/vital-signs?$filter=(visit EQ �SCREENING�) and (heightf is not null) and (weightf is not null)"				
		List<PimsVitalSigns> vitalSigns = new ArrayList<PimsVitalSigns>();
		
		for (String location : locList) {
			String vitalSignsUri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard;
			
			//add filter via CDS (clinical data service) filtering language.  Note that CDS exposes PIMS data.
			//vitalSignsUri += "/vital-signs?$filter=(visit%20EQ%20%E2%80%98SCREENING%E2%80%99)%20and%20(heightf%20is%20not%20null)%20and%20(weightf%20is%20not%20null)";
			vitalSignsUri += "/vital-signs?all=true&$filter=(visit EQ �SCREENING�) and (heightf is not null) and (weightf is not null)";
			RestServiceResult rsr = makePimsCall(vitalSignsUri, PimsOAuthTokenAccess.getAccessToken());
			
			vitalSigns.addAll(rsr.getObjectList(PimsVitalSignsRepresentation.class));
			log.info("PIMS study call for location=" + location + " for vital signs returned " + vitalSigns.size() + " elements.");
		}
		
		return vitalSigns;
	}
	
	private List<PimsDemography> getPimsDemographies(String studyId, List<String> locList, String dataStandard) throws Exception {
		//example URI
		//https://cordis-dev.pfizer.com/CORDISClinicalDataServiceV1/api/pims/data/brussels/study/C2541003/cdisc/demography
		List<PimsDemography> demographies = new ArrayList<PimsDemography>();
		
		for (String location : locList) {
			String demographyUri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard + "/demography?all=true";
			RestServiceResult rsr = makePimsCall(demographyUri, PimsOAuthTokenAccess.getAccessToken());

			demographies.addAll(rsr.getObjectList(PimsDemographyRepresentation.class));
			log.info("PIMS study call for location=" + location + " studyId=" + studyId + " for demographies returned " + demographies.size() + " elements.");
		}
		
		return demographies;
	}
	
	public List<String> getDosepFromStudyId(String studyId) throws Exception {
		this.setStudyAlias(studyId); //this value will be reset if an alias is needed
		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);		
		PimsStudyLocations locations = getPimsStudyLocations(studyId);
		if (locations == null) {
			
			throw new IllegalStateException("Study data not found for " + studyId + " or its aliases");
			
		}
		List<String> locList = locations.getLocations();
		
	//	User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		List<PimsDose> listDose = new ArrayList<PimsDose>();
		

		for (String location : locList) {
			List<PimsDose> doseList = getPimsDoseData(this.getStudyAlias(), location, locations.getDataStandard());
			listDose.addAll(doseList);
			log.info("PIMS study call for studyId=" + this.getStudyAlias()+ ", and location=" + location + " returned " + doseList.size() + " elements.");
		}

		return extractDistinctDosePFromDoses(listDose);
	}
	
	public List<PimsDose> getDosesFromStudyId(String studyId) throws Exception {
		//User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());

		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);		
		PimsStudyLocations locations = getPimsStudyLocations(studyId);
		List<String> locList = locations.getLocations();
		
		List<PimsDose> listDose = new ArrayList<PimsDose>();
		
		for (String location : locList) {
			PimsDose dose = null;
			List<PimsDose> doseList = getPimsDoseData(studyId, location, locations.getDataStandard());
			listDose.addAll(doseList);
			log.info("PIMS study call for studyId=" + studyId + ", and location=" + location + " returned " + doseList.size() + " elements.");
		}
		
		return listDose;
	}
	

	private List<PimsDose> getPimsDoseData(String studyId, String locItem, String dataStandard) throws Exception {
		String doseUri = generatePimsDoseUri(studyId, locItem, dataStandard, DOSEP_SUFFIX, "all");
		RestServiceResult rsr = makePimsCall(doseUri,PimsOAuthTokenAccess.getAccessToken());
		return rsr.getObjectList(PimsDoseRepresentation.class);
	}
	
	public PimsStudyLocations getPimsStudyLocations(String studyId) throws Exception {
		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);
	//	User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		String uri = PIMS_BASE_REFERENCE_URI + PIMS_STUDY + "/" + studyId + PIMS_LOCATIONS_SUFFIX;
		PimsStudyLocations locations = null;

		RestServiceResult rsr = makePimsCall(uri, PimsOAuthTokenAccess.getAccessToken());
		if (rsr != null) {
			locations = rsr.getObject(PimsStudyLocationsRepresentation.class);
			this.setStudyAlias(studyId);
		} else {
			List<String> aliases = getStudyAliases(studyId);

			for (String alias : aliases) {
				//for testing
				//alias = "B1521044";
				uri = PIMS_BASE_REFERENCE_URI + PIMS_STUDY + "/" + alias + PIMS_LOCATIONS_SUFFIX;
				rsr = makePimsCall(uri, PimsOAuthTokenAccess.getAccessToken());
				
				
				
				if (rsr != null) {
				
					this.setStudyAlias(alias);
					
					locations = rsr.getObject(PimsStudyLocationsRepresentation.class);
					StringBuilder logMessage = new StringBuilder(
							"PIMS locations for study id=" + alias + ":\r\n");

					for (String location : locations.getLocations()) {
						logMessage.append("" + location + "\r\n");
					}
					log.info(logMessage.toString());

					return locations;
				}
			}

		}
return locations;
	}


	private List<String> getStudyAliases(String studyId) throws ServiceCallerException {

//call opmeta service to find aliases
OpmetaServiceClient osc = new OpmetaServiceClient(Props.getOpmetaServiceServer(), Props.getOpmetaSerivcePort());
		return osc.getStudyAliases(this.getUserId(), studyId);

	}

	private static String generatePimsDoseUri(String studyId, String location, String dataStandard, String target,String queryPart) {
		String doseUri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard + "/" + target;
		if (queryPart != null && queryPart.isEmpty() == false) {
			doseUri += "?" + queryPart;
		}
		return doseUri;
	}	

	private ArrayList<String> extractDistinctDosePFromDoses(List<PimsDose> listDose) {
		Map<String,String> map = new HashMap<String,String>();
		for(PimsDose dose : listDose){
			map.put(dose.getDosep(),dose.getDosep());
		}
		
		StringBuffer logMessage = new StringBuffer("distinct dosep's found were:\r\n");
		
		for( String key : map.keySet()){
			logMessage.append( "" + key + "\r\n" );
		}
		
		log.info(logMessage.toString());
		
		return new ArrayList<String>(map.keySet());
	}
	
	private RestServiceResult makePimsCall(String uri, String token) throws RestClientException {
		RestServiceResult rsr = null;
		
		try {
			rsr = RestServiceCallerExt.get(token, uri);
		}
		catch (Exception ex) {
			String errorMessage = "Call to PIMS URI for uri=" + uri + " failed";
			if(rsr!=null && rsr.getStatus()!=null){
				errorMessage += " REST ERROR DESCRIPTION=" + rsr.getStatus().getDescription() + ", REST ERROR REASON=" + rsr.getStatus().getReasonPhrase();
				throw new IllegalStateException(errorMessage);
			}
		
		}
		
		if (rsr == null) return null;  //this returns null if the study id is not found, allowing teh code to try another uri
		rsr.setUsingStringRepresentation(true);
		log.info(rsr.getEntityAsText());
		return rsr;
	}

	public List<PimsTreatment> getTreatmentData(String studyId, List<String> locations, String dataStandard) throws Exception {
		//		�/pims/data/new-haven/study/B7471001/cdisc/treatment/restricted
//		User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		List<PimsTreatment> list = new ArrayList<PimsTreatment>();
		
		for( String location : locations ){
			String uri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard + PIMS_TREATMENTS_SUFFIX;
			RestServiceResult rsr = makePimsCall(uri,PimsOAuthTokenAccess.getAccessToken());

			list.addAll(rsr.getObjectList(PimsTreatmentRepresentation.class));
		}
		
		return list;
	}

	public List<PimsPharmacokinetics> getPKData(String studyId, List<String> locations, String dataStandard) throws Exception {
//		�/pims/data/new-haven/study/B7471001/cdisc/pharmacokinetics
//		User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		List<PimsPharmacokinetics> list = new ArrayList<PimsPharmacokinetics>();
		
		for( String location : locations ){
			String uri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard + PIMS_PK_DATA_SUFFIX;
			RestServiceResult rsr = makePimsCall(uri, PimsOAuthTokenAccess.getAccessToken());

			list.addAll(rsr.getObjectList(PimsPharmacokineticsRepresentation.class));
		}
		
		return list;
	}

	public List<PimsPkLabInfo> getLabInfoData(String studyId, List<String> locations, String dataStandard, String labelType) throws Exception {
		// �/pims/reference/new-haven/study/B7471001/pk-lab-info/?$filter=labelType%20EQ%20S
		// The filter above is labelType='S'     
	//	User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());
		List<PimsPkLabInfo> list = new ArrayList<PimsPkLabInfo>();
		
		for( String location : locations ){
			String uri = PIMS_BASE_REFERENCE_URI + "/" + location + PIMS_STUDY + "/" + studyId + PIMS_LAB_INFO_SUFFIX + labelType;
			RestServiceResult rsr = makePimsCall(uri, PimsOAuthTokenAccess.getAccessToken());

			list.addAll(rsr.getObjectList(PimsPkLabInfoRepresentation.class));
		}
		
		return list;		
	}

	//returns number of rows in CSV
	public int getStudyCompletionData(String studyId, StringBuffer csv) throws Exception {
		PimsStudyLocations locations = getPimsStudyLocations(studyId);
		List<PimsSubjectDisposition> subjectDispositions = getSubjectDispositions(studyId,locations.getDataStandard(),locations.getLocations());
		
		return convertSubjectDispositionsToCSV(subjectDispositions, csv);
	}
	
	List<PimsSubjectDisposition> getSubjectDispositions(String studyId, String dataStandard, List<String> locList) throws Exception {
		RestServiceCaller.addAcceptedMediaType(MediaType.APPLICATION_XML);		
		//example URI - Note that what we call study completion data is referred to in CDS as subject-disposition
		//		�/pims/data/new-haven/study/B7471001/cdisc/subject-disposition
		List<PimsSubjectDisposition> subjectDispositions = new ArrayList<PimsSubjectDisposition>();
	//	User user = new User(Props.getServiceAccountUser2(), Props.getServiceAccountPassword2());

		for (String location : locList) {
			String subjectDispositionUri = PIMS_BASE_DATA_URI + "/" + location + PIMS_STUDY + "/" + studyId + "/" + dataStandard + SUBJECT_DISPOSITION_SUFFIX; 
			RestServiceResult rsr = makePimsCall(subjectDispositionUri, PimsOAuthTokenAccess.getAccessToken());

			subjectDispositions.addAll(rsr.getObjectList(PimsSubjectDispositionRepresentation.class));
			log.info("PIMS study call for location=" + location + ", studyId=" + studyId + " for subject disposition returned " + subjectDispositions.size() + " elements.");
		}
		
		return subjectDispositions;
	}
	
	private int convertSubjectDispositionsToCSV(List<PimsSubjectDisposition> subjectDispositions, StringBuffer csv) {
		int rowCount = 0;
		
		createSubjectDispositionsHeader(csv);
		
		for(PimsSubjectDisposition dv : subjectDispositions ){
			nullCheck(csv,dv.getCountry());
			csv.append(",");
//			nullCheck(csv,dv.getDataStandard());
//			csv.append(",");
			nullCheck(csv,dv.getDsdtf());
			csv.append(",");
			nullCheck(csv,dv.getDsphase());
			csv.append(",");
			nullCheck(csv,dv.getDsreas());
			csv.append(",");
			nullCheck(csv,dv.getDsrsoth());
			csv.append(",");
			nullCheck(csv,dv.getDsstat());
			csv.append(",");
			nullCheck(csv,dv.getPeriod());
			csv.append(",");
			nullCheck(csv,dv.getPeriodc());
			csv.append(",");
			nullCheck(csv,dv.getPhase());
			csv.append(",");
			nullCheck(csv,dv.getPhasec());
			csv.append(",");
			nullCheck(csv,dv.getSiteid());
			csv.append(",");
			nullCheck(csv,dv.getSperiod());
			csv.append(",");
			nullCheck(csv,dv.getSperiodc());
			csv.append(",");
			nullCheck(csv,dv.getStudyid());
			csv.append(",");
			nullCheck(csv,dv.getSubevenf());
			csv.append(",");
			nullCheck(csv,dv.getSubjid());
			csv.append(",");
			nullCheck(csv,dv.getVisit());
			csv.append(",");
			nullCheck(csv,dv.getVisnumf());
			csv.append(",");
			csv.append(dv.getItem());
			csv.append(",");
			if( dv.getLastUpdateDate() != null ){
				nullCheck(csv,dv.getLastUpdateDate().toString());
			}
			else{
				csv.append("");
			}
			
			csv.append("\r\n");
			rowCount++;
		}
		
		return rowCount;
	}

	private void createSubjectDispositionsHeader(StringBuffer csv) {
		csv.append("Country".toUpperCase());
		csv.append(",");
//		csv.append("DataStandard".toUpperCase());
//		csv.append(",");
		csv.append("Dsdtf".toUpperCase());
		csv.append(",");
		csv.append("Dsphase".toUpperCase());
		csv.append(",");
		csv.append("Dsreas".toUpperCase());
		csv.append(",");
		csv.append("Dsrsoth".toUpperCase());
		csv.append(",");
		csv.append("Dsstat".toUpperCase());
		csv.append(",");
		csv.append("Period".toUpperCase());
		csv.append(",");
		csv.append("Periodc".toUpperCase());
		csv.append(",");
		csv.append("Phase".toUpperCase());
		csv.append(",");
		csv.append("Phasec".toUpperCase());
		csv.append(",");
		csv.append("Siteid".toUpperCase());
		csv.append(",");
		csv.append("Speriod".toUpperCase());
		csv.append(",");
		csv.append("Speriodc".toUpperCase());
		csv.append(",");
		csv.append("Studyid".toUpperCase());
		csv.append(",");
		csv.append("Subevenf".toUpperCase());
		csv.append(",");
		csv.append("Subjid".toUpperCase());
		csv.append(",");
		csv.append("Visit".toUpperCase());
		csv.append(",");
		csv.append("Visnumf".toUpperCase());
		csv.append(",");
		csv.append("Item".toUpperCase());
		csv.append(",");
		csv.append("LastUpdateDate".toUpperCase());
		
		csv.append("\r\n");
	}


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStudyAlias() {
		return studyAlias;
	}

	public void setStudyAlias(String studyAlias) {
		this.studyAlias = studyAlias;
	}

	public String isPIMSStudy(String studyId) throws Exception {
		PimsStudyLocations locs = null;
		String retVal = null;
		
		try {
			locs = getPimsStudyLocations(studyId);
			
			if( locs == null || locs.getLocations().size() == 0 ) {
				retVal = "NON-PIMS";
			}
			else {
				retVal = "PIMS";
			}
		}
		catch(Exception ex) {
			if( ex.getMessage().contains("null")) {
				log.info("PIMS Location call returned a NON-PIMS answer" + ex.getMessage());
				retVal = "NON-PIMS";
			}
			else {
				throw ex;
			}
		}
		
		return retVal;
	}
}

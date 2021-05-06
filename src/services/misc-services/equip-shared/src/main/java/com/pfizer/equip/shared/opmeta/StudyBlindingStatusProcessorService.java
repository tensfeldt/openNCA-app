package com.pfizer.equip.shared.opmeta;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pfizer.equip.shared.contentrepository.DepthType;
import com.pfizer.equip.shared.contentrepository.RepositoryService;
import com.pfizer.equip.shared.contentrepository.exceptions.NodeNotFoundException;
import com.pfizer.equip.shared.opmeta.entity.GraabsStudiesResponse;
import com.pfizer.equip.shared.opmeta.entity.GraabsStudyData;
import com.pfizer.equip.shared.opmeta.entity.Protocol;
import com.pfizer.equip.shared.opmeta.entity.UserProtocol;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;
import com.pfizer.equip.shared.utils.BeanUtils;

@Service
public class StudyBlindingStatusProcessorService {
   private static final Logger logger = LoggerFactory.getLogger(OperationalMetadataRepositoryService.class);
   private static final String SYSTEM_USER = AuditService.SYSTEM_USER;

   @Autowired
   private RepositoryService repositoryService;

   @Autowired
   private OperationalMetadataRepositoryService operationalMetadataRepositoryService;

   @Autowired
   private AuditService auditService;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   @Autowired
   private SharedApplicationProperties properties;

   private RestTemplate restTemplate;
   private String graabsBaseUrl;

   @PostConstruct
   private void initialize() {
      // for now this service uses basic authentication
      // this may change in the future
      String serviceUser = properties.getGraabsUser();
      String servicePassword = properties.getGraabsPassword();
      restTemplate = restTemplateBuilder.basicAuthorization(serviceUser, servicePassword).build();
      graabsBaseUrl = properties.getGraabsBaseUrl() + "graabs/studies";
   }

   private void handleHttpError(HttpStatusCodeException e) {
      if (e instanceof HttpClientErrorException) {
         throw new HttpClientErrorException(e.getStatusCode(), "GRAABS service returned 4xx (client error): " + e.getResponseBodyAsString());
      } else if (e instanceof HttpServerErrorException) {
         throw new HttpServerErrorException(e.getStatusCode(), "GRAABS service returned 5xx (server error): " + e.getResponseBodyAsString());
      } else {
         throw e;
      }
   }

   private GraabsStudiesResponse getGraabsStudiesList() {
      HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      GraabsStudiesResponse response = null;
      try {
         response = restTemplate.exchange(graabsBaseUrl, HttpMethod.GET, new HttpEntity<>(headers), GraabsStudiesResponse.class).getBody();
      } catch (HttpStatusCodeException e) {
         logger.error("Retrieving GRAABS studies list failed.");
         handleHttpError(e);
      }
      return response;
   }

   /**
    * 1. Iterate over every study ID returned by GRAABS
    * 2. If present in Modeshape:
    * - Determine if blinding source flag is set to GRAABS already. If not, set to GRAABS. Mark as modified.
    * - If blinding source is already set to GRAABS, check to see if the study blinding value changed. Mark as modified.
    * 3. If study was marked as modified, create new snapshot.
    * 4. Create audit entry for update of snapshot for study.
    * @throws ExecutionException 
    */
   public boolean updateStudyBlindingStatus() throws IOException, ExecutionException {
      GraabsStudiesResponse response = getGraabsStudiesList();
      long records = response.getStudyBlindingStatusSearchResults().getNumberOfRecordsFound();
      boolean performedUpdate = false;

      if (records > 0) {
         for (GraabsStudyData graabsStudyInfo : response.getStudyBlindingStatusSearchResults().getStudyBlindingStatuses()) {
            String protocolId = graabsStudyInfo.getProtocolId();
            Protocol protocol = new Protocol();
            // TODO: cannot assume program code is the first 4 characters
            String programCode = protocolId.substring(0, 4);
            protocol.setProgramCode(programCode);
            protocol.setStudyId(protocolId);

            // check to see if this is in Modeshape
            Protocol masterProtocol;
            boolean createNewSnapshot = false;
            boolean graabsIsBlinded = StudyBlindingStatus.fromString(graabsStudyInfo.getStudyBlindingStatus()) == StudyBlindingStatus.BLINDED;
            try {
               masterProtocol = repositoryService.getNode(Protocol.class, protocol.getPath());
            } catch (NodeNotFoundException e) {
               continue;
            }

            // check the blinding status source
            Protocol currentSnapshot = repositoryService.getNodeByUrl(Protocol.class, masterProtocol.getCurrentSnapshot(), DepthType.WITH_FOLDERS_AND_CHILD_RECORDS);
            // explicitly set the program code
            // for some strange reason the program code was not being set for the snapshot
            currentSnapshot.setProgramCode(programCode);
            StudyBlindingStatusSource source = StudyBlindingStatusSource.fromString(currentSnapshot.getStudyBlindingStatusSource());

            boolean studyIsBlinded = StudyBlindingStatus.fromString(currentSnapshot.getStudyBlindingStatus()) == StudyBlindingStatus.BLINDED;
            boolean isManualSource = source != StudyBlindingStatusSource.GRAABS;
            boolean blindingStatusChanged = graabsIsBlinded != studyIsBlinded;
            boolean isCurrentSnapshotDateOutdated = currentSnapshot.getStudyBlindingStatusDate() == null || graabsStudyInfo.getBlindingStatusDate().compareTo(currentSnapshot.getStudyBlindingStatusDate()) != 0;

            if (isManualSource || blindingStatusChanged || isCurrentSnapshotDateOutdated) {
               createNewSnapshot = true;
            }

            if (createNewSnapshot) {
               Protocol userProtocol = new Protocol();
               // filter out non-user-editable fields

               BeanUtils.copyNonNullProperties(currentSnapshot, userProtocol, null, "children");
               userProtocol.setStudyBlindingStatusSource(StudyBlindingStatusSource.GRAABS.getValue());
               userProtocol.setIsStudyBlinded(graabsIsBlinded);

               Date graabsStatusDate = graabsStudyInfo.getBlindingStatusDate();
               userProtocol.setStudyBlindingStatusDate(graabsStatusDate);

               if (currentSnapshot.getCandidateProjectUuid() != null) {
            	   userProtocol.setCandidateProjectUuid(repositoryService.getIdByUrl(currentSnapshot.getCandidateProjectUuid()));
               }

               operationalMetadataRepositoryService.updateEntity(userProtocol);
               
               // set audit entries based on conditions met above
               if (isManualSource) {
                  String action = String.format("Study blinding source changed, status source from Manual to GRAABS, old snapshot = %s",
                        repositoryService.getIdByUrl(masterProtocol.getCurrentSnapshot()));
                  auditService.insertAuditEntry(new AuditEntryInput(action, protocolId, EntityType.PROTOCOL.getValue(), SYSTEM_USER, ActionStatusType.SUCCESS, null));
               }
               if (blindingStatusChanged) {
                  String oldBlindingStatus = graabsIsBlinded ? StudyBlindingStatus.UNBLINDED.getValue() : StudyBlindingStatus.BLINDED.getValue();
                  String newBlindingStatus = graabsIsBlinded ? StudyBlindingStatus.BLINDED.getValue() : StudyBlindingStatus.UNBLINDED.getValue();
                  String action = String.format("Study blinding fields changed, status changed from %s to %s, old snapshot = %s", oldBlindingStatus, newBlindingStatus,
                        repositoryService.getIdByUrl(masterProtocol.getCurrentSnapshot()));
                  auditService.insertAuditEntry(new AuditEntryInput(action, protocolId, EntityType.PROTOCOL.getValue(), SYSTEM_USER, ActionStatusType.SUCCESS, null));
               }

               performedUpdate = true;
            }
         }
      }

      return performedUpdate;
   }
}

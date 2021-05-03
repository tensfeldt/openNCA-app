package tests;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.LibraryReference;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.Promotion;
import com.pfizer.pgrd.equip.dataframe.dto.PublishEvent;
import com.pfizer.pgrd.equip.dataframe.dto.QCRequest;
import com.pfizer.pgrd.equip.dataframe.dto.QCWorkflowItem;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEvent;
import com.pfizer.pgrd.equip.dataframe.dto.Script;
import com.pfizer.pgrd.equip.dataframe.dto.equipinterface.EquipMetadatable;


//this will create json files using MoXY for each of the java objects

public class TestJSONCreation {
	private static final Gson GSON = new Gson();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDataframeJSON() throws Exception {
		Dataframe dataframe= new Dataframe();
		dataframe.setCreatedBy("Michelle");
		//dataframe.setEquipId("12345");
		dataframe.setDataframeType("Dataset");
		dataframe.setCreated(new Date());
		dataframe.setVersionNumber((long)1.0);
		dataframe.setVersionSuperSeded(false);
		//dataframe.getMetadata().put("Study Blinded", "Y");
		//dataframe.getMetadata().put("StudyID", "12345");
		
		this.addMetadata(dataframe);
		
        // Output JSON
		System.out.println();
        FileWriter fw = new FileWriter("data/dataframe-test.json");
        String json = GSON.toJson(dataframe);
        fw.write(json);
        System.out.println();
	}
	
	@Test
	public void testQCRequestJSON() throws Exception {
		QCRequest qcrequest = new QCRequest();
		
		qcrequest.setQcDueDate(new Date());
		//qcrequest.getMetadata().put("Study Blinded", "Y");
		//qcrequest.getMetadata().put("Study Parameter", "12345");
		
		this.addMetadata(qcrequest);
		
		LibraryReference ref = new LibraryReference();
		ref.setLibraryRef("AAAAAAAA");
		qcrequest.setChecklistTemplateId(ref);

        // Output JSON
        String json = GSON.toJson(qcrequest);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/qcrequest-test.json");
        fw.write(json);
        System.out.println();

	}
	@Test
	public void testQCWorkflowItemJSON() throws Exception {
		QCWorkflowItem qcwi = new QCWorkflowItem();
		
		qcwi.setQcStatus("In process");
		//qcwi.getMetadata().put("Study Blinded", "Y");
		//qcwi.getMetadata().put("Study Parameter", "12345");
		
		this.addMetadata(qcwi);
		
		qcwi.setAssignedReviewer("Test Person");

        // Output JSON
        String json = GSON.toJson(qcwi);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/qcwi-test.json");
        fw.write(json);
        System.out.println();

	}
	
	
	@Test
	public void testAssemblyJSON() throws Exception {
		Assembly assembly= new Assembly();
		assembly.setCreatedBy("Michelle");
		//assembly.setEquipId("12345");
		assembly.setAssemblyType("Data Load");
		assembly.setCreated(new Date());
		assembly.setLoadStatus("Not Yet Loaded");
		
		//assembly.getMetadata().put("Study Blinded", "Y");
		//assembly.getMetadata().put("Study Parameter", "12345");
		
		this.addMetadata(assembly);

        // Output JSON
        String json = GSON.toJson(assembly);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/assembly-test.json");
        fw.write(json);
        System.out.println();

	}
	
	@Test
	public void testReportingEventJSON() throws Exception {
		ReportingEvent re= new ReportingEvent();
		re.setCreatedBy("Michelle");
		re.setCreated(new Date());
		
        // Output JSON
        String json = GSON.toJson(re);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/re-test.json");
        fw.write(json);
        System.out.println();

	}
	@Test
	public void testPromotionJSON() throws Exception {
		Promotion p= new Promotion();
		p.setCreatedBy("Michelle");
		p.setCreated(new Date());
		
        // Output JSON
        String json = GSON.toJson(p);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/promotion-test.json");
        fw.write(json);
        System.out.println();

	}
	@Test
	public void testPublishingEventJSON() throws Exception {
		PublishEvent pe= new PublishEvent();
		pe.setCreatedBy("Michelle");
		pe.setCreated(new Date());
		
        // Output JSON
        String json = GSON.toJson(pe);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/publishingevent-test.json");
        fw.write(json);
        System.out.println();
	}	
	
	
	
	@Test
	public void testCommentJSON() throws Exception {
		Comment comment= new Comment();
		comment.setCreatedBy("Michelle");
		comment.setBody("This is the body of my comment");
		comment.setCommentType("Data Load");
		comment.setCreated(new Date());
		
		//comment.getMetadata().put("Study Blinded", "Y");
		//comment.getMetadata().put("Study Parameter", "12345");
		
		this.addMetadata(comment);
		
        // Output JSON
        String json = GSON.toJson(comment);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/comment-test.json");
        fw.write(json);
        System.out.println();

	}
	
	@Test
	public void testDatasetJSON() throws Exception {
		Dataset dataset= new Dataset();
		dataset.setId("4dd1a309-ce40-462d-8f67-555e0eb57cee");
	
		//dataset.getMetadata().put("Study Blinded", "Y");
		//dataset.getMetadata().put("StudyID", "12345");
		
		this.addMetadata(dataset);
		
		dataset.setData("aaaaaaaaaaaa");
		dataset.setComplexDataId("629b7f93-69a6-4f62-abd1-9cb3260df8ba");
		
        // Output JSON
        String json = GSON.toJson(dataset);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/dataset-test.json");
        fw.write(json);
        System.out.println();

	}
	@Test
	public void testMetadataJSON() throws Exception {
		/*Map<String, String> map = new HashMap<>();

	
		map.put("Study Blinded", "Y");
		map.put("StudyID", "12345");*/
		
		List<Metadatum> map = generateMetadata();
        // Output JSON
		String json = GSON.toJson(map);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/metadata-test.json");
        fw.write(json);
        System.out.println();

	}
	
	@Test
	public void testScriptJSON() throws Exception {
		Script script = new Script();
		script.setCreatedBy("Michelle");
		LibraryReference ref = new LibraryReference();
		ref.setLibraryRef("123455667");
		script.setScriptBody(ref);
		script.setCreated(new Date());
	
		//script.getMetadata().put("Study Blinded", "Y");
		//script.getMetadata().put("StudyID", "12345");
		
		this.addMetadata(script);

        // Output JSON
        String json = GSON.toJson(script);
        System.out.println(json);
        System.out.println();
        FileWriter fw = new FileWriter("data/script-test.json");
        fw.write(json);
        System.out.println();
	}
	
	private void addMetadata(EquipMetadatable eo) {
		if(eo != null) {
			List<Metadatum> list = generateMetadata();
			eo.setMetadata(list);
		}
	}
	
	private List<Metadatum> generateMetadata() {
		List<Metadatum> list = new ArrayList<>();
		
		List<String> l1 = new ArrayList<>();
		l1.add("true");
		
		List<String> l2 = new ArrayList<>();
		l2.add("12345");
		
		list.add(new Metadatum("Study Blinded", l1, Metadatum.BOOLEAN_TYPE));
		list.add(new Metadatum("Study ID", l2, Metadatum.STRING_TYPE));
		
		return list;
	}
}

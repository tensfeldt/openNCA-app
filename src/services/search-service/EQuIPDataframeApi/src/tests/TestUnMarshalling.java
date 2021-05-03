package tests;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.pfizer.pgrd.equip.dataframe.dto.Dataframe;
import com.pfizer.pgrd.equip.dataframe.dto.Dataset;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;

public class TestUnMarshalling {
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
	public void testDataframeUnmarshalling() throws Exception {
		String json = this.fileToString("data/dataframe-test.json");
		Dataframe dataframe = GSON.fromJson(json, Dataframe.class);
		System.out.println(dataframe);
		System.out.println();
	}

	@Test
	public void testDatasetUnmarshalling() throws Exception {
		String json = this.fileToString("data/dataset-test.json");
		Dataset dataset = GSON.fromJson(json, Dataset.class);
		System.out.println(dataset);
		System.out.println();
	}

	@Test
	public void testMetadataUnmarshalling() throws Exception {
		String json = this.fileToString("data/metadata-test.json");
		Metadatum[] metadata = GSON.fromJson(json, Metadatum[].class);

		System.out.println(metadata);
		System.out.println();
	}

	private String fileToString(String path) {
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(path)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
}

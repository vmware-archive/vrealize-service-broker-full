package org.cloudfoundry.community.servicebroker.vrealize;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class CatalogTest {

	@Autowired
	Gson gson;

	@Test
	public void testCatalog() throws Exception {
		JsonElement je = gson.fromJson(getJson("catalog.json"),
				JsonElement.class);

		Catalog c = gson.fromJson(je, Catalog.class);
		assertNotNull(c);
		// System.out.println(gson.toJson(c));
	}

	private String getJson(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(new ClassPathResource(
				fileName).getURI())));
	}

}

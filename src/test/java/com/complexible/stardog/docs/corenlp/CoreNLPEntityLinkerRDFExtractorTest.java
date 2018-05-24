package com.complexible.stardog.docs.corenlp;

import java.io.FileInputStream;

import com.beust.jcommander.internal.Lists;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.docs.StardocsConnection;
import com.complexible.stardog.search.SearchOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import static com.complexible.common.rdf.model.Values.iri;
import static com.complexible.common.rdf.model.Values.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPEntityLinkerRDFExtractorTest {
	private static final String DB = "testExtractor";

	private static Stardog stardog;

	@BeforeClass
	public static void beforeClass() throws Exception {
		stardog = Stardog.builder().create();

		try (AdminConnection aConn = AdminConnectionConfiguration.toEmbeddedServer()
		                                                         .credentials("admin", "admin")
		                                                         .connect()) {
			if (aConn.list().contains(DB)) {
				aConn.drop(DB);
			}

			aConn.newDatabase(DB).set(SearchOptions.SEARCHABLE, true).create();
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		stardog.shutdown();
	}

	@Test
	public void testEntityLinker() throws Exception {

		IRI aBaltimore = iri("urn:Baltimore");

		try (Connection aConn = ConnectionConfiguration
			                        .to(DB)
			                        .credentials("admin", "admin")
			                        .connect()) {

			aConn.begin();
			aConn.add().statement(aBaltimore, RDFS.LABEL, literal("Baltimore"));
			aConn.commit();
		}

		try (Connection aConn = ConnectionConfiguration
			                        .to(DB)
			                        .credentials("admin", "admin")
			                        .connect()) {

			StardocsConnection aDocsConn = aConn.as(StardocsConnection.class);

			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("input.txt"),
				Lists.newArrayList("CoreNLPEntityLinkerRDFExtractor"),
				null
			);

			String aQuery = "select ?ref ?label {" +
			                "   graph ?doc {" +
			                "       ?doc <tag:stardog:api:docs:hasEntity> ?entity ." +
			                "       ?entity <http://purl.org/dc/terms/references> ?ref ;" +
			                "               rdfs:label ?label . " +
			                "   }" +
			                "}";
			TupleQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute();

			BindingSet aBindings = aRes.next();

			assertEquals(aBaltimore, aBindings.getValue("ref"));
			assertEquals(literal("Baltimore"), aBindings.getValue("label"));
			assertFalse(aRes.hasNext());
		}
	}

}

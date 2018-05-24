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
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import static com.complexible.common.rdf.model.Values.iri;
import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPRelationRDFExtractorTest {
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
	public void testRelationExtractor() throws Exception {
		try (Connection aConn = ConnectionConfiguration
			                        .to(DB)
			                        .credentials("admin", "admin")
			                        .connect()) {
			StardocsConnection aDocsConn = aConn.as(StardocsConnection.class);

			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("input.txt"),
				Lists.newArrayList("CoreNLPRelationRDFExtractor"),
				null
			);

			String aQuery = "select ?subjLabel ?pred ?objLabel {" +
			                "   graph ?doc {" +
			                "       ?subj ?pred ?obj ." +
			                "       ?subj rdfs:label ?subjLabel ." +
			                "       ?obj rdfs:label ?objLabel " +
			                "   }" +
			                "}" +
			                "order by ?objLabel";

			TupleQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute();

			BindingSet aBindings = aRes.next();
			assertEquals("Baltimore Orioles", aBindings.getValue("subjLabel").stringValue());
			assertEquals(iri("tag:stardog:api:docs:relation:org:city_of_headquarters"), aBindings.getValue("pred"));
			assertEquals("Baltimore", aBindings.getValue("objLabel").stringValue());

			aBindings = aRes.next();
			assertEquals("Baltimore Orioles", aBindings.getValue("subjLabel").stringValue());
			assertEquals(iri("tag:stardog:api:docs:relation:org:stateorprovince_of_headquarters"), aBindings.getValue("pred"));
			assertEquals("Maryland", aBindings.getValue("objLabel").stringValue());

			aBindings = aRes.next();
			assertEquals("Baltimore Orioles", aBindings.getValue("subjLabel").stringValue());
			assertEquals(iri("tag:stardog:api:docs:relation:org:country_of_headquarters"), aBindings.getValue("pred"));
			assertEquals("United States", aBindings.getValue("objLabel").stringValue());
		}
	}
}

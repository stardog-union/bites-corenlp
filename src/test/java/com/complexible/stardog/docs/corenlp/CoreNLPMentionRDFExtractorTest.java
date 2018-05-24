package com.complexible.stardog.docs.corenlp;

import java.io.FileInputStream;
import java.util.Set;

import com.beust.jcommander.internal.Lists;
import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.docs.StardocsConnection;
import com.complexible.stardog.search.SearchOptions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.query.TupleQueryResult;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPMentionRDFExtractorTest {
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
	public void testMentionExtractor() throws Exception {
		try (Connection aConn = ConnectionConfiguration
			                        .to(DB)
			                        .credentials("admin", "admin")
			                        .connect()) {
			StardocsConnection aDocsConn = aConn.as(StardocsConnection.class);

			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("input.txt"),
				Lists.newArrayList("CoreNLPMentionRDFExtractor"),
				null
			);

			String aQuery = "select ?label {" +
			                "   graph ?doc {" +
			                "       ?doc <tag:stardog:api:docs:hasEntity> ?entity ." +
			                "       ?entity rdfs:label ?label " +
			                "   }" +
			                "}";
			TupleQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute();

			Set<String> aEntities = Sets.newHashSet();
			while (aRes.hasNext()) {
				aEntities.add(aRes.next().getBinding("label").getValue().stringValue());
			}

			assertEquals(
				ImmutableSet.of(
				"Baltimore",
				"United States",
				"Maryland",
				"Baltimore Orioles",
				"Eastern Division of Major League Baseball 's American League")
				, aEntities);
		}
	}
}

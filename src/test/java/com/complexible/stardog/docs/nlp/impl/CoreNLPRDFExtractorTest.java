/*
 * bites-corenlp
 * Copyright (C) 2018 Stardog Union <http://stardog.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.complexible.stardog.docs.nlp.impl;

import java.io.FileInputStream;
import java.util.Set;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.protocols.http.client.HttpConnection;
import com.complexible.stardog.protocols.http.docs.client.HttpBitesConnection;
import com.complexible.stardog.search.SearchOptions;
import com.stardog.stark.IRI;
import com.stardog.stark.Value;
import com.stardog.stark.query.BindingSet;
import com.stardog.stark.query.SelectQueryResult;
import com.stardog.stark.vocabs.RDFS;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.stardog.stark.Values.iri;
import static com.stardog.stark.Values.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Pedro Oliveira
 */
public class CoreNLPRDFExtractorTest {
	private static final String DB = "testExtractor";

	private static ConnectionConfiguration mConnectionConfiguration;

	@BeforeClass
	public static void beforeClass() {

		try (AdminConnection aConn = AdminConnectionConfiguration.toServer("http://localhost:5820")
		                                                         .credentials("admin", "admin")
		                                                         .connect()) {
			if (aConn.list().contains(DB)) {
				aConn.drop(DB);
			}

			mConnectionConfiguration = aConn.newDatabase(DB).set(SearchOptions.SEARCHABLE, true).create();
		}
	}

	@After
	public void clear() {
		try (Connection aConn = mConnectionConfiguration.connect()) {
			aConn.begin();
			aConn.remove().all();
			aConn.commit();
		}
	}

	@Test
	public void testMentionExtractor() throws Exception {
		try (Connection aConn = mConnectionConfiguration.connect()) {
			HttpBitesConnection aDocsConn = new HttpBitesConnection(aConn.as(HttpConnection.class));

			// add document
			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("data/input.txt"),
				Lists.newArrayList("CoreNLPMentionRDFExtractor"),
				null
			);

			// get label of all extracted entities
			String aQuery = "select ?label {" +
			                "   graph ?doc {" +
			                "       ?doc <tag:stardog:api:docs:hasEntity> ?entity ." +
			                "       ?entity rdfs:label ?label " +
			                "   }" +
			                "}";
			try (SelectQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute()) {

				Set<String> aEntities = Sets.newHashSet();
				while (aRes.hasNext()) {
					aEntities.add(Value.lex(aRes.next().binding("label").get().value()));
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

	@Test
	public void testEntityLinker() throws Exception {
		IRI aBaltimore = iri("urn:Baltimore");

		try (Connection aConn = mConnectionConfiguration.connect()) {

			aConn.begin();
			aConn.add().statement(aBaltimore, RDFS.LABEL, literal("Baltimore"));
			aConn.commit();
		}

		try (Connection aConn = mConnectionConfiguration.connect()) {

			HttpBitesConnection aDocsConn = new HttpBitesConnection(aConn.as(HttpConnection.class));

			// add document
			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("data/input.txt"),
				Lists.newArrayList("CoreNLPEntityLinkerRDFExtractor"),
				null
			);

			// select linked entity, urn:Baltimore
			String aQuery = "select ?ref ?label {" +
			                "   graph ?doc {" +
			                "       ?doc <tag:stardog:api:docs:hasEntity> ?entity ." +
			                "       ?entity <http://purl.org/dc/terms/references> ?ref ;" +
			                "               rdfs:label ?label . " +
			                "   }" +
			                "}";
			try (SelectQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute()) {
				BindingSet aBindings = aRes.next();

				assertEquals(aBaltimore, aBindings.binding("ref").get().get());
				assertEquals(literal("Baltimore"), aBindings.binding("label").get().value());
				assertFalse(aRes.hasNext());
			}
		}
	}

	@Test
	public void testRelationExtractor() throws Exception {
		try (Connection aConn = mConnectionConfiguration.connect()) {
			HttpBitesConnection aDocsConn = new HttpBitesConnection(aConn.as(HttpConnection.class));

			// add document
			IRI aDocIri = aDocsConn.putDocument(
				"input",
				new FileInputStream("data/input.txt"),
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

			try (SelectQueryResult aRes = aConn.select(aQuery).parameter("doc", aDocIri).execute()) {
				// assert extracted relations
				BindingSet aBindings = aRes.next();
				assertEquals("Baltimore Orioles", Value.lex(aBindings.binding("subjLabel").get().value()));
				assertEquals(iri("tag:stardog:api:docs:relation:org:city_of_headquarters"), aBindings.binding("pred").get().value());
				assertEquals("Baltimore", Value.lex(aBindings.binding("objLabel").get().value()));

				aBindings = aRes.next();
				assertEquals("Baltimore Orioles", Value.lex(aBindings.binding("subjLabel").get().value()));
				assertEquals(iri("tag:stardog:api:docs:relation:org:stateorprovince_of_headquarters"), aBindings.binding("pred").get().value());
				assertEquals("Maryland", Value.lex(aBindings.binding("objLabel").get().value()));

				aBindings = aRes.next();
				assertEquals("Baltimore Orioles", Value.lex(aBindings.binding("subjLabel").get().value()));
				assertEquals(iri("tag:stardog:api:docs:relation:org:country_of_headquarters"), aBindings.binding("pred").get().value());
				assertEquals("United States", Value.lex(aBindings.binding("objLabel").get().value()));

				assertFalse(aRes.hasNext());
			}
		}
	}
}

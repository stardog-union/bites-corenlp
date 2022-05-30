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

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.nlp.EntityExtractor;
import com.complexible.stardog.docs.nlp.Spans;
import com.stardog.stark.IRI;
import com.stardog.stark.Statement;

import com.google.common.collect.Sets;

/**
 * {@link EntityLinker} using {@link CoreNLPDocumentParser}
 *
 * <pre>
 * {@code
 *
 * "The Orioles are a team based in Baltimore"
 *
 * iri:1    rdfs:label "Orioles" ;
 *          a ner:organization ;
 *          dcterms:references db:Baltimore .
 *
 * }
 * </pre>
 *
 * @author Pedro Oliveira
 */
public class CoreNLPEntityLinkerRDFExtractor extends AbstractEntityRDFExtractor {

	@Override
	protected StatementSource extractFromText(final Connection theConnection, final IRI theDocIri, final Reader theReader) throws Exception {
		EntityLinker aLinker =  new EntityLinker(
			new CoreNLPDocumentParser(),                            // parser
			new NERMentionExtractor(),                              // mention extractor
			c -> c,                                                 // filter (no-op)
			new DefaultCandidateFeatureGenerator(theConnection),    // candidate and feature generator
			c -> {},                                                // feature generator (no-op, subsumed by DefaultCandidateFeatureGenerator)
			new MaxRanking(),                                       // ranking function
			new TopThresholdSelector(0.95)              // candidate selector
		);

		Set<Statement> aGraph = Sets.newHashSet();

		// add each entity and its links to the model
		final Spans aSpans = aLinker.extract(theReader);

		aSpans.stream().forEach(aEntity -> addEntity(aGraph, theDocIri, aEntity, false, true, aSpans.getLinkedEntities(aEntity)));

		return MemoryStatementSource.of(aGraph);
	}

	@Override
	public EntityExtractor<Spans> getExtractor(Connection theConnection) throws IOException {
		return BasicMentionExtractor.getDefault(theConnection);
	}
}

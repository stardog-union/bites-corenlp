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

package com.complexible.stardog.docs.corenlp;

import java.io.Reader;
import java.util.Set;

import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.nlp.Span;
import com.complexible.stardog.docs.nlp.impl.AbstractEntityRDFExtractor;
import com.complexible.stardog.docs.nlp.impl.BasicMentionExtractor;
import com.complexible.stardog.docs.nlp.impl.NERMentionExtractor;
import com.stardog.stark.IRI;
import com.stardog.stark.Statement;

import com.google.common.collect.Sets;

/**
 * {@link BasicMentionExtractor} using {@link CoreNLPDocumentParser}
 *
 * <pre>
 * {@code
 *
 * "The Orioles are a team based in Baltimore"
 *
 * iri:1    rdfs:label "Orioles" ;
 *          a ner:organization .
 * iri:2    rdfs:label "Baltimore" ;
 *          a ner:city .
 *
 * }
 * </pre>
 *
 * @author Pedro Oliveira
 */
public class CoreNLPMentionRDFExtractor extends AbstractEntityRDFExtractor {

	@Override
	protected StatementSource extractFromText(final Connection theConnection, final IRI theDocIri, final Reader theText) throws Exception {
		BasicMentionExtractor aExtractor = new BasicMentionExtractor(
			new CoreNLPDocumentParser(),    // parser
			new NERMentionExtractor()       // mention extractor
		);

		Set<Statement> aGraph = Sets.newHashSet();

		// add each entity to model
		for (Span aEntity : aExtractor.extract(theText)) {
			addEntity(aGraph, theDocIri, aEntity, false, true);
		}

		return MemoryStatementSource.of(aGraph);
	}
}

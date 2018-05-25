package com.complexible.stardog.docs.corenlp;

import java.io.Reader;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.nlp.Span;
import com.complexible.stardog.docs.nlp.impl.AbstractEntityRDFExtractor;
import com.complexible.stardog.docs.nlp.impl.DefaultCandidateFeatureGenerator;
import com.complexible.stardog.docs.nlp.impl.EntityLinker;
import com.complexible.stardog.docs.nlp.impl.MaxRanking;
import com.complexible.stardog.docs.nlp.impl.NERMentionExtractor;
import com.complexible.stardog.docs.nlp.impl.TopThresholdSelector;
import com.google.common.collect.Multimap;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;

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
			new TopThresholdSelector(0.95)                          // candidate selector
		);

		Multimap<Span, Resource> aOutput = aLinker.extract(theReader);

		Model aModel = Models2.newModel();

		// add each entity and its links to the model
		aOutput.asMap().forEach((aEntity, aValues) -> addEntity(aModel, theDocIri, aEntity, false, true, aValues));

		return MemoryStatementSource.of(aModel);
	}
}

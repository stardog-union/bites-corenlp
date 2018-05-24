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
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPEntityLinkerRDFExtractor extends AbstractEntityRDFExtractor {

	@Override
	protected StatementSource extractFromText(final Connection theConnection, final IRI theDocIri, final Reader theReader) throws Exception {

		EntityLinker aLinker =  new EntityLinker(
			new CoreNLPDocumentParser(),
			new NERMentionExtractor(),
			c -> c,
			new DefaultCandidateFeatureGenerator(theConnection),
			c -> {},
			new MaxRanking(),
			new TopThresholdSelector(0.95)
		);

		Multimap<Span, Resource> aOutput = aLinker.extract(theReader);

		Model aModel = Models2.newModel();

		aOutput.asMap().forEach((aEntity, aValues) -> addEntity(aModel, theDocIri, aEntity, false, true, aValues));

		return MemoryStatementSource.of(aModel);
	}
}

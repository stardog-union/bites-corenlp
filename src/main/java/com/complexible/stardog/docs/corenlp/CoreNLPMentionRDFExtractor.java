package com.complexible.stardog.docs.corenlp;

import java.io.Reader;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.nlp.Span;
import com.complexible.stardog.docs.nlp.impl.AbstractEntityRDFExtractor;
import com.complexible.stardog.docs.nlp.impl.BasicMentionExtractor;
import com.complexible.stardog.docs.nlp.impl.NERMentionExtractor;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;

/**
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPMentionRDFExtractor extends AbstractEntityRDFExtractor {

	@Override
	protected StatementSource extractFromText(final Connection theConnection, final IRI theDocIri, final Reader theText) throws Exception {
		BasicMentionExtractor aExtractor = new BasicMentionExtractor(new CoreNLPDocumentParser(), new NERMentionExtractor());

		// add each entity to model
		Model aModel = Models2.newModel();
		for (Span aEntity : aExtractor.extract(theText)) {
			addEntity(aModel, theDocIri, aEntity, false, true);
		}

		return MemoryStatementSource.of(aModel);
	}
}

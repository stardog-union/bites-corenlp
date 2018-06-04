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
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.StatementSource;
import com.complexible.common.rdf.impl.MemoryStatementSource;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.docs.StardocsVocabulary;
import com.complexible.stardog.docs.extraction.tika.TextProvidingRDFExtractor;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.RDFS;

import static com.complexible.common.rdf.model.Values.literal;

/**
 * Uses {@link StanfordCoreNLP} to extract relations from text.
 *
 * <pre>
 * {@code
 *
 * "The Orioles are a team based in Baltimore"
 *
 * iri:1 rdfs:label "Orioles"
 * iri:2 rdfs:label "Baltimore"
 * iri:1 relation:org:city_of_headquarters iri:2
 *
 * }
 * </pre>
 *
 * @author Pedro Oliveira
 */
public class CoreNLPRelationRDFExtractor extends TextProvidingRDFExtractor {

	private static StanfordCoreNLP PIPELINE;

	/**
	 * Lazily load the {@link StanfordCoreNLP} pipeline
	 */
	private synchronized static StanfordCoreNLP getPipeline() {
		if (PIPELINE == null) {
			Properties aProps = new Properties();
			aProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp");
			PIPELINE = new StanfordCoreNLP(aProps);
		}

		return PIPELINE;
	}

	@Override
	protected StatementSource extractFromText(final Connection theConnection, final IRI theIRI, final Reader theReader) throws Exception {

		CoreDocument aDoc = new CoreDocument(CharStreams.toString(theReader));
		getPipeline().annotate(aDoc);

		Model aModel = Models2.newModel();

		for (CoreSentence aSentence: aDoc.sentences()) {
			for (RelationTriple aRelation: aSentence.relations()) {
				String aSubjStr = toString(aRelation.canonicalSubject);
				String aPredStr = aRelation.relationHead().value();
				String aObjStr = toString(aRelation.canonicalObject);

				IRI aSubj = mention(aSubjStr);
				IRI aPred = relation(aPredStr);
				IRI aObj = mention(aObjStr);

				// add label to each entity
				aModel.add(aSubj, RDFS.LABEL, literal(aSubjStr));
				aModel.add(aObj, RDFS.LABEL, literal(aObjStr));

				// add triple with relation
				aModel.add(aSubj, aPred, aObj);
			}
		}

		return MemoryStatementSource.of(aModel);
	}

	private static String toString(List<CoreLabel> theLabels) {
		return theLabels.stream().map(CoreLabel::word).collect(Collectors.joining(" "));
	}

	private static IRI mention(String theEntity) {
		return StardocsVocabulary.ontology().term("entity:" + Hashing.murmur3_128().hashString(theEntity, Charsets.UTF_8).toString());
	}

	private static IRI relation(String theRelation) {
		return StardocsVocabulary.ontology().term("relation:" + theRelation);
	}

}

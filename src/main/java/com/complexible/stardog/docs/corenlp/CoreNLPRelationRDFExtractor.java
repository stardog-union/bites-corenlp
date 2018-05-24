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
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
 */
public class CoreNLPRelationRDFExtractor extends TextProvidingRDFExtractor {

	private static StanfordCoreNLP PIPELINE;

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

				aModel.add(aSubj, RDFS.LABEL, literal(aSubjStr));
				aModel.add(aObj, RDFS.LABEL, literal(aObjStr));
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

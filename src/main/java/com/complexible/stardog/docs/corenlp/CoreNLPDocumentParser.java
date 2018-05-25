package com.complexible.stardog.docs.corenlp;

import java.util.Properties;

import com.complexible.stardog.docs.nlp.Document;
import com.complexible.stardog.docs.nlp.DocumentParser;
import com.complexible.stardog.docs.nlp.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * Transform a {@link String} into a {@link Document} using {@link StanfordCoreNLP}
 *
 * @author Pedro Oliveira
 */
public class CoreNLPDocumentParser implements DocumentParser {

	private static StanfordCoreNLP PIPELINE;

	/**
	 * Lazily load the {@link StanfordCoreNLP} pipeline
	 */
	private synchronized static StanfordCoreNLP getPipeline() {
		if (PIPELINE == null) {
			Properties aProps = new Properties();
			aProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
			PIPELINE = new StanfordCoreNLP(aProps);
		}

		return PIPELINE;
	}

	@Override
	public Document apply(final String theText) {

		if (theText == null || theText.isEmpty()) {
			return new Document(0);
		}

		CoreDocument aCoreDoc = new CoreDocument(theText);
		getPipeline().annotate(aCoreDoc);

		Document aDoc = new Document(aCoreDoc.sentences().size());

		int aSentenceID = 0;
		int aMentionID = 0;
		for (CoreSentence aSentence: aCoreDoc.sentences()) {

			Token[] aTokens = new Token[aSentence.tokens().size()];
			aDoc.set(aSentenceID, aTokens);

			int aTokenID = 0;
			for (CoreLabel aLabel: aSentence.tokens()) {
				aTokens[aTokenID++] = new Token(aDoc, aSentenceID, aLabel.word());
			}

			for (CoreEntityMention aMention: aSentence.entityMentions()) {
				for (CoreLabel aLabel: aMention.tokens()) {
					Token aToken = aTokens[aLabel.index() - 1];
					aToken.put(Token.NER, aMention.entityType().toLowerCase());
					aToken.put(Token.NER_SPAN, Integer.toString(aMentionID));
				}

				aMentionID++;
			}

			aSentenceID++;
		}

		return aDoc;
	}
}

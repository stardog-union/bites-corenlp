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

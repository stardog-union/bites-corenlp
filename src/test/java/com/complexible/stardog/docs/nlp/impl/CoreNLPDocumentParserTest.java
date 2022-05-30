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

import java.util.Arrays;

import com.complexible.stardog.docs.nlp.Document;
import com.complexible.stardog.docs.nlp.Token;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Oliveira
 */
public class CoreNLPDocumentParserTest {

	private static CoreNLPDocumentParser parser() {
		return new CoreNLPDocumentParser();
	}

	@Test
	public void empty() throws Exception {
		Document aDoc = parser().apply("");
		assertEquals(0, aDoc.size());
	}

	@Test
	public void singleSentence() throws Exception {
		Document aDoc = parser().apply("The Baltimore Orioles are a professional baseball team based in Baltimore, Maryland in the United States");
		assertEquals(1, aDoc.size());

		assertEquals(1, countEntityTokens(aDoc.sentence(0), "city"));
		assertEquals(1, countEntityTokens(aDoc.sentence(0), "state_or_province"));
		assertEquals(2, countEntityTokens(aDoc.sentence(0), "country"));
		assertEquals(2, countEntityTokens(aDoc.sentence(0), "organization"));
	}

	@Test
	public void multipleSentences() throws Exception {
		Document aDoc = parser().apply("The Baltimore Orioles are a professional baseball team based in Baltimore, Maryland in the United States. They are a\n" +
		                               "member of the Eastern Division of Major League Baseball's American League.");
		assertEquals(2, aDoc.size());

		assertEquals(1, countEntityTokens(aDoc.sentence(0), "city"));
		assertEquals(1, countEntityTokens(aDoc.sentence(0), "state_or_province"));
		assertEquals(2, countEntityTokens(aDoc.sentence(0), "country"));
		assertEquals(2, countEntityTokens(aDoc.sentence(0), "organization"));

		assertEquals(9, countEntityTokens(aDoc.sentence(1), "organization"));
	}

	private long countEntityTokens(final Token[] theTokens, final String theCategory) {
		return Arrays.stream(theTokens).filter(t -> t.first(Token.NER).orElse("").equals(theCategory)).count();
	}
}

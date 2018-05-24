package com.complexible.stardog.docs.corenlp;

import java.util.Arrays;

import com.complexible.stardog.docs.nlp.Document;
import com.complexible.stardog.docs.nlp.Token;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Oliveira
 * @version 5.2.4
 * @since 5.2.4
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

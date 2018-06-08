This project implements three [custom RDF extractors](https://www.stardog.com/docs/#_custom_extractors) based on Stanford's [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) library.

## CoreNLPMentionRDFExtractor

Extracts named entities mentions, with the same output format as Stardog's `entities` [extractor](https://www.stardog.com/docs/#_entities).

## CoreNLPEntityLinkerRDFExtractor

Extracts and links entity mentions to existing resources in a knowledge graph. Same output format as Stardog's `linker` [extractor](https://www.stardog.com/docs/#_linker).

## CoreNLPRelationRDFExtractor

Extracts relations between named entity mentions. For example, the sentence:

`The Orioles are a professional baseball team based in Baltimore`

Will generate three triples:

```
entity:e435cd0347642bc7d2736155815a54e2 rdfs:label "Orioles"
entity:eb3cdb4e267d28feebb638711f8bd7b1 rdfs:label "Baltimore"
iri:e435cd0347642bc7d2736155815a54e2 relation:org:city_of_headquarters iri:eb3cdb4e267d28feebb638711f8bd7b1
```

# Usage

1. Download the [latest release](https://github.com/stardog-union/bites-corenlp/releases)
2. Add the jar to Stardog's [classpath](https://www.stardog.com/docs/#_extending_stardog):
	* Copy it to `server/ext` or other folder in the server (e.g., `server/dbms`)
	* OR
	* Point the environment variable `STARDOG_EXT` to the its folder
3. Restart the Stardog server
4. `CoreNLPMentionRDFExtractor`, `CoreNLPEntityLinkerRDFExtractor`, and `CoreNLPRelationRDFExtractor` will be available as [RDF extractors](https://www.stardog.com/docs/#_unstructured_data), accessible through the CLI, API, and HTTP interfaces

For example, using the CLI, if you want to add a document to BITES and extract its entities:

```bash
stardog doc put --rdf-extractors CoreNLPMentionRDFExtractor myDatabase document.pdf
```

CoreNLP models can consume large amounts of system memory. If greeted with a `GC overhead limit exceeded` error when using any of the extractors, increase the amount of [memory available](https://www.stardog.com/docs/#_memory_usage) to Stardog.

## Advanced Usage

1. Tweak `build.gradle` to the [language of your choice](https://stanfordnlp.github.io/CoreNLP/download.html) (e.g., change CoreNLP dependency to `models-spanish`)
2. Run `gradlew clean fatjar` for a single jar, or `gradlew clean copyDeps` for individual dependencies
3. Add files in `build/libs` to Stardog's [classpath](https://www.stardog.com/docs/#_extending_stardog)
4. Restart the Stardog server

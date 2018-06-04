This project implements three [custom RDF extractors](https://www.stardog.com/docs/#_custom_extractors) based on Stanford's [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) library.

## CoreNLPMentionRDFExtractor

Extracts named entities mentions, with the same output format as Stardog's `entities` [extractor](https://www.stardog.com/docs/#_entities).

## CoreNLPEntityLinkerRDFExtractor

Extracts and links entity mentions to existing resources in a knowledge graph. Same output format as Stardog's `linker` [extractor](https://www.stardog.com/docs/#_linker).

## CoreNLPRelationRDFExtractor

Extracts relations between named entity mentions. For example, the sentence:

`The Orioles are a team based in Baltimore`

Will generate three triples:

```
entity:f06574bbbfa1a5b474f276714e769027 rdfs:label "Orioles"
entity:679a56e43cd3beace9e4ba690824b055 rdfs:label "Baltimore"
iri:f06574bbbfa1a5b474f276714e769027 relation:org:city_of_headquarters iri:679a56e43cd3beace9e4ba690824b055
```

# Usage

1. Run `gradlew clean jar copyDeps`
2. Add all the jars in `build/libs` to Stardog's [classpath](https://www.stardog.com/docs/#_extending_stardog):
	* Copy them to `server/ext` or other folder in the server (e.g., `server/dbms`)
	* OR
	* Point the environment variable `STARDOG_EXT` to the `build/libs` folder
3. Restart the Stardog server
4. `CoreNLPMentionRDFExtractor`, `CoreNLPEntityLinkerRDFExtractor`, and `CoreNLPRelationRDFExtractor` will be available as [RDF extractors](https://www.stardog.com/docs/#_unstructured_data), accessible through the CLI, API, and HTTP interfaces

CoreNLP models can consume large amounts of system memory. If greeted with a `GC overhead limit exceeded` error when using any of the extractors, increase the amount of [memory available](https://www.stardog.com/docs/#_memory_usage) to Stardog.

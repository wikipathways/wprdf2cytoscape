// WikiPathways RDF 2 Cytoscape
// Copyright 2018 Department of Bioinformatics - BiGCaT, Maastricht University
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.wp.rdf2cy;

/**
 * SPARQL queries to retrieve data from
 * WikiPathways RDF (currently working only for
 * Blazegraph RDF store)
 * 
 * @author mkutmon
 *
 */
public class SparqlQuery {
	
	public String getPathwayListQuery(String species) {
		return 	"PREFIX wp: <http://vocabularies.wikipathways.org/wp#> " + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				"SELECT DISTINCT ?wpIdentifier ?title " + 
				"WHERE { " + 
				"?pathway dc:title ?title . " +
				"?pathway dc:identifier ?wpIdentifier . " + 
				"?pathway wp:organismName \"" + species +"\"^^xsd:string . " + 
				"}";
	}

	public String getInteractionInfo(String pathwayId) {
		return "PREFIX wp: <http://vocabularies.wikipathways.org/wp#>" +
				"PREFIX dcterms: <http://purl.org/dc/terms/>" +
				"SELECT DISTINCT ?interaction (GROUP_CONCAT(?participants) AS ?par) (GROUP_CONCAT(?sources) AS ?s) (GROUP_CONCAT(?targets) AS ?t) (GROUP_CONCAT(?intType) AS ?interactionTypes)" +
				"WHERE {" +
				"  ?pathway dc:identifier <http://identifiers.org/wikipathways/" + pathwayId + "> ." +
				"  ?interaction dcterms:isPartOf ?pathway ." +
				"  ?interaction a wp:Interaction ." +
				"  ?interaction wp:participants ?participants ." +
				"  ?interaction a ?intType . " + 
				"  OPTIONAL {" +
				"    ?interaction wp:source ?sources ." +
				"  }" +
				"  OPTIONAL {" +
				"    ?interaction wp:target ?targets ." +
				"  }" +
				"} GROUP BY ?interaction";
	}
	
	public String getDatanodesQuery(String species) {
		return  "PREFIX wp: <http://vocabularies.wikipathways.org/wp#> " + 
				"PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				"SELECT DISTINCT ?datanode (GROUP_CONCAT(?type) AS ?dnType) (GROUP_CONCAT(?ensembl) AS ?ensid) (GROUP_CONCAT(?wd) AS ?wdid) (GROUP_CONCAT(?hmdb) AS ?hmdbid) (GROUP_CONCAT(?chebi) AS ?chebiid) " + 
				"WHERE {" + 
				"   ?pathway a wp:Pathway ." + 
				"   ?pathway wp:organismName \"Homo sapiens\"^^xsd:string ." + 
				"   ?datanode dcterms:isPartOf ?pathway ." + 
				"   ?datanode a wp:DataNode ." +
				"   ?datanode a ?type ." +
				"   OPTIONAL {" + 
				"       ?datanode wp:bdbEnsembl ?ensembl ." + 
				"   }" + 
				"   OPTIONAL {" + 
				"       ?datanode wp:bdbWikidata ?wd." + 
				"   }" + 
				"   OPTIONAL {" + 
				"       ?datanode wp:bdbChEBI ?chebi." + 
				"   }" + 
				"   OPTIONAL {" + 
				"       ?datanode wp:bdbHmdb ?hmdb." + 
				"   }" + 
				"} GROUP BY ?datanode";
	}
}

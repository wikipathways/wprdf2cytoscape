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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.wp.rdf2cy.model.Datanode;
import org.wp.rdf2cy.model.Interaction;
import org.wp.rdf2cy.model.Pathway;

/**
 * This script generates the input files for Cytoscape to
 * import the complete WikiPathways pathway network
 * (gene products, metabolites, complexes + interactions)
 * 
 * @author mkutmon
 *
 */
public class NetworkGenerator {

	// current queries for Blazegraph!
	private static String sparqlUrl = "http://localhost:9999/blazegraph/sparql";
	
	public static void main(String[] args) throws Exception {
		
		String species = "Homo sapiens";
		
		NetworkGenerator netGen = new NetworkGenerator();
		netGen.readSideMetabolites(new File("side-metabolites.txt"));
		netGen.buildNetwork(species);
	}
	
	private void readSideMetabolites(File file) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line = reader.readLine()) != null) {
			sideMetabolites.add(line);
		}
		reader.close();
	}

	private Set<String> sideMetabolites;
	private Map<String, Datanode> datanodes;
	private Map<String, Pathway> pathways;
	private Map<String, Interaction> interactions;
	private Map<String, String> datanodeType;
	private Map<String, String> interactionIdentifiers;	
	private Map<String, Set<String>> interactionIdentifiers2;	
	private Map<String, Map<Integer, Set<Interaction>>> sortedInteractions;
	private Map<String, Set<String>> pathwayInteractions;
	
	public NetworkGenerator() {
		sideMetabolites = new HashSet<String>();
		datanodes = new HashMap<>();
		pathways = new HashMap<>();
		interactions = new HashMap<>();
		datanodeType = new HashMap<>();
		sortedInteractions = new HashMap<>();
		interactionIdentifiers = new HashMap<>();
		pathwayInteractions = new HashMap<String, Set<String>>();
		interactionIdentifiers2 = new HashMap<String, Set<String>>();
	}
	
	private void buildNetwork(String species) throws Exception {
		System.out.println("[INFO]\tRetrieve all pathways for " + species + ".");
		retrievePathways(species);
		System.out.println("[INFO]\t" + pathways.size() + " pathways for " + species + " found.\n");
		
		System.out.println("[INFO]\tRetrieve all datanodes for " + species + ".");
		retrieveDatanodes(species);
		System.out.println("[INFO]\t" + datanodes.size() + " datanodes for " + species + " found and mapped.\n");
		
		System.out.println("[INFO]\tRetrieve all interactions for " + species + ".");
		retrieveInteractions();
		System.out.println("[INFO]\t" + interactions.size() + " interactions for " + species + " found.\n");
	
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("InteractionNetwork.txt")));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("PathwayNetwork.txt")));
		BufferedWriter writer3 = new BufferedWriter(new FileWriter(new File("InteractionData.txt")));
		
		Set<Interaction> done = new HashSet<Interaction>();
		int count = 1;
		writer.write("source\tint_type\ttarget\ttype\tdntype\n");
		writer2.write("source\ttarget\n");
		for(String s : interactions.keySet()) {
			if(!done.contains(interactions.get(s))) {
				Interaction i = interactions.get(s);
				String id = "int_" + count;
				interactionIdentifiers2.put(id, new HashSet<>());
				Set<Interaction> set = findInteractions(i);
				for(Interaction interaction : set) {
					interactionIdentifiers2.get(id).add(interaction.getId());
					String pwId = interaction.getPathway().getId();
					interactionIdentifiers.put(interaction.getId(), id);
					if(!pathwayInteractions.containsKey(pwId)) {
						writer2.write(pwId + "\t" + id + "\n");
						Set<String> set1 = new HashSet<>();
						set1.add(id);
						pathwayInteractions.put(pwId, set1);
					} else if(!pathwayInteractions.get(pwId).contains(id)) {
						writer2.write(pwId + "\t" + id + "\n");
						pathwayInteractions.get(pwId).add(id);
					}
				}
				
				done.addAll(set);
				count++;
			}
		}
		
		writer3.write("interaction\ttype\tint_list\tnode_type\n");
		
		for(String s : interactionIdentifiers2.keySet()) {
			if(interactionIdentifiers2.get(s).size() > 1) {
				System.out.println(s + "\t" + interactionIdentifiers2.get(s).size());
			}
		}
		
		for(String s : interactionIdentifiers2.keySet()) {
			Interaction i = interactions.get(interactionIdentifiers2.get(s).iterator().next());
			writer3.write(s + "\t" + i.getType() + "\t" + interactionIdentifiers2.get(s) + "\tInteraction\n");
			for(String p : i.getParticipants()) {
				String out = p;
				if(interactions.containsKey(p)) {
					out = interactionIdentifiers.get(p);
				}
				if(p.contains("identifiers.org") || p.contains("www.wikidata.org")) {
					out = p.substring(p.lastIndexOf("/") + 1);
				}
				String dnType = datanodeType.get(p);
					boolean directed = false;
					if(i.getSources().contains(p)) {
						writer.write(s + "\t" + i.getType() + "\t" + out + "\tsource\t" + dnType + "\n");
						directed = true;
					}
					if(i.getTargets().contains(p)) {
						writer.write(s + "\t" + i.getType() + "\t" + out + "\ttarget\t" + dnType + "\n");
						directed = true;
					}
					if(!directed) {
						writer.write(s + "\t" + i.getType() + "\t" + out + "\tparticipant\t" + dnType + "\n");
					}
				
			}
		}
		
		
		writer.close();
		writer2.close();
		writer3.close();
		
	}

	private void retrievePathways(String species) throws Exception {
		SparqlQuery sq = new SparqlQuery();
		String queryString = sq.getPathwayListQuery("Homo sapiens");
		InputStream is = Utils.runQuery(queryString, sparqlUrl);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line  = reader.readLine();		
		while((line = reader.readLine()) != null) {
			String [] buffer = line.split(",");
			String id = buffer[0].replace("http://identifiers.org/wikipathways/", "");
			String title = buffer[1];
			title = title.replace("\"", "");
			Pathway p = new Pathway(id, title, species);
			pathways.put(id, p);
		}
		reader.close();		
	}

	private void retrieveDatanodes(String species) throws Exception {
		SparqlQuery sq = new SparqlQuery();
		String queryString = sq.getDatanodesQuery(species);
		
		InputStream is = Utils.runQuery(queryString, sparqlUrl);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = reader.readLine();			
		while((line = reader.readLine()) != null) {
			String [] buffer = line.split(",");
			String originalUri = buffer[0];
			
			String ensembl = "";
			String wikidata = "";
			String hmdb = "";
			String chebi = "";
			if(buffer.length > 2) ensembl = buffer[2];
			if(buffer.length > 3) wikidata = buffer[3];
			if(buffer.length > 4) hmdb = buffer[4];
			if(buffer.length > 5) chebi = buffer[5];
			
			String objTypes = "";
			
			Set<String> ids = new HashSet<String>();
			if(!ensembl.equals("")) {
				objTypes = "GeneProduct";
				for(String s : ensembl.split(" ")) {
					if(s.contains("ENSG") && !s.contains("ENSGAL")) {
						ids.add(s);
					}
				}
			} else if (!wikidata.equals("")) {
				objTypes = "Metabolite";
				for(String s : wikidata.split(" ")) {
					if(!sideMetabolites.contains(s)) {
						ids.add(s);
					}
				}
			} else if (!chebi.equals("")) {
				objTypes = "Metabolite";
				for(String s : chebi.split(" ")) {
					ids.add(s);
				}
			} else if (!hmdb.equals("")) {
				objTypes = "Metabolite";
				for(String s : hmdb.split(" ")) {
					ids.add(s);
				}
			} else {
				if(originalUri.contains("R-HSA") || originalUri.contains("R-ALL")) {
					objTypes = "Complex";
					ids.add(originalUri);
				}
			}
			
			if(!ids.isEmpty()) {
				Datanode dn = new Datanode(originalUri, objTypes);
				dn.getUnifiedIds().addAll(ids);
				datanodes.put(originalUri, dn);
				
				datanodeType.put(originalUri, dn.getDatanodeType());
				for(String s : ids) {
					datanodeType.put(s, dn.getDatanodeType());
				}
			}
		}
		reader.close();
	}
	
	private void retrieveInteractions() throws Exception {
		for(String pwId : pathways.keySet()) {
			Set<String> valid = new HashSet<String>();
			{
			SparqlQuery sq = new SparqlQuery();
			String queryString = sq.getInteractionInfo(pwId);
			InputStream is = Utils.runQuery(queryString, sparqlUrl);
			// interaction, participants, sources, targets, interaction types
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line  = reader.readLine();	
			
			
			while((line = reader.readLine()) != null) {
				String [] buffer = line.split(",");
				
				Set<String> participants = new HashSet<String>();
				for(String s : buffer[1].split(" ")) {
					if(datanodes.containsKey(s)) {
						participants.add(s);
					} else if (s.contains("/WP/Interaction/")) {
						participants.add(s);
					}
				}
				
				if(participants.size() >= 2) {
					valid.add(buffer[0]);
				}
			}
			}
			
			SparqlQuery sq = new SparqlQuery();
			String queryString = sq.getInteractionInfo(pwId);
			InputStream is = Utils.runQuery(queryString, sparqlUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line  = reader.readLine();	
			
			while((line = reader.readLine()) != null) {
				String [] buffer = line.split(",");
				
				Set<String> participants = new HashSet<String>();
				for(String s : buffer[1].split(" ")) {
					if(datanodes.containsKey(s)) {
						participants.add(s);
					} else if (s.contains("/WP/Interaction/")) {
						if(valid.contains(s)) {
							participants.add(s);
						}
					}
				}
				
				if(participants.size() > 1) {
					Interaction i = new Interaction(pathways.get(pwId), buffer[0]);
					for(String s : participants) {
						if(datanodes.containsKey(s)) {
							i.getParticipants().addAll(datanodes.get(s).getUnifiedIds());
						} else {
							i.getParticipants().add(s);
						}
					}
					
					if(!buffer[2].equals("")) {
						for(String s : buffer[2].split(" ")) {
							if(datanodes.containsKey(s)) {
								i.getSources().addAll(datanodes.get(s).getUnifiedIds());
							} else if (s.contains("/WP/Interaction/")) {
								if(valid.contains(s)) {
									i.getSources().add(s);
								}
							}
						}
					}
					if(!buffer[3].equals("")) {
						for(String s : buffer[3].split(" ")) {
							if(datanodes.containsKey(s)) {
								i.getTargets().addAll(datanodes.get(s).getUnifiedIds());
							} else if (s.contains("/WP/Interaction/")) {
								if(valid.contains(s)) {
									i.getTargets().add(s);
								}
							}
						}
					}
					
					String t = buffer[4];
					t = t.replace("http://vocabularies.wikipathways.org/wp#", "");
					Set<String> types = new HashSet<String>();
					types.addAll(Arrays.asList(t.split(" ")));
					String intType = "";
					if(types.size()==1) {
						intType = types.iterator().next();
					} else if(types.size() == 2) {
						types.remove("Interaction");
						intType = types.iterator().next();
					} else if(types.size() == 3) {
						types.remove("Interaction");
						types.remove("DirectedInteraction");
						types.remove("Binding");
						intType = types.iterator().next();
					}
					i.setType(intType);
					interactions.put(i.getId(), i);
				}
			}
			reader.close();
		}
		
		for(String s : interactions.keySet()) {
			Interaction i = interactions.get(s);
			if(!sortedInteractions.containsKey(i.getType())) {
				sortedInteractions.put(i.getType(), new HashMap<>());
			}
			if(!sortedInteractions.get(i.getType()).containsKey(i.getParticipants().size())) {
				sortedInteractions.get(i.getType()).put(i.getParticipants().size(), new HashSet<>());
			}
			sortedInteractions.get(i.getType()).get(i.getParticipants().size()).add(i);
		}
	}
	
	
	private Set<Interaction> findInteractions(Interaction i) {
		Set<Interaction> set = new HashSet<Interaction>();
		set.add(i);
		for(Interaction i2 : sortedInteractions.get(i.getType()).get(i.getParticipants().size())) {
			if(i.equals(i2)) {
				set.add(i2);
			}
		}
		return set;
	}

}

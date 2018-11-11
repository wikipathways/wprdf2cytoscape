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

package org.wp.rdf2cy.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Data model to store information about interactions
 * 
 * @author mkutmon
 *
 */
public class Interaction {
 
	private Pathway pathway;
	private String id;
	private String type;
	private Set<String> participants;
	private Set<String> sources;
	private Set<String> targets;
	
	public Interaction(Pathway pathway, String id) {
		this.pathway = pathway;
		this.id = id;
		participants = new HashSet<String>();
		sources = new HashSet<String>();
		targets = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public Set<String> getParticipants() {
		return participants;
	}
	
	public Set<String> getSources() {
		return sources;
	}
	
	public Set<String> getTargets() {
		return targets;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String toString() {
		return id + "\t" + type + "\t" + participants.size() + "\t" + sources.size() + "\t" + targets.size();
	}

	public Pathway getPathway() {
		return pathway;
	}

	public boolean equals(Interaction i) {
		if(i.getType().equals(getType()) && i.getParticipants().equals(getParticipants()) && i.getSources().equals(getSources()) && i.getTargets().equals(getTargets())) {
			return true;
		}
		return false;
	}
}

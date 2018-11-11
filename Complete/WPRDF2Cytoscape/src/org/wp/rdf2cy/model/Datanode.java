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
 * Data model to store information about data nodes
 * 
 * @author mkutmon
 *
 */
public class Datanode {
	private String originalId;
	private Set<String> unifiedIds;
	private String datanodeType;
	
	public Datanode(String originalId, String datanodeType) {
		this.originalId = originalId;
		this.datanodeType = datanodeType;
		unifiedIds = new HashSet<String>();
	}

	public String getOriginalId() {
		return originalId;
	}

	public Set<String> getUnifiedIds() {
		return unifiedIds;
	}

	public String getDatanodeType() {
		return datanodeType;
	}
}

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 * Util function(s) to run query on Blazegraph
 * 
 * @author mkutmon
 *
 */
public class Utils {

	public static InputStream runQuery(String query, String sparqlUrl) throws Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();

		// Set credentials on the client
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("query", query));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		HttpPost httppost = new HttpPost(sparqlUrl);
		httppost.setEntity(entity);
		httppost.setHeader(HttpHeaders.ACCEPT, "text/csv");
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity responseEntity = response.getEntity();
		return responseEntity.getContent();
	}
}

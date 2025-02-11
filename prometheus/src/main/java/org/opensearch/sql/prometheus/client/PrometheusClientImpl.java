/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.prometheus.client;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrometheusClientImpl implements PrometheusClient {

  private static final Logger logger = LogManager.getLogger(PrometheusClientImpl.class);

  private final OkHttpClient okHttpClient;

  private final URI uri;

  public PrometheusClientImpl(OkHttpClient okHttpClient, URI uri) {
    this.okHttpClient = okHttpClient;
    this.uri = uri;
  }


  @Override
  public JSONObject queryRange(String query, Long start, Long end, String step) throws IOException {
    HttpUrl httpUrl = new HttpUrl.Builder()
        .scheme(uri.getScheme())
        .host(uri.getHost())
        .port(uri.getPort())
        .addPathSegment("api")
        .addPathSegment("v1")
        .addPathSegment("query_range")
        .addQueryParameter("query", query)
        .addQueryParameter("start", Long.toString(start))
        .addQueryParameter("end", Long.toString(end))
        .addQueryParameter("step", step)
        .build();
    logger.debug("queryUrl: " + httpUrl);
    Request request = new Request.Builder()
        .url(httpUrl)
        .build();
    Response response = this.okHttpClient.newCall(request).execute();
    JSONObject jsonObject = readResponse(response);
    return jsonObject.getJSONObject("data");
  }

  @Override
  public List<String> getLabels(String metricName) throws IOException {
    String queryUrl = String.format("%sapi/v1/labels?match[]=%s", uri.toString(), metricName);
    logger.debug("queryUrl: " + queryUrl);
    Request request = new Request.Builder()
        .url(queryUrl)
        .build();
    Response response = this.okHttpClient.newCall(request).execute();
    JSONObject jsonObject = readResponse(response);
    return toListOfStrings(jsonObject.getJSONArray("data"));
  }

  private List<String> toListOfStrings(JSONArray array) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < array.length(); i++) {
      result.add(array.optString(i));
    }
    return result;
  }


  private JSONObject readResponse(Response response) throws IOException {
    if (response.isSuccessful()) {
      JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
      if ("success".equals(jsonObject.getString("status"))) {
        return jsonObject;
      } else {
        throw new RuntimeException(jsonObject.getString("error"));
      }
    } else {
      throw new RuntimeException(
          String.format("Request to Prometheus is Unsuccessful with : %s", response.message()));
    }
  }


}

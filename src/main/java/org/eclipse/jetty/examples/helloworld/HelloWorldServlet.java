/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.jetty.examples.helloworld;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.UrlEncoded;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
@Slf4j(topic = "server")
public class HelloWorldServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    handle(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    handle(request, response);
  }

  private void handle(HttpServletRequest request, HttpServletResponse response) {
    try {
      HelloRequest req = buildRequest(request);
      fillResponse(req, response);
    } catch (Exception e) {
      try {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", e.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
        response.getWriter().println(jsonObject);
      } catch (IOException ex) {
        logger.error("error: {}", ex.getMessage());
      }
    }
  }

  private HelloRequest buildRequest(HttpServletRequest request) throws IOException {
    if (HttpMethod.GET.is(request.getMethod())) {
      return HelloRequest.newBuilder().setName(request.getParameter("name")).build();
    }
    if (HttpMethod.POST.is(request.getMethod())) {
      Stream.of(MimeTypes.Type.FORM_ENCODED,
              MimeTypes.Type.APPLICATION_JSON, MimeTypes.Type.TEXT_JSON,
              MimeTypes.Type.APPLICATION_JSON_8859_1, MimeTypes.Type.APPLICATION_JSON_UTF_8,
              MimeTypes.Type.TEXT_JSON_8859_1, MimeTypes.Type.TEXT_JSON_UTF_8)
          .filter(type -> type.is(request.getContentType()))
          .findFirst()
          .orElseThrow(() -> new UnsupportedOperationException("Unsupported content type"));
      String requestData = request.getReader().lines().collect(Collectors.joining());
      if (MimeTypes.Type.FORM_ENCODED.is(request.getContentType())) {
        requestData = getJsonString(requestData);
      }
      return GsonUtil.toMessage(HelloRequest.class, requestData);
    }
    throw new UnsupportedOperationException("Unsupported method");
  }

  public static String getJsonString(String str) {
    if (StringUtil.isEmpty(str)) {
      return new Gson().toJson(Collections.emptyMap());
    }
    MultiMap<String> params = new MultiMap<>();
    UrlEncoded.decodeUtf8To(str, params);
    return new Gson().toJson(params);
  }

  private void fillResponse(HelloRequest req, HttpServletResponse response) throws IOException {

    HelloReply reply = HelloWorldService.sayHello(req);
    GsonUtil.printJson(response, reply);
    response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
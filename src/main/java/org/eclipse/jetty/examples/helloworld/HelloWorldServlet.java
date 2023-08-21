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
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

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
    } catch (Exception ignored) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private HelloRequest buildRequest(HttpServletRequest request) throws IOException {
    if (HttpMethod.GET.is(request.getMethod())) {
      return HelloRequest.newBuilder().setName(request.getParameter("name")).build();
    }
    if (HttpMethod.POST.is(request.getMethod())) {
      if (MimeTypes.Type.FORM_ENCODED.is(request.getContentType())) {
        return HelloRequest.newBuilder().setName(request.getParameter("name")).build();
      }
      if (MimeTypes.Type.APPLICATION_JSON.is(request.getContentType())) {
        String requestData = request.getReader().lines().collect(Collectors.joining());
        return new Gson().fromJson(requestData, HelloRequest.class);
      }
    }
     throw new UnsupportedOperationException();
  }

  private void fillResponse(HelloRequest req, HttpServletResponse response) throws IOException {

    HelloReply reply = HelloWorldService.sayHello(req);
    GsonUtil.printJson(response, reply);
    response.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
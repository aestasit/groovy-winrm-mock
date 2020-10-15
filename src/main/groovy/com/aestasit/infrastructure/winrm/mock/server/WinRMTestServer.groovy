/*
 * Copyright (C) 2011-2020 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aestasit.infrastructure.winrm.mock.server

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.io.IOUtils
import org.eclipse.jetty.http.HttpVersion
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.util.ssl.SslContextFactory

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_OK
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import static org.apache.commons.io.IOUtils.write

/**
 * HTTP server implementation for use during WinRM tests.
 *
 * @author Sergey Korenko
 */
@CompileStatic
class WinRMTestServer {

  static final int HTTP_PORT = 5985
  static final int HTTPS_PORT = 5986

  Server server
  Map requestResponseMock = [:]

  void start() throws Exception {
    URL url = WinRMTestServer.class.getResource('/keystore')
    if (!url) {
      throw new FileNotFoundException('/keystore')
    }

    server = new Server();

    HttpConfiguration http_config = new HttpConfiguration()
    http_config.secureScheme = "https"
    http_config.securePort = HTTPS_PORT

    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config))
    http.port = HTTP_PORT
    SslContextFactory sslContextFactory = new SslContextFactory()
    sslContextFactory.with {
      keyStorePath = url.toString()
      keyStorePassword = "secret"
    }

    HttpConfiguration https_config = new HttpConfiguration(http_config)
    https_config.addCustomizer(new SecureRequestCustomizer())

    ServerConnector https = new ServerConnector(server,
      new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
      new HttpConnectionFactory(https_config))
    https.port = HTTPS_PORT

    server.setConnectors([http, https] as Connector[] )

    server.handler = getMockHandler()
    server.start()
  }

  Handler getMockHandler() {
    new AbstractHandler() {
      @Override
      void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        def requestBody = IOUtils.toString(baseRequest.inputStream)
        def responseBody = getResponseMockByRequest(requestBody)
        response.status = responseBody ? SC_OK : SC_NOT_FOUND
        response.contentType = "text/xml;charset=utf-8"
        write responseBody, response.outputStream

        baseRequest.handled = true
      }
    }
  }

  void stop() throws Exception {
    server.stop()
  }

  @CompileDynamic
  String getResponseMockByRequest(def requestBody){
    requestResponseMock.find { requestBody.contains(it.key)}?.value
  }
}
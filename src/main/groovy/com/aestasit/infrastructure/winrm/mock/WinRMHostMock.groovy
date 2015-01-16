/*
 * Copyright (C) 2011-2015 Aestas/IT
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

package com.aestasit.infrastructure.winrm.mock

import com.aestasit.infrastructure.winrm.mock.server.WinRMTestServer
import groovy.util.slurpersupport.GPathResult

/**
 * This class emulate WinRM host behaviour with HTTP and HTTPS for using in JUnit test
 *
 * @author Sergey Korenko
 */
class WinRMHostMock {
//  static Map commands = [:]

  /** Http server that emulates WinRM host to check command execution*/
  static WinRMTestServer winRMServer

  /**
   * Starts SSH server.
   */
  static void startWinRMServer(int port) {
    winRMServer = new WinRMTestServer()
//    winRMServer.start(WinRMTestServer.HTTP_PORT == port)
  }

  /**
   * Stops running SSH server.
   */
  static void stopWinRMServer() {
//    winRMServer?.stop()
  }

  /**
   * Adds remote command expectations and behavior.
   *
   * @param pattern command pattern to match
   * @param cl closure to execute for command.
   */
  static void command(String pattern, Closure cl) {
    decomposeExpectIntoRequestResponse(cl)
  }


  // each command execution is done in 4 steps:
  // 1. open WinRM shell - returns shellID
  // 2. execute command inside shell with shellID - returns commandID
  // 3. get command output inside shell with shellID by commandID
  // 4. close shell by shellID
  static void decomposeExpectIntoRequestResponse(Closure cl){
    // 1. mock open WinRM shell opening
    def openShellRequestFragment = "<wsa:Action s:mustUnderstand='true'>http://schemas.xmlsoap.org/ws/2004/09/transfer/Create</wsa:Action>"
    def shellID = UUID.randomUUID().toString().toUpperCase()

    def xmlText = WinRMHostMock.getClass().getResourceAsStream('/OpenShellResponse.xml').text
    def openShellResponse = new XmlParser().parseText(xmlText)

    openShellResponse?.'*:Body'[0].'*:ResourceCreated'[0].'*:ReferenceParameters'[0].'*:SelectorSet'[0].'*:Selector'[0].value = shellID
    def writer = new StringWriter()
    new XmlNodePrinter(new PrintWriter(writer)).print(openShellResponse)

    winRMServer.requestResponseMock[openShellRequestFragment] = writer.toString()
  }
}

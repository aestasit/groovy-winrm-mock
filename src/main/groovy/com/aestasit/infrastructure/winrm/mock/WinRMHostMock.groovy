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
package com.aestasit.infrastructure.winrm.mock

import com.aestasit.infrastructure.winrm.mock.server.WinRMTestServer
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * This class emulate WinRM host behaviour with HTTP and HTTPS for using in JUnit test
 *
 * @author Sergey Korenko
 */
@CompileStatic
class WinRMHostMock {
  /** Http server that emulates WinRM host to check command execution*/
  static WinRMTestServer winRMServer

  /**
   * Starts SSH server.
   */
  static void startWinRMServer() {
    winRMServer = new WinRMTestServer()
    winRMServer.start()
  }

  /**
   * Stops running SSH server.
   */
  static void stopWinRMServer() {
    winRMServer?.stop()
  }

  /**
   * Adds remote command expectations and behavior.
   *
   * @param pattern command pattern to match
   * @param cl closure to execute for command.
   */
  static void command(String command, String[] args, int result, String output, String errorOutput) {
    // each command execution is done in 4 steps:
    // 1. open WinRM shell - returns shellID
    // 2. execute command inside shell with shellID - returns commandID
    // 3. get command output inside shell with shellID by commandID
    // 4. close shell by shellID
    mockOpenShell()
    def commandID = mockExecuteCommand(command, args)
    mockCommandOutput(commandID, result, output, errorOutput)
    mockDeleteShell()
  }

  @CompileDynamic
  private static void mockOpenShell() {
    def shellID = UUID.randomUUID().toString().toUpperCase()

    def xmlText = WinRMHostMock.getClass().getResourceAsStream('/OpenShellResponse.xml').text
    def openShellResponse = new XmlParser().parseText(xmlText)

    openShellResponse.'*:Body'.'*:ResourceCreated'.'*:ReferenceParameters'.'*:SelectorSet'.'*:Selector'[0].value = shellID

    def openShellRequestKey = "<wsa:Action s:mustUnderstand='true'>http://schemas.xmlsoap.org/ws/2004/09/transfer/Create</wsa:Action>"
    winRMServer.requestResponseMock[openShellRequestKey] = getResponseString(openShellResponse)
  }

  @CompileDynamic
  private static String mockExecuteCommand(String command, String[] args) {
    def xmlText = WinRMHostMock.getClass().getResourceAsStream('/ExecuteCommandResponse.xml').text
    def executeCommandResponse = new XmlParser().parseText(xmlText)

    def commandID = UUID.randomUUID().toString().toUpperCase()
    executeCommandResponse.'*:Body'.'*:CommandResponse'.'*:CommandId'[0].value = commandID

    def executeCommandRequestKey = "<rsp:Command>${command}</rsp:Command>"
    winRMServer.requestResponseMock[executeCommandRequestKey] = getResponseString(executeCommandResponse)

    commandID
  }

  @CompileDynamic
  private static void mockCommandOutput(String commandID, int result, String output, String errorOutput) {
    def xmlText = WinRMHostMock.getClass().getResourceAsStream('/ExecutionResultsResponse.xml').text
    def executionResultsResponse = new XmlParser().parseText(xmlText)

    executionResultsResponse.'*:Body'.'*:ReceiveResponse'.'*:Stream'.findAll{!it.@CommandId.isEmpty()}?.each {it.@CommandId=commandID}
    executionResultsResponse.'*:Body'.'*:ReceiveResponse'.'*:CommandState'[0].@CommandId=commandID

    executionResultsResponse.'*:Body'.'*:ReceiveResponse'.'*:CommandState'.'*:ExitCode'[0].value = result

    executionResultsResponse.'*:Body'.'*:ReceiveResponse'.'*:Stream'.findAll{it.@Name == 'stdout' && !it.@End}[0].value = output?.bytes?.encodeBase64()?.toString()
    executionResultsResponse.'*:Body'.'*:ReceiveResponse'.'*:Stream'.findAll{it.@Name == 'stderr' && !it.@End}[0].value = errorOutput?.bytes?.encodeBase64()?.toString()

    def commandOutputRequestKey = "<rsp:DesiredStream CommandId='${commandID}'>stdout stderr</rsp:DesiredStream>"
    winRMServer.requestResponseMock[commandOutputRequestKey] = getResponseString(executionResultsResponse)
  }

  private static void mockDeleteShell() {
    def deleteRequestKey = "<wsa:Action s:mustUnderstand='true'>http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete</wsa:Action>"
    def deleteShellResponse = WinRMHostMock.class.getResourceAsStream('/ExecutionResultsResponse.xml').text
    winRMServer.requestResponseMock[deleteRequestKey] = deleteShellResponse
  }

  /**
   * Returns string which represents xml
   *
   * @param node identified node of XML which has to be represented as String
   * @return String
   */
  private static String getResponseString(Node node){
    StringWriter writer = new StringWriter()
    XmlNodePrinter nodePrinter = new XmlNodePrinter(new PrintWriter(writer),''){void printLineEnd() {} }
    nodePrinter.print(node)

    writer.toString()
  }
}

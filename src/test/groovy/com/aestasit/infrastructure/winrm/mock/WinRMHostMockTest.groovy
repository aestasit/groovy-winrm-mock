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

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * HTTP server implementation for use during WinRM tests.
 *
 * @author Sergey Korenko
 */
@CompileStatic
class WinRMHostMockTest {

  @Test
  void commandMocking() {
    WinRMHostMock.startWinRMServer()
    WinRMHostMock.command('whoami', [] as String[], 0, 'server name', '')
    WinRMHostMock.command('ipconfig', [] as String[], 0, 'ip data', '')
    WinRMHostMock.command('dir', [] as String[], 0, 'some dir info', '')
    WinRMHostMock.command('commmand', [] as String[], 0, 'some command output', null)
    WinRMHostMock.command('cmd', [] as String[], 1, null, 'Some error text')
    WinRMHostMock.stopWinRMServer()
  }
}

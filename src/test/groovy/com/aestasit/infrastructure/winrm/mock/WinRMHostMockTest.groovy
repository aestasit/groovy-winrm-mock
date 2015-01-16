package com.aestasit.infrastructure.winrm.mock

import org.junit.Test

/**
 * HTTP server implementation for use during WinRM tests.
 *
 * @author Sergey Korenko
 */
class WinRMHostMockTest {

  @Test
  void commandMocking() {
    WinRMHostMock.startWinRMServer(59859)
    WinRMHostMock.command('whoami'){ exitStatus, output, exception ->
      output = 'win-l9po57hvelf\\user'
    }
    println WinRMHostMock.winRMServer.requestResponseMock
    WinRMHostMock.stopWinRMServer()
  }
}

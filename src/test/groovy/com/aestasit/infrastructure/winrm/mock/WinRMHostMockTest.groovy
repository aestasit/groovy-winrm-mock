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
    WinRMHostMock.startWinRMServer(5985)
    WinRMHostMock.command('whoami', 0, 'win-l9po57hvelf\\user', '')
    WinRMHostMock.stopWinRMServer()
  }
}

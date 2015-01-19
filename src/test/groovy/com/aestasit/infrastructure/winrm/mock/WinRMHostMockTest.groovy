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
    WinRMHostMock.command('whoami', [] as String[], 0, 'server name', '')
    WinRMHostMock.command('ipconfig', [] as String[], 0, 'ip data', '')
    WinRMHostMock.command('dir', [] as String[], 0, 'some dir info', '')
    WinRMHostMock.stopWinRMServer()
  }
}

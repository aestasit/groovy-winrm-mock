package com.aestasit.infrastructure.winrm.mock.server

import org.junit.Test

import static org.junit.Assert.*

/**
 * Test for http server
 *
 * @author Sergey Korenko
 */
class WinRMTestServerTest {

  @Test
  void putCommand() {
    WinRMTestServer server = new WinRMTestServer()
    server.requestResponseMock['stack'] = 'lifo'
    server.requestResponseMock['queue'] = 'fifo'
    assert server.getResponseMockByRequest('stack') == 'lifo'
    assertNull server.getResponseMockByRequest('list')
  }

}

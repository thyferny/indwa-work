/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ImportDataWecSocket.java
 */
package com.alpine.miner.impls.websocket.importdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import com.alpine.miner.impls.controller.ImportDataController;

/**
 * @author Gary
 * Nov 14, 2012
 */
public class ImportDataWebSocket extends WebSocketServlet {
	private static final long serialVersionUID = -652480518218400702L;

	/* (non-Javadoc)
	 * @see org.apache.catalina.websocket.WebSocketServlet#createWebSocketInbound(java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected StreamInbound createWebSocketInbound(String arg0,
			HttpServletRequest request) {
		return new ImportDataInbound(request.getSession());
	}
	private static class ImportDataInbound extends StreamInbound{
		
		private HttpSession session;
		
		ImportDataInbound(HttpSession session){
			this.session = session;
		}

		/* (non-Javadoc)
		 * @see org.apache.catalina.websocket.StreamInbound#onBinaryData(java.io.InputStream)
		 */
		@Override
		protected void onBinaryData(InputStream arg0) throws IOException {
			
		}

		/* (non-Javadoc)
		 * @see org.apache.catalina.websocket.StreamInbound#onTextData(java.io.Reader)
		 */
		@Override
		protected void onTextData(Reader arg0) throws IOException {
			Integer currentIdx = (Integer) session.getAttribute(ImportDataController.PROGRESS_NODE_NAME);
			Integer lastIdx = currentIdx;
			while(currentIdx != null){
				if(currentIdx % 10000 == 0 && lastIdx != currentIdx){
					this.getWsOutbound().writeTextMessage(CharBuffer.wrap(currentIdx.toString()));
				}
				lastIdx = currentIdx;
				currentIdx = (Integer) session.getAttribute(ImportDataController.PROGRESS_NODE_NAME);
			}
			this.getWsOutbound().writeTextMessage(CharBuffer.wrap("-1"));
		}

		@Override
		protected void onClose(int status) {
			System.out.println("closing....");
			super.onClose(status);
		}

		@Override
		protected void onOpen(WsOutbound outbound) {
			System.out.println("opening....");
			super.onOpen(outbound);
		}
	}	
}

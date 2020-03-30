/**
 * @page License
 *
 *   Copyright (c) 2010 Guibin.Li. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.*;

import com.cartionsoft.messages.*;
import com.cartionsoft.utils.DataCRC32;


public class Send implements Runnable {

	private SSLSocket theConn;
	private DataCRC32 csvFile;
	private DataCRC32 sendFile;
	
	private String csvPath;
	private String sendPath;
	
	public void run() {
		// TODO Auto-generated method stub	
		try {
			if(!isDuplicate()) {
				byte[] data = genMessages(sendPath);
				sendData(theConn, data);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	/**
	 * Transfer value
	 * @param conn
	 * @param csvPath
	 * @param sendPath
	 */
	public Send(SSLSocket conn, String csvPath, String sendPath) {
		// TODO Auto-generated constructor stub
		this.theConn = conn;
		this.csvPath = csvPath;
		this.sendPath = sendPath;
	}
	
	/**
	 * <p> Send byte array data </p>
	 * @param data
	 */
	private void sendData(SSLSocket theConnection, byte[] data) throws Exception {
		BufferedOutputStream out = new BufferedOutputStream(
				theConnection.getOutputStream());
		out.write(data);
		out.flush();
		out.close();
		theConnection.close();
	}
	
	/**
	 * <p> Send file is duplicate </p>
	 * @return false
	 * @throws IOException
	 */
	private boolean isDuplicate() throws IOException {
		boolean result = false;
		
		csvFile = new DataCRC32(csvPath);
		String strCrc32 = csvFile.getCRC32Value();
		sendFile = new DataCRC32(sendPath);
		String strSendCrc32 = sendFile.getCRC32Value();
		
		if(strCrc32.equals(strSendCrc32) || strCrc32 == "" || strSendCrc32 =="") {
			result = true;
		}
		return result;
	}
	
	/**
	 * <p> Generated packet. </p>
	 * @return result
	 * @throws Exception
	 */
	private byte[] genMessages(String sendPath) throws Exception {
		List<Messages> containMessages = new ArrayList<Messages>();
		
		Report branchMessages = new Report();
		
		Messages headCmd = new MessageCMD(3);
		Messages station = new StationID(99);
		Messages customDate = new DateTimes();
		Messages counts = new CountSend();
		Messages md5value = new FileMD5(sendPath);
		Messages fileMessages = new FileReport(sendPath);
		Messages ipMessages = new LocalIP();
		
		branchMessages.addToMessages(headCmd);
		branchMessages.addToMessages(station);
		branchMessages.addToMessages(customDate);
		branchMessages.addToMessages(counts);
		branchMessages.addToMessages(md5value);
		branchMessages.addToMessages(fileMessages);
		branchMessages.addToMessages(ipMessages);
		
		containMessages.add(branchMessages);
		
		byte[] result = branchMessages.putMessages();
		
		return result;
	}
}

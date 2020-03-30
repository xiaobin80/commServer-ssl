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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.cartionsoft.ssl.ConnectionSSL;
import com.cartionsoft.utils.*;
import com.cartionsoft.xbf.*;
import com.cartionsoft.utils.message.*;

public class Server {

	private final int DEFAULT_PORT = 7006;
	
	private ReadConfig xmlRead;
	private ReadXbf xbf;

	private LogOper logger;
	private int port;
	private String keyPath;
	private String libPath;
	private String xbfPath;
	private String csvPath;
	private String sendPath;
	private String receivePath;
	
	private String savePath;
	private String saveName;
	private List<String> filesList;
	
	private String xbfContent;
	private String keyPWD;
	
	private MessagesParser messageParser;
	
	/**
	 * Provide XML path
	 * @param xmlPath
	 */
	public Server(String xmlPath) {
		// TODO Auto-generated constructor stub
		xmlRead = new ReadConfig(xmlPath);
		
		logger = new LogOper();
		messageParser = new ByteParser();
		filesList = new ArrayList<String>();
	}
	
	/**
	 * XML file for configuration information.
	 * @throws Exception
	 */
	public void getProperty() throws Exception {
		String strPort = "";
		if(strPort.equals("")) {
			port = DEFAULT_PORT;
		} else {	
			port = Integer.parseInt(strPort);
			if(port < 7000) {
				port = DEFAULT_PORT;
			}
		}
		
		keyPath = xmlRead.getElement(0, "key1");
		libPath = xmlRead.getElement(1, "lib-file1");
		xbfPath = xmlRead.getElement(2, "xbf-file1");
		csvPath = xmlRead.getElement(3, "csv-file1");
		sendPath = xmlRead.getElement(3, "send-file1");
		receivePath = xmlRead.getElement(3, "receive-path");
		
		savePath = xmlRead.getElement(4, "save-path");
		filesList.add(savePath);
		for (int i = 1; i < 6; i++) {
			saveName = xmlRead.getElement(4, "station" + i);
			filesList.add(saveName);
		}
	}
	
	
	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's DB user
	 */
	
	public String getDBUserMsSql(String xbfContent) {
		int iBeginIndex = xbfContent.indexOf("ID=");
		int iEndIndex = xbfContent.indexOf(";I");
		String strUser = xbfContent.substring(iBeginIndex + 3, iEndIndex);
		return strUser;
	}
	
	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's password
	 */
	public String getPWDMsSql(String xbfContent) {
		int iBeginIndex = xbfContent.indexOf("d=");
		int iEndIndex = xbfContent.indexOf(";Per");
		String strPwd = xbfContent.substring(iBeginIndex + 2, iEndIndex);
		return strPwd;
	}
	
	/**
	 * <p> Read XBF file "Password" section. </p>
	 * @return keyPWD
	 */
	private String getKeyPWD() {
		xbf = new ReadXbf(libPath);
		xbfContent = xbf.readRecordMsSql(-1, xbfPath);
		String strPwd = getPWDMsSql(xbfContent);
		keyPWD = strPwd;
		return keyPWD;
	}
	
	/**
	 * <p> Initialization process - the method call sequence. </p>
	 * @throws Exception
	 */
	public void init() throws Exception {
		getProperty();
		getKeyPWD();
	}
	
	
	public void runGram() throws Exception {
		
		ExecutorService execSend = Executors.newCachedThreadPool();
		ExecutorService execRecv = Executors.newCachedThreadPool();
		
		ConnectionSSL conn = ConnectionSSL.getInstance();
		SSLServerSocket server = conn.createServer(keyPWD);
		while(true) {
			try {
				SSLSocket theConnection = (SSLSocket) server.accept();
				Send send = new Send(theConnection, csvPath, sendPath);
				execSend.execute(send);
				// receive
				Receive receive = new Receive(theConnection, receivePath, filesList);
				execRecv.execute(receive);
				
			} catch (Exception e) {
				// TODO: handle exception
			}		
			
		}
	}
	
	/**
	 * @param args E:/receive/configQ.xml
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length < 1) {
			System.out.println("Usage: Found not configure file!");
			return;
		}
		
		Server server = new Server(args[0]);
		try {
			server.init();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}	
		
        try {
        	server.runGram();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}

}

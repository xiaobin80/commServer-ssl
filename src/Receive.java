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

import javax.net.ssl.*;

import com.cartionsoft.utils.message.*;
import com.cartionsoft.utils.*;

import java.text.SimpleDateFormat;
import java.util.*;


public class Receive implements Runnable {

	private String recvPath;
	private String filePath;
	private String fileName;
	private MessagesParser messagesParser;
	private List<String> fileList;
	private String originalName;
	private String oldDateTime;
	
	/**
	 * Incoming per minute count
	 */
	private int count;
	
	private SSLSocket theConn;
	
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			byte[] data = receiveData(theConn);
			
			byte[] textData = messagesParser.getMessages(data);
			byte[] md5Data = messagesParser.getMD5Message(data);
			byte[] stationID = messagesParser.getStationID(data);
			
			saveCSVFile(textData, md5Data, stationID);
		} catch (Exception e) {
			// TODO: handle exception
		}				
	}

	/**
	 * <p> Character arrays converter String </p>
	 * @param chArray
	 * @return result
	 */
	private String convert2String(char[] chArray) {
		String result = "";
		for (int i = 0; i < chArray.length; i++) {
			result += chArray[i];
		}
		return result;
	}
	
	/**
	 * <p> Calculation packet "text" part of the MD5 value </p>
	 * @param bytArray
	 * @return result
	 */
	private String getMD5String(byte[] bytArray) {
		MD5 md5 = new MD5();
		md5.init();
		char[] bytInput = convert2charArray(bytArray);
		md5.update(bytInput, bytArray.length);
		md5.md5final();
		
		
		StringBuffer strBuff = new StringBuffer();
		strBuff = md5.toHexString();
		String result = "";
		for (int i = 0; i < strBuff.length(); i++) {
			char ch = strBuff.charAt(i);
			result += String.valueOf(ch);
		}
		
		return result;
	}
	
	/**
	 * <p> Byte arrays converter character arrays </p>
	 * @param bytArray
	 * @return result
	 */
	private char[] convert2charArray(byte[] bytArray) {
		char[] result = new char[bytArray.length];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = (char) bytArray[i];
		}
		
		return result;
	}
	
	/**
	 * <p> Transfer SSLSocket, receive path, save path and name </p>
	 * @param conn
	 * @param recvPath
	 * @param lstFile
	 */
	public Receive(SSLSocket conn, String recvPath, List<String> lstFile) {
		// TODO Auto-generated constructor stub
		this.theConn = conn;
		this.recvPath = recvPath;
		messagesParser = new ByteParser();
		fileList = new ArrayList<String>();
		fileList = lstFile;
		count = 1;
	}
	
	/**
	 * <p> 0 - File path </p>
	 * <p> 1 - File name </p>
	 * <p> 2 - File name </p>
	 * <p> 3 - File name </p>
	 * <p> 4 - File name </p>
	 * <p> 5 - File name </p>
	 * @param lstFile
	 * @return result
	 */
	private String[] getFileList(List<String> lstFile) {
		int i = 0;
		String[] result = new String[lstFile.size()];
		for (String string : lstFile) {
			result[i] = string;
			i++;
		}
		
		return result;
	}
	
	/**
	 * <p> To enter a SSLSocket, return byte array. </p>
	 * @param conn
	 * @return data
	 * @throws Exception
	 */
	private byte[] receiveData(SSLSocket conn) throws Exception {
		InputStream in = new BufferedInputStream(
				conn.getInputStream());
	
		originalName = getOriginalName(oldDateTime);
		
		List<Byte> listBuffer = new ArrayList<Byte>();
		
		int b;
		while ((b = in.read()) != -1) {
			listBuffer.add((byte) b);
		}
		
		conn.close();
		
		byte[] data = new byte[listBuffer.size()];
		if(listBuffer.size() > 0) {
			OutputStream outOriginal = new DataOutputStream(
					new FileOutputStream(recvPath + File.separator + 
							originalName + ".dat"));
			int i = 0;
			
			for (Byte byte1 : listBuffer) {		
				data[i] = byte1;
				i++;
			}
			
			outOriginal.write(data);
			outOriginal.flush();
			outOriginal.close();
		}
		
		return data;
	}
	
	/**
	 * <p> Save the file for the site. </p>
	 * @param textData
	 * @param md5Data
	 * @param stationID
	 */
	private void saveCSVFile(byte[] textData, byte[] md5Data, byte[] stationID) {
		String strTextDataMD5 = getMD5String(textData);
		char[] chMD5Data = convert2charArray(md5Data);
		String strMD5Data = convert2String(chMD5Data);
		
		byte bytStationID = stationID[1];
		
		if(strTextDataMD5.equals(strMD5Data)) {
			
			filePath = getFileList(fileList)[0];
			
			switch (bytStationID) {
			case 1:
				fileName = getFileList(fileList)[1];				
				break;
				
			case 2:
				fileName = getFileList(fileList)[2];	
				break;
				
			case 3:
				fileName = getFileList(fileList)[3];	
				break;
			
			case 4:
				fileName = getFileList(fileList)[4];	
				break;
			
			case 5:
				fileName = getFileList(fileList)[5];	
				break;
			default:
				fileName = "datasb.txt";
				break;
			}
			try {
				OutputStream out = new DataOutputStream(
						new FileOutputStream(filePath + File.separator + fileName));
				out.write(textData);
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO: handle exception
			}

		}
	}
	
	/**
	 * <p> Save for the name of message </p>
	 * @param oldTime
	 * @return result
	 */
	private String getOriginalName(String oldTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String strDateTime = sdf.format(new Date());
				
		if(strDateTime.equals(oldTime)) {
			count++;
		} else {
			count = 1;
		}
		
		oldDateTime = strDateTime;		
		String result = "D" + strDateTime + count;
		return result;
	}
	
}

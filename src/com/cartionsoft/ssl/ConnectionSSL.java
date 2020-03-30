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

package com.cartionsoft.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.*;

public class ConnectionSSL {
	
	private final int DEFAULT_PORT = 7000;
	public final String algorithm = "SSL";
	private int port;
	private String propertyPath;
	private String keyPath;
	
	private static ConnectionSSL conn = new ConnectionSSL();
	
	private ConnectionSSL() {
		// TODO Auto-generated constructor stub
		propertyPath = getPropertyPath();
		
		port = getPort(propertyPath);
		if(port == 0) {
			port = DEFAULT_PORT;
		}
		keyPath = getKeyPath(propertyPath);
	}
	
	/**
	 * Singleton Implementation 
	 * @return conn
	 */
	public static ConnectionSSL getInstance() {
		return conn;
	}

	/**
	 * Path to the file access property
	 * @return path
	 */
	private String getPropertyPath() {
		String path = System.getProperty("user.dir") 
			+ File.separator + "port.properties";
		return path;
	}

	/**
	 * Key store file path from the property file for the value
	 * @param filePath
	 * @return path
	 */
	private String getKeyPath(String filePath) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(filePath));
		} catch (Exception e) {
			// TODO: handle exception
		}
		String path = p.getProperty("key");
		
		return path;
	}
	
	/**
	 * Port from the property file for the value
	 * @param filePath
	 * @return port
	 */
	private int getPort(String filePath) {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(filePath));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		//p.clear();
		String strPort = p.getProperty("port");
		int port = Integer.parseInt(strPort);
		
		return port;
	}
	
	/**
	 * Establish SSLServerSocket
	 * @param keyPWD
	 * @return server
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 */
	public SSLServerSocket createServer(String keyPWD) 
	throws NoSuchAlgorithmException, IOException, KeyManagementException,
	UnrecoverableKeyException, KeyStoreException, CertificateException {
		SSLContext context = SSLContext.getInstance(algorithm);
		
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
		
		KeyStore ks = KeyStore.getInstance("JKS");
		
		char[] pwd = keyPWD.toCharArray();
		ks.load(new FileInputStream(keyPath), pwd);
		
		kmf.init(ks, pwd);
		
		context.init(kmf.getKeyManagers(), null, null);
		SSLServerSocketFactory factory = context.getServerSocketFactory();
		
		SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(port);	
		String[] supported = server.getSupportedCipherSuites();
		server.setEnabledCipherSuites(supported);		
		return server;
	}
}

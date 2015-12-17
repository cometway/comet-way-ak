
package com.cometway.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



/**
* Implements a secure version of HTTPSLoader capable of accessing
* Web Services that use SSL.
*/

public class SecureHTTPLoader extends HTTPLoader
{
    /** This is the path and filename of the non-passphrased, PKCS8 encoded private key 
	in PEM format. If no path is given the current directory is used. */
	public String key_file = "server.key";
    /** This is the path and filename of the non-passphrased, X509 certificate associated
	with the private key. The format is PEM. If no path is given, the current directory is used. */
	public String cert1_file = "newcert.pem";


	protected HTTPResponse  response;

	public SecureHTTPLoader()
	{
		debugStr = "%SecureHTTPLoader% ";
		errorStr = "!SecureHTTPLoader! ";
		printStr = "[SecureHTTPLoader] ";
	}


	protected byte[] readBinaryFile(File file)
	{
		byte[] rval = null;
		if(file.exists()) {
			rval = new byte[(int)file.length()];
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(file));
				in.read(rval);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			try {
				in.close();
			}
			catch(Exception e) {;}
		}
		return(rval);
	}

	/**
	 * Opens a socket connection.
	 */

	protected void openSocket() throws Exception
	{
		FileInputStream fis = null;
		OutputStream out = null;
		if (requestProtocol.equals("https"))
		{
			File cert = new File(cert1_file);
			File key = new File(key_file);

			fis = new FileInputStream(cert);

			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate)certFactory.generateCertificate(fis);
			X509Certificate[] certs = new X509Certificate[1];
			certs[0] = certificate;
		    
			PKCS8EncodedKeySpec encodedKey = new PKCS8EncodedKeySpec(readBinaryFile(key));
			//	    X509EncodedKeySpec encodedKey = new X509EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privatekey = keyFactory.generatePrivate(encodedKey);
		    
			KeyStore store = KeyStore.getInstance("JKS","SUN");
			store.load(null,null);
			store.setKeyEntry("client",privatekey,new char[0],certs);

			SSLContext sc = SSLContext.getInstance("SSL");
			KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
			keyManager.init(store,new char[0]);
			TrustManager[] trusts = new TrustManager[1];
			trusts[0] = new TrustEverything();
			sc.init(keyManager.getKeyManagers(),trusts,null);

			SocketFactory sf = sc.getSocketFactory();

			if(proxyServer!=null) {
				requestSocket = new Socket(proxyServer,proxyServerPort);
				try {
					out = requestSocket.getOutputStream();
					out.write(("open "+requestHost+":"+requestPort+"\n").getBytes());
					out.flush();
				}
				catch(Exception e) {
					e.printStackTrace();
					try {
						requestSocket.close();
					}
					catch(Exception e2) {;}
					requestSocket = null;
					return;
				}
				requestSocket = ((SSLSocketFactory)sf).createSocket(requestSocket, requestHost, requestPort, true);
			}
			else {
				requestSocket = sf.createSocket(requestHost,requestPort);
				//		    requestSocket = new SSLSocket(requestHost,requestPort,sc);
			}
			((SSLSocket)requestSocket).setNeedClientAuth(false);
			((SSLSocket)requestSocket).setWantClientAuth(false);
			((SSLSocket)requestSocket).setUseClientMode(true);
			((SSLSocket)requestSocket).startHandshake();
		}
		else
		{
			super.openSocket();
		}

		try {fis.close();} catch(Exception e) {;}
		try {out.close();} catch(Exception e) {;}
	}


	// static methods


	/**
	 * This class can be called from the command line to retrieve a page.
	 */

	public static void main(String[] args)
	{
		SecureHTTPLoader loader = new SecureHTTPLoader();

		loader.setDebug(true);
		loader.setCookiesSupported(true);

		if (args.length == 1)
		{
			String  page = loader.getURL(args[0]);

			System.out.println(page);
		}
		else
		{
			long average = 0;
			int count = Integer.parseInt(args[1]);

			for (int i = 0; i < count; i++)
			{
				long loadtime = System.currentTimeMillis();
				String page = loader.getURL(args[0]);

				System.out.println(loader.getCookieString());


				// System.out.println(page);

				average += System.currentTimeMillis() - loadtime;
			}

			System.out.println("Average load time: " + average / count + "ms");
		}
	}


    
	class TrustEverything implements X509TrustManager
	{
		public TrustEverything()
		{
			;
		}


		public void checkClientTrusted(X509Certificate[] chain, String authType)
		{
			return;
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
		{
			return;
		}

		public X509Certificate[] getAcceptedIssuers()
		{
			return(new X509Certificate[0]);
		}
	}
	
}


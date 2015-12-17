
package com.cometway.httpd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;

/**
 * Routes HTTPS Requests from a server socket
 */
public class SecureWebServer extends WebServer
{

	public static final String VERSION_STR = "Comet Way Secure Web Server 2.4 2006-05-15";


    /**
     * Initializes this agent's properties by providing default values for each of the following missing properties:
     * 'bind_port' (default: 443)
     * 'certificate1' (default: testcert.pem) This must be a non-passphrased X509 certificate in PEM format
     * 'server_key' (default: testkey.key) This must be a non-passphrased PKCS8 encoded private key associated with the certificate in PEM format
     *
     * The certificate and key properties are pathnames to a certificate and private key. If there is no path, the
     * current directory will be used.
     *
     * @see com.cometway.httpd.WebServer for the rest of the properties
     */
	public void initProps()
	{
	    // Specific to this agent.
		setDefault("bind_port", "443");
		setDefault("certificate1","testcert.pem");
		setDefault("server_key","testkey.key");

		// Set the rest of the properties from the WebServer
		super.initProps();
	}

    /**
     * This method uses the server key and certificate to create an SSLServerSocket.
     */
	protected ServerSocket getServerSocket(String bind_address, int port) throws java.io.IOException
	{
		ServerSocket rval = null;
		FileInputStream fis = null;

		try {
			File cert = new File(getString("certificate1"));
			File key = new File(getString("server_key"));
	    
			fis = new FileInputStream(cert);
				    
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(fis);
			X509Certificate[] certs = new X509Certificate[1];
			certs[0] = certificate;
		
			fis = new FileInputStream(key);
			byte[] buffer = new byte[(int)key.length()];
			fis.read(buffer);
		
			PKCS8EncodedKeySpec encodedKey = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privatekey = keyFactory.generatePrivate(encodedKey);
		
			KeyStore store = KeyStore.getInstance("JKS","SUN");
			store.load(null, null);
			store.setKeyEntry("server", privatekey, new char[0], certs);
		
		
			SSLContext sc = SSLContext.getInstance("TLS");
			KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
			keyManager.init(store,new char[0]);
		
			//				    TrustManager[] tms = new TrustManager[1];
			//				    tms[0] = new TrustEverything();
			//				    sc.init(keyManager.getKeyManagers(),tms,null);
			sc.init(keyManager.getKeyManagers(),null,null);
			ServerSocketFactory ssf = sc.getServerSocketFactory();
			if (bind_address.equals("all")) {
				rval = ssf.createServerSocket(port,50);
			}
			else {
				InetAddress address = InetAddress.getByName(bind_address);
				rval = ssf.createServerSocket(port,50,address);
			}		
		}
		catch(java.security.cert.CertificateException ce) {
			error("Exception while opening socket", ce);
		}
		catch(java.security.NoSuchAlgorithmException nsae) {
			error("Exception while opening socket", nsae);
		}
		catch(java.security.spec.InvalidKeySpecException ikse) {
			error("Exception while opening socket", ikse);
		}
		catch(java.security.KeyStoreException kse) {
			error("Exception while opening socket", kse);
		}
		catch(java.security.KeyManagementException kme) {
			error("Exception while opening socket", kme);
		}
		catch(java.security.NoSuchProviderException nspe) {
			error("Exception while opening socket", nspe);
		}
		catch(java.security.UnrecoverableKeyException uke) {
			error("Exception while opening socket", uke);
		}

		try
		{
			fis.close();
		}
		catch (Exception e)
		{;}

		return (rval);
	}

    
	protected byte[] readBinaryFile(File file)
	{
		byte[] rval = null;

		if (file.exists())
		{
			rval = new byte[(int)file.length()];
			BufferedInputStream in = null;

			try
			{
				in = new BufferedInputStream(new FileInputStream(file));
				in.read(rval);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				in.close();
			}
			catch(Exception e)
			{;}
		}

		return (rval);
	}
}

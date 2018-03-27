/**
 * Project owner and Copyright permission 
 */
package com.greenkoncepts.gateway.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;


/**
 * @Author HuongHV
 */
public class SslUtil {
  public static SSLSocketFactory getSocketFactory (final String caCrtFile, final String crtFile, final String keyFile, final String password) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
     
    // load CA certificate
    PEMParser reader = new PEMParser(new InputStreamReader(new FileInputStream(new File(caCrtFile))));
    //PEMParser reader = new PEMParser(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(caCrtFile)));
    
    X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
    Certificate caCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(caCertHolder);
    reader.close();
     
    // load client certificate
    reader = new PEMParser(new InputStreamReader(new FileInputStream(new File(crtFile))));
    //reader = new PEMParser(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(crtFile)));
    X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
    Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
    
    reader.close();
     
    // load client private key
    reader = new PEMParser(new InputStreamReader(new FileInputStream(new File(keyFile))));
    //reader = new PEMParser(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(keyFile)));
    // Load the key object
    Object privatekey = reader.readObject();
    reader.close();
    
    // Check to see if the object returned is an encrypted key pair
    if (privatekey instanceof PEMEncryptedKeyPair) {
      try {
        privatekey = ((PEMEncryptedKeyPair) privatekey).decryptKeyPair(new JcePEMDecryptorProviderBuilder().build(password.toCharArray()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    
    byte[] encodedPublicKey = null;
    byte[] encodedPrivateKey = null;
    
    // Cast to a PEMKeyPair
    if (privatekey instanceof PEMKeyPair) {
      PEMKeyPair pair = (PEMKeyPair) privatekey;
      encodedPublicKey = pair.getPublicKeyInfo().getEncoded();
      encodedPrivateKey = pair.getPrivateKeyInfo().getEncoded();
    } 
   
    KeyFactory keyFactory = KeyFactory.getInstance( "RSA");
                             
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
    PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
    
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
    PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
    
    KeyPair key = new KeyPair(publicKey, privateKey);
    // --------------------------------------
    
    // CA certificate is used to authenticate server
    KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
    caKs.load(null, null);
    caKs.setCertificateEntry("ca-certificate", caCert);
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(caKs);
     
    // client key and certificates are sent to server so it can authenticate us
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    ks.setCertificateEntry("certificate", cert);
    ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, password.toCharArray());
     
    // finally, create SSL socket factory
    SSLContext context = SSLContext.getInstance("TLSv1");
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    
    return context.getSocketFactory();
  }
  
  /*
   * For test only
   */
  public static void main (String[] args) {

  }
}

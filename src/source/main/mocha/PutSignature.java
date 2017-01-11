package source.main.mocha;
import java.io.File;
import java.util.List;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.fornwall.apksigner.*;

/**
 * This class put a signature ato apk file.
 * @author takeuchi
 *
 */
public class PutSignature {
	static void usage(Options paramOptions)
	  {
	    new HelpFormatter().printHelp("apksigner [options] keystore input-apk output.apk", "Signs an input APK file using the specified keystore to produce a signed and zipaligned output APK.", paramOptions, "");
	    System.exit(1);
	  }
	
	
	public static void main(String[] args) throws Exception{
		String[] test = {"-p","csp_key1","csp.keystore","csptest.apk","cspcerts.apk"};
	}
	
	public void putSignature(String... paramVarArgs) throws Exception {
		
		Options localOptions = new Options();
	    CommandLine localCommandLine = null;
	    Option localOption1 = new Option("h", "help", false, "Display usage information.");
	    localOptions.addOption(localOption1);
	    Option localOption2 = new Option("p", "password", false, "Password for private key (default:android).");
	    localOption2.setArgs(1);
	    localOptions.addOption(localOption2);
	    
	    try
	    {
	      localCommandLine = new BasicParser().parse(localOptions, paramVarArgs);
	    }
	    catch (ParseException localParseException)
	    {
	      System.err.println(localParseException.getMessage());
	      usage(localOptions);
	    }
	    if (localCommandLine.hasOption(localOption1.getOpt())) {
	      usage(localOptions);
	    }
	    List localList = localCommandLine.getArgList();
	    if (localList.size() != 3) {
	      usage(localOptions);
	    }
	    String str1 = (String)localList.get(0);
	    String str2 = (String)localList.get(1);
	    String str3 = (String)localList.get(2);
	    char[] arrayOfChar;
	    if (localCommandLine.hasOption(localOption2.getOpt()))
	    {
	      Object localObject1 = localCommandLine.getOptionValue(localOption2.getOpt());
	      if ((localObject1 == null) || (((String)localObject1).equals(""))) {
	        arrayOfChar = null;
	      } else {
	        arrayOfChar = ((String)localObject1).toCharArray();
	      }
	    }
	    else
	    {
	      arrayOfChar = "android".toCharArray();
	    }
	    Object localObject1 = new File(str1);
	    if (!((File)localObject1).exists())
	    {
	      Object localObject2 = "alias";
	      System.out.println("Creating new keystore (using '" + new String(arrayOfChar) + "' as password and '" + (String)localObject2 + "' as the key alias).");
	      Object localObject3 = new CertCreator.DistinguishedNameValues();
	      ((CertCreator.DistinguishedNameValues)localObject3).setCommonName("APK Signer");
	      ((CertCreator.DistinguishedNameValues)localObject3).setOrganization("Earth");
	      ((CertCreator.DistinguishedNameValues)localObject3).setOrganizationalUnit("Earth");
	      CertCreator.createKeystoreAndKey(str1, arrayOfChar, "RSA", 2048, (String)localObject2, arrayOfChar, "SHA1withRSA", 30, (CertCreator.DistinguishedNameValues)localObject3);
	    }
	    Object localObject2 = KeyStoreFileManager.loadKeyStore(str1, null);
	    Object localObject3 = (String)((KeyStore)localObject2).aliases().nextElement();
	    X509Certificate localX509Certificate = (X509Certificate)((KeyStore)localObject2).getCertificate((String)localObject3);
	    try
	    {
	      PrivateKey localPrivateKey = (PrivateKey)((KeyStore)localObject2).getKey((String)localObject3, arrayOfChar);
	      ZipSigner.signZip(localX509Certificate, localPrivateKey, "SHA1withRSA", str2, str3);
	    }
	    catch (UnrecoverableKeyException localUnrecoverableKeyException)
	    {
	      System.err.println("apksigner: Invalid key password.");
	      System.exit(1);
	    }
	  }
}


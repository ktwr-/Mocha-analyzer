package source.main.mocha;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import javax.xml.bind.DatatypeConverter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * CSP Source Hash 
 * calculate source hash of script
 *  
 */

public class SourceHash {
	// javascript hash list
	ArrayList<String> scriptHashlist = new ArrayList<String>();
	// CSS hash list
	ArrayList<String> styleHashlist = new ArrayList<String>();
	
	private static final String TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	
	SourceHash(){	
	}
	
	public static void main(String args[]){
		SourceHash sh = new SourceHash();
		String test = sh.calc_hash("alert('1');", "sha-256");
		System.out.println(test);
	}
	
	/**
	 * This function find javascript or CSS strings and calculate source hash from them.
	 * 
	 * @param filename which you want to analyze
	 * @return Document 
	 */
	public Document add_source_hash(String filename){
		Document doc=null;
		try{
			File file = new File(filename);
			doc = Jsoup.parse(file,"UTF-8");
			// find javascript and calculate source hash
			scriptHashlist = new ArrayList<String>(source_hash(doc,"script"));
			// find CSS and calculate source hash
			styleHashlist = new ArrayList<String>(source_hash(doc,"style"));
		}catch(IOException e){
			System.out.println(e);
		}
		return doc;
	}
	
	/**
	 * This function inserts policy directive of Content Security Policy to document
	 * 
	 * @param doc document of file which you want to use source hash
	 * @return Document
	 */
	public Document insertCSP(Document doc){
		StringBuilder sb = new StringBuilder();
			sb.append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *; script-src 'self' ");
		if(!scriptHashlist.isEmpty()){
			for(int i = 0;i < scriptHashlist.size();i++){	
				sb.append("'");
		//		System.out.println(script_hashlist.get(i));
				sb.append(scriptHashlist.get(i));
				sb.append("' ");
			}
		}
		sb.append(";obj-src 'self';style-src 'self' ");
		if(!styleHashlist.isEmpty()){
			for(int i = 0; i < styleHashlist.size();i++){
				sb.append("'");
				sb.append(styleHashlist.get(i));
				sb.append("'");
			}
		}
		sb.append(";\">");
		Elements head = doc.getElementsByTag("head");
		head.first().before(sb.toString());
		//System.out.println(doc.toString());
		
		return doc;
	}
	/**
	 * 
	 * @param doc document of file which you want to find javascript
	 * @param tagname script or style
	 * @return
	 */
	private ArrayList<String> source_hash(Document doc,String tagname){
		ArrayList<String> hashlist = new ArrayList<String>();
		Elements ele = doc.getElementsByTag(tagname);
		for(int i =0;i<ele.size();i++){
			Element script = ele.get(i);
			if(!script.toString().contains("src")){
				String source = script.toString();
				String sourcepat = "<"+tagname+".*?>([\\s\\S]*)</"+tagname+">";
				Pattern p = Pattern.compile(sourcepat);
				Matcher m = p.matcher(source);
				if(m.find()){
					String hash = calc_hash(m.group(1),"sha-256");
					hashlist.add("sha256-"+hash);
				}
			}
		}
		return hashlist;
	}
	/**
	 * This function calculate source hash 
	 * 
	 * @param source which you can find javascript or CSS source in file.
	 * @param algoname which you can use sha-256 sha-512
	 * @return
	 */
	private String calc_hash(String source,String algoname){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance(algoname);
		}catch(NoSuchAlgorithmException e){
			System.out.println(e);
		}
		md.update(source.getBytes());
		//String encoded = Base64.getEncoder().encodeToString(md.digest());
		String oriencoded = encode(md.digest());
		//System.out.println(encoded);
		System.out.println(oriencoded);
		//String encoded = DatatypeConverter.printBase64Binary(md.digest());
	    return oriencoded;
		
	}
	
	public static String encode(String data){
	    try{
	      return encode(data.getBytes("UTF-8"));
	    }catch(Exception e){
	      return "";
	    }
	  }

	  public static String encode(byte data[]){
	    if(data.length == 0){ return ""; }
	    
	    int i;
	    int index[] = new int[1 + data.length * 4 / 3];
	    int count = 0;
	    int limit = data.length - 3;
	    int mod;
	    int padding;
	    StringBuilder result = new StringBuilder("");
	    
	    for(i = 0; i < limit; i+=3){
	      index[count++] = (data[i] & 0xfc) >>> 2;
	      index[count++] = ((data[i] & 0x03) << 4) + ((data[i+1] & 0xf0) >>> 4);
	      index[count++] = ((data[i+1] & 0x0f) << 2) + ((data[i+2] & 0xc0) >>> 6);
	      index[count++] = ((data[i+2]) & 0x3f);
	    }
	    
	    mod = data.length % 3;
	    if(mod == 0){
	      index[count++] = (data[i] & 0xfc) >>> 2;
	      index[count++] = ((data[i] & 0x03) << 4) + ((data[i+1] & 0xf0) >>> 4);
	      index[count++] = ((data[i+1] & 0x0f) << 2) + ((data[i+2] & 0xc0) >>> 6);
	      index[count++] = ((data[i+2]) & 0x3f);
	    }
	    else if(mod == 1){
	      index[count++] = (data[i] & 0xfc) >>> 2;
	      index[count++] = (data[i] & 0x03) << 4;
	    }
	    else if(mod == 2){
	      index[count++] = (data[i] & 0xfc) >>> 2;
	      index[count++] = ((data[i] & 0x03) << 4) + ((data[i+1] & 0xf0) >>> 4);
	      index[count++] = (data[i+1] & 0x0f) << 2;
	    }

	    for(i = 0 ; i < count; i++){
	      result.append(TABLE.charAt(index[i]));
	    }
	    
	    padding = (4 - result.length() % 4) % 4;
	    for(i = 0 ; i < padding ; i++){
	      result.append("=");
	    }
	    
	    return result.toString();
	  }
}

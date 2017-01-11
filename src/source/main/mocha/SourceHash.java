package source.main.mocha;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	ArrayList<String> script_hashlist = new ArrayList<String>();
	// CSS hash list
	ArrayList<String> style_hashlist = new ArrayList<String>();
	
	SourceHash(){	
	}
	
	public static void main(String args[]){
		SourceHash sh = new SourceHash();
		String test = sh.calc_hash("alert('引くなっ！');", "sha-256");
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
			script_hashlist = new ArrayList<String>(source_hash(doc,"script"));
			// find CSS and calculate source hash
			style_hashlist = new ArrayList<String>(source_hash(doc,"style"));
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
		if(!script_hashlist.isEmpty()){
			for(int i = 0;i < script_hashlist.size();i++){	
				sb.append("'");
		//		System.out.println(script_hashlist.get(i));
				sb.append(script_hashlist.get(i));
				sb.append("' ");
			}
		}
		sb.append(";obj-src 'self';style-src 'self' ");
		if(!style_hashlist.isEmpty()){
			for(int i = 0; i < style_hashlist.size();i++){
				sb.append("'");
				sb.append(style_hashlist.get(i));
				sb.append("'");
			}
		}
		sb.append(";\">");
		Elements head = doc.getElementsByTag("head");
		head.append(sb.toString());
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
		String encoded = Base64.getEncoder().encodeToString(md.digest());
	    return encoded;
		
	}
}

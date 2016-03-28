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

public class SourceHash {
	ArrayList<String> scripthashlist = new ArrayList<String>();
	ArrayList<String> stylehashlist = new ArrayList<String>();
	SourceHash(){
		
	}
	
	public static void main(String args[]){
		SourceHash sh = new SourceHash();
		String test = sh.calc_hash("alert('引くなっ！');", "sha-256");
		System.out.println(test);
	}
	
	public Document add_source_hash(String filename){
		Document doc=null;
		try{
			File file = new File(filename);
			doc = Jsoup.parse(file,"UTF-8");
			scripthashlist = new ArrayList<String>(source_hash(doc,"script"));
			stylehashlist = new ArrayList<String>(source_hash(doc,"style"));
		}catch(IOException e){
			System.out.println(e);
		}
		return doc;
	}
	
	public Document insertCSP(Document doc){
		StringBuilder sb = new StringBuilder();
			sb.append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *; script-src 'self' ");
		if(!scripthashlist.isEmpty()){
			for(int i = 0;i < scripthashlist.size();i++){	
				sb.append("'");
				//System.out.println(scripthashlist.get(i));
				sb.append(scripthashlist.get(i));
				sb.append("' ");
			}
		}
		sb.append(";obj-src 'self';style-src 'self';\">");
		Elements head = doc.getElementsByTag("head");
		head.append(sb.toString());
		System.out.println(doc.toString());
		
		return doc;
	}
	
	public ArrayList<String> source_hash(Document doc,String tagname){
		ArrayList<String> hashlist = new ArrayList<String>();
		Elements ele = doc.getElementsByTag(tagname);
		System.out.println(ele.toString());
		for(int i =0;i<ele.size();i++){
			Element script = ele.get(i);
			String pat = "(.*)<"+tagname+"(.*?)>(.*)";
			Matcher tm = Pattern.compile(pat).matcher(script.toString());
			tm.find();
			if(!tm.group(2).contains("src")){
				String source = script.toString();
				String sourcepat = "<"+tagname+".*?>([\\s\\S]*)</"+tagname+">";
				Pattern p = Pattern.compile(sourcepat);
				Matcher m = p.matcher(source);
				if(m.find()){
					String hash = calc_hash(m.group(1),"sha-256");
					System.out.println(hash);
					hashlist.add("sha256-"+hash);
				}
			}
		}
		return hashlist;
	}
	
	public String calc_hash(String source,String algoname){
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

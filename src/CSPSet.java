import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CSPSet {
	
	String csp_policy;
	HashMap<String,String> tagmatch;
	ArrayList<String> scriptdomain;
	ArrayList<String> styledomain;
	ArrayList<String> scripthash;
	ArrayList<String> stylehash;
	
	CSPSet(String policy){
		csp_policy =policy;
	}
	
	CSPSet(){
		
	}
	
	CSPSet(ArrayList<String> hashsc,ArrayList<String> hashst){
		if(!hashsc.isEmpty() ){
			scripthash = new ArrayList<String>(hashsc);
		}
		if(!hashst.isEmpty()){
			stylehash = new ArrayList<String>(hashst);
		}
		
	}
	
	public static void main(String args[]){
			CSPSet cs = new CSPSet();
			cs.init();
			ArrayList<String> file = new ArrayList<String>();
			file.add("./test/test.html");
			cs.add_path_matching(file);
	}
	
	public void init(){
		tagmatch = new HashMap<String,String>();
		tagmatch.put("script", "js");
		tagmatch.put("link", "css");
	}
	
	public void add_path_matching(ArrayList<String> htmlfile){
		for(int i=0; i<htmlfile.size();i++){
			try{
				File file = new File(htmlfile.get(i));
				Document doc = Jsoup.parse(file, "UTF-8");
				// for script tag
				scriptdomain = new ArrayList<String>(search_domain_for2("script",doc));
				// for style tag
				styledomain = new ArrayList<String>(search_domain_for2("style",doc));
				
			
			}catch(IOException e){
				System.out.println(e);
			}
		}
	}
	
	// for csp level2
	public ArrayList<String> search_domain_for2(String tagname,Document doc){
		Elements ele=doc.getElementsByTag(tagname);
		ArrayList<String> domain = new ArrayList<String>();
		for(int i = 0; i < ele.size(); i++){
			Element tmp = ele.get(i);
			if(tmp.toString().contains("http")){
				String pat = "(.*)(https?://.*\\."+tagmatch.get(tagname)+")\"(.*)";
				Matcher m = Pattern.compile(pat).matcher(tmp.toString());
				if(m.find()){
					System.out.println(m.group(2));
					domain.add(m.group(2));
				}
			}
		}
		
		return domain;
	}
	
	//for csp level1
	public ArrayList<String> search_domain_for1(String tagname, Document doc){
		Elements ele=doc.getElementsByTag(tagname);
		ArrayList<String> domain = new ArrayList<String>();
		for(int i=0;i<ele.size();i++){
			Element tmp = ele.get(i);
			if(tmp.toString().contains("http")){
				String pat = "(.*)(https?://.*?)(/.*\\."+tagmatch.get(tagname)+")\"(.*)";
				Matcher m = Pattern.compile(pat).matcher(tmp.toString());
				if(m.find()){
					System.out.println(m.group(2));
					domain.add(m.group(2));
				}
			}
		}
		if(!domain.isEmpty()){
			return domain;
		}
		return null;
	}
	
	
}
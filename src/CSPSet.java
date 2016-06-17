import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	Boolean bscdomain,bstdomain;
	Boolean bschash,bsthash;
	
	CSPSet(String policy){
		csp_policy =policy;
	}
	
	CSPSet(){
		bschash=false;
		bsthash=false;
		bscdomain=false;
		bstdomain=false;
		init();
	}
	
	public Document setCSP(Document doc,int csplevel,String directory){
		System.out.println("setCSP start");
		StringBuilder sb = new StringBuilder();
		String policy = "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *;script-src 'self' ";
		String stpolicy = "style-src 'self'";
		String quote = "'";
		String coron = ";";
		sb.append(policy);
		if(csplevel == 1){
			scriptdomain = new ArrayList<String>(search_domain_for1("script",doc));
			ArrayList<String> url = new ArrayList<String>(list_url_jsfile(doc,directory));
			scriptdomain.addAll(url);
			if(!scriptdomain.isEmpty()) bscdomain = true;
				
			styledomain = new ArrayList<String>(search_domain_for1("link",doc));
			if(!styledomain.isEmpty()) bstdomain = true;
				
		}else if(csplevel == 2){
			scriptdomain = new ArrayList<String>(search_domain_for2("script",doc));
			if(!scriptdomain.isEmpty()) bscdomain = true;
				
			styledomain = new ArrayList<String>(search_domain_for2("link",doc));
			if(!styledomain.isEmpty()) bstdomain = true;
		}
		
		if(bschash){
			for(int i=0; i < scripthash.size(); i++){
				sb.append(quote);
				sb.append(scripthash.get(i));
				sb.append(quote);
			}
		}
		if(bscdomain){
			for(int i=0; i < scriptdomain.size(); i++){
				sb.append(quote);
				sb.append(scriptdomain.get(i));
				sb.append(quote);
			}
		}
		sb.append(coron);
		sb.append(stpolicy);
		if(bsthash){
			for(int i=0; i< stylehash.size(); i++){
				sb.append(quote);
				sb.append(stylehash.get(i));
				sb.append(quote);
			}
		}
		if(bstdomain){
			for(int i = 0; i < styledomain.size(); i++){
				sb.append(quote);
				sb.append(styledomain.get(i));
				sb.append(quote);
			}
		}
		sb.append(coron);
		sb.append("\">");
		System.out.println(sb.toString());
		Elements header = doc.getElementsByTag("head");
		if(!header.get(0).toString().contains("Content-Security-Policy")){
			header.append(sb.toString());
		}else{
			String pat ="(.*)(<meta.*?http-equiv.*?Content-Security-Policy.*>)(.*)";
			Matcher m = Pattern.compile(pat).matcher(header.toString());
			m.find();
			System.out.println("test");
			System.out.println(m.group(2));
		}
		return doc;
	}
	
	
	public static void main(String args[]){
			CSPSet cs = new CSPSet();
			cs.init();
			
			ArrayList<String> file = new ArrayList<String>();
			file.add("./test/test.html");
			//cs.add_path_matching(file);
			try {
				Document doc = Jsoup.parse(new File(file.get(0)),"UTF-8");
				/*
				ArrayList<String> js_in_html = new ArrayList<String>(cs.search_js_inhtml(doc,"./test/"));
				ArrayList<String> js_in_html_url = new ArrayList<String>();
				for(int i =0;i<js_in_html.size();i++){
					modify_ajax md = new modify_ajax();
					HashSet<String> temp = new HashSet<String>(md.search_ajax(md.fileReader(js_in_html.get(i))));
					temp = new HashSet<String>(md.extrace_basedomain(temp));
					js_in_html_url.addAll(new ArrayList<String>(temp));
				}*/
				ArrayList<String> js_in_html_url = new ArrayList<String>(cs.list_url_jsfile(doc,"./test/"));
				for(int i=0;i<js_in_html_url.size();i++){
					System.out.println(js_in_html_url.get(i));
				}
				
			//	doc  = cs.setCSP(doc,1);
			//	System.out.println(doc.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	public void init(){
		tagmatch = new HashMap<String,String>();
		tagmatch.put("script", "js");
		tagmatch.put("link", "css");
	}
	
	public ArrayList<String> list_url_jsfile(Document doc,String directory){
		ArrayList<String> js_in_html = new ArrayList<String>(search_js_inhtml(doc,directory));
		ArrayList<String> js_in_html_url = new ArrayList<String>();
		System.out.println("test");
		for(int i =0;i<js_in_html.size();i++){
			System.out.println(js_in_html.get(i));
			modify_ajax md = new modify_ajax();
			HashSet<String> temp = new HashSet<String>(md.search_ajax(md.fileReader(js_in_html.get(i))));
			temp = new HashSet<String>(md.extrace_basedomain(temp));
			js_in_html_url.addAll(new ArrayList<String>(temp));
		}
		return js_in_html_url;
		
		
	}
	
	private ArrayList<String> search_js_inhtml(Document doc,String directory){
		Elements els = doc.getElementsByAttribute("src");
		ArrayList<String> jsfile = new ArrayList<String>();
		for(int i=0;i<els.size();i++){
			Element e = els.get(i);
			if(!e.toString().contains("http") && e.toString().contains("script")){
				Matcher m = Pattern.compile("src *= *[\'\"](.*)[\'\"]").matcher(e.toString());
				m.find();
				System.out.println(directory+m.group(1));
				jsfile.add(directory+m.group(1));
			}
			
		}
		return jsfile;
		
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
			System.out.println(tmp.toString());
			if(tmp.toString().contains("http")){
				bstdomain = true;
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
				bstdomain=true;
				String pat = "(.*)(https?://.*?)(/.*\\."+tagmatch.get(tagname)+")\"(.*)";
				Matcher m = Pattern.compile(pat).matcher(tmp.toString());
				if(m.find()){
					System.out.println(m.group(2));
					domain.add(m.group(2));
				}
			}
		}
		return domain;
	}
	
	public void setHashScript(ArrayList<String> hashsc){
		scripthash = new ArrayList<String>(hashsc);
		bschash = true;
	}
	public void setHashStyle(ArrayList<String> hashst){
		stylehash = new ArrayList<String>(hashst);
		bsthash = true;
	}
	
}

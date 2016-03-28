import java.io.File;
import java.io.FileWriter;
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
	
	public Document setCSP(Document doc,int csplevel){
		StringBuilder sb = new StringBuilder();
		String policy = "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *; report-uri /report.php;script-src 'self' ";
		String stpolicy = "style-src 'self' ";
		String quote = "'";
		String coron = ";";
		sb.append(policy);
		
		if(csplevel == 1){
			scriptdomain = new ArrayList<String>(search_domain_for1("script",doc));
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
			System.out.println(m.group(2));
			String html = doc.toString().replaceFirst(Pattern.quote(m.group(2)), "");
			doc = Jsoup.parse(html);
			header = doc.getElementsByTag("head");
			header.append(sb.toString());
		}
		return doc;
	}
	
	
	public static void main(String args[]){
			CSPSet cs = new CSPSet();
			jscript ajs = new jscript();
			cs.init();
			ArrayList<String> file = new ArrayList<String>();
			cs.add_path_matching(file);
			try {
				copyfile();
				System.out.println("finish");
				Thread.sleep(10000);
				ajs.getfilename("./csp");
			} catch (IOException e) {
				System.out.println(e);
			} catch (InterruptedException e){
				System.out.println(e);
			}
			
			//js.htmlanalyze("./csp/test.html");
			for(int i=0;i<ajs.htmlfile.size();i++){
				System.out.println(ajs.htmlfile.get(i));
				file.add(ajs.htmlfile.get(i));
			}
			try {
				for(int i =0;i<file.size();i++){
				Document doc = Jsoup.parse(new File(file.get(i)),"UTF-8");
				doc  = cs.insertnoneCSP(doc);
				System.out.println(doc.toString());
				File f = new File(file.get(i));
				FileWriter fw = new FileWriter(f);
				fw.write(doc.toString());
				fw.close();
				
				}
			}	 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				styledomain = new ArrayList<String>(search_domain_for2("link",doc));
				
			
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
	
	public Document insertselfCSP(Document doc){
		Element head = doc.getElementsByTag("head").get(0);
		String header = "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'self'; report-uri /report.php \">";
		head.append(header);
		return doc;
	}
	public Document insertnoneCSP(Document doc){
		Element head = doc.getElementsByTag("head").get(0);
		String header = "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src 'none'; report-uri /report.php \">";
	
		head.append(header);
		return doc;
	}
	public static void copyfile() throws IOException{
		String[] command = {"/bin/sh", "-c","cp -r ./hyouka/* ./csp"};
		Runtime.getRuntime().exec("mkdir csp");
		Runtime.getRuntime().exec(command);
		System.out.println("cp command");
	}

	
}

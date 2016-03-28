import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.regex.Matcher;



public class jscript {
	
	static ArrayList<String> removetag = new ArrayList<String>();
	static ArrayList<String> fileextension = new ArrayList<String>();
	static HashMap<String,String> eventhandler = new HashMap<String,String>();
	static HashMap<String,String> eventbody = new HashMap<String,String>();
	ArrayList<String> htmlfile = new ArrayList<String>();
	ArrayList<String> jsfile = new ArrayList<String>();
	ArrayList<String> ejsfile = new ArrayList<String>();
	static ArrayList<String> header;
	static ArrayList<String> body;
	static char jsfilename = 'a';
	static char stylefilename = 'a';
	static char eventid = 'a';
	static char eventname= 'a';
	static Boolean baJS = false;
	static int csplevel = 1;
	static Boolean noncesource =false;
	
	public static void main(String args[]) throws IOException{
		jscript js = new jscript();
		if(csplevel == 1){
			analyzeScript aS = new analyzeScript();
			
			try {
				copyfile();
				System.out.println("finish");
				Thread.sleep(1000);
				js.getfilename("./csp");
			} catch (IOException e) {
				System.out.println(e);
			} catch (InterruptedException e){
				System.out.println(e);
			}
			
			js.inline_init();
			//js.htmlanalyze("./csp/test.html");
			
			for(int i=0;i<js.htmlfile.size();i++){
				System.out.println(js.htmlfile.get(i));
				js.htmlanalyze(js.htmlfile.get(i));
			}
			//System.out.println("sample");
			//for(int i=0;i<jsfile.size();i++){
			//	System.out.println(jsfile.get(i));
			//	aS.analyzeScript(jsfile.get(i));
			//}
			
		}else if(csplevel == 2){
			if(noncesource == true){
				for(int i = 0;i<js.ejsfile.size();i++){
					NonceSource  ns = new NonceSource();
				}
			}else{
				System.out.println("test");
				try {
					copyfile();
					System.out.println("finish");
					Thread.sleep(1000);
					js.getfilename("./csp");
				} catch (IOException e) {
					System.out.println(e);
				} catch (InterruptedException e){
					System.out.println(e);
				}
				//source hash script
				Document doc;
				for(int i = 0;i<js.htmlfile.size();i++){
					CSPSet csp = new CSPSet();
					SourceHash sh = new SourceHash();
					String filename= js.htmlfile.get(i);
					String filepat = "(.*/)(.*)\\.(.*)";
					String directory = patternmatch(filename,filepat).group(1);
					doc = sh.add_source_hash(filename);
					String html = js.divideevent(doc, doc.toString(),directory);
					csp.setHashScript(sh.scripthashlist);
					csp.setHashStyle(sh.stylehashlist);
					doc = csp.setCSP(doc,csplevel);
					System.out.println("\nlast result\n");
					System.out.println(doc.toString());
				}
			}
		}
	}
	
	public void inline_init(){
		//correspondence table of event handler
		eventhandler.put("onclick", "click");
		eventhandler.put("ondblclick", "dblclick");
		eventbody.put("onload", "load");
		
		baJS = false;
	}
	
	public Document insertCSP(Document doc){
		Elements header = doc.getElementsByTag("head");
		header.append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *; script-src 'self'; object-src 'self'; style-src 'self';\">");
		return doc;
	}
	
	
	
	/**/
	
	public void htmlanalyze(String filename){
		CSPSet csp = new CSPSet();
		try{
			String filepat = "(.*/)(.*)\\.(.*)";
			String directory = patternmatch(filename,filepat).group(1);
			String beforedot = patternmatch(filename,filepat).group(2);
			File file = new File(filename);
			Document doc = Jsoup.parse(file, "UTF-8");
			String html = doc.toString();
			html = dividescript(doc,html,directory);
			doc = Jsoup.parse(html);
			html = dividestyle(doc,html,patternmatch(filename,filepat).group(2));
			doc = Jsoup.parse(html);
			html = dividehref(doc,html);
			doc = Jsoup.parse(html);
			html = divideevent(doc,html,directory);
			doc = Jsoup.parse(html);
			doc = csp.setCSP(doc,csplevel);
			html = doc.toString();
			FileWriter fw = new FileWriter(file);
			System.out.println("\nlast result\n"+html+"\n");
			fw.write(html);
			fw.close();
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	
	public void jsanalyze(String filename){
		try{
			File file = new File(filename);
			Document doc = Jsoup.parse(file, "UTF-8");
			System.out.println(doc.toString());
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public String dividescript(Document doc, String html,String filepath){
		Elements script = doc.getElementsByTag("script");
		for(int i=0;i < script.size();i++){
			Element tmp = script.get(i);
			if(tmp.toString().contains("src=")){
				String pat="(.*)<script().*src=(.*)>(.*)";
			}else{
				String mdhtml = dividescript(tmp.data(),filepath);
				html = html.replaceAll(Pattern.quote(tmp.toString()), mdhtml);
			}
		}
		return html;
	}
	
	public String dividestyle(Document doc,String html,String filename){
		Elements style = doc.getElementsByTag("style");
		for(int i=0; i< style.size();i++){
			Element tmp = style.get(i);
			//System.out.println(tmp.toString());
			if(!tmp.toString().contains("src=")){
				String mdhtml = dividestyle(tmp.data());
				html = html.replaceAll(Pattern.quote(tmp.toString()), mdhtml);
			}
		}
		Elements divstyle = doc.getElementsByAttribute("style");
		// modify style attribute
		String styletext = "";
		for(int i=0;i < divstyle.size();i++){
			Element tmp = divstyle.get(i);
			//System.out.println(tmp.toString());
			String stylepat = "(.*?)style=\"(.*?)\"(.*)";
			String id = String.valueOf(eventid);
			Matcher stylem = patternmatch(tmp.toString(),stylepat);
			if(stylem != null){
				String styleline= patternmatch(tmp.toString(),"(.*?)>(.*)").group(1)+">";
				System.out.println(styleline+"\n");
				String styleAttr = stylem.group(2);
				String mdhtml = "";
				String idpat = "<(.*?)id=\"(.*?)\"(.*?)>(.*)";
				Matcher idm = patternmatch(styleline,idpat);
				if(idm !=null){
					mdhtml = stylem.group(1)+stylem.group(3);
					id = idm.group(2);
				}else{
					mdhtml = stylem.group(1)+"id=\""+eventid+"\""+stylem.group(3)+"\n";
					eventid++;
				}
			styletext += textStyleAttr(id,styleAttr)+"\n";
			html = html.replaceAll(Pattern.quote(styleline),mdhtml); 
			}
			
		doc = Jsoup.parse(html);
		}
		if(!styletext.equals("")){
			try{
				File file = new File("./csp/"+filename+"styleattr.css");
				FileWriter fw = new FileWriter(file);
				fw.write(styletext);
				fw.close();
			}catch(IOException e){
				System.out.println(e);
			}
			Elements header = doc.getElementsByTag("head");
			header.append("<link href=\""+filename+"styleattr.css\" rel=\"stylesheet\" type=\"text/css\">");
		}
		
		
		return doc.toString();
	}
	
	public String dividehref(Document doc,String html){
			Elements atag = doc.getElementsByTag("a");
			for(int i=0;i < atag.size();i++){
				Element tmp = atag.get(i);
				if(tmp.toString().contains("href=\"javascript")){
					String pat="(.*)\"javascript:(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),pat);
					if(m != null){
						//System.out.println(m.group(2));
						if(m.group(2).contains("void(0)")){
							baJS = true;
							/*pattern <a href="javascript:(javascript)void(0)" onclick="(javascript)">test</a>*/
							if(tmp.toString().contains("onclick")){
								String onclickpat = "(.*?)onclick=\"(.*?)\"(.*)";
								String script = m.group(2);
								Matcher onclickm = patternmatch(m.group(1)+"\"\""+m.group(3),onclickpat);
								//System.out.println(onclickm.group());
								html = html.replaceAll(Pattern.quote(m.group()), onclickm.group(1)+"onclick=\""+onclickm.group(2)+";"+script+"\""+onclickm.group(3));
							/*pattern <a href="javascript:(javascript)void(0)></a>*/
							}else{
								html = html.replaceAll(Pattern.quote(m.group()), m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
							}
						}else{
							//System.out.println(m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
							html = html.replaceAll(Pattern.quote(m.group()), m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
							//System.out.println("test print\n");
						}
					}
						
				}
			}
		return html;
	}
	
	public String divideevent(Document doc,String html,String filepath){
		
		System.out.println("\n divide event");
		File file = new File(filepath+"event.js");
		int flag=0;
		try{
			FileWriter fw = new FileWriter(file);	
			if(baJS){
				fw.write("function stopDefAction(evt){ evt.preventDefault(); }\n");
			}
			
			for(String key : eventhandler.keySet()){
				Elements evhand = doc.getElementsByAttribute(key);
				for(int i=0;i<evhand.size();i++){
					String evid="";
					Element tmp = evhand.get(i);
					Boolean preventevent = false;
					//System.out.println(tmp.toString());
					String pat = "(.*)"+key+"=\"(.*?)\"(.*)";
					String patid = "(.*)id=\"(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),pat);
					String script = m.group(2);
					Matcher mid = patternmatch(tmp.toString(),patid);
					if(m.group(2).contains("void(0)")){
						preventevent = true;
						String rmvoid = tmp.toString().replace("void(0)", "");
						html = html.replaceAll(Pattern.quote(tmp.toString()), rmvoid);
						m= patternmatch(rmvoid,pat);
						System.out.println(m.group());
						script = m.group(2);
					}
					if(mid == null){
						evid = String.valueOf(eventid);
						html = html.replaceAll(Pattern.quote(m.group()), m.group(1)+"id=\""+eventid+"\""+m.group(3));
						eventid++;
					}else{
						evid = mid.group(2);
						html = html.replaceAll(Pattern.quote(m.group()), m.group(1)+m.group(3));
					}
					String template="";
					if(preventevent){
						template = preventvoid(evid,script,key); 
					}else{
						template = tempevent(evid,script,key);
					}
					//System.out.println(template);
					fw.write(template);
					flag = 1;
				}
			}
			for(String key : eventbody.keySet()){
				Elements evbody = doc.getElementsByAttribute(key);
				for(int i=0;i< evbody.size();i++){
					Element tmp = evbody.get(i);
					String pat = "(.*)"+key+"=\"(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),pat);
					String script = m.group(2);
					String template = loadevent(script,key);
					html = html.replaceAll(Pattern.quote(m.group()), m.group(1)+m.group(3));
					//System.out.println(template);
					fw.write(template);
					flag = 1;
				}
			}
			fw.close();
		}catch(IOException e){
			System.out.println(e);
		}
		doc = Jsoup.parse(html);
		if(flag == 1){
			Elements head = doc.getElementsByTag("head");
			head.append("\n<script src=\"event.js\"></script>");
			//System.out.println(head.toString());
		}
		return doc.toString();
	}
	/**
	 * modify setTimeout and setInterval if these contents conatins src="remote URI"
	 * 
	 * @param doc
	 * @param js 
	 * @return
	 */
	public String divideSetTI(Document doc,String js){
		
		return js;
	}
	
	/**
	 * write <html><head> ~ </head> part
	 * 
	 * @param fw writefile
	 */
	public static void writeheader(FileWriter fw){
		try{
			for(int i=0;i < header.size();i++){
				fw.write(header.get(i)+"\n");
				System.out.println(header.get(i)+"\n");
			}
			fw.write("</head>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * write after </head> part
	 * @param fw
	 */
	public static void writebody(FileWriter fw){
		try{
			for(int i=0;i < body.size();i++){
				fw.write(body.get(i)+"\n");
				System.out.println(body.get(i)+"\n");
			}
		}catch (IOException e){
			System.out.println(e);
		}
	}
	/**
	 * 
	 * @param str 		String which I want to analyze
	 * @param pattern	analyze pattern
	 * @return			return Matcher group
	 * 
	 * 
	 */
	public static Matcher patternmatch(String str,String pattern){
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		
		if(m.find()){
			return m;
		}
		return null;
	}
	
	/*divide file from html(now only Javascript)*/
	public static String dividescript(String text,String filepath){
		//System.out.println(text);
		/*write file*/
		String mdhtml="";
		try{
			File file = new File(filepath+jsfilename+".js");
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			
			fw.close();
			mdhtml = "<script src=\""+jsfilename+".js\"></script>\n";
			jsfilename++;
		}catch(IOException e){
			System.out.println(e);
			
		}
		
		/*instead of divide, write like <script src="~~" ></script> text*/
		
		return mdhtml;
	}
	
	public static String dividestyle(String text){
		String mdhtml = "";
		try{
			File file = new File("./csp/"+stylefilename+".css");
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			fw.close();
			mdhtml = "<link href=\""+stylefilename+".css\" rel=\"stylesheet\" type=\"text/css\">";
			stylefilename++;
		}catch(IOException e){
			System.out.println(e);
		}
		return mdhtml;
	}
	
	public String textStyleAttr(String id,String style){
		return "#"+id+" { " +style+" }";
	}
	public static String tempevent(String id,String script,String key){
		String templete = "var ev"+eventname+" = function() {\n"
				+ " var div = document.getElementById(\""+id+"\");\n"
				+ "	var popup = function () { \n"
				+ script +"; };\n div.addEventListener(\""+eventhandler.get(key)+"\",popup,false); };\n"
				+ "window.addEventListener(\"load\",ev"+eventname+",false);\n";
		eventname++;
		
		return templete;
	}
	
	public String preventvoid(String id,String script,String key){
		String template ="var ev"+eventname+" = function() {\n"
				+ " var div = document.getElementById(\""+id+"\");\n"
				+ "	var popup = function () { \n"
				+ script +"; };\n div.addEventListener(\""+eventhandler.get(key)+"\",popup,false); "
						+ "div.addEventListener(\"click\",stopDefAction,false); };\n"
				+ "window.addEventListener(\"load\",ev"+eventname+",false);\n";
		eventname++;;
		return template;
	}
	
	public void getfilename(String directory) throws IOException{
		File file = new File(directory);
		File files[] = file.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isFile()){
				if(files[i].getName().contains(".html")){
					htmlfile.add(files[i].toString());
				}else if(files[i].getName().contains(".js")){
					jsfile.add(files[i].toString());
				}else if(files[i].getName().contains(".ejs")){
					ejsfile.add(files[i].toString());
				}
			}else if(files[i].isDirectory()){
				getfilename(files[i].toString());
			}
		}
	}
	
	public static String loadevent(String script,String key){
		String templete ="var ev"+eventname+" = function(){ \n"+script+";\n};\n"
				+ "window.addEventListener(\"load\",ev"+eventname+",false);\n";
		eventname++;
		return templete;
	}
	
	public static void copyfile() throws IOException{
		String[] command = {"/bin/sh", "-c","cp -r ./test/* ./csp"};
		Runtime.getRuntime().exec("mkdir csp");
		Runtime.getRuntime().exec(command);
		System.out.println("cp command");
	}
	
	public static void reset() throws IOException{
		Runtime.getRuntime().exec("rm -rf csp");
		System.out.println("reset");
	}
	
}

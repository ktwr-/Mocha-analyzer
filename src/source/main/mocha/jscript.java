package source.main.mocha;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


// import package for using Jsoup
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.util.regex.Matcher;

public class jscript {
	
	static ArrayList<String> remove_tag = new ArrayList<String>();
	static ArrayList<String> file_extension = new ArrayList<String>();
	static HashMap<String,String> eventhandler = new HashMap<String,String>(){
		{
			put("onclick","click");
			put("ondblclick", "dblclick");
		}
	};
	static HashMap<String,String> eventbody = new HashMap<String,String>(){
		{
			put("onload","load");
		}
	};
	ArrayList<String> html_file = new ArrayList<String>();
	ArrayList<String> js_file = new ArrayList<String>();
	HashMap<String,ArrayList<String>> jsFileCheker = new HashMap<String,ArrayList<String>>(); // HashMap checks a JavaScript file is modified. ArrayList is file which is divide from JavaScript file.
	ArrayList<String> ejs_file = new ArrayList<String>();
	ArrayList<String> header;
	ArrayList<String> body;
	char jsfilename = 'a';
	char stylefilename = 'a';
	char eventid = 'a';
	char eventname= 'a';
	int csplevel = 1;
	public int inlinecount = 0;
	public int evalcount = 0;
	Boolean baJS = false;
	Boolean noncesource =false;
	
	public static void main(String args[]) throws IOException{
		jscript ajs = new jscript();
		if(ajs.csplevel == 1){
			analyzeScript aS = new analyzeScript();
			
			try {
				copyfile("www");
				System.out.println("finish");
				Thread.sleep(1000);
				ajs.getfilename("./csp");
			} catch (IOException e) {
				System.out.println(e);
			} catch (InterruptedException e){
				System.out.println(e);
			}
			
			for(int i=0;i<ajs.html_file.size();i++){
				System.out.println(ajs.html_file.get(i));
			}
			
			for(int i=0;i<ajs.html_file.size();i++){
				jscript js = new jscript();
				js.htmlAnalyze(ajs.html_file.get(i));
			}
			for(int i=0;i<ajs.js_file.size();i++){
				if(!ajs.js_file.get(i).contains("min.js") && !ajs.js_file.get(i).contains("leaflet") && !ajs.js_file.get(i).contains("data.js") && !ajs.js_file.get(i).contains("jquery")){
				System.out.println("file:"+ajs.js_file.get(i));
					aS.analyzeScript(ajs.js_file.get(i));
				}
			}
			for(int i=0;i<ajs.js_file.size();i++){
				System.out.println(ajs.js_file.get(i));
			}
			
		}else if(ajs.csplevel == 2){
			if(ajs.noncesource == true){
				for(int i = 0;i<ajs.ejs_file.size();i++){
					NonceSource  ns = new NonceSource();
				}
			}else{
				System.out.println("test");
				try {
					copyfile("test");
					System.out.println("finish");
					Thread.sleep(10000);
					ajs.getfilename("./csp");
				} catch (IOException e) {
					System.out.println(e);
				} catch (InterruptedException e){
					System.out.println(e);
				}
				//source hash script
				Document doc;
				for(int i = 0;i<ajs.html_file.size();i++){
					jscript js = new jscript();
					CSPSet csp = new CSPSet();
					SourceHash sh = new SourceHash();
					String filename= ajs.html_file.get(i);
					String filepat = "(.*/)(.*)\\.(.*)";
					String directory = patternmatch(filename,filepat).group(1);
					String beforedot = patternmatch(filename,filepat).group(2);
					doc = sh.add_source_hash(filename);
					String html = js.divideEvent(doc, doc.toString(),directory);
					doc = Jsoup.parse(html);
					doc = js.divide_styleid(doc,beforedot,directory);
					doc = Jsoup.parse(html);
					csp.setHashScript(sh.scriptHashlist);
					csp.setHashStyle(sh.styleHashlist);
					doc = csp.setCSP(doc,ajs.csplevel,directory,new JSList());
					System.out.println("\nlast result\n");
					System.out.println(doc.toString());
					File file = new File(filename);
					FileWriter fw = new FileWriter(file);
					fw.write(doc.toString());
					fw.close();
				}
			}
		}
	}
	/**
	 * 
	 * @param doc
	 * @return
	 */
	public Document insertCSP(Document doc){
		Elements header = doc.getElementsByTag("head");
		header.append("<meta http-equiv=\"Content-Security-Policy\" content=\"default-src *; script-src 'self'; object-src 'self'; style-src 'self';\">");
		return doc;
	}
	
	
	/**
	 * This function overwrites file.
	 * 
	 * @param filename(String) which you want to analyze.
	 * 
	 **/
	public void htmlAnalyze(String filename){
		CSPSet csp = new CSPSet();
		try{
			String filepat = "(.*/)(.*)\\.(.*)";
			String directory = patternmatch(filename,filepat).group(1);
			String before_dot = patternmatch(filename,filepat).group(2);
			File file = new File(filename);
			Document doc = Jsoup.parse(file, "UTF-8");
			String html = doc.toString();
			// divide javascript from html file
			doc = divideScript(doc,before_dot,directory);
			// divide CSS from html file
			doc = divideStyle(doc,before_dot,directory);
			// divide CSS which contains id from html file
			doc = divide_styleid(doc,before_dot,directory);
			//doc = Jsoup.parse(html);
			html = divideHref(doc,html);
			doc = Jsoup.parse(html);
			html = divideEvent(doc,html,directory);
			doc = Jsoup.parse(html);
			doc = csp.setCSP(doc,csplevel,directory,new JSList());
			html = doc.toString();
			FileWriter fw = new FileWriter(file);
			System.out.println("\nlast result\n"+html+"\n");
			fw.write(html);
			fw.close();
			System.out.println("inline count = "+inlinecount);
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	/**
	 * 
	 * @param doc
	 * @param beforedot
	 * @param filepath
	 * @return
	 */
	public Document divideScript(Document doc, String beforedot,String filepath){
		Elements script = doc.getElementsByTag("script");
		String html = doc.toString();
		for(int i=0;i < script.size();i++){
			Element tmp = script.get(i);
			String pat="(.*)<script().*src=(.*)>(.*)";
			if(Pattern.compile(pat).matcher(tmp.toString()).find()){
				System.out.println("src");
			}else{
				inlinecount++;
				String mdHtml = divide_script_from_file(tmp.data(),beforedot,filepath);
				html = html.replaceFirst(Pattern.quote(tmp.toString()), mdHtml);
			}
		}
		return Jsoup.parse(html);
	}
	
	public Document divide_styleid(Document doc, String filename,String filepath){
		Elements divstyle = doc.getElementsByAttribute("style");
		String html = doc.toString();
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
			html = html.replaceFirst(Pattern.quote(styleline),mdhtml); 
			}
			
		doc = Jsoup.parse(html);
		}
		if(!styletext.equals("")){
			try{
				File file = new File(filepath+filename+"styleattr.css");
				FileWriter fw = new FileWriter(file);
				fw.write(styletext);
				fw.close();
			}catch(IOException e){
				System.out.println(e);
			}
			Elements header = doc.getElementsByTag("head");
			header.append("<link href=\""+filename+"styleattr.css\" rel=\"stylesheet\" type=\"text/css\">");
		}
		
		return doc;
	}
	
	public Document divideStyle(Document doc,String filename,String filepath){
		Elements style = doc.getElementsByTag("style");
		String html = doc.toString();
		for(int i=0; i< style.size();i++){
			Element tmp = style.get(i);
			if(!tmp.toString().contains("src=")){
				String mdhtml = divide_style_from_file(tmp.data(),filename,filepath);
				html = html.replaceFirst(Pattern.quote(tmp.toString()), mdhtml);
			}
		}
		return doc;
	}
	
	public String divideHref(Document doc,String html){
			Elements atag = doc.getElementsByTag("a");
			for(int i=0;i < atag.size();i++){
				Element tmp = atag.get(i);
				if(tmp.toString().contains("href=\"javascript")){
					String pat="(.*)\"javascript:(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),pat);
					if(m != null){
						if(m.group(2).contains("void(0)")){
							baJS = true;
							/* pattern example <a href="javascript:(javascript)void(0)" onclick="(javascript)">test</a>*/
							if(tmp.toString().contains("onclick")){
								String onclickpat = "(.*?)onclick=\"(.*?)\"(.*)";
								String script = m.group(2);
								Matcher onclickm = patternmatch(m.group(1)+"\"\""+m.group(3),onclickpat);
								html = html.replaceFirst(Pattern.quote(m.group()), onclickm.group(1)+"onclick=\""+onclickm.group(2)+";"+script+"\""+onclickm.group(3));
								inlinecount++;
							/*pattern <a href="javascript:(javascript)void(0)></a>*/
							}else{
								inlinecount++;
								html = html.replaceFirst(Pattern.quote(m.group()), m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
							}
						}else{
							//System.out.println(m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
							inlinecount++;
							html = html.replaceFirst(Pattern.quote(m.group()), m.group(1)+"\"\" onclick=\""+m.group(2)+"\""+m.group(3));
						}
					}
						
				}
			}
		return html;
	}
	
	public String divideEvent(Document doc,String html,String filepath){
		
		File file = new File(filepath+"event.js");
		int flag=0;
		try{
			FileWriter fw = new FileWriter(file);	
			if(baJS){
				fw.write("function stopDefAction(evt){ evt.preventDefault(); }\n");
			}
			
			for(String key : eventhandler.keySet()){
				Elements evHand = doc.getElementsByAttribute(key);
				for(int i=0;i<evHand.size();i++){
					String evid="";
					Element tmp = evHand.get(i);
					Boolean preventEvent = false;
					String keyPattern = "(.*)"+key+"=\"(.*?)\"(.*)";
					String idPattern = "(.*)id=\"(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),keyPattern);
					String script = m.group(2);
					Matcher mid = patternmatch(tmp.toString(),idPattern);
					/**
					 * If the extracted JavaScript contains void(0), Mocha remove void(0). 
					 */
					if(m.group(2).contains("void(0)")){
						inlinecount++;
						preventEvent = true;
						String rmvoid = tmp.toString().replace("void(0)", "");
						html = html.replaceFirst(Pattern.quote(tmp.toString()), rmvoid);
						m= patternmatch(rmvoid,keyPattern);
						System.out.println(m.group());
						script = m.group(2);
					}
					/**
					 * If the tag doesn't have an id, Mocha adds an id to the tag.
					 */
					if(mid == null){
						inlinecount++;
						evid = String.valueOf(eventid);
						html = html.replaceFirst(Pattern.quote(m.group()), m.group(1)+"id=\""+eventid+"\""+m.group(3));
						eventid++;
					}else{
						evid = mid.group(2);
						html = html.replaceFirst(Pattern.quote(m.group()), m.group(1)+m.group(3));
					}
					String template="";
					if(preventEvent){
						inlinecount++;
						template = preventvoid(evid,script,key); 
					}else{
						template = tempevent(evid,script,key);
					}
					//System.out.println(template);
					fw.write(template);
					flag = 1;
				}
			}
			
			/**
			 * modify event handler type of onload
			 */
			for(String key : eventbody.keySet()){
				Elements evbody = doc.getElementsByAttribute(key);
				for(int i=0;i< evbody.size();i++){
					Element tmp = evbody.get(i);
					String pat = "(.*)"+key+"=\"(.*?)\"(.*)";
					Matcher m = patternmatch(tmp.toString(),pat);
					String script = m.group(2);
					String template = loadevent(script,key);
					html = html.replaceFirst(Pattern.quote(m.group()), m.group(1)+m.group(3));
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
	 * 
	 * @param str 		String which I want to analyze
	 * @param pattern	analyze pattern
	 * @return			return Matcher group
	 */
	public static Matcher patternmatch(String str,String pattern){
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		
		if(m.find()){
			return m;
		}
		return null;
	}
	
	/**
	 * divide file from html(now only Javascript)
	 * @param text
	 * @param beforedot 	file name before dot. For example, if file name is test.js, befordot is test.
	 * @param filepath		file path 
	 * */
	public String divide_script_from_file(String text,String beforedot,String filepath){
		//System.out.println(text);
		/*write file*/
		String mdhtml="";
		try{
			File file = new File(filepath+beforedot+jsfilename+".js");
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			
			fw.close();
			mdhtml = "<script src=\""+beforedot+jsfilename+".js\"></script>\n";
			jsfilename++;
		}catch(IOException e){
			System.out.println(e);
		}
		/*instead of divide, write like <script src="~~" ></script> text*/
		return mdhtml;
	}
	
	public String divide_style_from_file(String text,String beforedot,String path){
		String mdhtml = "";
		try{
			File file = new File(path+beforedot+stylefilename+".css");
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			fw.close();
			mdhtml = "<link href=\""+beforedot+stylefilename+".css\" rel=\"stylesheet\" type=\"text/css\">";
			stylefilename++;
		}catch(IOException e){
			System.out.println(e);
		}
		return mdhtml;
	}
	
	
	public String textStyleAttr(String id,String style){
		return "#"+id+" { " +style+" }";
	}
	
	
	public String tempevent(String id,String script,String key){
		String templete = "var ev"+eventname+" = function(){ \n"
				+ " var div = document.getElementById(\""+id+"\");\n"
				+ "	var popup = function (){ \n"
				+ script +";\n"
				+ " };\n"
				+ " div.addEventListener(\""+eventhandler.get(key)+"\",popup,false);\n"
				+ " };\n"
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
					html_file.add(files[i].toString());
				}else if(files[i].getName().contains(".js")){
					js_file.add(files[i].toString());
				}else if(files[i].getName().contains(".ejs")){
					ejs_file.add(files[i].toString());
				}
			}else if(files[i].isDirectory()){
				getfilename(files[i].toString());
			}
		}
	}
	
	public String loadevent(String script,String key){
		String templete ="var ev"+eventname+" = function(){ \n"+script+";\n};\n"
				+ "window.addEventListener(\"load\",ev"+eventname+",false);\n";
		eventname++;
		return templete;
	}
	
	/**
	 * 
	 * @param directoryname which you want to copying directory.
	 * @throws IOException
	 */
	public static void copyfile(String directoryname) throws IOException{
		String[] command = {"/bin/sh", "-c","cp -r ./"+directoryname+"/* ./csp"};
		Runtime.getRuntime().exec("mkdir csp");
		Runtime.getRuntime().exec(command);
		System.out.println("cp command");
	}
	
	
}

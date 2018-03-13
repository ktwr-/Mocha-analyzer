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

public class MochaMain{
	
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
	ArrayList<String> htmlFile = new ArrayList<String>();
	ArrayList<String> jsFile = new ArrayList<String>();
	HashMap<String,ArrayList<String>> jsFileCheker = new HashMap<String,ArrayList<String>>(); // HashMap checks a JavanalyzeScriptcript file is modified. ArrayList is file which is divide from JavanalyzeScriptcript file.
	ArrayList<String> ejsFile = new ArrayList<String>();
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
	FileList fileList = new FileList();
	
	public static void main(String args[]) throws IOException, InterruptedException{
		MochaMain Mocha = new MochaMain();
		if(Mocha.csplevel == 1){
			analyzeScript analyzeScript = new analyzeScript();
			/* copy file from www/ directory to csp/ directory
			 * main function applies CSP to files in csp directory.
			 * */
			copyfile("www");
			System.out.println("finish");
			Thread.sleep(1000);
			Mocha.fileList.getfilename("./csp");
			
		
			for(int i=0;i<Mocha.fileList.HTMLFileList.size();i++){
				String html;
				Mocha.htmlAnalyze(html = Mocha.fileList.HTMLFileList.get(i));
				for(int j = 0; j < Mocha.fileList.HTMLJSList.get(html).jsFile.size(); j++){
					String jsfile= Mocha.fileList.HTMLJSList.get(html).jsFile.get(i);
					if(!Mocha.fileList.JSModifyCheck.get(jsfile)){
						analyzeScript.analyzeScript(jsfile);
						Mocha.fileList.JSModifyCheck.put(jsfile, true);
					}
				}
			}
			for(int i=0;i<Mocha.fileList.JSFileList.size();i++){
				String jsfile = Mocha.fileList.JSFileList.get(i);
				if(!Mocha.fileList.JSModifyCheck.get(jsfile) && !jsfile.contains("min.js") && !jsfile.contains("leaflet") && !jsfile.contains("data.js") && !jsfile.contains("jquery")){
					//System.out.println("file:"+Mocha.fileList.JSFileList.get(i));
					analyzeScript.analyzeScript(Mocha.fileList.JSFileList.get(i));
				}
			}
			
		}else if(Mocha.csplevel == 2){
			if(Mocha.noncesource == true){
				for(int i = 0;i<Mocha.ejsFile.size();i++){
					NonceSource ns = new NonceSource();
				}
			}else{
				System.out.println("test");
				copyfile("test");
				System.out.println("finish");
				Thread.sleep(10000);
				Mocha.getFileName("./csp");
				//source hash script
				Document doc;
				for(int i = 0;i<Mocha.htmlFile.size();i++){
					jscript js = new jscript();
					CSPSet csp = new CSPSet();
					SourceHash sh = new SourceHash();
					String filename= Mocha.htmlFile.get(i);
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
					doc = csp.setCSP(doc,Mocha.csplevel,directory,new JSList());
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
	public void htmlAnalyze(String filename) throws IOException{
		CSPSet csp = new CSPSet();
		
		File file = new File(filename);
		String filepat = "(.*/)(.*)\\.(.*)";
		String directory = file.getParent()+"/";
		String before_dot = patternmatch(filename,filepat).group(2);
		Document doc = Jsoup.parse(file, "UTF-8");
		fileList.HTMLJSList.get(filename).findJSFile(file);
		// divide javascript from html file
		doc = divideScript(doc,before_dot,directory);
		// divide CSS from html file
		doc = divideStyle(doc,before_dot,directory);
		// divide CSS which contains id from html file
		doc = divide_styleid(doc,before_dot,directory);
		//doc = Jsoup.parse(html);
		doc = divideHref(doc);
		doc = divideEvent(doc,directory);
		doc = csp.setCSP(doc,csplevel,directory,fileList.HTMLJSList.get(filename));
		String html = doc.toString();
		FileWriter fw = new FileWriter(file);
		System.out.println("\nlast result\n"+html+"\n");
		fw.write(html);
		fw.close();
		System.out.println("inline count = "+inlinecount);
		
	}
	
	public void htmlAnalyzeCSP2(String filename) throws IOException {
		CSPSet csp = new CSPSet();
		
		Document doc;
		SourceHash sh = new SourceHash();
		
		String filepat = "(.*/)(.*)\\.(.*)";
		String directory = patternmatch(filename,filepat).group(1);
		String beforedot = patternmatch(filename,filepat).group(2);
		doc = sh.add_source_hash(filename);
		doc = divideEvent(doc,directory);
		doc = divide_styleid(doc,beforedot,directory);
		csp.setHashScript(sh.scriptHashlist);
		csp.setHashStyle(sh.styleHashlist);
		doc = csp.setCSP(doc,2,directory,new JSList());
		System.out.println("\nlast result\n");
		System.out.println(doc.toString());
		File file = new File(filename);
		FileWriter fw = new FileWriter(file);
		fw.write(doc.toString());
		fw.close();
		
		
		
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
	
	public Document divideHref(Document doc){
			String html = doc.toString();
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
		return Jsoup.parse(html);
	}
	
	public Document divideEvent(Document doc,String filepath){
		String html = doc.toString();
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
					 * If the extracted JavanalyzeScriptcript contains void(0), Mocha remove void(0). 
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
		return doc;
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
	
	public void getFileName(String directory) throws IOException{
		File file = new File(directory);
		File files[] = file.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isFile()){
				if(files[i].getName().contains(".html")){
					htmlFile.add(files[i].toString());
				}else if(files[i].getName().contains(".js")){
					jsFile.add(files[i].toString());
				}else if(files[i].getName().contains(".ejs")){
					ejsFile.add(files[i].toString());
				}
			}else if(files[i].isDirectory()){
				getFileName(files[i].toString());
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

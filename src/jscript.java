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

import org.mozilla.javascript.*;

import java.util.regex.Matcher;



public class jscript {
	
	static ArrayList<String> removetag = new ArrayList<String>();
	static ArrayList<String> fileextension = new ArrayList<String>();
	static HashMap<String,String> eventhandler = new HashMap<String,String>();
	static HashMap<String,String> eventbody = new HashMap<String,String>();
	static ArrayList<String> htmlfile = new ArrayList<String>();
	static ArrayList<String> jsfile = new ArrayList<String>();
	static ArrayList<String> header;
	static ArrayList<String> body;
	static char jsfilename = 'a';
	static char stylefilename = 'a';
	static char eventid = 'a';
	static char eventname= 'a';
	
	public static void main(String args[]) throws IOException{
		jscript js = new jscript();
		
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
		
		for(int i=0;i<htmlfile.size();i++){
			System.out.println(htmlfile.get(i));
		}
		for(int i=0;i<jsfile.size();i++){
			System.out.println(jsfile.get(i));
		}
		
		js.inline_init();
		//System.out.println(args[0]);
		//js.eventhandler();
		//js.analyzehtml("./test/test.html");
		js.htmlanalyze("./csp/test.html");
		System.out.println("sample");
		js.jsanalyze("./test/chtest/a.js");
		//Document document = Jsoup.parse(new File("./test/test.html"),"UTF-8");
		//System.out.println(document.getElementsByAttribute("unload"));
	}
	
	public void inline_init(){
		removetag.add("script");
		removetag.add("style");
		fileextension.add(".js");
		fileextension.add(".css");
		//correspondence table of event handler
		eventhandler.put("onclick", "click");
		eventhandler.put("ondblclick", "dblclick");
		eventbody.put("onload", "load");
	}
	
	
	
	
	public void eventhandler(){
		File file = new File("./test/test.html");
		try {
			FileReader filereader = new FileReader(file);
			BufferedReader bf = new BufferedReader(filereader);
			//find <> tag and extract content in <> tag 
			String str,tmp="";
			while((str = bf.readLine()) != null){
				
				if(str.contains("<")){
					String pat = "(.*?)(<.*?>)(.*)";
					//String pat="(.*<.*>.*){0,}";
					while(!str.matches(pat)){
						str += bf.readLine();
					}
					System.out.println(str);
					// str contains ">" 
					Matcher m = patternmatch(str,pat);
					tmp = m.group(2);
				}
				
				//if tmp contains event handler which type is onclick 
				for(Iterator<String> it = eventhandler.keySet().iterator(); it.hasNext();){
					String key = it.next();
					if(tmp.contains(key)){
						String pat = "(.*)"+key+"=\"(.*?)\"(.*)";
						String id = "(.*)id=\"(.+?)\"(.*)";
						if(tmp.matches(pat)){
							Matcher m = patternmatch(tmp,pat);
							String script = m.group(2);
							String wscr="";
							if(tmp.matches(id)){
								Matcher mid = patternmatch(tmp, id);
								String sid = mid.group(2);
								wscr = tempevent(sid,script,key);
							}else{
								wscr = tempevent(String.valueOf(eventid),script,key);
								eventid++;
							}
							System.out.println(wscr);
						}
						
						
					}
				}
				//if tmp contains event handler which type is onload
				for(Iterator<String> it = eventbody.keySet().iterator(); it.hasNext();){
					String key = it.next();
					if(tmp.contains("body") && tmp.contains(eventbody.get(key))){
						String pat="(.*"+key+"=\")(.*)(\".*)";
						String wscr ="";
						if(tmp.contains(key)){
							Matcher m = patternmatch(tmp,pat);
							String script = m.group(2);
							wscr = loadevent(script,key);
						}
						System.out.println(wscr);
					}
				}
			}
			bf.close();
			
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**/
	
	public void htmlanalyze(String filename){
		try{
			File file = new File(filename);
			Document doc = Jsoup.parse(file, "UTF-8");
			String html = doc.toString();
			html = dividescript(doc,html);
			html = dividestyle(doc,html);
			System.out.println(html);
			html = divideevent(doc,html);
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
	
	public String dividescript(Document doc, String html){
		Elements script = doc.getElementsByTag("script");
		for(int i=0;i < script.size();i++){
			Element tmp = script.get(i);
			System.out.println(tmp.toString()+"\n");
			if(tmp.toString().contains("src=")){
				String pat="(.*)<script().*src=(.*)>(.*)";
			}else{
				String mdhtml = dividescript(tmp.data());
				html = html.replaceAll(Pattern.quote(tmp.toString()), mdhtml);
			}
		}
		return html;
	}
	
	public String dividestyle(Document doc,String html){
		Elements style = doc.getElementsByTag("style");
		for(int i=0; i< style.size();i++){
			Element tmp = style.get(i);
			System.out.println(tmp.toString());
			if(!tmp.toString().contains("src=")){
				String mdhtml = dividestyle(tmp.data());
				html = html.replaceAll(Pattern.quote(tmp.toString()), mdhtml);
			}
		}
		return html;
	}
	
	public String divideevent(Document doc,String html){
		for(String key : eventhandler.keySet()){
			Elements evhand = doc.getElementsByAttribute(key);
			for(int i=0;i<evhand.size();i++){
				Element tmp = evhand.get(i);
				System.out.println(tmp.toString());
				String pat = "(.*)"+key+"=\"(.*?)\"(.*)";
				Matcher m = patternmatch(tmp.toString(),pat);
				String script = m.group(2);
				String template = tempevent(String.valueOf(eventid),script,key);
				System.out.println(template);
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
				System.out.println(template);
			}
		}
		return html;
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
	public static String dividescript(String text){
		//System.out.println(text);
		/*write file*/
		String mdhtml="";
		try{
			File file = new File("./csp/"+jsfilename+".js");
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
	public static String tempevent(String id,String script,String key){
		String templete = "var "+eventname+" = function() {\n"
				+ " var div = document.getElementById(\""+id+"\");\n"
				+ "	var popup = function () { \n"
				+ script +"; };\n div.addEventListener("+eventhandler.get(key)+",popup,false); };\n"
				+ "window.addEventListener(\"load\","+eventname+",false);\n";
		eventname++;
		
		return templete;
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
				}
			}else if(files[i].isDirectory()){
				getfilename(files[i].toString());
			}
		}
	}
	
	public static String loadevent(String script,String key){
		String templete ="var "+eventname+" = function(){ \n"+script+";\n};\n"
				+ "window.addEventListener(\"load\","+eventname+",false);\n";
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

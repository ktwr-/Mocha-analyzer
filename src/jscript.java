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
import java.util.regex.Matcher;

public class jscript {
	
	static ArrayList<String> removetag = new ArrayList<String>();
	static ArrayList<String> fileextension = new ArrayList<String>();
	static HashMap<String,String> eventhandler = new HashMap<String,String>();
	static HashMap<String,String> eventbody = new HashMap<String,String>();
	static ArrayList<String> header;
	static ArrayList<String> body;
	static char jsfilename = 'a';
	static char stylefilename = 'a';
	static char eventid = 'a';
	static char eventname= 'a';
	
	public static void main(String args[]){
		jscript js = new jscript();
		try {
			copyfile();
			System.out.println("finish");
			Thread.sleep(1000);
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e){
			System.out.println(e);
		}
		
		js.inline_init();
		//System.out.println(args[0]);
		js.eventhandler();
		js.analyzehtml("./test/test.html");

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
					String pat = "(.*)<(.*)>(.*)";
					while(!str.contains(">")){
						str += bf.readLine();
					}
					// str contains ">" 
					Matcher m = patternmatch(str,pat);
					tmp = m.group(2);
					
				}
				//if tmp contains event handler which type is onclick 
				for(Iterator<String> it = eventhandler.keySet().iterator(); it.hasNext();){
					String key = it.next();
					if(tmp.contains(eventhandler.get(key))){
						String pat = "(.*)"+eventhandler.get(key)+"\"(.*)\"(.*)";
						String id = "(.*)id=\"(.*)\"";
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
						String pat="(.*)"+eventbody.get(key)+"\"(.*)\"(.*)";
						String wscr ="";
						if(tmp.matches(pat)){
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
	
	
	/*catch program css, script or anything else which CSP applying*/
	public void analyzehtml(String filename){
		header = new ArrayList<String>();
		body = new ArrayList<String>();
		try{
			File file = new File(filename);
			FileReader filereader = new FileReader(file);
			BufferedReader bf = new BufferedReader(filereader);
			File wfile = new File("./csp/test.html");
			FileWriter fw = new FileWriter(wfile);
			/*flag is which part is the match word,script,eval or style*/
			int i,flag=0;
			String str,pat1,pat2="",pat3="";
			String div="";
			while((str = bf.readLine()) != null){
				/*find tag*/
				for(i =0;i < removetag.size();i++){
					div ="";
					/*find script,eval or style tag*/
					pat1 = "(.*)(<"+removetag.get(i)+".*)";
					pat2 = "(.*)(</"+removetag.get(i)+">)(.*)";
					pat3 = "(.*)(<"+removetag.get(i)+".*>)(.*)(</"+removetag.get(i)+">)(.*)";
					if(str.matches(pat1)){
						Matcher m = patternmatch(str,pat1);
						//div=m.group(2)+"\n";
						break;
					}
				}
			if(str.matches("</head>")){
				flag = 1;
			}
				/*write htmlfile for script*/
				switch(i){
				case 0:
					if(!str.contains("src")){
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(3);
							String mdhtml = dividescript(div,i);
							if(flag == 1){
								body.add(mdhtml);
							}else{
								header.add(mdhtml);
							}
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
						
						Matcher m = patternmatch(str,pat2);
						div += m.group(1);//+m.group(2);
						String mdhtml = dividescript(div,i);
							if(flag == 1){
								body.add(mdhtml);
							}else{
								header.add(mdhtml);
							}
						}
					}else{
						/*if script tag contains src,this script don't need divide file*/
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(2)+m.group(3)+m.group(4);
							if(flag == 1){
								body.add(div);
							}else{
								header.add(div);
							}
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
							Matcher m = patternmatch(str,pat2);
							div += m.group(1);//+m.group(2);
							if(flag == 1){
								body.add(div);
							}else{
								header.add(div);
							}
						}
					}
					break;
				//write css file
				case 1:
					if(str.matches(pat3)){
						Matcher m = patternmatch(str,pat3);
						div = m.group(3);
						if(flag == 1){
							body.add(div);
						}else{
							header.add(div);
						}
					}else{
						while(!(str = bf.readLine()).matches(pat2)){
							div += str+"\n";
						}
						Matcher m = patternmatch(str,pat2);
						div += m.group(1)+m.group(2);
						String mdhtml = dividestyle(div);
						if(flag == 1){
							body.add(mdhtml);
						}else{
							header.add(mdhtml);
						}
					}
					break;
				default:
					if(!str.matches("</head>")){
						if(flag == 1){
							body.add(str);
						}else{
							header.add(str);
						}
						
					}
					break;
				}
			}
			writeheader(fw);
			writebody(fw);
			filereader.close();
			fw.close();
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		
		
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
	public static String dividescript(String text,int type){
		//System.out.println(text);
		/*write file*/
		String mdhtml="";
		try{
			File file = new File("./csp/"+jsfilename+fileextension.get(type));
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			
			fw.close();
			mdhtml = "<"+removetag.get(type)+" src=\""+jsfilename+fileextension.get(type)+"\"></"+removetag.get(type)+">\n";
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
		String templete = "var "+id+" = function() {"
				+ " var div = document.getElementById("+id+");"
				+ "	var popup = function () { "
				+ script +" };div.addEventListener("+eventhandler.get(key)+",popup,false); };"
				+ "window.addEventListener(\"load\","+eventname+",false);";
		eventname++;
		
		return templete;
	}
	
	public static String loadevent(String script,String key){
		String templete ="var "+eventname+" = function(){ "+script+"};"
				+ "window.addEventListener(\"load\","+eventname+",false);";
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

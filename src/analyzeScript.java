import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analyze.js.*;


public class analyzeScript {
	
	public static char jsname ='a';
	public static char funcname ='a';
	public static char varname = 'a';
	public static char idname = 'a';
	public static char eventname = 'a';
	public static HashMap<String,String> eventh;
	
	public static String LINE_SEPARATOR_PATTERN = "\r\n[\u2028\u2029\u0085]";
	
	analyzeScript(){
		init();
	}
	
	public static void main(String args[]){
		analyzeScript as = new analyzeScript();
		
		String source = as.fileReader("./csp/cs.js");
		//System.out.println(source);
		String mdjs = as.createScript(source);
		if(!mdjs.equals(source)){
			as.overWrite(mdjs,"./csp/cs.js");
		}
		String set = as.fileReader("./csp/set.js");
		//System.out.println(set);
		String mdset = as.setIntTime(set);
		if(!mdset.equals(set)){
			as.overWrite(mdset,"./csp/set.js");
		}
		as.check_docwrite("./csp/cs.js");
		
		as.check_event_handler("./test/test.js");
		
	}
	
	public void init(){
		eventh = new HashMap<String,String>();
		eventh.put("onclick", "click");
	}
	
	public void analyzeScript(String filename){
		String source = fileReader(filename);
		String original = new String(source);
		System.out.println("start createScript");
		String mdjs = createScript(source);
		if(!mdjs.equals(source)){
			overWrite(mdjs,filename);
			source = mdjs;
		}
		System.out.println("finish createScript");
		String mdset = setIntTime(source);
		if(!mdset.equals(source)){
			overWrite(mdset,filename);
			source = mdset;
		}
		String mddocwrite = check_docwrite(filename);
		if(!mddocwrite.equals(source)){
			overWrite(mddocwrite,filename);
			source = mddocwrite;
		}
		String mdevhandle = check_event_handler(filename);
		if(!mdevhandle.equals(source)){
			overWrite(mdevhandle,filename);
			source = mdevhandle;
		}
		Modify_eval me = new Modify_eval();
		String mdev = me.extrace_evaltext(source,filename);
		if(!mdev.equals(source)){
			overWrite(mdev,filename);
			source = mdev;
		}
		/*if(!source.equals(original)){
			System.out.println("change script\n\n");
			overWrite(source,filename);
		}*/
		
		
	}
	
	public String check_docwrite(String filename){
		ArrayList<String> method = new ArrayList<String>(Search_method.search_method("document.write", filename));
		ArrayList<String> text = new ArrayList<String>(Search_method.find_text("document.write", fileReader(filename)));
		String file = fileReader(filename);
		//System.out.println("check");
		//System.out.println(file);
		String filepat =  "(.*/)(.*)\\.(.*)";
		String directory = patternmatch(filename,filepat).group(1);
		String beforedot = patternmatch(filename,filepat).group(2);
		for(int i=0;i<text.size();i++){
			System.out.println("method:"+method.get(i));
			System.out.println("text:"+text.get(i));
			String temp = textanalyze(text.get(i),directory,beforedot);
			if(temp.contains("event_handler_CSP_apply")){
				String[] split = temp.split("event_handler_CSP_apply");
				file = split[1]+"\n"+file;
				String js_write = split[1];
				temp = split[0];
			}
			System.out.println("aaa:"+temp);
			file = file.replaceAll(Pattern.quote(text.get(i)), temp);
			System.out.println("temp:\n"+ file);
		}
		//String pat = "([\\s\\S]*?)document\\.(write|writeln)\\(([\\s\\S]*)\\);[\\s\\S]*";
		//Matcher m = Pattern.compile(pat).matcher(source);
		//while(m.find()){
		//	System.out.println(m.group());
		//	System.out.println(m.group(3));
		//}
		//System.out.println("last:\n"+file);
		return file;
		
	}
	/**
	 * avoid inline script such as document.write('<div onclick="">aa</div>');
	 * 
	 * @param text document.write(text);
	 */
	public String textanalyze(String text,String directory,String beforedot){
		// if text has <script>
		if(text.contains("<script>")){
			String pat = "([\\s\\S]*)<script>([\\s\\S]*)</script>([\\s\\S]*)";
			Matcher m = Pattern.compile(pat).matcher(text);
			if(m.find()){
				System.out.println(m.group(2));
				
				String change_text = m.group(1)+"<script src=\""+directory+beforedot+jsname+".js\"></script>"+m.group(3);
				overWrite(m.group(2),directory+beforedot+jsname+".js");
				jsname++;
				System.out.println(text);
				System.out.println(change_text);
				return change_text;
			}
		}
		// if text has event handler
		for(String key : eventh.keySet()){
			if(text.contains(key)){
				String id="";
				int idflag=0;
				if(text.contains("id")){
					String patid = "(.*)id=[\"\']([\\s\\S]*?)[\"\']([\\s\\S]*)";
					Matcher m = Pattern.compile(patid).matcher(text);
					System.out.println(m.find());
					if(m.find()){
						id = m.group(2);
						idflag=1;
					}
					
				}else{
					id = String.valueOf(idname);
					idname++;
				}
				
				String patscrpt = "([\\s\\S]*?)"+key+"=[\"\']([\\s\\S]*?)[\"\']([\\s\\S]*)";
				Matcher mscript = Pattern.compile(patscrpt).matcher(text);
				String script="";
				if(mscript.find()){
					script=mscript.group(2);
					System.out.println(script);
				}
				String temp = tempevent(id,script,key);
				System.out.println(temp);
				System.out.println("\nmodify event handler");
				if(idflag == 0){
					// event handler don't have id
					String change_text = mscript.group(1)+"id=\""+id+"\""+mscript.group(3);
					System.out.println("change_text:"+change_text);
					System.out.println("text:"+temp);
					return change_text+"event_handler_CSP_apply"+temp;
					
				}else{
					// event handler have id
					String change_text = mscript.group(1)+mscript.group(3);
					System.out.println("change_text:"+change_text);
					System.out.println("text:"+temp);
					return change_text+"event_handler_CSP_apply"+temp;
					
				}
			}
		}
		return text;
		
	}
	/**
	 * check event handler such as in innerHTML or outerHTML
	 * 
	 * @param filename filename which you want to analyze 
	 */
	
	public String check_event_handler(String filename){
		String source = fileReader(filename);
		ArrayList<String> innerHTML = new ArrayList<String>(Search_method.find_text_equals("innerHTML", source));
		ArrayList<String> outerHTML = new ArrayList<String>(Search_method.find_text_equals("outerHTML", source));
		ArrayList<String> modify_innerHTML = new ArrayList<String>(check_event_handler_method("innerHTML",innerHTML));
		System.out.println(innerHTML.size());
		System.out.println(modify_innerHTML.size());
		if(innerHTML.size() == modify_innerHTML.size()){
		for(int i=0;i<innerHTML.size();i++){
			if(!innerHTML.get(i).equals(modify_innerHTML.get(i))){
				source = source.replaceAll(Pattern.quote(innerHTML.get(i)), modify_innerHTML.get(i));
			}
		}}
		ArrayList<String> modify_outerHTML = new ArrayList<String>(check_event_handler_method("innerHTML",innerHTML));
		check_event_handler_method("outerHTML",outerHTML);
		if(outerHTML.size() == modify_outerHTML.size()){
		for(int i=0;i<outerHTML.size();i++){
			if(!outerHTML.get(i).equals(modify_outerHTML.get(i))){
				source = source.replaceAll(Pattern.quote(outerHTML.get(i)), modify_outerHTML.get(i));
			}
		}}
		System.out.println("modify\n"+source);
		return source;
		
		
		
	}
	
	private ArrayList<String> check_event_handler_method(String methodname,ArrayList<String> tmp){
		ArrayList<String> ret_method = new ArrayList<String>();
		String pat = "(.*?)"+methodname+"([\\s\\S]*?)=(.*)\"(.*)\"(.*);";
		for(int i=0;i<tmp.size();i++){
			System.out.println(tmp.get(i));
			Matcher m = Pattern.compile(pat).matcher(tmp.get(i));
			if(m.find()){
				// if (inner|outer)HTML = " ~~~~ "; just text not variable
				System.out.println(m.group(4));
				String text = m.group(4);
				for(String key: eventh.keySet()){
					// if text such as " <div> ~~~ </div> " contains event handler
					if(text.contains(key)){
						//event handler
						System.out.println("event handler check in innerHTML or outerHTML");
						String id="";
						int idflag=0;
						if(text.contains("id")){
							String patid = "(.*)id=[\"\']([\\s\\S]*?)[\"\']([\\s\\S]*)";
							Matcher mid = Pattern.compile(patid).matcher(text);
							if(m.find()){
								id = m.group(2);
								idflag=1;
							}
					
						}else{
							id = String.valueOf(idname);
							idname++;
						}	
						String patscrpt = "([\\s\\S]*?)"+key+"=[\"\']([\\s\\S]*?)[\"\']([\\s\\S]*)";
						Matcher mscript = Pattern.compile(patscrpt).matcher(text);
						String script="";
						if(mscript.find()){
							script=mscript.group(2);
							System.out.println(script);
						}
						String temp = tempevent(id,script,key);
						//System.out.println(temp);
						System.out.println("\nmodify event handler");
						if(idflag == 0){
							// event handler don't have id
							String change_text = mscript.group(1)+"id=\""+id+"\""+mscript.group(3);
							System.out.println(change_text);
							System.out.println(text);
							
							System.out.println(tmp.get(i).replaceAll(Pattern.quote(text), change_text)+temp);
							ret_method.add(tmp.get(i).replaceAll(Pattern.quote(text), change_text)+"\n"+temp);
					
						}else{
							// event handler have id
							String change_text = mscript.group(1)+mscript.group(3);
							System.out.println(change_text);
							System.out.println(text);
							ret_method.add(tmp.get(i).replaceAll(Pattern.quote(text), change_text)+"\n"+temp);
							
					
						}
					}
				}
			}
			return ret_method;
				// if innerHTML takes variable as an argument
		}
		return ret_method;
		
	}
	
	
	public String fileReader(String filename){
		StringBuilder sb = new StringBuilder("");
		try{
			@SuppressWarnings("resource")
			BufferedReader bf = new BufferedReader(new FileReader(filename));
			String line;
			while((line = bf.readLine()) != null){
				sb.append(line + System.getProperty("line.separator"));
			}
		}catch(Exception e){
			System.err.println("No such file or directory");
			System.exit(0);
		}
		return sb.toString();
	}
	
	public String setIntTime(String source){
		String temp=source;
		if(source.contains("setTimeout") || source.contains("setInterval")){
			ArrayList<String> tmp = new ArrayList<String>();
			String pat1 = "setTimeout";
			String pat2 = "setInterval";
			tmp.add(pat1);
			tmp.add(pat2);
			for(int i=0;i<tmp.size();i++){
			String pat = "(.*)"+tmp.get(i)+"\\((.*?),(.*?)\\);(.*)";
			Matcher m = patternmatch(source,pat);
			if(m != null){
				//while(m1.find()){
					String text ="(.*?)\"(.*?)\"(.*?)";
					String func = "(.*?)\"(.*?)\"(.*?)";
					if(m.group(2).matches(text)){
						System.out.println("test");
						Matcher mtext = patternmatch(source,text);
						String template = "var var"+varname+" = function func"+funcname+"(){"+mtext.group(2)+"; }";
						temp = temp.replaceAll(Pattern.quote(m.group()), m.group(1)+tmp.get(i)+"\\(var"+varname+" ,"+m.group(3)+"\\);"+m.group(4));
						System.out.println(template+"\n"+temp);
						temp = template+"\n"+temp;
						funcname++;
						varname++;
					}else if(m.group(2).matches(func)){
						Matcher mfunc = patternmatch(source,func);
						String template = "var var"+varname+" = "+mfunc.group(2)+";";
						temp = temp.replaceAll(Pattern.quote(m.group()), m.group(1)+tmp.get(i)+"\\(var"+varname+" ,"+m.group(3)+"\\);"+m.group(4));
						System.out.println(template+"\n"+temp);
						temp = template+"\n"+temp;
						varname++;
					}
					//}
				}
			}
		}
			
		return temp;
	}
	/**
	 * var t = document.createElement(script);
	 * t.src or t.text
	 * @param source
	 * @return 
	 */
	public String createScript(String source){
		String temp=source;
		Matcher var;
		String pat = "(.*?)var (.*?)= document.createElement\\((.*?)[\'\"](.*?)script(.*?)[\'\"]\\)(.*?);(.*)";
		if((var = patternmatch(source,pat)) != null){
			String src = "(.*)"+var.group(2).trim()+"\\.src(.*?)=(.*?)\"(.*?)\"(.*)";
			String text = "(.*)"+var.group(2).trim()+"\\.text(.*?)=(.*?)\"(.*?)\"(.*)";
			Matcher m = patternmatch(source,src);
			/*if contains src=*/
			if(m != null){
				System.out.println(m.group(4));
				if(m.group(4).contains("http")){
					// use wget and 
					String[] command = {"wget","-P","./csp/",m.group(4)};
					String filename;
					try {
						Runtime.getRuntime().exec(command);
					} catch (IOException e) {
						System.out.println(e);
					}
					filename = patternmatch(m.group(4),"(.*)/(.*)").group(2);
					String replace = source.replaceAll(Pattern.quote(m.group()), var.group(2).trim()+"\\.src = \""+filename+"\";");
					temp = replace;
				}
			}
			/*if contains text=*/
			m = patternmatch(source,text);
			if(m != null){
				String script = m.group(4);
				/* random character setting at this line*/
				String insert = "var var"+varname+" = "+script+";";
				String replace = source.replaceAll(Pattern.quote(m.group()), var.group(2).trim()+"\\.text = \"var"+varname+"\";");
				temp = insert+"\n"+replace;
				varname++;
				
			}
		}
		
		return temp;
	};
	
	public static String tempevent(String id,String script,String key){
		String templete = "var csp_ev"+eventname+" = function() {\n"
				+ " var csp_div = document.getElementById(\""+id+"\");\n"
				+ "	var csp_func = function () { \n"
				+ script +"; };\n csp_div.addEventListener(\""+eventh.get(key)+"\",csp_func,false); };\n"
				+ "window.addEventListener(\"load\",csp_ev"+eventname+",false);\n";
		eventname++;
		
		return templete;
	}
	
	public void overWrite(String text,String filename){
		File file = new File(filename);
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(text);
			fw.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	private Matcher patternmatch(String str,String pattern){
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(str);
		
		if(m.find()){
			return m;
		}
		return null;
	}
}
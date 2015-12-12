import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.AstRoot;

public class analyzeScript {
	
	public static char jsname ='a';
	public static char funcname ='a';
	public static char varname = 'a';
	
	public static void main(String args[]){
		analyzeScript as = new analyzeScript();
		String source = as.fileReader("./csp/cs.js");
		System.out.println(source);
		String mdjs = as.createScript(source);
		if(!mdjs.equals(source)){
			as.overWrite(mdjs,"./csp/cs.js");
		}
		String set = as.fileReader("./csp/set.js");
		System.out.println(set);
		String mdset = as.setIntTime(set);
		if(!mdset.equals(set)){
			as.overWrite(mdset,"./csp/set.js");
		}
		
	}
	
	public void analyzeScript(String filename){
		String source = fileReader(filename);
		System.out.println("start createScript");
		String mdjs = createScript(source);
		if(!mdjs.equals(source)){
			overWrite(mdjs,filename);
		}
		System.out.println("finish createScript");
		String mdset = setIntTime(source);
		if(!mdset.equals(source)){
			overWrite(mdset,filename);
		}
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
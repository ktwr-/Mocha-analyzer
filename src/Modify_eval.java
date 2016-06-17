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

public class Modify_eval {
	// Eval arguments Pattern
	
	static final String JSONP_pat = "(^[A-Za-z0-9_\\$\\.\t\\[\\]\"\']*)=[\t\r\n]*(.*);*$";
	
	static final String Empty_pat = "^$";
	
	static final String Library_pat = "function *[A-Za-z0-9_\\$]* *\\("; //string must be greater 512bytes
	static int Library_flag = 0;
	
	static final String Typeof_pat1 = "^typeof*\\(? *([A-Za-z0-9_\\$\\.\t\\[\\]\"\']*)\\)?$";
	static final String Typeof_pat2 = "^typeof*\\(? *([A-Za-z0-9_\\$\\.\t\\[\\]\"\']*)\\)? *[!=<>]+";
	static final String Typeof_pat3 = "^typeof*\\(? *([A-Za-z0-9_\\$\\.\t\\[\\]\"\']*)\\)? *[!=<>]+[^\\)]*\\)[^\\}]*\\}? *;? *$";
	
	static final String Read_pat1 = "^[A-Za-z0-9_\\$]*$";
	static final String Read_pat2 = "^[A-Za-z0-9_\\$\\.\\[\\]\"\']*$";
	
	static final String Call_pat = "^[A-Za-z0-9_\\$\\.]*\\([A-Za-z0-9_\\$\\.\\[\\]\"\']*;?[\t\r\n]*\\);?$";
	
	static final String Assign_pat1 ="^[A-Za-z0-9_\\$\\.\\[\\]\"\']* *= *([A-Za-z0-9_\\.\\[\\]\"\']*);?[\t\r\n]*$";
	static final String Assign_pat2 ="^var ([A-Za-z0-9_\\$]*) *= *([A-Za-z0-9_\\$\\.\\[\\]\"\']*)?;?$";
	
	static final String Try_pat = "^try *\\{[^\\}]*\\} *catch *\\([^\\)]*\\) *\\{[^\\}]*\\} *;?$";
	
	Modify_eval(){
		
	}
	
	public static void main(String[] args){
		String text = "{\"hoge\":\"piyoa\"}";
		Modify_eval me = new Modify_eval();
		me.extrace_evaltext(modify_ajax.fileReader("./www/html10n.js/l10n.js"), "./www/html10n.js/l10n.js");
	}
	
	public String extrace_evaltext(String source,String filename){
		ArrayList<String> evallist = new ArrayList<String>(Search_method.search_method("eval", filename));
		ArrayList<String> evaltext = new ArrayList<String>(Search_method.find_text("eval", source));
		if(evallist.size() == evaltext.size()){
		for(int i=0;i<evallist.size();i++){
			String temp = mod_eval(evaltext.get(i));
			source = source.replaceAll(Pattern.quote(evallist.get(i)), temp);
		}
		}
		return source;
	}
	/**
	 * 
	 * @param str eval(str);
	 * @return modify str;
	 */
	public String mod_eval(String str){
		
		if(judge_JSON(str)){
			return mod_JSON(str);
		}else if(Pattern.compile(JSONP_pat).matcher(str).find()){
			System.out.println("test");
			Matcher m = Pattern.compile(JSONP_pat).matcher(str);
			if(m.find()){
				System.out.println(m.group(2));
				if(m.group(2).equals("")){
					return mod_else(str);
				}else{
					return mod_JSONP(m.group(1),m.group(2));
				}
			}
		// apply empty
		}else if(Pattern.compile(Empty_pat).matcher(str).find()){
			return mod_Empty();
		// apply library
		}else if(Pattern.compile(Library_pat).matcher(str).find()){
			Library_flag = 1;
			return mod_else(str);
			
		// apply assign
		}else if(Pattern.compile(Assign_pat1).matcher(str).find()){
			Matcher m = Pattern.compile(Assign_pat1).matcher(str);
			m.find();
			return mod_assign(m.group(1),"");
				
		}else if(Pattern.compile(Assign_pat2).matcher(str).find()){
			Matcher m = Pattern.compile(Assign_pat1).matcher(str);
			m.find();
			return mod_assign(m.group(1),m.group(2));
			
		}else if(Pattern.compile(Typeof_pat1).matcher(str).find()){
			Matcher m = Pattern.compile(Typeof_pat1).matcher(str);
			m.find();
			return mod_typeof(m.group(1));
		}else if(Pattern.compile(Typeof_pat2).matcher(str).find()){
			Matcher m = Pattern.compile(Typeof_pat2).matcher(str);
			m.find();
			return mod_typeof(m.group(1));
		}else if(Pattern.compile(Typeof_pat3).matcher(str).find()){
			Matcher m = Pattern.compile(Typeof_pat3).matcher(str);
			m.find();
			return mod_typeof(m.group(1));
		}else if(Pattern.compile(Read_pat1).matcher(str).find()){
			
			return str;
		}else if(Pattern.compile(Read_pat2).matcher(str).find()){
			
		}else if(Pattern.compile(Try_pat).matcher(str).find()){
			return mod_else(str);
		}
		return mod_else(str);
	}
	private Boolean judge_JSON(String text){
		String temp = text.replaceAll("\\\\(?:[\"\\\\/bfnrt]|u[0-9a-fA-F]{4})", "@");
		//System.out.println("replace @:"+temp);
		temp = temp.replaceAll("\"[^\"\\\n\r]*\"|true|false|null|-?\\d+(?:\\.\\d*)?(?:[eE][+\\-]?\\d+)?", "]");
		//System.out.println("replace ]:"+temp);
		temp = temp.replaceAll("(?:^|:|,)(?:\\s*\\[)+", "");
		//System.out.println("remove space:"+temp);

		return Pattern.compile("^[\\],:{}\\s]*$").matcher(temp).find();
	}
	
	public String mod_JSON(String text){
		return "JSON.parse(JSON.stringfy("+text+"))";
	}
	public String mod_JSONP(String id,String text){
		return "window["+id+"](JSON.parse("+text+"))";
	}
	
	public String mod_read(String id,String propertyName){
		if(propertyName.equals("")){
			return "window["+id+"]";
		}else{
			return "window["+id+"]["+propertyName+"]";
		}
	}
	public String mod_assign(String id,String propertyName){
		if(propertyName.equals("")){
			return "window["+id+"] = window["+id+"]";
		}else{
			return "window["+id+"]["+propertyName+"] = window["+id+"]";
		}
		
	}
	
	/**
	 * modify eval(typeof(~~~));
	 * @param id which is name of variable
	 * @return modify typeof text
	 */
	public String mod_typeof(String id){
		return "typeof(window["+id+"]);";
	}
	
	public String mod_Call(String id, String text){
		return "window["+id+"(window["+id+","+text+")";
	}
	
	public String mod_Empty(){
		return "void 0";
	}
	
	public String mod_else(String text){
		System.out.println(text);
		/*Matcher m = Pattern.compile("([\\s\\S]*)([\'\"].*[\'\"])([\\s\\S]*)").matcher(text);
		if(m.find()){
			return convertToUnicode(m.group(1))+m.group(2)+convertToUnicode(m.group(3));
		}else{
			return convertToUnicode(text);
		}*/
		System.out.println(convertToUnicode(text));
		return convertToUnicode(text);
		
	}
	
	
	/**
	 * Unicode文字列に変換する("あ" -> "\u3042")
	 * @param original
	 * @return
	 */
	private static String convertToUnicode(String original)
	{
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < original.length(); i++) {
	    	if(Pattern.compile("^[A-Za-z0-9\"\']").matcher(String.valueOf(original.charAt(i))).find()){
	    		sb.append(String.valueOf(original.charAt(i)));
	    	}else{
	    		sb.append(String.format("\\u%04X", Character.codePointAt(original, i)));
	    	}
	    }
	    String unicode = sb.toString();
	    return unicode;
	}
}

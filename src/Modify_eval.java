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
	
	static final String JSON_pat = "^[A-Za-z0-9_\\$\\.\t\\[\\]\"\']*=[\t\r\n]*(.*);*$";
	
	static final String Empty_pat = "^$";
	
	static final String Library_pat = "function *[A-Za-z0-9_\\$]* *("; //string must be greater 512bytes
	static int Library_flag = 0;
	
	static final String Typeof_pat1 = "^typeof*\\(? *[A-Za-z0-9_\\$\\.\t\\[\\]\"\']*\\)?$";
	static final String Typeof_pat2 = "^typeof*\\(? *[A-Za-z0-9_\\$\\.\t\\[\\]\"\']*\\)? *[!=<>]+";
	static final String Typeof_pat3 = "^typeof*\\(? *[A-Za-z0-9_\\$\\.\t\\[\\]\"\']*\\)? *[!=<>]+[^\\)]*\\)[^\\}]*\\}? *;? *$";
	
	static final String Read_pat1 = "^[A-Za-z0-9_\\$]*$";
	static final String Read_pat2 = "^[A-Za-z0-9_\\$\\.\\[\\]\"\']*$";
	
	static final String Call_pat = "^[A-Za-z0-9_\\$\\.]*\\([A-Za-z0-9_\\$\\.\\[\\]\"\']*;?[\t\r\n]*\\);?$";
	
	static final String Assign_pat1 ="^[A-Za-z0-9_\\$\\.\\[\\]\"\']* *= *[A-Za-z0-9_\\.\\[\\]\"\']*;?[\t\r\n]*$";
	static final String Assign_pat2 ="^var [A-Za-z0-9_\\$]* *(= *[A-Za-z0-9_\\$\\.\\[\\]\"\']*)?;?$";
	
	static final String Try_pat = "^try *\\{[^\\}]*\\} *catch *\\([^\\)]*\\) *\\{[^\\}]*\\} *;?$";
	
	
	public static void main(String[] args){
		
	}
	
	
	/**
	 * 
	 * @param str eval(str);
	 * @return modify str;
	 */
	public String mod_eval(String str){
		
		if(Pattern.compile(JSON_pat).matcher(str).find()){
			return mod_JSON(str);
		}else if(Pattern.compile(Empty_pat).matcher(str).find()){
			return str;
		}else if(Pattern.compile(Library_pat).matcher(str).find()){
			Library_flag = 1;
			return str;
		}else if(Pattern.compile(Typeof_pat1).matcher(str).find() || Pattern.compile(Typeof_pat2).matcher(str).find() || Pattern.compile(Typeof_pat3).matcher(str).find()){
			return str;
		}else{
		
			return str;
		}
		
	}
	
	public String mod_JSON(String text){
		return "JSON.parse("+text+");";
	}
	public String mod_JSONP(String text){
		return "window["+text+"JSON.parse();";
	}
	/**
	 * modify eval(typeof(~~~));
	 * @param id which is name of variable
	 * @return modify typeof text
	 */
	public String mod_typeof(String id){
		return "typeof(window["+id+"]);";
	}
	
	public String mod_read(String text){
		return null;
	}

}

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analyze.js.*;

public class modify_ajax {
	
	String ajaxpat = "\\$\\.ajax\\(";
	String ajax_string = "$.ajax(";
	ArrayList<String> url = new ArrayList<String>();
	
	modify_ajax(){
		
	}
	
	public static void main(String[] args){
		modify_ajax aj = new modify_ajax();
		String source = aj.fileReader("./test/gsimaps.js");
		aj.search_ajax(source);
	}
	
	public HashSet<String> extrace_basedomain(HashSet<String> url){
		HashSet<String> base = new HashSet<String>();
		ArrayList<String> temp = new ArrayList<String>(url);
		for(int i=0;i<temp.size();i++){
			Matcher m = Pattern.compile("(htt[p|ps]://.*?/)(.*)\\.(.*)").matcher(temp.get(i));
			m.find();
			base.add(m.group(1));
		}
		return base;
	}
	
	public HashSet<String> search_ajax(String source){
		Stack<String> stparent = new Stack<String>();
		//ArrayList<String> text = new ArrayList<String>();
		HashSet<String> text = new HashSet<String>();
		HashSet<String> url = new HashSet<String>();
		Matcher m = Pattern.compile(ajaxpat).matcher(source);
		Matcher mone = Pattern.compile("[\\s\\S]").matcher(source);
		while(m.find()){
			int start=m.end(),end = -1;
			mone.region(m.end()-1, mone.regionEnd());
			while(mone.find()){
				if(mone.group().equals("(")){
					stparent.push(mone.group());
				}else if(mone.group().equals(")")){
					stparent.pop();
					if(stparent.isEmpty()){
						m.region(mone.end(), m.regionEnd());
						end = mone.end()-1;
						break;
					}
				}
			}
			//System.out.println(ajax_string+source.substring(start, end));
			text.add(ajax_string+source.substring(start, end));
			url.add(search_url(source.substring(start,end)));
		}
		//for(String key: url){
		//	System.out.println("url = "+key);
		//}
		return match_url(url,source);
	}
	
	private HashSet<String> match_url(HashSet<String> url,String source){
		HashSet<String> tmp = new HashSet<String>();
		String urlpat = "[\\s]*[\"\'](http.*)[\"\']";
		for(String key : url){
			Matcher m = Pattern.compile(urlpat).matcher(key);
			if(m.find()){
				tmp.add(key);
			}else{
				String keytmp = key;
				String stmp="";
				if(key.contains("+")){
					String[] patkey = keytmp.split("\\+");
					for(int i=0;i<patkey.length;i++){
						stmp += find_url("[\\s]*[\"\'](.*)[\"\'][\\s]*",patkey[i],source);
					}
					//System.out.println(stmp);
					keytmp = stmp;
				}else{
					stmp = new String(find_url("[\\s]*[\"\'](.*)[\"\'][\\s]*",keytmp,source));
					//System.out.println(stmp);
					keytmp = stmp;
				}
				
				while(keytmp.contains("+")){
					String[] patkey = keytmp.split("\\+");
					stmp ="";
					for(int i=0;i<patkey.length;i++){
						stmp += find_url("[\\s]*[\"\'](.*)[\"\'][\\s]*",patkey[i],source);
					}
					//System.out.println(stmp);
					keytmp = stmp;
				}
				
				//System.out.println(stmp);
				Matcher http = Pattern.compile("(http.*\\.php)[\\s\\S]*").matcher(stmp);

				if(http.find()){
					String httpurl = http.group(1).replaceAll("[\"\']", "");
	//				System.out.println("url is "+httpurl);
					tmp.add(httpurl);
				}
			}
		}
		for(String key:tmp){
			System.out.println(key);
		}
		
		return tmp;
	}
	
	
	private String find_url(String pat,String str,String source){
		String tmp="";
		Matcher m = Pattern.compile(pat).matcher(str);
		if(m.find()){
			tmp += m.group(1);
		}else{
			tmp += Search_varitem.ret_veriable_value(source, str.trim());
		}
		return tmp.trim();
	}
	
	private String search_url(String ajax_text){
		String urlpat= "url[\\s]*?:(.*?),";
		Matcher m = Pattern.compile(urlpat).matcher(ajax_text);
		String tmp = "";
		if(m.find()){
			//System.out.println(m.group(1));
			tmp = m.group(1);
		}
		return tmp;
	}
	
	public static String fileReader(String filename){
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
		}
		return sb.toString();
	}
}

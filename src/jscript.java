import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class jscript {
	
	static ArrayList<String> removetag = new ArrayList<String>();
	static ArrayList<String> fileextension = new ArrayList<String>();
	static char jsfilename = 'a';
	static char stylefilename = 'a';
	
	public static void main(String args[]){
		inline_init();
		//System.out.println(args[0]);
		analyzehtml("test");

	}
	public static void inline_init(){
		removetag.add("script");
		removetag.add("style");
		fileextension.add(".js");
		fileextension.add(".css");
		
	}
	
	/*catch program css, script or anything else which CSP applying*/
	public static void analyzehtml(String filename){
		try{
			File file = new File("./test/test.html");
			FileReader filereader = new FileReader(file);
			BufferedReader bf = new BufferedReader(filereader);
			/*flag is which part is the match word,script,eval or style*/
			int i;
			String str,pat1,pat2="",pat3="";
			String div="";
			while((str = bf.readLine()) != null){
			
				/*find tag*/
				for(i =0;i < removetag.size();i++){
					/*find script,eval or style tag*/
					pat1 = "(.*)(<"+removetag.get(i)+".*)";
					pat2 = "(.*)(</"+removetag.get(i)+">)(.*)";
					pat3 = "(.*)(<"+removetag.get(i)+".*>)(.*)(</"+removetag.get(i)+">)(.*)";
					if(str.matches(pat1)){
						Matcher m = patternmatch(str,pat1);
						div=m.group(2)+"\n";
						break;
					}
				}
				
				/*write htmlfile for script*/
				switch(i){
				case 0:
					if(!str.contains("src")){
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(3);
							System.out.println(div);
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
						
						Matcher m = patternmatch(str,pat2);
						div += m.group(1)+m.group(2);
						dividefile(div,i);
						System.out.println(div+"\n");
						}
					}else{
						/*if script tag contains src,this script don't need divide file*/
						//System.out.println(div);
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(2)+m.group(3)+m.group(4);
							System.out.println(div);
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
							Matcher m = patternmatch(str,pat2);
							div += m.group(1)+m.group(2);
							System.out.println(div);
						}
					}
					break;
				default:
					System.out.println(str);
					break;
				}
			}
			filereader.close();
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
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
	public static void dividefile(String text,int type){
		//System.out.println(text);
		/*write file*/
		
		/*instead of divide, write like <script src="~~" ></script> text*/
		
		System.out.println("<"+removetag.get(type)+" src=\""+jsfilename+fileextension.get(type)+"\"></"+removetag.get(type)+">\n");
		jsfilename++;
	}
	
	/*divide CSS from html*/
	public static void writecss(String text){
		

	}
}

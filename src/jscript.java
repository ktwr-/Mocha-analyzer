import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
		
		try {
			copyfile();
			System.out.println("finish");
			Thread.sleep(1000);
		} catch (IOException e) {
			System.out.println(e);
		} catch (InterruptedException e){
			System.out.println(e);
		}
		
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
			File wfile = new File("./csp/test.html");
			FileWriter fw = new FileWriter(wfile);
			/*flag is which part is the match word,script,eval or style*/
			int i;
			String str,pat1,pat2="",pat3="";
			String div="";
			while((str = bf.readLine()) != null){
				System.out.println(str);
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
				
				/*write htmlfile for script*/
				switch(i){
				case 0:
					if(!str.contains("src")){
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(3);
							String mdhtml = dividescript(div,i);
							//System.out.println(div);
							fw.write(mdhtml);
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
						
						Matcher m = patternmatch(str,pat2);
						div += m.group(1);//+m.group(2);
						String mdhtml = dividescript(div,i);
						fw.write(mdhtml+"\n");
						//System.out.println(div+"\n");
						}
					}else{
						/*if script tag contains src,this script don't need divide file*/
						//System.out.println(div);
						if(str.matches(pat3)){
							Matcher m = patternmatch(str,pat3);
							div = m.group(2)+m.group(3)+m.group(4);
							//System.out.println(div);
							fw.write(div);
						}else{
							while(!(str = bf.readLine()).matches(pat2)){
								div += str+"\n";
							}
							Matcher m = patternmatch(str,pat2);
							div += m.group(1);//+m.group(2);
							//System.out.println(div);
							fw.write(div);
						}
					}
					break;
				//write css file
				case 1:
					if(str.matches(pat3)){
						Matcher m = patternmatch(str,pat3);
						div = m.group(3);
						//System.out.println(div);
						fw.write(div);
					}else{
						while(!(str = bf.readLine()).matches(pat2)){
							div += str+"\n";
						}
						Matcher m = patternmatch(str,pat2);
						div += m.group(1)+m.group(2);
						String mdhtml = dividestyle(div);
						fw.write(mdhtml+"\n");
						//System.out.println(div+"\n");
					}
					break;
				default:
					//System.out.println(str);
					fw.write(str+"\n");
					break;
				}
			}
			filereader.close();
			fw.close();
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

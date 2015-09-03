import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class jscript {
	
	static ArrayList<String> removetag = new ArrayList<String>();
	
	public static void main(String args[]){
		init();
		System.out.println(args[0]);
		findCSP(args[0]);
	}
	public static void init(){
		removetag.add("script");
		removetag.add("style");
		removetag.add("eval");
		
	}
	
	/*catch program css, script or anything else which CSP applying*/
	public static void findCSP(String filename){
		try{
			File file = new File("./test/test.html");
			FileReader filereader = new FileReader(file);
			int ch;
			while((ch = filereader.read()) != -1){
				System.out.print((char)ch);
			}
			/*find script tag and write another file */
			
			/*write htmlfile for script*/
			
			filereader.close();
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		
		
	}
	
	/*divide javascript from html*/
	public static void writescript(String text){
		
		
	}
	
	/*divide CSS from html*/
	public static void writecss(String text){
		

	}
}

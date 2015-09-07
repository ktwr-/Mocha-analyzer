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
		//System.out.println(args[0]);
		findCSP("test");
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
			BufferedReader bf = new BufferedReader(filereader);
			/*flag is which part is the match word,script,eval or style*/
			int i;
			String str;
			while((str = bf.readLine()) != null){
			
				/*find tag*/
				for(i =0;i < removetag.size();i++){
					/*find script,eval or style tag*/
					if(str.matches(".*<"+removetag.get(i)+">.*")){
						break;
					}
				}
				/*write htmlfile for script*/
			
				switch(i){
				case 0:
					System.out.println(str);
					while(!(str = bf.readLine()).matches(".*</script>.*")){
						System.out.println(str);
					}
					System.out.println(str+"\nendscript");
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
	
	/*divide javascript from html*/
	public static void writescript(String text){
		
		
	}
	
	/*divide CSS from html*/
	public static void writecss(String text){
		

	}
}

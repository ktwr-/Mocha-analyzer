package source.main.mocha;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JSList {
	
	public ArrayList<String> jsFile;
	public HashMap<String,ArrayList<String>> jsFileChecker;
	
	public static void main(String args[]) throws IOException{
		new JSList().test();
	}
	
	JSList(){
		jsFile= new ArrayList<String>();
		jsFileChecker = new HashMap<String,ArrayList<String>>();
	}

	
	public void findJSFile(File file) throws IOException{
		Document doc = Jsoup.parse(file, "UTF-8");
		Elements scripts = doc.getElementsByTag("script");
		Element script;
		for(int i=0;i < scripts.size();i++){
			if((script = scripts.get(i)).hasAttr("src")){
				if(!script.attr("src").contains("http")){
					this.jsFile.add(file.getParent()+"/"+script.attr("src"));
				}else{
					this.jsFile.add(script.attr("src"));
				}
			}
		}
	}
	
	/**
	 * unit test function
	 * @throws IOException 
	 */
	public void test() throws IOException{
		JSList jsList = new JSList();
		jsList.findJSFile(new File("./test/test.html"));
		for(int i=0;i < jsList.jsFile.size();i++){
			System.out.println(jsList.jsFile.get(i));
		}
		
	}
	
}

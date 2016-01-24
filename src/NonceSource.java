import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.mozilla.javascript.*;

// only support ejs

public class NonceSource {
	
	ArrayList<String> noncelist = new ArrayList<String>();
	NonceSource(){
		
	}
	
	public void add_nonce_source(String filename){
		try{
			File file = new File(filename);
			Document doc = Jsoup.parse(file,"UTF-8");
			
			//nonce_random.add(noncerandom(source));
			insertCSP(doc,noncelist);
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	
	public void insertCSP(Document doc,ArrayList<String> noncerandom){
		
	}
	
	public Document nonce_source(Document doc){
		String html = doc.toString();
		Elements ele = doc.getElementsByTag("script");
		for(int i=0;i<ele.size();i++){
			Element script = ele.get(i);
			String nonce = calculate_random(script.toString());
			noncelist.add(nonce);
			//add nonce-hash
			String replacehtml = script.toString().replaceAll(Pattern.quote("<script>"), "<script nonce=\""+nonce+"\">");
			html = html.replaceAll(Pattern.quote(script.toString()), replacehtml);
		}
		return null;
	}
	public void search_ejsrender(ArrayList<String> jsfilename,String filename){
		for(int i=0;i<jsfilename.size();i++){
			File file = new File(jsfilename.get(i));
			
		}
	}
	public String calculate_random(String source){
		
		return null;
	}
}

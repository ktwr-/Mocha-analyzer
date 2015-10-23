import java.io.BufferedReader;
import java.io.FileReader;
import org.mozilla.javascript.*;
import org.mozilla.javascript.ast.AstRoot;

public class analyzeScript {
	
	public static void main(String args[]){
		analyzeScript as = new analyzeScript();
		String source = as.fileReader("./test/chtest/event.js");
		System.out.println(source);
		try{
			AstRoot astRoot = new Parser().parse(source, "",1);
		}catch(org.mozilla.javascript.EvaluatorException e){
			e.printStackTrace();
			System.err.println(e.lineNumber());
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
	
}
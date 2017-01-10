import java.io.IOException;

import brut.apktool.*;
import brut.common.BrutException;
/**
 * This class provide source code from apk file. Use brut.apktool
 * 
 * @author takeuchi
 *
 */
public class SourceProvider {
	
	SourceProvider(){}
	
	public static void main(String[] args) throws IOException, InterruptedException, BrutException{
		SourceProvider sp = new SourceProvider();
		sp.build_apk("csp");
	}
	/**
	 * This function decodes apk file.
	 * 
	 * @param filename which you want to decode.
	 */
	public void provide_sourcecode(String filename) throws IOException, InterruptedException, BrutException {
		String[] str_apktool = {"d", filename };
		Main.myApktool(str_apktool);
	}
	/**
	 * This function builds directory to apk.
	 * 
	 * @param directory which you want to build.
	 */
	public void build_apk(String directory) throws IOException, InterruptedException, BrutException {
		String[] str_apktool = {"b", directory, "-o", "csptest.apk" };
		Main.myApktool(str_apktool);
		
	}
	
	

}

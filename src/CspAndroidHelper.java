import java.io.IOException;

import brut.common.BrutException;

/**
 * 
 * @author takeuchi
 *
 */
public class CspAndroidHelper {
	
	CspAndroidHelper(){
		
	}
	
	public static void main(String args[]) throws IOException, InterruptedException, BrutException, Exception{
		
		CspAndroidHelper.CspHelper("tmp",".");
		
		
	/*	
		SourceProvider sourceProvider = new SourceProvider();
		PutSignature putSignature = new PutSignature();
		
		String apkname = "tmp";
		
		// decode APK 
		sourceProvider.provide_sourcecode(apkname+".apk");
		
		// change to source code applying CSP 
		analyzeScript aS = new analyzeScript();
		jscript ajs = new jscript();
		
		ajs.getfilename(apkname);
		
		for(int i=0;i< ajs.html_file.size();i++){
			System.out.println(ajs.html_file.get(i));
		}
		
		for(int i=0;i< ajs.html_file.size();i++){
			jscript js = new jscript();
			js.htmlanalyze( ajs.html_file.get(i));
		}
		for(int i=0;i<ajs.js_file.size();i++){
			System.out.println("file:"+ajs.js_file.get(i));
			if(!ajs.js_file.get(i).contains("min.js") && !ajs.js_file.get(i).contains("leaflet") && !ajs.js_file.get(i).contains("data.js")){
				aS.analyzeScript(ajs.js_file.get(i));
			}
		}
		for(int i=0;i<ajs.js_file.size();i++){
			System.out.println(ajs.js_file.get(i));
		}
		
		
		// build APK 
		sourceProvider.build_apk(apkname);
		
		// put Signature / 
		String[] test = {"-p","csp_key1","csp.keystore", apkname+".apk", "csp.apk"};
		putSignature.putSignature(test);
		*/
		
	}
	
	public static void CspHelper(String name, String directory) throws Exception{
		SourceProvider sourceProvider = new SourceProvider();
		PutSignature putSignature = new PutSignature();
		
		String apkname = name;
		/* decode APK */
		sourceProvider.provide_sourcecode(directory+"/"+apkname+".apk");
		/* change to source code applying CSP */
		analyzeScript aS = new analyzeScript();
		jscript ajs = new jscript();
		
		ajs.getfilename(apkname);
		
		for(int i=0;i< ajs.html_file.size();i++){
			System.out.println(ajs.html_file.get(i));
		}
		
		for(int i=0;i< ajs.html_file.size();i++){
			jscript js = new jscript();
			js.htmlanalyze( ajs.html_file.get(i));
		}
		for(int i=0;i<ajs.js_file.size();i++){
			System.out.println("file:"+ajs.js_file.get(i));
			if(!ajs.js_file.get(i).contains("min.js") && !ajs.js_file.get(i).contains("leaflet") && !ajs.js_file.get(i).contains("data.js")){
				aS.analyzeScript(ajs.js_file.get(i));
			}
		}
		for(int i=0;i<ajs.js_file.size();i++){
			System.out.println(ajs.js_file.get(i));
		}
		
		
		/* build APK */
		sourceProvider.build_apk(apkname);
		
		/* put Signature */ 
		String[] test = {"-p","csp_key1","csp.keystore", apkname+".apk", "csp.apk"};
		putSignature.putSignature(test);
	}
	
	

}

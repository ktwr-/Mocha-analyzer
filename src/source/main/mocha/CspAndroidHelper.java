package source.main.mocha;
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
		CspAndroidHelper.CspHelper("tmp");
	}
	/**
	 * 
	 * this function decode apk and apply CSP
	 * 
	 * @param name apk name
	 * @param directory current directory
	 * @throws Exception
	 */
	public static void CspHelperApktool(String name, String directory) throws Exception{
		SourceProvider sourceProvider = new SourceProvider();
		PutSignature putSignature = new PutSignature();
		
		String apkname = name;
		// decode APK 
		sourceProvider.provide_sourcecode(directory+"/"+apkname+".apk");
		// change to source code applying CSP 
		analyzeScript aS = new analyzeScript();
		MochaMain ajs = new MochaMain();
		
		ajs.getFileName(apkname);
		
		for(int i=0;i< ajs.htmlFile.size();i++){
			System.out.println(ajs.htmlFile.get(i));
		}
		
		for(int i=0;i< ajs.htmlFile.size();i++){
			MochaMain js = new MochaMain();
			js.htmlAnalyze( ajs.htmlFile.get(i));
		}
		for(int i=0;i<ajs.jsFile.size();i++){
			if(!ajs.jsFile.get(i).contains("min.js") && !ajs.jsFile.get(i).contains("leaflet") && !ajs.jsFile.get(i).contains("data.js") && !ajs.jsFile.get(i).contains("jquery")){
				System.out.println("file:"+ajs.jsFile.get(i));
				aS.analyzeScript(ajs.jsFile.get(i));
			}
		}
		
		
		// build APK 
		sourceProvider.build_apk(apkname);
		
		// put Signature / 
		//String[] test = {"-p","csp_key1","csp.keystore", apkname+".apk", "csp.apk"};
		//putSignature.putSignature(test);
	}
	
	public static void CspHelper(String directory) throws IOException{
		// change to source code applying CSP 
		analyzeScript aS = new analyzeScript();
		MochaMain ajs = new MochaMain();
		
		ajs.getFileName(directory);	
		for(int i=0;i< ajs.htmlFile.size();i++){
			System.out.println(ajs.htmlFile.get(i));
		}
		
		for(int i=0;i< ajs.htmlFile.size();i++){
			ajs.htmlAnalyze(ajs.htmlFile.get(i));
		}
		for(int i=0;i<ajs.jsFile.size();i++){
			if(!ajs.jsFile.get(i).contains("min.js") && !ajs.jsFile.get(i).contains("leaflet") && !ajs.jsFile.get(i).contains("data.js") && !ajs.jsFile.get(i).contains("jquery")){
				System.out.println("file:"+ajs.jsFile.get(i));
				aS.analyzeScript(ajs.jsFile.get(i));
			}
		}
				
	}
	
	public static void Csp2Helper(String directory) throws IOException {
		// change to source code applying CSP 
			analyzeScript aS = new analyzeScript();
			MochaMain ajs = new MochaMain();
			
			ajs.getFileName(directory);	
			for(int i=0;i< ajs.htmlFile.size();i++){
				System.out.println(ajs.htmlFile.get(i));
			}
			
			for(int i=0;i< ajs.htmlFile.size();i++){
				ajs.htmlAnalyzeCSP2(ajs.htmlFile.get(i));
			}
			for(int i=0;i<ajs.jsFile.size();i++){
				if(!ajs.jsFile.get(i).contains("min.js") && !ajs.jsFile.get(i).contains("leaflet") && !ajs.jsFile.get(i).contains("data.js") && !ajs.jsFile.get(i).contains("jquery")){
					System.out.println("file:"+ajs.jsFile.get(i));
					aS.analyzeScript(ajs.jsFile.get(i));
				}
			}	
	}
	

}

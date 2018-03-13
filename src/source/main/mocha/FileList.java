package source.main.mocha;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author takeuchi
 * 
 */

public class FileList {
	ArrayList<String> HTMLFileList = new ArrayList<String>(); // HTML file in the directory
	ArrayList<String> JSFileList = new ArrayList<String>(); // JavaScript file in the directory
	HashMap<String,JSList> HTMLJSList = new HashMap<String,JSList>(); // HashMap(HTML file name, JavaScript file list in HTML file)
	HashMap<String,Boolean> JSModifyCheck = new HashMap<String,Boolean>();
	
	public void getfilename(String directory) throws IOException{
		File file = new File(directory);
		File files[] = file.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isFile()){
				if(files[i].getName().contains(".html")){
					HTMLFileList.add(files[i].toString());
					HTMLJSList.put(files[i].toString(), new JSList());
				}else if(files[i].getName().contains(".js")){
					JSFileList.add(files[i].toString());
					JSModifyCheck.put(files[i].toString(), false);
					
				}
			}else if(files[i].isDirectory()){
				getfilename(files[i].toString());
			}
		}
	}
	
	
	

}

package com.alpine.miner.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.alpine.utility.file.FileUtility;

public class DojoBuild {
    public static String projectRootDir = "";
    public static String releaseDirString = "";
	public static final String alpine_profile = "alpine.profile.js" ;
	public static String[] sourceFolders= new String[]{
		"WebContent/alpine",
		"WebContent/js/alpine" //note - i believe all dojo.includes have been removed from this directory.
	};
	
	public static void doBuild(){
       //Most of the profile file is defined in alpineProfile.template.
       //It's just missing three fields, which we create below.

       String theIncludes = generateLayerIncludes();
       String baseDir = getDojoSourceDir();
       String releaseDir = getReleaseDirString();

       System.out.println("base dojo dir is: " + baseDir);
        System.out.println("the release dir is: " + releaseDir);

        //Now we combines these with the template to create the new alpine.profile.js

        String templateLocation = getProfileTemplate();

        try
        {
            File f = new File(templateLocation);
            StringBuffer b = FileUtility.readFiletoString(f);

            String theTemplate = b.toString();
            if (theTemplate != null)
            {
                theTemplate = theTemplate.replace("$basepath","\"" + baseDir + "\"");
                theTemplate = theTemplate.replace("$releaseDir","\"" + releaseDir + "\"");
                theTemplate = theTemplate.replace("$theIncludes",theIncludes);
            }
            writeAlpineProfile(theTemplate);
        } catch (Exception e)
        {
            System.out.println("unable to create alpine.profile.js content");
            e.printStackTrace();
        }

	}

    private static String getReleaseDirString()
    {
        File f = new File(releaseDirString) ;

        return f.getAbsolutePath() + File.separator;
    }

    private static void writeAlpineProfile(String profile) throws Exception {
        String filePathName = getDojoSourceDir() + alpine_profile;
        System.out.println("location of alpine.profile.js: " + filePathName);
        File f= new File(filePathName);
        if (!f.exists()) f.createNewFile();
        FileUtility.writeFile(filePathName, profile);
    }


	
    public static String getProfileTemplate()
    {
        return getProjectRootDir() + "alpineProfile.template";

    }

	
	private static String getDojoSourceDir() {
		String releaseDir = getProjectRootDir()+"WebContent"
								+File.separator+"js"+File.separator;
		return releaseDir;
	}
	
	private static String getProjectRootDir() {
		File f = new File(projectRootDir);
		// f is root of web project, like
		//home/zhaoyong/dev/workspace/AlpineMinerWeb
		return f.getAbsolutePath() + File.separator;
	}

	private static String generateLayerIncludes(){
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < sourceFolders.length; i++) {
			String sourceFolder = sourceFolders[i];
			File file = new File(getProjectRootDir()+sourceFolder);
			try {
				addProfileDependencies(sb,file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        String results = sb.toString().trim();
        return results.substring(0, results.length() -1); //removing final comma

		
	}



//recrosive invoked
	private static void addProfileDependencies(StringBuffer sb,
			File file) throws  Exception {
		
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				addProfileDependencies(sb,files[i]);
			}
		}else{
			if(file.getName().endsWith(".js")
					||file.getName().endsWith(".jsp")){
				 
				System.out.println("file found:" +file.getName());
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = in.readLine();
				while (line != null) {
					line = line.replaceAll(" ", "");
					line = line.replaceAll("\t", "");
					if(line.startsWith("dojo.require")&&line.startsWith("dojo.requireLocalization") ==false
							&&line.endsWith(";")){
						line = line.replace(");", ",");
						line = line.replace("dojo.require(", "");
                        line = line.replace(".","/");
						//avoid the duplicated added
						if(sb.indexOf(line)<0 && !line.startsWith("\"dijit")){
							sb.append(line).append("\n");
						}
					}
					
					
					line = in.readLine();

				}
				in.close();
				
				 
			}
		}
		 
		
	}

    /**
     * This will create the profile file needed to dojo to compress and merge our javascript files.  Requires the following parameters
     * -dojoReleaseDirJS "/Users/sasher/Documents/alpine/adl/out/artifacts/AlpineIlluminator/js"
     * -webProjectDir "/Users/sasher/Documents/alpine/adl/AlpineMinerWeb"
     * @param args
     */

	public static void main (String [] args){
        if (args != null)
        {
            for (int i =0 ; i < args.length;i++)
            {
                if ("-webProjectDir".equals(args[i]))
                {
                    projectRootDir = args[i+1];
                }

                if ("-dojoReleaseDirJS".equals(args[i]))
                {
                    releaseDirString = args[i+1];
                }
            }
        }

        System.out.println("web project dir: " + projectRootDir);
        System.out.println("releaseDirString dir: " + releaseDirString);

        doBuild();
	}

}
 
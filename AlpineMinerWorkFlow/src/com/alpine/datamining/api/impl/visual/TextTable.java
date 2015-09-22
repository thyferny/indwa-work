/**
 * ClassName  TextTable.java
 *
 * Version information: 1.00
 *
 * Data: Jun 12, 2010
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.api.impl.visual;

import java.util.ArrayList;
import java.util.List;

import com.alpine.datamining.utility.Tools;

/**
 * @author John Zhao
 *
 */
public class TextTable {
	
	
	
	
	List<String[]> lines;
	
	private int []maxLengths;//max width for each column...

	private String locale;
 

	public TextTable(){
		    locale = System.getProperty("user.language");
		
		lines=new ArrayList<String[]>();
	}
	public List<String[]> getLines() {
		return lines;
	}

	public void setLines(List<String[]> lines) {
		this.lines = lines;
	}
	
	public void addLine(String[] line){
		lines.add(line);
	}
	/**
	 * @return
	 */
	public String toTableString() {
		if(lines.size()==0)
			return "";
		
		StringBuffer sb =new StringBuffer();
		 int colNo = lines.get(0).length;
		  maxLengths= countMaxLength(colNo);
		 
 		 
 
//		printSeperatorLine(sb,lineLength,maxLength);

		for(int i=0;i<lines.size();i++){
			printContentLine(sb,maxLengths,lines.get(i));
//			printSeperatorLine(sb,lineLength,maxLength);
		}
		return sb.toString();
	}
	private int countMaxLength() {
		int max=0;
		for(int i=0;i<lines.size();i++){
			String[] line=lines.get(i);
			for(int j=0;j<line.length;j++){
				if(line[j]!=null&&line[j].length()>max){
					max=line[j].length();
				}
			}
		}
		return max;
	}

	/**
	 * @param sb
	 * @param lineLength
	 * @param strings
	 */
	private void printContentLine(StringBuffer sb, int[] maxLengths, 
			String[] strings) {
		boolean isZhCncoding=locale.startsWith("zh");
		for(int i=0;i<strings.length;i++){
			 
			
	 
			int length=maxLengths[i]-strings[i] .length()+1;
			//us encoding need 2 blank...
			if(isZhCncoding==false){
				length=2*length;
				sb.append(" ");
			}
			sb.append(" ").append( strings[i] ) ;
			printBlank(sb,length);
		}
		
		// TODO Auto-generated method stub
		sb.append(" ").append(Tools.getLineSeparator());
	}
	/**
	 * @param sb
	 * @param length
	 */
	private void printBlank(StringBuffer sb, int length) {
		for(int i=0;i<length;i++){
			sb.append(" ");
		}
		
	}
	/**
	 * @param sb
	 * @param lineLength
	 */
	private void printSeperatorLine(StringBuffer sb, int lineLength,int colLength) {
		sb.append("+");
 		int j=0;
		for(int i=0;i<lineLength;i++){
// 			if(j==(colLength+1)){
// 				j=0;
// 				sb.append("+");
// 			}else{
				sb.append("-");
// 				j=j+1;
// 			}
			
		}
//		
//		if(colLength-j>0){
//			int z=colLength-j;
//			for(int i=0;i<z+1;i++){
//				sb.append("-");
//			}
//		}
//		if(colNo%2==1){
//			sb.append("-");
//		}
		sb.append("+").append(Tools.getLineSeparator());
		
	}
	/**
	 * @return
	 */
	private int[] countMaxLength(int colNo) {
		int max[]=new int[colNo];
		for(int i=0;i<lines.size();i++){
			String[] line=lines.get(i);
			
			for(int j=0;j<line.length;j++){
				
				if(line[j]!=null&&line[j].length()>max[j]){
					max[j]=line[j].length();
				}
			}
		}
		return max;
	}
	/**
	 * @return
	 */
	public int getColumnNumbers() {
		if(lines!=null&&lines.get(0)!=null){
			return lines.get(0).length;
		}
		return 0;
	}

}

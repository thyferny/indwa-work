package com.alpine.datamining.workflow.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class ToHtmlWriter {
	private static final String DOCTYPE="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
	private static final String DOCTYPE1="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
	private static final String LABLE_HTML_B="<html>";
	private static final String LABLE_HTML_E="</html>";
	private static final String LABLE_BODY_B="<body>";
	private static final String LABLE_BODY_E="</body>";
	private static final String LABLE_TABLE_B="<table border=2>";
	private static final String LABLE_TABLE_E="</table>";
	private static final String LABLE_TR_B="<tr>";
	private static final String LABLE_TR_E="</tr>";
	private static final String LABLE_TD_B="<td>";
	private static final String LABLE_TD_E="</td>";
	private static final String LABLE_P_B="<p>";
	private static final String LABLE_P_E="</p>";
	private static final String LABLE_IMAGE_B="<img src=\"";
	private static final String LABLE_IMAGE_E="\">";
	private static final String LABLE_LINK_B1="<a href=\"";
	private static final String LABLE_LINK_B2="\" target=\"";
	private static final String LABLE_LINK_B3="\">";
	private static final String LABLE_LINK_E="</a>";
	private static final String LABLE_ANCHOR_B1="<a name=\"";
	private static final String LABLE_ANCHOR_B2="\">";
	private static final String LABLE_ANCHOR_E="</a>";
	private static final String LABLE_DIV_B="<div class=\"dtree\">";
	private static final String LABLE_DIV_E="</div>";
	private static final String LABLE_SCRIPT_B="<script type=\"text/javascript\"";
	private static final String LABLE_SCRIPT_E="</script>";
	private static final String LABLE_SPAN_B="<span>";
	private static final String LABLE_SPAN_E="</span>";
	private static final String LABLE_OL_B1="<ol id=\"";
	private static final String LABLE_OL_B2="\">";
	private static final String LABLE_OL_E="</ol>";
	private static final String LABLE_UL_B="<ul>";
	private static final String LABLE_UL_B1="<ul id=\"";
	private static final String LABLE_UL_B2="\">";
	private static final String LABLE_UL_E="</ul>";
	private static final String LABLE_LI_B="<li>";
	private static final String LABLE_LI_E="</li>";
	private static final String LABLE_H1_B="<h1>";
	private static final String LABLE_H1_E="</h1>";
	private static final String LABLE_H2_B="<h2>";
	private static final String LABLE_H2_E="</h2>";
	private static final String LABLE_H3_B="<h3>";
	private static final String LABLE_H3_E="</h3>";
	private static final String LABLE_H4_B="<h4>";
	private static final String LABLE_H4_E="</h4>";
	private static final String LABLE_H5_B="<h5>";
	private static final String LABLE_H5_E="</h5>";
	private static final String ENCODING_ENGLISH="UTF-8";
	private static final String ENCODING_CHINA="GB2312";
	private static final String ENCODING_JAPEN="ISO-2022-JP";
	private static final String SCRIPT="<style type=\"text/css\">a:link,a:visited{text-decoration:none}#category { font-family: Verdana, Geneva, Arial, Helvetica, sans-serif;	white-space: nowrap;}#category li{ font-size: 18px; margin-left:-10px; }#category li li{ font-size: 16px; margin-left:5px; }#category li li li{ font-size: 14px; }#category ul {display:none; margin:0px;margin-left:10px; padding:0px 0px 0px 15px;}</style>"+
"<script type=\"text/javascript\">window.onload = function(){var spanNodes = document.getElementsByTagName(\"span\");	for (var i=0; i < spanNodes.length;i++){var uls = spanNodes[i].parentNode.getElementsByTagName(\"ul\")[0];if (uls){spanNodes[i].onclick = function(){var uls = this.parentNode.getElementsByTagName(\"ul\")[0];if (!(uls.open)){uls.style.display = \"block\";uls.open = true;} else {uls.style.display = \"none\";uls.open = null;}}}}	}</script>";
	
	private String title;
	
	private StringBuffer sbResult;
	private String sourceEncoding;
	
	public ToHtmlWriter(String sourceEncoding) {
		this.sourceEncoding=sourceEncoding;
		this.sbResult=new StringBuffer();
	}
	public ToHtmlWriter(StringBuffer sbResult,String sourceEncoding) {
		this.sourceEncoding=sourceEncoding;
		this.sbResult=sbResult;
	}
	
	public ToHtmlWriter() {
		this(getCharSet());
	}
	public void writeDoctype(){
		write(DOCTYPE);
	}
	public void writeDoctype1(){
		write(DOCTYPE1);
	}
	public void writeBeginHtml(){
		write(LABLE_HTML_B);
	}
	public void writeEndHtml(){
		write(LABLE_HTML_E);
	}
	
	public void writeBeginBody(){
		write(LABLE_BODY_B);
	}
	public void writeEndBody(){
		write(LABLE_BODY_E);
	}
	public void writeHead(){
		StringBuilder meta=new StringBuilder();
		meta.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
		meta.append(getCharSet()).append("\">");
		StringBuilder title=new StringBuilder();
		title.append("<title>").append(getTitle()).append("</title>");
		
		write("<head>");
		write(meta.toString());
		write(title.toString());
		write(SCRIPT);
		write("</head>");
	}

	public void writeAnchor(String str,String str1){
		write(LABLE_ANCHOR_B1);
		write(str);
		write(LABLE_ANCHOR_B2);
		write(str1);
		write(LABLE_ANCHOR_E);
	}
	public void writeSpan(String str){
		write(LABLE_SPAN_B);
		write(str);
		write(LABLE_SPAN_E);
	}
	public void writeH1(String str){
		write(LABLE_H1_B);
		write(str);
		write(LABLE_H1_E);
	}
	public void writeH2(String str){
		write(LABLE_H2_B);
		write(str);
		write(LABLE_H2_E);
	}
	public void writeH3(String str){
		write(LABLE_H3_B);
		write(str);
		write(LABLE_H3_E);
	}
	public void writeH4(String str){
		write(LABLE_H4_B);
		write(str);
		write(LABLE_H4_E);
	}
	public void writeH5(String str){
		write(LABLE_H5_B);
		write(str);
		write(LABLE_H5_E);
	}
	public void writeP(String str){
		write(LABLE_P_B);
		write(str);
		write(LABLE_P_E);
	}
	public void writeTable(String str){
		write(LABLE_TABLE_B);
		write(str);
		write(LABLE_TABLE_E);
	}
	public void writeTR(String str){
		write(LABLE_TR_B);
		write(str);
		write(LABLE_TR_E);
	}
	public void writeTD(String str){
		write(LABLE_TD_B);
		write(str);
		write(LABLE_TD_E);
	}
	public void writeOL(String str){
		write(LABLE_OL_B1);
		write(str);
		write(LABLE_OL_E);
	}
	public void writeOLWithId(String str,String id){
		write(LABLE_OL_B1);
		write(id);
		write(LABLE_OL_B2);
		write(str);
		write(LABLE_OL_E);
	}
	public void writeULWithId(String str,String id){
		write(LABLE_UL_B1);
		write(id);
		write(LABLE_UL_B2);
		write(str);
		write(LABLE_UL_E);
	}
	public void writeUL(String str){
		write(LABLE_UL_B);
		write(str);
		write(LABLE_UL_E);
	}
	public void writeLI(String str){
		write(LABLE_LI_B);
		write(str);
		write(LABLE_LI_E);
	}
	public void writeImg(String str) {
		write(LABLE_P_B);
		write(LABLE_IMAGE_B);
		str=str.substring(str.lastIndexOf(File.separator)+1, str.length());
		write(str);
		write(LABLE_IMAGE_E);
		write(LABLE_P_E);
	}
	public void writeLink(String str){
		write(LABLE_LINK_B1);
		String link=str.substring(str.lastIndexOf(File.separator)+1, str.length());
		write(link);
		write(LABLE_LINK_B3);
		write(str);
		write(LABLE_LINK_E);
	}
	public void writeA(String str,String str1,String target){
		write(LABLE_LINK_B1);
		write(str);
		if(target!=null&&!target.isEmpty()){
			write(LABLE_LINK_B2);
			write(target);
		}
		write(LABLE_LINK_B3);
		write(str1);
		write(LABLE_LINK_E);
	}
	public void writeDiv(String str){
		write(LABLE_DIV_B);
		write(str);
		write(LABLE_DIV_E);
	}
	
	public void writeScript(String str){
		write(LABLE_SCRIPT_B);
		write(str);
		write(LABLE_SCRIPT_E);
	}
	
	public void writeFrame(String name,String page){
		write("<frame name='");
		write(name);
		write("' src=\"");
		write(page);
		write("\">");
	}
	public void writeFrameSet(String[] percent,String[] frame){
		write("<frameset cols=\"");
		for(int i=0;i<percent.length;i++){
			write(percent[i]);
			if(percent.length>1&&i<percent.length-1){
				write(",");
			}
		}
		write("\">");
		for(int i=0;i<frame.length;i++){
			write(frame[i]);
		}
		write("</frameset>");
	}
	public void writeChapterTitle(String cTitle){
		StringBuilder sb_CTitle=new StringBuilder();
		sb_CTitle.append("<h1>").append(cTitle).append("</h1>");
		write(sb_CTitle.toString());
	}
	
	public void writePageSplit(){
		write("<HR style=\"FILTER: alpha(opacity=100)\" width=\"100%\" color=#987cb9 SIZE=3>");
	}
	private void write(String str){
		try {
			 //from Web is UTF-8 ,local is jdk's 
			sbResult.append(new String(str.getBytes(sourceEncoding),getCharSet()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getCharSet(){
		Locale local = Locale.getDefault();
		if (local.equals(Locale.CHINA)
				||local.equals(Locale.CHINESE)) {
			return ENCODING_CHINA;
		} else if(local.equals(Locale.JAPAN)
				||local.equals(Locale.JAPANESE)) {
			return ENCODING_JAPEN;
		} else {
			return ENCODING_ENGLISH;
		}
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return sbResult.toString();
	}

	public StringBuffer toStringBuffer() {
		return sbResult;
	}

	
}
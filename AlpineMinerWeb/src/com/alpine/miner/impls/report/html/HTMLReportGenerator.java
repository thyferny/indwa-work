/**
 * ClassName :HTMLReportGenerator.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.html;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.alpine.miner.impls.report.model.Chapter;
import com.alpine.miner.impls.report.model.ImageParagraph;
import com.alpine.miner.impls.report.model.IndexItem;
import com.alpine.miner.impls.report.model.Paragraph;
import com.alpine.miner.impls.report.model.ReporterModel;
import com.alpine.miner.impls.report.model.TableParagraph;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.utility.file.StringUtil;

/**
 * @author zhaoyong
 *
 */
public class HTMLReportGenerator {
	
	//this file is in the root of classpath
	private static final String CSS_FILE_CONTENT = "/report_content.css" ;
	private static final String CSS_FILE_NAV = "/report_navigator.css" ;
	private static final String ENCODING_UTF8=Persistence.ENCODING;
	//no use any more, 
	
	public static final String CSS_TOP_CHAPTER_TITLE="top_chapter_title"; 
	public static final String CSS_CHAPTER_TITLE="chapter_title";
	//public static final String CSS_CHAPTER_CONTENT="chapter_content";
	
	public static final String CSS_SUB_CHAPTER_PREFIX="sub_chapter_title_";

//	public static final String CSS_PARAGRAPH_TITLE="paragraph_title_";//+0,1,2..
	public static final  String CSS_LEGEND_TABLE="legendTable";
 
	
	public static final String CSS_OUTPUT_IMAGE="output_image";
	public static final String CSS_OUTPUT_TEXT="output_text";
	
	public static final String CSS_OUTPUT_TABLE="output_table";
	public static final String CSS_PROPERTY_TABLE="property_table";
	
	public static final String CSS_OUTPUT_TABLE4SCATTER = "output_table4scatter";
	
	public static final String JS="window.onload = function(){var spanNodes = document.getElementsByTagName(\"span\");	" +
			"for (var i=0; i < spanNodes.length;i++)" +
			"{var uls = spanNodes[i].parentNode.getElementsByTagName(\"ul\")[0];" +
			"if (uls){spanNodes[i].onclick = function(){var uls = this.parentNode.getElementsByTagName(\"ul\")[0];" +
			"if (!(uls.open)){uls.style.display = \"block\";uls.open = true;} else {uls.style.display = \"none\";" +
			"uls.open = null;}}}}	}";
	
	private static final String INDEX_TREE_HREF_PREFIX="content.html#" ;
	public static final String CSS_TABLED_GROUP = "tabledGroup"; //like kmeans 
	public static final String CSS_SUBPARAMETER = "sub_property"; 
	public static String toContentHtml(ReporterModel report,boolean isIE,Locale locale) throws Exception{
		
		HTMLDocument root= new HTMLDocument();
  
	 
		HTMLElement head = new HTMLHead(); 
	 
		 
		HTMLMeta meta = new HTMLMeta(getEncoding(isIE,locale)) ;	
		
		
		head.appendChild(meta) ;
		
		HTMLStyle style = new HTMLStyle( ) ;
		String reportCSS = getReportCSS(CSS_FILE_CONTENT); 
		style.appendChild(new HTMLTextNode(reportCSS)) ;  
		head.appendChild(style) ;
		
		root.appendChild(head) ;
		
		HTMLBody  body = new HTMLBody () ;
		root.appendChild(body) ;
		
		List<Chapter> chapters = report.getChapters();
 
 
		if(chapters!=null){
			for (Iterator<Chapter> iterator = chapters.iterator(); iterator
					.hasNext();) {
				Chapter chapter =  iterator.next(); 
				
				body.appendChilds(toHTMLElement(chapter,chapter.getStyleId())) ; 
				body.appendChild(new HTMLHR());
			}
		}
		return root.toHTML().toString();
		

	}

	private static String getEncoding(boolean isIE,Locale locale) {
		String encode =ENCODING_UTF8;
 		return encode;
	}

	/**
	 * @param chapter
	 * @return
	 * @throws Exception 
	 */
	private static List<HTMLElement> toHTMLElement(Chapter chapter,String titleCSS) throws Exception { 
		HTMLParagraph chapterTitle = new HTMLParagraph(chapter.getId(), titleCSS) ;
		chapterTitle.appendChild(new HTMLTextNode(chapter.getTitle())) ;
		List<HTMLElement>  chapterContent = toChapterContent(chapter);
		
		List<HTMLElement> chapterElements = new ArrayList<HTMLElement> (); 
		chapterElements.add(chapterTitle) ;
		chapterElements.addAll(chapterContent) ;
		return chapterElements;
	}

	/**
	 * @param chapter
	 * @return
	 * @throws Exception 
	 */
	private static List<HTMLElement>  toChapterContent(Chapter chapter) throws Exception {
		
		List<HTMLElement> chapterElements = new ArrayList<HTMLElement> (); 
		//1 paragraph
		List<Paragraph> paragraphs = chapter.getParagraphs();
		if(paragraphs!=null&&paragraphs.size()>0){
			for (Iterator<Paragraph> iterator = paragraphs.iterator(); iterator.hasNext();) {
				Paragraph paragraph = iterator.next();
				 List<HTMLElement>  subHTMLs=toHTMLElement(  paragraph) ; 
				 chapterElements.addAll(subHTMLs) ;
			}
			
			
		}
		//2 subchapter
		List<Chapter> subChapters = chapter.getSubChapters() ; 
		if(subChapters!=null&&subChapters.size()>0){
			for (Iterator<Chapter> iterator = subChapters.iterator(); iterator.hasNext();) {
				 Chapter subChapter = iterator.next();
				 List<HTMLElement>  subHTMLs=toHTMLElement(  subChapter,subChapter.getStyleId()) ;
				 chapterElements.addAll(subHTMLs) ;
			}
		}
		return chapterElements;
	}

	/**
	 * @param paragraph
	 * @return
	 * @throws Exception 
	 */
	private static List<HTMLElement> toHTMLElement(Paragraph paragraph) throws Exception {
		List<HTMLElement> paragraphElements = new ArrayList<HTMLElement> (); 

		//1 paragraph title
		if(paragraph.getTitle()!=null&&paragraph.getTitle().trim().length()>0){
			HTMLParagraph chapterTitle = new HTMLParagraph(paragraph.getId(), paragraph.getStyleId()) ;
			chapterTitle.appendChild(new HTMLTextNode(paragraph.getTitle())) ;
			paragraphElements.add(chapterTitle);
		}
		
		//2 content (text,table,image)
		
		String content = paragraph.getContent();

		//if image or table the content is the description of them
		if(paragraph instanceof ImageParagraph ){
			HTMLImage imageParagraph = new HTMLImage( ((ImageParagraph)paragraph).getImagePath(),null,paragraph.getStyleId() ) ;  
			paragraphElements.add(imageParagraph);
			
		}else if(paragraph instanceof TableParagraph ){
			
			HTMLTable tableParagraph = toHTMLTable((TableParagraph)paragraph) ; 
			paragraphElements.add(tableParagraph);
		} else if(StringUtil.isEmpty(content)==false){ 
			HTMLParagraph textParagraph = new HTMLParagraph("",paragraph.getStyleId());
			textParagraph.appendChild(new HTMLTextNode(content)) ;
			paragraphElements.add(textParagraph);
		}
 
		return paragraphElements;
	}

	/**
	 * @param paragraph
	 * @return
	 * @throws Exception 
	 */
	private static HTMLTable toHTMLTable(TableParagraph paragraph) throws Exception {

		// content is the
		HTMLTable tableParagraph = new HTMLTable("",paragraph.getStyleId());

		
		HTMLTR tr = new HTMLTR();
		String[] head = paragraph.getTableHeader();
		if(head!=null){
			for (int i = 0; i < head.length; i++) {
				HTMLTD td = new HTMLTD();
				td.appendChild(new HTMLTextNode(head[i])) ;
				tr.appendChild(td) ;
			}
		}
		tableParagraph.appendChild(tr);
		List<List<Paragraph>> rows = paragraph.getTableRows();
		if (rows != null) {
			for (Iterator iterator = rows.iterator(); iterator.hasNext();) {
				List<Paragraph> list = (List<Paragraph>) iterator.next();
				if(list!=null){
					tr = new HTMLTR();
					for (Iterator iterator2 = list.iterator(); iterator2
							.hasNext();) {
						Paragraph paragraph2 = (Paragraph) iterator2.next();
						HTMLTD td = new HTMLTD();
						td.appendChilds(toHTMLElement(paragraph2)) ;
						tr.appendChild(td) ; 
						
					}
					tableParagraph.appendChild(tr);
				}
			}
		}

		return tableParagraph;
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 * @throws Exception 
	 */
	public static String creatManiPageHtml(String indexFileName, String contentFileName,String title,boolean isIE, Locale locale) throws Exception {
		HTMLDocument root= new HTMLDocument(); 
		
		HTMLElement head = new HTMLHead(); 
		root.appendChild(head);
		HTMLMeta meta = new HTMLMeta(getEncoding(  isIE,   locale)) ;
	 
		HTMLSimpleElement titleElement = new HTMLSimpleElement("title", null, null, null, null) ;
		titleElement.appendChild(new HTMLTextNode(title)) ;
		meta.appendChild(titleElement) ;
		head.appendChild(meta) ;
 
		
		HTMLElement frameSet = new HTMLFrameSet("15%,85%", null, null);
		frameSet.appendChild(new HTMLFrame("index", indexFileName)) ;
		frameSet.appendChild(new HTMLFrame("content", contentFileName)) ;
		root.appendChild(frameSet);
		
		return root.toHTML().toString();
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private static String getReportCSS(String filePath) throws Exception { 
		InputStream inputStream = HTMLReportGenerator.class.getResourceAsStream(filePath)		 ;
		BufferedReader reader = (new BufferedReader(new InputStreamReader(inputStream) ));
		StringBuffer sb = new StringBuffer();
		int c;
		while ((c = reader.read()) != -1){
			sb.append((char) c);
		}
		inputStream.close();
		reader.close();
		return  sb.toString();
 
	}

	/**
	 * @param index
	 * @return
	 * @throws Exception 
	 */
	public static String toIndexHtml(List<IndexItem> index,boolean isIE, Locale locale) throws Exception { 
		
	HTMLDocument root= new HTMLDocument(); 
		
		HTMLElement head = new HTMLHead(); 
		root.appendChild(head);
		HTMLMeta meta = new HTMLMeta(getEncoding(isIE,   locale)) ;
		
		head.appendChild(meta) ;
		HTMLStyle style = new HTMLStyle( ) ;
		String reportCSS = getReportCSS(CSS_FILE_NAV); 
		style.appendChild(new HTMLTextNode(reportCSS)) ;  
		head.appendChild(style) ;
		HTMLScript script = new HTMLScript() ;
		script.appendChild(new HTMLTextNode(JS)) ;
		head.appendChild(script) ;
		
		HTMLBody body = new HTMLBody();
	 
		//"category" is fixed ...
		HTMLUL treeRoot= new HTMLUL("category");
		if(index!=null){
			for (Iterator iterator = index.iterator(); iterator.hasNext();) {
				IndexItem indexItem = (IndexItem) iterator.next();
				
				HTMLElement li = createIndexItemElement(  indexItem,true);  
				treeRoot.appendChild(li) ;
			}
		}
		body.appendChild(treeRoot) ;
		root.appendChild(body);
			return root.toHTML().toString();
	}

 

	/**
	 * @param indexItem
	 * @return
	 * @throws Exception 
	 */
	private static HTMLElement createIndexItemElement(IndexItem indexItem,boolean isRoot) throws Exception { 
		HTMLLI result= new HTMLLI();  
		if(indexItem.getChildren()==null||indexItem.getChildren().size()==0){
			/**
			 * <li>
				<a href="content.html#Node  1overview" target="content">Overview</a>
			</li>
			 * */
			if(indexItem.getTitle()!=null){
				HTMLElement li= new HTMLLI();  
				if(isRoot==true){
					li = new HTMLSpan();
				}
				HTMLElement anchor =new HTMLAnchor(INDEX_TREE_HREF_PREFIX+indexItem.getRefID(),"content");
				anchor.appendChild(new HTMLTextNode(indexItem.getTitle())) ;
				li.appendChild( anchor) ;
				if(isRoot==false){
					return li;
				}
				result.appendChild(li) ;  
			}else{
				return null;
			}
		 
		}else{
			/**
			 * <span>
			<a href="content.html#Node  1" target="content">Node  1:DB table</a>
		</span>
		<ul>
			<li>
				<a href="content.html#Node  1overview" target="content">Overview</a>
			</li>
			<li>
				<span><a href="content.html#Output1" target="content">Output</a>
				</span>
				<ul></ul>
			</li>
		</ul>
		*/
			//span...
			
			HTMLSpan span= new HTMLSpan();   
			HTMLElement anchor =new HTMLAnchor(INDEX_TREE_HREF_PREFIX+indexItem.getRefID(),"content");
			anchor.appendChild(new HTMLTextNode(indexItem.getTitle())) ;
			span.appendChild( anchor) ;
			result.appendChild(span) ;  
			List<IndexItem> children = indexItem.getChildren();
			
			HTMLElement ul = new HTMLUL(null);
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				IndexItem child = (IndexItem) iterator.next();
				HTMLElement li = createIndexItemElement(child,false);
				if(li!=null){
					ul.appendChild(li) ;
				}
			}
			//avoid the null index
			 if(ul.getChildren()!=null&&ul.getChildren().size()>0){
				 result.appendChild(ul) ;
			}
			  
		}
		return result; 
	 
	}
   

}

/**
 * ClassName TableEntity.java

 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.view.ui.dataset;
import java.util.ArrayList;
import java.util.List;

 

 

/**
 * @author jimmy
 *
 */
public class TableEntity {
	
	public static final int ADD_SELECTION_LINSTENER_FOR_SPLITMODEL = 0;

//	List<Image> catetoryImageList=new ArrayList<Image>();
//	List<Color[]> catetoryColorList=new ArrayList<Color[]>();
//	List<ArrayList<Image>> scaleImageList=new ArrayList<ArrayList<Image>>();
	private String[] columnHeaders;
	//data rows!
	private List<String[]> item = new ArrayList<String[]>();
	private String[] columnBar;
	private String countColumnName;
	private double maxValue =0;
//	private Color[] color;
	int height = 15;
	int categorylength =0;
	int maxHeight = 0;
	
	private int style=-1;
	private String system;
	private String url;
	private String userName;
	private String password;
	private String schema;
	private String title;
	private List<TableEntity> tableEntityList;//only for split model
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	private List<String[]> sortColumnList = new ArrayList<String[]>();
	public void addSortColumn(String columnName,String dataType){
		sortColumnList.add(new String[]{columnName,dataType});
	}
	public List<String[]> getSortColumnList(){
		return sortColumnList;
	}
	
	public String[] getColumn() {
		return columnHeaders;
	}
	public void setColumn(String[] tableColumn) {
		this.columnHeaders = tableColumn;
	}
	public List<String[]> getItem() {
		return item;
	}
	public void addItem(String[] tableItem) {
		this.item.add(tableItem);
	}
	public void clearItem(){
		item.clear();
	}
	/**
	 * show color bar on column
	 * @param tableColumnBar
	 */
	public void setColumnBar(String[] tableColumnBar){
		columnBar = tableColumnBar;
	}
	public String[] getColumnBar(){
		return columnBar;
	}
	/**
	 * count column value max value
	 * @param columnName
	 * @return double
	 */
	public double getMaxValue(String columnName){
		if(countColumnName != null && countColumnName.equals(columnName)){
			return maxValue;
		}else{
			countColumnName =columnName;
		}
		int index =-1;
		double tempValue =0;
		for(int i=0;i<getColumn().length;i++){
			if(getColumn()[i].equals(columnName)){
				index = i;
				break;
			}
		}
		for(int i=0;i<getItem().size();i++){
			if(Double.valueOf(getItem().get(i)[index].toString())>tempValue){
				tempValue = Double.valueOf(getItem().get(i)[index].toString());
			}
		}
		maxValue = tempValue;
		return tempValue;
	}
	
	private String columnColorCategory;
	public String getColumnColorCategory() {
		return columnColorCategory;
	}
	/**
	 * set column show color category diagram
	 * @param columnColorCategory
	 */
	public void setColumnColorCategory(String columnColorCategory) {
		this.columnColorCategory = columnColorCategory;
	}
	public String[] getColumnColorScale() {
		return columnColorScale;
	}
	/**
	 * set column show color scale diagram
	 * @param columnColorScale
	 */
	public void setColumnColorScale(String[] columnColorScale) {
		this.columnColorScale = columnColorScale;
	}

	private String[] columnColorScale;
	/**
	 * create random color
	 * @return
	 */
	final int characterHeight = 15; 
//	public Image createRandomColorCatetoryImage(int row, Display display ,int width,String[] items){ 
//	
//		if(catetoryImageList.size()>=row+1&&catetoryImageList.get(row)!=null){
//			setColorArray(catetoryColorList.get(row));
//			return catetoryImageList.get(row);
//		}
//		Image img = new Image(display, width,getMaxHeight());
//		int dheight = 0;//(getMaxHeight()-items.length*20)/2;
//		GC gc = new GC(img);
//		Color[] colors = UIUtilityCommon.getRandomColor(items.length);
//		for(int i=0;i<items.length;i++){
//			gc.setBackground(colors[i]);
//			gc.fillOval(0,(dheight+i*characterHeight), characterHeight, characterHeight);
// 			 gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//			gc.drawString(items[i], 25, i*characterHeight);
//		}
//		catetoryImageList.add(row,img);
//		gc.dispose();		
//		setColorArray(colors);
//		catetoryColorList.add(colors);
//		return img;
//	}
	
	
	int distanceWidth = 20;
	int distanceHeight = 20;
//	public Image createCategoryScaleImage(int rowNumber, int colNumber, Display display,int width,String[] items){
//		colNumber=colNumber-3;// filter the start 2
//		if(scaleImageList.size()<rowNumber+1){
//			scaleImageList.add(rowNumber,new ArrayList<Image>());
//		}
//	 
//		if(scaleImageList.get(rowNumber).size()>colNumber&&scaleImageList.get(rowNumber).get(colNumber)!=null){
//			 return scaleImageList.get(rowNumber).get(colNumber);
//		}
//		Color[] colors = getColorArray();
//		int height = getMaxHeight();
//		
//		Image img = new Image(display, width,height); 
//		GC gc = new GC(img);
//		double maxScaleValue = 0;
//		for(int i=0;i<items.length;i++){
//			maxScaleValue += Double.valueOf(items[i]);;
//		}
//		int currentHeight = 0;
//		width -=distanceWidth;
//		height -=distanceHeight;
//	 
//		for(int i=0;i<items.length;i++){
//			double currentValue = Double.valueOf(items[i]);
//			int scaleHeight = (int) (height/maxScaleValue*currentValue);
//			gc.setBackground(colors[i]);
//			gc.fillRectangle(distanceWidth/2,currentHeight+distanceHeight/2, width, scaleHeight);
//			currentHeight+=scaleHeight;
//		}
//		scaleImageList.get(rowNumber).add(colNumber,img);
//		gc.dispose();
//		return img;
//	}
//	
//
//	private void setColorArray(Color[] cc){
//		color = cc;
//	}
//	private Color[] getColorArray(){
//		return color;
//	}

	public int getMaxHeight(){
		if(maxHeight >0){
			return maxHeight;
		}
		if(getColumnColorCategory() != null && !getColumnColorCategory().isEmpty()){
			for(String[] temp:getItem()){
				String[] catetorys = temp[1].split(",");
				if(catetorys.length>categorylength){
					categorylength = catetorys.length; 
				}
			}
		}
		maxHeight = height*categorylength;
		return maxHeight;
	}

	public int getMaxCategoryWidth(){
		int maxWidth = 0;
		for (int row=0; row<getItem().size(); row++) {
			for(int i=0;i<getColumn().length;i++){
				String columnName = getColumn()[i];
				if(getColumnColorCategory() != null && getColumnColorCategory().equals(columnName)){
					String[] categorys = getItem().get(row)[i].split(",");
					for(String tt:categorys){
						int width = 25+(tt.length()*8);
						if(maxWidth < width){
							maxWidth = width;
						}
					}
				}
			}
		}
		return maxWidth;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	private String tableName=null;
	private String[] oriColumns;
	public String[] getOriColumns() {
		return oriColumns;
	}
 
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}

	public int getRowNumbers(){
		return item.size();
	}
	/**
	 * @param oriColumns
	 */
	public void setOriginalColumns(String[] oriColumns) {
		this.oriColumns=oriColumns;
		
	}
	public int getStyle() {
		return style;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	public List<TableEntity> getTableEntityList() {
		return tableEntityList;
	}
	public void setTableEntityList(List<TableEntity> tableEntityList) {
		this.tableEntityList = tableEntityList;
	}
	
}

package com.alpine.datamining.api.impl.visual.dataset;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.ui.RectangleEdge;

public class CustomBoxAndWhiskerRenderer extends BoxAndWhiskerRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2242564369200413439L;
	
	public void drawVerticalItem(Graphics2D g2,
			CategoryItemRendererState state, Rectangle2D dataArea,
			CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis,
			CategoryDataset dataset, int row, int column) {
		/*  687 */     BoxAndWhiskerCategoryDataset bawDataset = (BoxAndWhiskerCategoryDataset)dataset;
		/*      */ 
		/*  690 */     double categoryEnd = domainAxis.getCategoryEnd(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
		/*      */ 
		/*  692 */     double categoryStart = domainAxis.getCategoryStart(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
		/*      */ 
		/*  694 */     double categoryWidth = categoryEnd - categoryStart;
		/*      */ 
		/*  696 */     double xx = categoryStart;
		/*  697 */     int seriesCount = getRowCount();
		/*  698 */     int categoryCount = getColumnCount();
		/*      */ 
		/*  700 */     if (seriesCount > 1) {
		/*  701 */       double seriesGap = dataArea.getWidth() * getItemMargin() / (categoryCount * (seriesCount - 1));
		/*      */ 
		/*  703 */       double usedWidth = state.getBarWidth() * seriesCount + seriesGap * (seriesCount - 1);
		/*      */ 
		/*  707 */       double offset = (categoryWidth - usedWidth) / 2.0D;
		/*  708 */       xx = xx + offset + row * (state.getBarWidth() + seriesGap);
		/*      */     }
		/*      */     else
		/*      */     {
		/*  713 */       double offset = (categoryWidth - state.getBarWidth()) / 2.0D;
		/*  714 */       xx += offset;
		/*      */     }
		/*      */ 
		/*  717 */     double yyAverage = 0.0D;
		/*      */ 
		/*  720 */     Paint itemPaint = getItemPaint(row, column);
		/*  721 */     g2.setPaint(itemPaint);
		/*  722 */     Stroke s = getItemStroke(row, column);
		/*  723 */     g2.setStroke(s);
		/*      */ 
		/*  725 */     double aRadius = 0.0D;
		/*      */ 
		/*  727 */     RectangleEdge location = plot.getRangeAxisEdge();
		/*      */ 
		/*  729 */     Number yQ1 = bawDataset.getQ1Value(row, column);
		/*  730 */     Number yQ3 = bawDataset.getQ3Value(row, column);
		/*  731 */     Number yMax = bawDataset.getMaxRegularValue(row, column);
		/*  732 */     Number yMin = bawDataset.getMinRegularValue(row, column);
		/*  733 */     Shape box = null;
		/*  734 */     if ((yQ1 != null) && (yQ3 != null) && (yMax != null) && (yMin != null))
		/*      */     {
		/*  736 */       double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea, location);
		/*      */ 
		/*  738 */       double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea, location);
		/*      */ 
		/*  740 */       double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea, location);
		/*      */ 
		/*  742 */       double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea, location);
		/*      */ 
		/*  744 */       double xxmid = xx + state.getBarWidth() / 2.0D;
		/*      */ 
		/*  747 */       g2.draw(new Line2D.Double(xxmid, yyMax, xxmid, yyQ3));
		/*      */ 			if(state.getBarWidth()>100){
								g2.draw(new Line2D.Double(xx+(state.getBarWidth()/2-50), yyMax, xx + state.getBarWidth()-(state.getBarWidth()/2-50), yyMax));
								g2.draw(new Line2D.Double(xx+(state.getBarWidth()/2-50), yyMin, xx + state.getBarWidth()-(state.getBarWidth()/2-50), yyMin));
							}else{
								g2.draw(new Line2D.Double(xx, yyMax, xx + state.getBarWidth(), yyMax));
								g2.draw(new Line2D.Double(xx, yyMin, xx + state.getBarWidth(), yyMin));
							}
		
		/*      */ 
		/*  752 */       g2.draw(new Line2D.Double(xxmid, yyMin, xxmid, yyQ1));
		
		/*      */ 			if(state.getBarWidth()>100){
			/*  757 */       box = new Rectangle2D.Double(xx+(state.getBarWidth()/2-50), Math.min(yyQ1, yyQ3), 100, Math.abs(yyQ1 - yyQ3));	
							}else{
			/*  757 */       box = new Rectangle2D.Double(xx, Math.min(yyQ1, yyQ3), state.getBarWidth(), Math.abs(yyQ1 - yyQ3));
							}
		
		/*      */ 
		/*  759 */       if (getFillBox()) {
		/*  760 */         g2.fill(box);
		/*      */       }
		/*  762 */       g2.setStroke(getItemOutlineStroke(row, column));
//		/*  763 */       g2.setPaint(getItemOutlinePaint(row, column));
						 g2.setPaint(itemPaint);
		/*  764 */       g2.draw(box);
		/*      */     }
		/*      */ 
//		/*  767 */     g2.setPaint(getArtifactPaint());
					   g2.setPaint(itemPaint);
		/*      */ 
		/*  770 */     if (isMeanVisible()) {
		/*  771 */       Number yMean = bawDataset.getMeanValue(row, column);
		/*  772 */       if (yMean != null) {
		/*  773 */         yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(), dataArea, location);
		/*      */ 
		/*  775 */         aRadius = state.getBarWidth() / 4.0D;
		/*      */ 
		/*  778 */         if ((yyAverage > dataArea.getMinY() - aRadius) && (yyAverage < dataArea.getMaxY() + aRadius))
		/*      */         {
								Ellipse2D.Double avgEllipse = null;
								if(aRadius * 2.0D>10){
									avgEllipse = new Ellipse2D.Double(xx + aRadius+(aRadius-5), yyAverage - aRadius+(aRadius-5), 10, 10);/*      */
								}else{
									avgEllipse = new Ellipse2D.Double(xx + aRadius, yyAverage - aRadius, aRadius * 2.0D, aRadius * 2.0D);/*      */							
								}
//		 
		/*  783 */           g2.fill(avgEllipse);
		/*  784 */           g2.draw(avgEllipse);
		/*      */         }
		/*      */       }
		/*      */ 
		/*      */     }
		/*      */ 
		/*  790 */     if (isMedianVisible()) {
		/*  791 */       Number yMedian = bawDataset.getMedianValue(row, column);
							if (yMedian != null) {
								double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(), dataArea, location);
								if(state.getBarWidth()>100){
									g2.draw(new Line2D.Double(xx+(state.getBarWidth()/2-50), yyMedian, xx + state.getBarWidth()-(state.getBarWidth()/2-50), yyMedian));
								}else{
									g2.draw(new Line2D.Double(xx, yyMedian, xx + state.getBarWidth(), yyMedian));
								}
								
							}
		/*      */ 
		/*      */     }
		/*      */ 
		/*  801 */     double maxAxisValue = rangeAxis.valueToJava2D(rangeAxis.getUpperBound(), dataArea, location) + aRadius;
		/*      */ 
		/*  803 */     double minAxisValue = rangeAxis.valueToJava2D(rangeAxis.getLowerBound(), dataArea, location) - aRadius;
		/*      */ 
		/*  806 */     g2.setPaint(itemPaint);
		/*      */ 
		/*  809 */     double oRadius = state.getBarWidth() / 3.0D;
		/*  810 */     List outliers = new ArrayList();
		/*  811 */     OutlierListCollection outlierListCollection = new OutlierListCollection();
		/*      */ 
		/*  817 */     List yOutliers = bawDataset.getOutliers(row, column);
		/*  818 */     if (yOutliers != null) {
		/*  819 */       for (int i = 0; i < yOutliers.size(); ++i) {
		/*  820 */         double outlier = ((Number)yOutliers.get(i)).doubleValue();
		/*  821 */         Number minOutlier = bawDataset.getMinOutlier(row, column);
		/*  822 */         Number maxOutlier = bawDataset.getMaxOutlier(row, column);
		/*  823 */         Number minRegular = bawDataset.getMinRegularValue(row, column);
		/*  824 */         Number maxRegular = bawDataset.getMaxRegularValue(row, column);
		/*  825 */         if (outlier > maxOutlier.doubleValue()) {
		/*  826 */           outlierListCollection.setHighFarOut(true);
		/*      */         }
		/*  828 */         else if (outlier < minOutlier.doubleValue()) {
		/*  829 */           outlierListCollection.setLowFarOut(true);
		/*      */         }
		/*  831 */         else if (outlier > maxRegular.doubleValue()) {
		/*  832 */           double yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
		/*  834 */           outliers.add(new Outlier(xx + state.getBarWidth() / 2.0D, yyOutlier, oRadius));
		/*      */         }
		/*  837 */         else if (outlier < minRegular.doubleValue()) {
		/*  838 */           double yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea, location);
		/*      */ 
		/*  840 */           outliers.add(new Outlier(xx + state.getBarWidth() / 2.0D, yyOutlier, oRadius));
		/*      */         }
		/*      */ 
		/*  843 */         Collections.sort(outliers);
		/*      */       }
		/*      */ 
		/*  848 */       for (Iterator iterator = outliers.iterator(); iterator.hasNext(); ) {
		/*  849 */         Outlier outlier = (Outlier)iterator.next();
		/*  850 */         outlierListCollection.add(outlier);
		/*      */       }
		/*      */ 
		/*  853 */       Iterator iterator = outlierListCollection.iterator();
		/*  854 */       while (iterator.hasNext()) {
		/*  855 */         OutlierList list = (OutlierList)iterator.next();
		/*  856 */         Outlier outlier = list.getAveragedOutlier();
		/*  857 */         Point2D point = outlier.getPoint();
		/*      */ 
		/*  859 */         if (list.isMultiple()) {
		/*  860 */           drawMultipleEllipse(point, state.getBarWidth(), oRadius, g2);
		/*      */         }
		/*      */         else
		/*      */         {
		/*  864 */           drawEllipse(point, oRadius, g2);
		/*      */         }
		/*      */ 
		/*      */       }
		/*      */ 
		/*  869 */       if (outlierListCollection.isHighFarOut()) {
		/*  870 */         drawHighFarOut(aRadius / 2.0D, g2, xx + state.getBarWidth() / 2.0D, maxAxisValue);
		/*      */       }
		/*      */ 
		/*  874 */       if (outlierListCollection.isLowFarOut()) {
		/*  875 */         drawLowFarOut(aRadius / 2.0D, g2, xx + state.getBarWidth() / 2.0D, minAxisValue);
		/*      */       }
		/*      */ 
		/*      */     }
		/*      */ 
		/*  880 */     if ((state.getInfo() != null) && (box != null)) {
		/*  881 */       EntityCollection entities = state.getEntityCollection();
		/*  882 */       if (entities != null)
		/*  883 */         addItemEntity(entities, dataset, row, column, box);
		/*      */     }
	}
				private void drawMultipleEllipse(Point2D point, double boxWidth, double oRadius, Graphics2D g2)
	/*      */   {
	/*  913 */     Ellipse2D dot1 = new Ellipse2D.Double(point.getX() - (boxWidth / 2.0D) + oRadius, point.getY(), oRadius, oRadius);
	/*      */ 
	/*  915 */     Ellipse2D dot2 = new Ellipse2D.Double(point.getX() + boxWidth / 2.0D, point.getY(), oRadius, oRadius);
	/*      */ 
	/*  917 */     g2.draw(dot1);
	/*  918 */     g2.draw(dot2);
	/*      */   }
				
				private void drawEllipse(Point2D point, double oRadius, Graphics2D g2)
				/*      */   {
				/*  897 */     Ellipse2D dot = new Ellipse2D.Double(point.getX() + oRadius / 2.0D, point.getY(), oRadius, oRadius);
				/*      */ 
				/*  899 */     g2.draw(dot);
				/*      */   }
	
				private void drawHighFarOut(double aRadius, Graphics2D g2, double xx, double m)
				/*      */   {
				/*  931 */     double side = aRadius * 2.0D;
				/*  932 */     g2.draw(new Line2D.Double(xx - side, m + side, xx + side, m + side));
				/*  933 */     g2.draw(new Line2D.Double(xx - side, m + side, xx, m));
				/*  934 */     g2.draw(new Line2D.Double(xx + side, m + side, xx, m));
				/*      */   }
				
				 private void drawLowFarOut(double aRadius, Graphics2D g2, double xx, double m)
				 /*      */   {
				 /*  947 */     double side = aRadius * 2.0D;
				 /*  948 */     g2.draw(new Line2D.Double(xx - side, m - side, xx + side, m - side));
				 /*  949 */     g2.draw(new Line2D.Double(xx - side, m - side, xx, m));
				 /*  950 */     g2.draw(new Line2D.Double(xx + side, m - side, xx, m));
				 /*      */   }
}

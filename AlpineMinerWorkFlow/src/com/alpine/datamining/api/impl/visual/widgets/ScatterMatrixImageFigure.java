package com.alpine.datamining.api.impl.visual.widgets;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.swt.graphics.Image;

import com.alpine.datamining.api.impl.db.table.ScatterMatrixColumnPairs;

public class ScatterMatrixImageFigure extends ImageFigure {
	
	private ScatterMatrixColumnPairs columnPair;

	public ScatterMatrixImageFigure(Image image) {
		super(image);
	}

	public ScatterMatrixColumnPairs getColumnPair() {
		return columnPair;
	}

	public void setColumnPair(ScatterMatrixColumnPairs columnPair) {
		this.columnPair = columnPair;
	}
	
}

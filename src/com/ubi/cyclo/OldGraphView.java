package com.ubi.cyclo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OldGraphView extends View{

	private int measurementCount = 0;
	private float upperBound = 0.0f;
	private float lowerBound = 0.0f;
	
	int viewWidth;
	int viewHeight;

	private Paint paint = null;	
	private ArrayList<Float> dataListPlan = null;
	private ArrayList<Float> dataListLive = null;
	
    public OldGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
		
        dataListPlan = new ArrayList<Float>();
        dataListLive = new ArrayList<Float>();
        
        this.setBackgroundColor(Color.BLACK);
        
        // Configure the paint
		paint = new Paint();
		paint.setStrokeWidth(2);
		paint.setAntiAlias(true);
    }

    @Override
	public void onDraw(Canvas canvas) {	
	
		float width = viewWidth;
		float height = viewHeight;
		
		// Leave some space at the bottom
		height -= 10;
		
		// We draw the axes in white
		paint.setColor(Color.WHITE);
		
		// Draw the title
		paint.setTextSize(20);
//		canvas.drawText("Overview", (width / 2) - 60, 30, paint);
		paint.setTextSize(12);
		
		// Draw the x and y axes
		canvas.drawLine(38, 30, 38, height + 3, paint);
		canvas.drawLine(38, height + 1, width, height + 1, paint);
		for (int i = 0; i < 10; i++) {
			
			// Draw the markers and marker values
			String text = Float.toString(lowerBound + i * (upperBound - lowerBound) / 10);
			
			float y = height - i * (height / 10);
			canvas.drawLine(30, y, 38, y, paint);
			canvas.drawText(text, 2, y, paint);
		}
		
		// We used 40 pixels for the markers
		width -= 40;
		
		RectF rect = null;
		paint.setColor(Color.rgb(100, 100, 100));
		
		float delta = width / measurementCount;
		float relHeight = height / (upperBound - lowerBound);
		
		int i = 0;
		paint.setColor(Color.DKGRAY);
		for (Float p : dataListPlan) {
			
			// Draw the bar for this data point
			rect = new RectF(40 + i * delta, height - (p - lowerBound)*relHeight, 40 + ++i * delta, height);
			
			canvas.drawRect(rect, paint);
		}
		
		i=0;
		paint.setColor(Color.GRAY);
		for (Float p : dataListLive) {
			
			// Draw the bar for this data point
			rect = new RectF(40 + i * delta, height - (p - lowerBound)*relHeight, 40 + ++i * delta, height);
			
			canvas.drawRect(rect, paint);
		}
		
	}
	
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewWidth = xNew;
        viewHeight = yNew;
    }
	
	public void addValueToPlan(Float value) {
				
		// Add the new value to the list
		dataListPlan.add(value);
		if (dataListPlan.size() > measurementCount) {
			dataListPlan.remove(0);
		}
		if(value<getLowerBound())
			setLowerBound( Math.round(value-0.10f*(getUpperBound()-getLowerBound())) ); //10% margin upwards
		if(value>getUpperBound())
			setUpperBound( Math.round(value+0.10f*(getUpperBound()-getLowerBound())) ); //10% margin downwards
		
		// Mark this view for redrawing
		// Use postInvalidate() since this is not called on the main thread
		postInvalidate();
	}
	
	public void addValueToLive(Float value) {
		
		// Add the new value to the list
		if (dataListLive.size() > measurementCount) {
			dataListLive.remove(0);
		}
		dataListLive.add(value);
		if(value<getLowerBound())
			setLowerBound( Math.round(value-0.10f*(getUpperBound()-getLowerBound())) ); //10% margin upwards
		if(value>getUpperBound())
			setUpperBound( Math.round(value+0.10f*(getUpperBound()-getLowerBound())) ); //10% margin downwards
		
		// Mark this view for redrawing
		// Use postInvalidate() since this is not called on the main thread
		postInvalidate();
	}
	
	public int getMeasurementCount() {
		return measurementCount;
	}
	
	public void setMeasurementCount(int measurementCount) {
		this.measurementCount = measurementCount;
		
		// Resize the list for the new number of measurements
		while (measurementCount > dataListPlan.size()) {
        	dataListPlan.add(lowerBound);
		}
		while (measurementCount > dataListLive.size()) {
        	dataListLive.add(lowerBound);
		}
		while (measurementCount < dataListPlan.size()) {
			dataListPlan.remove(0);
		}
		while (measurementCount < dataListLive.size()) {
			dataListLive.remove(0);
		}
	}

	public float getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(float f) {
		this.upperBound = f;
	}

	public float getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(float f) {
		this.lowerBound = f;
	}
}


package com.jjoe64.graphview;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
	

public class CycloGraphView extends GraphView {
		private final Paint paintBackground;
		private boolean drawBackground;

		public CycloGraphView(Context context, AttributeSet attrs) {
			super(context, attrs);
			
			paintBackground = new Paint();
			paintBackground.setARGB(255, 20, 40, 60);
			paintBackground.setStrokeWidth(4);
		}
		
		public CycloGraphView(Context context, String title) {
			super(context, title);

			paintBackground = new Paint();
			paintBackground.setARGB(255, 196, 177, 53);   //255, 20, 40, 60
			paintBackground.setStrokeWidth(4);
		}

		@Override
		public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style) {
			// draw background
			
			//int a = (style.terraincolor & 0xFF000000)>>6;
			//int r = (style.terraincolor & 0x00FF0000)>>4;			
			//int g = (style.terraincolor & 0x0000FF00);
			//int b = (style.terraincolor & 0x000000FF)>>1;
			//Log.d("bla", "style="+Integer.toString(style.terraincolor));
			//Log.d("bla", "argb="+Integer.toString(a)+","+Integer.toString(r)+","+Integer.toString(g)+","+Integer.toString(b));
			paintBackground.setColor(style.terraincolor);
			//paintBackground.setARGB(255, 0, 177,0); 


			if(style.isPOI==true)
			{
				for (int i = 0; i < values.length; i++) {
					double valX = values[i].valueX - minX;
					double ratX = valX / diffX;
					double x = graphwidth * ratX;
					float X = (float) x + (horstart + 1);				
					canvas.drawLine(X, border, X, graphheight+border, paintBackground);
				}				
				return;
			}
			
			
			double lastEndY = 0;
			double lastEndX = 0;
			if (drawBackground) {
				float startY = graphheight + border;
				for (int i = 0; i < values.length; i++) {
					double valY = values[i].valueY - minY;
					double ratY = valY / diffY;
					double y = graphheight * ratY;

					double valX = values[i].valueX - minX;
					double ratX = valX / diffX;
					double x = graphwidth * ratX;

					float endX = (float) x + (horstart + 1);
					float endY = (float) (border - y) + graphheight +2;

					if (i > 0) {
						// fill space between last and current point
						double numSpace = ((endX - lastEndX) / 3f) +1;
						for (int xi=0; xi<numSpace; xi++) {
							float spaceX = (float) (lastEndX + ((endX-lastEndX)*xi/(numSpace-1)));
							float spaceY = (float) (lastEndY + ((endY-lastEndY)*xi/(numSpace-1)));

							// start => bottom edge
							float startX = spaceX;

							// do not draw over the left edge
							if (startX-horstart > 1) {		
												
									canvas.drawLine(startX, startY, spaceX, spaceY, paintBackground);
								
							}
						}
					}

					lastEndY = endY;
					lastEndX = endX;
				}
			}

			// draw data
			paint.setStrokeWidth(style.thickness);
			paint.setColor(style.color);

			lastEndY = 0;
			lastEndX = 0;
			for (int i = 0; i < values.length; i++) {
				double valY = values[i].valueY - minY;
				double ratY = valY / diffY;
				double y = graphheight * ratY;

				double valX = values[i].valueX - minX;
				double ratX = valX / diffX;
				double x = graphwidth * ratX;

				if (i > 0) {
					float startX = (float) lastEndX + (horstart + 1);
					float startY = (float) (border - lastEndY) + graphheight;
					float endX = (float) x + (horstart + 1);
					float endY = (float) (border - y) + graphheight;

					canvas.drawLine(startX, startY, endX, endY, paint);
				}
				lastEndY = y;
				lastEndX = x;
			}
		}

		public boolean getDrawBackground() {
			return drawBackground;
		}

		/**
		 * @param drawBackground true for a light blue background under the graph line
		 */
		public void setDrawBackground(boolean drawBackground) {
			this.drawBackground = drawBackground;
		}
	}




package com.ubi.cyclo;

import java.util.ArrayList;

public class RunningAverageFilter {
	private int N;
	private double oldMean;
	private ArrayList<Double> values;
	
	RunningAverageFilter(int n){
		setN(n);
		oldMean = 0.0f;
		values = new ArrayList<Double>();		
		for (int i = 0; i<N; i++) values.add( Double.valueOf(0.0f) );
	}
	
	public int getN(){return N;}
	public void setN(int n){ N = n;}
	
	public double filter(double newValue){
		double newMean = oldMean - values.get(0)/N + newValue/N;
		
		values.remove(0);
		values.add(newValue);
		oldMean = newMean;
		
		return newMean; 	
		
	}

    public void reset(){
        for (int i = 0; i<N; i++) values.set(i, Double.valueOf(0.0f) );
        oldMean = 0.0f;
    }
	
}

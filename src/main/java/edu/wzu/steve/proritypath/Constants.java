package edu.wzu.steve.proritypath;



import java.util.ArrayList;  
import java.util.Collections;  
import java.util.Comparator;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map; 
  

final class Constants {  

    public final static int MAX_V = 1000000000;  

}  

final class Prority {
	public final  static int MAX_P = 10;
		
	
}

  

final class Station {  

  

    public Station(final String name) {  

        this.name = name;  

        disSum = Constants.MAX_V;  
        

        previous = null;  

    }  

  

    public String getName() {  

        return this.name;  

    }  

    public int getDisSum() {  

        return this.disSum;  

    }  

    public void setDisSum(final int disSum) {  

        this.disSum = disSum;  

    }  

    public Station getPrevious() {  

        return this.previous;  

    }  

    public void setPrevious(final Station previous) {  

        this.previous = previous;  

    }  

  

    @Override  

    public String toString() {  
        return this.getClass().getSimpleName() + "[name=" + name + ", disSum=" + Integer.toString(disSum) + ", previous=" + (previous == null ? "null" : previous.name) + "]";  
    }  

  

    private final String name;  
    private int disSum;  
    private Station previous;  

}  

  

final class StationDisComparator implements Comparator<Station> {  
    @Override  
    public int compare(final Station s1, final Station s2) {  
        return s1.getDisSum() - s2.getDisSum();  

    }  

}  

  

final class Line {  

  

    public Line(final String start, final String end, final int dis) {  
        this.start = start;  
        this.end = end;  
        this.dis = dis;  

    }  

      

    public String getStart() {  
        return this.start;  

    }  

    public String getEnd() {  
        return this.end;  

    }  

    public int getDis() {  
        return this.dis;  

    }  

    private final String start;  
    private final String end;  
    private final int dis;  

}  


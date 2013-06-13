package cyclo.gpxparser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.location.Location;
import android.util.Log;


public class GPXParser
{
private static final SimpleDateFormat gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

public static List<Location> getPoints(Context myContext, String gpxFileName, boolean fakeTheValues)
{
    List<Location> points = null;
    try
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
  
        //FileInputStream fis = new FileInputStream(gpxFile);
		AssetManager assetManager = myContext.getAssets();
		InputStream inputStream = null;
        try {
            inputStream = assetManager.open(gpxFileName);
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        Document dom = builder.parse(inputStream);
        Element root = dom.getDocumentElement();
        NodeList items = root.getElementsByTagName("trkpt");

        points = new ArrayList<Location>();

        Random rnd = new Random();
        
        for(int j = 0; j < items.getLength(); j++)
        {
            Node item = items.item(j);
            NamedNodeMap attrs = item.getAttributes();
            NodeList props = item.getChildNodes();

            Location pt = null;
            
            double realLat = Double.parseDouble(attrs.getNamedItem("lat").getTextContent());
            double realLon = Double.parseDouble(attrs.getNamedItem("lon").getTextContent());
            long realTime = 0;
            double realAlt = 0;
            
            if( fakeTheValues == false ){
            	pt = new Location("cycloReal");
            	pt.setLatitude(realLat);
            	pt.setLongitude(realLon);
            }
            else{ //add small noise to location
            	pt = new Location("cycloFake");
            	pt.setLatitude(realLat + (rnd.nextDouble()-0.5)*2.0/1e6); 
            	pt.setLongitude(realLon + (rnd.nextDouble()-0.5)*2.0/1e6);        
            }
            

            for(int k = 0; k<props.getLength(); k++)
            {
                Node item2 = props.item(k);
                String name = item2.getNodeName();
                if(!name.equalsIgnoreCase("time")) continue;
                try
                {
                	realTime = (getDateFormatter().parse(item2.getFirstChild().getNodeValue())).getTime();
                	
                	if( fakeTheValues == false ){
                		pt.setTime(realTime);
                	}
                	else{ //add noise to time
                		//the code below generates jumpy motion
                		//long fiveMinutes = 1000 * 60 * 5;                				
                		//pt.setTime((getDateFormatter().parse(item2.getFirstChild().getNodeValue())).getTime() + (long)((rnd.nextDouble()-0.5)*fiveMinutes) );	
                		
                		//use slow sinusoid based on position and a constant 5sec delay
                		pt.setTime( realTime + 5*1000 + (long)( Math.sin(0.0001*System.currentTimeMillis())*1000*60 ) );
                	}
                }               
                
                catch(ParseException ex)
                {
                    ex.printStackTrace();
                }
                
                
                //TODO fake values
            }

            for(int y = 0; y<props.getLength(); y++)
            {
                Node item3 = props.item(y);
                String name = item3.getNodeName();
                if(!name.equalsIgnoreCase("ele")) continue;
                
                realAlt = Double.parseDouble(item3.getFirstChild().getNodeValue());
                
                if( fakeTheValues == false ){                	
                	pt.setAltitude(realAlt);
            	}
            	else{ //add +- 1m noise to elevation
            		pt.setAltitude(realAlt + (rnd.nextDouble()-0.5)*1.0);
            	}
            }

            
            //TODO fill in speed
            if (points.size() > 1){
            	Location prevPoint = points.get(points.size()-1);
            	long prevTime = prevPoint.getTime();
            	float[] distance = new float[1];
            	//the following returns the distance in meters.
            	Location.distanceBetween(prevPoint.getLatitude(), prevPoint.getLongitude(), pt.getLatitude(), pt.getLongitude(), distance);
            	float speed = distance[0]/(pt.getTime() - prevTime)*1000; //speed in m/s
            	speed = speed * 3.6f; // speed in km/h
            	pt.setSpeed(speed);
            }
            else{
            	pt.setSpeed(0);
            }
            
            points.add(pt);
            
            //Log.d("Cyclo",pt.toString());

        }

        //fis.close();
    }

    catch(FileNotFoundException e)
    {
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    catch(ParserConfigurationException ex)
    {

    }

    catch (SAXException ex) {
    }

    return points;
}

public static SimpleDateFormat getDateFormatter()
  {
    return (SimpleDateFormat)gpxDate.clone();
  }

}
/*import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class GPXParser {
	
	    // We don't use namespaces
	    private static final String ns = null;
	   
	    public List parse(InputStream in) throws XmlPullParserException, IOException {
	    	
	    	
	        try {
	            XmlPullParser parser = Xml.newPullParser();
	            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	            parser.setInput(in, null);
	            parser.nextTag();
	            return readFeed(parser);
	        } finally {
	            in.close();
	        }
	    }
	 
	    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
	    //The sample app extracts data for the entry tag and its nested tags title, link, and summary.
	    //It looks for elements tagged "entry" as a starting point for recursively processing the feed
	    		    
	    	List entries = new ArrayList();

	        parser.require(XmlPullParser.START_TAG, ns, "feed"); //
	        while (parser.next() != XmlPullParser.END_TAG) {
	            if (parser.getEventType() != XmlPullParser.START_TAG) {
	                continue;
	            }
	            String name = parser.getName();
	            // Starts by looking for the entry tag
	            if (name.equals("entry")) {
	                entries.add(readEntry(parser));
	            } else {
	                skip(parser);
	            }
	        }  
	        return entries;
	    }
	
	
	
}*/

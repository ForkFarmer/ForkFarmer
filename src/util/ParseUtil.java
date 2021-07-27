package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {
	
	final static Pattern quote = Pattern.compile("^\\s*\"((?:[^\"]|(?:\"\"))*?)\"\\s*,");

	public static List<String> parseCsv(String line)
	{
		
		line = line.substring(0, Math.min(line.length(), 1000));
		//if (line.length() > 1000)
//			System.out.println("Parsing: " + line.length());
	    List<String> list = new ArrayList<String>();
	    try {
	    line += ",";

	    for (int x = 0; x < line.length(); x++)
	    {
	        String s = line.substring(x);
	        if (s.trim().startsWith("\""))
	        {
	            Matcher m = quote.matcher(s);
	            if (!m.find())
	                throw new Exception("CSV is malformed");
	            list.add(m.group(1).replace("\"\"", "\""));
	            x += m.end() - 1;
	        }
	        else
	        {
	            int y = s.indexOf(",");
	            if (y == -1)
	                throw new Exception("CSV is malformed");
	            list.add(s.substring(0, y));
	            x += y;
	        }
	    }
	    
	    } catch (Exception e) {
	    	
	    }
	    return list;
	}

}

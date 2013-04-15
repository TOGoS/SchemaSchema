package togos.schemaschema;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PropertyUtil
{
	public static void addAll( Map<Property,Set<Object>> source, Map<Property,Set<Object>> dest ) {
		for( Map.Entry<Property,Set<Object>> e : source.entrySet() ) {
			Set<Object> vs = dest.get(e.getKey());
			if( vs == null ) dest.put(e.getKey(), vs = new TreeSet<Object>() );
			vs.addAll( e.getValue() );
		}
	}
}

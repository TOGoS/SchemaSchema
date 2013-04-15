package togos.schemaschema;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PropertyUtil
{
	public static void addAll( Map<Property,Set<Object>> dest, Map<Property,Set<Object>> source ) {
		for( Map.Entry<Property,Set<Object>> e : source.entrySet() ) {
			Set<Object> vs = dest.get(e.getKey());
			if( vs == null ) dest.put(e.getKey(), vs = new TreeSet<Object>() );
			vs.addAll( e.getValue() );
		}
	}
	
	public static void add( Map<Property,Set<Object>> dest, Property key, Object value ) {
		Set<Object> vs = dest.get(key);
		if( vs == null ) dest.put(key, vs = new TreeSet<Object>() );
		vs.add( value );
	}

	public static boolean hasValue( Map<Property,Set<Object>> propertyValues, Property key, Object value ) {
		Set<Object> vs = propertyValues.get(key);
		return vs != null && vs.contains(value);
	}
	
	public static boolean isTrue( Map<Property,Set<Object>> propertyValues, Property key ) {
		return hasValue( propertyValues, key, Boolean.TRUE );
	}
}

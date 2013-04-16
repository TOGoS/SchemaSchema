package togos.schemaschema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PropertyUtil
{
	//// Modify proprety lists
	
	public static void addAll( Map<Predicate,Set<Object>> dest, Predicate key, Collection<?> values ) {
		if( values.size() == 0 ) return;
		
		Set<Object> vs = dest.get(key);
		if( vs == null ) dest.put(key, vs = new TreeSet<Object>() );
		vs.addAll( values );
	}
	
	public static void addAll( Map<Predicate,Set<Object>> dest, Map<? extends Predicate,? extends Set<? extends Object>> source ) {
		for( Map.Entry<? extends Predicate,? extends Set<? extends Object>> e : source.entrySet() ) {
			Set<Object> vs = dest.get(e.getKey());
			if( vs == null ) dest.put(e.getKey(), vs = new TreeSet<Object>() );
			vs.addAll( e.getValue() );
		}
	}
	
	public static void add( Map<Predicate,Set<Object>> dest, Predicate key, Object value ) {
		Set<Object> vs = dest.get(key);
		if( vs == null ) dest.put(key, vs = new TreeSet<Object>() );
		vs.add( value );
	}
	
	//// Query proprety lists

	public static boolean hasValue( Map<Predicate,? extends Set<?>> properties, Predicate key, Object value ) {
		Set<?> vs = properties.get(key);
		return vs != null && vs.contains(value);
	}
	
	public static boolean isTrue( Map<Predicate,? extends Set<?>> properties, Predicate key ) {
		return hasValue( properties, key, Boolean.TRUE );
	}
	
	public static boolean isMemberOf( SchemaObject obj, Type t ) {
		if( hasValue( obj.getProperties(), Predicates.IS_MEMBER_OF, t ) ) return true;
		for( Type pt : t.getExtendedTypes() ) {
			if( isMemberOf( obj, pt ) ) return true;
		}
		return false;
	}
	
	public static Set<?> getAll(Map<Predicate, ? extends Set<?>> properties, Predicate key ) {
		Set<?> values = properties.get( key );
		if( values == null || values.size() == 0 ) return Collections.emptySet();
		return values;
	}
	
	public static <T> Set<T> getAll(Map<Predicate, ? extends Set<?>> properties, Predicate key, Class<T> klass ) {
		Set<?> values = properties.get( key );
		if( values == null || values.size() == 0 ) return Collections.emptySet();
		
		LinkedHashSet<T> valuesOfTheDesiredType = new LinkedHashSet<T>();
		for( Object v : values ) {
			if( klass.isAssignableFrom(v.getClass()) ) {
				valuesOfTheDesiredType.add(klass.cast(v));
			}
		}
		return valuesOfTheDesiredType;
	}
}

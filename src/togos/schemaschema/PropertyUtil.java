package togos.schemaschema;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PropertyUtil
{
	//// Modify proprety lists
	
	public static void addAll( Map<Predicate,Set<Object>> dest, Predicate key, Collection<?> values ) {
		if( values.size() == 0 ) return;
		
		Set<Object> vs = dest.get(key);
		if( vs == null ) dest.put(key, vs = new LinkedHashSet<Object>() );
		vs.addAll( values );
	}
	
	public static void addAll( Map<Predicate,Set<Object>> dest, Map<? extends Predicate,? extends Set<? extends Object>> source ) {
		for( Map.Entry<? extends Predicate,? extends Set<? extends Object>> e : source.entrySet() ) {
			Set<Object> vs = dest.get(e.getKey());
			if( vs == null ) dest.put(e.getKey(), vs = new LinkedHashSet<Object>() );
			vs.addAll( e.getValue() );
		}
	}
	
	public static void add( Map<Predicate,Set<Object>> dest, Predicate key, Object value ) {
		Set<Object> vs = dest.get(key);
		if( vs == null ) dest.put(key, vs = new LinkedHashSet<Object>() );
		vs.add( value );
	}
	
	//// Query proprety lists

	public static boolean hasValue( Map<Predicate,? extends Set<?>> properties, Predicate key ) {
		Set<?> vs = properties.get(key);
		return vs != null && vs.size() > 0;
	}
	
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

	public static String objectToString(Object o) {
		if( o instanceof SchemaObject && ((SchemaObject)o).getName() != null ) {
			return ((SchemaObject)o).getName();
		} else if( o == Boolean.TRUE ) {
			return "true";
		} else if( o == Boolean.FALSE ) {
			return "false";
		} else {
			return o.toString();
		}
	}
	
	public static String pairToString(Predicate key, Object v) {
		if( v == Boolean.TRUE ) {
			return key.getName();
		} else {
			return key + " @ " + objectToString(v);
		}
	}
	
	public static Set<?> getFirstInheritedValues( SchemaObject obj, Predicate pred ) {
		while( obj != null ) {
			Set<?> values = PropertyUtil.getAll(obj.getProperties(), pred);
			if( values.size() > 0 ) return values;
			
			Set<?> extended = PropertyUtil.getAll(obj.getProperties(), Predicates.EXTENDS);
			
			SchemaObject extendedObj = null;
			for( Object o : extended ) {
				if( o instanceof SchemaObject ) {
					if( extendedObj != null ) {
						throw new RuntimeException( obj.getName()+" extends more than one other SchemaObject; cannot find 'first' inherited value of "+pred.getName() );
					}
					extendedObj = (SchemaObject)o;
				}
			}
			
			obj = extendedObj;
		}
		
		return Collections.emptySet();
	}
	
	public static Object getFirstInheritedValue( SchemaObject obj, Predicate pred ) {
		Set<?> values = getFirstInheritedValues(obj, pred);
		if( values.size() > 1 ) {
			throw new RuntimeException( obj.getName()+" has more than one value for "+pred);
		}
		for( Object v : values )  return v;
		return null;
	}
}

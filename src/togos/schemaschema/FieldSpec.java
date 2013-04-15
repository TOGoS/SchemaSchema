package togos.schemaschema;

import java.util.Map;
import java.util.Set;

import togos.schemaschema.parser.ast.Phrase;

public class FieldSpec extends BaseSchemaObject
{
	public FieldSpec( String name ) {
		super(name);
	}
	
	protected static String toString( Object o ) {
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
	
	public String toString() {
		String s = Phrase.quoteIfNecessary(name);
		for( Map.Entry<Property,Set<Object>> e : propertyValues.entrySet() ) {
			for( Object v : e.getValue() ) {
				if( e.getKey() == Properties.TYPE || v == Boolean.TRUE ) {
					s += " : " + toString(v);
				} else {
					s += " : " + e.getKey().getName() + " @ " + toString(v);
				}
			}
		}
		return s;
	}
}

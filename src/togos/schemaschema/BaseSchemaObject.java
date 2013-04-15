package togos.schemaschema;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BaseSchemaObject implements SchemaObject, Comparable<SchemaObject>
{
	public final String name;
	public final Map<Property,Set<Object>> propertyValues;
	
	public BaseSchemaObject( String name, Map<Property,Set<Object>> propertyValues ) {
		this.name = name;
		this.propertyValues = propertyValues;
	}
		
	public BaseSchemaObject( String name ) {
		this( name, new TreeMap<Property,Set<Object>>() );
	}
	
	@Override public String getName() { return name; }
	@Override public Map<Property, Set<Object>> getPropertyValues() { return propertyValues; }
	
	
	@Override public boolean equals( Object oth ) {
		return oth instanceof SchemaObject && name.equals(((SchemaObject)oth).getName());
	}
	@Override public int compareTo(SchemaObject o) {
		return name.compareTo(o.getName());
	}
}

package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import togos.lang.SourceLocation;

public class BaseSchemaObject implements SchemaObject, Comparable<SchemaObject>
{
	protected final SourceLocation sLoc;
	protected final String name;
	protected String longName;
	public Object scalarValue;
	public final Map<Predicate,Set<SchemaObject>> properties = new LinkedHashMap<Predicate,Set<SchemaObject>>();
	
	public static BaseSchemaObject forScalar( Object scalarValue, SourceLocation sLoc ) {
		BaseSchemaObject obj = new BaseSchemaObject(null, sLoc);
		obj.scalarValue = scalarValue;
		return obj;
	}
	
	public BaseSchemaObject( String name, String longName, SourceLocation sLoc ) {
		this.name = name;
		this.longName = longName;
		this.sLoc = sLoc;
	}
		
	public BaseSchemaObject( String name, SourceLocation sLoc ) {
		this( name, (String)null, sLoc );
	}
	
	public BaseSchemaObject( String name, Type type, SourceLocation sLoc ) {
		this( name, sLoc );
		PropertyUtil.add( properties, Predicates.IS_MEMBER_OF, type );
	}
	
	@Override public SourceLocation getSourceLocation() { return sLoc; }
	@Override public String getName() { return name; }
	@Override public String getLongName() { return longName; }
	@Override public Map<Predicate, Set<SchemaObject>> getProperties() { return properties; }
	
	/**
	 * Used by Type objects to implement the Type interface
	 * Not intended to be useful by non-Type objects.
	 **/
	public Set<Type> getExtendedTypes() {
		return PropertyUtil.getAll( properties, Predicates.EXTENDS, Type.class );
	}
	
	@Override public int compareTo(SchemaObject o) {
		return name.compareTo(o.getName());
	}
	
	@Override public Object getScalarValue() {
		return scalarValue;
	}
	
	@Override public boolean hasScalarValue() {
		return scalarValue != null;
	}
}

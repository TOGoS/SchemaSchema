package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;

public class BaseSchemaObject implements SchemaObject, Comparable<SchemaObject>
{
	protected final SourceLocation sLoc;
	protected String name;
	protected String longName;
	public Object scalarValue;
	public final Map<Predicate,Set<SchemaObject>> properties = new LinkedHashMap<Predicate,Set<SchemaObject>>();
	
	public static final WeakHashMap<Object,BaseSchemaObject> scalarSchemaObjects = new WeakHashMap<Object,BaseSchemaObject>();
	
	public static BaseSchemaObject forScalar( Object scalarValue, String name, Type memberOf, SourceLocation sLoc ) {
		// Do we ALWAYS want to re-use the existing object?
		// Since name, memberof, sLoc may be different, this could be really confusing, so probably not.
		// Doing this now as short-term fix so that things like 'nullable' predicate,
		// when used multiple times, don't cause 'multiple values for predicate' errors.
		// That case might be better served by having a static final TRUE value somewhere.
		synchronized( scalarSchemaObjects ) {
			BaseSchemaObject obj = scalarSchemaObjects.get(scalarValue);
			if( obj == null ) {
				obj = new BaseSchemaObject(name, sLoc);
				obj.scalarValue = scalarValue;
				if( memberOf != null ) PropertyUtil.add(obj.getProperties(), Core.TYPE, memberOf);
				scalarSchemaObjects.put( scalarValue, obj );
			}
			return obj;
		}
	}
	
	public static BaseSchemaObject forScalar( Object scalarValue, SourceLocation sLoc ) {
		return forScalar( scalarValue, scalarValue == null ? "null" : scalarValue.toString(), null, sLoc );
	}
	
	public BaseSchemaObject( String name, String longName, SourceLocation sLoc ) {
		this.name = name;
		this.longName = longName;
		this.sLoc = sLoc;
		// TODO: name and long name should show up in properties,
		// but that has some circular dependencies when defining
		// those predicates themselves.
		/*
		if( Core.NAME != null ) {
			PropertyUtil.add( properties, Core.NAME, forScalar(name, sLoc) );
		}
		if( Core.LONGNAME != null ) {
			PropertyUtil.add( properties, Core.LONGNAME, forScalar(longName, sLoc) );
		}
		*/
	}
		
	public BaseSchemaObject( String name, SourceLocation sLoc ) {
		this( name, (String)null, sLoc );
	}
	
	public BaseSchemaObject( String name, Type type, SourceLocation sLoc ) {
		this( name, sLoc );
		setProperty( Core.TYPE, type );
	}
	
	public void setProperty( Predicate pred, SchemaObject value ) {
		assert pred != null;
		assert value != null;
		PropertyUtil.add(properties, pred, value);
		if( pred == Core.NAME && value.getScalarValue() instanceof String ) {
			this.name = (String)value.getScalarValue();
		}
		if( pred == Core.LONGNAME && value.getScalarValue() instanceof String ) {
			this.longName = (String)value.getScalarValue();
		}
	}
	
	@Override public SourceLocation getSourceLocation() { return sLoc; }
	@Override public String getName() {
		return PropertyUtil.getFirstInheritedScalar(this, Core.NAME, String.class, name);
	}
	@Override public String getLongName() {
		return PropertyUtil.getFirstInheritedScalar(this, Core.LONGNAME, String.class, longName);
	}
	@Override public Map<Predicate, Set<SchemaObject>> getProperties() { return properties; }
	
	/**
	 * Used by Type objects to implement the Type interface
	 * Not intended to be useful by non-Type objects.
	 **/
	public Set<Type> getExtendedTypes() {
		return PropertyUtil.getAll( properties, Core.EXTENDS, Type.class );
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

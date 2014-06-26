package togos.schemaschema;

import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;

public class SimpleType extends BaseSchemaObject implements Type
{
	public SimpleType( String name, String longName, Type typeType, SourceLocation sLoc ) {
		super(name, longName, sLoc);
		if( typeType != null ) PropertyUtil.add( properties, Core.TYPE, typeType );
	}
	
	public SimpleType( String name, String longName, SourceLocation sLoc ) {
		this( name, longName, null, sLoc );
	}
	
	public SimpleType( String name, SourceLocation sLoc ) {
		this( name, null, null, sLoc );
	}
}

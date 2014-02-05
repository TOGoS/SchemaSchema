package togos.schemaschema;

import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;

public class SimpleType extends BaseSchemaObject implements Type
{
	public SimpleType( String name, Type typeType, SourceLocation sLoc ) {
		super(name, sLoc);
		if( typeType != null ) PropertyUtil.add( properties, Core.TYPE, typeType );
	}
	
	public SimpleType( String name, SourceLocation sLoc ) {
		this( name, null, sLoc );
	}
}

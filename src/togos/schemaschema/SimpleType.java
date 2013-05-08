package togos.schemaschema;

import togos.lang.SourceLocation;

public class SimpleType extends BaseSchemaObject implements Type
{
	public SimpleType( String name, Type parent, SourceLocation sLoc ) {
		super(name, sLoc);
		PropertyUtil.add( properties, Predicates.IS_MEMBER_OF, parent );
	}
	
	public SimpleType( String name, SourceLocation sLoc ) {
		this( name, null, sLoc );
	}
}

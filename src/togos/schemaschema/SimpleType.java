package togos.schemaschema;

public class SimpleType extends BaseSchemaObject implements Type
{
	public SimpleType( String name, Type parent ) {
		super(name);
		PropertyUtil.add( properties, Predicates.IS_MEMBER_OF, parent );
	}
	
	public SimpleType( String name ) {
		this( name, null );
	}
}

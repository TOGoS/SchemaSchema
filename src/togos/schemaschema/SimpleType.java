package togos.schemaschema;

public class SimpleType extends BaseSchemaObject implements Type
{
	public SimpleType( String name, Type parent ) {
		super(name);
		PropertyUtil.add( propertyValues, Properties.TYPE, parent );
	}
	
	public SimpleType( String name ) {
		this( name, null );
	}
}

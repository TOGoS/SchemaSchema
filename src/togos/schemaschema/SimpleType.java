package togos.schemaschema;

public class SimpleType extends BaseSchemaObject implements Type
{
	protected final Type parent;
	
	public SimpleType( String name, Type parent ) {
		super(name);
		this.parent = parent;
	}
	
	public SimpleType( String name ) {
		this( name, null );
	}
	
	@Override public String getName() { return name; }
	@Override public Type getParentType() { return parent; }
}

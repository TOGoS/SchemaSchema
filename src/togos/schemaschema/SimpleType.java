package togos.schemaschema;

public class SimpleType implements Type
{
	protected final String name, longName;
	protected final Type parent;
	
	public SimpleType( String name, Type parent ) {
		this.name = name;
		this.longName = "schemaschema:"+name;
		this.parent = parent;
	}
	
	public SimpleType( String name ) {
		this( name, null );
	}
	
	@Override public String getName() { return name; }
	@Override public Type getParentType() { return parent; }
}

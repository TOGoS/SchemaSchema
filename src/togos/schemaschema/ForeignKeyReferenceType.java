package togos.schemaschema;


class ForeignKeyReferenceType extends BaseSchemaObject implements Type
{
	protected final Type parentType;
	public final ForeignKeySpec keySpec;
	
	@Override public Type getParentType() { return parentType; }
	
	public ForeignKeyReferenceType( String name, Type parentType, ForeignKeySpec keySpec ) {
		super(name);
		this.parentType = parentType;
		this.keySpec = keySpec;
	}
}

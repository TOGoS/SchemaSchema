package togos.schemaschema;


class ForeignKeyReferenceType implements Type {
	protected final String name;
	protected final Type parentType;
	public final ForeignKeySpec keySpec;
	
	@Override public String getName() { return name; }
	@Override public Type getParentType() { return parentType; }
	
	public ForeignKeyReferenceType( String name, Type parentType, ForeignKeySpec keySpec ) {
		this.name = name;
		this.parentType = parentType;
		this.keySpec = keySpec;
	}
}

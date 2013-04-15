package togos.schemaschema;

import java.util.Set;


class ForeignKeyReferenceType extends BaseSchemaObject implements Type
{
	protected final Type parentType;
	public final ForeignKeySpec keySpec;
	
	public ForeignKeyReferenceType( String name, Type parentType, ForeignKeySpec keySpec ) {
		super(name);
		this.parentType = parentType;
		this.keySpec = keySpec;
	}

	@Override
	public Set<Type> getParentTypes() {
		// TODO Auto-generated method stub
		return null;
	}
}

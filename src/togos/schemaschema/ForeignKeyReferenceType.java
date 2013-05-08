package togos.schemaschema;

import java.util.Set;

import togos.lang.SourceLocation;


class ForeignKeyReferenceType extends BaseSchemaObject implements Type
{
	protected final Type parentType;
	public final ForeignKeySpec keySpec;
	
	public ForeignKeyReferenceType( String name, Type parentType, ForeignKeySpec keySpec, SourceLocation sLoc ) {
		super(name, sLoc);
		this.parentType = parentType;
		this.keySpec = keySpec;
	}

	@Override
	public Set<Type> getExtendedTypes() {
		// TODO Auto-generated method stub
		return null;
	}
}

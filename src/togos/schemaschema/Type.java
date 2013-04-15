package togos.schemaschema;

import java.util.Set;

public interface Type extends SchemaObject
{
	public Set<Type> getParentTypes();
}

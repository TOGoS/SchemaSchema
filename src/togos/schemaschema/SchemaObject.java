package togos.schemaschema;

import java.util.Map;
import java.util.Set;

public interface SchemaObject {
	public String getName();
	public Map<Property,Set<Object>> getPropertyValues();
}

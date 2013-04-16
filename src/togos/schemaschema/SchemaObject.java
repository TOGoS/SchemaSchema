package togos.schemaschema;

import java.util.Map;
import java.util.Set;

public interface SchemaObject {
	public String getName();
	public String getLongName();
	public Map<Predicate,Set<Object>> getProperties();
}

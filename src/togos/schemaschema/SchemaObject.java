package togos.schemaschema;

import java.util.Map;
import java.util.Set;

import togos.lang.SourceLocation;

public interface SchemaObject {
	public SourceLocation getSourceLocation();
	public String getName();
	public String getLongName();
	public Map<Predicate,Set<SchemaObject>> getProperties();
	public boolean hasScalarValue();
	public Object getScalarValue();
}

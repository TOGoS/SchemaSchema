package togos.schemaschema;

import java.util.Collections;
import java.util.Map;

public class IndexSpec
{
	public final String name;
	public final Map<String,FieldSpec> fields;
	
	public IndexSpec( String name, Map<String,FieldSpec> fields ) {
		this.name = name;
		this.fields = Collections.unmodifiableMap(fields);
	}
	
	public String toString() { return StringUtil.join("\n", fields.values()); }
}

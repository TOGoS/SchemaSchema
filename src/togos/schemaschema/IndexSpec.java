package togos.schemaschema;

import java.util.LinkedHashMap;
import java.util.Map;

public class IndexSpec
{
	public final String name;
	public final Map<String,FieldSpec> fields;
	
	public IndexSpec( String name, Map<String,FieldSpec> fields ) {
		this.name = name;
		this.fields = fields;
	}
	
	public IndexSpec( String name ) {
		this( name, new LinkedHashMap<String,FieldSpec>() );
	}

	public String toString() { return StringUtil.join("\n", fields.values()); }
}

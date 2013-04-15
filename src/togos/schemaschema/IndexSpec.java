package togos.schemaschema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class IndexSpec
{
	public final String name;
	public final Set<FieldSpec> fields;
	
	public IndexSpec( String name, Set<FieldSpec> fields ) {
		this.name = name;
		this.fields = fields;
	}
	
	public IndexSpec( String name, Collection<FieldSpec> fields ) {
		this( name, new LinkedHashSet<FieldSpec>(fields) );
	}
	
	public IndexSpec( String name ) {
		this( name, new LinkedHashSet<FieldSpec>() );
	}

	public String toString() { return StringUtil.join("\n", fields); }
}

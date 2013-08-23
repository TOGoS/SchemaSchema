package togos.schemaschema;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import togos.lang.SourceLocation;

public class IndexSpec extends BaseSchemaObject
{
	public final Set<FieldSpec> fields;
	
	public IndexSpec( String name, Set<FieldSpec> fields, SourceLocation sLoc ) {
		super(name, sLoc);
		this.fields = fields;
	}
	
	public IndexSpec( String name, Collection<FieldSpec> fields, SourceLocation sLoc ) {
		this( name, new LinkedHashSet<FieldSpec>(fields), sLoc );
	}
	
	public IndexSpec( String name, SourceLocation sLoc ) {
		this( name, new LinkedHashSet<FieldSpec>(), sLoc );
	}
	
	public String toString() { return StringUtil.join("\n", fields); }
}

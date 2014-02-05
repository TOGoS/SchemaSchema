package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.defineType;
import togos.schemaschema.Namespace;
import togos.schemaschema.Type;

public class Types
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX);
	
	private Types() { }
	
	// TODO: All these things should themselves have type = class
	
	public static final Type CLASS     = defineType(NS, "class");
	
	public static final Type VOID      = defineType(NS, "void");
	public static final Type SCALAR    = defineType(NS, "scalar");
	public static final Type NUMBER    = defineType(NS, "number");
	public static final Type BOOLEAN   = defineType(NS, "boolean");
	public static final Type INTEGER   = defineType(NS, "integer");
	public static final Type STRING    = defineType(NS, "string");
	public static final Type REFERENCE = defineType(NS, "reference");
	public static final Type OBJECT    = defineType(NS, "object");
	public static final Type FIELD     = defineType(NS, "field");
	public static final Type PREDICATE = defineType(NS, "predicate");
}

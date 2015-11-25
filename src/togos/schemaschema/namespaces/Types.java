package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.defineType;
import togos.schemaschema.Namespace;
import togos.schemaschema.Type;

public class Types
{
	public static final Namespace NS = Core.TYPES_NS;
	
	private Types() { }
	
	// TODO: All these things should themselves have type = class
	// And maybe come from the RDF namespace
	
	public static final Type CLASS     = Core.CLASS;
	public static final Type PREDICATE = Core.PREDICATE;
	static {
		NS.addType(CLASS);
		NS.addType(PREDICATE);
	}
	
	public static final Type VOID      = defineType(NS, "void");
	public static final Type SCALAR    = defineType(NS, "scalar");
	public static final Type NUMBER    = defineType(NS, "number");
	public static final Type BOOLEAN   = defineType(NS, "boolean");
	public static final Type INTEGER   = defineType(NS, "integer");
	public static final Type STRING    = defineType(NS, "string");
	public static final Type REFERENCE = defineType(NS, "reference");
	public static final Type OBJECT    = defineType(NS, "object");
	public static final Type FIELD     = defineType(NS, "field");
	public static final Type UNIT      = defineType(NS, "unit");
	public static final Type FUNCTION  = defineType(NS, "function");
}

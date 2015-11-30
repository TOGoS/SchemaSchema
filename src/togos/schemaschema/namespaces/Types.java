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
	public static final Type OBJECT    = defineType(NS, "object"); // They're all 'objects'.  What is this supposed to mean?
	public static final Type SCALAR    = defineType(NS, "scalar");
	public static final Type NUMBER    = defineType(NS, "number");
	public static final Type BOOLEAN   = defineType(NS, "boolean");
	public static final Type INTEGER   = defineType(NS, "integer", NUMBER);
	public static final Type STRING    = defineType(NS, "string");
	public static final Type REFERENCE = defineType(NS, "reference");
	public static final Type FIELD     = defineType(NS, "field");
	public static final Type UNIT      = defineType(NS, "unit");
	public static final Type FUNCTION  = defineType(NS, "function");
	public static final Type COLLECTION= defineType(NS, "collection");
	public static final Type LIST      = defineType(NS, "list", COLLECTION);
	public static final Type SET       = defineType(NS, "set", COLLECTION);
	public static final Type MAP       = defineType(NS, "map", COLLECTION);
}

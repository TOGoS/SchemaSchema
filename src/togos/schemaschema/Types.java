package togos.schemaschema;

public class Types
{
	private Types() { }
	
	public static final Type VOID      = new SimpleType("void");
	public static final Type SCALAR    = new SimpleType("scalar");
	public static final Type NUMBER    = new SimpleType("number");
	public static final Type BOOLEAN   = new SimpleType("boolean");
	public static final Type INTEGER   = new SimpleType("integer");
	public static final Type STRING    = new SimpleType("string");
	public static final Type REFERENCE = new SimpleType("reference");
	public static final Type OBJECT    = new SimpleType("object");
	public static final Type CLASS     = new SimpleType("class");
}

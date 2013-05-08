package togos.schemaschema;

import togos.lang.BaseSourceLocation;

public class Types
{
	private static final BaseSourceLocation SLOC = new BaseSourceLocation(Types.class.getName(), 0, 0);
	
	private Types() { }
	
	public static final Type VOID      = new SimpleType("void", SLOC);
	public static final Type SCALAR    = new SimpleType("scalar", SLOC);
	public static final Type NUMBER    = new SimpleType("number", SLOC);
	public static final Type BOOLEAN   = new SimpleType("boolean", SLOC);
	public static final Type INTEGER   = new SimpleType("integer", SLOC);
	public static final Type STRING    = new SimpleType("string", SLOC);
	public static final Type REFERENCE = new SimpleType("reference", SLOC);
	public static final Type OBJECT    = new SimpleType("object", SLOC);
	public static final Type CLASS     = new SimpleType("class", SLOC);
}

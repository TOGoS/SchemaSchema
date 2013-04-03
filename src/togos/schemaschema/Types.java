package togos.schemaschema;

public class Types
{
	private Types() { }
	
	public static final Type VOID      = new SimpleType("Void");
	public static final Type SCALAR    = new SimpleType("Scalar");
	public static final Type NUMBER    = new SimpleType("Number");
	public static final Type INTEGER   = new SimpleType("Integer");
	public static final Type STRING    = new SimpleType("String");
	public static final Type REFERENCE = new SimpleType("Reference");
	public static final Type OBJECT    = new SimpleType("Object");
	
	public static final Type getRootType( Type t ) {
		for( Type parent; (parent = t.getParentType()) != null; t = parent );  
		return t;
	}
}

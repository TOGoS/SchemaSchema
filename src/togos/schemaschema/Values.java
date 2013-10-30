package togos.schemaschema;

import togos.lang.BaseSourceLocation;
import togos.schemaschema.parser.SchemaInterpreter;

public class Values
{
	private static final BaseSourceLocation SLOC = new BaseSourceLocation(Types.class.getName(), 0, 0);
	
	private Values() { }
	
	public static final SchemaObject NULL = BaseSchemaObject.forScalar(null, SLOC);
	
	public static final SchemaObject TRUE = BaseSchemaObject.forScalar(Boolean.TRUE, "true", Types.BOOLEAN, SLOC);
	public static final SchemaObject FALSE = BaseSchemaObject.forScalar(Boolean.FALSE, "false", Types.BOOLEAN, SLOC);
	
	public static final void defineBooleanValues(SchemaInterpreter interp) throws Exception {
		interp.defineThing(TRUE , false);
		interp.defineThing(FALSE, false);
	}
}

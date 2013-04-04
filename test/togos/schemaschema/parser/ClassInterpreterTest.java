package togos.schemaschema.parser;

import java.util.Map;

import junit.framework.TestCase;
import togos.lang.ParseError;
import togos.schemaschema.ObjectType;
import togos.schemaschema.Types;

public class ClassInterpreterTest extends TestCase
{
	public void testSimpleClass() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer\n" +
			"\tstr field : string\n" +
			"}";
		
		ClassInterpreter<ObjectType> ci = new ClassInterpreter<ObjectType>();
		ci.types.put("integer", Types.INTEGER);
		ci.types.put("string", Types.STRING);
		Map<String,ObjectType> classes = ci.parse(source);
		assertEquals( 1, classes.size() );
		for( String k : classes.keySet() ) assertEquals("some object", k);
		assertEquals( source, classes.get("some object").toString() );
	}
}

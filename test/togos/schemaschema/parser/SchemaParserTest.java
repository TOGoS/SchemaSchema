package togos.schemaschema.parser;

import java.util.Map;

import junit.framework.TestCase;
import togos.lang.ParseError;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.ObjectType;
import togos.schemaschema.Types;

public class SchemaParserTest extends TestCase
{
	public void testSimpleClass() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer\n" +
			"\tstr field : string\n" +
			"}";
		
		SchemaParser<ObjectType> ci = new SchemaParser<ObjectType>();
		ci.types.put("integer", Types.INTEGER);
		ci.types.put("string", Types.STRING);
		Map<String,ObjectType> classes = ci.parse(source);
		assertEquals( 1, classes.size() );
		for( String k : classes.keySet() ) assertEquals("some object", k);
		assertEquals( source, classes.get("some object").toString() );
		
		ObjectType ot = classes.get("some object");
		assertEquals( 2, ot.fieldsByName.size() );
		
		{
			FieldSpec intFieldSpec = ot.fieldsByName.get("int field");
			assertNotNull( intFieldSpec );
			assertEquals( "int field", intFieldSpec.name );
			assertFalse( intFieldSpec.isNullable );
			assertSame( Types.INTEGER, intFieldSpec.type );
		}
		
		{
			FieldSpec strFieldSpec = ot.fieldsByName.get("str field");
			assertNotNull( strFieldSpec );
			assertEquals( "str field", strFieldSpec.name );
			assertFalse( strFieldSpec.isNullable );
			assertSame( Types.STRING, strFieldSpec.type );
		}
	}
}

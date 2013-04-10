package togos.schemaschema.parser;

import java.util.Map;

import junit.framework.TestCase;
import togos.lang.ParseError;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.ObjectType;
import togos.schemaschema.Types;

public class SchemaParserTest extends TestCase
{
	SchemaParser<ObjectType> ci;
	public void setUp() {
		ci = new SchemaParser<ObjectType>();
		ci.types.put("integer", Types.INTEGER);
		ci.types.put("string", Types.STRING);
	}
	
	protected void assertFieldsNamedProperly( Map<String,FieldSpec> fieldMap ) {
		for( Map.Entry<String,FieldSpec> e : fieldMap.entrySet() ) {
			assertEquals( e.getKey(), e.getValue().name );
		}
	}
	
	protected ObjectType parseClass( String source, String className ) throws ParseError {
		Map<String,ObjectType> classes = ci.parse(source);
		assertEquals( 1, classes.size() );
		for( String k : classes.keySet() ) {
			assertEquals(className, k);
		}
		// assertEquals( source, classes.get("some object").toString() );
		return classes.get(className);
	}
	
	public void testSimpleClass() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer\n" +
			"\tstr field : string\n" +
			"}";
		
		ObjectType ot = parseClass( source, "some object" );
		assertEquals( source, ot.toString() );
		assertEquals( 2, ot.fieldsByName.size() );
		assertFieldsNamedProperly( ot.fieldsByName );
		
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
	
	public void testClassWithPrimaryKey() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer : primary key component\n" +
			"\tstr field : string : primary key component\n" +
			"}";
		
		ObjectType ot = parseClass( source, "some object" );
		
		assertEquals( 1, ot.indexesByName.size() );
		assertTrue( ot.indexesByName.containsKey("primary") );
		IndexSpec primaryIndex = ot.indexesByName.get("primary");
		assertEquals( "primary", primaryIndex.name );
		assertEquals( 2, primaryIndex.fields.size() );
		assertFieldsNamedProperly( primaryIndex.fields );
	}
}

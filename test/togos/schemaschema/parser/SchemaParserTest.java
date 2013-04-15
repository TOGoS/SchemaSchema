package togos.schemaschema.parser;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import togos.lang.ParseError;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.ComplexType;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.Types;
import togos.schemaschema.parser.asyncstream.StreamDestination;

public class SchemaParserTest extends TestCase
{
	SchemaParser ci;
	Map<String,SchemaObject> definedObjects;
	
	public void setUp() {
		definedObjects = new HashMap<String,SchemaObject>();
		
		ci = new SchemaParser();
		ci.types.put("integer", Types.INTEGER);
		ci.types.put("string", Types.STRING);
		ci.pipe(new StreamDestination<SchemaObject>() {
			@Override public void data(SchemaObject value) throws Exception {
				definedObjects.put( value.getName(), value );
			}
			@Override public void end() throws Exception { }
		});
	}
	
	protected void assertFieldsNamedProperly( Map<String,FieldSpec> fieldMap ) {
		for( Map.Entry<String,FieldSpec> e : fieldMap.entrySet() ) {
			assertEquals( e.getKey(), e.getValue().name );
		}
	}
	
	protected ComplexType parseClass( String source, String className ) throws ParseError {
		ci.parse(source, "(test source)");
		assertEquals( 1, definedObjects.size() );
		for( String k : definedObjects.keySet() ) {
			assertEquals(className, k);
		}
		SchemaObject so = definedObjects.get(className); 
		assertTrue("Parsed object expected to be a complex type", so instanceof ComplexType);
		// assertEquals( source, classes.get("some object").toString() );
		return (ComplexType)so;
	}
	
	public void testSimpleClass() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer\n" +
			"\tstr field : string\n" +
			"}";
		
		ComplexType ot = parseClass( source, "some object" );
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
		
		ComplexType ot = parseClass( source, "some object" );
		
		assertEquals( 1, ot.indexesByName.size() );
		assertTrue( ot.indexesByName.containsKey("primary") );
		IndexSpec primaryIndex = ot.indexesByName.get("primary");
		assertEquals( "primary", primaryIndex.name );
		assertEquals( 2, primaryIndex.fields.size() );
		assertFieldsNamedProperly( primaryIndex.fields );
	}
}

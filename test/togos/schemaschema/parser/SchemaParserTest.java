package togos.schemaschema.parser;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import togos.lang.ParseError;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.ComplexType;
import togos.schemaschema.Properties;
import togos.schemaschema.Property;
import togos.schemaschema.PropertyUtil;
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
		ci.defineFieldModifier("key", SchemaParser.IndexFieldModifierSpec.INSTANCE);
		ci.defineFieldModifier("index", SchemaParser.IndexFieldModifierSpec.INSTANCE);
		ci.defineType(Types.INTEGER);
		ci.defineType(Types.STRING);
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
	
	protected void assertPropertyValue( Object expectedValue, SchemaObject obj, Property prop ) {
		if( expectedValue == null && obj.getPropertyValues().get(prop) == null ) {
		} else {
			assertEquals( 1, obj.getPropertyValues().get(prop).size() );
			for( Object v : obj.getPropertyValues().get(prop) ) {
				assertEquals( expectedValue, v );
			}
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
			assertPropertyValue( null, intFieldSpec, Properties.NULLABLE );
			assertPropertyValue( Types.INTEGER, intFieldSpec, Properties.TYPE );
		}
		
		{
			FieldSpec strFieldSpec = ot.fieldsByName.get("str field");
			assertNotNull( strFieldSpec );
			assertEquals( "str field", strFieldSpec.name );
			assertFalse( PropertyUtil.isTrue(strFieldSpec.getPropertyValues(), Properties.NULLABLE) );
			assertPropertyValue( Types.STRING, strFieldSpec, Properties.TYPE );
		}
	}
	
	public void testClassWithPrimaryKey() throws ParseError {
		String source =
			"class some object {\n" +
			"\tint field : integer : key(primary)\n" +
			"\tstr field : string : key(primary)\n" +
			"}";
		
		ComplexType ot = parseClass( source, "some object" );
		
		assertEquals( 1, ot.indexesByName.size() );
		assertTrue( ot.indexesByName.containsKey("primary") );
		IndexSpec primaryIndex = ot.indexesByName.get("primary");
		assertEquals( "primary", primaryIndex.name );
		assertEquals( 2, primaryIndex.fields.size() );
	}
}

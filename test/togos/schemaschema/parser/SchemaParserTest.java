package togos.schemaschema.parser;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import togos.lang.InterpretError;
import togos.lang.ScriptError;
import togos.schemaschema.ComplexType;
import togos.schemaschema.EnumType;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.Predicate;
import togos.schemaschema.Predicates;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.Types;
import togos.schemaschema.parser.asyncstream.StreamDestination;

public class SchemaParserTest extends TestCase
{
	SchemaParser ci;
	Map<String,SchemaObject> definedObjects;
	
	public void setUp() throws Exception {
		definedObjects = new HashMap<String,SchemaObject>();
		
		ci = new SchemaParser();
		ci.defineFieldModifier("key", SchemaParser.FieldIndexModifierSpec.INSTANCE);
		ci.defineFieldModifier("index", SchemaParser.FieldIndexModifierSpec.INSTANCE);
		ci.defineType(Types.INTEGER);
		ci.defineType(Types.STRING);
		ci.defineClassPredicate( Predicates.EXTENDS );
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
	
	protected void assertPropertyValue( Object expectedValue, SchemaObject obj, Predicate prop ) {
		if( expectedValue == null && obj.getProperties().get(prop) == null ) {
		} else {
			assertNotNull( prop.name+" should not be null", obj.getProperties().get(prop) );
			assertEquals( 1, obj.getProperties().get(prop).size() );
			for( Object v : obj.getProperties().get(prop) ) {
				assertEquals( expectedValue, v );
			}
		}
	}
	
	protected ComplexType parseClass( String source, String className ) throws ScriptError {
		ci.parse(source, "(test source)");
		SchemaObject so = definedObjects.get(className); 
		assertTrue("Parsed object expected to be a complex type", so instanceof ComplexType);
		// assertEquals( source, classes.get("some object").toString() );
		return (ComplexType)so;
	}
	
	public void testSimpleClass() throws ScriptError {
		String source =
			"class 'some object' {\n" +
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
			assertPropertyValue( null, intFieldSpec, Predicates.IS_NULLABLE );
			assertPropertyValue( Types.INTEGER, intFieldSpec, Predicates.OBJECTS_ARE_MEMBERS_OF );
		}
		
		{
			FieldSpec strFieldSpec = ot.fieldsByName.get("str field");
			assertNotNull( strFieldSpec );
			assertEquals( "str field", strFieldSpec.name );
			assertFalse( PropertyUtil.isTrue(strFieldSpec.getProperties(), Predicates.IS_NULLABLE) );
			assertPropertyValue( Types.STRING, strFieldSpec, Predicates.OBJECTS_ARE_MEMBERS_OF );
		}
	}
	
	public void testExtendedClass() throws ScriptError {
		String source =
			"class 'some object' : extends(integer) {\n" +
			"\tnumber of bits : integer\n" +
			"}";
			
		ComplexType ot = parseClass( source, "some object" );
		assertEquals( source, ot.toString() );
	}
	
	public void testClassWithPrimaryKey() throws ScriptError {
		String source =
			"class 'some object' {\n" +
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
	
	public void testParseEnum() throws ScriptError {
		String source =
			"enum colorful color {\n" +
			"\tyellow\n" +
			"\tgreen\n" +
			"\tred\n" +
			"}";
		
		ComplexType ot = parseClass( source, "colorful color" );
		assertTrue( ot instanceof EnumType );
		EnumType et = (EnumType)ot;
		assertEquals( 3, et.validValues.size() );
		for( SchemaObject v : et.validValues ) {
			assertTrue( PropertyUtil.isMemberOf( v, et ) );
			assertTrue( "red".equals(v.getName()) || "green".equals(v.getName()) || "yellow".equals(v.getName()) );
		}
	}
	
	public void testEvaluateEnumValue() throws ScriptError {
		String source =
			"enum X {\n"+
			"  foo\n" +
			"  bar\n" +
			"}\n" +
			"\n" +
			"class property X : X\n" +
			"\n" +
			"class Y : X @ foo";
		
		ci.parse(source, "(test script)");
		
		EnumType enumX = (EnumType)ci.types.get("X");
		assertNotNull(enumX);
		Predicate predX = ci.predicates.get("X");
		assertNotNull(predX);
		
		ComplexType ot = parseClass( source, "Y" );
		assertEquals( 1, PropertyUtil.getAll(ot.getProperties(), predX).size() );
		for( Object v : PropertyUtil.getAll(ot.getProperties(), predX) ) {
			assertTrue( PropertyUtil.isMemberOf((SchemaObject)v, enumX) );
		}
	}
	
	public void testEvaluateInvalidEnumValue() throws ScriptError {
		String source =
			"enum X {\n"+
			"  foo\n" +
			"  bar\n" +
			"}\n" +
			"\n" +
			"class property X : X\n";
		
		ci.parse(source, "(test script)");
		
		EnumType enumX = (EnumType)ci.types.get("X");
		assertNotNull(enumX);
		Predicate predX = ci.predicates.get("X");
		assertNotNull(predX);
				
		try {
			ci.parse("class Y : X @ barrrr\n", "(more test script)");
			fail("Inalid enum value should have thrown InterpretError");
		} catch( InterpretError e ) { }
	}
}

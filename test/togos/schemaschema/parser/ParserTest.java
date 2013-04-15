package togos.schemaschema.parser;

import togos.lang.BaseSourceLocation;
import junit.framework.TestCase;

public class ParserTest extends TestCase
{
	public void testParse( String expectedOutput, String input ) throws Exception {
		assertEquals( expectedOutput, Parser.parseCommand( input, BaseSourceLocation.NONE ).toString() );
	}
	
	public void testParse( String source ) throws Exception {
		testParse( source, source );
	}

	public void testSimpleCommand() throws Exception {
		testParse(
			"foo"
		);
	}

	public void testComplicatedCommand() throws Exception {
		testParse( "foo 'bar baz' : quux(xyzzy) : xuuy quuz('foo bar'(baz)) = what a fine day(apple jacks) : throne" );
	}
	
	public void testAliasCommand() throws Exception {
		testParse( "alias field modifier 'entity ID' = unsigned integer" );
	}
	
	public void testClassDefCommand() throws Exception {
		testParse(
			"class x y z {\n" +
			"\tfoo : string(4)\n" +
			"\tbar : integer(0, 9999)\n" +
			"}"
		);
	}
	
	public void testAtModifiers() throws Exception {
		testParse(
			"class x y z : foo(bar) {\n" +
			"\tfoo : string(4)\n" +
			"\tbar : integer(0, 9999)\n" +
			"\tsome cool thing : x(y(z(a, b)))\n" +
			"}",
			"class x y z : foo @ bar {\n" +
			"\tfoo : string @ 4\n" +
			"\tbar : integer(0, 9999)\n" +
			"\tsome cool thing : x @ y @ z(a,b)\n" +
			"}"
		);
	}
}

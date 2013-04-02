package togos.schemaschema.parser;

import togos.lang.BaseSourceLocation;
import junit.framework.TestCase;

public class ParserTest extends TestCase
{
	public void testParse( String source ) throws Exception {
		assertEquals( source, Parser.parseCommand( source, BaseSourceLocation.NONE ).toString() );
	}

	public void testSimpleCommand() throws Exception {
		testParse(
			"foo"
		);
	}

	public void testCmoplicatedCommand() throws Exception {
		testParse(
			"foo 'bar baz' : quux(xyzzy) : xuuy quuz('foo bar'(baz)) = what a fine day(apple jacks) : throne"
		);
	}
}

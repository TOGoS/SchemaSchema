package togos.schemaschema.parser;

import togos.lang.ParseError;
import togos.schemaschema.parser.asyncstream.Collector;
import junit.framework.TestCase;

public class TokenizerTest extends TestCase
{
	protected void assertTokenization( String expected, String input ) throws Exception {
		Collector<Token> c = new Collector<Token>();
		Tokenizer t = new Tokenizer();
		t.pipe(c);
		t.data( input.toCharArray() );
		t.end();
		String[] eparts = expected.split("\\|");
		assertEquals( eparts.length, c.collection.size() );
	}
	
	protected void assertParseError( String input ) throws Exception {
		try {
			Tokenizer t = new Tokenizer();
			t.data( input.toCharArray() );
			t.end();
			fail("Parsing <"+input+"> should have caused a ParseError, but did not!");
		} catch( ParseError e ) {
		}
	}
	
	public void testTokenizeStuff() throws Exception {
		assertTokenization("foo", "foo");
	}
	
	public void testTokenizeBarewoirds() throws Exception {
		assertTokenization("foo|bar|baz|quux", "foo bar  baz  quux");
	}
	
	public void testTokenizeStrings() throws Exception {
		assertTokenization("foo bar baz|quux xyzzy|radsauce", "'foo bar baz' \"quux xyzzy\" radsauce");
	}
	
	public void testTokenizeThang() throws Exception {
		assertTokenization("foo'bar\r\n|{|}|quux\"xyzzy\t", "'foo\\'bar\\r\\n'{}\"quux\\\"xyzzy\\t\"");
	}
	
	public void testNoSqushedWords() throws Exception {
		assertParseError("foo'foo'");
		assertParseError("foo\"foo'");
		assertParseError("'foo'foo");
	}
}

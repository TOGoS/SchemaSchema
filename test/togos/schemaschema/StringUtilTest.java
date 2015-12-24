package togos.schemaschema;

import junit.framework.TestCase;

public class StringUtilTest extends TestCase
{
	public void testUnindentEmpty() {
		assertEquals("", StringUtil.unindent(""));
	}
	
	public void testUnindentBasicallyEmpty() {
		assertEquals("", StringUtil.unindent("   "));
	}
	
	public void testUnindentBasicallyEmptyMultipleLines() {
		assertEquals("", StringUtil.unindent("   \n \t \n  "));
	}
	
	public void testUnindentNoIndent() {
		assertEquals("birds are cool sometimes", StringUtil.unindent("   \nbirds are cool sometimes\n  "));
	}
	
	public void testUnindentSomeIndent() {
		assertEquals(
			"birds are cool sometimes\n"+
			"\tbut squirrels are rad always",
			StringUtil.unindent("   \n  birds are cool sometimes\n  \tbut squirrels are rad always\n \t \t"));
	}
}

package togos.schemaschema.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class UnmodifiableMapTest extends TestCase
{
	public void testInstanceReused() {
		HashMap<String,String> original = new HashMap<String,String>();
		original.put("Rice crispies", "Donut holes");
		
		Map<String,String> unmodifiable1 = Collections.unmodifiableMap(original);
		//Map<String,String> unmodifiable2 = Collections.unmodifiableMap(unmodifiable1);
		
		assertNotSame( original, unmodifiable1 );
		// It is not true!
		//assertSame( unmodifiable1, unmodifiable2 );
	}
}

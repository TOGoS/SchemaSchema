package togos.schemaschema;

import java.util.HashMap;
import java.util.Set;

import togos.codeemitter.WordUtil;
import togos.lang.BaseSourceLocation;
import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.parser.ast.Word;

public class Predicate extends BaseSchemaObject
{
	protected static HashMap<String,Predicate> byLongName = new HashMap<String,Predicate>(); 
	
	public static synchronized Predicate getWithoutInitializing(Namespace ns, String name) {
		String longName = ns.prefix + WordUtil.toCamelCase(name);
		Predicate p = byLongName.get(longName);
		if( p == null ) {
			p = new Predicate(BaseSourceLocation.NONE);
			//p.name = name;
			//p.longName = longName;
			byLongName.put(longName, p);
		}
		return p;
	}
	
	public Predicate(SourceLocation sLoc) {
		super(null, sLoc);
	}
	
	public Predicate(String name, SourceLocation sLoc) {
		super(name, sLoc);
	}

	public Predicate(String name, String longName, SourceLocation sLoc) {
		super(name, longName, sLoc);
	}
	
	public void addObjectType(Type t) {
		setProperty(Core.VALUE_TYPE, t);
	}
	
	public Set<Type> getObjectTypes() {
		return PropertyUtil.getAll(properties, Core.VALUE_TYPE, Type.class);
	}
	
	@Override public String toString() {
		return "predicate "+Word.quote(name);
	}
}

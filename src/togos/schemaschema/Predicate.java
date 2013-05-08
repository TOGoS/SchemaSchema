package togos.schemaschema;

import java.util.Set;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.ast.Word;

public class Predicate extends BaseSchemaObject
{
	public Predicate(String name, SourceLocation sLoc) {
		super(name, sLoc);
	}

	public Predicate(String name, String longName, SourceLocation sLoc) {
		super(name, longName, sLoc);
	}

	public Predicate(String name, String longName, Type objectType, SourceLocation sLoc) {
		super(name, longName, sLoc);
		this.addObjectType(objectType);
	}
	
	public void addObjectType(Type t) {
		PropertyUtil.add(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, t);
	}
	
	public Set<Type> getObjectTypes() {
		return PropertyUtil.getAll(properties, Predicates.OBJECTS_ARE_MEMBERS_OF, Type.class);
	}
	
	@Override public String toString() {
		return "predicate "+Word.quote(name);
	}
}

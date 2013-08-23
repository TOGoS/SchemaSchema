package togos.schemaschema;

import togos.lang.BaseSourceLocation;

public class Predicates
{
	private static final BaseSourceLocation SLOC = new BaseSourceLocation(Predicates.class.getName(), 0, 0);
	
	static final String RDF_NS  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	
	public static final Predicate IS_SELF_KEYED = new Predicate("is self-keyed", SLOC);
	public static final Predicate IS_MEMBER_OF = new Predicate("is member of", RDF_NS+"type", SLOC);
	/** Type of object values for predicates and predicate-like things (e.g. class fields). */ 
	public static final Predicate OBJECTS_ARE_MEMBERS_OF = new Predicate("objects are members of", SLOC);
	public static final Predicate EXTENDS = new Predicate("extends", RDFS_NS+"subClassOf", SLOC);
	/** Applicable to variables/fields/predicates that may contain/map to a null value */
	public static final Predicate IS_NULLABLE = new Predicate("is nullable", SLOC);
	public static final Predicate IS_ENUM_TYPE = new Predicate("is enum type", SLOC);
	/**
	 * Fields are instance variables.
	 * i.e. a class has a field, and an instance has that field -> some value as a predicate -> object.
	 */
	public static final Predicate HAS_FIELD = new Predicate("has field", SLOC);
}

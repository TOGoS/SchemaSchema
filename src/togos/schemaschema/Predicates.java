package togos.schemaschema;

public class Predicates
{
	static final String RDF_NS  = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	
	public static final Predicate IS_SELF_KEYED = new Predicate("is self-keyed");
	public static final Predicate IS_MEMBER_OF = new Predicate("is member of", RDF_NS+"type");
	/** Type of object values for predicates and predicate-like things (e.g. class fields). */ 
	public static final Predicate OBJECTS_ARE_MEMBERS_OF = new Predicate("objects are members of");
	public static final Predicate EXTENDS = new Predicate("extends", RDFS_NS+"subClassOf");
	public static final Predicate IS_NULLABLE = new Predicate("is nullable");
	
	/** Used to determine what parser is used to interpret objects declared as a given type. */
	public static final Predicate SCHEMA_COMMAND_PARSER = new Predicate("schema command parser");
}

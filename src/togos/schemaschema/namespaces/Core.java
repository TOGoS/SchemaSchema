package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;

public class Core
{
	public static final Namespace RDF_NS = Namespace.getInstance("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS_NS = Namespace.getInstance("http://www.w3.org/2000/01/rdf-schema#");
	
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX);
	
	private Core() { }
	
	public static final Predicate NAME     = definePredicate(NS, "name", Types.STRING, "short, natural-language name of the subject");
	public static final Predicate LONGNAME = definePredicate(NS, "long name", Types.STRING, "globally unique name; usually in URI form");
	public static final Predicate COMMENT  = definePredicate(NS, "comment", Types.STRING, "a note about the subject");
	
	public static final Predicate TYPE         = definePredicate(RDF_NS, "type", Types.CLASS, null);
	public static final Predicate EXTENDS      = definePredicate(RDFS_NS, "is subclass of", Types.CLASS, null);

	public static final Predicate VALUE_TYPE   = definePredicate(NS, "value type", Types.CLASS, "indicates that the subject is a field or predicate whose objects must be of the specified class");
	public static final Predicate IS_NULLABLE  = definePredicate(NS, "is nullable", Types.BOOLEAN, "indicates that the subject is a field for which null is a valid value");
	public static final Predicate IS_ENUM_TYPE = definePredicate(NS, "is enum type", Types.BOOLEAN, "indicates that the subject is a type that defines a list of valid named members");
	/**
	 * Fields are instance variables.
	 * i.e. a class has a field, and an instance has that field -> some value as a predicate -> object.
	 */
	public static final Predicate HAS_FIELD = definePredicate(NS, "has field", Types.FIELD, null);
	public static final Predicate HAS_FOREIGN_KEY = definePredicate(NS, "has foreign key", Types.FOREIGN_KEY, null);
	
	public static final Predicate IS_SELF_KEYED = definePredicate(NS, "is self-keyed", Types.BOOLEAN, "indicates the subject is a class that uses all of its fields as a primary key");
	public static final Predicate HAS_INDEX = definePredicate(NS, "has index", Types.INDEX, null);
}

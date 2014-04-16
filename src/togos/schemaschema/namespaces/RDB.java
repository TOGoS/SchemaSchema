package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import static togos.schemaschema.namespaces.NSUtil.defineType;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.Type;

/**
 * Defines types and properties relevant to relational databases 
 */
public class RDB
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"RDB/");
	
	public static final Type SCHEMA      = defineType(NS, "schema");
	public static final Type INDEX       = defineType(NS, "index");
	public static final Type SEQUENCE    = defineType(NS, "sequence");
	public static final Type FOREIGN_KEY = defineType(NS, "foreign key");
	
	// Table properties
	public static final Predicate NAME_IN_DB = definePredicate(NS, "name in database", Types.STRING,
		"name of the database object that the subject corresponds to; useful when different than what would be inferred based on the subject's canonical name");
	public static final Predicate IN_NAMESPACE = definePredicate(NS, "is in namespace", Types.OBJECT,
		"namespace (probably a 'schema' object) within which this object is defined");
	public static final Predicate HAS_FOREIGN_KEY = definePredicate(NS, "has foreign key", FOREIGN_KEY, null);
	public static final Predicate IS_SELF_KEYED = definePredicate(NS, "is self-keyed", Types.BOOLEAN, "indicates the subject is a class that uses all of its fields as a primary key");
	public static final Predicate HAS_INDEX = definePredicate(NS, "has index", INDEX, null);
	
	// Sequence properties
	public static final Predicate INITIAL_VALUE = definePredicate(NS, "initial value", Types.INTEGER, "initial value for sequences");
	
	// Column properties
	public static final Predicate IS_AUTO_INCREMENTED = definePredicate(NS, "is auto-incremented", Types.BOOLEAN, null);
	public static final Predicate DEFAULT_VALUE_SEQUENCE = definePredicate(NS, "default value sequence", SEQUENCE, null);
}

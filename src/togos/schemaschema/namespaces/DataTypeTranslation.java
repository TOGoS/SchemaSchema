package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;

public class DataTypeTranslation
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"DataTypeTranslation/");
	
	private DataTypeTranslation() { }
	
	public static final Predicate PGSQL_TYPE= definePredicate(NS, "Postgres type", Types.STRING, "name of Postgres type used to represent value in database");
	public static final Predicate MYSQL_TYPE= definePredicate(NS, "MySQL type", Types.STRING, "name of MySQL type used to represent value in database");
	public static final Predicate SQL_TYPE  = definePredicate(NS, "SQL type", Types.STRING, "name of ANSI SQL type used to represent value in database");
	public static final Predicate PHP_TYPE  = definePredicate(NS, "PHP type", Types.STRING, "name of PHP type used to represent value in PHP");
	public static final Predicate JSON_TYPE = definePredicate(NS, "JSON type", Types.STRING, "name of JS type used to represent value in JSON");
	
	public static final Predicate REGEX     = definePredicate(NS, "regex", Types.STRING, "regular expression to validate string representations of members");
}

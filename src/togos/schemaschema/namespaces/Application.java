package togos.schemaschema.namespaces;

import static togos.schemaschema.namespaces.NSUtil.definePredicate;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;

public class Application
{
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX+"Application/");
	
	private Application() { }
	
	public static final Predicate HAS_DB_TABLE = definePredicate(NS, "has an associated database table", Types.BOOLEAN, "indicates that there is a database table associated with this class");
	public static final Predicate IS_EXPOSED_VIA_SERVICE = definePredicate(NS, "is exposed via service", Types.BOOLEAN, "indicates that the subject's records should be exposed via services");
}

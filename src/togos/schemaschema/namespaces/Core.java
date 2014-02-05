package togos.schemaschema.namespaces;

import java.util.ArrayList;
import java.util.List;

import togos.lang.BaseSourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.Type;

public class Core
{
	public static final Namespace RDF_NS = Namespace.getInstance("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	public static final Namespace RDFS_NS = Namespace.getInstance("http://www.w3.org/2000/01/rdf-schema#");
	
	public static final Namespace NS = Namespace.getInstance(NSUtil.SCHEMA_PREFIX);
	
	private Core() { }
	
	// We have to resort to some 2-passery here so as not run into nulls
	// when using certain predicates as part of their own definitions.
	
	private static class PredicatePredefinition {
		public final Predicate pred;
		public final Type valueType;
		public final String comment;
		public PredicatePredefinition( Predicate p, Type valueType, String comment ) {
			this.pred = p;
			this.valueType = valueType;
			this.comment = comment;
		}
	}
	private static List<PredicatePredefinition> predefs = new ArrayList<PredicatePredefinition>();
	private static boolean fixedUp = false;
	private static Predicate predefinePredicate(Namespace ns, String name, Type valueType, String comment) {
		assert !fixedUp;
		Predicate pred = new Predicate(name, BaseSourceLocation.NONE);
		predefs.add(new PredicatePredefinition(pred, valueType, comment));
		ns.addPredicate(pred);
		return pred;
	}
	private static void fixUpPredefinitions() {
		assert !fixedUp;
		for( PredicatePredefinition ppd : predefs ) {
			if( ppd.valueType != null ) ppd.pred.addObjectType(ppd.valueType);
			if( ppd.comment != null ) ppd.pred.setProperty(COMMENT, BaseSchemaObject.forScalar(ppd.comment, BaseSourceLocation.NONE));
		}
		fixedUp = true;
	}
	
	public static final Predicate COMMENT  = predefinePredicate(NS, "comment", Types.STRING, "a note about the subject");
	
	public static final Predicate NAME     = predefinePredicate(NS, "name", Types.STRING, "short, natural-language name of the subject");
	public static final Predicate LONGNAME = predefinePredicate(NS, "long name", Types.STRING, "globally unique name; usually in URI form");
	
	public static final Predicate TYPE         = predefinePredicate(RDF_NS, "type", Types.CLASS, null);
	
	public static final Predicate EXTENDS      = predefinePredicate(RDFS_NS, "is subclass of", Types.CLASS, null);
	public static final Predicate IS_ENUM_TYPE = predefinePredicate(NS, "is enum type", Types.BOOLEAN, "indicates that the subject is a type that defines a list of valid named members");
	
	// Field/predicate properties
	public static final Predicate VALUE_TYPE   = predefinePredicate(NS, "value type", Types.CLASS, "indicates that the subject is a field or predicate whose objects must be of the specified class");
	public static final Predicate IS_NULLABLE  = predefinePredicate(NS, "is nullable", Types.BOOLEAN, "indicates that the subject is a field for which null is a valid value");
	public static final Predicate DEFAULT_VALUE= predefinePredicate(NS, "default value", null, "default value of field");
	public static final Predicate HAS_FIELD    = predefinePredicate(NS, "has field", Types.FIELD, null);
	
	static {
		fixUpPredefinitions();
	}
}

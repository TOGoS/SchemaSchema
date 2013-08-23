package togos.schemaschema;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import togos.lang.SourceLocation;
import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.ast.Word;

/**
 * All information in a ComplexType should be represented
 * within its SchemaObject properties.  The type exists solely
 * to provide additional type information for use in Java code.
 */
public class ComplexType extends BaseSchemaObject implements Type
{
	// TODO: Remove; use properties to represent these
	// TODO: Move index and foreign key fields to 'RelationalClass' class
	//protected final Map<String,FieldSpec> fieldsByName = new LinkedHashMap<String,FieldSpec>();
	protected final Map<String,IndexSpec> indexesByName = new LinkedHashMap<String,IndexSpec>();
	protected final Map<String,ForeignKeySpec> foreignKeysByName = new LinkedHashMap<String,ForeignKeySpec>();
	
	public ComplexType( String name, SourceLocation sLoc ) {
		super(name, sLoc);
	}
	
	public Collection<FieldSpec> getFields() {
		return PropertyUtil.getAllInheritedValuesOfClass(this, Predicates.HAS_FIELD, FieldSpec.class);
	}
	public FieldSpec getField(String fieldName) {
		for( FieldSpec f : getFields() ) if( fieldName.equals(f.getName())) return f;
		return null;
	}
	public boolean hasField(String fieldName) {
		for( FieldSpec f : getFields() ) if( fieldName.equals(f.getName())) return true;
		return false;
	}
	public void addField(FieldSpec fieldSpec) {
		PropertyUtil.add(this.getProperties(), Predicates.HAS_FIELD, fieldSpec);
	}
	
	public Collection<IndexSpec> getIndexes() {
		return indexesByName.values();
	}
	public boolean hasIndex(String name) {
		return indexesByName.containsKey(name);
	}
	public IndexSpec getIndex(String name) {
		return indexesByName.get(name);
	}
	public void addIndex(IndexSpec indexSpec) {
		indexesByName.put( indexSpec.name, indexSpec );
	}
	
	public void addForeignKey( ForeignKeySpec fks ) {
		foreignKeysByName.put( fks.name, fks );
	}
	public Collection<ForeignKeySpec> getForeignKeys() {
		return foreignKeysByName.values();
	}
	
	public String toString() {
		String extendStr = "";
		Set<Type> parentTypes = getExtendedTypes();
		if( parentTypes.size() > 0 ) {
			for( Type t : parentTypes ) {
				extendStr += extendStr.length() == 0 ? " : extends(" : ", ";
				extendStr += Phrase.quoteIfNecessary(t.getName());
			}
			extendStr += ")";
		}
		
		return "class "+Word.quote(name) + extendStr + (getFields().size() == 0 ? " " :
			" {\n" + StringUtil.indent("\t", StringUtil.join("\n", getFields())) + "\n}"
		);
	}
}

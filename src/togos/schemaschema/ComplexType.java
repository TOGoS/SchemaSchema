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
 * to provide additional type information.
 */
public class ComplexType extends BaseSchemaObject implements Type
{
	/** TODO: Remove; use properties to represent these */
	protected final Map<String,FieldSpec> fieldsByName = new LinkedHashMap<String,FieldSpec>();
	protected final Map<String,IndexSpec> indexesByName = new LinkedHashMap<String,IndexSpec>();
	protected final Map<String,ForeignKeySpec> foreignKeysByName = new LinkedHashMap<String,ForeignKeySpec>();
	
	public ComplexType( String name, SourceLocation sLoc ) {
		super(name, sLoc);
	}
	
	public Collection<FieldSpec> getFields() {
		return fieldsByName.values();
	}
	public FieldSpec getField(String fieldName) {
		return fieldsByName.get(fieldName);
	}
	public boolean hasField(String fieldName) {
		return fieldsByName.containsKey(fieldName);
	}
	public void addField(FieldSpec fieldSpec) {
		fieldsByName.put(fieldSpec.name, fieldSpec);
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
		
		return "class "+Word.quote(name) + extendStr + (fieldsByName.size() == 0 ? " " :
			" {\n" + StringUtil.indent("\t", StringUtil.join("\n", fieldsByName.values())) + "\n}"
		);
	}
}

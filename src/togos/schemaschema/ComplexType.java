package togos.schemaschema;

import java.util.Collection;
import java.util.Set;

import togos.lang.SourceLocation;
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.namespaces.RDB;
import togos.schemaschema.namespaces.Types;
import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.ast.Word;

/**
 * All information in a ComplexType should be represented
 * within its SchemaObject properties.  The type exists solely
 * to provide additional type information for use in Java code.
 */
public class ComplexType extends BaseSchemaObject implements Type
{
	public ComplexType( String name, SourceLocation sLoc ) {
		super(name, sLoc);
		setProperty(Core.TYPE, Types.CLASS);
	}
	
	public Collection<FieldSpec> getFields() {
		return PropertyUtil.getAllInheritedValuesOfClass(this, Core.HAS_FIELD, FieldSpec.class);
	}
	public FieldSpec getField(String name) {
		for( FieldSpec f : getFields() ) if( name.equals(f.getName())) return f;
		return null;
	}
	public boolean hasField(String name) {
		return getField(name) != null;
	}
	public void addField(FieldSpec fieldSpec) {
		PropertyUtil.add(this.getProperties(), Core.HAS_FIELD, fieldSpec);
	}
	
	public Collection<IndexSpec> getIndexes() {
		return PropertyUtil.getAllInheritedValuesOfClass(this, RDB.HAS_INDEX, IndexSpec.class);
	}
	public IndexSpec getIndex(String name) {
		for( IndexSpec i : getIndexes() ) if(name.equals(i.getName())) return i;
		return null;
	}
	public boolean hasIndex(String name) {
		return getIndex(name) != null;
	}
	public void addIndex(IndexSpec indexSpec) {
		PropertyUtil.add(this.getProperties(), RDB.HAS_INDEX, indexSpec);
	}
	
	public Collection<ForeignKeySpec> getForeignKeys() {
		return PropertyUtil.getAllInheritedValuesOfClass(this, RDB.HAS_FOREIGN_KEY, ForeignKeySpec.class);
	}
	public void addForeignKey( ForeignKeySpec fks ) {
		PropertyUtil.add(this.getProperties(), RDB.HAS_FOREIGN_KEY, fks);
	}
	
	public String toString() {
		String extendStr = "";
		Set<Type> parentTypes = getExtendedTypes();
		if( parentTypes.size() > 0 ) {
			for( Type t : parentTypes ) {
				extendStr += extendStr.length() == 0 ? " : is subclass of(" : ", ";
				extendStr += Phrase.quoteIfNecessary(t.getName());
			}
			extendStr += ")";
		}
		
		return "class "+Word.quote(name) + extendStr + (getFields().size() == 0 ? " " :
			" {\n" + StringUtil.indent("\t", StringUtil.join("\n", getFields())) + "\n}"
		);
	}
}

package togos.schemaschema.parser;

import static togos.schemaschema.PropertyUtil.isTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import togos.asyncstream.BaseStreamSource;
import togos.asyncstream.StreamDestination;
import togos.asyncstream.StreamUtil;
import togos.lang.BaseSourceLocation;
import togos.lang.CompileError;
import togos.lang.ScriptError;
import togos.lang.SourceLocation;
import togos.schemaschema.BaseSchemaObject;
import togos.schemaschema.ComplexType;
import togos.schemaschema.EnumType;
import togos.schemaschema.FieldSpec;
import togos.schemaschema.ForeignKeySpec;
import togos.schemaschema.IndexSpec;
import togos.schemaschema.Namespace;
import togos.schemaschema.Predicate;
import togos.schemaschema.PropertyUtil;
import togos.schemaschema.SchemaObject;
import togos.schemaschema.Type;
import togos.schemaschema.namespaces.Core;
import togos.schemaschema.namespaces.RDB;
import togos.schemaschema.namespaces.Types;
import togos.schemaschema.parser.SchemaInterpreter.Modifier.ApplicationTarget;
import togos.schemaschema.parser.ast.Block;
import togos.schemaschema.parser.ast.Command;
import togos.schemaschema.parser.ast.Parameterized;
import togos.schemaschema.parser.ast.Phrase;
import togos.schemaschema.parser.ast.Word;

public class SchemaInterpreter extends BaseStreamSource<SchemaObject,CompileError> implements StreamDestination<Command,CompileError>
{
	public static class RedefinitionError extends CompileError {
		private static final long serialVersionUID = 1L;
		
		public RedefinitionError( String message, SourceLocation sLoc ) {
			super(message, sLoc);
		}
	}
	
	protected static String singleString( Parameterized p, String contextDescription ) throws CompileError {
		if( p.parameters.length != 0 ) {
			throw new CompileError( contextDescription + " cannot take arguments", p.sLoc );
		}
		return p.subject.unquotedText();
	}
	
	protected static void applyFieldModifier( Modifier m, ComplexType t, FieldSpec f ) {
		if( m instanceof FieldModifier ) {
			((FieldModifier)m).apply(t, f);
		} else {
			m.apply(f);
		}
	}
	
	//// Command interpreters ////
	
	interface CommandInterpreter {
		/**
		 * @return true if this interpreter recognized and interpreted the command.
		 * @throws CompileError
		 */
		public boolean interpretCommand( Command cmd, int cmdPrefixLength ) throws CompileError;
	}
	
	static final boolean isBareword( Word w, String text ) {
		return w.quoting == Token.Type.BAREWORD && text.equals(w.text);
	}
	
	protected void importEverythingFromNamespace( Namespace ns, SourceLocation sLoc ) throws CompileError {
		for( Object obj : ns.items.values() ) {
			if( obj instanceof SchemaObject ) {
				SchemaObject so = (SchemaObject)obj;
				defineSomething(so.getName(), so, false, sLoc);
			}
		}
	}
	
	class ImportCommandInterpreter implements CommandInterpreter {
		@Override public boolean interpretCommand( Command cmd, int cmdPrefixLength ) throws CompileError {
			ensureNoParameters(cmd.subject, "symbol being defined");
			if( cmd.modifiers.length > 0 || cmd.body.commands.length > 0 || cmd.subject.parameters.length > 0 ) {
				throw new CompileError("Malformed import statement", cmd.sLoc );
			}
			Phrase p = cmd.subject.subject;
			if( p.words.length == cmdPrefixLength + 1 ) {
				// import 'something'
				String origin = p.words[cmdPrefixLength].text;
				Object thing = importables.get(origin);
				if( thing == null ) {
					throw new CompileError("Import failed; couldn't find '"+origin+"'", cmd.sLoc);
				}
				if( !(thing instanceof SchemaObject) ) {
					throw new CompileError("Import failed; '"+origin+"' is not a SchemaObject, so its name can't be inferred", cmd.sLoc);
				}
				SchemaObject soThing = (SchemaObject)thing;
				defineSomething(soThing.getName(), soThing, false, cmd.sLoc);
			} else if( p.words.length == cmdPrefixLength + 3 && isBareword(p.words[cmdPrefixLength+1], "as") ) {
				// import 'something' as 'something else'
				String origin = p.words[cmdPrefixLength].text;
				String alias = p.words[cmdPrefixLength+2].text;
				Object thing = importables.get(origin);
				if( thing == null ) {
					throw new CompileError("Import failed; couldn't find '"+origin+"'", cmd.sLoc);
				}
				defineSomething(alias, thing, false, cmd.sLoc);
			} else if( p.words.length == cmdPrefixLength + 3 &&
				isBareword(p.words[cmdPrefixLength],"everything") &&
				isBareword(p.words[cmdPrefixLength+1],"from")
			) {
				// import everything from 'somewhere'
				String origin = p.words[cmdPrefixLength+2].text;
				Object thing = importables.get(origin);
				if( thing == null ) {
					throw new CompileError("Import failed; couldn't find '"+origin+"'", cmd.sLoc);
				}
				if( !(thing instanceof Namespace) ) {
					throw new CompileError("Import failed; '"+origin+"' is not a Namespace", cmd.sLoc);
				}
				importEverythingFromNamespace( ((Namespace)thing), cmd.sLoc );
			} else {
				throw new CompileError("Malformed import statement: "+cmd.toString(), cmd.sLoc );
			}
			return true;
		}
	}
	
	
	abstract class DefinitionCommandInterpreter implements CommandInterpreter {
		protected abstract void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError;
		
		@Override public boolean interpretCommand( Command cmd, int cmdPrefixLength ) throws CompileError {
			ensureNoParameters(cmd.subject, "symbol being defined");
			Phrase cmdPhrase = cmd.subject.subject;
			boolean allowRedefinition;
			if( cmdPrefixLength >= 2 && cmdPhrase.startsWithWord("redefine") ) {
				cmdPhrase = cmdPhrase.tail(1);
				--cmdPrefixLength;
				allowRedefinition = true;
			} else {
				allowRedefinition = false;
			}
			interpretDefinition( cmdPhrase.tail(cmdPrefixLength).unquotedText(), cmd.modifiers, cmd.body, allowRedefinition, cmd.sLoc );
			return true;
		}
	}
	
	class ClassDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
		final Type metaClass;
		public ClassDefinitionCommandInterpreter( Type metaClass ) {
			this.metaClass = metaClass;
		}
		public ClassDefinitionCommandInterpreter() {
			this(null);
		}
		
		public ComplexType parseClass( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc )
			throws CompileError
		{
			ComplexType t = new ComplexType( name, sLoc );
			if( metaClass != null ) PropertyUtil.add( t.properties, Core.TYPE, metaClass );
			
			for( Command fieldCommand : body.commands ) {
				for( Parameterized fieldNameParameter : fieldCommand.subject.parameters ) {
					throw new CompileError("Field name cannot have parameters", fieldNameParameter.sLoc );
				}
				
				String foreignTypeName = null;
				Block referenceBody = null;
				ArrayList<Modifier> _fieldModifiers = new ArrayList<Modifier>();
				
				boolean isReferenceField = false;
				ArrayList<Parameterized> normalModifiers = new ArrayList<Parameterized>();
				for( Parameterized mod : fieldCommand.modifiers ) {
					if( "reference".equals(mod.subject.unquotedText()) ) {
						if( mod.parameters.length != 1 ) {
							throw new CompileError(
								"'reference' field modifier takes a single parameter: "+
								"the name of the type being referenced.  Got "+
								mod.parameters.length+" parameters", mod.sLoc
							);
						}
						foreignTypeName = singleString( mod.parameters[0] ,"foreign type name" );
						if( fieldCommand.body == null ) {
							throw new CompileError(
								"'reference' field specification requires a block", fieldCommand.sLoc
							);
						}
						if( fieldCommand.body.commands.length == 0 ) {
							throw new CompileError(
								"'reference' field specificatino requires at least one foreign key component",
								fieldCommand.body.sLoc
							);
						}
						isReferenceField = true;
						referenceBody = fieldCommand.body;
					} else {
						normalModifiers.add(mod);
					}
				}
				
				SymbolLookupContext<ModifierSpec> modifierLookupContext = isReferenceField ? referenceModifiers : fieldModifiers;
				
				for( Parameterized mod : normalModifiers ) {
					ModifierSpec ms = modifierLookupContext.get(mod.subject);
					_fieldModifiers.add( ms.bind( SchemaInterpreter.this, mod.parameters, mod.sLoc ) );
				}
				
				if( referenceBody != null ) {
					String fieldName = fieldCommand.subject.subject.unquotedText();
					
					assert foreignTypeName != null;
					ComplexType foreignType = foreignTypeName.equals(name) ? t : (ComplexType)types.get(foreignTypeName);
					
					ArrayList<ForeignKeySpec.Component> fkComponents = new ArrayList<ForeignKeySpec.Component>();
					for( Command fkCommand : referenceBody.commands ) {
						String foreignFieldName = singleString( fkCommand.subject, "foreign field name" );
						FieldSpec foreignField = foreignType.getField(foreignFieldName);
						if( foreignField == null ) {
							throw new CompileError("Foreign key constraint references non-existent field '"+foreignFieldName+"' on type '"+foreignTypeName+"'", fkCommand.subject.sLoc);
						}
						
						Command localFieldNode;
						if( fkCommand.body.commands.length > 0 ) {
							if( fkCommand.body.commands.length != 1 ) {
								throw new CompileError(
									"Foreign key component requires exactly 0 or 1 local field specifications; given "+
									fkCommand.body.commands.length, fkCommand.body.sLoc
								);
							}
							if( fkCommand.modifiers.length != 0 ){
								throw new CompileError(
									"Modifiers not allowed for foreign field specification",
									fkCommand.modifiers[0].sLoc
								);
							}
							localFieldNode = fkCommand.body.commands[0];
						} else {
							localFieldNode = fkCommand;
						}
						
						Type foreignFieldType = foreignField.getObjectType();
						
						String localFieldName = singleString(localFieldNode.subject, "local field name");
						FieldSpec localField = getSimpleField( t, localFieldNode );
						
						// If the local field already specifies a type, it must match
						// the foreign field's type:
						for( Type localFieldType : localField.getObjectTypes() ) {
							if( localFieldType != foreignFieldType ) {
								throw new CompileError(
									"Local copy of reference field '"+localFieldName+"' "+
									"specifies a type ("+localFieldType.getName()+") "+
									"that is different than the foreign field's type ("+foreignFieldType.getName()+")",
									localFieldNode.sLoc
								);
							}
						}
						PropertyUtil.add( localField.getProperties(), Core.VALUE_TYPE, foreignFieldType );
						
						fkComponents.add( new ForeignKeySpec.Component(foreignField, localField) );
						
						for( Modifier m : _fieldModifiers ) {
							// This is here so that key(index) or like such as modifiers have their effects
							// transferred from the reference field to its constituent parts.
							// It might make sense to disallow other types of modifiers here.
							switch( m.getTarget() ) {
							case REFERENCE: break;
							default:
								if( m instanceof FieldModifier ) {
									((FieldModifier)m).apply( t, localField );
								} else {
									m.apply( localField );
								}
							}
						}
					}
					
					ForeignKeySpec fk = new ForeignKeySpec(fieldName, foreignType, fkComponents, fieldCommand.sLoc);
					
					for( Modifier m : _fieldModifiers ) if( m.getTarget() == Modifier.ApplicationTarget.REFERENCE ) {
						m.apply( fk );
					}
					
					t.addForeignKey( fk );
				} else {
					// Note that in this case, _fieldModifiers is ignored; defineSimpleField does its own gathering.
					defineSimpleField( t, fieldCommand );
				}
			}
			
			for( Parameterized mod : modifiers ) {
				ModifierSpec m = classModifiers.get(mod.subject);
				m.bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(t);
			}
			
			if( isTrue(t, RDB.IS_SELF_KEYED) ) {
				t.addIndex(new IndexSpec("primary", t.getFields(), sLoc));
			}
			
			return t;
		}

		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError {
			Type t = parseClass( name, modifiers, body, sLoc );
			
			defineType( t, allowRedefinition );
			CommandInterpreter instanceInterpreter;
			if( PropertyUtil.getFirstInheritedValue(t, Core.EXTENDS, (SchemaObject)null) == Types.CLASS ) {
				// If the defined class extends class, then instances will themselves be classes
				// and can use ClassDefinitionCommandInterpreter
				instanceInterpreter = new ClassDefinitionCommandInterpreter( t );
			} else {
				// Otherwise instances are just generic objects and
				// will use the plain old object interpreter
				instanceInterpreter = new ObjectCommandInterpreter();
			}
			commandInterpreters.put(t.getName(), instanceInterpreter, allowRedefinition, t.getSourceLocation());
		}
	}
	
	class EnumDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
		private EnumType parseEnum( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws CompileError {
			EnumType t = new EnumType(name, sLoc);
			
			for( Parameterized mod : modifiers ) {
				classModifiers.get(mod.subject).bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(t);
			}
			
			for( Command c : body.commands ) {
				ensureNoParameters(c.subject, "enum value");
				if( c.body.commands.length > 0 ) {
					throw new CompileError("Enum value body is ignored", c.body.sLoc);
				}
				
				SchemaObject obj = t.addValidValue(c.subject.subject.unquotedText(), c.sLoc);
				for( Parameterized mod : c.modifiers ) {
					generalModifiers.get(mod.subject).bind(SchemaInterpreter.this, mod.parameters, mod.sLoc).apply(obj);
				}
			}
			
			return t;
		}
		
		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError {
			defineType( parseEnum( name, modifiers, body, sLoc ), allowRedefinition );
		}
	}
	
	class ObjectCommandInterpreter extends DefinitionCommandInterpreter {
		public ObjectCommandInterpreter() { }
		
		protected void defineObject( SchemaObject obj, boolean allowRedefinition ) throws CompileError {
			things.put( obj.getName(), obj, allowRedefinition, obj.getSourceLocation() );
			_data( obj );
		}
		
		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError {
			defineObject( parseObject( name, modifiers, body, sLoc ), allowRedefinition );
		}
	}
	
	class PropertyDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
		final SymbolLookupContext<ModifierSpec> modifierLookupContext;
		final Modifier.ApplicationTarget at;
		
		public PropertyDefinitionCommandInterpreter( SymbolLookupContext<ModifierSpec> modifierLookupContext, Modifier.ApplicationTarget at ) {
			this.modifierLookupContext = modifierLookupContext;
			this.at = at;
		}
		
		protected void definePredicate( Predicate pred, boolean allowRedefinition ) throws CompileError {
			predicates.put( pred.getName(), pred, allowRedefinition, pred.getSourceLocation() );
			defineModifier( modifierLookupContext, pred.getName(), new SimplePredicateModifierSpec(pred, at), allowRedefinition, pred.getSourceLocation() );
			_data( pred );
		}
		
		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError {
			definePredicate( parseProperty( name, modifiers, body, sLoc ), allowRedefinition );
		}
	}
	
	class ModifierDefinitionCommandInterpreter extends DefinitionCommandInterpreter {
		final SymbolLookupContext<ModifierSpec> registeredModifiers;
		public ModifierDefinitionCommandInterpreter( SymbolLookupContext<ModifierSpec> predefinedModifiers ) {
			this.registeredModifiers = predefinedModifiers;
		}
		
		@Override public void interpretDefinition( String name, Parameterized[] modifiers, Block body, boolean allowRedefinition, SourceLocation sLoc ) throws CompileError {
			defineModifier( registeredModifiers, name, parseModifierSpec(name, modifiers, body, registeredModifiers), allowRedefinition, sLoc );
		}
	}
	
	//// Modifiers
	
	interface ModifierSpec {
		public Modifier bind( SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc ) throws CompileError;
	}
	
	interface Modifier {
		/**
		 * Sometimes the thing which is being modified is ambiguous.
		 * Modifiers return one of these to give a hint.
		 */
		enum ApplicationTarget {
			WHATEVER,
			CLASS,
			FIELD,
			REFERENCE
		}
		
		public ApplicationTarget getTarget();
		public void apply( SchemaObject subject );
	}
	
	interface FieldModifier extends Modifier {
		public void apply( ComplexType classObject, FieldSpec fieldSpec );
	}
	
	public static class SimplePredicateModifierSpec implements ModifierSpec {
		final Predicate predicate;
		final Modifier.ApplicationTarget at;
		
		public SimplePredicateModifierSpec( Predicate p, Modifier.ApplicationTarget at ) {
			this.predicate = p;
			this.at = at;
		}
		
		@Override public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws CompileError {
			final Set<SchemaObject> values = new LinkedHashSet<SchemaObject>();
			if( params.length == 0 ) {
				values.add( BaseSchemaObject.forScalar(Boolean.TRUE, sLoc) );
			} else {
				for( Parameterized p : params ) {
					values.add( sp.evaluate( predicate, p ) );
				}
			}
			
			return new Modifier() {
				@Override public ApplicationTarget getTarget() {
					return at;
				}
				@Override public void apply(SchemaObject subject) {
					PropertyUtil.addAll( subject.getProperties(), predicate, values );					
				}
			};
		}
	}
	
	/** Modifier that is equivalent to a set of property -> value pairs */
	public static class AliasModifier implements Modifier, ModifierSpec {
		final String name;
		final Map<Predicate,Set<SchemaObject>> propertyValues;
		
		public AliasModifier( String name, Map<Predicate,Set<SchemaObject>> propertyValues ) {
			this.name = name;
			this.propertyValues = propertyValues;
		}
		
		@Override
		public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws CompileError {
			if( params.length > 0 ) {
				throw new CompileError(name+" modifier takes no arguments", params[0].sLoc);
			}
			return this;
		}
		
		@Override
		public ApplicationTarget getTarget() {
			// With the current architecture it's not possible to have multiple
			// targets in an alias.  :P
			return Modifier.ApplicationTarget.WHATEVER;
		}
		
		@Override
		public void apply(SchemaObject subject) {
			PropertyUtil.addAll( subject.getProperties(), propertyValues );
		}
	}
	
	public static class FieldIndexModifierSpec implements ModifierSpec {
		public static FieldIndexModifierSpec INSTANCE = new FieldIndexModifierSpec();
		
		@Override
		public Modifier bind(SchemaInterpreter sp, Parameterized[] params, final SourceLocation sLoc) throws CompileError {
			final ArrayList<String> indexNames = new ArrayList<String>();
			
			if( params.length == 0 ) {
				throw new CompileError("Index modifier with no arguments is useless", sLoc);
			}
			for( Parameterized indexParam : params ) {
				for( Parameterized indexParamParam : indexParam.parameters ) {
					throw new CompileError("Index name takes no parameters, but parameters given", indexParamParam.sLoc);
				}
				indexNames.add( indexParam.subject.unquotedText() );
			}

			return new FieldModifier() {
				@Override public ApplicationTarget getTarget() {
					return Modifier.ApplicationTarget.FIELD;
				}
				
				@Override
				public void apply(SchemaObject subject) {
					throw new UnsupportedOperationException("Index field modifier doesn't support apply(subject) -- use the other apply method");
				}
				
				@Override
				public void apply(ComplexType classObject, FieldSpec fieldSpec) {
					for( String indexName : indexNames ) {
						IndexSpec index = classObject.getIndex(indexName);
						if( index == null ) {
							index = new IndexSpec(indexName, sLoc);
							classObject.addIndex(index);
						}
						index.fields.add( fieldSpec );
					}
				}
			};
		}
	}
	
	/**
	 * Used to specify an new (anonymous) enum type for a single field
	 */
	public static class EnumModifierSpec implements ModifierSpec {
		final Predicate pred;
		public EnumModifierSpec( Predicate pred ) {
			this.pred = pred;
		}
		
		@Override
		public Modifier bind(SchemaInterpreter sp, final Parameterized[] params, final SourceLocation sLoc) throws CompileError {
			for( Parameterized p : params ) {
				for( Parameterized pp : p.parameters ) {
					throw new CompileError( "Enum values cannot themselves take parameters", pp.sLoc);
				}
			}
			return new Modifier() {
				@Override public ApplicationTarget getTarget() {
					return Modifier.ApplicationTarget.FIELD;
				}
				
				@Override public void apply(SchemaObject subject) {
					EnumType er = new EnumType(subject.getName(), sLoc);
					for( Parameterized p : params ) {
						er.addValidValue(p.subject.unquotedText(), p.subject.sLoc);
					}
					PropertyUtil.add( subject.getProperties(), pred, er );
				}
			};
		}
	}
	
	////
	
	class SymbolLookupContext<T> {
		final SymbolLookupContext<? super T> parent;
		final String name;
		final Map<String,T> values = new HashMap<String,T>();
		final Class<T> valueClass;
		SymbolLookupContext( SymbolLookupContext<? super T> parent, String name, Class<T> valueClass ) {
			this.parent = parent;
			this.name = name;
			this.valueClass = valueClass;
		}
		
		protected void dump( PrintStream dest ) {
			SymbolLookupContext<?> ctx = this;
			while( ctx != null ) {
				dest.println(ctx.name+" context");
				for( Map.Entry<String,?> e : ctx.values.entrySet() ) {
					dest.println("  "+e.getKey()+" : "+e.getValue()+" ("+e.getValue().getClass().getName()+")");
				}
				ctx = ctx.parent;
			}
		}
		
		public boolean isDefined( String name ) {
			SymbolLookupContext<? super T> ctx = this;
			while( ctx != null ) {
				if( ctx.values.containsKey(name) ) return true;
				ctx = ctx.parent;
			}
			return false;
		}
		
		public T get(String name, Class<T> requiredType, boolean throwOnNotFound, boolean throwOnWrongType, SourceLocation refLoc) throws CompileError {
			SymbolLookupContext<? super T> ctx = this;
			while( ctx != null ) {
				Object o = ctx.values.get(name);
				if( o != null ) {
					if( requiredType.isInstance(o) ) {
						return requiredType.cast(o);
					} else {
						if( throwOnWrongType ) {
							throw new CompileError(this.name + " '"+name+"' is not a "+requiredType.getName()+", but "+o.getClass().getName(), refLoc);
						}
						return null;
					}
				}
				ctx = ctx.parent;
			}
			if( throwOnNotFound ) {
				//dump( System.err );
				throw new CompileError("'"+name+"' is not defined as a "+this.name, refLoc);
			}
			return null;
		}
		
		public T get(Phrase p) throws CompileError {
			return get(p.unquotedText(), valueClass, true, true, p.sLoc); 
		}
		
		public T get(String name) throws CompileError {
			return get(name, valueClass, true, true, BaseSourceLocation.NONE); 
		}
		
		public void put(String name, T value, boolean allowRedefinition, SourceLocation sLoc) throws RedefinitionError {
			if( isDefined(name) && !allowRedefinition ) {
				throw new RedefinitionError("Redefining "+this.name+" '"+name+"'", sLoc);
			}
			
			SymbolLookupContext<? super T> ctx = this;
			while( ctx != null ) {
				ctx.values.put(name, value);
				ctx = ctx.parent;
			}
		}
	}
	
	protected SymbolLookupContext<SchemaObject> things = new SymbolLookupContext<SchemaObject>(null, "thing", SchemaObject.class);
	protected SymbolLookupContext<Type> types = new SymbolLookupContext<Type>(things, "type", Type.class);
	protected SymbolLookupContext<Predicate> predicates = new SymbolLookupContext<Predicate>(things, "predicate", Predicate.class);
	protected SymbolLookupContext<ModifierSpec> generalModifiers = new SymbolLookupContext<ModifierSpec>(null, "general modifier", ModifierSpec.class);
	protected SymbolLookupContext<ModifierSpec> fieldModifiers = new SymbolLookupContext<ModifierSpec>(generalModifiers, "field modifier", ModifierSpec.class);
	protected SymbolLookupContext<ModifierSpec> referenceModifiers = new SymbolLookupContext<ModifierSpec>(fieldModifiers, "reference modifier", ModifierSpec.class);
	protected SymbolLookupContext<ModifierSpec> classModifiers = new SymbolLookupContext<ModifierSpec>(generalModifiers, "class modifier", ModifierSpec.class);
	protected SymbolLookupContext<CommandInterpreter> commandInterpreters = new SymbolLookupContext<CommandInterpreter>(null, "command interpreter", CommandInterpreter.class);
	protected Namespace importables = new Namespace("");
	
	public SchemaInterpreter() { }
	
	public void defineThing( String name, SchemaObject v, boolean allowRedefinition ) throws CompileError {
		things.put(v.getName(), v, allowRedefinition, v.getSourceLocation());
		_data( v );
	}
	
	public void defineThing( SchemaObject v, boolean allowRedefinition ) throws CompileError {
		defineThing(v.getName(), v, allowRedefinition);
	}
	
	public void defineType( String name, Type t, boolean allowRedefinition ) throws CompileError {
		types.put( name, t, allowRedefinition, t.getSourceLocation() );
		HashMap<Predicate,Set<SchemaObject>> appliedProperties = new HashMap<Predicate,Set<SchemaObject>>();
		PropertyUtil.add(appliedProperties, Core.VALUE_TYPE, t);
		fieldModifiers.put( name, new AliasModifier(Core.VALUE_TYPE.getName(), appliedProperties), allowRedefinition, t.getSourceLocation() );
		_data( t );
	}
	
	public void defineType( Type t, boolean allowRedefinition ) throws CompileError {
		defineType(t.getName(), t, allowRedefinition);
	}
	
	public void defineType( Type t ) throws CompileError {
		defineType( t, false );
	}
	
	boolean allowIsLessModifierShorthand = true;
	
	protected void defineModifier( SymbolLookupContext<ModifierSpec> modifierMap, String name, ModifierSpec spec, boolean allowRedefinition, SourceLocation sLoc )
		throws CompileError 
	{
		modifierMap.put( name, spec, allowRedefinition, sLoc );
		if( allowIsLessModifierShorthand && name.startsWith("is ") ) {
			String shorthand = name.substring(3);
			//System.err.println("Duplicate '"+name+"' as '"+shorthand+"'");
			if( !modifierMap.isDefined(shorthand) ) { 
				modifierMap.put( shorthand, spec, false, sLoc );
			}
		}
	}
	
	public void defineFieldModifier( String name, ModifierSpec spec ) throws CompileError {
		defineModifier(fieldModifiers, name, spec, false, BaseSourceLocation.NONE );
	}
	
	public void defineClassModifier( String name, ModifierSpec spec ) throws CompileError {
		defineModifier(classModifiers, name, spec, false, BaseSourceLocation.NONE );
	}
	
	protected void definePredicate( String name, Predicate pred, SymbolLookupContext<ModifierSpec> modifierContext, Modifier.ApplicationTarget at, boolean allowRedefinition )
		throws CompileError
	{
		predicates.put( name, pred, false, pred.getSourceLocation() );
		defineModifier( modifierContext, name, new SimplePredicateModifierSpec(pred, at), false, pred.getSourceLocation() );
		_data( pred );
	}

	protected void definePredicate( Predicate pred, SymbolLookupContext<ModifierSpec> modifierContext, Modifier.ApplicationTarget at, boolean allowRedefinition )
		throws CompileError
	{
		definePredicate( pred.getName(), pred, modifierContext, at, allowRedefinition );
	}
	
	public void defineReferencePredicate( Predicate pred ) throws CompileError {
		definePredicate( pred, referenceModifiers, Modifier.ApplicationTarget.REFERENCE, false );
	}
	
	public void defineFieldPredicate( Predicate pred ) throws CompileError {
		definePredicate( pred, fieldModifiers, Modifier.ApplicationTarget.FIELD, false );
	}
	
	public void defineClassPredicate( Predicate pred ) throws CompileError {
		definePredicate( pred, classModifiers, Modifier.ApplicationTarget.WHATEVER, false );
	}
	
	public void defineGenericPredicate( Predicate pred ) throws CompileError {
		definePredicate( pred, generalModifiers, Modifier.ApplicationTarget.WHATEVER, false );
	}
	
	public void defineCommand( String name, CommandInterpreter interpreter, boolean allowRedefinition, SourceLocation sLoc )
		throws CompileError
	{
		commandInterpreters.put( name, interpreter, allowRedefinition, sLoc );
	}
	
	public void defineCommand( String name, CommandInterpreter interpreter ) {
		try {
			commandInterpreters.put( name, interpreter, false, BaseSourceLocation.NONE );
		} catch( CompileError e ) {
			throw new RuntimeException(e);
		}
	}
	
	public void defineSomething( String name, Object v, boolean allowRedefinition, SourceLocation sLoc )
		throws CompileError
	{
		// TODO: support modifiers
		if( v instanceof Predicate ) {
			definePredicate( name, (Predicate)v, generalModifiers, ApplicationTarget.WHATEVER, allowRedefinition );
		} else if( v instanceof Type ) {
			defineType(name, (Type)v, allowRedefinition);
		} else if( v instanceof SchemaObject ) {
			defineThing(name, (SchemaObject)v, allowRedefinition);
		} else {
			throw new CompileError("Don't know how to define a "+v.getClass(), sLoc);
		}
	}
	
	public void defineImportable( Namespace ns ) {
		importables.addNamespace(ns);
	}
	
	protected SchemaObject evaluate( SchemaObject v, Parameterized[] parameters ) throws CompileError {
		if( parameters.length > 0 ) {
			throw new CompileError("Don't know how to parameterize "+v.getClass(), parameters[0].sLoc);
		}
		return v;
	}
	
	/**
	 * @param context predicate whose object we are evaluating; may be null
	 * @param p Parameterized representation of the value
	 * @return
	 * @throws CompileError
	 */
	protected SchemaObject evaluate( Predicate context, Parameterized p ) throws CompileError {
		if( p.subject.words.length == 1 && p.subject.words[0].quoting == Token.Type.DOUBLE_QUOTED_STRING ) {
			return BaseSchemaObject.forScalar(p.subject.unquotedText(), p.subject.sLoc);
		}
		// TODO: Parse number literals
		
		String name = p.subject.unquotedText();
		Set<SchemaObject> possibleValues = new LinkedHashSet<SchemaObject>();
		if( context != null ) {
			for( Type t : context.getObjectTypes() ) {
				if( t instanceof EnumType ) {
					for( SchemaObject enumValue : ((EnumType)t).getValidValues() ) {
						if( name.equals(enumValue.getName()) ) {
							possibleValues.add(enumValue);
						}
					}
				}
			}
		}
		
		if( possibleValues.size() == 0 ) {
			SchemaObject v = things.get(p.subject);
			if( v != null ) possibleValues.add( v );
		}
		
		if( possibleValues.size() == 0 ) {
			throw new CompileError("Unrecognized symbol: "+Word.quote(name), p.subject.sLoc);
		} else if( possibleValues.size() > 1 ) {
			// TODO: list definition locations
			throw new CompileError("Symbol "+Word.quote(name)+" is ambiguous", p.subject.sLoc);
		} else {
			for( SchemaObject v : possibleValues ) {
				return evaluate( v, p.parameters );
			}
			throw new RuntimeException("Somehow foreach body wasn't evaluated for single-item set");
		}
	}
	
	protected FieldSpec defineSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws CompileError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		if( objectType.hasField(fieldName) ) {
			throw new CompileError( "Field '"+fieldName+"' already defined", fieldCommand.sLoc );
		}
		
		FieldSpec fieldSpec = new FieldSpec( fieldName, fieldCommand.sLoc );
		
		for( Parameterized modifier : fieldCommand.modifiers ) {
			ModifierSpec ms = fieldModifiers.get(modifier.subject);
			Modifier m = ms.bind( this, modifier.parameters, modifier.sLoc );
			applyFieldModifier( m, objectType, fieldSpec );
		}
		
		objectType.addField( fieldSpec );
		return fieldSpec;
	}
	
	protected FieldSpec getSimpleField(
		ComplexType objectType,
		Command fieldCommand
	) throws CompileError {
		String fieldName = singleString(fieldCommand.subject, "field name");
		FieldSpec fieldSpec = objectType.getField(fieldName);
		if( fieldSpec == null ) {
			fieldSpec = defineSimpleField( objectType, fieldCommand );
		} else if( fieldCommand.modifiers.length > 0 ) {
			throw new CompileError( "Cannot redefine field '"+fieldName+"'", fieldCommand.sLoc );
		}
		return fieldSpec;
	}
	
	protected SchemaObject parseObject( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws CompileError {
		BaseSchemaObject obj = new BaseSchemaObject( name, sLoc );
		for( Parameterized p : modifiers ) {
			ModifierSpec ms = generalModifiers.get(p.subject);
			ms.bind(this, p.parameters, p.sLoc).apply(obj);
		}
		for( Command c : body.commands ) {
			throw new CompileError("Object literals cannot have a block", c.sLoc );
		}
		return obj;
	}
	
	protected Predicate parseProperty( String name, Parameterized[] modifiers, Block body, SourceLocation sLoc ) throws CompileError {
		Predicate pred = new Predicate(name, sLoc);
		for( Parameterized p : modifiers ) {
			ModifierSpec ms = fieldModifiers.get(p.subject);
			ms.bind(this, p.parameters, p.sLoc).apply(pred);
		}
		for( Command c : body.commands ) {
			throw new CompileError("Property definitions cannot have a block", c.sLoc );
		}
		return pred;
	}
	
	private ModifierSpec parseModifierSpec(final String name, Parameterized[] modifierModifiers, Block body, final SymbolLookupContext<ModifierSpec> predefinedModifiers) throws CompileError {
		final ArrayList<Modifier> subModifiers = new ArrayList<Modifier>();
		if( body.commands.length != 1 ) {
			throw new CompileError("modifier definition must have exactly 1 command, "+body.commands.length+" given",
				body.commands.length == 0 ? body.sLoc : body.commands[1].sLoc );
		}
		for( Command cmd : body.commands ) {
			for( Parameterized p : cmd.getSubjectAndModifiers() ) {
				ModifierSpec ms = predefinedModifiers.get(p.subject);
				subModifiers.add( ms.bind(this, p.parameters, p.sLoc) );
			}
		}
		return new ModifierSpec() {
			@Override
			public Modifier bind(SchemaInterpreter sp, Parameterized[] params, SourceLocation sLoc) throws CompileError {
				if( params.length > 0 ) {
					throw new CompileError("Custom "+predefinedModifiers.name+" '"+name+"' takes no parameters", sLoc);
				}
				return new FieldModifier() {
					@Override public ApplicationTarget getTarget() {
						// Might want to allow specification of this somehow, someday
						return ApplicationTarget.WHATEVER;
					}
					
					@Override
					public void apply( ComplexType classObject, FieldSpec fieldSpec ) {
						for( Modifier m : subModifiers ) {
							applyFieldModifier( m, classObject, fieldSpec );
						}
					}
					
					@Override public void apply( SchemaObject subject ) {
						for( Modifier m : subModifiers ) {
							m.apply(subject);
						}
					}
				};
			}
		};
	}
	
	protected void ensureNoParameters( Parameterized s, String context ) throws CompileError {
		for( Parameterized classNameParameter : s.parameters ) {
			throw new CompileError(context + " cannot have parameters", classNameParameter.sLoc );
		}
	}
	
	@Override public void data(Command value) throws CompileError {
		Phrase cmd = value.subject.subject;
		for( int prefixLength = cmd.words.length; prefixLength >= 1; --prefixLength ) {
			Phrase typeName = cmd.head(prefixLength);
			if( typeName.words.length >= 2 && typeName.startsWithWord("redefine") ) {
				typeName = typeName.tail(1);
			}
			CommandInterpreter ci = commandInterpreters.get(typeName.unquotedText(), CommandInterpreter.class, false, false, value.sLoc);
			if( ci != null && ci.interpretCommand(value, prefixLength) ) {
				return;
			}
		}
		
		/*
		if( cmd.words.length >= 2 ) {
			Phrase typeName = cmd.head(cmd.words.length-1);
			if( cmd.words.length >= 3 && cmd.startsWithWord("redefine") ) {
				typeName = typeName.tail(1);
			}
			CommandInterpreter ci = commandInterpreters.get(typeName);
			if( ci.interpretCommand(value, cmd.words.length-1) ) {
				return;
			}
		}
		*/
		
		throw new CompileError("Unrecognised command: '"+cmd+"'", value.sLoc);
	}

	@Override public void end() throws CompileError {
		_end();
	}
	
	//// Convenience methods for when you don't feel like setting
	//// up your own tokenizers and yaddah yaddah yaddah
	
	public void parse( Reader r, String sourceName ) throws IOException, ScriptError {
		Tokenizer t = new Tokenizer();
		if( sourceName != null ) t.setSourceLocation( sourceName, 1, 1 );
		Parser p = new Parser();
		p.pipe(this);
		t.pipe(p);
		try {
			StreamUtil.pipe( r, t, true );
		} catch( CompileError e ) {
			throw e;
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
	
	public void parse( String source, String sourceName ) throws ScriptError {
		try {
			parse( new StringReader(source), sourceName );
		} catch( IOException e ) {
			throw new RuntimeException(e);
		}
	}
}

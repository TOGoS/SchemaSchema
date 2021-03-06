package togos.schemaschema.parser;

import togos.schemaschema.parser.SchemaInterpreter.Modifier;


public class CommandInterpreters
{
	private CommandInterpreters() { }
	
	public static void defineUndefineCommand( SchemaInterpreter sp ) {
		sp.defineCommand("undefine", sp.new UndefineCommandInterpreter());
	}
	
	public static void defineAliasCommand( SchemaInterpreter sp ) {
		sp.defineCommand("alias", sp.new AliasCommandInterpreter());
	}
	
	public static void defineImportCommand( SchemaInterpreter sp ) {
		sp.defineCommand("import", sp.new ImportCommandInterpreter());
	}
	
	public static void defineExtensionCommand( SchemaInterpreter sp ) {
		sp.defineCommand("extend", sp.new ExtensionCommandInterpreter());
	}
	
	public static void defineTypeDefinitionCommands( SchemaInterpreter sp ) {
		sp.defineCommand("class", sp.new ClassDefinitionCommandInterpreter());
		sp.defineCommand("enum", sp.new EnumDefinitionCommandInterpreter());
		sp.defineCommand("property", sp.new PropertyDefinitionCommandInterpreter(sp.generalModifiers, Modifier.ApplicationTarget.WHATEVER));
		sp.defineCommand("modifier", sp.new ModifierDefinitionCommandInterpreter(sp.generalModifiers));
		sp.defineCommand("field property", sp.new PropertyDefinitionCommandInterpreter(sp.fieldModifiers, Modifier.ApplicationTarget.FIELD));
		sp.defineCommand("reference property", sp.new PropertyDefinitionCommandInterpreter(sp.referenceModifiers, Modifier.ApplicationTarget.REFERENCE));
		sp.defineCommand("class property", sp.new PropertyDefinitionCommandInterpreter(sp.classModifiers, Modifier.ApplicationTarget.CLASS));
		sp.defineCommand("field modifier", sp.new ModifierDefinitionCommandInterpreter(sp.fieldModifiers));
		sp.defineCommand("class modifier", sp.new ModifierDefinitionCommandInterpreter(sp.classModifiers));
	}
}

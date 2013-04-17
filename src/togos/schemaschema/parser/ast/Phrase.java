package togos.schemaschema.parser.ast;

import java.util.Arrays;

import togos.lang.BaseSourceLocation;
import togos.lang.SourceLocation;
import togos.schemaschema.parser.Tokenizer;

public class Phrase extends ASTNode
{
	public static final Phrase EMPTY = new Phrase(new Word[0]);
	
	public static String quoteIfNecessary( String text ) {
		for( char c : text.toCharArray() ) {
			if( c != ' ' && !Tokenizer.isWordChar(c) ) {
				return Word.quote(text);
			}
		}
		return text;
	}
	
	public final Word[] words;
	
	public Phrase( Word[] words ) {
		super( words.length == 0 ? BaseSourceLocation.NONE : words[0].sLoc );
		this.words = words;
	}

	public Phrase( Word[] words, SourceLocation sLoc ) {
		super( sLoc );
		this.words = words;
	}
	
	public String toString() {
		String s = "";
		for( Word w : words ) {
			if( s.length() > 0 ) s += " ";
			s += w.toString();
		}
		return s;
	}
	
	public boolean startsWithWord( String word ) {
		return (words.length > 0 && words[0].text.equals(word));
	}
	
	public boolean startsWithWords( String... w ) {
		if( words.length < w.length ) return false;
		for( int i=0; i<w.length; ++i ) {
			if( !words[i].text.equals(w[i]) ) return false;
		}
		return true;
	}
	
	public Phrase tail() {
		return tail(1);
	}
	
	public Phrase head( int length ) {
		if( words.length < length ) length = words.length;
		return new Phrase( Arrays.copyOfRange(words, 0, length));
	}

	public Phrase tail( int skip ) {
		if( words.length <= skip ) return EMPTY;
		return new Phrase( Arrays.copyOfRange(words, skip, words.length));
	}
	
	public String unquotedText() {
		String s = "";
		for( Word w : words ) {
			if( s.length() > 0 ) s += " ";
			s += w.text;
		}
		return s;
	}
}

package assignment3;

import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;

import java.io.IOException;
import java.io.*;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.*;

import java.lang.Error;
import java.util.stream.Collectors; 


// ***********************************
public class LiveOak3Compiler
{
	public static void main(String[] args) throws IOException { 		
		String infile = args[0];
		String outfile = args[1]; 

		String samcode = compiler(infile);

		Writer writer = null;
		try{
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "utf-8"));
			writer.write(samcode);
		} 
		catch( IOException e ){
			// e.getMessage();
		} 
		finally {
			try{
				writer.close();
			} 
			catch( Exception e ) {
				System.out.println( e.getMessage() );
			}
		}
	}

  
// ***********************************
	static String compiler(String fileName) throws Error {
		try 
		{		
			// reset StaticInfo 
			StaticInfo.InputFileName = fileName;
			StaticInfo.LabelCnt = 0;

			StaticInfo.MethodIndexMap = new HashMap<String, Integer>();
			StaticInfo.ClassIndexMap = new HashMap<String, Integer>();

			StaticInfo.SymbolTable = new ArrayList<MethodInfo>();
			StaticInfo.SymbolTable2 = new ArrayList<ClassInfo>();

			StaticInfo.WhileLoopEndLabelStack = new Stack<String>();

			SamTokenizer f = new SamTokenizer(fileName, SamTokenizer.TokenizerOptions.PROCESS_STRINGS);    
		
			ProgramNode program_node = get_ProgramNode(f);  
			// System.out.println("=================== BUILD ABSTRACT SYNTAX TREE - DONE " );

			if( !StaticInfo.ClassIndexMap.containsKey("Main") )
				StaticInfo.printError("missing Main class");

			if( !StaticInfo.MethodIndexMap.containsKey("Main__main") )
				StaticInfo.printError("missing main method");

			program_node.examine();

			String pgm = program_node.get_SamCode();	 

			ClassInfo class_info = StaticInfo.SymbolTable2.get( StaticInfo.ClassIndexMap.get("Main") );
			int num_classVar = class_info.classVarID.size();			
		
			String res = "";
			res += "PUSHIMM 0\n";   
			res += "PUSHIMM " + num_classVar + "\n";
			res += "MALLOC \n";
 
			res += "LINK \n";
			res += "JSR " + "Main__main" + " \n" ;
			res += "POPFBR\n";
			res += "ADDSP -1 \n";
			res += "STOP \n";

			res += pgm;
  
			res += StrOper.get_SamCode_str_rev(); 
			res += StrOper.get_SamCode_str_concat(); 
			res += StrOper.get_SamCode_str_cmp(); 
			res += StrOper.get_SamCode_str_repeat(); 
			// System.out.println("=================== SAM CODE GENERATION - DONE " );

			return res;
		} 
		catch (Exception e) 
		{
			System.err.println("Failed to compile " + StaticInfo.InputFileName); 
			System.out.println(e.getMessage());
 			throw new Error("");
		}
	}




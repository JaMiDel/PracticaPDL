package Objetos;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Writter {

	private String nameWriter;
	
	private Writer fileWriter;
	private Writer consoleWriter;
	private Document windowsWriter;
	
	public Writter(String name){
		this.nameWriter = name;
		
		try {
			this.fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.nameWriter +  ".txt"), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// CHECK:
			// e.printStackTrace();
		} catch (FileNotFoundException e) {
			// CHECK:
			// e.printStackTrace();
		}
	}
	
	public void setConsole(Object console){
		if(console instanceof OutputStream){
			this.consoleWriter = new BufferedWriter(new OutputStreamWriter(System.out));
		} else if (console instanceof Document) {
			this.windowsWriter = (Document) console;
		}
	}
	
	public boolean write(Object message){
		boolean status = false;
		try {
			
			this.fileWriter.write(message + "\n");
			this.fileWriter.flush();
			this.writeConsole(message);
			status = true;
		} catch (IOException e) {
			// CHECK:
			// e.printStackTrace();
		}
		return status;
	}
	
	private void writeConsole(Object message) throws IOException {
		if (this.consoleWriter != null){
			this.consoleWriter.write(message + System.lineSeparator());
			this.consoleWriter.flush();
		} else if (this.windowsWriter != null){
			try {
				this.windowsWriter.insertString(this.windowsWriter.getLength(), message + System.lineSeparator(), null);
			} catch (BadLocationException e) {
				// CHECK:
				// e.printStackTrace();
			}
		}	
	}

	public boolean save(){
		boolean status = false;
		try {
			this.fileWriter.close();
			status = true;
		} catch (IOException e) {
			// CHECK:
			// e.printStackTrace();
		}
		return status;
	}
	
}

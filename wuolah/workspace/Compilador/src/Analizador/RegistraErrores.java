package Analizador;

import Objetos.Writter;

public class RegistraErrores{
	@SuppressWarnings("unused")
	private String mensaje;
	@SuppressWarnings("unused")
	private int linea;
	private Writter errWritter;
	public RegistraErrores(String msg){
		this.errWritter = new Writter(msg);
	}
	public void write(String msg){
		this.errWritter.write(msg);
	}
}
package Analizador;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import Objetos.EntradaAux;
import Objetos.Funcion;
import Objetos.Reader;
import Objetos.Tipo;
import Objetos.Token;
import Objetos.Writter;

public class AnalizadorSintactico {

	private Map<String, ArrayList<Token>> mapFirst;
	private Map<String, ArrayList<Token>> mapFollow;
	private Token actualToken;
	private Token lastToken;
	private Token lastTokenF;
	private ArrayList<Token> auxList;
	private String parse;
	private String tipoF;
	private String args;
	private boolean esFuncion;
	private boolean esEq;
	private boolean esCuerpo;
	private Reader lectura;
	private AnalizadorLexico aLexico;
	private Writter parseWritter;
	private RegistraErrores regErrores;
	private TablaSimbolosControl stHandler;
	private EntradaAux entry = new EntradaAux(Tipo.VACIO, 0);
	private String rutaAbsoluta;

	public AnalizadorSintactico(String ruta, String rutaFichero) {

		this.rutaAbsoluta = rutaFichero;
		this.stHandler = new TablaSimbolosControl(this.rutaAbsoluta);
		// Writter("C:\\Users\\RAYS\\Documents\\Salidas\\parse.txt");
		this.parseWritter = new Writter(rutaFichero + "\\parse");
		this.parse = "Descendente ";
		this.args = "";
		this.tipoF = "";
		this.esFuncion = false;
		this.esEq = false;
		this.esEq = false;
		this.lectura = new Reader(ruta);
		// ErrorControl("C:\\Users\\RAYS\\Documents\\Salidas\\errores.txt");
		this.regErrores = new RegistraErrores(rutaFichero + "\\errores");
		// Writter("C:\\Users\\RAYS\\Documents\\Salidas\\token.txt");
		Writter escritura = new Writter(rutaFichero + "\\token");
		this.aLexico = new AnalizadorLexico(lectura, escritura, stHandler, regErrores);
		this.actualToken = this.aLexico.getToken();

		this.inicializarMaps();
		this.stHandler.createTable(this.rutaAbsoluta);
		this.ProcedureZ();
		this.stHandler.removeTable();
		this.parseWritter.write(parse);
		System.out.println("Archivos creados en ruta: " + this.rutaAbsoluta);
		System.out.println("Finalizado satisfactoriamente");
	}

	// REALIZADO
	private void ProcedureZ() {
		if (this.getFirst("P").contains(this.actualToken)) {
			this.parse += " 1";
			this.ProcedureP();
		} else if (this.actualToken.equals(Token.EOL)) {
			this.parse += " 2";
			this.compToken(Token.EOL);
			this.ProcedureZ();
		} else {
			this.regErrores
					.write(String.format("Linea %d: Analizador Sintactico - Estructura de programa o EOL incorrecta",
							this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureP() {
		if (this.getFirst("B").contains(this.actualToken)) {
			this.parse += " 3";
			this.ProcedureB();
			this.compToken(Token.EOL);
			this.ProcedureZ();
		} else if (this.getFirst("F").contains(this.actualToken)) {
			this.parse += " 4";
			this.ProcedureF();
			this.compToken(Token.EOL);
			this.ProcedureZ();
		} else if (this.actualToken.equals(Token.EOF)) {
			this.parse += " 5";
			this.compToken(Token.EOF);
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Codigo, funcion o EOF incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureB() {
		if (this.actualToken.equals(Token.VAR)) {
			this.parse += " 6";
			this.compToken(Token.VAR);
			this.ProcedureT();
			this.compToken(Token.ID);
			if (this.stHandler.buscarTipoTS((Integer) this.lastToken.getAttribute()).equals(Tipo.UNDEFINED)) {
				this.stHandler.insertarTipoTS((Integer) this.lastToken.getAttribute(), entry.tipo, entry.tam);
			} else {
				this.regErrores.write(String.format("Linea %d: Analizador Semantico - Variable ' %s ' ya declarada",
						this.aLexico.getFileReader().getCurrentLine() - 1,
						this.stHandler.buscarEntradaPorID((Integer) this.lastToken.getAttribute()).getLexema()));
			}
			this.ProcedureD();
		} else if (this.actualToken.equals(Token.WHILE)) {
			this.parse += " 7";
			this.compToken(Token.WHILE);
			this.compToken(Token.PARENTESIS_I);
			this.ProcedureR();
			this.compToken(Token.PARENTESIS_D);
			this.compToken(Token.EOL);
			this.ProcedureI();
		} else if (this.getFirst("S").contains(this.actualToken)) {
			this.parse += " 8";
			this.ProcedureS();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Declaracion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureI() {
		if (this.getFirst("S").contains(this.actualToken)) {
			this.parse += " 9";
			this.ProcedureS();
		} else if (this.actualToken.equals(Token.LLAVE_I)) {
			this.parse += " 10";
			this.compToken(Token.LLAVE_I);
			this.compToken(Token.EOL);
			this.Procedure1();
			this.compToken(Token.LLAVE_D);
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Error en declaracion WHILE",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void Procedure1() {
		if (this.getFirst("B").contains(this.actualToken)) {
			this.parse += " 11";
			this.ProcedureB();
			this.compToken(Token.EOL);
			this.Procedure2();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Sentencia incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void Procedure2() {
		if (this.getFirst("1").contains(this.actualToken)) {
			this.parse += " 12";
			this.Procedure1();
		} else if (this.getFollow("2").contains(this.actualToken)) {
			this.parse += " 13";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Sentencia incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureD() {
		if (this.actualToken.equals(Token.COMA)) {
			this.parse += " 14";
			this.compToken(Token.COMA);
			this.compToken(Token.ID);
			if (this.stHandler.buscarTipoTS((Integer) this.lastToken.getAttribute()).equals(Tipo.UNDEFINED)) {
				this.stHandler.insertarTipoTS((Integer) this.lastToken.getAttribute(), entry.tipo, entry.tam);
			} else {
				this.regErrores.write(String.format("Linea %d: Analizador Semantico - Variable ' %s ' ya declarada",
						this.aLexico.getFileReader().getCurrentLine() - 1,
						this.stHandler.buscarEntradaPorID((Integer) this.lastToken.getAttribute()).getLexema()));
			}
			this.ProcedureD();
		} else if (this.actualToken.equals(Token.ASIGNACION)) {
			this.parse += " 15";
			this.compToken(Token.ASIGNACION);
			this.ProcedureR();
			this.Procedure3();
		} else if (this.getFollow("D").contains(this.actualToken)) {
			this.parse += " 16";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Declaracion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void Procedure3() {
		if (this.actualToken.equals(Token.COMA)) {
			this.parse += " 17";
			this.compToken(Token.COMA);
			this.compToken(Token.ID);
			if (this.stHandler.buscarTipoTS((Integer) this.lastToken.getAttribute()).equals(Tipo.UNDEFINED)) {
				this.stHandler.insertarTipoTS((Integer) this.lastToken.getAttribute(), entry.tipo, entry.tam);
			} else {
				this.regErrores.write(String.format("Linea %d: Analizador Semantico - Variable ' %s ' ya declarada",
						this.aLexico.getFileReader().getCurrentLine() - 1,
						this.stHandler.buscarEntradaPorID((Integer) this.lastToken.getAttribute()).getLexema()));
			}
			this.ProcedureD();
		} else if (this.getFollow("3").contains(this.actualToken)) {
			this.parse += " 18";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Declaracion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureS() {
		if (this.actualToken.equals(Token.ID)) {
			this.parse += " 19";
			this.compToken(Token.ID);
			lastTokenF = this.lastToken;
			this.ProcedureM();
			if (esEq) {
				if (!this.stHandler.buscarTipoTS((Integer) lastTokenF.getAttribute()).equals(entry.tipo)) {
					this.regErrores.write(String.format("Linea %d: Analizador Semantico - Error en asignacion",
							this.aLexico.getFileReader().getCurrentLine() - 1));

				}
			}
			esEq = false;
		} else if (this.actualToken.equals(Token.RETURN)) {
			this.parse += " 20";
			this.compToken(Token.RETURN);
			if (esCuerpo) {
				this.ProcedureX();
				if (!entry.tipo.equals(tipoF) && !tipoF.equals(Tipo.VACIO)) {
					this.regErrores.write(
							String.format("Linea %d: Analizador Semantico - El valor a devolver no es el esperado",
									this.aLexico.getFileReader().getCurrentLine() - 1));
				}
			} else {
				this.regErrores.write(String.format("Linea %d: Analizador Semantico - Uso incorrecto de 'return'",
						this.aLexico.getFileReader().getCurrentLine()));
			}
		} else if (this.actualToken.equals(Token.WRITE)) {
			this.parse += " 21";
			this.compToken(Token.WRITE);
			this.compToken(Token.PARENTESIS_I);
			esFuncion = true;
			this.ProcedureR();
			esFuncion = false;
			this.compToken(Token.PARENTESIS_D);
		} else if (this.actualToken.equals(Token.PROMPT)) {
			this.parse += " 22";
			this.compToken(Token.PROMPT);
			this.compToken(Token.PARENTESIS_I);
			this.compToken(Token.ID);
			this.compToken(Token.PARENTESIS_D);
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Sentencia incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureM() {
		if (this.actualToken.equals(Token.ASIGNACION)) {
			this.parse += " 23";
			this.compToken(Token.ASIGNACION);
			this.ProcedureR();
		} else if (this.actualToken.equals(Token.MAYOR)) {
			this.parse += " 24";
			this.compToken(Token.MAYOR);
			if (entry.tipo.equals(Tipo.INT)) {
				this.compToken(Token.MAYOR);
				this.ProcedureR();
				if (!entry.tipo.equals(Tipo.INT)) {
					this.regErrores.write(String.format("Linea %d: Analizador Sematico - Uso incorrecto de MAYOR '>'",
							this.aLexico.getFileReader().getCurrentLine()));
				}
			}
			entry.tipo = Tipo.BOOL;
		} else if (this.actualToken.equals(Token.PARENTESIS_I)) {
			this.parse += " 25";
			this.esFuncion = true;
			this.compToken(Token.PARENTESIS_I);
			this.ProcedureL();
			this.compToken(Token.PARENTESIS_D);
			this.esFuncion = false;
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Sentencia incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureT() {
		if (this.actualToken.equals(Token.INT)) {
			this.parse += " 26";
			this.compToken(Token.INT);
			entry.tam = 2;
			entry.tipo = "INT";
		} else if (this.actualToken.equals(Token.BOOL)) {
			this.parse += " 27";
			this.compToken(Token.BOOL);
			entry.tam = 1;
			entry.tipo = "BOOL";
		} else if (this.actualToken.equals(Token.CHARS)) {
			this.parse += " 28";
			this.compToken(Token.CHARS);
			entry.tam = 8;
			entry.tipo = "CHARS";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Tipo de dato incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureX() {
		if (this.getFirst("R").contains(this.actualToken)) {
			this.parse += " 29";
			this.ProcedureR();
		} else if (this.getFollow("X").contains(this.actualToken)) {
			this.parse += " 30";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureR() {
		if (this.getFirst("V").contains(this.actualToken)) {
			this.parse += " 31";
			this.ProcedureV();
			this.ProcedureN();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureN() {
		if (this.actualToken.equals(Token.OR)) {
			this.parse += " 32";
			this.compToken(Token.OR);
			this.ProcedureV();
			this.ProcedureN();
		} else if (this.getFollow("N").contains(this.actualToken)) {
			this.parse += " 33";

		} else {
			this.regErrores.write(String.format("Linea %d:  Analizador Semantico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureV() {
		if (this.getFirst("U").contains(this.actualToken)) {
			this.parse += " 34";
			this.ProcedureU();
			this.ProcedureO();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureO() {
		if (this.actualToken.equals(Token.ASIGNACIONRESTO)) {
			this.parse += " 35";
			String tipo_aux = entry.tipo;
			this.compToken(Token.ASIGNACIONRESTO);
			this.ProcedureU();
			if (!entry.tipo.equals(tipo_aux)) {
				this.regErrores.write(String.format(
						"Linea %d: Analizador Semantico - Uso incorrecto del operador ASIGNACION LOGICO '|='",
						this.aLexico.getFileReader().getCurrentLine()));
			}
		} else if (this.getFollow("O").contains(this.actualToken)) {
			this.parse += " 36";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureU() {
		if (this.getFirst("E").contains(this.actualToken)) {
			this.parse += " 37";
			this.ProcedureE();
			this.ProcedureW();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureW() {
		if (this.actualToken.equals(Token.RESTO)) {
			this.parse += " 38";
			if (entry.tipo.equals(Tipo.INT)) {
				this.compToken(Token.RESTO);
				this.ProcedureU();
				if (!entry.tipo.equals(Tipo.INT)) {
					this.regErrores.write(
							String.format("Linea %d: Analizador Semantico - El resto tiene que ser entre enteros",
									this.aLexico.getFileReader().getCurrentLine()));
				}
			}
		} else if (this.getFollow("W").contains(this.actualToken)) {
			this.parse += " 39";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureE() {
		if (this.actualToken.equals(Token.ID)) {
			this.parse += " 40";
			this.compToken(Token.ID);
			if (esFuncion) {
				if (args == "") {
					args = this.stHandler.buscarTipoTS((Integer) lastToken.getAttribute()).toString();
				} else {
					args = args + "," + this.stHandler.buscarTipoTS((Integer) lastToken.getAttribute()).toString();
				}
			}
			entry.tipo = this.stHandler.buscarTipoTS((Integer) lastToken.getAttribute()).toString();
			this.ProcedureY();
		} else if (this.actualToken.equals(Token.PARENTESIS_I)) {
			this.parse += " 41";
			this.compToken(Token.PARENTESIS_I);
			this.ProcedureR();
			this.compToken(Token.PARENTESIS_D);
		} else if (this.actualToken.equals(Token.NUM)) {
			this.parse += " 42";
			this.compToken(Token.NUM);
			if (args == "") {
				args = Tipo.INT;
			} else {
				args = args + "," + Tipo.INT;
			}
			entry.tipo = Tipo.INT;
		} else if (this.actualToken.equals(Token.CAD)) {
			this.parse += " 43";
			this.compToken(Token.CAD);
			if (args == "") {
				args = Tipo.CHARS;
			} else {
				args = args + "," + Tipo.CHARS;
			}
			entry.tipo = Tipo.CHARS;
		} else if (this.actualToken.equals(Token.TRUE)) {
			this.parse += " 44";
			this.compToken(Token.TRUE);
			entry.tipo = Tipo.BOOL;
		} else if (this.actualToken.equals(Token.FALSE)) {
			this.parse += " 45";
			this.compToken(Token.FALSE);
			entry.tipo = Tipo.BOOL;
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureY() {
		if (this.actualToken.equals(Token.PARENTESIS_I)) {
			this.parse += " 46";
			this.compToken(Token.PARENTESIS_I);
			this.ProcedureL();
			this.compToken(Token.PARENTESIS_D);
		} else if (this.getFollow("Y").contains(this.actualToken)) {
			this.parse += " 47";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Llamada a funcion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureL() {
		if (this.getFirst("R").contains(this.actualToken)) {
			this.parse += " 48";
			this.ProcedureR();
			this.ProcedureQ();
		} else if (this.getFollow("L").contains(this.actualToken)) {
			this.parse += " 49";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureQ() {
		if (this.actualToken.equals(Token.COMA)) {
			this.parse += " 50";
			this.compToken(Token.COMA);
			this.ProcedureR();
			this.ProcedureQ();
		} else if (this.getFollow("Q").contains(this.actualToken)) {
			this.parse += " 51";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Expresion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureF() {
		if (this.actualToken.equals(Token.FUNCTION)) {
			this.parse += " 52";
			this.compToken(Token.FUNCTION);
			this.ProcedureH();
			this.compToken(Token.ID);
			lastTokenF = this.lastToken;
			this.stHandler.createTable(this.rutaAbsoluta);
			this.compToken(Token.PARENTESIS_I);
			this.ProcedureA();
			this.compToken(Token.PARENTESIS_D);
			this.stHandler.insertarTipoTS((Integer) lastTokenF.getAttribute(), new Funcion(this.args, this.tipoF),
					null);
			args = "";
			this.compToken(Token.EOL);
			this.ProcedureG();
			this.compToken(Token.LLAVE_I);
			esCuerpo = true;
			this.compToken(Token.EOL);
			this.ProcedureC();
			esCuerpo = false;
			this.compToken(Token.LLAVE_D);
			this.stHandler.removeTable();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Declaracion de funcion incorrecta",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureG() {
		if (this.actualToken.equals(Token.EOL)) {
			this.parse += " 53";
			this.compToken(Token.EOL);
			this.ProcedureG();
		} else if (this.getFollow("G").contains(this.actualToken)) {
			this.parse += " 54";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Salto de linea incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureH() {
		if (this.getFirst("T").contains(this.actualToken)) {
			this.parse += " 55";
			this.ProcedureT();
			this.tipoF = entry.tipo;
		} else if (this.getFollow("H").contains(this.actualToken)) {
			this.parse += " 56";
			this.tipoF = Tipo.VACIO;
		} else {
			this.regErrores
					.write(String.format("Linea %d: Analizador Sintactico - Dator devuelto por funcion incorrecto",
							this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureA() {
		if (this.getFirst("T").contains(this.actualToken)) {
			this.parse += " 57";
			this.ProcedureT();
			this.compToken(Token.ID);
			this.stHandler.insertarTipoTS((Integer) this.lastToken.getAttribute(), entry.tipo, entry.tam);
			this.args = entry.tipo;
			this.ProcedureK();
		} else if (this.getFollow("A").contains(this.actualToken)) {
			this.parse += " 58";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Parametro de funcion incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureK() {
		if (this.actualToken.equals(Token.COMA)) {
			this.parse += " 59";
			this.compToken(Token.COMA);
			this.ProcedureT();
			this.compToken(Token.ID);
			this.stHandler.insertarTipoTS((Integer) this.lastToken.getAttribute(), entry.tipo, entry.tam);
			this.args = args + "," + entry.tipo;
			this.ProcedureK();
		} else if (this.getFollow("K").contains(this.actualToken)) {
			this.parse += " 60";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Parametro de funcion incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureC() {
		if (this.getFirst("B").contains(this.actualToken)) {
			this.parse += " 61";
			this.ProcedureB();
			this.compToken(Token.EOL);
			this.ProcedureG();
			this.ProcedureJ();
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Codigo en funcion incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	// REALIZADO
	private void ProcedureJ() {
		if (this.getFirst("C").contains(this.actualToken)) {
			this.parse += " 62";
			this.ProcedureC();
		} else if (this.getFollow("J").contains(this.actualToken)) {
			this.parse += " 63";
		} else {
			this.regErrores.write(String.format("Linea %d: Analizador Sintactico - Codigo en funcion incorrecto",
					this.aLexico.getFileReader().getCurrentLine()));
		}
	}

	private void compToken(Token token) {
		if (this.actualToken.equals(Token.EOF)) {

		} else {
			if (token.equals(Token.EOL)) {
				while (!this.actualToken.equals(Token.EOL) && !this.actualToken.equals(Token.EOF)) {
					this.nextToken();
				}
			}
			if (this.actualToken.equals(token)) {
				if (actualToken.equals(Token.ID) || actualToken.equals(Token.NUM)) {
					this.lastToken = this.actualToken;

				}
				this.nextToken();
			} else if (this.actualToken.equals(Token.EOF)) {

			}

			else {
				this.regErrores.write(String.format("Linea %d: Analizador Sintactico, Error token no esperado",
						this.aLexico.getFileReader().getCurrentLine()));
				nextToken();
				compToken(actualToken);
			}
		}
	}

	private void nextToken() {
		this.actualToken = this.aLexico.getToken();
		if (this.actualToken == null) {
			this.nextToken();
		}
	}

	private ArrayList<Token> getFirst(String a) {
		ArrayList<Token> list = new ArrayList<Token>();
		list = this.mapFirst.get(a);
		return list;
	}

	private ArrayList<Token> getFollow(String a) {
		ArrayList<Token> list = new ArrayList<Token>();
		list = this.mapFollow.get(a);
		return list;
	}

	// INICIALIZA MAPS
	private void inicializarMaps() {
		this.mapFirst = new HashMap<String, ArrayList<Token>>();

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.EOL);
		this.auxList.add(Token.FUNCTION);
		this.auxList.add(Token.EOF);
		this.mapFirst.put("Z", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.FUNCTION);
		this.auxList.add(Token.EOF);
		this.mapFirst.put("P", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.mapFirst.put("B", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.LLAVE_I);
		this.mapFirst.put("I", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.mapFirst.put("1", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("2", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.ASIGNACION);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("D", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("3", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.mapFirst.put("S", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ASIGNACION);
		this.auxList.add(Token.MAYOR);
		this.auxList.add(Token.PARENTESIS_I);
		this.mapFirst.put("M", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.INT);
		this.auxList.add(Token.BOOL);
		this.auxList.add(Token.CHARS);
		this.mapFirst.put("T", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("X", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.mapFirst.put("R", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.OR);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("N", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.mapFirst.put("V", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ASIGNACIONRESTO);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("O", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.mapFirst.put("U", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.RESTO);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("W", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.mapFirst.put("E", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("Y", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.auxList.add(Token.PARENTESIS_I);
		this.auxList.add(Token.NUM);
		this.auxList.add(Token.CAD);
		this.auxList.add(Token.TRUE);
		this.auxList.add(Token.FALSE);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("L", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("Q", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.FUNCTION);
		this.mapFirst.put("F", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("G", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.INT);
		this.auxList.add(Token.BOOL);
		this.auxList.add(Token.CHARS);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("H", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.INT);
		this.auxList.add(Token.BOOL);
		this.auxList.add(Token.CHARS);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("A", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("K", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.mapFirst.put("C", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.LAMBDA);
		this.mapFirst.put("J", auxList);

		// FOLLOWS
		this.mapFollow = new HashMap<String, ArrayList<Token>>();

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.NULL);
		this.mapFollow.put("Z", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.NULL);
		this.mapFollow.put("P", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("B", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("I", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.LLAVE_D);
		this.mapFollow.put("1", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.LLAVE_D);
		this.mapFollow.put("2", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("D", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("3", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("S", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("M", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.mapFollow.put("T", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("X", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("R", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("N", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("V", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("O", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ASIGNACIONRESTO);
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("U", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ASIGNACIONRESTO);
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("W", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.RESTO);
		this.auxList.add(Token.ASIGNACIONRESTO);
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("E", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.RESTO);
		this.auxList.add(Token.ASIGNACIONRESTO);
		this.auxList.add(Token.OR);
		this.auxList.add(Token.PARENTESIS_D);
		this.auxList.add(Token.COMA);
		this.auxList.add(Token.EOL);
		this.mapFollow.put("Y", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.mapFollow.put("L", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.mapFollow.put("Q", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.EOL);
		this.mapFollow.put("F", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.LLAVE_I);
		this.auxList.add(Token.VAR);
		this.auxList.add(Token.WHILE);
		this.auxList.add(Token.ID);
		this.auxList.add(Token.RETURN);
		this.auxList.add(Token.WRITE);
		this.auxList.add(Token.PROMPT);
		this.auxList.add(Token.LLAVE_D);
		this.mapFollow.put("G", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.ID);
		this.mapFollow.put("H", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.mapFollow.put("A", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.PARENTESIS_D);
		this.mapFollow.put("K", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.LLAVE_D);
		this.mapFollow.put("C", auxList);

		this.auxList = new ArrayList<>();
		this.auxList.add(Token.LLAVE_D);
		this.mapFollow.put("J", auxList);

	}
}

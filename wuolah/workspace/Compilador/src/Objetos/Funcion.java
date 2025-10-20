package Objetos;

public class Funcion{
	
	public String args;
	public String ret;
	
	public Funcion(String args, String ret){
		this.args = args;
		this.ret = ret;
	}
	
	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	@Override
	public String toString(){
		return String.format("%s->%s", this.args, this.ret);
	}
}
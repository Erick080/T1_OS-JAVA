import java.util.Scanner;

public class parser {
    public static Aplicacao parse_detalhes_aplicacao(String linha) {
        Scanner scanner = new Scanner(linha);
        scanner.useDelimiter(" ");
        Aplicacao aplicacao = new Aplicacao();
        String[] def_aplicacao = new String[3];
        int i = 0;
        while (scanner.hasNext() && i < 3) {
            def_aplicacao[i] = scanner.next();
            i++;
        }
        scanner.close();

        if (def_aplicacao[0].equals("RT"))
            aplicacao.escalonamento = Escalonamento.RealTime;
        else
            aplicacao.escalonamento = Escalonamento.BestEffort;

        aplicacao.prioridade = Integer.parseInt(def_aplicacao[1]);
        aplicacao.chegada = Double.parseDouble(def_aplicacao[2]);

        return aplicacao;
    }

    public static Instrucao parse_instrucao(String linha) {
        Scanner scanner = new Scanner(linha);
        scanner.useDelimiter(" ");
        Instrucao instrucao = new Instrucao();
        String[] def_instrucao = new String[2];
        int i = 0;
        while (scanner.hasNext() && i <= 2) {
            String item = scanner.next();
            //System.out.println("item -> " + item + "i = " + i);
            if (item.contains(":")) { // Verifica se é um loop
                instrucao.op = item;
                return instrucao;
            }
            if(i < 1) {i++;continue;}
            
            if (i < 1 && item.isBlank()) { // Ignora espaços em branco no início de cada linha
                continue;
            }
            def_instrucao[i - 1] = item;
            i++;
        }
        scanner.close();

        instrucao.op = def_instrucao[0];
        instrucao.param = def_instrucao[1];

        //System.out.println("" + def_instrucao[0] + " " + def_instrucao[1]);
        //define categoria da instrucao
        switch(instrucao.op){
            case "add":
            case "sub":
            case "mult":
            case "div":
                //instrucao.categoria = Categoria.Aritmetico;
                instrucao.setCategoria(Categoria.Aritmetico);
                break;
            case "load":
            case "store":
                instrucao.categoria = Categoria.Memoria;
                break;
            case "brany":
            case "brpos":
            case "brzero":
            case "brneg":
                instrucao.categoria = Categoria.Salto;
                break;
            case "syscall":
                instrucao.categoria = Categoria.Sistema;
                break;
            default:
                break;
        }
        return instrucao;
    }

    public static Variavel parse_variavel(String linha) {
        Scanner scanner = new Scanner(linha);
        scanner.useDelimiter(" ");
        Variavel variavel = new Variavel();
        String[] def_variavel = new String[2];
        int i = 0;
        while (scanner.hasNext() && i < 3) {
            String item = scanner.next();
            //System.out.println("item ->" +item +"i = " + i);
            if (i < 1) {
                i++;
                continue;
            }
            def_variavel[i - 1] = item;
            i++;
        }
        scanner.close();

        variavel.nome = def_variavel[0];
        variavel.valor = Integer.parseInt(def_variavel[1]);

        return variavel;
    }


}

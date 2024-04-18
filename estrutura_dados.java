import java.util.*;

enum Escalonamento {
    RealTime,
    BestEffort
}

enum Categoria {
    Aritmetico,
    Memoria,
    Salto,
    Sistema
}

enum Estado {
    Finalizado,
    Executando,
    Pronto,
    Desbloqueado,
    Bloqueado,
    NaoChegou
}

class Variavel {
    public String nome;
    public int valor;
}

class Instrucao {
    public String op;
    public String param;
    public Categoria categoria;

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}

class Aplicacao implements Comparable<Aplicacao>{
    public Escalonamento escalonamento;
    public Estado estado;
    public int id;
    public int pc;
    public int acc;
    public int prioridade; //-1 se for BE
    public int tempo_bloqueado;
    public double chegada;
    public List<Instrucao> instrucoes;
    public List<Variavel> variaveis;
    public static int gera_intervalo_bloqueio(){
        Random rand = new Random();
        int numero = rand.nextInt(3) + 3;
        return numero;
    }
    public int execute_instrucao(Scanner s) {
        if(estado != Estado.Desbloqueado)
            estado = Estado.Executando;
        
        System.out.println();
        System.out.println("Executando aplicacao " + id);
        System.out.println("pc = " + pc);
        Instrucao instrucao = instrucoes.get(pc);
        System.out.println("Instrucao ->" + instrucao.op + " " + instrucao.param);
        String op = instrucao.op;
        String param = instrucao.param;
        //verificar tipo de enderecamento
        int valor_param = 0;
        Variavel variavel = null;
    
        if(param == null){
            pc += 1;
            return acc;
        }

        if(param.contains("#")){
            valor_param = Integer.parseInt(param.substring(1));
        } else{
            if(instrucao.categoria == Categoria.Sistema){
                valor_param = Integer.parseInt(param);
            }
            else{
                for(Variavel v : variaveis){
                    if(v.nome.equals(param)){
                        valor_param = v.valor;
                        variavel = v;
                    }
                }
            }
        }

       // System.out.println("no switch ->" + instrucao.categoria);
        switch(instrucao.categoria){
            case Aritmetico:
                if(op.equals( "add")){
                    acc += valor_param;
                    System.out.println("acc += " +valor_param + " = " + acc);
                }
                else if(op.equals("sub")){
                    acc -= valor_param;
                    System.out.println("acc -= " +valor_param + " = " + acc);
                }
                else if(op.equals("mult")){
                    acc *= valor_param;
                    System.out.println("acc *= " +valor_param + " = " + acc);
                }
                else if(op.equals("div")){
                    acc /= valor_param;
                    System.out.println("acc /= " +valor_param + " = " + acc);
                }
                break;

            case Memoria:
                if(op.equals("load")){
                    acc = valor_param;
                    System.out.println("acc <- " + param + "   acc = " + acc);
                }
                else if(op.equals("store")){
                    variavel.valor = acc;
                    System.out.println("acc -> " + variavel.nome +"   " +variavel.nome+ " = " + acc);
                }

                break;

            case Salto:
                //procura pela posicao do label
                int pos = -1;
                for(int i = 0; i < instrucoes.size(); i++){
                    if(instrucoes.get(i).op.startsWith(param)){
                        pos = i;
                    }
                }              

                if(op.equals("branny")){
                    pc = pos;
                    System.out.println("pc = " + pos);
                }

                else if(op.equals("brpos"))
                    if(acc > 0){
                        pc = pos;
                        System.out.println("pc = " + pos);
                    }
                
                else if(op.equals("brzero")){
                    if(acc == 0){
                        System.out.println("pc = " + pos);
                        pc = pos;
                    }
                }

                else if(op.equals("brneg")){
                    if(acc < 0){
                        System.out.println("pc = " + pos);
                        pc = pos;
                    }
                }

                break;

            case Sistema:
                if(valor_param == 0){
                    //encerrar programa
                    System.out.println("exit");
                    estado = Estado.Finalizado;
                    return acc;
                }
                else if(valor_param == 1){ 
                    if(estado != Estado.Desbloqueado){ //se ainda n foi bloqueado, bloqueia
                        System.out.println("BLOCK");
                        estado = Estado.Bloqueado;
                        tempo_bloqueado = gera_intervalo_bloqueio();
                        return acc;
                    }
                    else{ //ja passou pelo bloqueio
                        System.out.println("print(acc)");
                        estado = Estado.Executando;
                        System.out.println(acc);
                    }
                }
                else if(valor_param == 2){
                    if(estado != Estado.Desbloqueado){
                        System.out.println("BLOCK");
                        //bloqueia
                        estado = Estado.Bloqueado;
                        tempo_bloqueado = gera_intervalo_bloqueio();
                        return acc;

                    }
                    else{
                        estado = Estado.Executando;
                        System.out.println("Digite o input: ");
                        acc = s.nextInt();
                    }
                }
                break;

            default:
                break;
            
            
        }
        pc+=1;
        if(pc >= instrucoes.size()){
            estado = Estado.Finalizado;
        }
        return acc;
    }
    @Override
    public int compareTo(Aplicacao o) {
        return Double.compare(this.chegada, o.chegada);
    }
    Aplicacao(){
        this.instrucoes = new ArrayList<>();
        this.variaveis = new ArrayList<>();
        this.estado = Estado.NaoChegou; //estado inicial
    }
    public void setId(int id) {
        this.id = id;
    }
}

 class Lista_RT {
    public Queue<Aplicacao> alta_prioridade;
    public Queue<Aplicacao> baixa_prioridade;
    public int aplicacoes_prontas_alta;
    public int aplicacoes_prontas_baixa;
    public int indice_alta;
    public int indice_baixa;

    Lista_RT(){
        this.alta_prioridade = new LinkedList<>();
        this.baixa_prioridade = new LinkedList<>();
    }

    public void procura_aplicacoes_prontas(double tempo_escalonador) {
        aplicacoes_prontas_alta = 0;
        aplicacoes_prontas_baixa = 0;

        for(Aplicacao a : alta_prioridade){
            if(a.chegada <= tempo_escalonador && (a.estado != Estado.Finalizado || a.estado != Estado.Bloqueado) ){
                aplicacoes_prontas_alta++;
                if(a.estado == Estado.Desbloqueado){
                    continue;
                }
                a.estado = Estado.Pronto;
            }

        }
        for(Aplicacao a : baixa_prioridade){
            if(a.chegada <= tempo_escalonador && (a.estado != Estado.Finalizado || a.estado != Estado.Bloqueado) ){
                aplicacoes_prontas_baixa++;
                if(a.estado == Estado.Desbloqueado){
                    continue;
                }
                a.estado = Estado.Pronto;
            }

        }

    }

}


class Lista_BE {
    public Queue<Aplicacao> aplicacoes;
    public int indice;
    public boolean aplicacao_pronta;

    Lista_BE(){
        this.aplicacoes = new LinkedList<>();
    }

    public void procura_aplicacao_pronta(double tempo_escalonador) {
        aplicacao_pronta = false;
        for(Aplicacao a : aplicacoes){
            if(a.chegada <= tempo_escalonador && (a.estado != Estado.Finalizado || a.estado != Estado.Bloqueado)){
                aplicacao_pronta = true;
                if(a.estado == Estado.Desbloqueado){
                    continue;
                }
                else 
                a.estado = Estado.Pronto;

            }
        }
       
    }
        
}


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

class app{
    static Lista_RT lista_RT;
    static Lista_BE lista_BE;
    static List<Aplicacao> aplicacoes_bloqueadas;
    static List<Aplicacao> aplicacoes_finalizadas;
    static double tempo_exec_escalonador;
    static double quantum;
    static double tempo_exec_FCFS;

    public static int algoritmo_RT(Scanner s){
        int aplicacoes_completadas = 0;
        double tempo_exec = 0;
        Aplicacao aplicacao;
        //executa uma aplicacao rt alta disponivel por quantum de tempo
        if(lista_RT.aplicacoes_prontas_alta > 0){
            aplicacao = lista_RT.alta_prioridade.peek();
            while(tempo_exec < quantum){
                aplicacao.execute_instrucao(s);
                tempo_exec +=1;
                if(aplicacao.estado == Estado.Finalizado){
                    aplicacoes_completadas = 1;
                    aplicacoes_finalizadas.add(aplicacao);
                    lista_RT.alta_prioridade.remove();
                    return aplicacoes_completadas;
                }
                else if(aplicacao.estado == Estado.Bloqueado){
                    aplicacoes_bloqueadas.add(aplicacao);
                    lista_RT.alta_prioridade.remove();
                    break;
                }
            }
        }

        //se nenhuma aplicacao rt alta esta disponivel, executa uma baixa por quantidade quantum de tempo
        else if(lista_RT.aplicacoes_prontas_baixa > 0){
            //System.out.println("Indice baixa -> " + lista_RT.indice_baixa);
            aplicacao = lista_RT.baixa_prioridade.peek();
            while(tempo_exec < quantum){
                aplicacao.execute_instrucao(s);
                tempo_exec += 1;
                if(aplicacao.estado == Estado.Finalizado){
                    aplicacoes_completadas = 1;
                    aplicacoes_finalizadas.add(aplicacao);
                    lista_RT.baixa_prioridade.remove();
                    return aplicacoes_completadas;
                }
                else if(aplicacao.estado == Estado.Bloqueado){
                    aplicacoes_bloqueadas.add(aplicacao);
                    lista_RT.baixa_prioridade.remove();
                    break;
                }
            }
        }

        tempo_exec_escalonador += quantum;
        return aplicacoes_completadas;
    }

    public static int algoritmo_BE(Scanner s){
        int aplicacoes_completadas = 0;
        double tempo_exec = 0;
        Aplicacao aplicacao = lista_BE.aplicacoes.peek();

        while(tempo_exec < tempo_exec_FCFS){
            aplicacao.execute_instrucao(s);
            tempo_exec += 1;
            if(aplicacao.estado == Estado.Finalizado){
                aplicacoes_completadas = 1;
                aplicacoes_finalizadas.add(aplicacao);
                lista_BE.aplicacoes.remove();
                return aplicacoes_completadas;
            }
            else if(aplicacao.estado == Estado.Bloqueado){
                aplicacoes_bloqueadas.add(aplicacao);
                lista_BE.aplicacoes.remove();
                break;
            }

        }
        tempo_exec_escalonador += tempo_exec;
        return aplicacoes_completadas;
    }

    public static void escalonador(int num_aplicacoes, Scanner s){
        tempo_exec_escalonador = 0;
        int num_aplicacoes_exec = 0;
        lista_RT.indice_alta = 0;
        lista_RT.indice_baixa = 0;
        lista_BE.indice = 0;

        while(num_aplicacoes_exec < num_aplicacoes){
            lista_RT.aplicacoes_prontas_alta = 0;
            lista_RT.aplicacoes_prontas_baixa = 0;
            lista_BE.aplicacao_pronta = false;

            System.out.println("Tempo do escalonador: " + tempo_exec_escalonador);
            //verifica se alguma aplicacao bloqueada ja pode ser desbloqueada
           // System.out.println("nro de blockeadas = " + aplicacoes_bloqueadas.size());
            if(aplicacoes_bloqueadas.size() > 0){
                for(int i = 0; i < aplicacoes_bloqueadas.size(); i++){
                    Aplicacao a = aplicacoes_bloqueadas.get(i);
                    a.tempo_bloqueado -= 1;
                    if(a.tempo_bloqueado == 0){
                        a.estado = Estado.Desbloqueado;
                        if(a.escalonamento == Escalonamento.RealTime){
                            if(a.prioridade == 0) lista_RT.alta_prioridade.add(a);
                            else {
                                lista_RT.baixa_prioridade.add(a);
                             }
                        }
                        else lista_BE.aplicacoes.add(a);

                        aplicacoes_bloqueadas.remove(i);
                    }
                }
            }

            //verifica se existem aplicacoes RT prontas
            lista_RT.procura_aplicacoes_prontas(tempo_exec_escalonador);
            if(lista_RT.aplicacoes_prontas_alta > 0 || lista_RT.aplicacoes_prontas_baixa > 0){
                num_aplicacoes_exec += algoritmo_RT(s);    
            }
            else {
                //se nao tem aplicacoes de RT prontas, verifica as de BE
                lista_BE.procura_aplicacao_pronta(tempo_exec_escalonador);
                if(lista_BE.aplicacao_pronta == true){
                    num_aplicacoes_exec += algoritmo_BE(s);
                } else{ //se nenhuma aplicacao esta pronta, apenas incrementa o tempo do escalonador
                    tempo_exec_escalonador += 1;
                }
            }

            System.out.println("---Status das aplicacoes: ");
            System.out.println("--fila RT alta preferencia: ");
            for (Aplicacao a : lista_RT.alta_prioridade) {
                System.out.println("id - " + a.id + " estado = " + a.estado);
            }
            System.out.println("--fila RT baixa preferencia: ");
            for (Aplicacao a : lista_RT.baixa_prioridade) {
                System.out.println("id - " + a.id + " estado = " + a.estado);
            }
            System.out.println("--fila BE: ");
            for (Aplicacao a : lista_BE.aplicacoes) {
                System.out.println("id - " + a.id + " estado = " + a.estado);
            }
            System.out.println("--lista Bloqueadas: ");
            for (Aplicacao a : aplicacoes_bloqueadas) {
               System.out.println("id - " + a.id);
            }
            System.out.println("--lista Finalizadas: ");
            for (Aplicacao a : aplicacoes_finalizadas) {
                System.out.println("id - " + a.id);                
            }

            System.out.println();
        }
    }

    public static void main(String[] args) {
        int num_aplicacoes_total;
        Scanner s = new Scanner(System.in);
        System.out.println("Digite o numero de aplicacoes a serem processadas: ");
        num_aplicacoes_total = s.nextInt();
        //s.close();
        lista_RT = new Lista_RT();
        lista_BE = new Lista_BE();
        aplicacoes_bloqueadas = new ArrayList<>();
        aplicacoes_finalizadas = new ArrayList<>();

//parse do arquivo
        File arquivo = new File("input.txt");
        try {
            Scanner scanner = new Scanner(arquivo);
            Aplicacao aplicacao = new Aplicacao();
            aplicacao.pc = 0;
            aplicacao.acc = 0;
            aplicacao.tempo_bloqueado = 0;
            aplicacao.estado = Estado.NaoChegou;
            
            scanner.nextLine(); // pula primeira linha

            while (scanner.hasNextLine()) {
                for (int i = 0; i < num_aplicacoes_total; i++) {
                    aplicacao.setId(i);

                    String linha = scanner.nextLine();
                    aplicacao = parser.parse_detalhes_aplicacao(linha);
                    System.out.println("Aplicacao esc" + aplicacao.escalonamento);
                    scanner.nextLine();

                    while (scanner.hasNextLine()) {
                        linha = scanner.nextLine();
                        if (linha.charAt(0) == '.') break;
                        aplicacao.instrucoes.add(parser.parse_instrucao(linha));
                    }

                    scanner.nextLine();
                    scanner.nextLine();

                    while (scanner.hasNextLine()) {
                        linha = scanner.nextLine();
                        if (linha.charAt(0) == '.') break;
                        aplicacao.variaveis.add(parser.parse_variavel(linha));
                    }

                    if (aplicacao.escalonamento == Escalonamento.RealTime) {
                        if (aplicacao.prioridade == 0)
                            lista_RT.alta_prioridade.add(aplicacao);
                        else if (aplicacao.prioridade == 1) lista_RT.baixa_prioridade.add(aplicacao);
                    } else lista_BE.aplicacoes.add(aplicacao);
                    
                    if(scanner.hasNextLine())
                        scanner.nextLine();
                }
            }
            //scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        quantum = 0;
        tempo_exec_FCFS = 0;


        if((lista_RT.alta_prioridade.size() + lista_RT.baixa_prioridade.size()) > 0){
            System.out.println("Digite o valor do quantum: ");
            //scan.next();
            quantum = s.nextInt();
        }
        if(lista_BE.aplicacoes.size() > 0){
            System.out.println("Digite o tempo de exec do algoritmo de FCFS: ");
            tempo_exec_FCFS = s.nextInt();
        }
        //s.close();
        Collections.sort((LinkedList)lista_RT.alta_prioridade);
        Collections.sort((LinkedList)lista_RT.baixa_prioridade);
        Collections.sort((LinkedList)lista_BE.aplicacoes);

        escalonador(num_aplicacoes_total, s);
       /*  System.out.println(lista_BE.aplicacoes.get(0).variaveis.get(1).nome);
        System.out.println("Aplicacoes RT prioridade baixa");
        for(Aplicacao a : lista_RT.baixa_prioridade){
            System.out.println("App " + a.id);
            for(Instrucao i : a.instrucoes){
                System.out.println("" + i.op +" " + i.param);
            }
        }*/

    }
        
    }

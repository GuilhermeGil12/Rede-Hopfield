import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Rede Neural de Hopfield
 * Reconhecimento de caracteres a partir de um arquivo CSV.
 *
 * O arquivo "dados.csv" deve estar NA MESMA PASTA deste programa.
 * Cada linha do arquivo e um desenho achatado, com valores +1 e -1.
 * As linhas de cima sao os padroes a armazenar; a ULTIMA linha e o
 * padrao desconhecido que sera apresentado a rede.
 *
 * Compilar e executar:
 *     javac RedeHopfield.java
 *     java RedeHopfield
 */
public class RedeHopfield {

    static final String ARQUIVO = "dados.csv";

    // formato do desenho, usado so para imprimir na tela
    static final int LINHAS_DESENHO  = 5;
    static final int COLUNAS_DESENHO = 6;

    static final String[] NOMES = {"3", "4", "1", "8", "T", "L", "Triangulo"};

    static int N;              // numero de neuronios (descoberto no arquivo)
    static int M;              // numero de padroes armazenados
    static int[][] padroes;    // os padroes
    static int[] desconhecido; // o padrao a reconhecer
    static int[][] pesos;      // matriz N x N


    // =================================================================
    // LEITURA DO ARQUIVO
    // =================================================================
    /**
     * Converte um pedaco de texto em numero inteiro.
     * Aceita "1", " 1 ", "1,00" (virgula decimal) e "1.00" (ponto decimal),
     * porque o Excel costuma exportar os valores com casas decimais.
     */
    static int paraInteiro(String texto) {
        texto = texto.trim().replace("\"", "").replace(",", ".");
        return (int) Math.round(Double.parseDouble(texto));
    }

    static int[][] lerCSV(String caminho) throws IOException {

        List<int[]> linhas = new ArrayList<>();
        BufferedReader leitor = new BufferedReader(new FileReader(caminho));
        String linha;

        while ((linha = leitor.readLine()) != null) {

            linha = linha.trim();
            if (linha.isEmpty()) continue;

            // descobre o separador: ponto e virgula ou virgula
            String separador = linha.contains(";") ? ";" : ",";
            String[] partes = linha.split(separador);

            // se a primeira coluna nao for numero, e cabecalho: pula
            try {
                paraInteiro(partes[0]);
            } catch (NumberFormatException e) {
                continue;                          // era linha de cabecalho
            }

            int[] valores = new int[partes.length];
            for (int k = 0; k < partes.length; k++) {
                valores[k] = paraInteiro(partes[k]);
            }
            linhas.add(valores);
        }
        leitor.close();

        return linhas.toArray(new int[0][]);
    }


    // =================================================================
    // PASSO 1 - MATRIZ DE PESOS
    //     w_ij = soma dos x_i * x_j sobre os M padroes,  se i != j
    //     w_ii = 0
    // =================================================================
    static void treinar() {
        pesos = new int[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                if (i == j) continue;              // diagonal fica zero

                int soma = 0;
                for (int r = 0; r < M; r++) {
                    soma += padroes[r][i] * padroes[r][j];
                }
                pesos[i][j] = soma;
            }
        }
    }


    // funcao de ativacao tipo rele
    static int sinal(int valor, int anterior) {
        if (valor > 0) return 1;
        if (valor < 0) return -1;
        return anterior;
    }


    // =================================================================
    // PASSOS 2 e 3 - PROCESSO ITERATIVO
    // Atualiza um neuronio por vez, usando o valor novo em seguida.
    // =================================================================
    static int[] reconhecer(int[] entrada, long semente, boolean mostrar) {

        int[] y = entrada.clone();                 // PASSO 2
        Random sorteio = new Random(semente);

        for (int iteracao = 1; iteracao <= 20; iteracao++) {

            int[] anterior = y.clone();
            int[] ordem = ordemSorteada(sorteio);

            for (int k = 0; k < N; k++) {          // PASSO 3
                int j = ordem[k];

                int soma = 0;
                for (int i = 0; i < N; i++) {
                    soma += pesos[i][j] * y[i];
                }
                y[j] = sinal(soma, y[j]);
            }

            if (mostrar) desenhar(y, "Iteracao " + iteracao);

            if (iguais(y, anterior)) {
                if (mostrar) System.out.println("   >> a saida parou de mudar: rede convergiu");
                break;
            }
        }
        return y;
    }


    // =================================================================
    // IMPRESSAO
    // =================================================================
    static void desenhar(int[] vetor, String titulo) {
        System.out.println("\n" + titulo + ":");
        for (int l = 0; l < LINHAS_DESENHO; l++) {
            System.out.print("   ");
            for (int c = 0; c < COLUNAS_DESENHO; c++) {
                System.out.print(vetor[l * COLUNAS_DESENHO + c] > 0 ? "##" : "..");
            }
            System.out.println();
        }
    }

    /** Tabela com todas as posicoes nas linhas e todos os desenhos nas colunas. */
    static void tabelaPadroes() {
        System.out.println("\n" + repetir("=", 78));
        System.out.println("TABELA COMPLETA: cada posicao em cada desenho");
        System.out.println(repetir("=", 78));

        System.out.printf("%-6s%-8s", "pos", "linha");
        for (int r = 0; r < M; r++) System.out.printf("%9s", nome(r));
        System.out.printf("%14s%n", "desconhecido");
        System.out.println(repetir("-", 78));

        for (int i = 0; i < N; i++) {
            System.out.printf("%-6d%-8d", i + 1, (i / COLUNAS_DESENHO) + 1);
            for (int r = 0; r < M; r++) System.out.printf("%9d", padroes[r][i]);
            System.out.printf("%14d%n", desconhecido[i]);
        }
    }

    /** Mostra a conta completa de um neuronio: peso x valor, parcela por parcela. */
    static void tabelaNeuronio(int j, int[] estado) {
        System.out.println("\n" + repetir("=", 62));
        System.out.println("CONTA DETALHADA DO NEURONIO " + (j + 1));
        System.out.println(repetir("=", 62));
        System.out.printf("%-8s%-14s%-12s%-12s%s%n",
                          "pos i", "peso w(i,j)", "valor y(i)", "parcela", "acumulado");
        System.out.println(repetir("-", 62));

        int acumulado = 0;
        for (int i = 0; i < N; i++) {
            int parcela = pesos[i][j] * estado[i];
            acumulado += parcela;
            String marca = (i == j) ? "   <- diagonal, sempre zero" : "";
            System.out.printf("%-8d%-14d%-12d%-12d%d%s%n",
                              i + 1, pesos[i][j], estado[i], parcela, acumulado, marca);
        }

        System.out.println(repetir("-", 62));
        System.out.println("SOMA FINAL = " + acumulado);
        int resultado = sinal(acumulado, estado[j]);
        System.out.println("O rele decide: " + (acumulado > 0 ? "soma positiva" : "soma negativa")
                           + " -> o neuronio " + (j + 1) + " vira " + resultado
                           + (resultado > 0 ? " (preto)" : " (branco)"));
        System.out.println("Valor que estava antes: " + estado[j]
                           + (resultado != estado[j] ? "  ==> MUDOU" : "  ==> continuou igual"));
    }

    static void mostrarPesos(int quantas) {
        System.out.println("\n" + repetir("=", 62));
        System.out.println("MATRIZ DE PESOS (" + N + " x " + N + ") - primeiras "
                           + quantas + " linhas e colunas");
        System.out.println(repetir("=", 62));

        System.out.print("      ");
        for (int j = 0; j < quantas; j++) System.out.printf("%5d", j + 1);
        System.out.println();

        for (int i = 0; i < quantas; i++) {
            System.out.printf("%5d ", i + 1);
            for (int j = 0; j < quantas; j++) System.out.printf("%5d", pesos[i][j]);
            System.out.println();
        }
        System.out.println("(a diagonal e toda zero: o neuronio nao se liga em si mesmo)");
    }


    // =================================================================
    // AUXILIARES
    // =================================================================
    static int[] ordemSorteada(Random sorteio) {
        int[] ordem = new int[N];
        for (int i = 0; i < N; i++) ordem[i] = i;
        for (int i = N - 1; i > 0; i--) {
            int t = sorteio.nextInt(i + 1);
            int guarda = ordem[i]; ordem[i] = ordem[t]; ordem[t] = guarda;
        }
        return ordem;
    }

    static boolean iguais(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    static int qualPadrao(int[] y) {
        for (int r = 0; r < M; r++) if (iguais(y, padroes[r])) return r;
        return -1;
    }

    static int pixelsIguais(int[] a, int[] b) {
        int c = 0;
        for (int i = 0; i < a.length; i++) if (a[i] == b[i]) c++;
        return c;
    }

    static String nome(int r) {
        String n = (r < NOMES.length) ? NOMES[r] : ("P" + (r + 1));
        return n.length() > 6 ? n.substring(0, 6) : n;
    }

    static String repetir(String s, int vezes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vezes; i++) sb.append(s);
        return sb.toString();
    }


    // =================================================================
    // PROGRAMA PRINCIPAL
    // =================================================================
    public static void main(String[] args) {

        int[][] dados;
        try {
            dados = lerCSV(ARQUIVO);
        } catch (IOException e) {
            System.out.println("ERRO: nao consegui abrir o arquivo \"" + ARQUIVO + "\".");
            System.out.println("Coloque o arquivo na mesma pasta deste programa.");
            return;
        }

        if (dados.length < 2) {
            System.out.println("ERRO: o arquivo precisa ter pelo menos 2 linhas.");
            return;
        }

        // ultima linha = padrao desconhecido; as de cima = padroes
        N = dados[0].length;
        M = dados.length - 1;

        padroes = new int[M][];
        for (int r = 0; r < M; r++) padroes[r] = dados[r];
        desconhecido = dados[M];

        System.out.println(repetir("=", 62));
        System.out.println("REDE NEURAL DE HOPFIELD");
        System.out.println(repetir("=", 62));
        System.out.println("Arquivo lido      : " + ARQUIVO);
        System.out.println("Neuronios (N)     : " + N);
        System.out.println("Padroes (M)       : " + M);
        System.out.printf ("Limite recomendado: M <= 15%% de N = %.1f%n", 0.15 * N);
        if (M > 0.15 * N) {
            System.out.println("AVISO: o numero de padroes esta acima do limite recomendado.");
            System.out.println("       A rede pode parar em pontos de equilibrio errados.");
        }

        // --- desenhos dos padroes ---
        System.out.println("\n" + repetir("=", 62));
        System.out.println("PADROES ARMAZENADOS");
        System.out.println(repetir("=", 62));
        for (int r = 0; r < M; r++) desenhar(padroes[r], "Padrao " + (r + 1) + " - " + nome(r));

        // --- tabela completa ---
        tabelaPadroes();

        // --- PASSO 1 ---
        treinar();
        mostrarPesos(Math.min(12, N));

        // --- conta detalhada de um neuronio ---
        int neuronioExemplo = Math.min(26, N - 1);      // neuronio 27
        tabelaNeuronio(neuronioExemplo, desconhecido);

        // --- PASSOS 2 e 3 ---
        System.out.println("\n" + repetir("=", 62));
        System.out.println("RECONHECIMENTO");
        System.out.println(repetir("=", 62));
        desenhar(desconhecido, "Entrada (padrao desconhecido)");

        int[] resposta = null;
        long semente = 1;

        for (long tentativa = 1; tentativa <= 50; tentativa++) {
            int[] r = reconhecer(desconhecido, tentativa, false);
            if (qualPadrao(r) >= 0) { semente = tentativa; resposta = r; break; }
        }

        if (resposta == null) {
            resposta = reconhecer(desconhecido, 1, true);
        } else {
            System.out.println("\n(ordem de atualizacao numero " + semente + ")");
            reconhecer(desconhecido, semente, true);
        }

        // --- resultado ---
        System.out.println("\n" + repetir("=", 62));
        System.out.println("RESULTADO");
        System.out.println(repetir("=", 62));

        int achou = qualPadrao(resposta);
        if (achou >= 0) {
            System.out.println("A rede reconheceu o padrao " + (achou + 1)
                               + ": caractere \"" + nome(achou) + "\"");
        } else {
            System.out.println("A rede parou num estado que nao e nenhum dos padroes.");
            System.out.println("(ponto de equilibrio 'alienigena')");
            for (int r = 0; r < M; r++) {
                System.out.println("   parecido com \"" + nome(r) + "\": "
                                   + pixelsIguais(resposta, padroes[r]) + "/" + N + " pixels");
            }
        }
    }
}
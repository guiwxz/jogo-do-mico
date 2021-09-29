/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author VIP
 */
public class Server extends Thread {

  private static List<Player> players;
  private Player player;
  private Socket conexao;
  private String nomeCliente;

  private static List<Carta> baralho;

  public Server(Player c) {

    player = c;

  }

  /**
   * funcao que me diz quem é o outro jogador
   * @return outroPlayer, o outro jogador da sessão
   */
  public Player getOutroPlayer() {
    if (players.size() > 1) {
      if (player.getId() == players.get(0).getId()) {
        return players.get(1);
      } else {
        return players.get(0);
      }
    } else {
      return player;
    }
  }

  /**
   * funcao de pegar a carta do baralho do oponente
   * @param saida
   */
  public void pegarAction(PrintStream saida) {

    Player outroPlayer = getOutroPlayer();

    // pega uma carta aleatoria da mao do outro jogador
    Carta c = outroPlayer.getAleatorio();
    if (c != null) {
      // remove do baralho dele e adiciona no meu
      outroPlayer.getBaralhoProprio().remove(c);
      player.getBaralhoProprio().add(c);

      sendToAll("\n## A carta pega foi " + c.getId() + " " + (c.getMico() ? "(MICO)" : ""), true);
    } else {
      sendToAll("## Não foi possível pegar a carta", true);
    }

    // VERIFICA OS PARES DO MEU BARALHO
    List<Carta> pares = player.getPares();
    if (pares.size() > 0) {
      String ids = "";
      // as cartas que formaram pares na minha mao voltam pro baralho do servidor
      for (Carta carta : pares) {
        ids += carta.getId() + ", ";
        baralho.add(carta);
      }
      sendToAll("## " + player.getNome().toUpperCase() + " formou pares com id: " + ids, true);
    } else {
      sendToAll("## " + player.getNome().toUpperCase() + " não formou pares\n", true);
    }

  }

  /**
   * funcao que me informa se o jogo terminou e quem venceu
   * @return 'true' se o jogo terminou, 'false' caso nao terminou
   */
  public Boolean verifyEndGame() {
    Player outroPlayer = getOutroPlayer();

    // se o meu baralho tem tamanho 1 (uma carta) 
    // E essa carta que está na posicao 0 for o mico 
    // E a vez de alguem ainda estiver ativa (significa que o jogo está rodando)
    if ( //? aqui verifica se eu estou com o mico
      player.getBaralhoProprio().size() == 1 
      && player.getBaralhoProprio().get(0).getMico() 
      && (player.getVez() || outroPlayer.getVez())
    ) {
      sendToAll("** O JOGADOR " + outroPlayer.getNome() + " VENCEU **", true);
      sendToAll("## " + player.getNome() + " ficou com o mico", true);

      player.setVez(false);
      outroPlayer.setVez(false);

      outroPlayer.addWin();
      return true;

    } else if ( //? aqui verifica se o outro está com o mico
      outroPlayer.getBaralhoProprio().size() == 1 
      && outroPlayer.getBaralhoProprio().get(0).getMico() 
      && (player.getVez() || outroPlayer.getVez())
    ){
      sendToAll("** O JOGADOR " + player.getNome() + " VENCEU **", true);
      sendToAll("## " + outroPlayer.getNome() + " ficou com o mico", true);

      player.setVez(false);
      outroPlayer.setVez(false);

      player.addWin();
      return true;

    }

    return false;
  }

  /**
   * funcao que cria o baralho na primeira vez
   */
  public static void criarBaralho() {
    baralho = new ArrayList<Carta>();

    for (int i = 1; i <= 8; i++) {
      Carta c = new Carta();
      Integer j = 0;
      if (i == 8) { // se a carta for a 8 significa que é o mico
        c.setId(i);
        c.setMico(true);
        baralho.add(c);
        break;
      } else {
        while (j < 2) { // isso aqui serve pra adicionar 2 vezes o mesmo numero da carta (pra formar os pares)
          c.setId(i);
          baralho.add(c);
          j++;
        }
      }
    }

    Collections.shuffle(baralho);
  }

  /**
   * funcao que é invocada quando o jogo é iniciado
   * - devolve as cartas pro baralho do server
   * - embaralha e distruibui pros jogadores novamente
   */
  public void startGame() {
    Player outroPlayer = getOutroPlayer();
    // devolve as cartas q sobraram pro server
    while(!player.getBaralhoProprio().isEmpty())
      baralho.add(player.removeCarta(0));
    while(!outroPlayer.getBaralhoProprio().isEmpty())
      baralho.add(outroPlayer.removeCarta(0));

    Collections.shuffle(baralho);

    // entrega as cartas pros jogadores
    // basicamente entrega 7 cartas pra um jogador e 8 pro outro
    Integer total;
    int i = 0;
    while (i < 2) {
      if (baralho.size() > 8)
        total = 7;
      else
        total = 8;

      for (int j = 0; j<total; j++) {
        Carta c = baralho.remove(0);
        players.get(i).giveCarta(c);
        System.out.println(players.get(i).getNome() + " recebeu " + c.getId());
      }

      i++;
    }

    sendToAll("\n** O JOGO FOI INICIADO **", true);
    sendToAll("## As jogadas sao intercaladas. O jogador " + player.getNome() + " incia", true);
    sendToAll("## Escreva '-pegar' para puxar uma carta do outro jogador", true);
  }

  /**
   * funcao que manda mensagem pra todos clientes conectados
   * @param linha mensagem que deve ser passada pros jogadores 
   * @param self condição booleana que determina se a mensagem deve ser mandada pro cliente da thread
   *  quando o cliente digita no chat, eu quero repassar isso apenas pros outros clientes e não para mim, assim o 'self' fica falso
   *  Em outras palavras: quando eu recebo mensagem de um cliente, eu repasso apenas pros outros menos esse cliente,
   *    e caso o servidor queira mandar uma mensagem aos clientes, deve-se mandar para todos 
   */
  public void sendToAll(String linha, Boolean self) {
    for (Player p: players) {
      PrintStream chat = (PrintStream) p.getSaida();
      if (self) { // caso true envia mensagem pra todos outros incluindo a thread atual
        chat.println(linha);

      } else if (p != player) { // caso falso envia mensagem pra todos clientes menos essa thread que digitou
        chat.println(player.getNome() + linha);
      }
    }
  }

  public void run() {
    try {
      // entrada é oq o cliente manda
      BufferedReader entrada = new BufferedReader(new InputStreamReader(player.getSocket().getInputStream()));
      // saida é oq o server manda pro cliente
      PrintStream saida = new PrintStream(player.getSocket().getOutputStream());
      
      player.setSaida(saida);

      nomeCliente = entrada.readLine();

      if (nomeCliente == null) {
        return;
      }

      player.setNome(nomeCliente);      
      
      String linha = " ";
      while (linha != null) {

        Player outroPlayer = getOutroPlayer();

        System.out.println("player: " + player.getVez());
        System.out.println("outroPlyer: " + outroPlayer.getVez());
        
        linha = entrada.readLine();

        // caso nao seja minha vez de jogar, minha mensagem nao vai aparecer pros demais, nao vai passar desse if por causa do continue
        if (outroPlayer.getVez() && !player.getVez()) {
          System.out.println("nao é sua vez");
          saida.println("## Não é sua vez");
          continue;
        }

        // distribui a mensagem aos demais
        sendToAll(" >> " + linha, false);

        // if que inicia o jogo
        // CASO nao seja NEM a minha vez 
        // E NEM a vez do outro jogador (significa que o jogo nao está rodando) 
        // E algum deles digitar '-start' isso vai fazer o jogo começar, pois vai setar true na vez desse jogador que iniciou o jogo
        if (!player.getVez() && !outroPlayer.getVez() && linha.equals("-start")) {
          startGame();

          player.mostrarCartas();
          outroPlayer.mostrarCartas();

          player.setVez(true);
        }

        // if que executa o pegar a carta do outro baralho
        // quando eu digitar verifica SE é a minha vez e verifica se eu digitei o comando '-pegar'
        if (player.getVez() && linha.equals("-pegar")) {
          System.out.println("action pegar executada");
          pegarAction(saida);

          // muda a vez 
          player.setVez(!player.getVez());
          outroPlayer.setVez(!outroPlayer.getVez());

          //mostra as cartas
          player.mostrarCartas();
          outroPlayer.mostrarCartas();
        }

        // if que verifica se o jogo terminou, retorna
        if (verifyEndGame()) {
          // mostra o histórico, quanto cada um ganhou
          sendToAll(player.getNome() + " possui " + player.getWins() + " vitórias", true);
          sendToAll(outroPlayer.getNome() + " possui " + outroPlayer.getWins() + " vitórias", true);

          // pede para o jogador vencedor se desejam jogar novamente
          sendToAll("## " + player.getNome() + " deseja jogar novamente? (-sim | -nao)", true);
          linha = entrada.readLine();

          // condicao que reinicia o jogo caso ele digite '-sim'
          if (linha.equals("-sim")) {
            startGame();

            player.mostrarCartas();
            outroPlayer.mostrarCartas();

            player.setVez(true);
            
            // else verifica se o jogo nao foi reiniciado (nao é a vez de ninguém) e finaliza o jogo
          } else if (!player.getVez() && !outroPlayer.getVez()){
            sendToAll("## GGWP", true);
            sendToAll("## O jogo foi encerrado", true);
            // fecha conexao dos 2 e sai do laço
            outroPlayer.getSocket().close();
            player.getSocket().close();
            break;
          }
        }

        // condicao de saida caso algum deles digite o comando '-sair'
        if (linha.equals("-sair")) {
          sendToAll("## O jogo foi encerrado", true);
          // fecha conexao dos 2 e sai do laço
          outroPlayer.getSocket().close();
          player.getSocket().close();
          break;
        }

      }

      sendToAll("## Saiu do chat!", false);
      players.remove(saida);
      conexao.close();

    } catch (IOException e) {
      System.out.println("IOException: " + e);
    } catch (NullPointerException e) {
      System.out.println("Servidor desconectado");
    }
  }

  public static void main(String args[]) {

    criarBaralho();

    players = new ArrayList<Player>();

    try {
      ServerSocket s = new ServerSocket(2222);

      Integer i = 1;

      while (i <= 2) {
        System.out.println("Esperando jogador " + i + " se conectar....");
        Socket conexao = s.accept();
        Player player = new Player();
        player.setId(i-1);
        player.setIp(conexao.getRemoteSocketAddress().toString());
        player.setSocket(conexao);

        players.add(player);

        Thread t = new Server(player);
        t.start();

        System.out.println(" Conectou!: " + conexao.getRemoteSocketAddress());

        i++;
      }

      //!!!!!!!! botei isso pq tava dando erro mas talvez quebre
      s.close();

    } catch (Exception e) {
      System.out.println("IOException: " + e);
    }
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.*;
import java.io.*;

/**
 *
 * @author VIP
 */
public class Client extends Thread {

  private static boolean done = false;

  private Socket conexao;

  public Client(Socket s) {
    conexao = s;
  }

  // cliente manipula a sua thread dentro do jogo
  // cada thread faz referencia a um Player dentro do servidor, que recebe os comandos de cada Cliente
  public void run() {
    try {
      BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
      String linha;

      while (true) {
        linha = entrada.readLine();
        if (linha == null) {
          System.out.println("Conexão encerrada");
          break;
        }
        System.out.println(linha);
      }
    } catch (IOException e) {
      System.out.println("IOException do cliente: " + e);
    }
    done = true;
  }

  public static void main(String args[]) {

    try {
      Socket conexao = new Socket("127.0.0.1", 2222);

      PrintStream saida = new PrintStream(conexao.getOutputStream());
      BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));

      System.out.printf("Entre com seu nome: ");
      String meuNome = teclado.readLine();
      saida.println(meuNome);
      Thread t = new Client(conexao);
      t.start();

      String linha;
      while (true) {
        System.out.printf(">> ");
        linha = teclado.readLine();
        if (done)
          break;
        saida.println(linha);
      }

    } catch (Exception e) {
      System.out.println("IOException do cliente: " + e);
    }
  }

}

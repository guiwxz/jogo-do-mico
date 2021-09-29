/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VIP
 */
public class Player {
  private Integer id;
  private String ip;
  private String nome;
  private PrintStream saida;
  private Socket socket;
  private List<Carta> baralhoProprio = new ArrayList<>();
  private Boolean vez = false;
  private Integer wins = 0;

  /**
   * 
   * @return uma lista de pares existentes no meu baralho
   */
  public List<Carta> getPares(){
    List<Carta> pares = new ArrayList<>();

    for (Carta c: baralhoProprio) {
      Integer occur = 0;
      for (Carta c2: baralhoProprio) {
        if (c.getId() == c2.getId()) {
          occur++;
          if (occur == 2) {
            pares.add(c);
          }
        }
      }
    }

    // removendo as cartas pares do meu baralho
    for (Carta c: pares) {
      baralhoProprio.remove(c);
    }
    
    return pares;
  }

  /**
   * @return the baralhoProprio
   */
  public List<Carta> getBaralhoProprio() {
    return baralhoProprio;
  }

  /**
   * @param aBaralhoProprio the baralhoProprio to set
   */
  public void setBaralhoProprio(List<Carta> aBaralhoProprio) {
    baralhoProprio = aBaralhoProprio;
  }

  public void giveCarta(Carta carta) {
    this.baralhoProprio.add(carta);
  }

  public Carta removeCarta(int index) {
    Carta c = new Carta();
    c = this.baralhoProprio.remove(index);
    return c;
  }

  public Carta getAleatorio() {
    if (baralhoProprio.size() > 0) {
      Random random = new Random();
      Integer index = random.nextInt(baralhoProprio.size());

      return baralhoProprio.get(index);
    }
    return null;
  }

  public void mostrarCartas() {
    saida.printf("Suas cartas: ");
    for (Carta c: baralhoProprio) {
      saida.printf(c.getId() + ", ");
    }
    saida.println("\n");
  }

  /**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * @param ip the ip to set
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * @return the nome
   */
  public String getNome() {
    return nome;
  }

  /**
   * @param nome the nome to set
   */
  public void setNome(String nome) {
    this.nome = nome;
  }

  /**
   * @return the saida
   */
  public PrintStream getSaida() {
    return saida;
  }

  /**
   * @param saida the saida to set
   */
  public void setSaida(PrintStream saida) {
    this.saida = saida;
  }

  /**
   * @return the socket
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * @param socket the socket to set
   */
  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  /**
   * @return the vez
   */
  public Boolean getVez() {
    return vez;
  }

  /**
   * @param vez the Vez to set
   */
  public void setVez(Boolean vez) {
    this.vez = vez;
  }

  /**
   * @return the wins
   */
  public Integer getWins() {
    return wins;
  }

  /**
   * @param win the win to set
   */
  public void setWins(Integer win) {
    this.wins = win;
  }

  /**
   * add 1 win to the player
   */
  public void addWin() {
    this.wins++;
  }
}

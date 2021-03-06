package kayttoliittyma;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import logiikka.Tilanne;
import logiikka.Tilanne.Nappula;



/**
 * Shakkilaudan grafiikka.
 * @author Ilari Kauko
 * @version 4.10.2020
 */
public class Lauta extends JPanel {
	
	private static final String AAKKOSET = "ABCDEFGH";
	private int reuna, ruutu, ihminen;
	private BufferedImage[][] nappulat;
	private Color tumma, vaalea, siirto;
	private Tilanne peli;
	
	
	/**
	 * konstruktori
	 * @param ihminen 0, jos valkoiset nappulat halutaan laudan alareunaan ja 1, jos mustat
	 * @param reuna paljonko pikeleitä ruutujen reunoille jätetään
	 * @param ruutu ruudun leveys pikseleinä
	 * @param nappulat nappulakuvakkeet, jotka laudalle piirretään
	 * @param tumma väri, jolla tummat ruudut väritetään
	 * @param vaalea väri, jolla vaaleat ruudut väritetään
	 * @param siirto väri, jolla kuvataan viiva juuri siirretyn nappulan lähtö- ja maaliruudun välillä
	 * @param gui käyttöliittymäikkuna, johon lauta piirretään
	 */
	public Lauta(int ihminen, int reuna, int ruutu, BufferedImage[][] nappulat, Color tumma, Color vaalea, Color siirto, ShakkiSwing gui) {
		this.ihminen = ihminen;
		this.reuna = reuna;
		this.ruutu = ruutu;
		this.nappulat = nappulat;
		this.tumma = tumma;
		this.vaalea = vaalea;
		this.siirto = siirto;
		peli = new Tilanne();
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				if (x <= reuna || reuna + ruutu*8 <= x) return;
				int y = e.getY();
				if (y <= reuna || reuna + ruutu*8 <= y) return;
				int[] k = toLogiikka(x, y);
				gui.siirra(k[1], k[0]);
			}
		});
	}

	
	/**
	 * Piirtää kentän uudelleen vallitsevan tilanteen mukaan.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(tumma);
		g.fillRect(0, 0, reuna*2 + ruutu*8, reuna*2 + ruutu*8);
		g.setColor(vaalea);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		for (int i = 0; i < 8; i++) {
			String kirjain = AAKKOSET.substring(i, i+1);
			if (ihminen == 1) kirjain = AAKKOSET.substring(7 - i, 8 - i);
			String nro = ""+(8-i);
			if (ihminen == 1) nro = ""+(i+1);
			drawCenteredString(g, nro, 0, reuna + ruutu*i, reuna, ruutu);
			drawCenteredString(g, nro, reuna + ruutu*8, reuna + ruutu*i, reuna, ruutu);
			drawCenteredString(g, kirjain, reuna + ruutu*i, 0, ruutu, reuna);
			drawCenteredString(g, kirjain, reuna + ruutu*i, reuna + ruutu*8, ruutu, reuna);
		}
		g.fillRect(reuna - 1, reuna - 1, ruutu*8 + 2, ruutu*8 + 2);
		g.setColor(tumma);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int[] gui = toGUI(i, j);
				if ((i + j)%2 == 0) g.fillRect(gui[0], gui[1], ruutu, ruutu);
				Nappula n = peli.getNappula(j, i);
				if (n != null) g.drawImage(nappulat[n.getPuoli()][n.getMuoto()], gui[0], gui[1], null);
			}
		}
		int[][] lahdot = peli.getLahdot();
		int[][] maalit = peli.getMaalit();
		g.setColor(siirto);
		for (int i = 0; i < lahdot.length; i++) {
			int[] gui = toGUI(lahdot[i][1], lahdot[i][0]);
			int[] guiMaalit = toGUI(maalit[i][1], maalit[i][0]);
			if (maalit[i][0] != -1) g.drawLine(gui[0] + ruutu/2, gui[1] + ruutu/2, guiMaalit[0] + ruutu/2, guiMaalit[1] + ruutu/2);
		}
	}
	
	
	/**
	 * Määrittää laudalla klikatun kohdan koordinaattien perusteella ruudun, jota klikattiin
	 * @param x klikatun kohdan x-koordinaatti
	 * @param y klikatun kohdan y-koordinaatti
	 * @return klikatun ruudun rivi ja sarake
	 */
	public int[] toLogiikka(double x, double y) {
		if (ihminen == 0) return new int[] {(int)((x - reuna)/ruutu), 7 - (int)((y - reuna)/ruutu)};
		return new int[] {7 - (int)((x - reuna)/ruutu), (int)((y - reuna)/ruutu)};
	}
	
	
	/**
	 * Edellisen lähes käänteisfunktio: annettuja riviä ja saraketta vastaavan ruudun keskipiste näytöllä
	 * @param x rivi
	 * @param y sarake
	 * @return riviä ja saraketta vastaavan ruudun keskipisteen koordinaatit näytöllä
	 */
	public int[] toGUI(int x, int y) {
		if (ihminen == 0) return new int[] {reuna + x*ruutu, reuna + 7*ruutu - y*ruutu};
		return new int[] {reuna + 7*ruutu - x*ruutu, reuna + y*ruutu};
	}
	
	
	/**
	 * Piirtää tilanteen uudelleen.
	 * @param peli tilanne
	 */
	public void piirra(Tilanne peli) {
		this.peli = peli;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint(reuna, reuna, ruutu*8, ruutu*8);
			}
		});
	}
	
	
	/**
	 * Kirjoittaa tekstin annetun suorakulmion keskelle.
	 * @param g alusta, johon kirjoitetaan
	 * @param text kirjoitettava teksti
	 * @param x1 suorakulmion vasen reuna
	 * @param y1 suorakulmion yläreuna
	 * @param wi suorakulmion leveys
	 * @param he suorakulmion korkeus
	 */
	public void drawCenteredString(Graphics g, String text, int x1, int y1, int wi, int he) {
	    FontMetrics metrics = g.getFontMetrics(getFont());
	    int x = x1 + wi/2 - metrics.stringWidth(text)/2;
	    int y = y1 + he/2 + metrics.getHeight()/2;
	    g.drawString(text, x, y);
	}
}
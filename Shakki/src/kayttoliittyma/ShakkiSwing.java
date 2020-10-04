package kayttoliittyma;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import logiikka.Tilanne;
import logiikka.Tilanne.Nappula;

/**
 * Graafinen shakkipeli.
 * @author Ilari Kauko
 * @version 3.10.2020
 */
public class ShakkiSwing {
	
	private JFrame frame;
	private Lauta lauta;
	private int puoli, ihminen;
	private Tilanne peli;
	private int[] klikattu;
	private BufferedImage[][] nappulat = haeNappulat("src/kayttoliittyma/");
	
	
	/**
	 * Aloittaa uuden pelin.
	 */
	public void uusiPeli() {
		frame = new JFrame("Shakki");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String syote = JOptionPane.showInputDialog(frame, "Anna puoli (1=musta, muu=valkoinen):", "Uusi peli", JOptionPane.DEFAULT_OPTION);
		if (syote == null) return;
		ihminen = 0;
		if (syote.equals("1")) ihminen = 1;
		peli = new Tilanne();
		puoli = 0;
		lauta = new Lauta(ihminen, 30, 80, nappulat, new Color(153, 76, 0), new Color(255, 178, 102), Color.RED, this);
		frame.add(lauta);
		frame.pack();
		frame.setVisible(true);
		if (puoli != ihminen) teeSiirto();
	}
	
	
	/**
	 * Hakee nappulakuvat annetusta polusta. Kuvien nimet on hiukan epäkäytännöllisesti kovakoodattu.
	 * @param polku mitä kuvat löytyvät
	 * @return luettelo kuvista muodossa, jossa ne on piirrettävissä paneeliin.
	 */
	private static BufferedImage[][] haeNappulat(String polku) {
		try {
			BufferedImage[][] nappulat = new BufferedImage[2][8];
			nappulat[1][0] = ImageIO.read(new File(polku+"talonpoika.gif"));
			nappulat[1][1] = ImageIO.read(new File(polku+"torni.gif"));
			nappulat[1][2] = ImageIO.read(new File(polku+"ratsu.gif"));
			nappulat[1][3] = ImageIO.read(new File(polku+"lahetti.gif"));
			nappulat[1][4] = ImageIO.read(new File(polku+"kuningatar.gif"));
			nappulat[1][5] = ImageIO.read(new File(polku+"kuningas.gif"));
			nappulat[0][0] = ImageIO.read(new File(polku+"talonpoikav.gif"));
			nappulat[0][1] = ImageIO.read(new File(polku+"torniv.gif"));
			nappulat[0][2] = ImageIO.read(new File(polku+"ratsuv.gif"));
			nappulat[0][3] = ImageIO.read(new File(polku+"lahettiv.gif"));
			nappulat[0][4] = ImageIO.read(new File(polku+"kuningatarv.gif"));
			nappulat[0][5] = ImageIO.read(new File(polku+"kuningasv.gif"));
			return nappulat;
		} catch (IOException e) {
			return null;
		}
	}
	
	
	/**
	 * Suorittaa shakkisiirron reaktiot käyttöliittymätasolla. Jos lähtöruutua ei ole valittu, 
	 * asetetaan klikattu ruutu siksi ja odotetaan, että käyttäjä klikkaa maaliruutua. Jos lähtoruutu on valittu ja siirto
	 * on laillinen, tehdään sen mukainen siirto ja siirretään vuoro tietokoneelle.
	 * @param i ruudun rivi
	 * @param j ruudun sarake
	 */
	public void siirra(int i, int j) {
		if (puoli != ihminen) return;
		if (klikattu == null) {
			Nappula n = peli.getNappula(i, j);
			if (n != null && n.getPuoli() == puoli) klikattu = new int[] {i, j};
		} else {
			int muoto = peli.getNappula(klikattu[0], klikattu[1]).getMuoto();
			if (muoto == 0 && i == 7 - puoli*7) {
				try {
					muoto = Integer.parseInt(JOptionPane.showInputDialog(frame, "Anna uusi muoto (1=torni, 2=ratsu, 3=lähetti, muu=kuningatar):", "Talonpoika laidassa", JOptionPane.DEFAULT_OPTION));
				} catch (NumberFormatException ex) {
					muoto = 4;
				}
				if (muoto < 1 || 4 < muoto) muoto = 4;
			}
			Tilanne seuraaja = peli.syotaSiirto(klikattu[0], klikattu[1], i, j, muoto);
			klikattu = null;
			if (seuraaja == null) return;
			if (seuraaja.toistoa()) {
				String syote = JOptionPane.showInputDialog(frame, "Sama asetelma on toistunut 3 kertaa. Otatko tasapelin? (1=kyllä, muu=ei):", "Toistoa", JOptionPane.DEFAULT_OPTION);
				if (syote.equals("1")) seuraaja = peli.syotaSiirto(0, 0, i, j, -1);
			}
			peli = seuraaja;
			lauta.piirra(peli);
			if (loppuuko()) return;
			puoli = (puoli + 1)%2;
			teeSiirto();
		}
	}
	
	
	/**
	 * Tutkii, päättääkö tilanne pelin ja jos päättää, ehdottaa käyttäjälle uutta peliä ja sulkee edellisen.
	 * @return päättääkö tilanne pelin
	 */
	public boolean loppuuko() {
		String palaute = "";
		if (peli.matti((puoli + 1)%2)) {
			if (puoli == 1) palaute = "Shakkimatti, musta voitti.";
			else palaute = "Shakkimatti, valkoinen voitti.";
		} else if (peli.patti((puoli + 1)%2)) palaute = "Patti.";
		if (palaute.equals("")) return false;
		JFrame poistettava = frame;
		if (JOptionPane.showConfirmDialog(null, palaute+" Jatketaanko?", "Peli ohi", JOptionPane.YES_NO_OPTION) == 0) {
			uusiPeli();
		}
		poistettava.setVisible(false);
		return true;
	}
	
	
	/**
	 * Suorittaa tietokoneen siirron käyttöliittymätasolla.
	 */
	public void teeSiirto() {
		new Thread(new Runnable() {
			public void run() {
				peli = peli.parasSiirto(puoli);
				lauta.piirra(peli);
				if (loppuuko()) return;
				if (peli.shakki(ihminen)) JOptionPane.showMessageDialog(frame, "Shakki!");
				puoli = (puoli + 1)%2;
				if (peli.toistoa()) {
					String syote = JOptionPane.showInputDialog(frame, "Sama asetelma on toistunut 3 kertaa. Otatko tasapelin? (1=kyllä, muu=ei):", "Toistoa", JOptionPane.DEFAULT_OPTION);
					if (syote.equals("1")) peli = peli.syotaSiirto(0, 0, 0, 0, -1);
					if (loppuuko()) return;
				}
			}
		}).start();
	}
	
	
	/**
	 * Pääohjelma. Käynnistää pelin graafisen version.
	 * @param args ei käytössä
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ShakkiSwing().uusiPeli();
			}
		});
	}
}

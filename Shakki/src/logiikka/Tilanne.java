package logiikka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


/**
 * Yksittäisen pelitilanteen luokka. Hyödyntämällä staattisen ja ei-staattisen eroja koko 
 * komentoriviversio shakista on saatu aikaan kahdella luokalla.
 * @author Ilari Kauko
 * @version 3.10.2020
 */
public class Tilanne {
		
	/**
	 * Nappulalle on oma luokkansa. Puolet ja muodot on numeroitu.
	 * @author Ilari Kauko
	 *
	 */
	public static class Nappula {
		private int puoli, muoto;
		
		/**
		 * konstruktori
		 * @param muoto
		 * @param puoli
		 */
		public Nappula(int muoto, int puoli) {
			this.puoli = puoli;
			this.muoto = muoto;
		}
		
		
		/**
		 * @return nappulan muoto
		 */
		public int getMuoto() {
			return muoto;
		}
		
		
		/**
		 * @return nappulan puoli
		 */
		public int getPuoli() {
			return puoli;
		}
		
		
		/**
		 * Katsoo, vastaavatako kaksi nappulaa toisiaan. Käytetään tutkittaessa, täsmäävätkö kaksi skenaariota.
		 * @param toinen verrattava nappula
		 * @return täsmäävätkö muoto ja puoli
		 */
		public boolean equals(Nappula toinen) {
			return muoto == toinen.muoto && puoli == toinen.puoli;
		}
	}
	
	private static final Scanner LUKIJA = new Scanner(System.in);
	// 0 vastaa sotilasta, 1 tornia, 2 ratsua, 3 lähettiä, 4 kuningatarta ja 5 kuningasta.
	private static final int[] VAHVUUDET = new int[] {1, 5, 3, 3, 9}; // Nämä vahvuudet on yleisesti hyväksytty shakkiyhteisössä.
	private static final String[] VALKOISET = new String[] {"♟ ", "♜ ", "♞ ", "♝ ", "♛ ", "♚ "};
	private static final String[] MUSTAT = new String[] {"♙ ", "♖ ", "♘ ", "♗ ", "♕ ", "♔ "};
	private static final List<String> AAKKOSET = Arrays.asList(new String[] {"A","B","C","D","E","F","G","H"});
	
	private boolean[][] linnoitukset;
	private int[][] lahdot; // mistä ruuduista nappulat ovat siirtyneet tilanteen mukaisiin ruutuihin
	private int[][] maalit; // mihin ruutuihin nappulat ovat siirtyneet
	private Nappula[][] lauta;
	private Tilanne edeltaja; // Jokainen tilanne tietää, mikä tilanne itseä edelsi.

	
	/**
	 * Konstruktori. Luo aloitustilanteen.
	 */
	public Tilanne() {
		lauta = new Nappula[8][8];
		for (int i = 0; i < 8; i++) {
			lauta[1][i] = new Nappula(0, 0);
			lauta[6][i] = new Nappula(0, 1);
		}
		
		for (int i = 0; i < 3; i++) {
			lauta[0][i] = new Nappula(i+1, 0);
			lauta[0][8-i-1] = new Nappula(i+1, 0);
			lauta[7][i] = new Nappula(i+1, 1);
			lauta[7][8-i-1] = new Nappula(i+1, 1);
		}
		
		lauta[0][3] = new Nappula(4, 0);
		lauta[0][4] = new Nappula(5, 0);
		lauta[7][3] = new Nappula(4, 1);
		lauta[7][4] = new Nappula(5, 1);
		linnoitukset = new boolean[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) linnoitukset[i][j] = true;
		}
		lahdot = new int[][] {};
		maalit = new int[][] {};
	}
	
	
	/**
	 * Konstruktori. Luo tilanteen toisen tilanteen ja siihen tehtävien siirtojen perusteella.
	 * @param lahto edeltävä tilanne
	 * @param lahdot ruudut, joista nappulaa siirretään
	 * @param maalit ruudut, joihin nappulat siirretään
	 */
	public Tilanne(Tilanne lahto, int[][] lahdot, int[][] maalit) {
		lauta = new Nappula[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (lahto.lauta[i][j] != null && lahto.lauta[i][j].muoto != 6) lauta[i][j] = lahto.lauta[i][j];
			}
		}
		linnoitukset = new boolean[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) linnoitukset[i][j] = lahto.linnoitukset[i][j];
		}
		for (int i = 0; i < lahdot.length; i++) {
			Nappula siirrettava = lauta[lahdot[i][0]][lahdot[i][1]];
			if (siirrettava == null) continue;
			for (int j = 0; j < 2; j++) {
				if (lahdot[i][0] == j*7) {
					if (lahdot[i][1] == 0) linnoitukset[j][0] = false;
					if (lahdot[i][1] == 7) linnoitukset[j][1] = false;
					if (lahdot[i][1] == 4) {
						linnoitukset[j][0] = false;
						linnoitukset[j][1] = false;
					}
				}
				if (maalit[i][0] == j*7) {
					if (maalit[i][1] == 0) linnoitukset[j][0] = false;
					if (maalit[i][1] == 7) linnoitukset[j][1] = false;
				}
			}
			lauta[lahdot[i][0]][lahdot[i][1]] = null;
		    if (maalit[i][0] != -1) lauta[maalit[i][0]][maalit[i][1]] = siirrettava;
		}
		this.lahdot = lahdot;
		this.maalit = maalit;
		edeltaja = lahto;
	}
	
	
	/**
	 * Tulostaa tilanteen komentoriville.
	 */
	public void tulosta() {
		System.out.println("  A B C D E F G H");
		for (int i = 0; i < 8; i++) {
			System.out.print((i+1)+" ");
			for (int j = 0; j < 8; j++) {
				Nappula n = lauta[i][j];
				if (n == null) {
					if ((i+j)%2 == 0) System.out.print("\u25A0 ");
					else System.out.print("  ");
				}
				else if (n.puoli == 0) System.out.print(VALKOISET[n.muoto]);
				else System.out.print(MUSTAT[n.muoto]);
			}
			System.out.println(i+1);
		}
		System.out.println("  A B C D E F G H");
	}
	
	
	/**
	 * Arvioi väliltä [0,1-0,9], miten suotuisa tilanne on kummankin pelaajan kannalta.
	 * Käytetään yksinkertaisuuden vuoksi pelkkää aineellista voimaa.
	 * @return 0,1, jos pelkästään valkoisella on nappuloita kuninkaan lisäksi, 0,9, jos pelkästään mustalla on, muuten edellisten väliltä
	 */
	public double eval() {
		int summa = 0;
		int mustalla = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (lauta[i][j] == null || lauta[i][j].muoto == 5) continue;
				summa += VAHVUUDET[lauta[i][j].muoto];
				if (lauta[i][j].puoli == 1) mustalla += VAHVUUDET[lauta[i][j].muoto];
			}
		}
		return 0.1 + 0.8*mustalla/summa;
	}
	
	
	/**
	 * Arvioi rekursiivisesti, miten suotuisa tilanne on pelaajien kannalta.
	 * @param rekursio montako kertaa funktiota vielä aiotaan kutsua; suorituskyky on rajallinen
	 * @param pelaaja 0, jos siirtovuoro on valkoisella, 1 jos mustalla
	 * @param a alaraja tulevalle arvolle
	 * @param b yläraja tulevalle arvolle 
	 * @return 0, jos valkoiselle löytyy voittostrategia, 1 jos mustalle, muuten edellisten väliltä kuvaava luku
	 */
	public double payoff(int rekursio, int pelaaja, double a, double b) {
		ArrayList<Tilanne> seuraajat = seuraajat(pelaaja);
		// Jos sallittuja siirtoja ei ole vuorossa olevalla pelaajalla, on kyseessä joko matti tai patti.
		if (seuraajat.size() == 0) {
			if (shakki(pelaaja)) return (pelaaja + 1)%2; // shakkimatti
			return 0.5; // patti
		}
		if (rekursio == 0) return eval();
		if (pelaaja == 1) {
			double arvo = 0;
			for (int i = 0; i < seuraajat.size() && a < b; i++) {
				Tilanne seuraaja = seuraajat.get(i);
				double skenaario = seuraaja.payoff(rekursio-1, 0, a, b);
				arvo = Math.max(arvo, skenaario);
				a = Math.max(a, skenaario);
			}
			return arvo;
		} else {
			double arvo = 1;
			for (int i = 0; i < seuraajat.size() && a < b; i++) {
				Tilanne seuraaja = seuraajat.get(i);
				double skenaario = seuraaja.payoff(rekursio-1, 1, a, b);
				arvo = Math.min(arvo, skenaario);
				b = Math.min(b, skenaario);
			}
			return arvo;
		}
	}
	
	
	/**
	 * Tutkii, onko kuningas uhattuna
	 * @param puoli
	 * @return onko annetun puolen kuningas uhattuna
	 */
	public boolean shakki(int puoli) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (lauta[i][j] != null && lauta[i][j].puoli != puoli) {
					ArrayList<int[]> siirrot = siirrot(i, j);
					for (int[] siirto : siirrot) {
						Nappula n = lauta[siirto[0]][siirto[1]];
						if (n != null && n.muoto == 5 && n.puoli == puoli) return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * @param puoli
	 * @return onko annetun puolen kuningas uhattuna ilman torjuntamahdollisuutta
	 */
	public boolean matti(int puoli) {
		return shakki(puoli) && seuraajat(puoli).size() == 0;
	}
	
	
	/**
	 * @param puoli
	 * @return onko tilanne se, ettei puolella ole laillisia siirtoja kuninkaan ollessa kuitenkin uhkaamatta
	 */
	public boolean patti(int puoli) {
		return !shakki(puoli) && seuraajat(puoli).size() == 0;
	}
	
	
	/**
	 * @return onko tilannetta vastaava tilanne toistunut vähintään 2 kertaa
	 */
	public boolean toistoa() {
		int toistoja = 0;
		Tilanne edellinen = this;
		while ((edellinen = edellinen.edeltaja) != null) {
			if (edellinen.equals(this) && ++toistoja == 3) return true;
		}
		return false;
	}
	
	
	/**
	 * Shakkilaudan indeksitarkistin
	 * @param i
	 * @param j
	 * @return onko laudalla i:tä vastaavaa riviä ja j:tä vastaavaa saraketta
	 */
	public static boolean laudalla(int i, int j) {
		return -1 < i && -1 < j && i < 8 && j < 8;
	}
	
	
	/**
	 * Tornin, lähetin ja kuningattaren siirtomahdollisuuksien listaamiseen käytetty metodi.
	 * @param i rivi
	 * @param j sarake
	 * @param di
	 * @param dj
	 * @return lista suoran linjan muodostavista koordinaateista, joita vastaaville ruuduille nappula saa siirtyä
	 */
	public ArrayList<int[]> linja(int i, int j, int di, int dj) {
		ArrayList<int[]> palaute = new ArrayList<int[]>();
		Nappula s = lauta[i][j];
		int k = i + di;
		int l = j + dj;
		while (laudalla(k, l) && lauta[k][l] == null) {
			palaute.add(new int[] {k, l});
			k += di;
			l += dj;
		}
		if (laudalla(k, l) && lauta[k][l] != null && lauta[k][l].puoli != s.puoli) palaute.add(new int[] {k, l}); 
		return palaute;
	}
	
	
	/**
	 * Tutkii, onko annetun puolen annettu linnoitus sallittu.
	 * @param puoli
	 * @param pituus 0 jos pitkä, 1 jos lyhyt
	 * @return onko annettu linnoitus sallittu
	 */
	public boolean linnoitettava(int puoli, int pituus) {
		if (!linnoitukset[puoli][pituus] || shakki(puoli)) return false;
		if (pituus == 0) {
			for (int i = 1; i < 4; i++) {
				if (lauta[puoli*7][i] != null) return false;
			}
			Tilanne ylitys = new Tilanne(this, new int[][] { {puoli*7, 4}}, new int[][] {{puoli*7, 3}});
			if (ylitys.shakki(puoli)) return false;
		} else {
			for (int i = 5; i < 7; i++) {
				if (lauta[puoli*7][i] != null) return false;
			}
			Tilanne ylitys = new Tilanne(this, new int[][] { {puoli*7, 4}}, new int[][] {{puoli*7, 5}});
			if (ylitys.shakki(puoli)) return false;
		}
		return true;
	}
	
	
	/**
	 * Listaa tilanteet, jotka vallitsevaa tilannetta saavat seurata.
	 * @param puoli puoli, jolla on siirtovuoro
	 * @return lista mahdollisista seuraavista tilanteista
	 */
	public ArrayList<Tilanne> seuraajat(int puoli) {
		ArrayList<Tilanne> palaute = new ArrayList<Tilanne>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (lauta[i][j] != null && lauta[i][j].puoli == puoli) {
					ArrayList<int[]> siirrot = siirrot(i, j);
					for (int[] siirto : siirrot) {
						Tilanne seuraaja = new Tilanne(this, new int[][]{{i,j}}, new int[][] { siirto});
						if (lauta[i][j].muoto == 0 && siirto[0] == 7 - puoli*7) {
							for (int k = 1; k < 5; k++) {
								Tilanne uusi = seuraaja.clone();
								uusi.lauta[siirto[0]][siirto[1]] = new Nappula(k, puoli);
								palaute.add(uusi);
							}
						} else palaute.add(seuraaja);
					}
					if (lauta[i][j].muoto == 0 && i == 4 - puoli) {
						int suunta = 2;
						if (suunta == 0) suunta = -2;
						if (j != 0 && lauta[i][j-1] != null && lauta[i][j-1].puoli != puoli && lauta[i][j-1].muoto == 0 && maalit[0][0] == i && maalit[0][1] == j-1 && lahdot[0][0] == i-suunta) {
							palaute.add(new Tilanne(this, new int[][] {{i, j}, {i, j-1}}, new int[][] {{i-suunta/2, j-1}, {-1, 0}}));
						}
						if (j != 7 && lauta[i][j+1] != null && lauta[i][j+1].puoli != puoli && lauta[i][j+1].muoto == 0 && maalit[0][0] == i && maalit[0][1] == j+1 && lahdot[0][0] == i-suunta) {
							palaute.add(new Tilanne(this, new int[][] {{i, j}, {i, j+1}}, new int[][] {{i-suunta/2, j+1}, {-1, 0}}));
						}
					}
				}
			}
		}
		if (linnoitettava(puoli, 0)) palaute.add(new Tilanne(this, new int[][]{ {puoli*7,4}, {puoli*7,0}}, new int[][]{ {puoli*7,2}, {puoli*7,3}}));
		if (linnoitettava(puoli, 1)) palaute.add(new Tilanne(this, new int[][]{ {puoli*7,4}, {puoli*7,7}}, new int[][]{ {puoli*7,6}, {puoli*7,5}}));
		// Vasta viimeiseksi poistetaan tilanteet, jotka asettaisivat kuninkaan uhatuksi.
		for (int i = palaute.size() - 1; i >= 0; i--) {
			Tilanne s = palaute.get(i);
			if (s.shakki(puoli)) palaute.remove(i);
		}
		return palaute;
	}
	
	
	/**
	 * @param i
	 * @param j
	 * @return lista koordinaateista, joihin annettujen koordinaattien nappula saa siirtyä
	 */
	public ArrayList<int[]> siirrot(int i, int j) {
		Nappula s = lauta[i][j];
		ArrayList<int[]> palaute = new ArrayList<int[]>();
		if (s.muoto == 0) {
			int suunta = s.puoli;
			if (suunta == 0) suunta = -1;
			if (lauta[i-suunta][j] == null) {
				if (i == 6 && s.puoli == 1 && lauta[i-2][j] == null || i == 1 && s.puoli == 0 && lauta[3][j] == null) {
					palaute.add(new int[]{i-suunta*2, j});
				}
				palaute.add(new int[]{i-suunta,j});
			}
			if (j != 0 && lauta[i-suunta][j-1] != null && lauta[i-suunta][j-1].puoli != s.puoli) palaute.add(new int[] {i-suunta, j-1});
			if (j != 7 && lauta[i-suunta][j+1] != null && lauta[i-suunta][j+1].puoli != s.puoli) palaute.add(new int[] {i-suunta, j+1});
		} else if (s.muoto == 1) {
			palaute.addAll(linja(i, j, -1, 0));
			palaute.addAll(linja(i, j, 1, 0));
			palaute.addAll(linja(i, j, 0, -1));
			palaute.addAll(linja(i, j, 0, 1));
		} else if (s.muoto == 2) {
			for (int k = -2; k < 3; k++) {
				for (int l = -2; l < 3; l++) {
					if (k*k + l*l == 5 && laudalla(i+k, j+l) && (lauta[i+k][j+l] == null || lauta[i+k][j+l].puoli != s.puoli)) palaute.add(new int[] {i+k, j+l});
				}
			}
		} else if (s.muoto == 3) {
			palaute.addAll(linja(i, j, -1, -1));
			palaute.addAll(linja(i, j, 1, -1));
			palaute.addAll(linja(i, j, -1, 1));
			palaute.addAll(linja(i, j, 1, 1));	
		} else if (s.muoto == 4) {
			for (int k = -1; k < 2; k++) {
				for (int l = -1; l < 2; l++) {
					if (k != 0 || l != 0) palaute.addAll(linja(i, j, k, l));
				}
			}
		} else if (s.muoto == 5) {
			for (int k = i-1; k < i+2; k++) {
				for (int l = j-1; l < j+2; l++) {
					if (laudalla(k, l) && (lauta[k][l] == null || lauta[k][l].puoli != s.puoli)) palaute.add(new int[] {k, l});
				}
			}
		}
		return palaute;
	}
	
	
	/**
	 * @param toinen
	 * @return täsmäävätkö kaksi tilannetta
	 */
	public boolean equals(Tilanne toinen) {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				boolean tyhja = lauta[i][j] == null;
				if (tyhja != (toinen.lauta[i][j] == null)) return false;
				if (!tyhja && !lauta[i][j].equals(toinen.lauta[i][j])) return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Kloonaa tilanteen.
	 * @return tilanne, joka vastaa itseä
	 */
	public Tilanne clone() {
		Tilanne uusi = new Tilanne();
		uusi.lauta = new Nappula[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (lauta[i][j] != null) uusi.lauta[i][j] = lauta[i][j];
			}
		}
		uusi.linnoitukset = new boolean[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) uusi.linnoitukset[i][j] = linnoitukset[i][j];
		}
		uusi.lahdot = new int[lahdot.length][2];
		for (int i = 0; i < lahdot.length; i++) {
			uusi.lahdot[i][0] = lahdot[i][0];
			uusi.lahdot[i][1] = lahdot[i][1];
		}
		uusi.maalit = new int[maalit.length][2];
		for (int i = 0; i < maalit.length; i++) {
			uusi.maalit[i][0] = maalit[i][0];
			uusi.maalit[i][1] = maalit[i][1];
		}
		return uusi;
	}
	
	
	/**
	 * Uuden siirron vastaanottaminen komentorivillä.
	 * @param puoli siirtovuorossa oleva puoli
	 * @return siirtoa seuraava tilanne
	 */
	public Tilanne syotaSiirto(int puoli) {
		System.out.print("Anna siirto (esim. A2-A3)>");
		String vastaus = LUKIJA.nextLine().toUpperCase();
		if (!vastaus.matches("[A-H][1-8]-[A-H][1-8]")) return syotaSiirto(puoli);
		int lahtoJ = AAKKOSET.indexOf(vastaus.substring(0,1));
		int lahtoI = Integer.parseInt(vastaus.substring(1,2)) - 1;
		int maaliJ = AAKKOSET.indexOf(vastaus.substring(3,4));
		int maaliI = Integer.parseInt(vastaus.substring(4,5)) - 1;
		Tilanne tulos;
		int muoto = -1;
		if (lauta[lahtoI][lahtoJ] != null && maaliI == 7 - puoli*7 && lauta[lahtoI][lahtoJ].muoto == 0) {
			System.out.println("Anna uuden nappulan muoto (1=torni, 2=ratsu, 3=lähetti, muu=kuningatar");
			vastaus = LUKIJA.nextLine().toUpperCase();
			if (vastaus.matches("[1-3]")) muoto = Integer.parseInt(vastaus);
			else muoto = 4;
		}
		try {
			tulos = syotaSiirto(lahtoI, lahtoJ, maaliI, maaliJ, muoto);
		} catch (Exception e) {
			e.printStackTrace();
			return syotaSiirto(puoli);
		}
		if (tulos == null) {
			System.out.println("Laiton siirto");
			return syotaSiirto(puoli);
		}
		return tulos;
		
	}
	
	
	/**
	 * Yleisempi syötteen siirtometodi.
	 * @param i1 lähtörivi
	 * @param j1 lähtösarake
	 * @param i2 maalirivi
	 * @param j2 maalisarake
	 * @param muoto mihin muotoon sotilas ylennetään, jos se saavuttaa laudan vastakkaisen laidan
	 * @return siirtoa seuraava tilanne
	 */
	public Tilanne syotaSiirto(int i1, int j1, int i2, int j2, int muoto) {
		if (lauta[i1][j1] == null) return null;
		Tilanne uusi = null;
		int puoli = lauta[i1][j1].puoli;
		if (lauta[i1][j1].muoto == 5 && j2 - j1 == -2) {
			uusi = new Tilanne(this, new int[][] {{i1, j1}, {puoli*7, 0}}, new int[][] {{i2, j2}, {puoli*7, 3}});
		} else if (lauta[i1][j1].muoto == 5 && j2 - j1 == 2) {
			uusi = new Tilanne(this, new int[][] {{i1, j1}, {puoli*7, 7}}, new int[][] {{i2, j2}, {puoli*7, 5}});
		} else if (lauta[i1][j1].muoto == 0 && j2 != j1 && lauta[i2][j2] == null) {
			int suunta = 1;
			if (puoli == 1) suunta = -1;
			if (lauta[i2+suunta][j2] == null) return null;
			uusi = new Tilanne(this, new int[][] {{i1, j1}, {i2+suunta, j2}}, new int[][]{{i2, j2}, {-1, 0}});
		} else uusi = new Tilanne(this, new int[][] {{i1, j1}}, new int[][] {{i2, j2}});
		if (i2 == 7 - puoli*7 && uusi.lauta[i2][j2].muoto == 0) uusi.lauta[i2][j2] = new Nappula(muoto, puoli);
		uusi.tulosta();
		for (Tilanne t : seuraajat(puoli)) {
			if (uusi.equals(t)) return t;
		}
		return null;
	}
	
	
	/**
	 * Arvioi parhaan siirron, mikä tilanteessa voidaan tehdä. Käytetään minmax-algoritmia.
	 * @param pelaaja kumman puolen kannalta tutkitaan
	 * @return tilanne, joka parhaaksi arvioitua siirtoa seuraa
	 */
	public Tilanne parasSiirto(int pelaaja) {
		ArrayList<Tilanne> ehdokkaat = seuraajat(pelaaja);
		int n = ehdokkaat.size();
		if (n == 1) return ehdokkaat.get(0); // Jos on vain yksi laillinen siirto, on käytettävä sitä.
		Collections.shuffle(ehdokkaat);
		double[] payoff = new double[n];
		long deadline = System.currentTimeMillis() + 10000;
		double arvo = 1;
		int rekursio = 3;
		if (pelaaja == 1) arvo = 0;
		for (int i = 0; i < ehdokkaat.size() && System.currentTimeMillis() < deadline; i++) {
			Tilanne seuraaja = ehdokkaat.get(i);
			double skenaario;
			if (pelaaja == 1) skenaario = seuraaja.payoff(rekursio, 0, arvo, 1);
			else skenaario = seuraaja.payoff(rekursio, 1, 0, arvo);
			if (deadline < System.currentTimeMillis()) break;
			payoff[i] = skenaario;
			if (pelaaja == 1 && arvo < skenaario || pelaaja == 0 && skenaario < arvo) arvo = skenaario;
		}
		int parasPaikka = 0;
		for (int i = 1; i < n; i++) {
			if (pelaaja == 1 && payoff[parasPaikka] < payoff[i] || pelaaja == 0 && payoff[i] < payoff[parasPaikka]) parasPaikka = i;
		}
		return ehdokkaat.get(parasPaikka);
	}

	
	/**
	 * @return ruudut, joista nappulat ovat viime siirrolla syntyneet
	 */
	public int[][] getLahdot() {
		return lahdot;
	}
	
	
	/**
	 * @return ruudut, joihin nappulat ovat viime siirrolla siirtyneet
	 */
	public int[][] getMaalit() {
		return maalit;
	}
	
	
	/**
	 * @param i rivi
	 * @param j sarake
	 * @return ruudulla oleva nappula
	 */
	public Nappula getNappula(int i, int j) {
		return lauta[i][j];
	}
	
	
	/**
	 * Pääohjelmassa on shakin komentoriviversio.
	 * @param args
	 */
	public static void main(String[] args) {
		Tilanne peli = new Tilanne();
		System.out.println("Anna puoli (0=valkoinen, muu=musta)>");
		String syote = LUKIJA.nextLine();
		int pelaaja = 0;
		if (!syote.equals("0")) pelaaja = 1;
		int puoli = 0;
		while (peli.seuraajat(puoli).size() != 0) {
			peli.tulosta();
			if (puoli == pelaaja) peli = peli.syotaSiirto(puoli);
			else peli = peli.parasSiirto(puoli);
			puoli = (puoli + 1)%2;
		}
		peli.tulosta();
		if (peli.matti(puoli)) {
			if (puoli == 1) System.out.println("Shakkimatti, valkoinen voitti.");
			else System.out.println("Shakkimatti, musta voitti.");
		} else System.out.println("Patti: "+puoli);
	}
}
package de.phbouillon.android.games.alite.io;

/* Alite - Discover the Universe on your Favorite Android Device
 * Copyright (C) 2015 Philipp Bouillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful and
 * fun, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * http://http://www.gnu.org/licenses/gpl-3.0.txt.
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import de.phbouillon.android.games.alite.Alite;
import de.phbouillon.android.games.alite.AliteLog;
import de.phbouillon.android.games.alite.L;
import de.phbouillon.android.games.alite.R;
import de.phbouillon.android.games.alite.model.CommanderData;
import de.phbouillon.android.games.alite.model.Equipment;
import de.phbouillon.android.games.alite.model.EquipmentStore;
import de.phbouillon.android.games.alite.model.InventoryItem;
import de.phbouillon.android.games.alite.model.Laser;
import de.phbouillon.android.games.alite.model.LegalStatus;
import de.phbouillon.android.games.alite.model.Player;
import de.phbouillon.android.games.alite.model.PlayerCobra;
import de.phbouillon.android.games.alite.model.Rating;
import de.phbouillon.android.games.alite.model.Weight;
import de.phbouillon.android.games.alite.model.generator.GalaxyGenerator;
import de.phbouillon.android.games.alite.model.generator.StringUtil;
import de.phbouillon.android.games.alite.model.generator.SystemData;
import de.phbouillon.android.games.alite.model.missions.Mission;
import de.phbouillon.android.games.alite.model.missions.MissionManager;
import de.phbouillon.android.games.alite.model.trading.TradeGood;
import de.phbouillon.android.games.alite.model.trading.TradeGoodStore;

public class FileUtils {
	private static final String ALITE_COMMANDER_EXTENSION = ".cmdr";

	private static final String[] keys = new String[] {
        "JMz343q8dmb~yC5UUTf8w151GY99P15=iZ687O(Ae8473L39iz6x468l",
        "3+6~1cIWu2jdjP-OaxIq5W#nAf28YF{5H5Dgtf}1u64HfBrVyD8>8t{2",
        "Sn}6HeBJ13pJ&P0J5z1UY63GA1e8ni13O5yMw4FN2p1nIQx925WfE7f_",
        "n15645c3tfh8ah144YZ60WF3W0AL5A01qCZ#s0d9wq3t1zJbwsbX87N6",
        "MJo5i1K5MbpW15jN374M2zw}5l7B11zq^9%2S(j'HJ=|~>NT2S:9xD)n",
        ")?=}]+m@{D@fL 2E<[8':;>[7.>[Cz16=N##4<,qN.]7~S8@571QV>x<",
        "n*3).jH-e.9)K3A%7o8;701bj17&G5Vh)&4z8jxx6_ n%F&+]5,'[7:F",
        "R'+-4wf6)1Q?%%)3m2]&@]K]&%X*+W_#(*'1^;+o)!X@;2$O[&r9=Q!2",
        "[)1#qy)=2Q>u[72++20~x'^7F.8F^YE/+]91\"2{+#473E-Y:-+{>:;:[",
        "<{q:I](Y5,;o-|_d(_32%.rO3.,;5!b_52#t[:3?/B\"'{>[oR_41[64;",
        "~,2[{8M*_)}.=N[T3MJ21jTTxZ531btUlXJ48U4ZXfje58YGKJ2RB60x",
        "B1m3S4vuC43q,-01nO6A377d~[)L#(Y!'_2(1)0W0\"~9@X8.;<(_';$5",
        "9)|]4^86F|($,!%-%k\"8}t\"&>!&1[5:czdkt{ccH,1;nKISF&268J/cB",
        "rfp|4:G)HMEVhRpOqt.L.|=krD1v6xgD13Q>*PvxQ}!:4n2J)]=\":*5_",
        ".3bBXoQ3_=|\"{i}1+5@f~rG7[~]+F)]\",.E;LO( .%O2]^)@Y<4r-F!8",
        "6)@5/%59}7\"\")rN[.-.F&h=.;^'[%W->{<:|/i2'4{&8tG<u'\"=_G17m",
        "zaObfFPL>cb3t7jOk4O7ustpbZtVmkhDPafLmsmYbjggoF&RoVHutBTG",
        "rwNJ3D%qZ^.]1l/@9;&)$1~(G_N,[<|F4/%<';,)_r4D#3&.oS!>(@)^",
        "O#/7,=-?46c#zcm0h6-(9L;/iiMtpW6}w8722JO:#<26:6O1$51. {}7",
        "+G<>1t+O[9NW0250k^8T52D8u913>7]!7y=+|'ri[Se^7h@xq*$M{#H_",
        "5YP*@,~3(G_1#i}\"x796s,xd&&I:)^#8oFV1/o=6v$3XWxVy77KerEU4",
        "Xjhp8tw%RBk3>;G8LzDMFq2eQ0dwvdP7trrDc]sS48,$L3\"u26{*<Z3M",
        "3|Ys>u2mb8),{:2 M!;%?~+5('>77^ D7.3:R@*#?f558~5xToMYx8G]",
        "ElBOBh)g;<AvPpYOK%61cou4^^MhYzCjsc3)NNUjDQ1P]T5YzqxpwCV2",
        "Cj<xo83O n8%7)<_$<_,[]h]7$H:0-@6]*/,1p5@;>&) >/< )l0&f(k",
        " }>{4'~Pl|wkU]D81Mv!F4s65TL@a:m-Uv[1*K_565RuHSt'IL~D|W!R",
        "b|WTV/1O8;}zc7B9QM;l+QwA.aRdp4AK8Jc!Hs8nwQg6O4qxg*l2v8lq",
        "n57ukdlvPaY2uEuzbg*u7@9Owq_0TUm/v8fY7OCS18q-f+CcS8bO!@6x",
        "_q9?g8b!-YG9wV#Met%.X.8_orih%XEvB5cQbI8=gv/H3P3_=O7Z.7Z;",
        "4*c*Z=CnUY8#%x8V7/py.1QY+ln1n27Qfejl2_qfli+ee*DTdlo.Sy/D",
        "wEC-dY/posjlOWq-fCFm#9=.iUXCN8;3ff*x=2JViU@9dpEF5zqPsjM+",
        "KDcIA;b3u2F6h%?0=7Dt_Hs%QQIlS4L!SCm@ZBUl=JPxjUwB4*W8fq#C",
        "ck#6**k*rJofcEa#mpz9%?f_rG4ciZnyLje*KkwDvZGZ3RvzlfV@@EKn",
        "E!P5IIeOl*U=xl4C%wNp-GPahE#CEx8GY@7OV@?n!-3#;2C/xT;R#rz.",
        "2-XEQbN_DNr61p3Bswu/ZYd2C#c8zqO3N9jgq@Wqy6+6KSVHm!nS;0vd",
        "isCZ/cTk/+s%GXi+ZDXqlurwlUW4D_6jNF..-;rWnZ6XwcBOjz%=cF%-",
        "AS31GmhCDI1AZk/cjBDL+UJQSHUaD@HHKJ.sQ*PA%4TnyA*/znhaC4B2",
        "Jni.RG.NBs+PmdLt8CSGBsoRRUyng3GfISj0dR6yvFmFNwkWb+pAl?0n",
        ".R3_fi4VmfGT=hPrSgX!GiEs7bLsOG=Np_2Mk9eWYQq5qRc5m;4Fu4hm",
        "MLMDWLHfl%0;2DHgBBu*%WCq/R.RZ-q7mPAYwaANJKTxneOr*XDsKrt1",
        "ItEFM@GwZ=imC67yBg25CbCrCdbB7.w?tKOCW?ATDuYt_zoGncr0*-94",
        "qjC5lE*WQo=#r4bGgj1fDgua*Vled!E4Xgi9%R4/4;Zf5wl5BlUdKe1K",
        "*y=Qpi;XjMrKVoNLr1yGGPrP63c#yyt=2Y;QYogV/B_tFakjYGzf2vEw",
        "H0g9FFw7pxf;r_@MVcGET;KaP=qr/ONHTYJuHuQ=cLEe!n%bQmKaoAdJ",
        "qBqLmE7bkY0T+SFxLNzH=2k2BPbkD9pP5/DGDEeXaz?u9wn;hjObXVQ-",
        "o-KX0XXx5hY-O.uH5c3Q8@=byeCqvO!v6PoP=HuCXT-bXz5i-Hs!p/Cy",
        "?6vVU3CrlzE/hfNy50-oBoD/vr1nsAG6M@UKiY5vRU@vu6Ba4=N*Llwd",
        "2wx0Cl.ltV9qLDPh?QW4yfb*qVuz+QvxR@e9xFt3WbBZ_iV@03??P#2S",
        "R8K3#WycxLYUvd6*upiWeC%H/3.HW0iZKIo*!**A0vz7JASX_FXwgg%S",
        "%C3KXrG7r.Jn*??40is5aI..lxKRc9=1PV_%I1zJp2HK#1hCKp!.+J%7",
        "E5wxg-r#FeXr;Bw8vjQuA+H6PIG9@?@FcM-aFBz0LPFje@FJ=;Pb=V6Q",
        "x@hY54BLnSO7MW=46mj85ucUC;=%2mLHcvzPrb!dO=!LyU_uEird8.iC",
        "kIV7+IgBGHfot0MfrFt-zQx5BW-T!5VYKw1jFKqFk?Fq*eAaVL82ctaU",
        "hn6nUMGY9Bb_/0agmna5KG%tVl;6_pBpWhBpbC8T044-DEbs8cP-TsJa",
        "uMR6OWcT/8wpy7Fk2C=dZEqRJb;raNW1Xx?P7CJocl*Re9?AS2IQy3Bp",
        "tAw9Z7=8m!RA@@zz=cB-PZ+Xh_UuFGCrwcs7dWOrrD2ir?KgCfWV1NaQ",
        "LYcdnV3f%xalU%WtNgTLtb2n8+rw63*jBYtE?8DVXWq?qwMKIzIU=Ox9",
        "t*rz1HvJx#j!VSzbd;-X88_MyhnGvrp+y7iERbrQxeJzNQ-@A3#!aaB7",
        "%fBUqpnQYwUl7vKBYuwezU/itwG%0ujY6xM8aXwI@=an!ZFbs*p20uIv",
        "gpy=ivn1M_2KjZyG;nn5=imPj1W+E3uMonc7w2xRdwa/u!%TOj5NGZ1.",
        "DeZ3Z0wgZqs!;#gS*VcTHmvv8Dvr.gFMye3Rd?%0A?xEZ;WWCmY7_i@n",
        "60?VEEh%kdJqLP*!dzTM*rV-4ZpiIVSbnPiz/tv6d4r_-2L9xceTO1xP",
        "fGo1m/7PK5C!4N_6DYzI/Z/BO272@.M9pSU=F?fk_b8G0QFenC_=DiCd",
        "aLyUWm*t/je_5anUWS/cZgnP.jX4saEXrWI;?-Z?Zmrf-ynWmyt7iwE5",
        "njW9PHAvfGbjRpah=FD+-HaW_BS?3T*?SOYipn1Rl!A4XDE_YM#ctQGo",
        "IA@N*G%KOhRCR#%ca8Z%Gs/%_VsrbjYBI7ZpwobfD+n*9B2gTlB7#Ohm",
        "B;RqkX75JP?gpZ3yb?-TFJG?qMX*hnK?a9oyW8?2C53tHV#?5Pa;ZNh#",
        "#W/YA!XsFAG28Emv/IQXcj+RRdHSMMrMcw7A8t2HhKnGOLI1z1;Q_Kme",
        "i1Ct+Ff%f?aMUm+ddJ*R_4HJVdpiPbPOIcoHqJTc2uyT8!gN#r++UUi1",
        "u=Yhm;0E2IC%;39T*jmYG+XZ_cqvnb;*=+jx89@u5to0=oPL4q;84Kv7",
        "t.kJeFoaLM.KTMiDt5djVtffNf@_eLI5PzD9Y4a*FOp4pF#kG=P/3Ll/",
        "pF!.Or!/EP?rIw2*fodHDy*IoV-l_ICDNETS8bxlSq0aKXjiNZrP4d!2",
        "N+h=2hC!N#dEckDD#%*k.l;Q8cXto+V6IcB;?357o4MS?fG0WnY*oi%1",
        "*QIaKfeVkt!AiFJKKF_RZ2WdWuXAmgSxjWI0vewIZ9xXkGRQeN1jmycK",
        "?1EnIe-MaUnL;glta+#mF.%QcGtY26L7?;5=aJA!.l;PpFT;z.Dy6@F!",
        "9+KB7mgrI2R/xrcW9_orBzxSzvw33jAE%+Hm./KiEdH9-TXHt3*+Yl?J",
        "M-NM@0+w1NG+j*X+8Fmvs-hqYpJ/uN2Z3.ZK8.saphHyMju4gCCT.WnY",
        "dt+Yq-Cohz?lmNB9jMFL36e-?cqrLigtiB3OLU7raqQkoJSSAP69Qcrt",
        "P.dOVhd/TYDlg9yPbb2Np=X8JXr!yucFgK/M#sf.MRQPF48Lgrh*xeDV",
        "yrE;@hXw_;EW+mY6_3AY0H2clcfHMmfBG0kWohI2?rTIzmaATx==d2sj",
        "0yikCQjgpDQKBBSlvVeI+u0/Ce+ltmubH=h*.v01I9pvWQ/SffUIXk3Q",
        ";jhuc;+CZ-CHqcmAs0ABEE09fsFORmDuNR#mNFYK/nQhN#yD%Umuk2L=",
        "5vP6#v7%8f?Zk/9gW*Bo-Tvn=CV;/fJiF.4o@oTb2Xv4;Zp=i/Sv1uFm",
        "Ay@-Z+%dVjUmSUw9sx8nv#qkdcl/Ml4i=RoPyeB#eSilECxNagCE8o#I",
        "HVb;Y=Q729tA2gLNv=IcfFmh1Yw3jzwWWfHdCEqlBy6Sgxi=U-f#fl0C",
        "5?SAUjYCMRwWZ4Kraub3WTBE@2iuU5!*2EvI9pRhV3GOALrm0-n8K_3l",
        "=NKoB14eW7rUJC47c9dpN9D1L_=fRJ25YJPvfznw/z%QrWcpF2gXpNYG",
        "JgdE7YMMF8!A+IczV4qk?+K-qRYl0ePK8Im/Wi-xn2L5ac-.Z3B7u+MS",
        "SplpAxjxs8P1tdd+==;OybkNdaL_cen;!P03;Vx*TkMpNJb_aO#1@.u?",
        "EXMwmmULRS5d0w*uTI?OO?AT/PsNn8G;s5zwJAZ3P.U?b;S0YTi4Dg2K",
        "X%xW?HxjL88?;QlPvO.PxF%B;%-TaZ=MDoZRaD9IY86pMX3I5P/*imkR",
        "/wZBSeI9#v#f#U%2b*#-VZ-oJ3kKZE1nEHR107@=z;yngXQlXUlsuv!x",
        "70*l=.j9?aa6COBq/.MYyXQ-m.+FqI=aKr=A?bA1l-tF_hiJS?twju/k",
        "jB/?KeT8h?uiVAvu.h990L#Fm19vmT;orfo=cFqFk7uC9A-SV/yB7M6Y",
        "J=fgrvWO_F@*j2R=#2R=n*Sm@ramr_4fvP7jwJm_lfF4J.5iu5KE!@7T",
        "y%qAU6tAYiRT!5T4QUfH6ld#86598zj4fn74qly19Efx3mv/mc-xizkK",
        "fyPkB7Ow.j-e;lO2s0V1F2LWL6Wf+9W3DvR#WO0B*6zUyQBAOJE4d5H7",
        "n%ZSk68KKHbZUXfeAT*WD#v!4WR14oZUnhad-hoG;mHq-z3#0x4J=83w",
        "UdlOpGev5G89_Cpis5Yn7EHEpZBH7Pb210Ii_ao#Ii74@?!xHPeKkQ*Q"
    };

	private static final String ENCRYPTION = "Blowfish";
	private static final int COMMANDER_FILE_FORMAT_VERSION = 3;

	private Cipher cipher;
	private Alite game;

	// allowed max. value is 31, after that new field has to be used for version number
	private byte fileFormatVersion;

	private static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz_";
	private static final Random rnd = new Random();

	private String generateRandomString(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		}
		return sb.toString();
	}

	private String generateRandomFilename() {
		String fileName;
		do {
			fileName = CommanderData.DIRECTORY_COMMANDER + generateRandomString(12) + ALITE_COMMANDER_EXTENSION;
		} while (game.getFileIO().exists(fileName));
		return fileName;
	}

	public FileUtils() {
		game = Alite.get();
		try {
			cipher = Cipher.getInstance(ENCRYPTION);
		} catch (NoSuchAlgorithmException ignored) {
			AliteLog.e("FileUtils initializer", "Encryption not available");
		} catch (NoSuchPaddingException ignored) {
			AliteLog.e("FileUtils initialized", "Padding not available");
		}
	}

	@SuppressLint("TrulyRandom")
	private byte[] encrypt(byte[] toEncrypt, String strKey) {
		byte[] result = toEncrypt;
		if (cipher != null) {
			try {
				SecretKeySpec key = new SecretKeySpec(strKey.getBytes(StringUtil.CHARSET), ENCRYPTION);
				cipher.init(Cipher.ENCRYPT_MODE, key);
				result = cipher.doFinal(toEncrypt);
			} catch (GeneralSecurityException e) {
				AliteLog.e("Encrypt", "Error During Encryption", e);
			}
		}
		return result;
	}

	private byte[] decrypt(byte[] toDecrypt, String strKey) {
		byte[] result = toDecrypt;
		if (cipher != null) {
			try {
				SecretKeySpec key = new SecretKeySpec(strKey.getBytes(StringUtil.CHARSET), ENCRYPTION);
				cipher.init(Cipher.DECRYPT_MODE, key);
				result = cipher.doFinal(toDecrypt);
			} catch (GeneralSecurityException e) {
				AliteLog.e("Decrypt", "Error During Decryption", e);
			}
		}
		return result;
	}

	private byte[] encodeLongs(long[] toEncrypt, String strKey) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(toEncrypt.length * 8);
		for (long l: toEncrypt) {
			buffer.putLong(l);
		}
		byte[] input = new byte[toEncrypt.length * 8];
		buffer.flip();
		buffer.get(input);
		return encrypt(input, strKey);
	}

	private long[] decodeLongs(byte[] toDecrypt, String strKey) {
		byte[] decrypted = decrypt(toDecrypt, strKey);

		ByteBuffer buffer = ByteBuffer.allocateDirect(decrypted.length);
		buffer.put(decrypted);
		buffer.flip();
		long[] result = new long[buffer.limit() / 8];
		for (int i = 0; i < buffer.limit() / 8; i++) {
			result[i] = buffer.getLong(i);
		}

		return result;
	}

	private void writeString(DataOutputStream dos, String string) throws IOException {
		dos.writeByte(string.getBytes().length);
		dos.write(string.getBytes());
	}

	private String readStringWithLength(DataInputStream dis) throws IOException {
		return readString(dis, dis.readByte());
	}

	private String readString(DataInputStream dis, int size) throws IOException {
		byte[] input = new byte[size];
		dis.read(input);
		return new String(input);
	}

	private byte[] zipBytes(final byte[] input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			ZipEntry entry = new ZipEntry("_");
			entry.setSize(input.length);
			zos.putNextEntry(entry);
			zos.write(input);
			zos.closeEntry();
		}
		return baos.toByteArray();
	}

	private byte[] unzipBytes(final byte[] input) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(input);
		byte[] buffer = new byte[1024];
		try (ZipInputStream zip = new ZipInputStream(bais);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length)) {
			int read;
			if (zip.getNextEntry() != null) {
				while ((read = zip.read(buffer)) != -1) {
					baos.write(buffer, 0, read);
				}
			}
			return baos.toByteArray();
		}
	}

	private void setEquipped(PlayerCobra cobra, Equipment equip, boolean value) {
		if (value) {
			cobra.addEquipment(equip);
		} else {
			cobra.removeEquipment(equip);
		}
	}

	private int readByte(DataInputStream dis) throws IOException {
		return dis.readByte() & 0xFF;
	}

	public final void loadCommander(DataInputStream dis) throws IOException {
		game.getPlayer().setName(loadVersionAndPlayerName(dis));
		if (fileFormatVersion <= 2) {
			loadCommanderUnderV3(dis);
		} else {
			loadCommanderV3(dis);
		}
		AliteLog.d("[ALITE] loadCommander", String.format("Loaded Commander (v%d) '%s', galaxyNumber: %d",
			fileFormatVersion, game.getPlayer().getName(), game.getGenerator().getCurrentGalaxy()));
	}

	private void loadCommanderV3(DataInputStream dis) throws IOException {
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = player.getCobra();
		cobra.clearInventory();
		cobra.clearEquipment();
		generator.buildGalaxy(readByte(dis));
		player.setCurrentSystem(generator.getSystem(readByte(dis)));
		player.setHyperspaceSystem(generator.getSystem(readByte(dis)));
		if (player.getCurrentSystem() == null) {
			if (player.getHyperspaceSystem() == null) {
				player.setHyperspaceSystem(generator.getSystem(0));
				player.setCurrentSystem(generator.getSystem(0));
			} else {
				player.setCurrentSystem(player.getHyperspaceSystem());
			}
		}
		cobra.setFuel(readByte(dis));
		player.setCash(dis.readLong());
		player.setRating(Rating.values()[readByte(dis)]);
		player.setLegalStatus(LegalStatus.values()[readByte(dis)]);
		game.setGameTime(dis.readLong());
		player.setScore(dis.readInt());
		player.getMarket().setFluct(dis.read());
		player.getMarket().generate();
		int val = dis.readInt();
		AliteLog.d("LOADING COMMANDER", "Parsing Tradegoods " + val);
		for (int i = 0; i < val; i++) {
			player.getMarket().setQuantity(TradeGoodStore.get().getGoodById(dis.readInt()), dis.read());
		}

		player.setLegalValue(dis.readInt());
		cobra.setRetroRocketsUseCount(dis.readInt());
		int currentSystem = dis.readInt();
		int hyperspaceSystem = dis.readInt();
		if (generator.getCurrentGalaxy() == 8 && player.getRating() == Rating.ELITE) {
			if (player.getCurrentSystem() != null && player.getCurrentSystem().getIndex() == 0 && currentSystem == 1) {
				player.setCurrentSystem(SystemData.RAXXLA_SYSTEM);
			}
			if (player.getHyperspaceSystem() != null && player.getHyperspaceSystem().getIndex() == 0 && hyperspaceSystem == 1) {
				player.setHyperspaceSystem(SystemData.RAXXLA_SYSTEM);
			}
		}

		AliteLog.d("LOADING COMMANDER", "Getting IJC, JC, and SCs");
		player.setIntergalacticJumpCounter(dis.readInt());
		player.setJumpCounter(dis.readInt());
		int x = dis.readInt();
		int y = dis.readInt();
		if (x != 0 || y != 0) {
			player.setPosition(x, y);
			player.setCurrentSystem(null);
		}

		cobra.setMissiles(readByte(dis));
		val = dis.readInt();
		AliteLog.d("LOADING COMMANDER", "Parsing Equipment " + val);
		for (int i = 0; i < val; i++) {
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(dis.readInt()));
		}
		cobra.setLaser(PlayerCobra.DIR_FRONT, (Laser) EquipmentStore.get().getEquipmentById(dis.readInt()));
		cobra.setLaser(PlayerCobra.DIR_RIGHT, (Laser) EquipmentStore.get().getEquipmentById(dis.readInt()));
		cobra.setLaser(PlayerCobra.DIR_REAR, (Laser) EquipmentStore.get().getEquipmentById(dis.readInt()));
		cobra.setLaser(PlayerCobra.DIR_LEFT, (Laser) EquipmentStore.get().getEquipmentById(dis.readInt()));

		player.setCheater(readByte(dis) != 0);
		player.setKillCount(dis.readInt());

		player.clearMissions();
		AliteLog.d("LOADING COMMANDER", "Loading Missions");
		int activeMissionCount = dis.readInt();
		AliteLog.d("Loading Commander", "Active missions: " + activeMissionCount);
		int completedMissionCount = dis.readInt();
		Set<Integer> missionIds = new HashSet<>();
		for (int i = 0; i < activeMissionCount; i++) {
			int missionId = dis.readInt();
			Mission m = MissionManager.getInstance().get(missionId);
			if (m == null) {
				AliteLog.e("[ALITE] loadCommander", "Invalid active mission id: " + missionId + " - skipping. The commander file seems to be broken.");
				continue;
			}
			m.load(dis);
			if (!missionIds.contains(missionId)) {
				missionIds.add(missionId);
				player.addActiveMission(m);
				AliteLog.d("Loading Commander", "Active mission: " + m.getClass().getName());
			} else {
				AliteLog.d("Warning: Duplicate mission", "  Duplicate mission: " + m.getClass().getName() + " -- ignoring.");
			}
		}
		for (int i = 0; i < completedMissionCount; i++) {
			int missionId = dis.readInt();
			Mission m = MissionManager.getInstance().get(missionId);
			if (m == null) {
				AliteLog.e("[ALITE] loadCommander", "Invalid completed mission id: " + missionId +
					" - skipping. The commander file seems to be broken.");
				continue;
			}
			if (missionIds.contains(missionId)) {
				player.removeActiveMission(m);
			}
			player.addCompletedMission(m);
		}

		val = dis.readInt();
		AliteLog.d("Loading Commander", "Inventory items: " + val);
		for (int i = 0; i < val; i++) {
			TradeGood good = TradeGoodStore.get().getGoodById(dis.readInt());
			long weightInGrams = dis.readLong();
			long price = dis.readLong();
			long unpunishedWeightInGrams = dis.readLong();
			if (good != null) {
				cobra.setTradeGood(good, Weight.grams(weightInGrams), price);
				cobra.addUnpunishedTradeGood(good, Weight.grams(unpunishedWeightInGrams));
			}
		}
	}

	public void loadCommanderUnderV3(DataInputStream dis) throws IOException {
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = player.getCobra();
		cobra.clearInventory();
		cobra.clearEquipment();
		generator.buildGalaxy(readByte(dis));
		dis.readChar(); // seed 0
		dis.readChar(); // seed 1
		dis.readChar(); // seed 2
		player.setCurrentSystem(generator.getSystem(readByte(dis)));
		player.setHyperspaceSystem(generator.getSystem(readByte(dis)));
		if (player.getCurrentSystem() == null) {
			if (player.getHyperspaceSystem() == null) {
				player.setHyperspaceSystem(generator.getSystem(0));
				player.setCurrentSystem(generator.getSystem(0));
			} else {
				player.setCurrentSystem(player.getHyperspaceSystem());
			}
		}
		cobra.setFuel(readByte(dis));
		player.setCash(dis.readLong());
		player.setRating(Rating.values()[readByte(dis)]);
		player.setLegalStatus(LegalStatus.values()[readByte(dis)]);
		game.setGameTime(dis.readLong());
		player.setScore(dis.readInt());
		player.getMarket().setFluct(dis.read());
		player.getMarket().generate();
		AliteLog.d("LOADING COMMANDER", "Parsing Tradegoods");
		for (TradeGood tg : TradeGoodStore.get().goods()) {
			if (!tg.isSpecialGood()) {
				player.getMarket().setQuantity(tg, dis.read());
			}
		}
		for (TradeGood tg : TradeGoodStore.get().goods()) {
			if (!tg.isSpecialGood()) {
				int gram = dis.readInt();
				if (gram > 0) {
					cobra.addTradeGood(tg, Weight.grams(gram), 0);
				}
			}
		}
		player.setLegalValue(dis.readInt());
		cobra.setRetroRocketsUseCount(dis.readInt());
		int currentSystem = dis.readInt();
		int hyperspaceSystem = dis.readInt();
		if (generator.getCurrentGalaxy() == 8 && player.getRating() == Rating.ELITE) {
			if (player.getCurrentSystem() != null && player.getCurrentSystem().getIndex() == 0 && currentSystem == 1) {
				player.setCurrentSystem(SystemData.RAXXLA_SYSTEM);
			}
			if (player.getHyperspaceSystem() != null && player.getHyperspaceSystem().getIndex() == 0 && hyperspaceSystem == 1) {
				player.setHyperspaceSystem(SystemData.RAXXLA_SYSTEM);
			}
		}
		AliteLog.d("LOADING COMMANDER", "Getting IJC, JC, and SCs");
		player.setIntergalacticJumpCounter(dis.readInt());
		player.setJumpCounter(dis.readInt());
		int grams = dis.readInt();
		if (grams != 0) {
			cobra.setTradeGood(TradeGoodStore.get().getGoodById(TradeGoodStore.THARGOID_DOCUMENTS), Weight.grams(grams),0);
		}
		int tons = dis.readInt();
		if (tons != 0) {
			cobra.setTradeGood(TradeGoodStore.get().getGoodById(TradeGoodStore.UNHAPPY_REFUGEES), cobra.getFreeCargo(), 0);
		}
		int x = dis.readInt();
		int y = dis.readInt();
		if (x != 0 || y != 0) {
			player.setPosition(x, y);
			player.setCurrentSystem(null);
		}
		dis.readLong(); // Placeholder for "special cargo"
		dis.readLong(); // Placeholder for "special cargo"
		dis.readLong(); // Placeholder for "special cargo"
		AliteLog.d("LOADING COMMANDER", "Parsing Equipment");
		int missileEnergy = readByte(dis);
		cobra.setMissiles(missileEnergy & 7);
		if (missileEnergy > 7) {
			cobra.addEquipment(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT));
		}
		int equipment = readByte(dis);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY), (equipment & 1) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_SYSTEM), (equipment & 2) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP), (equipment & 4) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE), (equipment & 8) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB), (equipment & 16) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER), (equipment & 32) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE), (equipment & 64) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS), (equipment & 128) > 0);
		int laser = readByte(dis);
		Laser.equipLaser(laser & 15, EquipmentStore.PULSE_LASER, cobra);
		Laser.equipLaser(laser >> 4, EquipmentStore.BEAM_LASER, cobra);
		laser = readByte(dis);
		Laser.equipLaser(laser & 15, EquipmentStore.MINING_LASER, cobra);
		Laser.equipLaser(laser >> 4, EquipmentStore.MILITARY_LASER, cobra);
		equipment = readByte(dis);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT), (equipment & 1) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE), (equipment & 2) > 0);
		setEquipped(cobra, EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_JAMMER), (equipment & 4) > 0);
		player.setCheater(readByte(dis) != 0);
		readByte(dis); // Placeholder for "special equipment"
		readByte(dis); // Placeholder for "special equipment"
		dis.readInt(); // Placeholder for "special equipment"
		readByte(dis); // Placeholder for number of kills to next "Right On, Commander"-Msg.
		dis.readShort(); // Placeholder for number of kills to next "Good Shooting, Commander"-Msg.
		player.setKillCount(dis.readInt());

		// Deprecated: Used to contain the statistics filename here...
		byte[] buffer = new byte[23];
		dis.read(buffer, 0, 23);

		player.clearMissions();
		AliteLog.d("LOADING COMMANDER", "Loading Missions");
		try {
			int activeMissionCount = dis.readInt();
			AliteLog.d("Loading Commander", "Active missions: " + activeMissionCount);
			int completedMissionCount = dis.readInt();
			Set<Integer> missionIds = new HashSet<>();
			for (int i = 0; i < activeMissionCount; i++) {
				int missionId = dis.readInt();
				Mission m = MissionManager.getInstance().get(missionId);
				if (m == null) {
					AliteLog.e("[ALITE] loadCommander", "Invalid active mission id: " + missionId + " - skipping. The commander file seems to be broken.");
					continue;
				}
				m.load(dis);
				if (!missionIds.contains(missionId)) {
					missionIds.add(missionId);
					player.addActiveMission(m);
					AliteLog.d("Loading Commander", "  Active mission: " + m.getClass().getName());
				} else {
					AliteLog.d("Warning: Duplicate mission", "  Duplicate mission: " + m.getClass().getName() + " -- ignoring.");
				}
			}
			for (int i = 0; i < completedMissionCount; i++) {
				int missionId = dis.readInt();
				Mission m = MissionManager.getInstance().get(missionId);
				if (m == null) {
					AliteLog.e("[ALITE] loadCommander", "Invalid completed mission id: " + missionId +
						" - skipping. The commander file seems to be broken.");
					continue;
				}
				if (missionIds.contains(missionId)) {
					player.removeActiveMission(m);
				}
				player.addCompletedMission(m);
			}

			for (TradeGood good : TradeGoodStore.get().goods()) {
				if (!good.isSpecialGood()) {
					long price = dis.readLong();
					InventoryItem item = cobra.getInventoryItemByGood(good);
					if (item != null) {
						cobra.setTradeGood(good, item.getWeight(), price);
					}
				}
			}
			for (TradeGood good : TradeGoodStore.get().goods()) {
				if (!good.isSpecialGood()) {
					long weightInGrams = dis.readLong();
					InventoryItem item = cobra.getInventoryItemByGood(good);
					if (item != null) {
						cobra.setUnpunishedTradeGood(good, Weight.grams(weightInGrams));
					}
				}
			}
		} catch (IOException e) {
			AliteLog.e("[ALITE] loadCommander", "Old version. Cmdr data lacks mission data, price and unpunished data for inventory", e);
			// Alite commander file version 1: Did not store mission data. Ignore...
		}
	}

	private String loadVersionAndPlayerName(DataInputStream dis) throws IOException {
		fileFormatVersion = dis.readByte();
		// Before versioning player name was placed here directly
		if (fileFormatVersion >= 32) {
			// so the read byte is not the file version number but the first char of the player name
			String playerName = (char) fileFormatVersion + readString(dis, 15).trim();
			fileFormatVersion = 1;
			return playerName;
		}
		return readStringWithLength(dis);
	}

	public final void loadCommander(String fileName) throws IOException {
		AliteLog.d("LOADING COMMANDER", "Filename = " + fileName);
		byte[] commanderData = getCommanderData(fileName);
		if (commanderData == null) {
			throw new IOException("Ouch! Couldn't load commander " + fileName + ". No changes to current commander were made.");
		}
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(commanderData))) {
			loadCommander(dis);
		}
		AliteLog.d("LOADING COMMANDER", "DONE...");
	}

	private byte[] getCommanderData(String fileName) throws IOException {
		return unzipBytes(decrypt(game.getFileIO().readFileContents(fileName,
			2 + getHeaderLength(fileName)), getKey(fileName)));
	}

	private int getHeaderLength(String fileName) throws IOException {
		try(ByteArrayInputStream bis = new ByteArrayInputStream(game.getFileIO().readPartialFileContents(fileName, 2))) {
			return bis.read() * 256 + bis.read();
		}
	}

	public final void saveCommander(String commanderName) throws IOException {
		saveCommander(commanderName, generateRandomFilename());
	}

	public final void saveCommander(String commanderName, String fileName) throws IOException {
		Player player = game.getPlayer();
		if (commanderName == null) {
			commanderName = player.getName();
		} else {
			player.setName(commanderName);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		DataOutputStream dos = new DataOutputStream(bos);
		saveCommander(dos);
		dos.close();
		byte[] commanderData = encrypt(zipBytes(bos.toByteArray()), getKey(fileName));

		bos = new ByteArrayOutputStream(1024);
		dos = new DataOutputStream(bos);
		dos.writeByte(COMMANDER_FILE_FORMAT_VERSION);
		writeString(dos, commanderName);
		writeString(dos, player.getCurrentSystem() == null ? L.string(R.string.cmdr_unknown_system) : player.getCurrentSystem().getName());
		dos.writeLong(game.getGameTime());
		dos.writeInt(player.getScore());
		dos.write(player.getRating().ordinal());
		byte[] headerData = encrypt(bos.toByteArray(), getKey(fileName));

		game.getFileIO().mkDir(CommanderData.DIRECTORY_COMMANDER);
		try (OutputStream fos = game.getFileIO().writeFile(fileName)) {
			fos.write(headerData.length >> 8);
			fos.write(headerData.length & 255);
			fos.write(headerData);
			fos.write(commanderData);
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				AliteLog.e("[Alite] saveCommander", "Error when writing commander (v" + COMMANDER_FILE_FORMAT_VERSION + ").", e);
			}
		}
		AliteLog.d("[Alite] saveCommander", "Saved Commander '" + player.getName() + "' (v" + COMMANDER_FILE_FORMAT_VERSION + ").");
	}

	public final void saveCommander(DataOutputStream dos) throws IOException {
		if (COMMANDER_FILE_FORMAT_VERSION <= 2) {
			saveCommanderUnderV3(dos);
		} else {
			saveCommanderV3(dos);
		}
	}

	private void saveCommanderV3(DataOutputStream dos) throws IOException {
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = player.getCobra();
		int marketFluct = player.getMarket().getFluct();
		dos.writeByte(COMMANDER_FILE_FORMAT_VERSION);
		writeString(dos, player.getName());
		dos.writeByte(generator.getCurrentGalaxy());
		int currentSystem = player.getCurrentSystem() == null ? 0 : player.getCurrentSystem().getIndex();
		int hyperspaceSystem = player.getHyperspaceSystem() == null ? 0 : player.getHyperspaceSystem().getIndex();
		dos.writeByte(currentSystem == 256 ? 0 : currentSystem);
		dos.writeByte(hyperspaceSystem == 256 ? 0 : hyperspaceSystem);
		dos.writeByte(cobra.getFuel());
		dos.writeLong(player.getCash());
		dos.writeByte(player.getRating().ordinal());
		dos.writeByte(player.getLegalStatus().ordinal());
		dos.writeLong(game.getGameTime());
		dos.writeInt(player.getScore());
		dos.write(marketFluct);
		dos.writeInt(TradeGoodStore.get().goods().size());
		for (TradeGood good: TradeGoodStore.get().goods()) {
			dos.writeInt(good.getId());
			dos.write(player.getMarket().getQuantity(good));
		}
		dos.writeInt(player.getLegalValue());
		dos.writeInt(cobra.getRetroRocketsUseCount());
		dos.writeInt(currentSystem == 256 ? 1 : 0);
		dos.writeInt(hyperspaceSystem == 256 ? 1 : 0);
		dos.writeInt(player.getIntergalacticJumpCounter());
		dos.writeInt(player.getJumpCounter());
		if (player.getCurrentSystem() == null && player.getPosition() != null) {
			dos.writeInt(player.getPosition().x);
			dos.writeInt(player.getPosition().y);
		} else {
			dos.writeLong(0);
		}
		dos.writeByte(cobra.getMissiles());
		dos.writeInt(cobra.getInstalledEquipment().size());
		for (Equipment e : cobra.getInstalledEquipment()) {
			dos.writeInt(e.getId());
		}
		dos.writeInt(cobra.getLaser(PlayerCobra.DIR_FRONT) != null ? cobra.getLaser(PlayerCobra.DIR_FRONT).getId() : -1);
		dos.writeInt(cobra.getLaser(PlayerCobra.DIR_RIGHT) != null ? cobra.getLaser(PlayerCobra.DIR_RIGHT).getId() : -1);
		dos.writeInt(cobra.getLaser(PlayerCobra.DIR_REAR) != null ? cobra.getLaser(PlayerCobra.DIR_REAR).getId() : -1);
		dos.writeInt(cobra.getLaser(PlayerCobra.DIR_LEFT) != null ? cobra.getLaser(PlayerCobra.DIR_LEFT).getId() : -1);

		dos.writeByte(player.isCheater() ? 1 : 0);
		dos.writeInt(player.getKillCount());
		dos.writeInt(player.getActiveMissions().size());
		dos.writeInt(player.getCompletedMissions().size());
		for (Mission m: player.getActiveMissions()) {
			dos.writeInt(m.getId());
			dos.write(m.save());
		}
		for (Mission m: player.getCompletedMissions()) {
			dos.writeInt(m.getId());
		}
		dos.writeInt(cobra.getInventory().size());
		for (InventoryItem item: cobra.getInventory()) {
			dos.writeInt(item.getGood().getId());
			dos.writeLong((int) item.getWeight().getWeightInGrams());
			dos.writeLong(item.getPrice());
			dos.writeLong(item.getUnpunished().getWeightInGrams());
		}
	}

	private void saveCommanderUnderV3(DataOutputStream dos) throws IOException {
		Player player = game.getPlayer();
		GalaxyGenerator generator = game.getGenerator();
		PlayerCobra cobra = player.getCobra();
		int marketFluct = player.getMarket().getFluct();
		dos.writeByte(COMMANDER_FILE_FORMAT_VERSION);
		writeString(dos, player.getName());
		dos.writeByte(generator.getCurrentGalaxy());
		dos.writeChar(0); // seed 0
		dos.writeChar(0); // seed 1
		dos.writeChar(0); // seed 2
		int currentSystem = player.getCurrentSystem() == null ? 0 : player.getCurrentSystem().getIndex();
		int hyperspaceSystem = player.getHyperspaceSystem() == null ? 0 : player.getHyperspaceSystem().getIndex();
		dos.writeByte(currentSystem == 256 ? 0 : currentSystem);
		dos.writeByte(hyperspaceSystem == 256 ? 0 : hyperspaceSystem);
		dos.writeByte(cobra.getFuel());
		dos.writeLong(player.getCash());
		dos.writeByte(player.getRating().ordinal());
		dos.writeByte(player.getLegalStatus().ordinal());
		dos.writeLong(game.getGameTime());
		dos.writeInt(player.getScore());
		dos.write(marketFluct);
		for (TradeGood good : TradeGoodStore.get().goods()) {
			if (!good.isSpecialGood()) {
				dos.write(player.getMarket().getQuantity(good));
			}
		}
		for (TradeGood good : TradeGoodStore.get().goods()) {
			if (!good.isSpecialGood()) {
				InventoryItem item = cobra.getInventoryItemByGood(good);
				dos.writeInt(item == null ? 0 : (int) item.getWeight().getWeightInGrams());
			}
		}
		dos.writeInt(player.getLegalValue());
		dos.writeInt(cobra.getRetroRocketsUseCount());
		dos.writeInt(currentSystem == 256 ? 1 : 0);
		dos.writeInt(hyperspaceSystem == 256 ? 1 : 0);
		dos.writeInt(player.getIntergalacticJumpCounter());
		dos.writeInt(player.getJumpCounter());
		InventoryItem item = cobra.getInventoryItemByGood(TradeGoodStore.get().getGoodById(TradeGoodStore.THARGOID_DOCUMENTS));
		dos.writeInt(item == null ? 0 : (int) item.getWeight().getWeightInGrams());
		item = cobra.getInventoryItemByGood(TradeGoodStore.get().getGoodById(TradeGoodStore.UNHAPPY_REFUGEES));
		dos.writeInt(item == null ? 0 : (int) item.getWeight().getWeightInGrams());
		if (player.getCurrentSystem() == null && player.getPosition() != null) {
			dos.writeInt(player.getPosition().x);
			dos.writeInt(player.getPosition().y);
		} else {
			dos.writeLong(0);
		}
		dos.writeLong(0); // Placeholder for "special cargo"
		dos.writeLong(0); // Placeholder for "special cargo"
		dos.writeLong(0); // Placeholder for "special cargo"
		dos.writeByte(cobra.getMissiles() + (cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.EXTRA_ENERGY_UNIT)) ? 8 : 0));
		int equipment = (cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.LARGE_CARGO_BAY))      ?   1 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_SYSTEM))          ?   2 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.FUEL_SCOOP))          ?   4 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ESCAPE_CAPSULE))      ?   8 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ENERGY_BOMB))         ?  16 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.DOCKING_COMPUTER))    ?  32 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.GALACTIC_HYPERDRIVE)) ?  64 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.RETRO_ROCKETS))       ? 128 : 0);
		dos.writeByte(equipment);
		dos.writeByte(Laser.getLaserValue(cobra, EquipmentStore.PULSE_LASER) + (Laser.getLaserValue(cobra, EquipmentStore.BEAM_LASER) << 4));
		dos.writeByte(Laser.getLaserValue(cobra, EquipmentStore.MINING_LASER) + (Laser.getLaserValue(cobra, EquipmentStore.MILITARY_LASER) << 4));
		equipment = (cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.NAVAL_ENERGY_UNIT)) ? 1 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.CLOAKING_DEVICE)) ? 2 : 0) +
			(cobra.isEquipmentInstalled(EquipmentStore.get().getEquipmentById(EquipmentStore.ECM_JAMMER)) ? 4 : 0);
		dos.writeByte(equipment);
		dos.writeByte(player.isCheater() ? 1 : 0);
		dos.writeByte(0); // Placeholder for "special equipment"
		dos.writeByte(0); // Placeholder for "special equipment"
		dos.writeInt(0); // Placeholder for "special equipment"
		dos.writeByte(0); // Placeholder for number of kills to next "Right On, Commander"-Msg.
		dos.writeShort(0); // Placeholder for number of kills to next "Good Shooting, Commander"-Msg.
		dos.writeInt(player.getKillCount());
		// Dummy String: Deprecated statistics filename
		writeString(dos, "1234567890123456789012");
		dos.writeInt(player.getActiveMissions().size());
		dos.writeInt(player.getCompletedMissions().size());
		for (Mission m: player.getActiveMissions()) {
			dos.writeInt(m.getId());
			dos.write(m.save());
		}
		for (Mission m: player.getCompletedMissions()) {
			dos.writeInt(m.getId());
		}
		for (TradeGood good : TradeGoodStore.get().goods()) {
			if (!good.isSpecialGood()) {
				item = cobra.getInventoryItemByGood(good);
				dos.writeLong(item == null ? 0 : item.getPrice());
			}
		}
		for (TradeGood good : TradeGoodStore.get().goods()) {
			if (!good.isSpecialGood()) {
				item = cobra.getInventoryItemByGood(good);
				dos.writeLong(item == null ? 0 : item.getUnpunished().getWeightInGrams());
			}
		}
	}

	private String determineOldestAutosaveSlot() {
		long oldestDate = 0;
		String oldestFilename = "";
		for (int i = 0; i < 3; i++) {
			String fileName = getAutoSavedFileName(i);
			long date = game.getFileIO().fileLastModifiedDate(fileName);
			// file does not exist
			if (date == 0) {
				return fileName;
			}
			// "date" returns the time passed since 1970.
			// Hence, a smaller value means that the file is older, because it has been saved earlier.
			if (date < oldestDate || oldestDate == 0) {
				oldestDate = date;
				oldestFilename = fileName;
			}
		}
		return oldestFilename;
	}

	private String getAutoSavedFileName(int index) {
		return CommanderData.DIRECTORY_COMMANDER + CommanderData.AUTO_SAVED_COMMANDER_FILENAME +
			(index != 0 ? index : "") + ALITE_COMMANDER_EXTENSION;
	}

	private String determineYoungestAutosaveSlot() {
		long youngestDate = 0;
		String youngestFilename = getAutoSavedFileName(0);
		for (int i = 0; i < 3; i++) {
			String fileName = getAutoSavedFileName(i);
			long date = game.getFileIO().fileLastModifiedDate(fileName);
			if (date == 0) {
				continue;
			}
			// "date" returns the time passed since 1970.
			// Hence, a larger value means that the file is younger, because it has been saved later.
			if (date > youngestDate) {
				youngestDate = date;
				youngestFilename = fileName;
			}
		}
		return youngestFilename;
	}

	private void backupCommander(String oldFileName) {
		if (!game.getFileIO().exists(oldFileName)) {
			return;
		}
		CommanderData info = getQuickCommanderInfo(oldFileName);
		if (info == null || info.getGameTime() <= game.getGameTime()) {
			return;
		}
		String newFileName = generateRandomFilename();
		ByteArrayInputStream bis = null;
		OutputStream fos = null;
		ByteArrayOutputStream bos = null;
		try {
			byte[] commanderData = getCommanderData(oldFileName);
			if (commanderData == null) {
				AliteLog.e("[Alite] backupCommander",
					"Ouch! Couldn't load commander " + oldFileName + ". No changes to current commander were made.");
				return;
			}
			bis = new ByteArrayInputStream(commanderData);
			bos = new ByteArrayOutputStream(1024);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			bis.close();
			commanderData = encrypt(zipBytes(bos.toByteArray()), getKey(newFileName));

			bos = new ByteArrayOutputStream(1024);
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeByte(COMMANDER_FILE_FORMAT_VERSION);
			writeString(dos, info.getName());
			writeString(dos, info.getDockedSystem());
			dos.writeLong(info.getGameTime());
			dos.writeInt(info.getPoints());
			bos.write(info.getRating().ordinal());
			byte[] headerData = encrypt(bos.toByteArray(), getKey(newFileName));

			game.getFileIO().mkDir(CommanderData.DIRECTORY_COMMANDER);
			fos = game.getFileIO().writeFile(newFileName);
			fos.write(headerData.length >> 8);
			fos.write(headerData.length & 255);
			fos.write(headerData);
			fos.write(commanderData);
		} catch (IOException e) {
			AliteLog.e("[Alite] backupCommander", "Error while creating backup.", e);
		} finally {
			closeResource(bis);
			closeResource(fos);
			closeResource(bos);
		}
		AliteLog.d("[Alite] backupCommander", "Copied Commander '" + info.getName() + "'.");
	}

	private void closeResource(Closeable res) {
		try {
			if (res != null) {
				res.close();
			}
		} catch (IOException ignored) { }
	}

	public final void autoSave() throws IOException {
		String fileName = determineOldestAutosaveSlot();
		backupCommander(fileName);
		saveCommander(null, fileName);
	}

	public final void autoLoad() throws IOException {
		String autosaveFilename = determineYoungestAutosaveSlot();
		if (game.getFileIO().exists(autosaveFilename)) {
			loadCommander(autosaveFilename);
		}
	}

	public CommanderData getQuickCommanderInfo(String fileName) {
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(decrypt(
				game.getFileIO().readPartialFileContents(fileName, 2, getHeaderLength(fileName)), getKey(fileName))))) {
			String name = loadVersionAndPlayerName(dis);
			String currentSystem = fileFormatVersion == 1 ? readString(dis, 8).trim() : readStringWithLength(dis);
			long gameTime = dis.readLong();
			int points = dis.readInt();
			Rating rating = Rating.values()[dis.read()];
			return new CommanderData(name, currentSystem, gameTime, points, rating, fileName);
		} catch (IOException e) {
			AliteLog.e("[ALITE] loadCommander", "Error when loading commander " + fileName + ".", e);
		}
		return null;
	}

	public File[] getCommanderFiles() {
		return game.getFileIO().getFiles(CommanderData.DIRECTORY_COMMANDER, ".*\\" + ALITE_COMMANDER_EXTENSION);
	}

	public boolean existsSavedCommander() {
		File[] commanders = getCommanderFiles();
		return commanders != null && commanders.length > 0 &&
			(commanders.length > 1 || !getAutoSavedFileName(0).equals(commanders[0].getName()));
	}

	private String getKey(String fileName) {
		int keyIndex = fileName.hashCode();
		if (keyIndex == Integer.MIN_VALUE) {
			keyIndex = Integer.MAX_VALUE;
		} else {
			keyIndex = Math.abs(keyIndex);
		}
		keyIndex %= keys.length;
		return keys[keyIndex];
	}
}

/*
 * COMPSIS � Computadores e Sistemas Ind. e Com. LTDA<br>
 * Produto $(product_name} - ${product_description}<br>
 *
 * Data de Cria��o: 26/02/2014<br>
 * <br>
 * Todos os direitos reservados.
 */

package br.com.compsis.sicatinstall;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DOCUMENTA��O DA CLASSE <br>
 * ---------------------- <br>
 * FINALIDADE: <br>
 * TODO Definir documenta��o da classe. <br>
 * <br>
 * HIST�RICO DE DESENVOLVIMENTO: <br>
 * 26/02/2014 - @author Lucas Israel - Primeira vers�o da classe. <br>
 * <br>
 * <br>
 * LISTA DE CLASSES INTERNAS: <br>
 */

public class WARUtil {
	/**
	 * Verifica se o arquivo ear est� compactado
	 * 
	 * @param earFile
	 * @return
	 * @throws IOException
	 */
	public static boolean isCompressed(final File earFile) throws IOException {
		if(earFile.isFile()){
			RandomAccessFile raf = new RandomAccessFile(earFile, "r");
			long n = raf.readInt();
			raf.close();
			return n == 0x504B0304;			
		}
		return false;
	}

	/**
	 * Obtem um nome temporario a partir de um arquivo
	 * 
	 * @param zipFile
	 * @param outDir
	 * @return
	 */
	public static File getFolderForExtract(File zipFile, File outDir) {
		String out = new StringBuilder(outDir.getAbsolutePath()).append("\\")
				.append(zipFile.getName()).append("_").toString();
		return new File(out);
	}
	
	/**
	 * Extrai o EAR
	 * @param zipfile
	 * @param outdir
	 */
    public static void extractWAR ( File zipfile , File outdir ) {
		File folderForExtract = getFolderForExtract(zipfile, outdir);
		extract(zipfile, folderForExtract);
		while(!zipfile.delete()) {
			try {
				Thread.sleep(100l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		folderForExtract.renameTo(zipfile);
	}

	/***
	 * Extract zipfile to outdir with complete directory structure
	 * 
	 * @param zipfile
	 *            Input .zip file
	 * @param outdir
	 *            Output directory
	 */
    private static void extract ( File zipfile , File outdir ) {
		try {
			ZipInputStream zin = new ZipInputStream(
					new FileInputStream(zipfile));
			ZipEntry entry;
			String name, dir;
			while ((entry = zin.getNextEntry()) != null) {
				name = entry.getName();
				if (entry.isDirectory()) {
					mkdirs(outdir, name);
					continue;
				}
				/*
				 * this part is necessary because file entry can come before
				 * directory entry where is file located i.e.: /foo/foo.txt
				 * /foo/
				 */
				dir = dirpart(name);
				if (dir != null)
					mkdirs(outdir, dir);

				extractFile(zin, outdir, name);
			}
			zin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final int BUFFER_SIZE = 4096;
	private static void extractFile(ZipInputStream in, File outdir, String name)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(new File(outdir, name)));
		int count = -1;
		while ((count = in.read(buffer)) != -1)
			out.write(buffer, 0, count);
		out.close();
	}

	private static void mkdirs(File outdir, String path) {
		File d = new File(outdir, path);
		if (!d.exists())
			d.mkdirs();
	}

	private static String dirpart(String name) {
		int s = name.lastIndexOf(File.separatorChar);
		return s == -1 ? null : name.substring(0, s);
	}
}

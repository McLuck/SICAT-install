/*
 * COMPSIS � Computadores e Sistemas Ind. e Com. LTDA<br>
 * Produto $(product_name} - ${product_description}<br>
 *
 * Data de Cria��o: 26/02/2014<br>
 * <br>
 * Todos os direitos reservados.
 */

package br.com.compsis.sicatinstall;

/** 
 * DOCUMENTA��O DA CLASSE <br>
 * ---------------------- <br>
 * FINALIDADE: <br>
 * TODO Definir documenta��o da classe. <br>
 * <br>
 * HIST�RICO DE DESENVOLVIMENTO: <br>
 * 26/02/2014 - @author Lucas Israel - Primeira vers�o da classe. <br>
 *<br>
 *<br>
 * LISTA DE CLASSES INTERNAS: <br>
 */

public class Main {

    public static void main ( String[] args ) {
        if ( args.length > 0 ) {
            for ( String argument : args ) {
                System.out.println( argument );
                if ( argument.contains( "=" ) ) {
                    String[] keyValue = argument.split( "=" );
                    String key = keyValue[ 0 ];
                    String value = null;
                    if ( keyValue.length > 1 ) {
                        value = keyValue[ 1 ];
                    } else {
                        value = keyValue[ 0 ];
                    }
                    System.setProperty( key , value );
                } else {
                    System.setProperty( argument , argument );
                }
            }
        }
		PathsSicatUtil.installArtfacts();
	}
}

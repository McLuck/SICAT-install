/*
 * COMPSIS � Computadores e Sistemas Ind. e Com. LTDA<br>
 * Produto $(product_name} - ${product_description}<br>
 *
 * Data de Cria��o: 26/02/2014<br>
 * <br>
 * Todos os direitos reservados.
 */

package br.com.compsis.sicatinstall;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import com.google.common.io.Files;

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

public class PathsSicatUtil {
	public static String PATH_SICAT_HOME = System.getenv("SICAT_HOME");
    public static File FOLDER_SICAT_HOME;
    public static String PATH_DEPLOY_SGAP;
    public static File FOLDER_DEPLOY_SGAP;
    public static String PATH_SICAT_INSTALL;
    public static final String SEPARATOR = File.separator;

    public static OS getOS () {
        String osName = getVariable( "os.name" );
        if ( osName != null && osName.toLowerCase().contains( "mac" ) )
            return OS.MAC;
        return OS.WINDOWS;
    }

    public static String getVariable ( String variable ) {
        String value = System.getProperty( variable );
        if ( value == null )
            value = System.getenv( variable );
        return value;
    }

    public static void definePaths () {
        StringBuilder builder = new StringBuilder();
        PATH_SICAT_INSTALL = getVariable( "SICAT_INSTALL" );
        if ( PATH_SICAT_INSTALL == null ) {
            PATH_SICAT_INSTALL = getVariable( "SICAT_INSTALL" );
        }
        if ( PATH_SICAT_INSTALL == null ) {
            PATH_SICAT_INSTALL = getVariable( "currentDirectory" );
        }
        if ( PATH_SICAT_INSTALL == null ) {
            if ( getOS() == OS.MAC ) {
                builder.setLength( 0 );
                builder.append( getVariable( "user.home" ) ).append( SEPARATOR ).append( "projetos" ).append( SEPARATOR ).append( "legado" );
                PATH_SICAT_INSTALL = builder.toString();
            } else {
                PATH_SICAT_INSTALL = getVariable( "LEGADO" );
            }
        }
        if ( PATH_SICAT_INSTALL == null ) {
            throw new RuntimeException(
                    "Nao foi possivel encontrar a origem do SGAP. Defina variavel de ambiente LEGADO ou informe argumento do programa LEGADO ou parametros do programa LEGADO com o local da raiz do projeto. Caso esteja no MAC, pode mover o projeto para ~/projetos/legado" );
        } else if ( ! PATH_SICAT_INSTALL.endsWith( SEPARATOR ) ) {
            PATH_SICAT_INSTALL += SEPARATOR;
        }


        if ( PATH_SICAT_HOME == null ) {
            if ( getOS() == OS.WINDOWS ) {
                builder.setLength( 0 );
                PATH_SICAT_HOME = builder.append( "d:" ).append( SEPARATOR ).append( "sicat" ).append( SEPARATOR ).append( "app" ).toString();
            } else if ( getOS() == OS.MAC ) {
                builder.setLength( 0 );
                PATH_SICAT_HOME = builder.append( getVariable( "user.home" ) ).append( SEPARATOR ).append( "servers" ).append( SEPARATOR ).append( "sicat" ).append( SEPARATOR ).append( "app" )
                        .toString();
            } else {
                throw new RuntimeException(
                        "Nao foi possivel definir onde esta o diretorio HOME de deploy do JBOSS. Defina argumento ou variavel de ambiente SICAT_HOME ou JBOSS_HOME direcionando para o diretorio APP do jboss 6 ou coloque no caminho D:\\sicat\\app (windows) ou em ~/servers/sicat/app (MAC)" );
            }
        }
        FOLDER_SICAT_HOME = new File( PATH_SICAT_HOME );
        
        builder.setLength( 0 );
        builder.append( PATH_SICAT_HOME );
        if ( ! PATH_SICAT_HOME.endsWith( SEPARATOR ) ) {
            builder.append( SEPARATOR );
        }
        builder.append( "server" ).append( SEPARATOR ).append( "sgap" ).append( SEPARATOR ).append( "deploy" ).append( SEPARATOR ).append( "sgap-sa" );
        PATH_DEPLOY_SGAP = builder.toString();
        FOLDER_DEPLOY_SGAP = new File( PATH_DEPLOY_SGAP );
    }
	/**
	 * Obtem o EAR instalado
	 * @return
	 */
    public static File getDeployedAppWAR () {
		for( File file : FOLDER_DEPLOY_SGAP.listFiles() ){
			if(file.getName().toLowerCase().endsWith(".war")){
                return file;
			}
		}
        return null;
	}
	
	
	/**
	 * Lista todos arquivos pendente para instala��o
	 * @return
	 */
	public static File[] getFilesForInstall(){
        return new File( PATH_SICAT_INSTALL ).listFiles();
	}
	
	public static void installArtfacts() {
        definePaths();
		List<File> jarFiles = new ArrayList<File>();
        installWAR();
        fillJarToDeploy( jarFiles , null );
		deployJarFiles(jarFiles);
		System.out.println("Processo concluido!");
	}

    private static boolean needInstallNewWar () throws IOException {
        if ( getVariable( "-f" ) != null || getVariable( "-F" ) != null ) {
            return true;
        }
        File war = getDeployedAppWAR();
        if(war == null) {
            return true;
        } else if(war.isFile()) {
            if ( WARUtil.isCompressed( war ) ) {
                WARUtil.extractWAR( war , FOLDER_DEPLOY_SGAP );
            } else {
                return true;
            }
        }
        return false;
    }

    public static File findNewWar () {
        File folder = null;
        if ( PATH_SICAT_INSTALL.toLowerCase().endsWith( "legado" + SEPARATOR ) ) {
            folder = new File( new File( PATH_SICAT_INSTALL , "SGAP" ) , "target" );
        } else {
            folder = new File( PATH_SICAT_INSTALL );
        }
        for ( File fileToDeploy : folder.listFiles() ) {
            if ( fileToDeploy.isFile() && fileToDeploy.getName().toLowerCase().endsWith( ".war" ) ) {
                return fileToDeploy;
            }
        }

        return null;
    }

    private static void installWAR () {
        File newWar = null;
        try {
            if ( needInstallNewWar() ) {
                newWar = findNewWar();
                if ( newWar == null ) {
                    throw new RuntimeException( "Nenhum arquivo WAR foi encontrado para deploy no local " + PATH_SICAT_INSTALL );
                }
            } else {
                return;
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        deployWAR( newWar );
	}
	
	public static void deployWAR(final File warFile) {
        File installedWAR = getDeployedAppWAR();
		recursiveDelete(installedWAR);
        System.out.println( new StringBuilder( "Instalando novo WAR: " ).append( warFile.getAbsolutePath() ).toString() );
		installedWAR = new File(FOLDER_DEPLOY_SGAP, warFile.getName());
		try {
            Files.copy( warFile , installedWAR );
            if ( WARUtil.isCompressed( installedWAR ) ) {
                System.out.println( "Arquivo copiado em formato WAR. Iniciando extracao do arquivo." );
                WARUtil.extractWAR( installedWAR , FOLDER_DEPLOY_SGAP );
			}
            System.out.println( "Novo WAR instalado!" );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

    public static void fillJarToDeploy ( List < File > jarFiles , File folder ) {
        if ( getVariable( "-f" ) != null || getVariable( "-F" ) != null ) {
            return;
        }
        if ( folder == null ) {
            String currentDir = getVariable( "currentDir" );
            if ( currentDir == null ) {
                System.err.println(
                        "Nenhum diretorio definido para iniciar a busca por modulos para deploy. Para definir deploy somente de modulos, definir variavel de ambiente, argumento de VM ou parametro do programa chamado currentDir\n" );
            } else {
                folder = new File( currentDir );
            }
        }
        if ( folder != null ) {
            if ( folder.isDirectory() && folder.getName().toLowerCase().equals( "target" ) ) {
                for ( File candidate : folder.listFiles() ) {
                    if ( candidate.isFile() && candidate.getName().endsWith( ".jar" ) && ! candidate.getName().endsWith( "-sources.jar" )
                            && ! candidate.getName().toLowerCase().endsWith( "-tests.jar" ) ) {
                        jarFiles.add( candidate );
                    }
                }
            } else if ( folder.isDirectory() && ! folder.getName().toLowerCase().equals( "src" ) && ! folder.getName().toLowerCase().equals( ".classpath" ) ) {
                for ( File subFiles : folder.listFiles() ) {
                    if ( subFiles.isDirectory() && ! subFiles.getName().toLowerCase().equals( "src" ) ) {
                        fillJarToDeploy( jarFiles , subFiles );
                    }
                }
            }
        }
    }

	public static void deployJarFiles(List<File> jarFiles) {
        File deployedWAR = getDeployedAppWAR();
        File libWar = new File( new File( deployedWAR , "WEB-INF" ) , "lib" );
		List<File> deployeds = new ArrayList<File>();
		deployeds.addAll(Arrays.asList(libWar.listFiles()));
		for (File jar : deployeds ){
			if(jar.getAbsolutePath().equals(libWar.getAbsolutePath())){
				continue;
			}
			ListIterator<File> iterator = jarFiles.listIterator();
			while(iterator.hasNext()){
				File file = iterator.next();
				if(getModuleNameWithoutVersion(jar).equals(getModuleNameWithoutVersion(file))) {
					try {
						recursiveDelete(jar);
						File destination = null;
                        destination = libWar;

						File newJar = new File(destination, file.getName());
                        Files.copy( file , newJar );
						iterator.remove();
                        System.out.println( new StringBuilder( "Artefato alterado: " ).append( jar.getName() ).append( " => " ).append( newJar.getName() ) );
					} catch (IOException e) {
						e.printStackTrace();
					}
				}				
			}			
		}
	}
	
	/**
	 * Apaga o arquivo solicitado. <br>
	 * Caso o arquivo informado seja um diret�rio, efetua a remo��o recursiva das pastas e arquivos.
	 * @param aFile
	 */
	public static void recursiveDelete(final File aFile){
		if(aFile!=null){
			if(aFile.exists()){
				if(aFile.isDirectory()){
					for (File sub : aFile.listFiles()) {
						recursiveDelete(sub);
					}
					aFile.delete();
				} else {
					aFile.delete();
				}				
			}
		}
	}
	
	private static Attributes getAtributesManifest(final File aFile) {
		try {
			JarFile jarFile;
			jarFile = new java.util.jar.JarFile(aFile);
			Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
			jarFile.close();
			return mainAttributes;
		} catch (IOException e) {
			return null;
		}
	}
	
	private static String getModuleType(final File aFile){
		Attributes mainAttributes = getAtributesManifest(aFile);
		if(mainAttributes == null){
			return null;
		}
		String moduleTyoe = mainAttributes.getValue("moduleType");
		return moduleTyoe;
	}
	
	private static String getModuleNameWithoutVersion(final File aFile){
		Attributes mainAttributes = getAtributesManifest(aFile);
		if(mainAttributes !=null ){
			String moduleName = mainAttributes.getValue("module");
			if(moduleName != null){
				return moduleName;
			}			
		}
		
		if(!aFile.getName().contains("-")){
			return aFile.getName();
		}
		String[] names = aFile.getName().split("-");
		StringBuilder stringBuilder = new StringBuilder();
		if(aFile.getName().toUpperCase().contains("SNAPSHOT")){
			for (int i = names.length-1; i >=0 ; i--) {
				if(names[i].toUpperCase().contains("SNAPSHOT")){
					continue;
				}
				if(i < names.length-1){
					for (int j = 0; j <= i -1; j++) {
						stringBuilder.append(names[j]).append("-");
					}				
					break;
				}
			}			
		} else {
			for (int i = names.length-1; i >=0 ; i--) {
				if(i < names.length-1){
					for (int j = 0; j <= i; j++) {
						stringBuilder.append(names[j]).append("-");
					}				
					break;
				}
			}			
		}
		return stringBuilder.toString();
	}
}

import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;
import java.nio.ByteBuffer;


class Matriz
{
  static Object lock = new Object();
  static int N = 8;
  static double[][] A = new double[N][N];
  static double[][] B = new double[N][N];
  static double[][] C = new double[N][N];
 

  // lee del DataInputStream todos los bytes requeridos

  static void read(DataInputStream f,byte[] j,int posicion,int longitud) throws Exception
  {
    while (longitud > 0)
    {
      int n = f.read(j,posicion,longitud);
      posicion += n;
      longitud -= n;
    }
  }

  static class Worker extends Thread
  {
    Socket conexion;

    
    
    Worker(Socket conexion)
    {
      this.conexion = conexion;

    }

    //------------------Codigo del Servidor-------------------------------
    public void run()
    {
        try
        {
          DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
          DataInputStream entrada = new DataInputStream(conexion.getInputStream());
          int calculo_completo=0;//Bandera para mostrar C
          int recibe_nodo;

        recibe_nodo=entrada.readInt();
       

        //Matriz A1
        ByteBuffer a1 = ByteBuffer.allocate(N*(N/2)*8);
        for(int i=0 ; i<N/2 ; i++)
            for(int j=0;j<N;j++)
            a1.putDouble(A[i][j]);

        byte[] matriza1 = a1.array();
        //Matriz A2
        ByteBuffer a2 = ByteBuffer.allocate(N*(N/2)*8);
        for(int i=N/2 ; i<N ; i++)
        for(int j=0;j<N;j++)
            a2.putDouble(A[i][j]);  

        byte[] matriza2 = a2.array();  

        //Matriz B1
        ByteBuffer b1 = ByteBuffer.allocate(N*(N/2)*8);
        for(int i=0;i<N/2;i++)
        for(int j=0;j<N;j++)
            b1.putDouble(B[i][j]);
    
        byte[] matrizb1 = b1.array(); 

        //Matriz B2
        ByteBuffer b2 = ByteBuffer.allocate(N*(N/2)*8);
        for(int i=N/2;i<N;i++)
            for(int j=0;j<N;j++)
            b2.putDouble(B[i][j]);
        
        byte[] matrizb2 = b2.array();  
        
        //-----------switch para enviar matrices dependiendo del nodo--------
          switch(recibe_nodo){
              case 1:
                salida.write(matriza1);
                salida.flush();
                System.out.println("A1 enviado al nodo 1");
                salida.write(matrizb1);
                salida.flush();
                System.out.println("B1 enviado al nodo 1");
                         
                // recibe numeros de C j lo mete i la matriz C/1 A1B1
                byte[] c1 = new byte[(N/2)*(N/2)*8];
                read(entrada,c1,0,(N/2)*(N/2)*8);
                ByteBuffer buf1 = ByteBuffer.wrap(c1);
                for(int i=0 ; i<N/2 ; i++)
                    for(int j=0;j<N/2;j++)
                        C[i][j]=buf1.getDouble();

                System.out.println("C1 recibido del nodo "+recibe_nodo+'\n');     
                break;   

              case 2:
              salida.write(matriza1);
              salida.flush();
              System.out.println("A1 enviado al nodo 2");
              salida.write(matrizb2);
              salida.flush();
              System.out.println("B2 enviado al nodo 2");
    
                // recibe numeros de C j lo mete a la matriz C/2 A1B2
                byte[] c2 = new byte[(N/2)*(N/2)*8];
                read(entrada,c2,0,(N/2)*(N/2)*8);
                ByteBuffer buf2 = ByteBuffer.wrap(c2);
                for(int i=0 ; i<N/2 ; i++)
                    for(int j=N/2;j<N;j++)
                        C[i][j]=buf2.getDouble();

                System.out.println("C2 recibido del nodo "+recibe_nodo+'\n');      
              break; 


              case 3:
              salida.write(matriza2);
              salida.flush();
              System.out.println("A2 enviado al nodo 3");
              salida.write(matrizb1);
              salida.flush();
              System.out.println("B1 enviado al nodo 3");
                
                // recibe numeros de C j lo mete a la matriz C/3 A2B1
                byte[] c3 = new byte[(N/2)*(N/2)*8];
                read(entrada,c3,0,(N/2)*(N/2)*8);
                ByteBuffer buf3 = ByteBuffer.wrap(c3);
                for(int i=N/2 ; i<N ; i++)
                    for(int j=0;j<N/2;j++)
                        C[i][j]=buf3.getDouble();
              
                System.out.println("C3 recibido del nodo "+recibe_nodo+'\n');  
              break; 
                    
              case 4:
              salida.write(matriza2);
              salida.flush();
              System.out.println("A2 enviado al nodo 4");
              salida.write(matrizb2);
              salida.flush();
              System.out.println("B2 enviado al nodo 4");

                // recibe numeros de C j lo mete a la matriz C/3 A2B1
                byte[] c4 = new byte[(N/2)*(N/2)*8];
                read(entrada,c4,0,(N/2)*(N/2)*8);
                ByteBuffer buf4 = ByteBuffer.wrap(c4);
                for(int i=N/2 ; i<N ; i++)
                    for(int j=N/2;j<N;j++)
                        C[i][j]=buf4.getDouble();
                    
             calculo_completo=1;
             System.out.println("C4 recibido del nodo "+recibe_nodo+'\n');  
             break; 

              default:       
              break;

          }

          if(calculo_completo==1){
                //Calculo del checksum
                long checksum=0;
                for( int i=0 ; i<N ; i++ )
                    for( int j=0 ; j<N ; j++ )
                        checksum+=C[i][j]; 
                    
                System.out.println("Checksum: " + checksum);
                
                if(N==4){
                    
                    //Impresión de matriz C/1 A1B2
                    System.out.println("--------------------------");
                    for(int i=0 ; i<N ; i++){
                        for(int j=0;j<N;j++){
                        System.out.print(C[i][j]+", ");
                        }
                        System.out.println();
                        }
                    System.out.println("------MATRIZC C----------\n");
                }
        }

          salida.close();
          entrada.close();
          conexion.close();
        }
        catch (Exception e)
        {
          System.out.println(e.getMessage());
        }
    }
  }
  public static void main(String[] args) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Uso:");
      System.out.println("java PI <nodo>");
      System.exit(0);
    }
   int  nodo = Integer.valueOf(args[0]);
    if (nodo == 0)
    {
        //Inicializando Matrices
               for(int m=0 ; m<N ; m++){
                for(int n=0 ; n<N ; n++){
                    A[m][n]= 2*m+n;
                    B[m][n]= 2*m-n;
                }
            }
            System.out.println("Matrices A y B inicializadas");
    
           //FOR que transpone la matriz B en si misma
            for(int m=0 ; m<N ; m++){
              for(int n=m ; n<N ; n++){
                double temp = B[m][n];
                B[m][n] = B[n][m];
                B[n][m] = temp;
            }
            }
            System.out.println("Matriz B transpuesta");
    
        ServerSocket servidor = new ServerSocket(50000);
        Worker[] w= new Worker[4];
          
        for(int i=0;i<4;i++){
            Socket conexion= servidor.accept();
            w[i]=new Worker(conexion);
            w[i].start();
        }

        for(int i=0;i<4;i++){
            w[i].join();
        }
    
 
    }
    else
    {
    Socket conexion = null;
        for(;;)
          try
          {
              conexion = new Socket("localhost",50000);
              break;
          }
          catch (Exception e)
          {
            Thread.sleep(100);
          }
     
    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
    DataInputStream entrada = new DataInputStream(conexion.getInputStream());

    double[][] matrizA= new double[N/2][N];
    double[][] matrizB = new double[N/2][N];
    double[][] matrizC = new double[N/2][N/2];
    
    //Envia nodo
    salida.writeInt(nodo);

    //Recibe matriz A1 o A2 y lo guarda en la matrizA
	byte[] a = new byte[N*(N/2)*8];
	read(entrada,a,0,N*(N/2)*8);
	ByteBuffer b = ByteBuffer.wrap(a);
	for(int i=0 ; i<N/2 ; i++){
		for(int j=0;j<N;j++){
			matrizA[i][j]=b.getDouble();
		}
    }
    

    // recibe matriz B1 o B2 y lo mete a la matriz B/2
	byte[] c = new byte[N*(N/2)*8];
	read(entrada,c,0,N*(N/2)*8);
	ByteBuffer d = ByteBuffer.wrap(c);
	for(int i=0 ; i<N/2 ; i++){
		for(int j=0;j<N;j++){
			matrizB[i][j]=d.getDouble();
		}
    }

    
    //Calculando C(1,2,3,4)
    for( int i=0 ; i<(N/2) ; i++ )
    for( int j=0 ; j<N/2 ; j++ )
        for( int k=0 ; k<N ; k++ )
            matrizC[i][j]+=matrizA[i][k]*matrizB[j][k];

    //Envio de la matriz C
    ByteBuffer c1 = ByteBuffer.allocate((N/2)*(N/2)*8);
        for(int i=0;i<N/2;i++)
            for(int j=0;j<N/2;j++)
                c1.putDouble(matrizC[i][j]);
            byte[] matrizC1 = c1.array();

            salida.write(matrizC1);
            salida.flush(); 
            System.out.println("Matriz C["+nodo+"] calculada y enviada al servidor\n");

    entrada.close();
    salida.close();
    conexion.close();
    }
  }
}

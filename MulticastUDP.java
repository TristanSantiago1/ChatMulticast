import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class MulticastUDP {

    

    @SuppressWarnings("deprecation")
    public static void main (String[] args) {


        try {
            int PUERTO = 9000;
            byte[] bufferSalida =new  byte[1024];
            String linea;
            Scanner scanner = new Scanner(System.in);    
            String nombre; 
            boolean mensajeando = true;   
           

            System.out.println("Ingresa tu nombre: "); 
            nombre = scanner.nextLine();             
            System.out.println("Ahora puedes enviar mensajes");

            InetAddress grupo = InetAddress.getByName("224.0.0.0");
            MulticastSocket socketMulti = new MulticastSocket(PUERTO);
            socketMulti.joinGroup(grupo);

            HiloLectura hiloN = new HiloLectura(socketMulti, grupo, PUERTO, nombre);
            Thread hilo = new Thread(hiloN);
            hilo.start();

            String entrada;
            bufferSalida = (nombre + " : se ha unido al chat.").getBytes();
            DatagramPacket mensajeSalida = new DatagramPacket(bufferSalida, bufferSalida.length, grupo, PUERTO);
            socketMulti.send(mensajeSalida);

            while (mensajeando) {
                entrada = scanner.nextLine();
                
                if (entrada.equalsIgnoreCase("Adios")) {
                    hiloN.Detener();
                    linea = nombre + ": Ha salido del chat";      
                    mensajeando = false;
                } else {
                    linea = nombre + " :" + entrada;
                }
                bufferSalida = linea.getBytes();
                mensajeSalida = new DatagramPacket(bufferSalida, bufferSalida.length, grupo, PUERTO);
                socketMulti.send(mensajeSalida);
            }
            scanner.close();
            socketMulti.leaveGroup(grupo);
            socketMulti.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

}

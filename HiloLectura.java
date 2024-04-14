import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HiloLectura implements Runnable{

    private MulticastSocket socketMulticast;
    private InetAddress grupoCast;
    private int puerto;
    private boolean ejecutando = true; 
    private String nombre;

    public HiloLectura(MulticastSocket socket, InetAddress grupo, int puerto, String nombre){
        this.socketMulticast = socket;
        this.grupoCast = grupo;
        this.puerto = puerto;
        this.nombre = nombre;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        String linea;
        while (ejecutando) {                          
                DatagramPacket mensajeEntrada = new DatagramPacket(buffer, buffer.length, grupoCast, puerto);               
                try {
                    socketMulticast.receive(mensajeEntrada);
                } catch (IOException e) {}                
                linea = new String(buffer, 0, mensajeEntrada.getLength());
                if (!linea.startsWith(nombre)) {
                    System.out.println(linea);
                }           
        }
    }

    public void Detener(){
        ejecutando = false;
    }

}

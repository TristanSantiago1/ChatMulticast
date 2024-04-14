import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastUDPChatGrafico  extends JFrame{


    public JTextArea areaMensajes;
    public JTextField txfMensaje;
    public JButton btnEnviar;
    public JLabel lblNombre;
    
    static MulticastSocket socketMulticast;
    static boolean ejecutando = true; 
    public static String nombre;
    static int PUERTO = 9000;
    static byte[] bufferSalida =new  byte[1024];
    String linea;    
    static InetAddress grupo;
    static HiloLecturaGrafico hiloLectura;


    public MulticastUDPChatGrafico() {
        setTitle("Chat Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        setSize(screenWidth / 4, screenHeight / 4);
        setLayout(new BorderLayout());
        setResizable(false);

      
        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        areaMensajes.setLineWrap(true);
        JScrollPane messageScroll = new JScrollPane(areaMensajes);
        add(messageScroll, BorderLayout.CENTER);
       
        txfMensaje = new JTextField();
        add(txfMensaje, BorderLayout.NORTH);

        
        btnEnviar = new JButton("Enviar");       
        add(btnEnviar, BorderLayout.EAST);

       
        lblNombre = new JLabel();
        lblNombre.setBorder(new EmptyBorder(10,10,10,10));
        add(lblNombre, BorderLayout.WEST);

        setVisible(true);

        btnEnviar.addActionListener((ActionListener) new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                
                String mensaje = txfMensaje.getText();  
                EnviarMensaje(mensaje);  
            }
        });

        txfMensaje.addKeyListener((KeyListener) new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String mensaje = txfMensaje.getText();  
                    EnviarMensaje(mensaje);  
                }
            }
        });

        addWindowListener((WindowListener) new WindowAdapter() {            
            @Override
            public void windowClosing(WindowEvent e) {
                EnviarMensaje("Adios"); 
            }
        });
        
    }   



    @SuppressWarnings("deprecation")
    
    public static void main(String[] args) {      

         nombre = JOptionPane.showInputDialog("Por favor ingresa tu nombre:");
        try {
            grupo = InetAddress.getByName("224.0.0.0");
            socketMulticast = new MulticastSocket(PUERTO);
            socketMulticast.joinGroup(grupo);           
            bufferSalida = (nombre + " : se ha unido al chat.").getBytes();
            DatagramPacket mensajeSalida = new DatagramPacket(bufferSalida, bufferSalida.length, grupo, PUERTO);
            socketMulticast.send(mensajeSalida);
        } catch (IOException e) {
            e.printStackTrace();
            return; 
        }
        MulticastUDPChatGrafico ventana = new MulticastUDPChatGrafico();       
        ventana.lblNombre.setText(nombre);
        ventana.setVisible(true);     
        hiloLectura = new HiloLecturaGrafico(ventana, socketMulticast, grupo, PUERTO, nombre);
        Thread hilo = new Thread(hiloLectura);
        hilo.start();

    }


    @SuppressWarnings("deprecation")
    public void EnviarMensaje(String entrada) {   
        if(ejecutando){
            areaMensajes.append(entrada+"\n");
            txfMensaje.setText("");
            if (entrada.equalsIgnoreCase("Adios")) {
                linea = nombre + ": Ha salido del chat";   
                ejecutando = false;    
                hiloLectura.Detener();      
            } else {
                linea = nombre + " :" + entrada;
            }
            bufferSalida = linea.getBytes();
            DatagramPacket mensajeSalida = new DatagramPacket(bufferSalida, bufferSalida.length, grupo, PUERTO);
            try {
                socketMulticast.send(mensajeSalida);
                if (!ejecutando) {
                    socketMulticast.leaveGroup(grupo);
                    socketMulticast.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }      
        }              
    }
}


class HiloLecturaGrafico implements Runnable{

    private MulticastUDPChatGrafico multicastUDPChatGrafico;
    private MulticastSocket socket;
    private InetAddress grupo;
    private int Puerto;
    private String nombre;
    private boolean ejecutando = true;

    public HiloLecturaGrafico(MulticastUDPChatGrafico ventana, MulticastSocket socket, InetAddress grupo, int puerto, String nombre){
        this.multicastUDPChatGrafico = ventana;
        this.socket = socket;
        this.grupo = grupo;
        this.Puerto = puerto;
        this.nombre = nombre;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        String linea;
        while (ejecutando) {                          
                DatagramPacket mensajeEntrada = new DatagramPacket(buffer, buffer.length, grupo, Puerto);               
                try {
                    socket.receive(mensajeEntrada);
                } catch (IOException e) {}                
                linea = new String(buffer, 0, mensajeEntrada.getLength());
                if (!linea.startsWith(nombre)) {
                    multicastUDPChatGrafico.areaMensajes.append(linea+"\n");
                }           
        }
    }

    public void Detener(){
        ejecutando = false;
    }
}

